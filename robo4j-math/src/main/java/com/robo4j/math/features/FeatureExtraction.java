/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 * 
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.math.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.robo4j.math.geometry.CurvaturePoint2f;
import com.robo4j.math.geometry.Line2f;
import com.robo4j.math.geometry.Point2f;

/**
 * Simple and fast feature extraction from lists of Point2f.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class FeatureExtraction {
	/**
	 * The residual variance of the Lidar Lite (25 mm).
	 */
	private static final double RESIDUAL_VARIANCE = 0.025f;

	/**
	 * Auxiliary constant parameter for segmenting
	 */
	private static final double AUXILIARY_CONSTANT = Math.toRadians(10);

	/**
	 * Length variance constant
	 */
	private static final double Uk = 0.02;

	/**
	 * Minimal samples for a Line2D
	 */
	private static final int MIN_Line2D_SAMPLES = 4;

	/**
	 * Minimal angle deviation for a Line2D
	 */
	private static final float Line2D_ANGLE_THRESHOLD = 0.5f;

	/**
	 * Minimal angle deviation to be part of a possible corner
	 */
	private static final float CURVATURE_THRESHOLD = (float) (Math.PI / 6);

	/**
	 * Minimal angle deviation to be a corner
	 */
	private static final float CORNER_THRESHOLD = (float) (Math.PI / 3);

	/**
	 * Calculates the segments in the scan, using the Borg and Aldon adaptive
	 * break algorithm.
	 * 
	 * @param Point2fs
	 *            the Point2fs to segment.
	 * @param angularResolution
	 *            of the scan in radians.
	 * 
	 * @return the Point2fs broken up into segments.
	 */
	public static List<List<Point2f>> segment(List<Point2f> Point2fs, float angularResolution) {
		List<List<Point2f>> segments = new ArrayList<List<Point2f>>(10);

		Iterator<Point2f> iterator = Point2fs.iterator();
		List<Point2f> currentSegment = new ArrayList<Point2f>();
		segments.add(currentSegment);

		Point2f lastPoint2D = iterator.next();
		currentSegment.add(lastPoint2D);

		while (iterator.hasNext()) {
			Point2f nextPoint2D = iterator.next();
			float delta = nextPoint2D.distance(lastPoint2D);
			double maxRange = segmentMaxRange(lastPoint2D.getRange(), angularResolution);
			if (delta > maxRange) {
				currentSegment = new ArrayList<Point2f>();
				segments.add(currentSegment);
			}
			currentSegment.add(nextPoint2D);
			lastPoint2D = nextPoint2D;
		}
		return segments;
	}

	public static float calculateVectorAngle(Point2f b, Point2f center, Point2f f) {
		if (b.equals(center) || f.equals(center)) {
			return 0;
		}

		double bDeltaX = center.getX() - b.getX();
		double bDeltaY = center.getY() - b.getY();

		double fDeltaX = f.getX() - center.getX();
		double fDeltaY = f.getY() - center.getY();

		return (float) (Math.atan2(fDeltaX, fDeltaY) - Math.atan2(bDeltaX, bDeltaY));
	}

	private static double segmentMaxRange(float lastRange, float angularResolution) {
		return lastRange * Math.sin(angularResolution) / Math.sin(AUXILIARY_CONSTANT - angularResolution) + 3 * RESIDUAL_VARIANCE;
	}

	public static float[] calculateSimpleVectorAngles(List<Point2f> Point2fs) {
		if (Point2fs.size() < 5) {
			return null;
		}

		float[] alphas = new float[Point2fs.size()];
		for (int i = 0; i < Point2fs.size(); i++) {
			Point2f before = i == 0 ? Point2fs.get(0) : Point2fs.get(i - 1);
			Point2f center = Point2fs.get(i);
			Point2f following = i == Point2fs.size() - 1 ? Point2fs.get(i) : Point2fs.get(i + 1);
			alphas[i] = calculateVectorAngle(before, center, following);
		}
		return alphas;
	}

	public static FeatureSet getFeatures(List<Point2f> sample, float angularResolution) {
		return extractFeatures(sample, angularResolution);
	}

	private static FeatureSet extractFeatures(List<Point2f> sample, float angularResolution) {
		List<List<Point2f>> segments = segment(sample, angularResolution);
		List<CurvaturePoint2f> corners = new ArrayList<>();
		List<Line2f> Line2fs = new ArrayList<>();
		for (List<Point2f> Point2fs : segments) {
			if (Point2fs.size() < MIN_Line2D_SAMPLES) {
				continue;
			}
			float[] deltaAngles = calculateSamplePoint2DDeltaAngles(Point2fs);
			if (deltaAngles == null) {
				continue;
			}
			Line2fs.addAll(extractLine2Ds(Point2fs, deltaAngles));
			corners.addAll(extractCorners(Point2fs, deltaAngles));
		}

		FeatureSet result = new FeatureSet(Line2fs, corners);
		return result;
	}

	@SuppressWarnings("unused")
	private static Collection<? extends CurvaturePoint2f> extractCornersOld(List<Point2f> Point2fs, float[] deltaAngles) {
		List<CurvaturePoint2f> corners = new ArrayList<>();
		for (int i = 0; i < deltaAngles.length; i++) {
			if (Math.abs(deltaAngles[i]) > CURVATURE_THRESHOLD) {
				int maxIndex = i;
				float maxPhi = deltaAngles[i];
				int j = i + 1;
				float totalPhi = maxPhi;
				while (j < deltaAngles.length - 1) {
					if (Math.abs(deltaAngles[j]) > CURVATURE_THRESHOLD && Math.signum(deltaAngles[i]) == Math.signum(deltaAngles[j])) {
						totalPhi += deltaAngles[j];
						if (deltaAngles[j] > maxPhi) {
							maxPhi = deltaAngles[j];
							maxIndex = j;
						}
						j++;
					} else {
						i = j;
						break;
					}
				}

				if (Math.abs(totalPhi) > CORNER_THRESHOLD) {
					corners.add(CurvaturePoint2f.fromPoint(Point2fs.get(maxIndex), totalPhi));
				}
			}
		}
		return corners;
	}

	private static Collection<? extends CurvaturePoint2f> extractCorners(List<Point2f> Point2fs, float[] deltaAngles) {
		List<CurvaturePoint2f> corners = new ArrayList<>();
		for (int i = 0; i < deltaAngles.length; i++) {
			if (Math.abs(deltaAngles[i]) > CURVATURE_THRESHOLD) {
				int maxIndex = i;
				float maxPhi = deltaAngles[i];
				float totalPhi = maxPhi;
				int last = Math.min(i + 4, deltaAngles.length);
				for (int k = i + 1; k < last; k++) {
					totalPhi += deltaAngles[k];
					if (deltaAngles[k] > maxPhi) {
						maxPhi = deltaAngles[k];
						maxIndex = k;
					}
					i = k;
				}

				if (Math.abs(totalPhi) > CORNER_THRESHOLD && Math.signum(totalPhi) == Math.signum(maxPhi) && maxIndex - 3 >= 0
						&& maxIndex + 4 < deltaAngles.length) {
					Point2f p = Point2fs.get(maxIndex);
					Point2f b = Point2fs.get(maxIndex - 3);
					Point2f f = Point2fs.get(maxIndex + 3);
					float cornerAlpha = calculateVectorAngle(b, p, f);
					if (cornerAlpha > CORNER_THRESHOLD) {
						corners.add(CurvaturePoint2f.fromPoint(p, cornerAlpha));
					}
				}
			}
		}
		return corners;
	}

	public static float[] calculateSamplePoint2DDeltaAngles(List<Point2f> Point2fs) {
		if (Point2fs.size() < 5) {
			return null;
		}

		float[] alphas = new float[Point2fs.size()];
		for (int i = 0; i < Point2fs.size(); i++) {
			if (i == 0 || i == Point2fs.size() - 1) {
				alphas[i] = 0;
				continue;
			}
			int kb = calculateKB(Point2fs, i);
			int kf = calculateKF(Point2fs, i);
			Point2f before = Point2fs.get(i - kb);
			Point2f center = Point2fs.get(i);
			Point2f following = Point2fs.get(i + kf);
			alphas[i] = calculateVectorAngle(before, center, following);
		}
		return alphas;
	}

	public static int calculateKF(List<Point2f> Point2fs, int Point2DIndex) {
		if (Point2DIndex >= Point2fs.size() - 1) {
			return 0;
		}
		double length = 0;
		double distance = 0;
		Point2f startPoint2D = Point2fs.get(Point2DIndex);
		int i = Point2DIndex;
		while (i < Point2fs.size() - 1) {
			length += Point2fs.get(i + 1).distance(Point2fs.get(i));
			distance = Point2fs.get(i + 1).distance(startPoint2D);
			if ((length - Uk) >= distance) {
				break;
			}
			i++;
		}
		return i - Point2DIndex;
	}

	public static int calculateKB(List<Point2f> Point2fs, int Point2DIndex) {
		if (Point2DIndex < 1) {
			return 0;
		}
		float length = 0;
		float distance = 0;
		Point2f startPoint2D = Point2fs.get(Point2DIndex);
		int i = Point2DIndex;
		while (i > 0) {
			length += Point2fs.get(i - 1).distance(Point2fs.get(i));
			distance = Point2fs.get(i - 1).distance(startPoint2D);
			if ((length - Uk) >= distance) {
				break;
			}
			i--;
		}
		return Point2DIndex - i;
	}

	public static final void main(String[] args) {
		Point2f b = Point2f.fromPolar(18, 18);
		Point2f center = Point2f.fromPolar(19, 19);
		Point2f f = Point2f.fromPolar(20, 20);
		float radians = calculateVectorAngle(b, center, f);

		System.out.println("Vec angle: " + Math.toDegrees(radians) + " radians: " + radians);
	}

	private static List<Line2f> extractLine2Ds(List<Point2f> Point2fs, float[] deltaAngles) {
		List<Line2f> Line2fs = new ArrayList<>();
		for (int i = 0; i < deltaAngles.length - MIN_Line2D_SAMPLES;) {
			while (i < deltaAngles.length - 1 && Math.abs(deltaAngles[i]) > Line2D_ANGLE_THRESHOLD) {
				i++;
			}
			int j = i;
			while (j < deltaAngles.length - 2 && (Math.abs(deltaAngles[j]) <= Line2D_ANGLE_THRESHOLD)) {
				j++;
			}
			if (j - i - 1 >= MIN_Line2D_SAMPLES) {
				Line2fs.add(new Line2f(Point2fs.get(i), Point2fs.get(j)));
			}
			i = j;
		}
		return Line2fs;
	}

	public static float getAngularResolution(List<Point2f> Point2fs) {
		return Point2fs.get(1).getAngle() - Point2fs.get(0).getAngle();
	}
}
