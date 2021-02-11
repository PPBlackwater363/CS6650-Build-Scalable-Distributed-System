package io.swagger.client.model;
import java.io.*;
import java.util.*;

public class Configuration {
    private int maxStores;
    private int numCustomers;
    private int maxItemID;
    private int numPurchases;
    private int numItemPerPurchase;
    private int date;
    private String baseUrl;

    public static Configuration readFromProperties(String fileName) throws IOException {
        FileInputStream fis = null;
        Properties prop = null;

        try {
            fis = new FileInputStream(fileName);
            prop = new Properties();
            prop.load(fis);
        } catch(IOException fnfe) {
            fnfe.printStackTrace();
        } finally {
            assert fis != null;
            fis.close();
        }

        return createConfiguration(prop);
    }

    private static Configuration createConfiguration(Properties prop) {
        String maxStoresInput = prop.getProperty("maxStores");
        String numCustomersInput = prop.getProperty("numCustomers");
        String maxItemIDInput = prop.getProperty("maxItemID");
        String numPurchasesInput = prop.getProperty("numPurchases");
        String numItemPerPurchaseInput = prop.getProperty("numItemPerPurchase");
        String dateInput = prop.getProperty("date");
        String baseUrlInput = prop.getProperty("baseUrl");

        if (baseUrlInput == null || maxStoresInput == null) {
            throw new IllegalArgumentException("Missing store number or base url");
        }

        try {
            int maxStores = Integer.parseInt(maxStoresInput);
            int numCustomers = numCustomersInput == null ? 1000 : Integer.parseInt(numCustomersInput);
            int maxItemID = maxItemIDInput == null ? 100000 : Integer.parseInt(maxItemIDInput);
            int numPurchases = numPurchasesInput == null ? 60 : Integer.parseInt(numPurchasesInput);
            int numItemPerPurchase = numItemPerPurchaseInput == null ? 5 : Integer.parseInt(numItemPerPurchaseInput);
            int date = dateInput == null ? 20210101 : Integer.parseInt(dateInput);

            if (numCustomers <= 0) {
                throw new IllegalArgumentException("Number of customers should be positive.");
            }

            if (numItemPerPurchase > 20 || numItemPerPurchase < 1) {
                throw new IllegalArgumentException("Number of items for each purchase should between 1 and 20");
            }

            return new Configuration(maxStores, numCustomers,maxItemID, numPurchases, numItemPerPurchase, date, baseUrlInput);
        } catch(NumberFormatException e) {
            throw new NumberFormatException("Parsing configuration failed");
        }
    }

    private Configuration(int maxStores, int numCustomers, int maxItemID, int numPurchases, int numItemPerPurchase,
                          int date, String baseUrl) {
        this.maxStores = maxStores;
        this.numCustomers = numCustomers;
        this.maxItemID = maxItemID;
        this.numPurchases = numPurchases;
        this.numItemPerPurchase = numItemPerPurchase;
        this.date = date;
        this.baseUrl = baseUrl;
    }

    public int getMaxStores() {
        return maxStores;
    }

    public void setMaxStores(int maxStores) {
        this.maxStores = maxStores;
    }

    public int getNumCustomers() {
        return numCustomers;
    }

    public void setNumCustomers(int numCustomers) {
        this.numCustomers = numCustomers;
    }

    public int getMaxItemID() {
        return maxItemID;
    }

    public void setMaxItemID(int maxItemID) {
        this.maxItemID = maxItemID;
    }

    public int getNumPurchases() {
        return numPurchases;
    }

    public void setNumPurchases(int numPurchases) {
        this.numPurchases = numPurchases;
    }

    public int getNumItemPerPurchase() {
        return numItemPerPurchase;
    }

    public void setNumItemPerPurchase(int numItemPerPurchase) {
        this.numItemPerPurchase = numItemPerPurchase;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
