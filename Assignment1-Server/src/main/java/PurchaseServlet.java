import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Random;

public class PurchaseServlet extends javax.servlet.http.HttpServlet {

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

        System.out.println(request.getPathInfo());

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        String urlPath = request.getPathInfo();

        JSONObject res;

        if (urlPath == null || urlPath.isEmpty()) {
            System.out.println("url problem");
            res = new JSONObject("{\"message\": \"Missing Parameters.\"}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(res);
        }

        String[] urlParts = urlPath.split("/");

        if (!isPostUrlValid(urlParts)) {
            System.out.println("not valid url");
            res = new JSONObject("{\"message\": \"Data not found.\"}");
            System.out.println("Read Failed");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(res);
        } else {
            StringBuilder jb = new StringBuilder();
            String line = null;
            try {
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null)
                    jb.append(line);
            } catch (Exception e) {
                throw new IOException("Error reading json");
            }
            try {
                JSONObject jsonObject = new JSONObject(jb.toString());
                System.out.println(jb.toString());
//                System.out.println(jsonObject.get("items"));
//                JSONObject itemsJsonObject = new JSONObject(jsonObject.get("items"));

                if (!jsonObject.has("items") || jsonObject.isNull("items")) {
                    System.out.println("no items");
                    System.out.println("Write Failed");
                    res = new JSONObject("{\"message\": \"Invalid inputs.\"}");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println(res);
                }
                else {
                    System.out.println("Success");
                    res = new JSONObject("{\"message\": \"Write successful.\"}");
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.getWriter().println(res);
                }
            } catch (JSONException e) {
                throw new IOException("Error parsing JSON request string");
            }
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
//        System.out.println(urlPath.length);
//        System.out.println(urlPath[1]);
        return urlPath.length == 7 && urlPath[1].equals("purchase") && urlPath[3].equals("customer") && urlPath[5].equals("date");
//        return true;
    }

    private boolean isGetForPurchaseUrlValid(String[] urlPath) {
        return urlPath.length == 7 && urlPath[1].equals("purchase") && urlPath[3].equals("customer") && urlPath[5].equals("date");
//        return true;
    }
}
