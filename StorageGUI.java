package client;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class StorageGUI extends JFrame {

    private final DefaultListModel<String> fileListModel;
    private final JTextArea statusArea;

    private final JButton uploadButton;

    private final JProgressBar progressBar;

    private File[] selectedFiles;

    public StorageGUI() {

        setTitle("Storage Client");
        setSize(700, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        selectedFiles = new File[0];

        fileListModel = new DefaultListModel<>();

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setRows(6);

        JButton selectButton = new JButton("Select Files");
        uploadButton = new JButton("Upload");

        JList<String> fileList = new JList<>(fileListModel);


        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        selectButton.addActionListener(e -> selectFiles());
        uploadButton.addActionListener(e -> uploadSelectedFiles());

        JPanel topPanel = new JPanel();
        topPanel.add(selectButton);
        topPanel.add(uploadButton);

        JScrollPane filePane = new JScrollPane(fileList);

        JScrollPane statusPane = new JScrollPane(statusArea);
        statusPane.setPreferredSize(new Dimension(700, 120));

        // Layout container for center area (progress + files)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(progressBar, BorderLayout.NORTH);
        centerPanel.add(filePane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(statusPane, BorderLayout.SOUTH);
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
            appendStatus("No files selected.");
            return;
        }

        selectedFiles = files;

        fileListModel.clear();

        for (File file : selectedFiles) {
            fileListModel.addElement(file.getAbsolutePath());
        }

        progressBar.setValue(0);

        appendStatus(
                "Selected " +
                selectedFiles.length +
                " file(s)."
        );
    }

    private void uploadSelectedFiles() {

        if (selectedFiles.length == 0) {
            appendStatus("No files selected.");
            return;
        }

        uploadButton.setEnabled(false);
        progressBar.setValue(0);

        appendStatus(
                "Uploading " +
                selectedFiles.length +
                " file(s)..."
        );

        new Thread(() -> {

            try {

                int total = selectedFiles.length;

                for (int i = 0; i < total; i++) {

                    File file = selectedFiles[i];

                    appendStatus("Uploading: " + file.getName());

                    // simulate per-file progress step
                    int progress = (int) (((i) / (double) total) * 100);

                    final int finalProgress = progress;

                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(finalProgress);
                        progressBar.setString(finalProgress + "%");
                    });

                    // call your real uploader
                    StorageClient.uploadFiles(new File[]{file});
                }

                SwingUtilities.invokeLater(() -> {

                    progressBar.setValue(100);
                    progressBar.setString("100%");

                    appendStatus("Upload completed successfully.");

                    selectedFiles = new File[0];
                    fileListModel.clear();

                    uploadButton.setEnabled(true);
                });

            } catch (Exception ex) {

                ex.printStackTrace();

                SwingUtilities.invokeLater(() -> {

                    appendStatus("Upload failed: " + ex.getMessage());

                    uploadButton.setEnabled(true);
                });
            }

        }).start();
    }

    private void appendStatus(String text) {

        statusArea.append(text + "\n");

        statusArea.setCaretPosition(
                statusArea.getDocument().getLength()
        );
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            StorageGUI gui = new StorageGUI();
            gui.setVisible(true);

        });
    }
}