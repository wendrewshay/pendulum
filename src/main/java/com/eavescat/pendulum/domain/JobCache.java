package com.eavescat.pendulum.domain;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义任务缓存
 * Created by wendrewshay on 2019/7/13 14:43
 */
public class  JobCache {
    /**
     * 存储
     */
    private static Map<String, JobInfo> storeMap = new ConcurrentHashMap<>();

    /**
     * 查询所有数据
     * @author wendrewshay
     * @date 2019/7/13 16:47
     * @return Collection<JobInfo>
     */
    public static Collection<JobInfo> getAll() {
        return storeMap.values();
    }

    /**
     * 将数据放入缓存
     * @author wendrewshay
     * @date 2019/7/13 14:51
     * @param cacheKey 缓存键
     * @param cacheValue 缓存值
     */
    public static void put(String cacheKey, JobInfo cacheValue) {
        storeMap.put(cacheKey, cacheValue);
    }

    /**
     * 从缓存中获取数据
     * @author wendrewshay
     * @date 2019/7/13 14:52
     * @param cacheKey 缓存键
     * @return JobInfo
     */
    public static JobInfo get(String cacheKey) {
        return storeMap.get(cacheKey);
    }

    /**
     * 移除某个缓存数据
     * @author wendrewshay
     * @date 2019/7/13 14:55
     * @param cacheKey 缓存键
     */
    public static void remove(String cacheKey) {
        storeMap.remove(cacheKey);
    }

    /**
     * 清理缓存
     * @author wendrewshay
     * @date 2019/7/13 14:54
     */
    public static void clear() {
        storeMap.clear();
    }

    /**
     * 是否为空
     * @author wendrewshay
     * @date 2019/7/19 17:59
     * @return boolean
     */
    public static boolean isEmpty() {
        return storeMap.isEmpty();
    }
}
