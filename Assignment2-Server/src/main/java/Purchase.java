
public class Purchase {
    private String storeID;
    private String clientID;
    private String date;

    public Purchase(String storeID, String clientID, String date) {
        this.storeID = storeID;
        this.clientID = clientID;
        this.date = date;
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
}
