import com.rabbitmq.client.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    private static String query(String id) {
        if (id.charAt(0) == 's') {
            String storeID = id.substring(1);
            System.out.println(storeID);
            return queryStore(storeID);
        } else {
            String itemID = id.substring(1);
            System.out.println(itemID);
            return queryItem(itemID);
        }
    }

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.queuePurge(RPC_QUEUE_NAME);

            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");

            Object monitor = new Object();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String response = "";

                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    String n = message;
                    response = query(n);
                    System.out.println(" [.] (" + message + ")");
//                    response += query(n);
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e.toString());
                } finally {
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> { }));
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String queryStore(String storeID) {
        System.out.println("query for store");
        String output = "";
        java.sql.Connection connection = null;
        PreparedStatement insertStmt = null;
        Statement selectStmt = null;
        String insertQueryStatement = "INSERT INTO purchaseItem (storeId, clientId, itemId, numOfItem, date) VALUES (?, ?, ?, ?, ?);";
        String query = "SELECT itemID, COUNT(*) AS NUM FROM purchaseItem WHERE storeID = '" + storeID + "' GROUP BY itemID ORDER BY NUM DESC LIMIT 10;";

        try {
            connection = ConnectionManager.getDataSource().getConnection();
            selectStmt = connection.createStatement();
            ResultSet rs = selectStmt.executeQuery(query);
            while (rs.next()) {
                String res = rs.getString("itemId");
                String num = rs.getString("NUM");
                System.out.println("itemId: " + res + "numberOfItems:" + num);
                output += "{\"itemId\": " + res + "," + "\"numberOfItems\": " + num + "},";
            }
            output = output.substring(0, output.length() - 1);
            output = "{\"stores\":[ " + output + "]}";

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return output;
    }

    public static String queryItem(String itemID) {
        System.out.println("query for item");
        String output = "";
        java.sql.Connection connection = null;
        PreparedStatement insertStmt = null;
        Statement selectStmt = null;
        String query = "SELECT storeID, COUNT(*) AS NUM FROM purchaseItem WHERE itemID = '" + itemID + "' GROUP BY storeID ORDER BY NUM DESC LIMIT 5;";

        try {
            connection = ConnectionManager.getDataSource().getConnection();
            selectStmt = connection.createStatement();
            ResultSet rs = selectStmt.executeQuery(query);
            while (rs.next()) {
                String res = rs.getString("storeId");
                String num = rs.getString("NUM");
                System.out.println("storeId: " + res + "numberOfItems:" + num);
                output += "{\"storeId\": " + res + "," + "\"numberOfItems\": " + num + "},";
            }
            output = output.substring(0, output.length() - 1);
            output = "{\"stores\":[ " + output + "]}";

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return output;
    }

}