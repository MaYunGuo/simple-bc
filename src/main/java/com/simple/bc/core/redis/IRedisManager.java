package com.simple.bc.core.redis;

import java.util.List;
import java.util.Map;

public interface IRedisManager {

    boolean setToMapRedis(String keyFormat, String key, String mapKey, Object mapValue);

    Map<Object,Object> getMapFromRedis(String keyFormat, String key);

    Object getMapFromRedis(String keyFormat, String key, String mapKey);
}
