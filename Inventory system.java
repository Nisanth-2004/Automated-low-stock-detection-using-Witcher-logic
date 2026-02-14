
import java.sql.*;

public class InventorySystem {
    // DB Connection Details
    private static final String URL = "jdbc:mysql://localhost:3306/InventoryDB";
    private static final String USER = "root"; 
    private static final String PASS = "password"; // Change to your DB password

    public static void main(String[] args) {
        // Example: Simulate selling 11 Wireless Mouses
        // This will drop stock from 15 to 4, triggering the watcher.
        simulateSale(1, 11); 
    }

    /**
     * Simulates a product sale and updates the database.
     */
    public static void simulateSale(int productId, int quantitySold) {
        String updateSQL = "UPDATE products SET stock_count = stock_count - ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setInt(1, quantitySold);
            pstmt.setInt(2, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Transaction Successful: " + quantitySold + " units sold.");
                // TRIGGER THE WATCHER
                runLowStockWatcher(conn, productId);
            }

        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    /**
     * THE WATCHER: Logic that automates business decisions based on data.
     */
    public static void runLowStockWatcher(Connection conn, int productId) throws SQLException {
    String query = "SELECT product_name, stock_count, price FROM products WHERE id = ?";
    
    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, productId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String name = rs.getString("product_name");
            int currentStock = rs.getInt("stock_count");
            double price = rs.getDouble("price");

            if (currentStock < 5) {
                System.out.println("\n--- LOW STOCK ALERT ---");
                System.out.println("Product: " + name);
                System.out.println("Current Stock: " + currentStock);
                System.out.println("Price: $" + price);
                System.out.println("ACTION REQUIRED: Please reorder immediately.");
                System.out.println("------------------------\n");
            }
        }
    }
}

}
