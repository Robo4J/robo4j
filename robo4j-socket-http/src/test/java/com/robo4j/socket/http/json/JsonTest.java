package com.robo4j.socket.http.json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonTest {

    private static final String jsonBasicValues = "{ \"number\"\n :  42, \"message\" \t: \"no message\", \"active\" : false , \"floatNumber\" : 0.42}";
    private static final String jsonBasicValueWithStringArray = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, \"active\" : false, " +
            "\"message\" \t: \"no message\", \"arrayOne\":[\"one\",\"two\"]}";
    private static final String jsonBasicValueWithStringAndIntegerArrays = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, " +
            "\"active\" : false, \"message\" \t: \"no message\", \"arrayOne\":[\"one\", \"two\"], \"arrayTwo\" : [1, 2 ,3 ]}";
    private static final String jsonBasicValueWithStringAndIntegerAndObjectArrays = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, " +
            "\"active\" : false,  \"arrayOne\":[\"one\", \"two\"], \"message\" \t: \"no message\", \"arrayTwo\" : [1, 2 ,3 ], " +
            "\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}]}";

    private static final String jsonBasicObjectArrays = "{\"number\"\n :  42,\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}]," +
            " \"active\" : false}";

    private static final String jsonBasicObjectArraysAndStringMap = "{\"number\"\n :  42," +
            "\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}]," +
            " \"active\" : false, \"simpleMap\": {\"one\":\"one1\",\"two\":\"two2\"}}";


    @Test
    public void basicValuesJsonParse(){
        JsonReader parser = new JsonReader(jsonBasicValues);
        JsonDocument document = parser.read();
        Map<String, Object> map = document.getMap();
        Assert.assertTrue(map.get("number").equals(42));
        Assert.assertTrue(map.get("message").equals("no message"));
        Assert.assertTrue(map.get("active").equals(false));
        Assert.assertTrue(map.get("floatNumber").equals(0.42));
    }

    @Test
    public void jsonBasicValuesAndStringArrayTest(){
        JsonReader parser = new JsonReader(jsonBasicValueWithStringArray);
        JsonDocument document = parser.read();
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
        JsonDocument document = parser.read();
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
        JsonDocument document = parser.read();
        System.out.println("document: " + document);
        Map<String, Object> map = document.getMap();
        List<Object> resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
        List<Object> resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();
        ((JsonDocument) map.get("arrayThree")).getArray();

        Assert.assertTrue(map.get("number").equals(42));
        Assert.assertTrue(map.get("active").equals(false));
        Assert.assertTrue(map.get("message").equals("no message"));
        Assert.assertTrue(map.get("floatNumber").equals(0.42));
        Assert.assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
        Assert.assertTrue(Arrays.asList(1, 2,3).containsAll(resultIntegerArray));

    }


    @Test
    public void jsonBasicObjectArraysTest(){


        JsonReader parser = new JsonReader(jsonBasicObjectArrays);
        JsonDocument document = parser.read();
        System.out.println("document: " + document);
        Map<String, Object> map = document.getMap();
        List<Object> resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();

        JsonDocument obj1 = (JsonDocument)resultObjectArray.get(0);
        JsonDocument obj2 = (JsonDocument)resultObjectArray.get(1);

        // [{"name":"name1","value": 22}, {"name":"name2","value": 42}]
        Assert.assertTrue(map.get("number").equals(42));
        Assert.assertTrue(map.get("active").equals(false));
        Assert.assertTrue(obj1.getMap().get("name").toString().equals("name1"));
        Assert.assertTrue((int)obj1.getMap().get("value") == 22);
        Assert.assertTrue(obj2.getMap().get("name").toString().equals("name2"));
        Assert.assertTrue((int)obj2.getMap().get("value") == 42);
        Assert.assertTrue(resultObjectArray.size() == 2);

    }

    @Test
    public void jsonBasicObjectArraysAndSimpleMapTest(){

        JsonReader parser = new JsonReader(jsonBasicObjectArraysAndStringMap);
        JsonDocument document = parser.read();
        System.out.println("document: " + document);
        Map<String, Object> map = document.getMap();
        List<Object> resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();
        Map<String, Object> simpleMap = ((JsonDocument) map.get("simpleMap")).getMap();

        JsonDocument obj1 = (JsonDocument)resultObjectArray.get(0);
        JsonDocument obj2 = (JsonDocument)resultObjectArray.get(1);

        // [{"name":"name1","value": 22}, {"name":"name2","value": 42}]
        Assert.assertTrue(map.get("number").equals(42));
        Assert.assertTrue(map.get("active").equals(false));
        Assert.assertTrue(obj1.getMap().get("name").toString().equals("name1"));
        Assert.assertTrue((int)obj1.getMap().get("value") == 22);
        Assert.assertTrue(obj2.getMap().get("name").toString().equals("name2"));
        Assert.assertTrue((int)obj2.getMap().get("value") == 42);
        Assert.assertTrue(resultObjectArray.size() == 2);
        Assert.assertTrue(simpleMap.get("one").equals("one1"));
        Assert.assertTrue(simpleMap.get("two").equals("two2"));

    }

}
