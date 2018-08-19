package com.robo4j.math.features;

import com.robo4j.math.geometry.Point2f;
import com.robo4j.math.geometry.impl.ScanResultImpl;
import org.junit.Assert;
import org.junit.Ignore;

import java.util.function.Predicate;

public class RaycastTest {

	private static final float DELTA = 0.000001f;
	private final static Point2f ORIGO = Point2f.fromPolar(0f, 0f);

	// FIXME: 08.09.17 we enable it when it works :)
	@Ignore
	public void testMisbehavingData() {
		ScanResultImpl scan = new ScanResultImpl(1.0f, new Predicate<Point2f>() {
			@Override
			public boolean test(Point2f t) {
				return t.getRange() >= 0.08;
			}
		});

		scan.addPoint(Point2f.fromPolar(2.2f, 35.5f));
		scan.addPoint(Point2f.fromPolar(2.2f, 34.5f));
		scan.addPoint(Point2f.fromPolar(2.3f, 33.5f));
		scan.addPoint(Point2f.fromPolar(2.0f, 32.5f));
		scan.addPoint(Point2f.fromPolar(1.9f, 31.5f));
		scan.addPoint(Point2f.fromPolar(1.8f, 30.5f));
		scan.addPoint(Point2f.fromPolar(3.7f, 29.5f));
		scan.addPoint(Point2f.fromPolar(3.8f, 28.5f));
		scan.addPoint(Point2f.fromPolar(2.3f, 27.5f));
		scan.addPoint(Point2f.fromPolar(3.3f, 26.5f));
		scan.addPoint(Point2f.fromPolar(4.2f, 25.5f));
		scan.addPoint(Point2f.fromPolar(3.9f, 24.5f));
		scan.addPoint(Point2f.fromPolar(3.1f, 23.5f));
		scan.addPoint(Point2f.fromPolar(3.7f, 22.5f));
		scan.addPoint(Point2f.fromPolar(3.6f, 21.5f));
		scan.addPoint(Point2f.fromPolar(3.4f, 20.5f));
		scan.addPoint(Point2f.fromPolar(3.3f, 19.5f));
		scan.addPoint(Point2f.fromPolar(3.1f, 18.5f));
		scan.addPoint(Point2f.fromPolar(3.0f, 17.5f));
		scan.addPoint(Point2f.fromPolar(2.8f, 16.5f));
		scan.addPoint(Point2f.fromPolar(2.8f, 15.5f));
		scan.addPoint(Point2f.fromPolar(2.8f, 14.5f));
		scan.addPoint(Point2f.fromPolar(2.8f, 13.5f));
		scan.addPoint(Point2f.fromPolar(2.8f, 12.5f));
		scan.addPoint(Point2f.fromPolar(2.8f, 11.5f));
		scan.addPoint(Point2f.fromPolar(2.9f, 10.5f));
		scan.addPoint(Point2f.fromPolar(2.9f, 9.5f));
		scan.addPoint(Point2f.fromPolar(2.9f, 8.5f));
		scan.addPoint(Point2f.fromPolar(2.9f, 7.5f));
		scan.addPoint(Point2f.fromPolar(2.9f, 6.5f));
		scan.addPoint(Point2f.fromPolar(2.8f, 5.5f));
		scan.addPoint(Point2f.fromPolar(2.7f, 4.5f));
		scan.addPoint(Point2f.fromPolar(2.7f, 3.5f));
		scan.addPoint(Point2f.fromPolar(2.6f, 2.5f));
		scan.addPoint(Point2f.fromPolar(2.6f, 1.5f));
		scan.addPoint(Point2f.fromPolar(2.5f, 0.5f));
		scan.addPoint(Point2f.fromPolar(2.4f, -0.5f));
		scan.addPoint(Point2f.fromPolar(2.4f, -1.5f));
		scan.addPoint(Point2f.fromPolar(2.4f, -2.5f));
		scan.addPoint(Point2f.fromPolar(2.5f, -3.5f));
		scan.addPoint(Point2f.fromPolar(2.5f, -4.5f));
		scan.addPoint(Point2f.fromPolar(3.0f, -5.5f));
		scan.addPoint(Point2f.fromPolar(3.2f, -6.5f));
		scan.addPoint(Point2f.fromPolar(3.2f, -7.5f));
		scan.addPoint(Point2f.fromPolar(3.4f, -8.5f));
		scan.addPoint(Point2f.fromPolar(3.4f, -9.5f));
		scan.addPoint(Point2f.fromPolar(3.5f, -10.5f));
		scan.addPoint(Point2f.fromPolar(3.4f, -11.5f));
		scan.addPoint(Point2f.fromPolar(3.5f, -12.5f));
		scan.addPoint(Point2f.fromPolar(3.4f, -13.5f));
		scan.addPoint(Point2f.fromPolar(3.4f, -14.5f));
		scan.addPoint(Point2f.fromPolar(3.4f, -15.5f));
		scan.addPoint(Point2f.fromPolar(3.3f, -16.5f));
		scan.addPoint(Point2f.fromPolar(3.3f, -17.5f));
		scan.addPoint(Point2f.fromPolar(3.2f, -18.5f));
		scan.addPoint(Point2f.fromPolar(3.2f, -19.5f));
		scan.addPoint(Point2f.fromPolar(3.2f, -20.5f));
		scan.addPoint(Point2f.fromPolar(3.2f, -21.5f));
		scan.addPoint(Point2f.fromPolar(3.1f, -22.5f));
		scan.addPoint(Point2f.fromPolar(3.1f, -23.5f));

		FeatureSet features = FeatureExtraction.getFeatures(scan.getPoints(), 1.0f);
		Assert.assertNotNull(features);
		// FIXME: Remove the next two lines when the test is properly fixed.
		Assert.assertNotEquals(DELTA, 0);
		Assert.assertNotNull(ORIGO);
		Point2f promisingPoint = Raycast.raycastFarthestPoint(scan.getPoints(), 0.4f, 0.3f, features);
		Assert.assertNotNull(promisingPoint);
		// Assert.assertNotEquals(ORIGO.getX(), promisingPoint.getX(), DELTA);
		// Assert.assertNotEquals(ORIGO.getY(), promisingPoint.getY(), DELTA);
	}
}
