package com.robo4j.socket.http.json;

import com.robo4j.socket.http.util.RoboReflectException;

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
	private Stack<JsonDocumentWrapper> stack = new Stack<>();
	private JsonDocument document;
	private int index;
	private String currentKey;
	private Object currentValue;

	public JsonReader(String json) {
		this.json = json;
        char activeChar = getCharSkipWhiteSpace();
        currentRead = getActualReadType(currentRead, activeChar);
        document = getNewDocument(currentRead);
	}

	@Deprecated
	public char initRead(){
		return json.charAt(++index);
	}

	private ReadType getActualReadTypeWithValidCharacter(ReadType previousRead) {
		getCharSkipWhiteSpace();
		char activeChar = json.charAt(index);
		switch (activeChar) {
			case COLON:
				return ReadType.START_VALUE;
			case CURLY_BRACKET_LEFT:
			return ReadType.START_OBJECT;
			case CURLY_BRACKET_RIGHT:
			return ReadType.END_OBJECT;
			case SQUARE_BRACKET_LEFT:
				return ReadType.START_ARRAY;
			case SQUARE_BRACKET_RIGHT:
				return ReadType.END_ARRAY;
			case COMMA:
				index++;
				switch (previousRead){
					case END_ARRAY:
						return ReadType.START_ARRAY;
					case END_ARRAY_ELEMENT:
						return ReadType.START_ARRAY_ELEMENT;
					case END_VALUE:
						return ReadType.START_VALUE;
					case END_OBJECT:
						return ReadType.START_OBJECT;
					default:
				}
			case QUOTATION_MARK:
				throw new RoboReflectException("get actual ready quotation mark " + previousRead);
			default:

		}
		return previousRead;
	}

	public JsonDocument readStep(){

		char activeChar = initRead();

		while(index < json.length()){
			currentRead = getActualReadTypeWithValidCharacter(currentRead);

			switch (currentRead){
				case START_ARRAY:
					//current key is null when array of array
					JsonDocumentWrapper documentWrapper = new JsonDocumentWrapper(currentKey, document);
					stack.push(documentWrapper);
					document = getNewDocument(currentRead);
					currentKey = null;
					currentValue = null;
					break;
				case END_ARRAY:

					if(currentKey == null && stack.size() > 0 && stack.peek().getDocument().isArray()){
						JsonDocumentWrapper arrayDocumentWrapper = stack.pop();
						arrayDocumentWrapper.getDocument().add(document);
						document = arrayDocumentWrapper.getDocument();
						currentKey = null;
					} else if(currentKey != null) {
						document.put(currentKey, document);
						currentKey = null;
						currentValue = null;
					} else {
						throw new RoboReflectException("end array currentKey: " + currentKey +
								" activeChar: " + activeChar + " document: " + document );
					}
					break;
				case START_ARRAY_ELEMENT:
					break;
				case END_ARRAY_ELEMENT:
					break;
				case START_OBJECT:
					JsonDocumentWrapper objWrapperObject = new JsonDocumentWrapper(currentKey, document);
					currentKey = null;
					stack.push(objWrapperObject);
					document = getNewDocument(currentRead);
					break;
				case END_OBJECT:
					break;
				case START_KEY:
					activeChar = json.charAt(++index);
					currentKey = readString(ReadType.END_KEY, activeChar);
					break;
				case START_VALUE:
					break;
				default:
					throw new RoboReflectException("read step not expecting:" + currentKey + " char: " + activeChar);
			}
			index++;
		}
		return document;
	}

	@Deprecated
	public JsonDocument read() {
		++index;
		while (index < json.length()) {
			char activeChar = getCharSkipWhiteSpace();
			currentRead = getActualReadType(currentRead, activeChar);

			switch (currentRead) {
			case START_ARRAY:
				if (currentKey != null && !document.isArray()) {
					JsonDocumentWrapper documentWrapper = new JsonDocumentWrapper(currentKey, document);
					currentKey = null;
					stack.push(documentWrapper);
					document = getNewDocument(currentRead);
				}
				currentRead = ReadType.START_ARRAY_ELEMENT;
				break;
			case END_ARRAY:
				if (activeChar == SQUARE_BRACKET_RIGHT) {
					if (stack.size() > 0) {
						JsonDocumentWrapper documentWrapper = stack.pop();
						if(documentWrapper.getDocument().isArray()){
							documentWrapper.getDocument().add(document);
							currentKey = null;
							currentValue = null;
						} else {
							currentKey = documentWrapper.getName();
							currentValue = document;
						}

						document = documentWrapper.getDocument();
					}
				} else {
					throw new RoboReflectException("not proper array close");
				}
				break;
			case START_ARRAY_ELEMENT:
                startArrayElementByActiveChar(activeChar);
                break;
			case END_ARRAY_ELEMENT:
				if (activeChar == COMMA) {
					currentRead = ReadType.START_ARRAY_ELEMENT;
				} else {
					currentRead = ReadType.END_ARRAY;
				}
				break;
			case START_OBJECT:
				currentRead = ReadType.START_KEY;
				break;
			case END_OBJECT:
				putCurrentKeyValue(currentKey, currentValue);
				if(stack.size() > 0){
					document = stack.peek().getDocument().isArray() ? getLastStackElementAndInner(true) :
							getLastStackElementAndInner(false);
				} else {
					currentRead = ReadType.END_VALUE;
				}
				break;
			case START_KEY:
				activeChar = json.charAt(++index);
				currentKey = readString(ReadType.END_KEY, activeChar);
				break;
			case END_KEY:
				if (activeChar == COLON) {
					currentRead = ReadType.START_VALUE;
				} else {
					throw new JsonException("not expected value after key");
				}
				break;
			case START_VALUE:
                startValueByActiveChar(activeChar);
                break;
			case END_VALUE:
			    if(activeChar == COMMA){
                    currentRead = ReadType.START_KEY;
                }
				putCurrentKeyValue(currentKey, currentValue);
				break;
			}

			index++;
		}
		return document;
	}

    private char getCharSkipWhiteSpace() {
        char result = json.charAt(index);
        while (WHITE_SPACE_SET.contains(result)) {
            result = json.charAt(++index);
        }
        return result;
    }


    private ReadType getActualReadType(ReadType currentRead, char activeChar) {
		if(document != null && document.isArray() && currentRead.equals(ReadType.START_ARRAY)){
			return ReadType.START_ARRAY_ELEMENT;
		} else {
			switch (activeChar) {
				case CURLY_BRACKET_LEFT:
					if (currentRead != null && currentRead.equals(ReadType.START_ARRAY_ELEMENT)) {
						activeChar = json.charAt(--index);
						return currentRead;
					} else if (currentRead != null && currentRead.equals(ReadType.START_VALUE)) {
						activeChar = json.charAt(--index);
						return currentRead;
					}
					return ReadType.START_OBJECT;
				case CURLY_BRACKET_RIGHT:
					return ReadType.END_OBJECT;
				case QUOTATION_MARK:
					if (currentRead.equals(ReadType.START_OBJECT)) {
						return ReadType.START_KEY;
					}
					break;
				case SQUARE_BRACKET_LEFT:
					if(currentRead != null && currentRead.equals(ReadType.START_ARRAY)){
						return ReadType.START_ARRAY_ELEMENT;
					} else if(currentRead != null && currentRead.equals(ReadType.START_ARRAY_ELEMENT)) {
						return currentRead;
					} else {
						return ReadType.START_ARRAY;
					}
				case SQUARE_BRACKET_RIGHT:
					return ReadType.END_ARRAY;
				case COMMA:
					if (currentRead.equals(ReadType.END_ARRAY)) {
						return document.isArray() ? ReadType.START_ARRAY : ReadType.END_VALUE;
					}
				default:
					break;

			}
		}

        return currentRead;
    }

    private void startValueByActiveChar(char activeChar) {
        switch (activeChar) {
        case CURLY_BRACKET_LEFT:
            JsonDocumentWrapper objDocWrapper = new JsonDocumentWrapper(currentKey, document);
            currentKey = null;
            stack.push(objDocWrapper);
            document = getNewDocument(ReadType.START_OBJECT);
            break;
        case SQUARE_BRACKET_LEFT:
            JsonDocumentWrapper arrayDocWrapper = new JsonDocumentWrapper(currentKey, document);
            currentKey = null;
            stack.push(arrayDocWrapper);
            document = getNewDocument(ReadType.START_ARRAY);
            break;
        case QUOTATION_MARK:
            activeChar = json.charAt(++index);
            currentValue = readString(ReadType.END_VALUE, activeChar);
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
                JsonDocumentWrapper documentWrapper = new JsonDocumentWrapper(null, document);
                stack.push(documentWrapper);
                document = getNewDocument(ReadType.START_OBJECT);
                break;
            case SQUARE_BRACKET_LEFT:
                // TODO: 12/28/17 (miro) implement + test
				JsonDocumentWrapper arrayDocumentWrapper = new JsonDocumentWrapper(null, document);
				stack.push(arrayDocumentWrapper);
				document = getNewDocument(ReadType.START_ARRAY);
                break;
            case QUOTATION_MARK:
                activeChar = json.charAt(++index);
                String elementString = readString(ReadType.END_ARRAY_ELEMENT, activeChar);
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
                if (NUMBER_SET.contains(activeChar)) {
                    Object elementNumber = readNumber(activeChar);
                    document.add(elementNumber);
                    currentRead = ReadType.END_ARRAY_ELEMENT;
                    break;
                } else {
                    throw new RoboReflectException("wrong array element reading: " + activeChar);
                }
            }
    }

    private JsonDocument getLastStackElementAndInner(boolean isInner){
		JsonDocumentWrapper documentWrapper = stack.pop();
		if(isInner){
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
			currentRead = ReadType.START_ARRAY;
			return new JsonDocument(JsonDocument.Type.ARRAY);
		}
		throw new RoboReflectException("new document: " + readType);
	}

	private Object readNumber(char activeCharacter) {
		boolean isInteger = true;
		StringBuilder sb = new StringBuilder();
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
		sb.append(startCharacter);
		char activeCharacter = startCharacter;
		while (activeCharacter != CHARACTER_E) {
			activeCharacter = json.charAt(++index);
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
		char ch = json.charAt(++index);
		if (ch != compareCharacter) {
			throw new RoboReflectException("not similar characters");
		}
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
		//formatter:off
		NUMBER,
		INTEGER,
		STRING,
		TRUE,
		FALSE,
		NULL,
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
		//formatter:off
	}

}
