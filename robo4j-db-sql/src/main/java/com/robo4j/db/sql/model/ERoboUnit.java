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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.db.sql.model;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "ROBO_UNIT")
public class ERoboUnit implements ERoboEntity<Long> {

	private Long id;
	private String uid;
	private String config;
	private ERoboUnit parent;
	private List<ERoboUnit> parts;
	private List<ERoboPoint> points;

	public ERoboUnit() {
		parts = new ArrayList<>();
		points = new ArrayList<>();
	}

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "UID")
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@Column(name = "CONFIG")
	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "PARENT_ID")
	public ERoboUnit getParent() {
		return parent;
	}

	public void setParent(ERoboUnit parent) {
		this.parent = parent;
	}

	@OneToMany(mappedBy = "parent")
	public List<ERoboUnit> getParts() {
		return parts;
	}

	public void setParts(List<ERoboUnit> parts) {
		this.parts = parts;
	}

	public void addPart(ERoboUnit unit) {
		this.parts.add(unit);
	}

	public void addParts(List<ERoboUnit> units) {
		this.parts.addAll(units);
	}

	@OneToMany(mappedBy = "unit")
	public List<ERoboPoint> getPoints() {
		return points;
	}

	public void setPoints(List<ERoboPoint> points) {
		this.points = points;
	}

	public void addPoint(ERoboPoint point) {
		this.points.add(point);
	}

	public void addPoints(List<ERoboPoint> points) {
		this.points.addAll(points);
	}

	@Override
	public String toString() {
		return "ERoboUnit{" + "id=" + id + ", uid='" + uid + '\'' + ", config='" + config + '\'' + ", parent=" + parent
				+ ", parts=" + parts + ", points=" + points + '}';
	}
}
