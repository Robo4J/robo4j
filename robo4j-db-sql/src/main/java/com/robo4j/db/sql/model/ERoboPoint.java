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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@Entity
@Table(name = "ROBO_POINT")
public class ERoboPoint implements ERoboEntity<Long> {

	private Long id;
	private ERoboUnit unit;
	private String valueType;
	private String values;

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

	@ManyToOne(fetch = FetchType.EAGER)
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
		return "ERoboPoint{" + "id=" + id + ", valueType='" + valueType + '\'' + ", values='" + values + '\'' + '}';
	}
}
