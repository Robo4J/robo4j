/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This RoboRequestDynamicFactory.java  is part of robo4j.
 * module: robo4j-core
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.request;

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ResponseAttributeDTO;
import com.robo4j.socket.http.dto.ResponseDecoderUnitDTO;
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import com.robo4j.socket.http.units.CodecRegistry;
import com.robo4j.socket.http.units.HttpServerUnit;
import com.robo4j.socket.http.units.ServerPathConfig;
import com.robo4j.socket.http.units.SocketDecoder;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.ReflectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Dynamically configurable request factory
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboRequestFactory implements DefaultRequestFactory<Object> {
	private static final List<HttpMethod> GET_POST_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.POST);
	private final CodecRegistry codecRegistry;

	public RoboRequestFactory(final CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	/**
	 * Generic robo context overview. It returns all units registered into the context including system id.
	 * The 1st position is reserved for the system
	 *
	 * @param context
	 *            robo context
	 * @return descripton of desired context
	 */
	@Override
	public Object processGet(RoboContext context) {
		if (!context.getUnits().isEmpty()) {

			final List<ResponseUnitDTO> unitList = context.getUnits().stream()
					.map(u -> new ResponseUnitDTO(u.getId(), u.getState())).collect(Collectors.toList());
			unitList.add(0, new ResponseUnitDTO(context.getId(), context.getState()));
			return JsonUtil.toJsonArray(unitList);
		} else {
			SimpleLoggingUtil.error(getClass(), "internal error: no units available");
		}
		return null;
	}

	// FIXME correct available methods according to the configuration
	@Override
	@SuppressWarnings("unchecked")
	public Object processGet(ServerPathConfig pathConfig) {
		final RoboReference<?> unitRef = pathConfig.getRoboUnit();
		final SocketDecoder<?, ?> decoder = codecRegistry.getDecoder(unitRef.getMessageType());

		if(unitRef.getMessageType().equals(Object.class) || decoder == null){
			 List<ResponseAttributeDTO> attrList = unitRef.getKnownAttributes().stream()
					 .map(d -> {
						 try {
							 Object val = unitRef.getAttribute(d).get();
							 ResponseAttributeDTO attributeDTO = new ResponseAttributeDTO();
							 attributeDTO.setId(d.getAttributeName());
							 attributeDTO.setType(d.getAttributeType().getTypeName());
							 attributeDTO.setValue(String.valueOf(val));

							 if(d.getAttributeName().equals(HttpServerUnit.ATTR_PATHS)){
								 attributeDTO.setType("java.util.ArrayList");
							 }
							 return attributeDTO;


						 } catch (InterruptedException | ExecutionException e) {
							 SimpleLoggingUtil.error(getClass(), e.getMessage());
							 return null;
						 }
					 })
					 .filter(Objects::nonNull)
					 .collect(Collectors.toList());
			 return JsonUtil.toJsonArrayServer(attrList);

		} else {
			final ResponseDecoderUnitDTO result = new ResponseDecoderUnitDTO();
			result.setId(unitRef.getId());
			result.setCodec(decoder.getDecodedClass().getName());
			result.setMethods(GET_POST_METHODS);
			return ReflectUtils.createJson(result);
		}


	}

	@Override
	public Object processServerGet(ServerPathConfig pathConfig) {
		final ResponseDecoderUnitDTO result = new ResponseDecoderUnitDTO();
		return ReflectUtils.createJson(result);
	}

	/**
	 * currently is supported POST message in JSON format
	 *
	 * example: { "value" : "move" }
	 *
	 * @param unitReference
	 *            desired unit
	 * @param message
	 *            string message
	 * @return processed object
	 */
	@Override
	public Object processPost(final RoboReference<?> unitReference, final String message) {
		final SocketDecoder<Object, ?> decoder = codecRegistry.getDecoder(unitReference.getMessageType());
		return decoder != null ? decoder.decode(message) : null;
	}

}
