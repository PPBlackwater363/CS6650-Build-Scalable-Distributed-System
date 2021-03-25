public class Purchase {
    private String storeID;
    private String clientID;
    private String date;
    private String item1ID;
    private String item2ID;
    private String item3ID;
    private String item4ID;
    private String item5ID;

    public Purchase() {
    }

    public Purchase(String storeID, String clientID, String date, String item1ID, String item2ID, String item3ID, String item4ID, String item5ID) {
        this.storeID = storeID;
        this.clientID = clientID;
        this.date = date;
        this.item1ID = item1ID;
        this.item2ID = item2ID;
        this.item3ID = item3ID;
        this.item4ID = item4ID;
        this.item5ID = item5ID;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getItem1ID() {
        return item1ID;
    }

    public void setItem1ID(String item1ID) {
        this.item1ID = item1ID;
    }

    public String getItem2ID() {
        return item2ID;
    }

    public void setItem2ID(String item2ID) {
        this.item2ID = item2ID;
    }

    public String getItem3ID() {
        return item3ID;
    }

    public void setItem3ID(String item3ID) {
        this.item3ID = item3ID;
    }

    public String getItem4ID() {
        return item4ID;
    }

    public void setItem4ID(String item4ID) {
        this.item4ID = item4ID;
    }

    public String getItem5ID() {
        return item5ID;
    }

    public void setItem5ID(String item5ID) {
        this.item5ID = item5ID;
    }
}
