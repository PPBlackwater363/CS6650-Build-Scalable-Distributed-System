import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.model.Configuration;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import io.swagger.client.api.PurchaseApi;

//import org.apache.logging.log4j.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;


public class PhaseThread implements Runnable {
    private int startPurchaseIDs;
    private int endPurchaseIDs;
    private int startTime;
    private int endTime;
    private int numPost;
    private int storeID;
    private int custID;
    private String date;
    private Configuration configuration;
    private CountDownLatch currentLatch;
    private CountDownLatch nextLatch;
    private PurchaseApi apiInstance;
    private ThreadLocalRandom randomGenerator;
    private PerformanceEvaluator performanceEvaluator;
    private CSVWriter writer;

//    private static final Logger logger = LogManager.getLogger();

    public PhaseThread(int startPurchaseIDs, int endPurchaseIDs, int startTime, int endTime, int numPost,
                       Configuration configuration, CountDownLatch currentLatch, CountDownLatch nextLatch,
                       PerformanceEvaluator performanceEvaluator, CSVWriter writer, int storeID, int custID, String date) {
        this.startPurchaseIDs = startPurchaseIDs;
        this.endPurchaseIDs = endPurchaseIDs;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numPost = numPost;
        this.configuration = configuration;
        this.currentLatch = currentLatch;
        this.nextLatch = nextLatch;
        this.performanceEvaluator = performanceEvaluator;
        this.storeID = storeID;
        this.custID = custID;
        this.date = date;
        this.writer = writer;

        this.apiInstance = new PurchaseApi();
        apiInstance.getApiClient().setBasePath(this.configuration.getBaseUrl());
        this.randomGenerator = ThreadLocalRandom.current();

    }
    @Override
    public void run() {
        try {
//            System.out.println("send post");
            doPost();
            currentLatch.countDown();
//            if (nextLatch != null) {
//                nextLatch.countDown();
//            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private void doPost() throws  ApiException {
        Purchase purchase = new Purchase();
        PurchaseItems purchaseItems = new PurchaseItems();
        purchaseItems.setNumberOfItems(this.configuration.getNumItemPerPurchase());
//        System.out.println("c1");

        int numOfItems = this.configuration.getNumItemPerPurchase();
        // every store runs for 9 hours per day
        for (int a = 0; a < 9; a++) {
            for (int i = 0; i < numPost; i++) {
                for (int j = 0; j < numOfItems; j++) {

                    PurchaseItems currPurchaseItems = new PurchaseItems();
                    currPurchaseItems.setNumberOfItems(1);
                    String currItemID = this.randomValue(1, this.configuration.getMaxItemID());
                    currPurchaseItems.setItemID(currItemID);
                    purchase.addItemsItem(currPurchaseItems);
                }
                try {
                    long startTime = System.currentTimeMillis();
//                    System.out.println("c2");
                    ApiResponse<Void> response = apiInstance.newPurchaseWithHttpInfo(purchase, this.storeID, this.custID, this.date);
                    long endTime = System.currentTimeMillis();
                    long[] curr = new long[2];
//                    System.out.println("startTime" + startTime);
                    curr[0] = startTime;
                    curr[1] = endTime;
                    writer.timeList.add(curr);
//                    writer.writeRecord(startTime, "POST", endTime - startTime, response.getStatusCode());
                    performanceEvaluator.getNumSuccessfulRequest().getAndIncrement();
                } catch (ApiException e) {
                    performanceEvaluator.getNumUnsuccessfulRequest().getAndIncrement();
//                logger.info("Error occurred while communicating with the server: " + e.getMessage());
                }
            }

        }
    }

    public String randomValue(int low, int high) {
        return String.valueOf(randomGenerator.nextInt(low, high + 1));
    }

    public int getStartPurchaseIDs() {
        return startPurchaseIDs;
    }

    public void setStartPurchaseIDs(int startPurchaseIDs) {
        this.startPurchaseIDs = startPurchaseIDs;
    }

    public int getEndPurchaseIDs() {
        return endPurchaseIDs;
    }

    public void setEndPurchaseIDs(int endPurchaseIDs) {
        this.endPurchaseIDs = endPurchaseIDs;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getNumPost() {
        return numPost;
    }

    public void setNumPost(int numPost) {
        this.numPost = numPost;
    }

    public int getStoreID() {
        return storeID;
    }

    public void setStoreID(int storeID) {
        this.storeID = storeID;
    }

    public int getCustID() {
        return custID;
    }

    public void setCustID(int custID) {
        this.custID = custID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public CountDownLatch getCurrentLatch() {
        return currentLatch;
    }

    public void setCurrentLatch(CountDownLatch currentLatch) {
        this.currentLatch = currentLatch;
    }

    public CountDownLatch getNextLatch() {
        return nextLatch;
    }

    public void setNextLatch(CountDownLatch nextLatch) {
        this.nextLatch = nextLatch;
    }

    public PurchaseApi getApiInstance() {
        return apiInstance;
    }

    public void setApiInstance(PurchaseApi apiInstance) {
        this.apiInstance = apiInstance;
    }

    public ThreadLocalRandom getRandomGenerator() {
        return randomGenerator;
    }

    public void setRandomGenerator(ThreadLocalRandom randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public PerformanceEvaluator getPerformanceEvaluator() {
        return performanceEvaluator;
    }

    public void setPerformanceEvaluator(PerformanceEvaluator performanceEvaluator) {
        this.performanceEvaluator = performanceEvaluator;
    }

    public CSVWriter getWriter() {
        return writer;
    }

    //    public static Logger getLogger() {
//        return logger;
//    }
}
