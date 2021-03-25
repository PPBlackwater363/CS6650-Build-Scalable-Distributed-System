import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeoutException;

public class PurchaseServlet extends javax.servlet.http.HttpServlet {
    private final static String QUEUE_NAME = "threadExQ1";
    private final static int NUM_MESSAGES_PER_THREAD =10;
    ConnectionFactory factory;
    Connection conn = null;

    public void init() {
        factory = new ConnectionFactory();
        // change the ip address if you change the rabbitMQ
        factory.setHost("100.25.180.178");
        factory.setUsername("test1");
        factory.setPassword("test1");
        try {
            conn = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        String urlPath = request.getPathInfo();

        JSONObject res;

        if (urlPath == null || urlPath.isEmpty()) {
//            System.out.println("url problem");
            res = new JSONObject("{\"message\": \"Missing Parameters.\"}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(res);
        }

        String[] urlParts = urlPath.split("/");

        if (!isPostUrlValid(urlParts)) {
            res = new JSONObject("{\"message\": \"Data not found.\"}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(res);
        } else {
            String storeId = urlParts[2];
            String clientId = urlParts[4];
            String date = urlParts[6];
//            System.out.println("aaa");
            String requestBody = getBodyFromRequest(request);
//            System.out.println(requestBody);
            String[] stringList = requestBody.split("\"");

            String item1_ID = stringList[5];
            String item2_ID = stringList[11];
            String item3_ID = stringList[17];
            String item4_ID = stringList[23];
            String item5_ID = stringList[29];

            if (storeId.length() == 0 || clientId.length() == 0 || date.length() == 0) {
                res = new JSONObject("{\"message\": \"Invalid inputs.\"}");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                Purchase purchase = new Purchase(storeId, clientId, date, item1_ID, item2_ID, item3_ID, item4_ID, item5_ID);
                Gson gson = new Gson();
                String jsonRequest = gson.toJson(purchase);
                try {
                    Channel channel = conn.createChannel();
                    channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                    // deliveryMode(2) - persistent deliveryMode(1) - non-persistent
                    channel.basicPublish("", QUEUE_NAME, new AMQP.BasicProperties.Builder().deliveryMode(2).build(), jsonRequest.getBytes("UTF-8"));
                    channel.close();
                } catch (IOException | TimeoutException ex) {
                    ex.printStackTrace();
                }
                res = new JSONObject("{\"message\": \"Data created.\"}");
                response.setStatus(HttpServletResponse.SC_CREATED);
            }
            response.getWriter().println(res);
        }

    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        String urlPath = request.getPathInfo();

        JSONObject res;

        if (urlPath == null || urlPath.isEmpty()) {
            res = new JSONObject("{\"message\": \"Missing Parameters.\"}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(res);
        }

        String[] urlParts = urlPath.split("/");

        if (isGetForPurchaseUrlValid(urlParts)) {
            String storeID = urlParts[2];
            String customerID = urlParts[4];
            String date = urlParts[6];
            if (storeID.length() == 0 || customerID.length() == 0 || date.length() == 0) {
                res = new JSONObject("{\"message\": \"Invalid inputs.\"}");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println(res);
            }
            else {
                res = new JSONObject("{\"storeID\": " + storeID + ", \"custID\": " + customerID + "}");
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().println(res);
            }
        }
        else {
            res = new JSONObject("{\"message\": \"Data not found.\"}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(res);
        }
    }

    private boolean isPostUrlValid(String[] urlPath) {
        return urlPath.length == 7 && urlPath[1].equals("purchase") && urlPath[3].equals("customer") && urlPath[5].equals("date");
    }

    private boolean isGetForPurchaseUrlValid(String[] urlPath) {
        return urlPath.length == 7 && urlPath[1].equals("purchase") && urlPath[3].equals("customer") && urlPath[5].equals("date");
    }

    public static byte[] serialize(Item obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static byte[] serializePurchaseItem(Item obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    private String getBodyFromRequest(HttpServletRequest request){
        StringBuilder jb = new StringBuilder();
        String line;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
            return jb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
