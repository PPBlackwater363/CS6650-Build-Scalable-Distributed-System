import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadClient {

    public static void main(String[] args) {

//        PurchaseApi apiInstance = new PurchaseApi();
//        Purchase body = new Purchase(); // Purchase | items purchased
//        Integer storeID = 56; // Integer | ID of the store the purchase takes place at
//        Integer custID = 56; // Integer | customer ID making purchase
//        String date = "20210101"; // String | date of purchase
//        try {
//            apiInstance.newPurchase(body, storeID, custID, date);
//        } catch (ApiException e) {
//            System.err.println("Exception when calling PurchaseApi#newPurchase");
//            e.printStackTrace();
//        }

        try {
            Random random = new Random();
            final int storeID = 56;
            final int custID = random.nextInt(storeID) +  storeID * 1000;
            final String date = "20210101";
            final Configuration configuration = Configuration.readFromProperties("configuration.properties");
            final PerformanceEvaluator performanceEvaluator = new PerformanceEvaluator(new AtomicInteger(0), new AtomicInteger(0));

//            final int maxThreads = configuration.getMaxStores();
            // get the total number of threads
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

            Runnable runPhaseEast = new Runnable() {
                @Override
                public void run() {
                    try {
                        execute(numThreads, startPurchaseID, endPurchaseID, phaseEastStartTime, phaseEastEndTime,
                                numPosts, configuration, latchForMiddle, latchForWest, performanceEvaluator,
                                storeID, custID, date, Region.EAST);
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

            final int phaseCentralStartTime = configuration.getNumPurchases() * 3 + 1;
            final int phaseCentralEndTime = configuration.getNumPurchases() * 5;


//            System.out.println("amount" + (performanceEvaluator.getNumUnsuccessfulRequest().get() + performanceEvaluator.getNumUnsuccessfulRequest().get()));

            Runnable runPhaseCentral = new Runnable() {
                @Override
                public void run() {
                    try {
                        execute(numThreads, startPurchaseID, endPurchaseID, phaseCentralStartTime, phaseCentralEndTime,
                                numPosts, configuration, null, latchForMiddle, performanceEvaluator,
                                storeID, custID, date, Region.MIDDLE);
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

            final int phaseWestStartTime = configuration.getNumPurchases() * 5 + 1;
//            final int phaseWestEndTime = configuration.getNumPurchases() * 7;
            final CountDownLatch phaseWestCountDown = new CountDownLatch(maxThreads/2);



            Runnable runPhaseWest = new Runnable() {
                @Override
                public void run() {
                    try {
                        execute(maxThreads/2, startPurchaseID, endPurchaseID, phaseWestStartTime, Integer.MAX_VALUE,
                                numPosts, configuration, null, null, performanceEvaluator,
                                storeID, custID, date, Region.WEST);
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
            long throughput = (long)totalRequest * 1000 / wallTime ;
            long ratio = ((long)wallTime - (long) throughput) / (long) throughput;

            System.out.println();
            System.out.println("Number of requests: " + totalRequest);
            System.out.println("Number of successful requests: " + numSuccessfulRequest);
            System.out.println("Number of unsuccessful requests: " + numUnSuccessfulRequest);
            System.out.println("Total run time (wall time): " + wallTime);
            System.out.println("Throughput: " + throughput);
            System.out.println("Wall time / throughput: " + ratio);


        } catch(IOException | InterruptedException e) {
            System.err.println("Exception when calling PurchaseApi");
            e.printStackTrace();
        }
    }

    public static void execute(int numThreads, int startPurchaseID, int endPurchaseID, int startTime, int endTime,
                               int numPost, Configuration configuration, CountDownLatch current, CountDownLatch next,
                               PerformanceEvaluator performanceEvaluator, int storeID, int custID, String date, Region region) throws InterruptedException {
        int purchasePer = endPurchaseID / numThreads;
        int start = startPurchaseID;
        int end = purchasePer;
        Thread [] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
//            System.out.println(i);
            if (i == numThreads - 1) {
                end = configuration.getNumPurchases();
            }
//            System.out.println("numPost" + numPost);
            PhaseThread thread = new PhaseThread(startPurchaseID, endPurchaseID, startTime, endTime, numPost, configuration, current, next, performanceEvaluator,
                    storeID, custID, date, region);
            threads[i] = new Thread(thread);
            threads[i].start();;
            start = end + 1;
            end += purchasePer;
        }
//        System.out.println("Executes Successfully.");
//        current.await();
        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }

    }
}
