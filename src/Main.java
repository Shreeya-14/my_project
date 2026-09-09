import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

public class Main {
    private static SensorManager manager;

    public static void main(String[] args) {
        try {
            manager = new SensorManager(
                CSVLoader.loadSensors("data/farm_sensors.csv")
            );

            HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0
            );

            server.createContext("/api/health", new HealthHandler());
            server.createContext("/api/summary", new SummaryHandler());
            server.createContext("/api/sensors", new SensorsHandler());
            server.createContext("/api/range", new RangeHandler());
            server.createContext("/api/nearest", new NearestHandler());
            server.createContext("/api/zones", new ZonesHandler());
            server.createContext("/api/fertilizers", new FertilizerHandler());
            server.createContext("/api/fractional", new FractionalHandler());
            server.createContext("/api/add", new AddHandler());
            server.createContext("/api/remove", new RemoveHandler());

            // Serves index.html, field.html, irrigation.html, etc.
            server.createContext("/", new StaticFileHandler());

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();

            System.out.println("AgriSpatial is running at:");
            System.out.println("http://localhost:8080");

        } catch (Exception error) {
            System.out.println("Project could not start:");
            error.printStackTrace();
        }
    }

    static class HealthHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            sendJson(ex, "{\"status\":\"online\"}");
        }
    }

    static class SummaryHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            synchronized (manager) {
                ArrayList<Sensor> sensors = manager.getAllSensors();

                int dry = 0;
                double totalMoisture = 0;
                double totalPH = 0;

                for (Sensor sensor : sensors) {
                    totalMoisture += sensor.moisture;
                    totalPH += sensor.ph;

                    if (sensor.moisture < 35) {
                        dry++;
                    }
                }

                double averageMoisture =
                    sensors.isEmpty() ? 0 : totalMoisture / sensors.size();

                double averagePH =
                    sensors.isEmpty() ? 0 : totalPH / sensors.size();

                String status = averageMoisture < 35
                    ? "Irrigate now"
                    : averageMoisture < 60
                        ? "Monitor"
                        : "Healthy";

                sendJson(ex, String.format(Locale.US,
                    "{\"sensorCount\":%d,\"drySensors\":%d," +
                    "\"averageMoisture\":%.1f,\"averagePH\":%.2f," +
                    "\"farmStatus\":\"%s\"}",
                    sensors.size(), dry, averageMoisture,
                    averagePH, status
                ));
            }
        }
    }

    static class SensorsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            synchronized (manager) {
                sendJson(ex,
                    "{\"sensors\":" +
                    sensorsToJson(manager.getAllSensors()) +
                    "}"
                );
            }
        }
    }

    static class RangeHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            try {
                Map<String, String> p = parameters(ex);

                int xmin = number(p, "xmin");
                int xmax = number(p, "xmax");
                int ymin = number(p, "ymin");
                int ymax = number(p, "ymax");

                if (!coordinate(xmin) || !coordinate(xmax) ||
                    !coordinate(ymin) || !coordinate(ymax) ||
                    xmin > xmax || ymin > ymax) {
                    sendError(ex, 400, "Invalid range values.");
                    return;
                }

                synchronized (manager) {
                    RangeIndex.RangeResult result =
                        manager.rangeSearch(xmin, xmax, ymin, ymax);

                    String recommendation =
                        IrrigationService.getRecommendation(result.sensors);

                    sendJson(ex,
                        "{\"hits\":" + sensorsToJson(result.sensors) +
                        ",\"count\":" + result.sensors.size() +
                        ",\"candidates\":" + result.candidatesChecked +
                        ",\"recommendation\":\"" +
                        escape(recommendation) +
                        "\"}"
                    );
                }
            } catch (Exception error) {
                sendError(ex, 400, "Range query failed.");
            }
        }
    }

    static class NearestHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            try {
                Map<String, String> p = parameters(ex);
                int x = number(p, "x");
                int y = number(p, "y");

                if (!coordinate(x) || !coordinate(y)) {
                    sendError(ex, 400, "Coordinates must be 0 to 100.");
                    return;
                }

                synchronized (manager) {
                    KDTree.NearestResult result =
                        manager.nearestSensor(x, y);

                    sendJson(ex, String.format(Locale.US,
                        "{\"sensor\":%s,\"distance\":%.2f," +
                        "\"nodesVisited\":%d}",
                        result.sensor.toJson(),
                        result.distance,
                        result.nodesVisited
                    ));
                }
            } catch (Exception error) {
                sendError(ex, 400, "Nearest query failed.");
            }
        }
    }

    static class ZonesHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            synchronized (manager) {
                ArrayList<IrrigationService.IrrigationZone> zones =
                    manager.getZones();

                StringBuilder json = new StringBuilder("{\"zones\":[");
                for (int i = 0; i < zones.size(); i++) {
                    if (i > 0) {
                        json.append(",");
                    }
                    json.append(zones.get(i).toJson());
                }
                json.append("]}");

                sendJson(ex, json.toString());
            }
        }
    }

    static class FertilizerHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            synchronized (manager) {
                sendJson(ex, manager.getSoilReport().toJson());
            }
        }
    }

    static class FractionalHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            try {
                int moisture = number(parameters(ex), "moisture");

                synchronized (manager) {
                    FractionalCascade.CascadeResult r =
                        manager.fractionalSearch(moisture);

                    sendJson(ex,
                        "{\"mainPosition\":" + r.mainPosition +
                        ",\"northPosition\":" + r.northPosition +
                        ",\"southPosition\":" + r.southPosition +
                        ",\"mainSensor\":" + r.mainSensor.toJson() +
                        ",\"northSensor\":" + r.northSensor.toJson() +
                        ",\"southSensor\":" + r.southSensor.toJson() +
                        "}"
                    );
                }
            } catch (Exception error) {
                sendError(ex, 400, "Fractional cascading query failed.");
            }
        }
    }

    static class AddHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(ex, 405, "Use POST for this endpoint.");
                return;
            }

            synchronized (manager) {
                Sensor sensor = manager.addRandomSensor();

                sendJson(ex,
                    "{\"success\":true,\"sensor\":" +
                    sensor.toJson() +
                    "}"
                );
            }
        }
    }

    static class RemoveHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("DELETE")) {
                sendError(ex, 405, "Use DELETE for this endpoint.");
                return;
            }

            String id = parameters(ex).get("id");

            synchronized (manager) {
                boolean removed = manager.removeSensor(id);

                sendJson(ex,
                    "{\"success\":" + removed +
                    ",\"removed\":" + removed +
                    "}"
                );
            }
        }
    }

    static class StaticFileHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String requestPath = ex.getRequestURI().getPath();

            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            Path webRoot =
                Paths.get("web").toAbsolutePath().normalize();

            Path file =
                webRoot.resolve(requestPath.substring(1)).normalize();

            if (!file.startsWith(webRoot) ||
                !Files.exists(file) ||
                Files.isDirectory(file)) {
                sendError(ex, 404, "File not found.");
                return;
            }

            byte[] content = Files.readAllBytes(file);

            ex.getResponseHeaders().set(
                "Content-Type",
                contentType(requestPath)
            );

            ex.sendResponseHeaders(200, content.length);

            OutputStream output = ex.getResponseBody();
            output.write(content);
            output.close();
        }
    }

    private static Map<String, String> parameters(HttpExchange ex) {
        Map<String, String> output = new HashMap<String, String>();
        String query = ex.getRequestURI().getRawQuery();

        if (query == null) {
            return output;
        }

        for (String pair : query.split("&")) {
            String[] item = pair.split("=", 2);

            if (item.length == 2) {
                try {
                    output.put(
                        URLDecoder.decode(item[0], "UTF-8"),
                        URLDecoder.decode(item[1], "UTF-8")
                    );
                } catch (Exception ignored) {
                }
            }
        }

        return output;
    }

    private static int number(Map<String, String> p, String key) {
        return Integer.parseInt(p.get(key));
    }

    private static boolean coordinate(int value) {
        return value >= 0 && value <= 100;
    }

    private static String sensorsToJson(ArrayList<Sensor> sensors) {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < sensors.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(sensors.get(i).toJson());
        }

        json.append("]");
        return json.toString();
    }

    private static String escape(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n");
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (path.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (path.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        return "application/octet-stream";
    }

    private static void sendJson(HttpExchange ex, String json)
        throws IOException {

        byte[] response = json.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        ex.sendResponseHeaders(200, response.length);

        OutputStream output = ex.getResponseBody();
        output.write(response);
        output.close();
    }

    private static void sendError(
        HttpExchange ex, int status, String message
    ) throws IOException {

        String json =
            "{\"success\":false,\"error\":\"" +
            escape(message) +
            "\"}";

        byte[] response = json.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        ex.sendResponseHeaders(status, response.length);

        OutputStream output = ex.getResponseBody();
        output.write(response);
        output.close();
    }
}