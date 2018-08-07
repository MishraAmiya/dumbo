package ir.sahab.nimbo.jimbo.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    public static final String URL_FRONTIER_TOPIC;
    public static final int FETCHER_THREAD_NUM;
    public static final int PARSER_THREAD_NUM;
    public static final int BLOCKING_QUEUE_SIZE;
    public static final String HBASE_TABLE_NAME;
    public static final String HBASE_DATA_CF_NAME;
    public static final String HBASE_MARK_CF_NAME;
    public static final String HBASE_SITE_DIR;

    static {
        String resourceName = "conf.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
            props.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        URL_FRONTIER_TOPIC = props.getProperty("url_frontier_topic");
        FETCHER_THREAD_NUM = Integer.valueOf(props.getProperty("fetcher_thread_num"));
        PARSER_THREAD_NUM = Integer.valueOf(props.getProperty("parser_thread_num"));
        BLOCKING_QUEUE_SIZE = Integer.valueOf(props.getProperty("blocking_queue_size"));
        HBASE_TABLE_NAME = props.getProperty("hbase_table_name");
        HBASE_MARK_CF_NAME = props.getProperty("hbase_mark_cf_name");
        HBASE_DATA_CF_NAME = props.getProperty("hbase_data_cf_name");
        HBASE_SITE_DIR = props.getProperty("hbase_site_dir");
    }
}
