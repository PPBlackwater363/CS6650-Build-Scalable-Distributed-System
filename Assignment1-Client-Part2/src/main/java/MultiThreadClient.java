import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadClient {

    public static void main(String[] args) {
        try {

            Random random = new Random();
            final int storeID = 56;
            final int custID = random.nextInt(storeID) +  storeID * 1000;
            final String date = "20210101";

            final Configuration configuration = Configuration.readFromProperties("configuration.properties");
            final StringBuilder csvContent = new StringBuilder();
            final CSVWriter writer = new CSVWriter(csvContent);
            csvContent.append("Start Time");
            csvContent.append(",");
            csvContent.append("Request Type");
            csvContent.append(",");
            csvContent.append("Latency");
            csvContent.append(",");
            csvContent.append("Response Code");
            csvContent.append("\n");

            final PerformanceEvaluator performanceEvaluator = new PerformanceEvaluator(new AtomicInteger(0), new AtomicInteger(0));

            final int maxThreads = Integer.parseInt(args[0]);
            final int numThreads = maxThreads  / 4;
            final int startPurchaseID = 1;
            final int endPurchaseID = maxThreads;
            final int phaseEastStartTime = 1;
            final int phaseEastEndTime = configuration.getNumPurchases() * 3;
            final int numPosts = configuration.getNumPurchases();
//            System.out.println(numPosts);
            final CountDownLatch latchForMiddle = new CountDownLatch(1);
            final CountDownLatch latchForWest = new CountDownLatch(1);

            // Phase 1, the east phase, will launch (maxStores/4) threads.

            int PhaseEast = 0;

            Runnable runPhaseEast = new Runnable() {
                @Override
                public void run() {
                    try {
                        execute(numThreads, startPurchaseID, endPurchaseID, phaseEastStartTime, phaseEastEndTime,
                                numPosts, configuration, latchForMiddle, latchForWest, performanceEvaluator,
                                storeID, custID, date, writer, Region.EAST);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            long startTime = System.currentTimeMillis();
            System.out.println("Phase East started.");
            Thread phaseEast = new Thread(runPhaseEast);
            phaseEast.start();
            latchForMiddle.await();

            final int phaseCentralStartTime = configuration.getNumPurchases() * 3;
            final int phaseCentralEndTime = configuration.getNumPurchases() * 5;
            final CountDownLatch phaseCentralNext = new CountDownLatch(maxThreads / 10);
            final CountDownLatch phaseCentralCountDown = new CountDownLatch(maxThreads / 4);


            // Phase 2 After any store thread has sent 5 hours of purchases (numPurchasesx5),
            // launch the remaining (maxStores/2) threads - the west phase.
//            while((performanceEvaluator.getNumSuccessfulRequest().get() + performanceEvaluator.getNumUnsuccessfulRequest().get()) < configuration.getNumPurchases() * 3 - 1) {
////                System.out.println("while amount" + (performanceEvaluator.getNumSuccessfulRequest().get() + performanceEvaluator.getNumUnsuccessfulRequest().get()));
//            }

            Runnable runPhaseCentral = new Runnable() {
                @Override
                public void run() {
                    try {
                        execute(numThreads, startPurchaseID, endPurchaseID, phaseCentralStartTime, phaseCentralEndTime,
                                numPosts, configuration, null, latchForMiddle, performanceEvaluator,
                                storeID, custID, date, writer, Region.MIDDLE);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            System.out.println("Phase Central started.");
            Thread phaseCentral = new Thread(runPhaseCentral);
            phaseCentral.start();
            latchForWest.await();
//            phaseCentralNext.await();
//            phaseEastNext.await();

            final int phaseWestStartTime = configuration.getNumPurchases() * 5;
            final CountDownLatch phaseWestCountDown = new CountDownLatch(maxThreads/2);


            // After any store thread has sent 5 hours of purchases (numPurchasesx5), launch the remaining (maxStores/2) threads - the west phase.
            while((performanceEvaluator.getNumSuccessfulRequest().get() + performanceEvaluator.getNumUnsuccessfulRequest().get()) < configuration.getNumPurchases() * 5 -1) {
//                System.out.println("while amount" + (performanceEvaluator.getNumSuccessfulRequest().get() + performanceEvaluator.getNumUnsuccessfulRequest().get()));
            }

            Runnable runPhaseWest = new Runnable() {
                @Override
                public void run() {
                    try {
                        execute(maxThreads/2, startPurchaseID, endPurchaseID, phaseWestStartTime, Integer.MAX_VALUE,
                                numPosts, configuration, null, null, performanceEvaluator,
                                storeID, custID, date, writer, Region.WEST);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            System.out.println("Phase West started.");
            Thread phaseWest = new Thread(runPhaseWest);
            phaseWest.start();


            phaseEast.join();
            System.out.println("East Phase completed.");

            phaseCentral.join();
            System.out.println("Central Phase completed.");

            phaseWest.join();
            System.out.println("West Phase completed.");

            long endTime = System.currentTimeMillis();

            int totalRequest = performanceEvaluator.getNumSuccessfulRequest().get() + performanceEvaluator.getNumUnsuccessfulRequest().get();
            int numSuccessfulRequest = performanceEvaluator.getNumSuccessfulRequest().get();
            int numUnSuccessfulRequest = performanceEvaluator.getNumUnsuccessfulRequest().get();
            long wallTime = endTime - startTime;
            long throughput = (long)totalRequest / (wallTime / 1000);

            System.out.println();
            System.out.println("Number of requests: " + totalRequest);
            System.out.println("Number of successful requests: " + numSuccessfulRequest);
            System.out.println("Number of unsuccessful requests: " + numUnSuccessfulRequest);
            System.out.println("Total run time (wall time): " + wallTime);
            System.out.println("Throughput: " + throughput);

            long csvEnd = System.currentTimeMillis();

//            List<Integer> postList = writer.getPostList();
//            Collections.sort(postList);

            List<long[]> timeList = writer.getTimeList();

//            System.out.println("size:" + timeList.size());

            List<Integer> postList = new ArrayList<>();
            int sumPost = 0;
            for (int i = 0; i < timeList.size(); i++) {
//                System.out.println(postList.get(i));
                long start = timeList.get(i)[0];
                long end = timeList.get(i)[1];
                long latency = end - start;
//                System.out.println(start);
//                System.out.println(latency);
                writer.writeRecord(start, "POST", latency, 200);
                sumPost += latency;
                postList.add((int)latency);
            }
            writer.exportFile();
//            System.out.println(sumPost);
//            System.out.println(totalRequest);

//            int sumPost = postList.stream().mapToInt(Integer::intValue).sum();
            int meanPostResponseTime = sumPost / totalRequest;
            int medianPostResponseTime = postList.get(postList.size() / 2);
//            int meanPostResponseTime = sumPost / maxThreads;
//            int medianPostResponseTime = postList.get(maxThreads / 2);
            long totalWallTime = csvEnd - startTime;
            long totalThroughput = (totalRequest) / (totalWallTime / 1000);
            int p99Post = postList.get(postList.size() * 99 / 100);
            int maxPostResponseTime = postList.get(postList.size() - 1);

            System.out.println();
            System.out.println("Posts mean response time: " + meanPostResponseTime);
            System.out.println("Posts median response time: " + medianPostResponseTime);
            System.out.println("Total run time (wall time): " + totalWallTime);
            System.out.println("p99 response time for POSTs: " + p99Post);
            System.out.println("Max response time for POSTs: " + maxPostResponseTime);


            long ratio = ((long) totalWallTime - (long) throughput) / (long) throughput;
            System.out.println("Wall time / throughput: " + ratio);



        } catch(IOException | InterruptedException e) {
            System.err.println("Exception when calling PurchaseApi");
            e.printStackTrace();
        }
    }

    public static void execute(int numThreads, int startPurchaseID, int endPurchaseID, int startTime, int endTime,
                               int numPost, Configuration configuration, CountDownLatch next, CountDownLatch current,
                               PerformanceEvaluator performanceEvaluator, int storeID, int custID, String date, CSVWriter writer, Region region) throws InterruptedException {
        int purchasePer = endPurchaseID / numThreads;
        int start = startPurchaseID;
        int end = purchasePer;
        Thread [] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
//            System.out.println(i);
            long csvStartTime = System.currentTimeMillis();
            if (i == numThreads - 1) {
                end = configuration.getNumPurchases();
            }
//            System.out.println("numPost" + numPost);
            PhaseThread thread = new PhaseThread(startPurchaseID, endPurchaseID, startTime, endTime, numPost, configuration, current, next, performanceEvaluator,
                    writer, storeID, custID, date, region);
            threads[i] = new Thread(thread);
            threads[i].start();;
            start = end + 1;
            end += purchasePer;
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }
//        System.out.println("Executes Successfully.");
//        current.await();
    }
}
