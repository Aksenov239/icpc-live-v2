package ru.ifmo.acm.datapassing;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class Data implements CachedData {
    public CreepingLineData creepingLineData;
    public ClockData clockData;
    public AdvertisementData advertisementData;
    public StandingsData standingsData;
    public PersonData personData;
    public TeamData teamData;
    public CameraData cameraData;
    public SplitScreenData splitScreenData;
    public BreakingNewsData breakingNewsData;

    //TODO merge this to statuses, as subclass.

    private static CacheLoader<Class<? extends CachedData>, CachedData> loader = new CacheLoader<Class<? extends CachedData>, CachedData>() {
        public CachedData load(Class<? extends CachedData> clazz) throws IllegalAccessException, InstantiationException {
            return clazz.newInstance().initialize();
        }
    };

    public static LoadingCache<Class<? extends CachedData>, CachedData> cache =
            CacheBuilder.newBuilder().build(loader);

    public Data initialize() {
        try {
            creepingLineData = (CreepingLineData) cache.get(CreepingLineData.class);
            clockData = (ClockData) cache.get(ClockData.class);
            advertisementData = (AdvertisementData) cache.get(AdvertisementData.class);
            standingsData = (StandingsData) cache.get(StandingsData.class);
            personData = (PersonData) cache.get(PersonData.class);
            teamData = (TeamData) cache.get(TeamData.class);
            splitScreenData = (SplitScreenData) cache.get(SplitScreenData.class);
            breakingNewsData = (BreakingNewsData) cache.get(BreakingNewsData.class);
            //cameraData = (CameraData) cache.get(CameraData.class);
            //System.err.println(teamData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
