import org.apache.commons.dbcp2.*;

public class ConnectionManager {
    private static BasicDataSource dataSource;

    // NEVER store sensitive information below in plain text!
    private static final String HOST_NAME = "cs6650-database.civh85nirkt4.us-east-1.rds.amazonaws.com";
    //    private static final String PORT = System.getProperty("MySQL_PORT");
    private static final int PORT = 3306;
    private static final String DATABASE = "cs6650";
    private static final String USERNAME = "cs6650";
    private static final String PASSWORD = "AYdy0011";

    static {
        // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
        dataSource = new BasicDataSource();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
        dataSource.setUrl(url);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setInitialSize(10);
        dataSource.setMaxTotal(60);
    }

    public static BasicDataSource getDataSource() {
        return dataSource;
    }
}