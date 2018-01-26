package com.robo4j.socket.http.util;

import com.robo4j.LifecycleState;
import com.robo4j.socket.http.dto.ResponseUnitDTO;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * json related utils tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonUtilTests {


    @Test
    public void jsonToListTest(){

        String json = "[{\"id\":\"unit1\",\"state\":\"INITIALIZED\"}," +
                "{\"id\":\"unit2\",\"state\":\"STARTED\"}, " +
                "{\"id\":\"unit3\",\"state\":\"FAILED\"}]";

        List<ResponseUnitDTO> expectedResult = Arrays.asList(new ResponseUnitDTO("unit1", LifecycleState.INITIALIZED),
                new ResponseUnitDTO("unit2", LifecycleState.STARTED), new ResponseUnitDTO("unit3", LifecycleState.FAILED));
        List<ResponseUnitDTO> result = JsonUtil.jsonToList(ResponseUnitDTO.class, json);

        Assert.assertNotNull(result);
        Assert.assertArrayEquals(expectedResult.toArray(), result.toArray());
    }

    @Test
    public void jsonToListEmptyTest(){
        String json = "[]";
        List<ResponseUnitDTO> result = JsonUtil.jsonToList(ResponseUnitDTO.class, json);

        Assert.assertNotNull(result);
        Assert.assertArrayEquals(Collections.emptyList().toArray(), result.toArray());
    }


    @Test
    public void mapToJsonTest(){
        String expectedJson ="{\"key1\":\"value1\",\"key2\":\"value2\"}";
        Map<String, String> testMap = new HashMap<>();
        testMap.put("key1","value1");
        testMap.put("key2","value2");

        String result = JsonUtil.toJsonMap(testMap);
        System.out.println("result: " + result);

        Assert.assertNotNull(result);
        Assert.assertEquals(result, expectedJson);
    }

    @Test
    public void mapToJsonEmptyTest(){
        String expectedJson ="{}";

        String result = JsonUtil.toJsonMap(new HashMap<>());
        System.out.println("result: " + result);

        Assert.assertNotNull(result);
        Assert.assertEquals(result, expectedJson);

    }

    @Test
    public void objectMapToJsonTest(){
        String expectedJson ="{\"key1\":2,\"key2\":3}";
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key1",2);
        testMap.put("key2",3);

        String result = JsonUtil.toJsonMapObject(testMap);
        System.out.println("result: " + result);

        Assert.assertNotNull(result);
        Assert.assertEquals(result, expectedJson);
    }

}
