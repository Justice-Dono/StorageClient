package client;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.UUID;
import java.awt.FileDialog;
import java.awt.Frame;

public class StorageClient {

    private static final String WORKER_URL = "https://clean-upload-api.jordan-tewnion.workers.dev/";

    public static void main(String[] args) {
        FileDialog dialog = new FileDialog((Frame)null, "Select File");
        dialog.setMode(FileDialog.LOAD);
        dialog.setMultipleMode(true);
        dialog.setVisible(true);
        File[] local_file = dialog.getFiles();
        if(local_file.length == 0){
            System.out.println("No files selected");
            System.exit(0);
        }
        try {
            uploadFiles(local_file);
            System.out.println("File made it to upload");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);

        }
    }

    public static void uploadFiles(File[] files) throws Exception {

        String boundary = "Boundary-" + UUID.randomUUID();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));

        for (File file : files) {

            if (!file.exists()) {
                System.out.println("Skipping missing file: " + file.getName());
                continue;
            }

            byte[] fileBytes = Files.readAllBytes(file.toPath());

            
         writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"files\"; filename=\"")
                  .append(file.getName())
                  .append("\"\r\n");
            writer.append("Content-Type: application/octet-stream\r\n\r\n");
            writer.flush();

            outputStream.write(fileBytes);
            outputStream.flush();

            writer.append("\r\n");
            writer.flush();

            System.out.println("Prepared: " + file.getName());
        }

    // Closing boundary
        writer.append("--")
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

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response: " + response.body());

        System.out.println("Upload finished!");
    }
}