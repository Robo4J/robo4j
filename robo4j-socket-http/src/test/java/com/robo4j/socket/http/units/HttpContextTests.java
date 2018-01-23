package com.robo4j.socket.http.units;

import com.robo4j.util.Utf8Constant;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpContextTests {


    @Test
    public void serverDefaultContextTest(){

        ServerContext context = ServerContentBuilder.Builder().build(null);

        System.out.println("context:" + context);
        Assert.assertNotNull(context);
        Assert.assertTrue(!context.isEmpty());
        Assert.assertTrue(context.containsPath(Utf8Constant.UTF8_SOLIDUS));

    }

    @Test
    public void clientDefaultContextTest(){

        ClientContext context = ClientContextBuilder.Builder().build(null);

        System.out.println("context: " + context);
        Assert.assertNotNull(context);
        Assert.assertTrue(context.isEmpty());
    }
}
