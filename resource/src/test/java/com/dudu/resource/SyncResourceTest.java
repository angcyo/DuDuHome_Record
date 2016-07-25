package com.dudu.resource;

import com.dudu.resource.resource.ResourceState;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SyncResourceTest {
    @Test
    public void test_MySyncRes_init_release_ok() throws Exception {
        MySyncResource r = new MySyncResource();
        System.out.println("test_MySyncRes_init_release_ok");
        assertEquals(ResourceState.UnInit, r.getResourceState());
        r.init();
        assertEquals(ResourceState.Inited, r.getResourceState());
        r.release();
        assertEquals(ResourceState.UnInit, r.getResourceState());
    }

    @Test
    public void test_MySyncRes_init_N_times_ok() throws Exception {
        MySyncResource r = new MySyncResource();
        System.out.println("test_MySyncRes_init_N_times_ok, init和release必须成对出现");
        assertEquals(ResourceState.UnInit, r.getResourceState());
        r.init();
        assertEquals(ResourceState.Inited, r.getResourceState());
        r.init();
        assertEquals(ResourceState.Inited, r.getResourceState());
        r.release();
        assertEquals(ResourceState.Inited, r.getResourceState());
        r.release();
        assertEquals(ResourceState.UnInit, r.getResourceState());
    }
}
