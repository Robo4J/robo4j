package com.robo4j.socket.http.json;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonParserTest {

    private static final String jsonBasicValues = "{ \"number\"\n :  42, \"message\" \t: \"no message\", \"active\" : false , \"floatNumber\" : 0.42}";
    private static final String jsonBasicValueWithStringArray = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, \"active\" : false, " +
            "\"message\" \t: \"no message\", \"arrayOne\":[\"one\",\"two\"]}";
    private static final String jsonBasicValueWithStringAndIntegerArrays = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, " +
            "\"active\" : false, \"message\" \t: \"no message\", \"arrayOne\":[\"one\", \"two\"], \"arrayTwo\" : [1, 2 ,3 ]}";
    private static final String jsonBasicValueWithStringAndIntegerAndObjectArrays = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, " +
            "\"arrayOne\":[\"one\", \"two\"], \"message\" \t: \"no message\", \"arrayTwo\" : [1, 2 ,3 ], " +
            "\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}], \"active\" : false}";

    private static final String jsonBasicValueAndObjectValueWithStringAndIntegerAndObjectArrays = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, " +
            "\"arrayOne\":[\"one\", \"two\"], \"message\" \t: \"no message\", \"arrayTwo\" : [1, 2 ,3 ], " +
            "\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}], \"active\" : false," +
            "\"child\" :{\"name\":\"name11\",\"value\":1} }";


    private static final String jsonArrayObject = "{\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}]}";
    private static final String jsonValueObject = "{\"child\" : {\"name\": \"name11\",\"value\":1," +
            " \"child\": {\"name\":\"name11\",\"age\":0, \"numbers\":[1,2,3], " +
            "\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}]}}}";

    @Test
    public void basicArrayObjectTest(){
        JsonReader parser = new JsonReader(jsonValueObject);
        parser.read2();
        JsonDocument document = parser.getDocument();
        System.out.println("DOC: " + document);
    }

    @Test
    public void basicValuesJsonParse(){
        JsonReader parser = new JsonReader(jsonBasicValues);
        parser.read2();
        JsonDocument document = parser.getDocument();
        Map<String, Object> map = document.getMap();
        Assert.assertTrue(map.get("number").equals(42));
        Assert.assertTrue(map.get("message").equals("no message"));
        Assert.assertTrue(map.get("active").equals(false));
        Assert.assertTrue(map.get("floatNumber").equals(0.42));
    }

    @Test
    public void jsonBasicValuesAndStringArrayTest(){
        JsonReader parser = new JsonReader(jsonBasicValueWithStringArray);
        parser.read2();
        JsonDocument document = parser.getDocument();
        Map<String, Object> map = document.getMap();
        Assert.assertTrue(map.get("number").equals(42));
        Assert.assertTrue(map.get("active").equals(false));
        Assert.assertTrue(map.get("message").equals("no message"));
        Assert.assertTrue(map.get("floatNumber").equals(0.42));
        List<Object> resultArray = ((JsonDocument) map.get("arrayOne")).getArray();
        Assert.assertTrue(Arrays.asList("one", "two").containsAll(resultArray));

        System.out.println("document: " + document);
    }

    @Test
    public void jsonBasicValuesAndStringAndIntegerArraysTest(){
        JsonReader parser = new JsonReader(jsonBasicValueWithStringAndIntegerArrays);
        parser.read2();
        JsonDocument document = parser.getDocument();
        Map<String, Object> map = document.getMap();
        List<Object> resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
        List<Object> resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();

        Assert.assertTrue(map.get("number").equals(42));
        Assert.assertTrue(map.get("active").equals(false));
        Assert.assertTrue(map.get("message").equals("no message"));
        Assert.assertTrue(map.get("floatNumber").equals(0.42));
        Assert.assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
        Assert.assertTrue(Arrays.asList(1, 2,3).containsAll(resultIntegerArray));

        System.out.println("document: " + document);
    }

    @Test
    public void jsonBasicValuesAndStringAndIntegerAndObjectArraysTest(){

        JsonReader parser = new JsonReader(jsonBasicValueWithStringAndIntegerAndObjectArrays);
        parser.read2();
        JsonDocument document = parser.getDocument();
        System.out.println("document: " + document);
        Map<String, Object> map = document.getMap();
        List<Object> resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
        List<Object> resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();
        List<Object> resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();
        JsonDocument arrayObj1 = (JsonDocument) resultObjectArray.get(0);

        Assert.assertTrue(map.get("number").equals(42));
        Assert.assertTrue(map.get("active").equals(false));
        Assert.assertTrue(map.get("message").equals("no message"));
        Assert.assertTrue(map.get("floatNumber").equals(0.42));
        Assert.assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
        Assert.assertTrue(Arrays.asList(1, 2,3).containsAll(resultIntegerArray));
        Assert.assertTrue(resultObjectArray.size() == 2);
        Assert.assertTrue(arrayObj1.getMap().get("name").equals("name1"));
        Assert.assertTrue(arrayObj1.getMap().get("value").equals(22));

    }

    @Test
    public void jsonBasicValuesAndObjectValueAndStringAndIntegerAndObjectArraysTest(){

        JsonReader parser = new JsonReader(jsonBasicValueAndObjectValueWithStringAndIntegerAndObjectArrays);
        parser.read2();
        JsonDocument document = parser.getDocument();
        System.out.println("document: " + document);
        Map<String, Object> map = document.getMap();
        List<Object> resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
        List<Object> resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();
        List<Object> resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();
        JsonDocument arrayObj1 = (JsonDocument) resultObjectArray.get(0);
        JsonDocument childObj = (JsonDocument) map.get("child");

        Assert.assertTrue(map.get("number").equals(42));
        Assert.assertTrue(map.get("active").equals(false));
        Assert.assertTrue(map.get("message").equals("no message"));
        Assert.assertTrue(map.get("floatNumber").equals(0.42));
        Assert.assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
        Assert.assertTrue(Arrays.asList(1, 2,3).containsAll(resultIntegerArray));
        Assert.assertTrue(resultObjectArray.size() == 2);
        Assert.assertTrue(arrayObj1.getMap().get("name").equals("name1"));
        Assert.assertTrue(arrayObj1.getMap().get("value").equals(22));
        Assert.assertTrue(childObj.getMap().get("name").equals("name11"));
        Assert.assertTrue(childObj.getMap().get("value").equals(1));

    }
}
