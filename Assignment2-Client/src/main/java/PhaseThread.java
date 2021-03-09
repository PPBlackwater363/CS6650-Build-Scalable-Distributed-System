import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.model.Configuration;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import io.swagger.client.api.PurchaseApi;
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
    private Region region;

    public PhaseThread(int startPurchaseIDs, int endPurchaseIDs, int startTime, int endTime, int numPost,
                       Configuration configuration, CountDownLatch currentLatch, CountDownLatch nextLatch,
                       PerformanceEvaluator performanceEvaluator, CSVWriter writer, int storeID, int custID, String date, Region region) {
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
        this.region = region;
        ApiClient client = new ApiClient();
        client.setConnectTimeout(60000);
        client.setReadTimeout(60000);
        this.apiInstance = new PurchaseApi(client);
        apiInstance.getApiClient().setBasePath(this.configuration.getBaseUrl());
        this.randomGenerator = ThreadLocalRandom.current();

    }
    @Override
    public void run() {
        try {
            doPost();
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private void doPost() throws  ApiException {

        Purchase purchase = new Purchase();
        PurchaseItems purchaseItems = new PurchaseItems();
        purchaseItems.setNumberOfItems(this.configuration.getNumItemPerPurchase());

        int numOfItems = this.configuration.getNumItemPerPurchase();

        for (int j = 0; j < numOfItems; j++) {
            PurchaseItems currPurchaseItems = new PurchaseItems();
            currPurchaseItems.setNumberOfItems(1);
            String currItemID = this.randomValue(1, this.configuration.getMaxItemID());
            currPurchaseItems.setItemID(currItemID);
            purchase.addItemsItem(currPurchaseItems);
        }

        // every store runs for 9 hours per day
        for (int a = 0; a < 9; a++) {
            for (int i = 0; i < numPost; i++) {
                try {
                    long startTime = System.currentTimeMillis();
                    ApiResponse<Void> response = apiInstance.newPurchaseWithHttpInfo(purchase, this.storeID, this.custID, this.date);
                    long endTime = System.currentTimeMillis();
                    long[] curr = new long[2];
                    curr[0] = startTime;
                    curr[1] = endTime;
                    writer.timeList.add(curr);
//                    writer.writeRecord(startTime, "POST", endTime - startTime, response.getStatusCode());
                    performanceEvaluator.getNumSuccessfulRequest().getAndIncrement();
                } catch (ApiException e) {
                    performanceEvaluator.getNumUnsuccessfulRequest().getAndIncrement();

                }
            }
            if (currentLatch != null && a==3 && (this.region == Region.EAST)) {
                currentLatch.countDown();
            }

            if (nextLatch != null && a==6 && (this.region == Region.EAST || this.region == Region.MIDDLE)) {
                nextLatch.countDown();
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

}
