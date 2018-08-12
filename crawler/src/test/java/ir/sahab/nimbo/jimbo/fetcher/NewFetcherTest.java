package ir.sahab.nimbo.jimbo.fetcher;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class NewFetcherTest {

    private final int queueSize = 10000;
    private final ArrayBlockingQueue<String> shuffledLinksQueue = new ArrayBlockingQueue<>(queueSize);
    private final ArrayBlockingQueue<String> webPagesQueue = new ArrayBlockingQueue<>(queueSize);

    private final int threadCount = 1;
    private final FetcherSetting fetcherSetting = new FetcherSetting(threadCount);

    @Test
    public void testFetch() throws InterruptedException, ExecutionException {

        NewFetcher newFetcher = new NewFetcher(shuffledLinksQueue, webPagesQueue, fetcherSetting);
        List<Future<HttpResponse>> futures = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            futures.add(newFetcher.fetch("https://en.wikipedia.org/wiki/" + i));
        }

        for (int i = 0; i < futures.size(); i++) {
            Future<HttpResponse> responseFuture = futures.get(i);

            responseFuture.get();

            System.out.println("document - number " + i);
        }

    }

    @Test
    public void testClient3() throws InterruptedException, ExecutionException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException {
        List<Future<HttpResponse>> futures = new ArrayList<>();
        NewFetcher newFetcher = new NewFetcher(shuffledLinksQueue, webPagesQueue, fetcherSetting);
        CloseableHttpAsyncClient client = newFetcher.getClient(0);

        for (int i = 0; i < 200; i++) {
            futures.add(client.execute(new HttpGet("https://en.wikipedia.org/wiki/" + i), null));
        }

        for (int i = 0; i < 200; i++) {
            futures.add(newFetcher.fetch("https://en.wikipedia.org/wiki/" + i));
        }

        for (int i = 0; i < futures.size(); i++) {
            Future<HttpResponse> responseFuture = futures.get(i);

            long time = System.currentTimeMillis();
            EntityUtils.toString(responseFuture.get().getEntity());
            System.out.println( i + " ----> " + (System.currentTimeMillis() - time));
        }

        client.close();
    }

    @Test
    public void testFetchMultiClient() throws InterruptedException, ExecutionException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException {

        final int clientsCount = threadCount/10 + 1 ;
        final List<Future<HttpResponse>> futures = new ArrayList<>();
        final NewFetcher newFetcher = new NewFetcher(shuffledLinksQueue, webPagesQueue, fetcherSetting);

        final CloseableHttpAsyncClient clients[] = new CloseableHttpAsyncClient[clientsCount];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = newFetcher.getClient(i);
        }

        for (int i = 0; i < 2000; i++) {
            CloseableHttpAsyncClient client = clients[i%clientsCount];

            HttpGet request = new HttpGet("https://en.wikipedia.org/wiki/" + i);
            Future<HttpResponse> future = client.execute(request, null);
            futures.add(future);
        }

        for (int i = 0; i < futures.size(); i++) {
            Future<HttpResponse> responseFuture = futures.get(i);

//            EntityUtils.toString(responseFuture.get().getEntity());
            responseFuture.get();

            System.out.println("document number " + i);
        }

        for (CloseableHttpAsyncClient client: clients) {
            client.close();
        }
    }

    @Test
    public void createClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        NewFetcher newFetcher = new NewFetcher(shuffledLinksQueue, webPagesQueue, fetcherSetting);
        CloseableHttpAsyncClient client = newFetcher.getClient(0);

        client.close();
    }

    @Test
    public void fetch() throws InterruptedException {
        NewFetcher newFetcher = new NewFetcher(shuffledLinksQueue, webPagesQueue, fetcherSetting);

        List<Future<HttpResponse>> futures = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            futures.add(newFetcher.fetch("https://en.wikipedia.org/wiki/" + i));
        }

        Runnable runnable = () -> {
            for (int i = 0; i < futures.size(); i++) {

                final long time = System.currentTimeMillis();
                try {
                    futures.get(i).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                System.out.println(i + " -> " + (System.currentTimeMillis() - time));
            }
        };

        new Thread(runnable).start();
        Thread.sleep(200000);
    }

    @Test
    public void testWorkers() throws InterruptedException {
        NewFetcher newFetcher = new NewFetcher(shuffledLinksQueue, webPagesQueue, fetcherSetting);

        for (int i = 0; i < 200; i++) {
            shuffledLinksQueue.put("https://en.wikipedia.org/wiki/" + i);
        }

        new Thread(newFetcher).start();

        while(true) {
            System.out.println(webPagesQueue.size());
            if (webPagesQueue.size() == 200) {
                newFetcher.stop();
                break;
            }

            Thread.sleep(1000);
        }
    }
}