package org.openhab.binding.mysensors.test;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.binding.mysensors.internal.factory.MySensorsCacheFactory;

import com.google.gson.reflect.TypeToken;

public class CacheTest {

    private MySensorsCacheFactory c = null;

    @BeforeClass
    public void init() {
        c = MySensorsCacheFactory.getCacheFactory();
    }

    @Test
    public void writeGivenIdsCache() {
        MySensorsCacheFactory c = MySensorsCacheFactory.getCacheFactory();
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ids.add(2);
        ids.add(3);
        ids.add(5);
        c.writeCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, ids, new TypeToken<ArrayList<Integer>>() {
        }.getType());
    }

    @Test
    public void readGivenIdsCache() {
        c = MySensorsCacheFactory.getCacheFactory();
        System.out.println(c.readCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, new ArrayList<Integer>(),
                new TypeToken<ArrayList<Integer>>() {
                }.getType()));
    }

    @AfterClass
    public void deleteCache() {
        c.deleteCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE);
    }

}