package com.nova.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RedissonClient;

/**
 * {@code DataLoader} is an abstract base class for loading data from various sources
 * and storing it in a Redis database.
 * <p>
 * This class provides a common interface for different types of data loaders,
 * with a logger for logging messages and a Redisson client for interacting with Redis.
 * </p>
 * <p>
 * Concrete implementations of this class should provide the specific logic for
 * loading data from the chosen source and processing it as required.
 * </p>
 *
 */
public abstract class DataLoader {

    /** Logger for logging messages */
    protected static final Logger logger = LogManager.getLogger(DataLoader.class);

    /** Redisson client used for interacting with Redis */
    protected RedissonClient redisson;

    /**
     * Constructs a {@code DataLoader} with the specified Redisson client.
     *
     * @param redisson the Redisson client used to interact with Redis
     */
    public DataLoader(RedissonClient redisson) {
        this.redisson = redisson;
    }

    /**
     * Loads data from the source and stores it in Redis.
     * <p>
     * This method should be implemented by concrete subclasses to provide
     * the specific logic for loading and processing data.
     * </p>
     */
    public abstract void loadData();

}
