/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.util;

import static com.robo4j.util.Utf8Constant.UTF8_QUOTATION_MARK;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class JsonElementStringBuilder {

	private StringBuilder sb = new StringBuilder();

	private JsonElementStringBuilder() {
	}

	public static JsonElementStringBuilder Builder() {
		return new JsonElementStringBuilder();
	}

	public JsonElementStringBuilder addQuotation(Object element) {
		addQuotationElement(element);
		return this;
	}

	public JsonElementStringBuilder addQuotationWithDelimiter(Object delimiter, Object element) {
		addQuotation(element);
		sb.append(delimiter);
		return this;
	}

	public JsonElementStringBuilder add(Object element) {
		sb.append(element);
		return this;
	}

	public JsonElementStringBuilder addWithDelimiter(Object delimiter, Object element) {
		add(element);
		sb.append(delimiter);
		return this;
	}

	public String build() {
		return sb.toString();
	}

	private void addQuotationElement(Object element) {
		sb.append(UTF8_QUOTATION_MARK).append(element).append(UTF8_QUOTATION_MARK);
	}
}
