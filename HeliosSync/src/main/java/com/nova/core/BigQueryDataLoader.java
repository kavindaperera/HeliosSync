package com.nova.core;


import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import org.redisson.api.RedissonClient;

public class BigQueryDataLoader extends DataLoader {

    private static final String BIGQUERY_QUERY = System.getenv("BIGQUERY_QUERY");

    public BigQueryDataLoader(RedissonClient redisson) {
        super(redisson);
    }

    @Override
    public void loadData() {
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(BIGQUERY_QUERY).build();

        try {
            TableResult result = bigquery.query(queryConfig);
            result.iterateAll().forEach(row -> {
                redisson.getBucket(row.get("id").getStringValue()).set(row.get("value").getStringValue());
            });
            logger.info("Data loaded from BigQuery to Redis.");
        } catch (Exception e) {
            logger.error("Error loading data from BigQuery: ", e);
        }
    }

}
