package com.nova.cache.loader;

import com.nova.core.DataLoader;
import org.redisson.api.RedissonClient;


public class CSVDataLoader extends DataLoader {

    /**
     * Constructs a {@code CSVDataLoader} with the specified Redisson client.
     *
     * @param redisson the Redisson client used to interact with Redis
     */
    public CSVDataLoader(RedissonClient redisson) {
        super(redisson);
    }

    @Override
    public void loadData() {

    }
}
