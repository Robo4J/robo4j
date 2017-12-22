package com.robo4j.socket.http.json;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonTest {

    private static final char CURLY_BRACKET_LEFT = '\u007B';
    private static final char CURLY_BRACKET_RIGHT = '\u007D';
    private static final char NEW_LINE_UNIX = '\n';
    private static final char NEW_LINE_MAC = '\r';
    private static final char NEW_TAB = '\t';
    private static final char NEW_SPACE = '\u0020';
    private static final Set<Character> WHITE_SPACE_SET = new HashSet<>(Arrays.asList(NEW_LINE_MAC, NEW_LINE_UNIX, NEW_SPACE, NEW_TAB));


    private static final String json1 = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"]," +
            "\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}, " +
            "\"persons\":[{\"name\":\"name1\",\"value\":22, \"child\":{\"name\":\"name11\",\"value\":0, " +
            "\"child\":{\"name\":\"name111\",\"value\":42}}},{\"name\":\"name2\",\"value\":5}], " +
            "\"personMap\":{\"key1\":\"value1\",\"key2,\":\"value2\"}}";
    private static final String json2 = "{ \"number\"\n :  42, \"message\" \t: \"no message\", \"active\" : false , \"floatNumber\" : 0.42}";
    private static final String json3 = "{ \"number\"\n :  42,  \"message\" \t: \"no message\"}";

    @Test
    public void parsingTest(){
        StringBuilder sb = new StringBuilder();

        String json = json2;
        for(int i=0; i<json.length(); i++){
            char activeChar = json.charAt(i);
            if(!WHITE_SPACE_SET.contains(activeChar)){
                sb.append(activeChar);
            }
            System.out.print(json.charAt(i));
        }


        System.out.println("");
        System.out.println("Result: " + sb.toString());

    }

    @Test
    public void jsonParserTest(){
        JsonReader parser = new JsonReader(json3);
        parser.read();
        JsonDocument document = parser.getDocument();
        System.out.println("document: " + document);
    }



}
