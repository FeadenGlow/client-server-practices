package practice5;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;

import practice4.Product;
import practice4.ProductService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class StoreHttpServer {
    private static final int DEFAULT_PORT = 5002;

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "password";

    private static final String SECRET = "simple-secret-key";

    private final int port;
    private final ProductService productService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET);

    private HttpServer server;

    public StoreHttpServer() {
        this(DEFAULT_PORT, new ProductService());
    }

    public StoreHttpServer(int port, ProductService productService) {
        this.port = port;
        this.productService = productService;
    }

    public static void main(String[] args) {
        StoreHttpServer server = new StoreHttpServer();
        server.start();
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/login", this::handleLogin);

            HttpContext productsContext = server.createContext("/products", this::handleProducts);
            productsContext.setAuthenticator(new JwtAuthenticator());

            server.start();

            System.out.println("HTTP server started on port " + port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot start HTTP server", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }

        try {
            UserCredentials credentials = mapper.readValue(
                    exchange.getRequestBody(),
                    UserCredentials.class
            );

            if (!ADMIN_LOGIN.equals(credentials.getLogin())
                    || !ADMIN_PASSWORD.equals(credentials.getPassword())) {
                sendJson(exchange, 401, Map.of("error", "Wrong login or password"));
                return;
            }

            String token = createToken(credentials.getLogin());

            sendJson(exchange, 200, new AuthResponse(token));
        } catch (Exception e) {
            sendJson(exchange, 400, Map.of("error", e.getMessage()));
        }
    }

    private void handleProducts(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/products")) {
                if (method.equals("PUT")) {
                    createProduct(exchange);
                    return;
                }

                sendJson(exchange, 405, Map.of("error", "Method not allowed"));
                return;
            }

            if (path.startsWith("/products/")) {
                Integer id = getProductId(path);

                if (id == null) {
                    sendJson(exchange, 400, Map.of("error", "Wrong product id"));
                    return;
                }

                if (method.equals("GET")) {
                    getProduct(exchange, id);
                    return;
                }

                if (method.equals("POST")) {
                    updateProduct(exchange, id);
                    return;
                }

                if (method.equals("DELETE")) {
                    deleteProduct(exchange, id);
                    return;
                }

                sendJson(exchange, 405, Map.of("error", "Method not allowed"));
                return;
            }

            sendJson(exchange, 404, Map.of("error", "Not found"));
        } catch (Exception e) {
            sendJson(exchange, 400, Map.of("error", e.getMessage()));
        }
    }

    private void createProduct(HttpExchange exchange) throws IOException {
        ProductRequest request = mapper.readValue(
                exchange.getRequestBody(),
                ProductRequest.class
        );

        Product existingProduct = productService.findByName(request.getName());

        if (existingProduct != null) {
            sendJson(exchange, 409, Map.of("error", "Product with this name already exists"));
            return;
        }

        Product createdProduct = productService.create(request.toProduct());

        sendJson(exchange, 201, createdProduct);
    }

    private void getProduct(HttpExchange exchange, int id) throws IOException {
        Product product = productService.getById(id);

        if (product == null) {
            sendJson(exchange, 404, Map.of("error", "Product not found"));
            return;
        }

        sendJson(exchange, 200, product);
    }

    private void updateProduct(HttpExchange exchange, int id) throws IOException {
        Product product = productService.getById(id);

        if (product == null) {
            sendJson(exchange, 404, Map.of("error", "Product not found"));
            return;
        }

        ProductRequest request = mapper.readValue(
                exchange.getRequestBody(),
                ProductRequest.class
        );

        Product productWithSameName = productService.findByName(request.getName());

        if (productWithSameName != null && productWithSameName.getId() != id) {
            sendJson(exchange, 409, Map.of("error", "Product with this name already exists"));
            return;
        }

        request.applyTo(product);
        productService.update(product);

        sendJson(exchange, 200, product);
    }

    private void deleteProduct(HttpExchange exchange, int id) throws IOException {
        Product product = productService.getById(id);

        if (product == null) {
            sendJson(exchange, 404, Map.of("error", "Product not found"));
            return;
        }

        productService.delete(id);

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private Integer getProductId(String path) {
        try {
            String idText = path.substring("/products/".length());

            return Integer.parseInt(idText);
        } catch (Exception e) {
            return null;
        }
    }

    private String createToken(String login) {
        return JWT.create()
                .withSubject(login)
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(algorithm);
    }

    private boolean isTokenValid(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();

            DecodedJWT decodedJWT = verifier.verify(token);

            return ADMIN_LOGIN.equals(decodedJWT.getSubject());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private class JwtAuthenticator extends Authenticator {
        @Override
        public Result authenticate(HttpExchange exchange) {
            List<String> values = exchange.getRequestHeaders().get("Authorization");

            if (values == null || values.isEmpty()) {
                return new Failure(401);
            }

            String authorization = values.get(0);
            String[] parts = authorization.split(" ");

            if (parts.length != 2 || !parts[0].equals("Bearer")) {
                return new Failure(401);
            }

            String token = parts[1];

            if (!isTokenValid(token)) {
                return new Failure(401);
            }

            return new Success(new HttpPrincipal(ADMIN_LOGIN, "user"));
        }
    }
}