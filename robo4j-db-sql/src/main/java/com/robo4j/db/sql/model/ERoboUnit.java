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

package com.robo4j.db.sql.model;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "robo_unit")
public class ERoboUnit implements ERoboEntity<Long> {

	private Long id;
	private ZonedDateTime createdOn;
	private ZonedDateTime updatedOn;
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
	@Column(name = "id")
	@SequenceGenerator(name = "robo_unit_generator", sequenceName = "robo_unit_sequence", allocationSize = 1)
	@GeneratedValue(generator = "robo_unit_generator")
	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@CreationTimestamp
	@Column(name = "created_on", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@Override
	public ZonedDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(ZonedDateTime createdOn) {
		this.createdOn = createdOn;
	}

	@UpdateTimestamp
	@Column(name = "updated_on", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@Override
	public ZonedDateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(ZonedDateTime updateddOn) {
		this.updatedOn = updateddOn;
	}

	@Column(name = "uid")
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@Column(name = "config")
	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "parent_id")
	public ERoboUnit getParent() {
		return parent;
	}

	public void setParent(ERoboUnit parent) {
		this.parent = parent;
	}

	@OneToMany(mappedBy = "parent", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
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

	@OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
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
		return "ERoboUnit{" + "id=" + id + ", createdOn=" + createdOn + ", updatedOn=" + updatedOn + ", uid='" + uid
				+ '\'' + ", config='" + config + '\'' + ", parent=" + parent + ", parts=" + parts + ", points=" + points
				+ '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ERoboUnit))
			return false;

		ERoboUnit roboUnit = (ERoboUnit) o;

		if (id != null ? !id.equals(roboUnit.id) : roboUnit.id != null)
			return false;
		if (createdOn != null ? !createdOn.equals(roboUnit.createdOn) : roboUnit.createdOn != null)
			return false;
		if (updatedOn != null ? !updatedOn.equals(roboUnit.updatedOn) : roboUnit.updatedOn != null)
			return false;
		if (uid != null ? !uid.equals(roboUnit.uid) : roboUnit.uid != null)
			return false;
		if (config != null ? !config.equals(roboUnit.config) : roboUnit.config != null)
			return false;
		if (parent != null ? !parent.equals(roboUnit.parent) : roboUnit.parent != null)
			return false;
		if (parts != null ? !parts.equals(roboUnit.parts) : roboUnit.parts != null)
			return false;
		return points != null ? points.equals(roboUnit.points) : roboUnit.points == null;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
		result = 31 * result + (updatedOn != null ? updatedOn.hashCode() : 0);
		result = 31 * result + (uid != null ? uid.hashCode() : 0);
		result = 31 * result + (config != null ? config.hashCode() : 0);
		result = 31 * result + (parent != null ? parent.hashCode() : 0);
		result = 31 * result + (parts != null ? parts.hashCode() : 0);
		result = 31 * result + (points != null ? points.hashCode() : 0);
		return result;
	}
}
