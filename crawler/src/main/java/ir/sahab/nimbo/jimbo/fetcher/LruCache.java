package ir.sahab.nimbo.jimbo.fetcher;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * singleton pattern
 * cache the domains
 */
public class LruCache {

    private static final Logger logger = LoggerFactory.getLogger(LruCache.class);

    private static final String PROP_NAME = "lru.properties";
    private static LruCache lruCache = null;
    private int maxCacheSize;
    private int duration;
    private Cache<String, Integer> cache;


    private LruCache() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(PROP_NAME));
            maxCacheSize = Integer.parseInt(properties.getProperty("max_cache"));
            duration = Integer.parseInt(properties.getProperty("duration"));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        cache = Caffeine.newBuilder()
                .maximumSize(maxCacheSize)
                .expireAfterWrite(duration, TimeUnit.SECONDS)
                .build();
    }

    public synchronized static LruCache getInstance() {
        if (lruCache == null) {
            lruCache = new LruCache();
        }
        return lruCache;
    }


    /**
     * add a site domain to cache, if it existInLru, it throws exception, else it add it
     * cache has a capacity and timeout for each domain
     * if it goes more than capacity, it remove element in a strange way to optimum itself
     *
     * @param url domain of site
     */
    public boolean add(String url) {
        cache.put(url, 1);
        return true;
    }

    void remove(String url) {
        cache.invalidate(url);
    }

    public boolean exist(String url) {
        return cache.getIfPresent(url) != null;

    }

    int getMaxCacheSize() {
        return maxCacheSize;
    }

    int getDuration() {
        return duration;
    }

    /**
     * clear all sites in cache
     */
    void clear() {
        cache.invalidateAll();
    }
}
