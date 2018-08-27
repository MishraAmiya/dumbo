package ir.sahab.nimbo.jimbo.main;

import ir.sahab.nimbo.jimbo.elasticsearch.ElasticsearchSetting;
import ir.sahab.nimbo.jimbo.elasticsearch.ElasticsearchWebpageModel;
import ir.sahab.nimbo.jimbo.elasticsearch.ElasticsearchHandler;
import ir.sahab.nimbo.jimbo.rss.RssFeed;
import ir.sahab.nimbo.jimbo.rss.RssFeedMessage;
import ir.sahab.nimbo.jimbo.rss.RssFeedParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

<<<<<<< HEAD:newsfetcher/src/main/java/ir/sahab/nimbo/jimbo/main/Main.java
public class Main {
=======
public class Exec extends Thread {
>>>>>>> e8a7c6e7bfef0d257e875ec8732638b47e24ee80:newsfetcher/src/main/java/ir/sahab/nimbo/jimbo/main/Exec.java
    static ArrayBlockingQueue<ElasticsearchWebpageModel> blockingQueue = new ArrayBlockingQueue<>(1000);

    static String fetch(String url, NewsSite site) {
        try {
            System.out.println(url);
            Document doc = Jsoup.connect(url).validateTLSCertificates(false).timeout(10000).get();
            Elements divs = doc.select(site.getTag() + "[" + site.getAttribute() + "]");
            for (Element div : divs) {
                if (div.attr(site.getAttribute()).contains(site.getAttributeValue())) {
                    return div.text();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
<<<<<<< HEAD:newsfetcher/src/main/java/ir/sahab/nimbo/jimbo/main/Main.java
    public static void main(String[] args) throws IOException {
        final ElasticsearchHandler elasticsearchHandler = new ElasticsearchHandler(blockingQueue, new ElasticsearchSetting());
=======

    @Override
    public void run() {
//        final ElasticsearchThreadFactory elasticsearchThreadFactory = new ElasticsearchThreadFactory(blockingQueue);
//        elasticsearchThreadFactory.createNewThread();
>>>>>>> e8a7c6e7bfef0d257e875ec8732638b47e24ee80:newsfetcher/src/main/java/ir/sahab/nimbo/jimbo/main/Exec.java
        final List<NewsSite> urls = Seeder.getInstance().getUrls();
        int rssNumber = urls.size();
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
        HashSet<String> rssMessages = new HashSet<>();
        ex.scheduleAtFixedRate(new Runnable() {
            private int i = 0;

            @Override
            public void run() {
                new Thread(() -> {
                    NewsSite url;
                    try {
                        url = urls.get(i);
                    } catch (IndexOutOfBoundsException e) {
                        i = 0;
                        url = urls.get(i);
                    }
                    RssFeedParser parser = new RssFeedParser(url.getUrl());
                    RssFeed rssFeed = parser.readFeed();
                    for (RssFeedMessage message : rssFeed.getMessages()) {
                        if (rssMessages.add(message.getLink())) {
                            String text = fetch(message.getLink(), url);
<<<<<<< HEAD:newsfetcher/src/main/java/ir/sahab/nimbo/jimbo/main/Main.java
                            if(text != null) {
                                System.out.println(message.getLink());
=======
                            if (text != null) {
>>>>>>> e8a7c6e7bfef0d257e875ec8732638b47e24ee80:newsfetcher/src/main/java/ir/sahab/nimbo/jimbo/main/Exec.java
                                blockingQueue.add(new ElasticsearchWebpageModel(message.getLink(), text, message.getTitle(), message.getDescription()));
                            }
                        }
                    }
                    i++;
                }
                ).run();
            }
<<<<<<< HEAD:newsfetcher/src/main/java/ir/sahab/nimbo/jimbo/main/Main.java
        }, 0, 60/rssNumber, TimeUnit.SECONDS);
        elasticsearchHandler.run();
=======
        }, 0, 60 / rssNumber, TimeUnit.SECONDS);
        while (true) {
            try {
                System.out.println(blockingQueue.take().getArticle());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
>>>>>>> e8a7c6e7bfef0d257e875ec8732638b47e24ee80:newsfetcher/src/main/java/ir/sahab/nimbo/jimbo/main/Exec.java
    }
}
