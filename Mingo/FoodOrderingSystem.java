import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class FoodOrderingSystem extends JFrame {
    private Map<String, Double> menu;
    private DefaultListModel<String> orderList = new DefaultListModel<>();
    private MongoDBHandler dbHandler = new MongoDBHandler();

    public FoodOrderingSystem() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("Nimbus not available, using default.");
        }

        menu = dbHandler.loadMenu(); // Load menu from MongoDB

        setTitle("Mingo– Online Food Ordering");
        setSize(600, 500);
        setLayout(new BorderLayout());
        setIconImage(new ImageIcon("logo.png").getImage()); // optional app icon

        // Components
        JComboBox<String> menuBox = new JComboBox<>(menu.keySet().toArray(new String[0]));
        JButton addBtn = new JButton("Add Item");
        JButton placeBtn = new JButton("Place Order");
        JButton historyBtn = new JButton("Show History");
        JButton resetBtn = new JButton("Reset / Clear");
        JList<String> orderDisplay = new JList<>(orderList);
        JTextArea billArea = new JTextArea(15, 40);

        // Styling
        Font font = new Font("Arial", Font.BOLD, 14);
        addBtn.setFont(font);
        placeBtn.setFont(font);
        historyBtn.setFont(font);
        resetBtn.setFont(font);

        addBtn.setBackground(new Color(144, 238, 144));   // light green
        placeBtn.setBackground(new Color(255, 200, 100)); // orange
        historyBtn.setBackground(new Color(173, 216, 230)); // light blue
        resetBtn.setBackground(new Color(255, 99, 71));   // tomato red

        orderDisplay.setBorder(BorderFactory.createTitledBorder("Current Order"));
        billArea.setBorder(BorderFactory.createTitledBorder("Bill / History"));
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        billArea.setEditable(false);
        billArea.setBackground(new Color(245, 245, 245));

        // Layout panels
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(menuBox);
        topPanel.add(addBtn);
        topPanel.add(placeBtn);
        topPanel.add(historyBtn);
        topPanel.add(resetBtn);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(orderDisplay), BorderLayout.CENTER);
        add(new JScrollPane(billArea), BorderLayout.SOUTH);

        // Actions
        addBtn.addActionListener(e -> {
            orderList.addElement((String) menuBox.getSelectedItem());
        });

        placeBtn.addActionListener(e -> {
            java.util.List<String> order = Collections.list(orderList.elements());
            Thread t = new Thread(new OrderProcessor(order, menu, dbHandler));
            t.start();
            double total = order.stream().mapToDouble(item -> menu.get(item)).sum();
            billArea.setText("Order placed:\n" + order + "\nTotal: $" + String.format("%.2f", total));
        });

        historyBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("Order History:\n");
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

            for (var doc : dbHandler.getOrderHistory()) {
                Date date = doc.getDate("timestamp"); // ✅ consistent field name
                String formattedDate = (date != null) ? sdf.format(date) : "N/A";

                sb.append("Items: ").append(doc.get("items"))
                  .append(" | Total: $").append(String.format("%.2f", doc.getDouble("total")))
                  .append(" | Date: ").append(formattedDate)
                  .append("\n");
            }
            billArea.setText(sb.toString());
        });

        resetBtn.addActionListener(e -> {
            orderList.clear();
            billArea.setText("");
            menuBox.setSelectedIndex(-1);
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new FoodOrderingSystem();
    }
}
