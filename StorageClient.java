package client;
import java.io.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.UUID;

public class StorageClient {

    private static final String WORKER_URL = "https://clean-upload-api.jordan-tewnion.workers.dev/";

    public static void main(String[] args) {

        File local_file = new File("test.webp");
        System.out.println(local_file.exists());
        if (!local_file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        try {
            uploadFile(local_file);
            System.out.println("File made it to upload");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uploadFile(File file) throws Exception {

        String boundary = "Boundary-" + UUID.randomUUID();

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));

        // Multipart form header
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
              .append(file.getName())
              .append("\"\r\n");
        writer.append("Content-Type: application/octet-stream\r\n\r\n");
        writer.flush();

        // File bytes
        outputStream.write(fileBytes);
        outputStream.flush();

        // Closing boundary
        writer.append("\r\n--")
              .append(boundary)
              .append("--\r\n");
        writer.flush();

        byte[] requestBody = outputStream.toByteArray();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(WORKER_URL))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }
}