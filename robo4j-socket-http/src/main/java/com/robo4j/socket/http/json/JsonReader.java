package com.robo4j.socket.http.json;

import com.robo4j.socket.http.util.RoboReflectException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
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
	private static final char CHARACTER_E = 'e';
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
	private static final Set<Character> NUMBER_SET = new HashSet<>(Arrays.asList(CHARACTER_0, CHARACTER_1, CHARACTER_2,
			CHARACTER_3, CHARACTER_4, CHARACTER_5, CHARACTER_6, CHARACTER_7, CHARACTER_8, CHARACTER_9));

	private final String json;
	private ReadType currentRead;
	private Stack<JsonDocument> stack = new Stack<>();
	private JsonDocument document;
	private String documentKey;
	private int index = 0;
	private String currentKey;
	private Object currentValue;
	private int depth = 0;

	JsonReader(String json) {
		this.json = json;
	}

	private char getCharSkipWhiteSpace() {
		char result = json.charAt(index);
		if (WHITE_SPACE_SET.contains(result) && (currentRead == null || currentRead.equals(ReadType.END_KEY)
				|| currentRead.equals(ReadType.START_VALUE) || currentRead.equals(ReadType.END_VALUE)
				|| currentRead.equals(ReadType.START_KEY) || currentRead.equals(ReadType.START_OBJECT)
				|| currentRead.equals(ReadType.END_ARRAY_ELEMENT))) {
			while (WHITE_SPACE_SET.contains(result)) {
				result = json.charAt(++index);
			}
		}

		return result;
	}

	private ReadType getReadType(char activeCharacter) {
		switch (activeCharacter) {
		case SQUARE_BRACKET_LEFT:
			return ReadType.START_ARRAY;
		case SQUARE_BRACKET_RIGHT:
			return ReadType.END_ARRAY;
		case CURLY_BRACKET_LEFT:
			return ReadType.START_OBJECT;
		case CURLY_BRACKET_RIGHT:
			return ReadType.END_OBJECT;
		}
		return null;
	}

	private void readArrayElement(char activeChar) {
		switch (activeChar) {
		case QUOTATION_MARK:
			activeChar = json.charAt(++index);
			String element = readString(ReadType.END_ARRAY_ELEMENT, activeChar);
			document.add(element);
			break;
		case CURLY_BRACKET_LEFT:
			currentRead = ReadType.START_OBJECT;
			stack.push(document);
			document = getNewDocument(currentRead);
			currentKey = null;
			currentValue = null;
			index--;
			break;
		default:
			if (NUMBER_SET.contains(activeChar)) {
				Object number = readNumber(activeChar);
				currentRead = ReadType.END_ARRAY_ELEMENT;
				document.add(number);
			} else {
				currentRead = ReadType.END_ARRAY;
			}
			break;

		}
	}

	private ReadType getActualReadType(ReadType currentRead, char activeChar) {
		return null;
	}

	public void read2() {
		char activeChar = getCharSkipWhiteSpace();
		currentRead = getActualReadType(currentRead, activeChar);
		while (index < json.length()) {
			activeChar = getCharSkipWhiteSpace();
			currentRead = getActualReadType(currentRead, activeChar);

			switch (currentRead) {

			case START_ARRAY:
				break;
			case END_ARRAY:
				break;
			case START_ARRAY_ELEMENT:
				break;
			case END_ARRAY_ELEMENT:
				break;
			case START_OBJECT:
				break;
			case END_OBJECT:
				break;
			case START_DOCUMENT:
				document = getNewDocument(currentRead);
				break;
			case END_DOCUMENT:
				break;
			case START_KEY:
				break;
			case END_KEY:
				break;
			case START_VALUE:
				break;
			case END_VALUE:
				break;
			}

			index++;
		}
	}

	public void read() {
		while (index < json.length()) {
			char activeChar = getCharSkipWhiteSpace();
			ReadType readType = getReadType(activeChar);

			// create new document
			if (readType != null && document == null && depth == 0) {
				document = getNewDocument(readType);
				index++;
				activeChar = getCharSkipWhiteSpace();
				if (activeChar == QUOTATION_MARK) {
					currentRead = ReadType.START_KEY;
				}
			}

			if (currentRead == ReadType.START_OBJECT && activeChar == CURLY_BRACKET_LEFT) {
				currentRead = ReadType.START_KEY;
			} else if (currentRead == ReadType.START_ARRAY) {
				currentRead = ReadType.START_ARRAY_ELEMENT;
				readArrayElement(activeChar);
			} else if (currentRead == ReadType.END_ARRAY_ELEMENT) {
				if (activeChar == COMMA) {
					index++;
					activeChar = getCharSkipWhiteSpace();
					readArrayElement(activeChar);
				} else {
					currentRead = ReadType.END_ARRAY;
				}
			} else if (currentRead == ReadType.END_ARRAY) {
				currentValue = document;
				document = stack.pop();
				currentRead = ReadType.END_VALUE;
				index--;

			} else if (currentRead == ReadType.START_KEY) {
				activeChar = json.charAt(++index);
				currentKey = readString(ReadType.END_KEY, activeChar);
			} else if (currentRead == ReadType.END_KEY && activeChar == COLON) {
				currentRead = ReadType.START_VALUE;
			} else if (currentRead == ReadType.START_VALUE) {
				switch (activeChar) {
				case QUOTATION_MARK:
					activeChar = json.charAt(++index);
					currentValue = readString(ReadType.END_VALUE, activeChar);
					break;
				case CURLY_BRACKET_LEFT:
					System.out.println("read object");
					documentKey = currentKey;
					JsonDocument documentElement = getNewDocument(readType);
					stack.push(document);
					document = documentElement;
					currentRead = ReadType.START_KEY;
					break;
				case SQUARE_BRACKET_LEFT:
					currentRead = ReadType.START_ARRAY;
					stack.push(document);
					documentKey = currentKey;
					document = getNewDocument(currentRead);
					break;
				case CHARACTER_F:
				case CHARACTER_T:
					currentValue = readBoolean(activeChar);
					currentRead = ReadType.END_VALUE;
					break;
				default:
					if (NUMBER_SET.contains(activeChar)) {
						currentValue = readNumber(activeChar);
						currentRead = ReadType.END_VALUE;
					} else {
						throw new RoboReflectException("wrong json reading: " + activeChar);
					}
				}

			} else if (document != null) {
				if (currentRead == ReadType.END_VALUE) {
					if (currentKey == null) {
						currentKey = documentKey;
					}
					document.put(currentKey, currentValue);
					currentKey = null;
					currentValue = null;
					if (activeChar == COMMA) {
						currentRead = ReadType.START_KEY;
					} else {
						currentRead = ReadType.END_OBJECT;
					}
				} else if (currentRead == ReadType.END_OBJECT) {
					if (document.isArrays()) {
						currentRead = ReadType.END_ARRAY;
					} else if (activeChar == COMMA) {
						currentRead = ReadType.START_OBJECT;
						JsonDocument documentElement = document;
						document = stack.pop();
						if (document.isArrays()) {
							document.add(documentElement);
							stack.push(document);
							document = getNewDocument(currentRead);
						}
					} else {
						if (documentKey != null) {
							currentKey = documentKey;
							documentKey = null;
						}
						currentValue = document;
						document = stack.pop();
						activeChar = getCharSkipWhiteSpace();
						if (document.isArrays() && activeChar == SQUARE_BRACKET_RIGHT) {
							document.add(currentValue);
							currentValue = null;
							currentRead = ReadType.END_ARRAY;
						} else if (document != null && activeChar == CURLY_BRACKET_RIGHT) {
							document.put(currentKey, currentValue);
							currentRead = ReadType.END_OBJECT;
						} else {
							currentRead = ReadType.END_VALUE;
						}
					}
				}
			}

			index++;
		}
	}

	private JsonDocument getNewDocument(ReadType readType) {
		switch (readType) {
		case START_OBJECT:
			currentRead = ReadType.START_OBJECT;
			return new JsonDocument(JsonDocument.Type.OBJECT);

		case START_ARRAY:
			currentRead = ReadType.START_ARRAY;
			return new JsonDocument(JsonDocument.Type.ARRAY);
		}
		throw new RoboReflectException("new document: " + readType);
	}

	public JsonDocument getDocument() {
		return document;
	}

	private Object readNumber(char startCharacter) {
		boolean isInteger = true;
		StringBuilder sb = new StringBuilder();
		char activeCharacter = startCharacter;
		do {
			sb.append(activeCharacter);
			if (activeCharacter == DOT) {
				isInteger = false;
			}
			activeCharacter = json.charAt(++index);

		} while (NUMBER_SET.contains(activeCharacter) || activeCharacter == DOT);
		--index;
		if (isInteger) {
			return Integer.valueOf(sb.toString());
		} else {
			return Double.valueOf(sb.toString());
		}
	}

	private Boolean readBoolean(char startCharacter) {
		StringBuilder sb = new StringBuilder();
		char activeCharacter = json.charAt(++index);
		sb.append(startCharacter);
		do {
			sb.append(activeCharacter);
			activeCharacter = json.charAt(++index);
		} while (activeCharacter != CHARACTER_E);

		return Boolean.valueOf(sb.toString());
	}

	private String readString(ReadType endType, char activeCharacter) {
		StringBuilder sb = new StringBuilder();
		while (activeCharacter != QUOTATION_MARK) {
			sb.append(activeCharacter);
			activeCharacter = json.charAt(++index);
		}
		currentRead = endType;
		return sb.toString();
	}

	private enum ReadType {
		STRING, NUMBER, INTEGER, TRUE, FALSE, NULL, START_ARRAY, END_ARRAY, START_ARRAY_ELEMENT, END_ARRAY_ELEMENT, START_OBJECT, END_OBJECT, START_DOCUMENT, END_DOCUMENT, START_VALUE, END_VALUE, START_KEY, END_KEY
	}

}
