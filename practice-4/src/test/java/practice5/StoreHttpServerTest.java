package practice5;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice4.ProductService;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class StoreHttpServerTest {
    private Path dbPath;
    private StoreHttpServer server;

    @BeforeEach
    public void setUp() throws IOException {
        Files.createDirectories(Path.of("target"));

        dbPath = Files.createTempFile(Path.of("target"), "http-test-", ".db");

        ProductService productService = new ProductService(
                "jdbc:sqlite:" + dbPath.toAbsolutePath()
        );

        int port = findFreePort();

        server = new StoreHttpServer(port, productService);
        server.start();

        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (server != null) {
            server.stop();
        }

        Files.deleteIfExists(dbPath);

        RestAssured.reset();
    }

    @Test
    public void shouldLoginAndReturnToken() {
        String token = login();

        assertThat(token).isNotBlank();
    }

    @Test
    public void shouldRejectLoginWithWrongPassword() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "login": "admin",
                          "password": "wrong"
                        }
                        """)
                .when()
                .post("/login")
                .then()
                .statusCode(401);
    }

    @Test
    public void shouldRejectProductRequestWithoutToken() {
        given()
                .when()
                .get("/products/1")
                .then()
                .statusCode(401);
    }

    @Test
    public void shouldCreateAndReadProduct() {
        String token = login();

        Response createResponse = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {
                          "name": "buckwheat",
                          "category": "food",
                          "quantity": 10,
                          "price": 45.5
                        }
                        """)
                .when()
                .put("/products")
                .then()
                .statusCode(201)
                .extract()
                .response();

        int id = createResponse.path("id");

        Response getResponse = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/products/" + id)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(getResponse.<String>path("name")).isEqualTo("buckwheat");
        assertThat(getResponse.<String>path("category")).isEqualTo("food");
        assertThat(getResponse.<Integer>path("quantity")).isEqualTo(10);
        assertThat(getResponse.<Float>path("price")).isEqualTo(45.5f);
    }

    @Test
    public void shouldNotCreateProductWithSameName() {
        String token = login();

        String body = """
                {
                  "name": "buckwheat",
                  "category": "food",
                  "quantity": 10,
                  "price": 45.5
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(body)
                .when()
                .put("/products")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(body)
                .when()
                .put("/products")
                .then()
                .statusCode(409);
    }

    @Test
    public void shouldReturn404WhenProductNotFound() {
        String token = login();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/products/999")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldUpdateProduct() {
        String token = login();

        int id = createProduct(token);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {
                          "name": "rice",
                          "category": "food",
                          "quantity": 20,
                          "price": 35.0
                        }
                        """)
                .when()
                .post("/products/" + id)
                .then()
                .statusCode(200);

        Response response = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/products/" + id)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.<String>path("name")).isEqualTo("rice");
        assertThat(response.<String>path("category")).isEqualTo("food");
        assertThat(response.<Integer>path("quantity")).isEqualTo(20);
        assertThat(response.<Float>path("price")).isEqualTo(35.0f);
    }

    @Test
    public void shouldReturn404WhenUpdatingUnknownProduct() {
        String token = login();

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {
                          "name": "rice",
                          "category": "food",
                          "quantity": 20,
                          "price": 35.0
                        }
                        """)
                .when()
                .post("/products/999")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldDeleteProduct() {
        String token = login();

        int id = createProduct(token);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/products/" + id)
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/products/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldReturn404WhenDeletingUnknownProduct() {
        String token = login();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/products/999")
                .then()
                .statusCode(404);
    }

    private String login() {
        return given()
                .contentType("application/json")
                .body("""
                        {
                          "login": "admin",
                          "password": "password"
                        }
                        """)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    private int createProduct(String token) {
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("""
                        {
                          "name": "buckwheat",
                          "category": "food",
                          "quantity": 10,
                          "price": 45.5
                        }
                        """)
                .when()
                .put("/products")
                .then()
                .statusCode(201)
                .extract()
                .response();

        return response.path("id");
    }

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}