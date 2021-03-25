import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "PurchaseGetItemServlet")
public class PurchaseGetItemServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String itemID = request.getPathInfo().substring(1);
        System.out.println(itemID);
        JSONObject res;

        if (itemID == null || itemID.isEmpty()) {
            res = new JSONObject("{\"message\": \"Missing Parameters.\"}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(res);
        } else {
            itemID = "i" + itemID;
            String final_response = "";

            try (RPCClient clientRpc = new RPCClient()) {
                System.out.println(" [x] Requesting itemID = " + itemID);
                final_response = clientRpc.call(itemID);
                System.out.println(" [.] Got '" + final_response + "'");
            } catch (IOException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }
//            res = new JSONObject("{\"message\": \"Data created.\"}");
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().println(final_response);
        }


    }
}
