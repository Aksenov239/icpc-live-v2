package ru.ifmo.acm.datapassing;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class Data {
    public CreepingLineData creepingLineData;
    public ClockData clockData;
    public AdvertisementData advertisementData;
    public static Gson gson = new Gson();

    static CacheLoader<Class<? extends CachedData>, CachedData> loader = new CacheLoader<Class<? extends CachedData>, CachedData>() {
        public CachedData load(Class<? extends CachedData> clazz) throws IllegalAccessException, InstantiationException {
            return clazz.newInstance().initialize();
        }
    };
    static LoadingCache<Class<? extends CachedData>, CachedData> cache =
            CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(loader);


    public static String getCurrentData() {
        Data data = new Data();
        try {
            data.creepingLineData = new CreepingLineData().initialize();
            data.clockData = new ClockData().initialize();
            data.advertisementData = new AdvertisementData().initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return gson.toJson(data, Data.class);
    }
}
