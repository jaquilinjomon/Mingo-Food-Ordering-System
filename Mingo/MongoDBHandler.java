import com.mongodb.client.*;
import org.bson.Document;
import java.util.*;
import static com.mongodb.client.model.Projections.*; 

public class MongoDBHandler {
    private MongoCollection<Document> menuCollection;
    private MongoCollection<Document> ordersCollection;

    public MongoDBHandler() {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = client.getDatabase("FoodOrderingDB");
        menuCollection = db.getCollection("menu");
        ordersCollection = db.getCollection("orders");
    }

   public Map<String, Double> loadMenu() {
    Map<String, Double> menu = new HashMap<>();
    for (Document doc : menuCollection.find()) {
        String name = doc.getString("name");
        Number priceNum = doc.get("price", Number.class); // handles Int32 or Double
        double price = priceNum.doubleValue();
        menu.put(name, price);
    }
    return menu;
}


    public void saveOrder(List<String> items, double total) {
        Document order = new Document("items", items)
                .append("total", total)
                .append("timestamp", new Date());
        ordersCollection.insertOne(order);
    }

   public List<Document> getOrderHistory() {
    List<Document> history = new ArrayList<>();
    // Use fields(excludeId()) to filter out the MongoDB ID
    for (Document doc : ordersCollection.find().projection(fields(excludeId()))) {
        history.add(doc);
    }
    return history;
}
}

