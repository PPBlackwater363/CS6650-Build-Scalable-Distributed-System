
import java.sql.*;
import org.apache.commons.dbcp2.*;

public class PurchaseDAO {
    private static BasicDataSource dataSource;

    public PurchaseDAO() {
        dataSource = ConnectionManager.getDataSource();
    }

    public void createPurchase(Purchase newPurchase) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;

        String insertQueryStatement = "INSERT INTO purchase (storeId, clientId, date) " +
                "VALUES (?,?,?)";

        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setString(1, newPurchase.getStoreID());
            preparedStatement.setString(2, newPurchase.getClientID());
            preparedStatement.setString(3, newPurchase.getDate());

            preparedStatement.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
