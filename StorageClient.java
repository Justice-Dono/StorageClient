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
        dialog.setVisible(true);
        String directory = dialog.getDirectory();
        String filename = dialog.getFile();
        File local_file = null;
        if (filename != null) {
            local_file = new File(directory, filename);
            System.out.println(local_file.getAbsolutePath());
        }
        if(!local_file.exists()){
            System.out.println("Chosen file does not exist!");
            System.exit(0);
        }
        try {
            uploadFile(local_file);
            System.out.println("File made it to upload");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);

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
        System.out.println("Upload Finished!");
        return;
    }
}