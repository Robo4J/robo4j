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

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@Entity
@Table(name = "ROBO_POINT")
public class ERoboPoint implements ERoboEntity<Long> {

	private Long id;
	private ZonedDateTime createdOn;
	private ZonedDateTime updatedOn;
	private ERoboUnit unit;
	private String valueType;
	private String values;

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = "robo_point_generator", sequenceName = "robo_point_sequence", allocationSize = 1)
	@GeneratedValue(generator = "robo_point_generator")
	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@CreationTimestamp
	@Column(name = "CREATED_ON", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@Override
	public ZonedDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(ZonedDateTime createdOn) {
		this.createdOn = createdOn;
	}

	@UpdateTimestamp
	@Column(name = "UPDATED_ON", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@Override
	public ZonedDateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(ZonedDateTime updateddOn) {
		this.updatedOn = updateddOn;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROBO_UNIT_ID")
	public ERoboUnit getUnit() {
		return unit;
	}

	public void setUnit(ERoboUnit unit) {
		this.unit = unit;
	}

	@Column(name = "VALUE_TYPE")
	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	@Column(name = "VALUES")
	public String getValues() {
		return values;
	}

	public void setValues(String values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "ERoboPoint{" + "id=" + id + ", createdOn=" + createdOn + ", updatedOn=" + updatedOn + ", valueType='"
				+ valueType + '\'' + ", values='" + values + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ERoboPoint))
			return false;

		ERoboPoint roboPoint = (ERoboPoint) o;

		if (id != null ? !id.equals(roboPoint.id) : roboPoint.id != null)
			return false;
		if (createdOn != null ? !createdOn.equals(roboPoint.createdOn) : roboPoint.createdOn != null)
			return false;
		if (updatedOn != null ? !updatedOn.equals(roboPoint.updatedOn) : roboPoint.updatedOn != null)
			return false;
		if (unit != null ? !unit.equals(roboPoint.unit) : roboPoint.unit != null)
			return false;
		if (valueType != null ? !valueType.equals(roboPoint.valueType) : roboPoint.valueType != null)
			return false;
		return values != null ? values.equals(roboPoint.values) : roboPoint.values == null;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
		result = 31 * result + (updatedOn != null ? updatedOn.hashCode() : 0);
		result = 31 * result + (unit != null ? unit.hashCode() : 0);
		result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
		result = 31 * result + (values != null ? values.hashCode() : 0);
		return result;
	}
}
