package com.robo4j.socket.http.json;


import static com.robo4j.util.Utf8Constant.UTF8_APOSTROPHE;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonParser {


    public static final char CHAR_SPACE = '\u0020';
    public static final char CHAR_QUOTATION_MARK = '"';
    public static final char CHAR_CURLY_BRACKET_RIGHT = '}';
    public static final char CHAR_CURLY_BRACKET_LEFT = '{';
    public static final char CHAR_SQUARE_BRACKET_LEFT = '[';
    private String iniString;
    private char[] buffer;
    private int maxIndex;
    private int index;
    private int current;
    private int startCurrent;

    public void parse(String json){
        iniString = json;
        buffer = json.toCharArray();
        maxIndex = buffer.length;
        while (index < maxIndex){
            parse();
        }
    }

    private void parse(){
        readValue();
    }

    private void readValue(){
        readUnitWhiteSpace();
        read();
        switch (current){
            case 'n':
                readNull();
                break;
            case 't':
                readTrue();
                break;
            case 'f':
                readFalse();
                break;
            case CHAR_QUOTATION_MARK:
                readString();
                break;
            case CHAR_SQUARE_BRACKET_LEFT:
                readArray();
                break;
            case CHAR_CURLY_BRACKET_LEFT:
                readObject();
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                readNumber();
                break;
            default:
                throw new JsonException("index:"+ index +", current:" + String.valueOf((char)current));
        }
    }

    private void readNull(){
        read();
        readRequiredChar('u');
        readRequiredChar('l');
        readRequiredChar('l');
    }

    private void readTrue(){
        read();
        readRequiredChar('r');
        readRequiredChar('u');
        readRequiredChar('e');
    }

    private void readFalse(){
        read();
        readRequiredChar('a');
        readRequiredChar('l');
        readRequiredChar('s');
        readRequiredChar('e');
    }

    // TODO: 12/19/17 (miro) - improve test scenario
    private void readString(){
        while (current != CHAR_QUOTATION_MARK){
            read();
        }
    }

    private void readArray(){
        throw new JsonException("array not implemented");
    }

    private void read(){
        readUnitWhiteSpace();
        System.out.print(String.valueOf((char)current));
        current = buffer[index++];
    }


    private void readNumber(){
        while (isDigit()){
            read();
        }
    }

    private boolean isDigit() {
        return current >= '0' && current <= '9';
    }

    private void readObject(){
        read();
        int nestingLevel = 0;
        while(current != CHAR_CURLY_BRACKET_RIGHT){
            String name = readKey();
            System.out.println("name:" + name);

        }
        read();
    }

    private String readKey(){
        read();
        if(current != CHAR_QUOTATION_MARK){
            throw new JsonException("key expected");
        }
        return readStringValue();
    }

    private String readStringValue(){
        read();
        StringBuilder sb = new StringBuilder((char)current);
        while (current != CHAR_QUOTATION_MARK){
            current = buffer[index++];
            sb.append((char) current);
        }
        return sb.toString();
    }

    private void readRequiredChar(char ch){
        if (!readChar(ch)) {
            throw new JsonException(UTF8_APOSTROPHE + ch + UTF8_APOSTROPHE);
        }
    }

    private boolean readChar(char ch){
        if(current != ch){
            return false;
        }
        read();
        return true;
    }


    private boolean isRequiredChar(char ch) {
        return current != ch;
    }

    private void readUnitWhiteSpace(){
        while (isWhiteSpace()){
            current = buffer[index++];
        }
    }

    private boolean isWhiteSpace() {
        return current == CHAR_SPACE || current == '\t' || current == '\n' || current == '\r';
    }

}
