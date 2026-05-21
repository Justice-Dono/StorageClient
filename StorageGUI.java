package client;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class StorageGUI extends JFrame {

    private final DefaultListModel<String> fileListModel;
    private final JTextArea statusArea;

    private File[] selectedFiles;

    public StorageGUI() {

        setTitle("Storage Client");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        selectedFiles = new File[0];

        fileListModel = new DefaultListModel<>();
        statusArea = new JTextArea();

        statusArea.setEditable(false);

        JButton selectButton = new JButton("Select Files");
        JButton uploadButton = new JButton("Upload");

        JList<String> fileList = new JList<>(fileListModel);

        selectButton.addActionListener(e -> selectFiles());

        uploadButton.addActionListener(e -> uploadSelectedFiles());

        JPanel topPanel = new JPanel();
        topPanel.add(selectButton);
        topPanel.add(uploadButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(fileList), BorderLayout.CENTER);
        add(new JScrollPane(statusArea), BorderLayout.SOUTH);
    }

    private void selectFiles() {

        FileDialog dialog = new FileDialog(
                this,
                "Select Files",
                FileDialog.LOAD
        );

        dialog.setMultipleMode(true);
        dialog.setVisible(true);

        File[] files = dialog.getFiles();

        if (files == null || files.length == 0) {
            statusArea.append("No files selected.\n");
            return;
        }

        selectedFiles = files;

        fileListModel.clear();

        for (File file : selectedFiles) {
            fileListModel.addElement(file.getAbsolutePath());
        }

        statusArea.append(
                "Selected " +
                selectedFiles.length +
                " file(s).\n"
        );
    }

    private void uploadSelectedFiles() {

        if (selectedFiles.length == 0) {
            statusArea.append("No files selected.\n");
            return;
        }

        statusArea.append(
                "Uploading " +
                selectedFiles.length +
                " file(s)...\n"
        );

        // Run upload in background so UI doesn't freeze
        new Thread(() -> {

            try {

                StorageClient.uploadFiles(selectedFiles);

                SwingUtilities.invokeLater(() ->
                        statusArea.append(
                                "Upload completed successfully.\n"
                        )
                );

            } catch (Exception ex) {

                SwingUtilities.invokeLater(() ->
                        statusArea.append(
                                "Upload failed: " +
                                ex.getMessage() +
                                "\n"
                        )
                );

                ex.printStackTrace();
            }

        }).start();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            StorageGUI gui = new StorageGUI();
            gui.setVisible(true);
        });
    }
}