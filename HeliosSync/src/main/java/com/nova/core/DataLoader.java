package com.nova.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RedissonClient;

public abstract class DataLoader {

    protected static final Logger logger = LogManager.getLogger(DataLoader.class);

    protected RedissonClient redisson;

    public DataLoader(RedissonClient redisson) {
        this.redisson = redisson;
    }

    public abstract void loadData();

}
