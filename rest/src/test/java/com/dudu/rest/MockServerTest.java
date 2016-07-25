package com.dudu.rest;

import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.service.GuardService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.observables.BlockingObservable;

import static okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
/**
 * Created by Administrator on 2016/3/29.
 */
public class MockServerTest {
    @Rule
    public final MockWebServer server = new MockWebServer();

    private GuardService service;

    @Before
    public void setUp() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(GuardService.class);
    }

    @Test
    public void bodySuccess200() {
        server.enqueue(new MockResponse().setBody("{" +
                "  \"resultCode\": 0,\n" +
                "  \"resultMsg\": \"成功\"\n" +
                "}"));

        BlockingObservable<RequestResponse> o = service.obtainVerificationCode("1111111").toBlocking();
        RequestResponse requestResponse = new RequestResponse();
        requestResponse.resultCode = 0;
        requestResponse.resultMsg = "成功";
        assertThat(o.first().resultCode).isEqualTo(requestResponse.resultCode);
    }

    @Test public void bodySuccess404() {
        server.enqueue(new MockResponse().setResponseCode(404));

        BlockingObservable<RequestResponse> o = service.obtainVerificationCode("1111111").toBlocking();
        try {
            o.first();
            fail();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            assertThat(cause).isInstanceOf(HttpException.class).hasMessage("HTTP 404 Client Error");
        }
    }

    @Test public void bodyFailure() {
        server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST));

        BlockingObservable<RequestResponse> o = service.obtainVerificationCode("1111111").toBlocking();
        try {
            o.first();
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(IOException.class);
        }
    }

}
