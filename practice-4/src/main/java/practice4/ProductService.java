package practice4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private final String url;

    public ProductService() {
        this("jdbc:sqlite:store.db");
    }

    public ProductService(String url) {
        this.url = url;
        createTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    category TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    price REAL NOT NULL
                )
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot create products table", e);
        }
    }

    public synchronized Product create(Product product) {
        validate(product);

        String sql = """
                INSERT INTO products (name, category, quantity, price)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory());
            statement.setInt(3, product.getQuantity());
            statement.setDouble(4, product.getPrice());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }

            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot create product", e);
        }
    }

    public synchronized Product getById(int id) {
        String sql = """
                SELECT id, name, category, quantity, price
                FROM products
                WHERE id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return readProduct(resultSet);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot read product", e);
        }
    }

    public synchronized Product findByName(String name) {
        String sql = """
                SELECT id, name, category, quantity, price
                FROM products
                WHERE LOWER(name) = LOWER(?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return readProduct(resultSet);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot find product by name", e);
        }
    }

    public synchronized void update(Product product) {
        validate(product);

        String sql = """
                UPDATE products
                SET name = ?, category = ?, quantity = ?, price = ?
                WHERE id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory());
            statement.setInt(3, product.getQuantity());
            statement.setDouble(4, product.getPrice());
            statement.setInt(5, product.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot update product", e);
        }
    }

    public synchronized void delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot delete product", e);
        }
    }

    public synchronized List<Product> search(ProductFilter filter) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, name, category, quantity, price
                FROM products
                WHERE 1 = 1
                """);

        List<Object> values = new ArrayList<>();

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" AND LOWER(name) LIKE LOWER(?)");
            values.add("%" + filter.getName() + "%");
        }

        if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
            sql.append(" AND LOWER(category) = LOWER(?)");
            values.add(filter.getCategory());
        }

        if (filter.getMinQuantity() != null) {
            sql.append(" AND quantity >= ?");
            values.add(filter.getMinQuantity());
        }

        if (filter.getMaxQuantity() != null) {
            sql.append(" AND quantity <= ?");
            values.add(filter.getMaxQuantity());
        }

        if (filter.getMinPrice() != null) {
            sql.append(" AND price >= ?");
            values.add(filter.getMinPrice());
        }

        if (filter.getMaxPrice() != null) {
            sql.append(" AND price <= ?");
            values.add(filter.getMaxPrice());
        }

        sql.append(" ORDER BY id LIMIT ? OFFSET ?");
        values.add(filter.getSize());
        values.add(filter.getOffset());

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setValues(statement, values);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Product> products = new ArrayList<>();

                while (resultSet.next()) {
                    products.add(readProduct(resultSet));
                }

                return products;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot search products", e);
        }
    }

    public synchronized int count() {
        String sql = "SELECT COUNT(*) FROM products";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot count products", e);
        }
    }

    public synchronized void deleteAll() {
        String sql = "DELETE FROM products";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot delete products", e);
        }
    }

    private void setValues(PreparedStatement statement, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            int index = i + 1;

            if (value instanceof Integer) {
                statement.setInt(index, (Integer) value);
            } else if (value instanceof Double) {
                statement.setDouble(index, (Double) value);
            } else {
                statement.setString(index, value.toString());
            }
        }
    }

    private Product readProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getInt("quantity"),
                resultSet.getDouble("price")
        );
    }

    private void validate(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw new IllegalArgumentException("Product category is required");
        }

        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }

        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }
    }
}