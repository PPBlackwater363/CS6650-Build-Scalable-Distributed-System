import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.apache.commons.dbcp2.BasicDataSource;
import org.bson.Document;
import java.util.Arrays;
import com.mongodb.Block;

import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;

public class MongoDBConnectionManager {

    private static MongoClient client;

    private static final String CONNECT_STRING = "mongodb://localhost:27017";

    static {
        MongoClientURI connectionString = new MongoClientURI(CONNECT_STRING);
        client = new MongoClient(connectionString);
    }

    public static MongoClient getClient() {
        return client;
    }
}
