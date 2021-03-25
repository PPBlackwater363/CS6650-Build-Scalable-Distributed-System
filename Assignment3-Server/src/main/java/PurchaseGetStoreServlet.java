import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "PurchaseGetStoreServlet")
public class PurchaseGetStoreServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String storeID = request.getPathInfo().substring(1);
        System.out.println(storeID);

        JSONObject res;

        if (storeID == null || storeID.isEmpty()) {
//            System.out.println("url problem");
            res = new JSONObject("{\"message\": \"Missing Parameters.\"}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(res);
        } else {
            storeID = "s" + storeID;
            String final_response = "";

            try (RPCClient clientRpc = new RPCClient()) {
                System.out.println(" [x] Requesting StoreID = " + storeID);
                final_response = clientRpc.call(storeID);
                System.out.println(" [.] Got '" + final_response + "'");
            } catch (IOException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }

//            res = new JSONObject("{" + final_response + "}");
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().println(final_response);
        }
    }
}
