package practice4;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductServiceTest {
    private Path dbPath;
    private ProductService productService;

    @BeforeEach
    public void setUp() throws IOException {
        Files.createDirectories(Path.of("target"));

        dbPath = Files.createTempFile(Path.of("target"), "products-test-", ".db");
        productService = new ProductService("jdbc:sqlite:" + dbPath.toAbsolutePath());

        productService.deleteAll();
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(dbPath);
    }

    @Test
    public void shouldCreateAndReadProduct() {
        Product created = productService.create(
                new Product("buckwheat", "food", 10, 45.5)
        );

        Product fromDb = productService.getById(created.getId());

        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getName()).isEqualTo("buckwheat");
        assertThat(fromDb.getCategory()).isEqualTo("food");
        assertThat(fromDb.getQuantity()).isEqualTo(10);
        assertThat(fromDb.getPrice()).isEqualTo(45.5);
    }

    @Test
    public void shouldUpdateProduct() {
        Product product = productService.create(
                new Product("buckwheat", "food", 10, 45.5)
        );

        product.setName("rice");
        product.setCategory("food");
        product.setQuantity(20);
        product.setPrice(30.0);

        productService.update(product);

        Product fromDb = productService.getById(product.getId());

        assertThat(fromDb.getName()).isEqualTo("rice");
        assertThat(fromDb.getQuantity()).isEqualTo(20);
        assertThat(fromDb.getPrice()).isEqualTo(30.0);
    }

    @Test
    public void shouldDeleteProduct() {
        Product product = productService.create(
                new Product("buckwheat", "food", 10, 45.5)
        );

        productService.delete(product.getId());

        Product fromDb = productService.getById(product.getId());

        assertThat(fromDb).isNull();
        assertThat(productService.count()).isEqualTo(0);
    }

    @Test
    public void shouldFindProductByName() {
        productService.create(new Product("buckwheat", "food", 10, 45.5));

        Product product = productService.findByName("buckwheat");

        assertThat(product).isNotNull();
        assertThat(product.getName()).isEqualTo("buckwheat");
    }

    @Test
    public void shouldSearchByNameAndCategory() {
        productService.create(new Product("buckwheat", "food", 10, 45.5));
        productService.create(new Product("rice", "food", 20, 35.0));
        productService.create(new Product("hammer", "tools", 5, 100.0));

        ProductFilter filter = new ProductFilter()
                .setName("buck")
                .setCategory("food");

        List<Product> products = productService.search(filter);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("buckwheat");
    }

    @Test
    public void shouldSearchByQuantityRange() {
        productService.create(new Product("buckwheat", "food", 10, 45.5));
        productService.create(new Product("rice", "food", 20, 35.0));
        productService.create(new Product("flour", "food", 50, 25.0));

        ProductFilter filter = new ProductFilter()
                .setMinQuantity(15)
                .setMaxQuantity(40);

        List<Product> products = productService.search(filter);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("rice");
    }

    @Test
    public void shouldSearchByPriceRange() {
        productService.create(new Product("buckwheat", "food", 10, 45.5));
        productService.create(new Product("rice", "food", 20, 35.0));
        productService.create(new Product("hammer", "tools", 5, 100.0));

        ProductFilter filter = new ProductFilter()
                .setMinPrice(40.0)
                .setMaxPrice(80.0);

        List<Product> products = productService.search(filter);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("buckwheat");
    }

    @Test
    public void shouldSupportDynamicFilters() {
        productService.create(new Product("buckwheat", "food", 10, 45.5));
        productService.create(new Product("rice", "food", 20, 35.0));
        productService.create(new Product("hammer", "tools", 5, 100.0));

        ProductFilter filter = new ProductFilter()
                .setMinPrice(40.0);

        List<Product> products = productService.search(filter);

        assertThat(products)
                .extracting(Product::getName)
                .containsExactly("buckwheat", "hammer");
    }

    @Test
    public void shouldSearchWithPagination() {
        productService.create(new Product("product-1", "food", 1, 10.0));
        productService.create(new Product("product-2", "food", 2, 20.0));
        productService.create(new Product("product-3", "food", 3, 30.0));

        ProductFilter filter = new ProductFilter()
                .setPage(2)
                .setSize(1);

        List<Product> products = productService.search(filter);

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("product-2");
    }
}