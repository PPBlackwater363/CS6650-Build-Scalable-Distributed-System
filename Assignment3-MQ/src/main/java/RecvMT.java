import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RecvMT {

    private final static String QUEUE_NAME = "threadExQ1";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                        final Channel channel = connection.createChannel();
                        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                        // max one message per receiver
                        channel.basicQos(1);
                        System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
                        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), "UTF-8");
//                        System.out.println("message");
                        Gson gson = new Gson();
//                        System.out.println("bbb");
//                        Item item = null;
                        Purchase purchase = gson.fromJson(message, Purchase.class);
//                        try {
//                            item = deserialize(delivery.getBody());
//                        } catch (ClassNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                        writeToDatabase(item);
                        writeToDatabase2(purchase);
//                        System.out.println("aaa");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            System.out.println(message);
                    };
                    // process messages
                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
                } catch (IOException ex) {
                    Logger.getLogger(RecvMT.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        // start threads and block to receive messages
        for (int i = 0; i < 1000; i++) {
            Thread recv = new Thread(runnable);
            recv.start();
        }
    }

    public static Item deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (Item) is.readObject();
    }

    public static void writeToDatabase(Item item) {
        java.sql.Connection connection = null;
        PreparedStatement insertStmt = null;
        String insertQueryStatement = "INSERT INTO purchaseItem (storeId, clientId, itemId, numOfItem, date) VALUES (?, ?, ?, ?, ?);";

        try {
            connection = ConnectionManager.getDataSource().getConnection();
            insertStmt = connection.prepareStatement(insertQueryStatement);
            insertStmt.setString(1, item.getStoreID());
            insertStmt.setString(2, item.getClientID());
            insertStmt.setString(3, item.getItemID());
            insertStmt.setString(4, "1");
            insertStmt.setString(5, item.getDate());
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (insertStmt != null) {
                    insertStmt.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void writeToDatabase2(Purchase purchase) {
        java.sql.Connection connection = null;
        PreparedStatement insertStmt = null;
        String insertQueryStatement = "INSERT INTO purchaseItem (storeId, clientId, itemId, numOfItem, date) VALUES (?, ?, ?, ?, ?);";

        try {
            connection = ConnectionManager.getDataSource().getConnection();
            insertStmt = connection.prepareStatement(insertQueryStatement);
            insertStmt.setString(1, purchase.getStoreID());
            insertStmt.setString(2, purchase.getClientID());
            insertStmt.setString(3, purchase.getItem1ID());
            insertStmt.setString(4, "1");
            insertStmt.setString(5, purchase.getDate());
            insertStmt.executeUpdate();
            insertStmt.setString(3, purchase.getItem2ID());
            insertStmt.executeUpdate();
            insertStmt.setString(3, purchase.getItem3ID());
            insertStmt.executeUpdate();
            insertStmt.setString(3, purchase.getItem4ID());
            insertStmt.executeUpdate();
            insertStmt.setString(3, purchase.getItem5ID());
            insertStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (insertStmt != null) {
                    insertStmt.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

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
}