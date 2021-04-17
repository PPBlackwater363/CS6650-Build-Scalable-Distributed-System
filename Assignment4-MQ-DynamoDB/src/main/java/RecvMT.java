import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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

import java.util.UUID;

import java.util.Arrays;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import org.bson.Document;


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

//                        writeToDatabase(item);
//                        writeToDatabase2(purchase);
                        writeToMongoDB(purchase);
//                            writeToDynamoDB(purchase);
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

    public static void writeToDynamoDB(Purchase purchase) {

        System.out.println("Write to DynamoDB...");

        // Build connection
        try {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
            DynamoDB dynamoDB = new DynamoDB(client);

            Table table = dynamoDB.getTable("Request");

            UUID uuid = UUID.randomUUID();
            String pKey = uuid.toString();

            String storeId = purchase.getStoreID();
            String clientId = purchase.getClientID();
            String date = purchase.getDate();
            String item1Id = purchase.getItem1ID();
            String item2Id = purchase.getItem2ID();
            String item3Id = purchase.getItem3ID();
            String item4Id = purchase.getItem4ID();
            String item5Id = purchase.getItem5ID();

            Item item = new Item()
                    .withPrimaryKey("pkey", pKey)
                    .withString("storeId", storeId)
                    .withString("clientId", clientId)
                    .withString("date", date)
                    .withString("item1Id", item1Id)
                    .withString("item2Id", item2Id)
                    .withString("item3Id", item3Id)
                    .withString("item4Id", item4Id)
                    .withString("item5Id", item5Id);

            // Write the item to the table
            PutItemOutcome outcome = table.putItem(item);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done");
    }

    public static void writeToMongoDB(Purchase purchase) {

        System.out.println("Write to mongoDB");

        MongoClient client = MongoDBConnectionManager.getClient();
        MongoDatabase database = client.getDatabase("CS6650-Purchase");
        MongoCollection<Document> collection = database.getCollection("purchase");

        String storeId = purchase.getStoreID();
        String clientId = purchase.getClientID();
        String date = purchase.getDate();
        String item1Id = purchase.getItem1ID();
        String item2Id = purchase.getItem2ID();
        String item3Id = purchase.getItem3ID();
        String item4Id = purchase.getItem4ID();
        String item5Id = purchase.getItem5ID();

        Document doc = new Document("storeId", storeId)
                .append("clientId", clientId)
                .append("date", date)
                .append("item1Id", item1Id)
                .append("item2Id", item2Id)
                .append("item3Id", item3Id)
                .append("item4Id", item4Id)
                .append("item5Id", item5Id);

        collection.insertOne(doc);

        System.out.println("Done");
    }
}