package ir.sahab.nimbo.jimbo.fetcher;

import ir.sahab.nimbo.jimbo.kafaconfig.KafkaTopics;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;

public class Fetcher implements Runnable {
    private final Consumer<Long, String> consumer;
    private final Producer<Long, String> producer;
    private final ArrayBlockingQueue queue;

    Fetcher(ArrayBlockingQueue queue, Consumer<Long, String> consumer, Producer<Long, String> producer) {
        this.queue = queue;
        this.consumer = consumer;
        this.producer = producer;
    }

    /**
     * @return The HTML as a document
     * @throws IOException when URL does not link to a valid page
     */
    private Document getUrlBody(URL url) throws IOException {
        LruCache lruCache = LruCache.getInstance();
        String host = url.getHost();
        if (!lruCache.exist(host)) {
            lruCache.add(host);
            return Jsoup.connect(url.toString()).validateTLSCertificates(false).get();
        }
        return null;
    }

    private void linkProcess(String url) {
        try {
            Document body = getUrlBody(new URL(url));
            if (!Validate.isValid(body))
                return;
            if (body == null) {
                producer.send(new ProducerRecord<>(KafkaTopics.URL_FRONTIER.toString(),
                        null, url));
            } else {
                System.out.println("hello");
                queue.put(body);
            }
        } catch (UnsupportedMimeTypeException e) {
//            Logger.getInstance().logToFile(e.getMessage());
            System.err.println("Unsupported mime : " + url);
        } catch (MalformedURLException e) {
//            Logger.getInstance().logToFile(e.getMessage());
            System.err.println("malformed url :  " + url);
        } catch (IOException e) {
//            Logger.getInstance().logToFile(e.getMessage());
            System.err.println("io exception : " + url);
        } catch (InterruptedException e) {
            System.err.println("INTERRUPTED EXCEPTION!!!");
        }
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            final ConsumerRecords<Long, String> consumerRecords;
            synchronized (consumer) {
                consumerRecords = consumer.poll(500);
                consumer.commitAsync();
            }
            consumerRecords.forEach(record -> linkProcess(record.value()));
        }
    }


}
