package com.robo4j.socket.http.json;

import com.robo4j.socket.http.util.RoboReflectException;
import com.robo4j.util.StringConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * JsonReader parses valid Json string and create JsonDocument with appropriate
 * structure
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonReader {

	private static final char NEW_LINE_UNIX = '\n';
	private static final char NEW_LINE_MAC = '\r';
	private static final char NEW_TAB = '\t';
	private static final char NEW_SPACE = '\u0020';
	private static final char SPACE = ' ';
	private static final char QUOTATION_MARK = '\u0022';
	private static final char COMMA = '\u002C';
	private static final char DOT = '\u002E';
	private static final char COLON = '\u003A';
	private static final char SQUARE_BRACKET_LEFT = '\u005B';
	private static final char SQUARE_BRACKET_RIGHT = '\u005D';
	private static final char CURLY_BRACKET_LEFT = '\u007B';
	private static final char CURLY_BRACKET_RIGHT = '\u007D';
	private static final char CHARACTER_F = 'f';
	private static final char CHARACTER_T = 't';
	private static final char CHARACTER_N = 'n';
	private static final char CHARACTER_U = 'u';
	private static final char CHARACTER_L = 'l';
	private static final char CHARACTER_E = 'e';
	private static final char CHARACTER_MINUS = '-';
	private static final char CHARACTER_0 = '0';
	private static final char CHARACTER_1 = '1';
	private static final char CHARACTER_2 = '2';
	private static final char CHARACTER_3 = '3';
	private static final char CHARACTER_4 = '4';
	private static final char CHARACTER_5 = '5';
	private static final char CHARACTER_6 = '6';
	private static final char CHARACTER_7 = '7';
	private static final char CHARACTER_8 = '8';
	private static final char CHARACTER_9 = '9';

	private static final Set<Character> WHITE_SPACE_SET = new HashSet<>(
			Arrays.asList(SPACE, NEW_LINE_MAC, NEW_LINE_UNIX, NEW_SPACE, NEW_TAB));
	private static final Set<Character> NUMBER_SET = new HashSet<>(
			Arrays.asList(CHARACTER_MINUS, CHARACTER_0, CHARACTER_1, CHARACTER_2, CHARACTER_3, CHARACTER_4, CHARACTER_5,
					CHARACTER_6,CHARACTER_7, CHARACTER_8, CHARACTER_9, DOT));

	private final char[] jsonChars;
	private ReadType currentRead;
	private Stack<JsonDocumentWrapper> stack = new Stack<>();
	private JsonDocument document;
	private int index;
	private String currentKey;
	private Object currentValue;

	public JsonReader(String json) {
		this.jsonChars = json.toCharArray();
		final char activeChar = getCharSkipWhiteSpace();
		final ReadType readType = getInitialReadType(activeChar);
		document = getNewDocument(readType);
		index++;
	}

	public JsonDocument read() {
		while (index < jsonChars.length) {

			char activeChar = getCharSkipWhiteSpace();

			currentRead = getActualReadType(currentRead, activeChar);

			switch (currentRead) {
			case START_ARRAY:
				// moving elements
				switch (document.getType()) {
				case ARRAY:
					break;
				case OBJECT:
					document = addCurrentDocumentToStackAndGetNew(currentKey, currentRead);
					break;
				default:
					throw new IllegalStateException("not allowed state");
				}
				currentRead = ReadType.START_ARRAY_ELEMENT;
				break;
			case END_ARRAY:
				// moving elements
				if (stack.size() > 0) {
					JsonDocumentWrapper documentWrapper = stack.pop();
					switch (documentWrapper.getDocument().getType()) {
					case ARRAY:
						documentWrapper.getDocument().add(document);
						break;
					case OBJECT:
						currentKey = documentWrapper.getName();
						currentValue = document;
						break;
					default:
						throw new IllegalStateException("not allowed state");

					}
					document = documentWrapper.getDocument();
				}
				break;
			case START_ARRAY_ELEMENT:
				// reading elements
				startArrayElementByActiveChar(activeChar);
				break;
			case END_ARRAY_ELEMENT:
				// moving elements
				currentRead = activeChar == COMMA ? ReadType.START_ARRAY_ELEMENT : ReadType.END_ARRAY;
				break;
			case END_OBJECT:
				// moving elements
				putCurrentKeyValue(currentKey, currentValue);
				if (stack.size() > 0) {
					document = stack.peek().getDocument().getType().equals(JsonDocument.Type.ARRAY)
							? getLastStackElement(true)
							: getLastStackElement(false);
				} else {
					currentRead = ReadType.END_VALUE;
				}
				break;
			case START_KEY:
				// reading element
				currentKey = readString(ReadType.END_KEY);
				break;
			case END_KEY:
				// moving element
				if (activeChar == COLON) {
					currentRead = ReadType.START_VALUE;
				} else {
					throw new JsonException("not expected value after key");
				}
				break;
			case START_VALUE:
				// reading element
				startElementByActiveChar(activeChar);
				break;
			case END_VALUE:
				// moving element
				if (activeChar == COMMA) {
					currentRead = ReadType.START_KEY;
				}
				putCurrentKeyValue(currentKey, currentValue);
				break;
			case ERROR:
				throw new RoboReflectException("not valid json");
			}

			index++;
		}
		return document;
	}

	/**
	 * iterate until not white space character
	 *
	 * @return valid character
	 */
	private char getCharSkipWhiteSpace() {
		char result = jsonChars[index];
		while (WHITE_SPACE_SET.contains(result)) {
			result = jsonChars[++index];
		}
		return result;
	}

	private ReadType getInitialReadType(char activeChar) {
		switch (activeChar) {
		case CURLY_BRACKET_LEFT:
			return ReadType.START_OBJECT;
		case SQUARE_BRACKET_LEFT:
			return ReadType.START_ARRAY;
		default:
			return ReadType.ERROR;
		}
	}

	private JsonDocument addCurrentDocumentToStackAndGetNew(String name, ReadType readType) {
		JsonDocumentWrapper documentWrapper = new JsonDocumentWrapper(name, document);
		stack.push(documentWrapper);
		return getNewDocument(readType);
	}

	private ReadType getActualReadType(ReadType currentRead, final char activeChar) {
		switch (activeChar) {
		case QUOTATION_MARK:
			if (currentRead.equals(ReadType.START_OBJECT)) {
				return ReadType.START_KEY;
			}
			break;
		case CURLY_BRACKET_LEFT:
			switch (currentRead) {
			case START_ARRAY_ELEMENT:
			case START_VALUE:
				--index;
				return currentRead;
			default:
				return ReadType.START_OBJECT;
			}
		case CURLY_BRACKET_RIGHT:
			return ReadType.END_OBJECT;
		case SQUARE_BRACKET_RIGHT:
			return ReadType.END_ARRAY;
		case COMMA:
			if (currentRead.equals(ReadType.END_ARRAY)) {
				return document.getType().equals(JsonDocument.Type.ARRAY) ? ReadType.START_ARRAY : ReadType.END_VALUE;
			}
		default:
			break;

		}
		return currentRead;
	}

	private void startElementByActiveChar(char activeChar) {
		switch (activeChar) {
		case CURLY_BRACKET_LEFT:
			document = addCurrentDocumentToStackAndGetNew(currentKey, ReadType.START_OBJECT);
			currentKey = null;
			break;
		case SQUARE_BRACKET_LEFT:
			document = addCurrentDocumentToStackAndGetNew(currentKey, ReadType.START_ARRAY);
			currentKey = null;
			break;
		case QUOTATION_MARK:
			currentValue = readString(ReadType.END_VALUE);
			break;
		case CHARACTER_F:
		case CHARACTER_T:
			currentValue = readBoolean(activeChar);
			currentRead = ReadType.END_VALUE;
			break;
		case CHARACTER_N:
			currentValue = readNull();
			currentRead = ReadType.END_VALUE;
			break;
		default:
			if (NUMBER_SET.contains(activeChar)) {
				currentValue = readNumber(activeChar);
				currentRead = ReadType.END_VALUE;
				break;
			} else {
				throw new RoboReflectException("wrong json reading: " + activeChar);
			}
		}
	}

	private void startArrayElementByActiveChar(char activeChar) {
		switch (activeChar) {
		case CURLY_BRACKET_LEFT:
			document = addCurrentDocumentToStackAndGetNew(null, ReadType.START_OBJECT);
			break;
		case SQUARE_BRACKET_LEFT:
			document = addCurrentDocumentToStackAndGetNew(null, ReadType.START_ARRAY);
			break;
		case QUOTATION_MARK:
			String elementString = readString(ReadType.END_ARRAY_ELEMENT);
			document.add(elementString);
			break;
		case CHARACTER_F:
		case CHARACTER_T:
			Boolean elementBoolean = readBoolean(activeChar);
			document.add(elementBoolean);
			currentRead = ReadType.END_ARRAY_ELEMENT;
			break;
		case CHARACTER_N:
			currentValue = readNull();
			currentRead = ReadType.END_ARRAY_ELEMENT;
			break;
		default:
			Number elementNumber = readNumber(activeChar);
			document.add(elementNumber);
			currentRead = ReadType.END_ARRAY_ELEMENT;
			break;
		}
	}

	/**
	 *
	 * @param arrayElement
	 *            array element
	 * @return return the proper active element
	 */
	private JsonDocument getLastStackElement(boolean arrayElement) {
		JsonDocumentWrapper documentWrapper = stack.pop();
		if (arrayElement) {
			documentWrapper.getDocument().add(document);
			currentRead = ReadType.END_ARRAY_ELEMENT;
			return documentWrapper.getDocument();
		} else {
			currentKey = documentWrapper.getName();
			currentValue = document;
			currentRead = ReadType.END_VALUE;
			return documentWrapper.getDocument();
		}

	}

	private void putCurrentKeyValue(String key, Object value) {
		document.put(key, value);
		currentKey = null;
		currentValue = null;
	}

	private JsonDocument getNewDocument(ReadType readType) {
		switch (readType) {
		case START_OBJECT:
			currentRead = ReadType.START_OBJECT;
			return new JsonDocument(JsonDocument.Type.OBJECT);

		case START_ARRAY:
			currentRead = ReadType.START_ARRAY_ELEMENT;
			return new JsonDocument(JsonDocument.Type.ARRAY);
		}
		throw new RoboReflectException("new document: " + readType);
	}

	private Number readNumber(char activeCharacter) {
		boolean isInteger = true;
		StringBuilder sb = new StringBuilder();
		do {
			sb.append(activeCharacter);
			if (activeCharacter == DOT) {
				isInteger = false;
			}
			activeCharacter = jsonChars[++index];
		} while (NUMBER_SET.contains(activeCharacter));
		--index;
		if (isInteger) {
			return Integer.valueOf(sb.toString());
		} else {
			return Double.valueOf(sb.toString());
		}
	}

	private Boolean readBoolean(char startCharacter) {
		final StringBuilder sb = new StringBuilder();
		sb.append(startCharacter);
		char activeCharacter = startCharacter;
		while (activeCharacter != CHARACTER_E) {
			activeCharacter = jsonChars[++index];
			sb.append(activeCharacter);
		}

		return Boolean.valueOf(sb.toString());
	}

	private Object readNull() {
		readCheckCurrentCharacterCompare(CHARACTER_U);
		readCheckCurrentCharacterCompare(CHARACTER_L);
		readCheckCurrentCharacterCompare(CHARACTER_L);
		return null;
	}

	private void readCheckCurrentCharacterCompare(char compareCharacter) {
		char ch = jsonChars[++index];
		if (ch != compareCharacter) {
			throw new RoboReflectException("not similar characters");
		}
	}

	private String readString(ReadType endType) {
		StringBuilder sb = new StringBuilder();
		char readChar = jsonChars[++index];
		currentRead = endType;
		if(readChar == QUOTATION_MARK){
			return StringConstants.EMPTY;
		}
		do {
			sb.append(readChar);
			readChar = jsonChars[++index];
		} while (readChar != QUOTATION_MARK);

		return sb.toString();
	}

	private enum ReadType {
		//@formatter:off
		START_ARRAY,
		END_ARRAY,
		START_ARRAY_ELEMENT,
		END_ARRAY_ELEMENT,
		START_OBJECT,
		END_OBJECT,
		START_VALUE,
		END_VALUE,
		START_KEY,
		END_KEY,
		ERROR
		//@formatter:on
	}

}
