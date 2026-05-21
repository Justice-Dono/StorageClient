package client;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class StorageGUI extends JFrame {

    private final DefaultListModel<String> fileListModel;
    private final JTextArea statusArea;

    private final JButton uploadButton;

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
        statusArea.setRows(8);

        JButton selectButton = new JButton("Select Files");
        uploadButton = new JButton("Upload");

        JList<String> fileList = new JList<>(fileListModel);

        selectButton.addActionListener(e -> selectFiles());
        uploadButton.addActionListener(e -> uploadSelectedFiles());

        JPanel topPanel = new JPanel();
        topPanel.add(selectButton);
        topPanel.add(uploadButton);

        JScrollPane filePane = new JScrollPane(fileList);

        JScrollPane statusPane = new JScrollPane(statusArea);
        statusPane.setPreferredSize(
                new Dimension(700, 150)
        );

        add(topPanel, BorderLayout.NORTH);
        add(filePane, BorderLayout.CENTER);
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
            fileListModel.addElement(
                    file.getAbsolutePath()
            );
        }

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

        appendStatus(
                "Uploading " +
                selectedFiles.length +
                " file(s)..."
        );

        new Thread(() -> {

            try {

                System.out.println(
                        "Calling uploadFiles()"
                );

                String response =
                        StorageClient.uploadFiles(
                                selectedFiles
                        );

                System.out.println(
                        "uploadFiles() returned"
                );

                System.out.println(
                        "Response: " + response
                );

                SwingUtilities.invokeLater(() -> {

                    System.out.println(
                            "Inside invokeLater"
                    );

                    appendStatus(
                            "Upload completed successfully."
                    );


                    selectedFiles = new File[0];
                    fileListModel.clear();


                    uploadButton.setEnabled(true);
                });

            } catch (Exception ex) {

                ex.printStackTrace();

                SwingUtilities.invokeLater(() -> {

                    appendStatus(
                            "Upload failed:"
                    );

                    appendStatus(
                            ex.getMessage()
                    );

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

            StorageGUI gui =
                    new StorageGUI();

            gui.setVisible(true);

        });
    }
}