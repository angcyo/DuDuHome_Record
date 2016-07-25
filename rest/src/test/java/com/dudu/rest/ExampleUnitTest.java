package com.dudu.rest;

import com.dudu.rest.model.active.ActiveRequestResponse;
import com.google.gson.Gson;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_gson() throws Exception {
        String jsonString = "{\n" +
                "  \"result\": {\n" +
                "    \"aliPayUri\": \"http://119.29.23.51/group1/M00/00/03/CmhJmVcbQ8mAKLP8AAAFRoglG00132.png\",\n" +
                "    \"tencentPayUri\": \"http://119.29.23.51/group1/M00/00/03/CmhJwVcbQ8mAGXqXAAAHBXia7gg620.png\"\n" +
                "  },\n" +
                "  \"resultCode\": 40023,\n" +
                "  \"resultMsg\": \"激活成功\"\n" +
                "}";
        Gson gson = new Gson();
        ActiveRequestResponse activeRequestResponse = gson.fromJson(jsonString, ActiveRequestResponse.class);
        Assertions.assertThat(activeRequestResponse).isNotNull();
    }
}