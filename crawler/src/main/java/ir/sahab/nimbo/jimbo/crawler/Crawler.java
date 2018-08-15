package ir.sahab.nimbo.jimbo.crawler;

import ir.sahab.nimbo.jimbo.elasticsearch.ElasticCannotLoadException;
import ir.sahab.nimbo.jimbo.elasticsearch.ElasticSearchThread;
import ir.sahab.nimbo.jimbo.elasticsearch.ElasticsearchThreadFactory;
import ir.sahab.nimbo.jimbo.elasticsearch.ElasticsearchWebpageModel;
import ir.sahab.nimbo.jimbo.fetcher.FetcherSetting;
import ir.sahab.nimbo.jimbo.fetcher.Fetcher;
import ir.sahab.nimbo.jimbo.fetcher.Worker;
import ir.sahab.nimbo.jimbo.parser.Parser;
import ir.sahab.nimbo.jimbo.parser.ParserSetting;
import ir.sahab.nimbo.jimbo.parser.WebPageModel;
import ir.sahab.nimbo.jimbo.shuffler.Shuffler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Crawler {

    private final Shuffler shuffler;
    private final ArrayBlockingQueue<List<String>> shuffledLinksQueue;

    private final Fetcher fetcher;
    private final ArrayBlockingQueue<WebPageModel> rawPagesQueue;

    private final Parser parser;
    private final ArrayBlockingQueue<ElasticsearchWebpageModel> elasticQueue;

    private ElasticSearchThread esThread;

    public Crawler(CrawlerSetting crawlerSetting) {

        shuffledLinksQueue = new ArrayBlockingQueue<>(crawlerSetting.getShuffledQueueMaxSize());
        shuffler = new Shuffler(shuffledLinksQueue);

        rawPagesQueue = new ArrayBlockingQueue<>(crawlerSetting.getRawPagesQueueMaxSize());
        fetcher = new Fetcher(shuffledLinksQueue, rawPagesQueue, new FetcherSetting());

        elasticQueue = new ArrayBlockingQueue<>(crawlerSetting.getElasticQueueMaxSize());
        parser = new Parser(rawPagesQueue, elasticQueue, new ParserSetting());

        // TODO: what?
        try {
            ElasticsearchThreadFactory elasticsearchThreadFactory = new ElasticsearchThreadFactory(elasticQueue);
            esThread = elasticsearchThreadFactory.createNewThread();
        } catch (ElasticCannotLoadException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * constructor for testing
     */
    Crawler() {
        this(new CrawlerSetting(10000, 10000, 10000));
    }

    public void crawl() throws InterruptedException {

        new Thread(shuffler).start();

        parser.runWorkers();
        fetcher.runWorkers();
        // TODO: what?
        new Thread(esThread).start();

        long uptime = System.currentTimeMillis();
        while(true) {

            System.out.println("shuffled queue: " + shuffledLinksQueue.size()
                    + ",\t fetched queue: " + rawPagesQueue.size() + ", parsedQueue" +  Parser.parsedPages.intValue()
                    + ",\t uptime: " + (System.currentTimeMillis() - uptime));
            System.out.println(Worker.log());
            System.out.println("--------------------------------");
            Thread.sleep(10000);
        }
    }

}
