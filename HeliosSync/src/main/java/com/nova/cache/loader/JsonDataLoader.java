package com.nova.cache.loader;

import com.nova.core.DataLoader;
import org.redisson.api.RedissonClient;

public class JsonDataLoader extends DataLoader {

    /**
     * Constructs a {@code JsonDataLoader} with the specified Redisson client.
     *
     * @param redisson the Redisson client used to interact with Redis
     */
    public JsonDataLoader(RedissonClient redisson) {
        super(redisson);
    }

    @Override
    public void loadData() {

    }
}
