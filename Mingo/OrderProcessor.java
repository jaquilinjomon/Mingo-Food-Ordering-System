import javax.swing.*;
import java.util.*;

class OrderProcessor implements Runnable {
    private List<String> order;
    private Map<String, Double> menu;
    private MongoDBHandler dbHandler;

    public OrderProcessor(List<String> order, Map<String, Double> menu, MongoDBHandler dbHandler) {
        this.order = order;
        this.menu = menu;
        this.dbHandler = dbHandler;
    }

    @Override
    public void run() {
        double total = 0;
        try {
            // ✅ Check if order is empty
            if (order == null || order.isEmpty()) {
                throw new InvalidOrderException("No items! Please select items.");
            }

            for (String item : order) {
                if (!menu.containsKey(item)) {
                    throw new InvalidOrderException("Item not found: " + item);
                }
                total += menu.get(item);
            }

            dbHandler.saveOrder(order, total);
            System.out.println("Order processed: " + order + " Total: " + total);

        } catch (InvalidOrderException e) {
            // Show error popup in GUI instead of just console
            JOptionPane.showMessageDialog(null, e.getMessage(), "Order Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
