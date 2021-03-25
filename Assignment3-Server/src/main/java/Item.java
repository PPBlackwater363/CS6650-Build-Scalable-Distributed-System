import java.io.Serializable;

public class Item implements Serializable {
    private String storeID;
    private String clientID;
    private String itemID;
    private String date;

    public Item(String storeID, String clientID, String itemID, String date) {
        this.storeID = storeID;
        this.clientID = clientID;
        this.itemID = itemID;
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

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}