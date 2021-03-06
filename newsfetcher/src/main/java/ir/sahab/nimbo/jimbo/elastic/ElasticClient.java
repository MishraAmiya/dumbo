package ir.sahab.nimbo.jimbo.elastic;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ElasticClient {

    private static ElasticClient ourInstance = new ElasticClient();
    private RestHighLevelClient client;

    public static ElasticClient getInstance() {
        return ourInstance;
    }

    private ElasticClient() {
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(Config.ES_HOSTS.get(0).getHostName(),
                                Config.ES_HOSTS.get(0).getPort(),
                                Config.ES_SCHEME),
                        new HttpHost(Config.ES_HOSTS.get(1).getHostName(),
                                Config.ES_HOSTS.get(1).getPort(),
                                Config.ES_SCHEME))
                        .setRequestConfigCallback(
                                requestConfigBuilder ->
                                        requestConfigBuilder
                                                .setConnectTimeout(Config.ES_CONNECTION_TIMEOUT)
                                                .setSocketTimeout(Config.ES_SOCKET_TIMEOUT))
                        .setMaxRetryTimeoutMillis(Config.ES_MAXRETRY_TIMEOUT));
    }

    public ArrayList<SearchHit> simpleElasticSearch(String mustFind) {
        ArrayList<String> simpleQuery = new ArrayList<>();
        simpleQuery.add(mustFind);
        return jimboElasticSearch(simpleQuery, new ArrayList<>(), new ArrayList<>());
    }

    public ArrayList<SearchHit> jimboElasticSearch(
            ArrayList<String> mustFind,
            ArrayList<String> mustNotFind,
            ArrayList<String> shouldFind) {
        SearchRequest searchRequest = new SearchRequest(Config.ES_INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        String fields[] = {"url", "content", "title", "description", "date"};
        for (String phrase : mustFind) {
            MultiMatchQueryBuilder multiMatchQueryBuilder =
                    QueryBuilders.multiMatchQuery(
                            phrase, fields)
                            .type(MultiMatchQueryBuilder.Type.PHRASE);
            for (String field : fields)
                multiMatchQueryBuilder.field(field, Config.getScoreField(field));
            boolQuery.must(multiMatchQueryBuilder);
        }
        for (String phrase : mustNotFind) {
            boolQuery.mustNot(
                    QueryBuilders.multiMatchQuery(
                            phrase, fields)
                            .type(MultiMatchQueryBuilder.Type.PHRASE));
        }
        for (String phrase : shouldFind) {
            MultiMatchQueryBuilder multiMatchQueryBuilder =
                    QueryBuilders.multiMatchQuery(
                            phrase, fields)
                            .type(MultiMatchQueryBuilder.Type.PHRASE);
            for (String field : fields)
                multiMatchQueryBuilder.field(field, Config.getScoreField(field));
            boolQuery.should(multiMatchQueryBuilder);
        }
        searchSourceBuilder.query(boolQuery);
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            return new ArrayList<SearchHit>(Arrays.asList(searchHits));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } catch (RuntimeException r) {
            r.printStackTrace();
            return null;
        }
    }

}
