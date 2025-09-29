package Server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UDPServer extends JFrame {
    private JTextField txtPort;
    private JButton btnConnect, btnDisconnect, btnChooseDir;
    private JTable tblHistory;
    private DefaultTableModel modelHistory;
    private JLabel lblStatus, lblSaveDir;
    private DatagramSocket socket;
    private File saveDir;
    private boolean running = false;

    public UDPServer() {
        setTitle("📥 UDP File Server");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ========== TOP PANEL ==========
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(30, 144, 255));

        JLabel lblPort = new JLabel("Cổng:");
        lblPort.setForeground(Color.WHITE);
        lblPort.setFont(new Font("Arial", Font.BOLD, 14));

        txtPort = new JTextField("8888", 8);
        txtPort.setFont(new Font("Arial", Font.PLAIN, 14));

        btnConnect = new JButton("Kết nối");
        btnConnect.setBackground(new Color(46, 204, 113));
        btnConnect.setForeground(Color.WHITE);

        btnDisconnect = new JButton("Ngắt kết nối");
        btnDisconnect.setBackground(new Color(231, 76, 60));
        btnDisconnect.setForeground(Color.WHITE);
        btnDisconnect.setEnabled(false);

        btnChooseDir = new JButton("Chọn thư mục lưu");
        btnChooseDir.setBackground(new Color(241, 196, 15));
        btnChooseDir.setForeground(Color.BLACK);

        topPanel.add(lblPort);
        topPanel.add(txtPort);
        topPanel.add(btnConnect);
        topPanel.add(btnDisconnect);
        topPanel.add(btnChooseDir);

        add(topPanel, BorderLayout.NORTH);

        // ========== TABLE LỊCH SỬ ==========
        String[] cols = {"Tên file", "Kích thước (bytes)", "Người gửi", "Thời gian", "Vị trí lưu"};
        modelHistory = new DefaultTableModel(cols, 0);
        tblHistory = new JTable(modelHistory);
        tblHistory.setRowHeight(25);
        tblHistory.getTableHeader().setBackground(new Color(52, 73, 94));
        tblHistory.getTableHeader().setForeground(Color.WHITE);
        tblHistory.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(tblHistory);
        add(scroll, BorderLayout.CENTER);

        // ========== STATUS PANEL ==========
        JPanel statusPanel = new JPanel(new BorderLayout());
        lblStatus = new JLabel(" ⚪ Chưa kết nối", SwingConstants.LEFT);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatus.setForeground(Color.RED);

        lblSaveDir = new JLabel(" 📂 Thư mục lưu: Chưa chọn", SwingConstants.LEFT);
        lblSaveDir.setFont(new Font("Arial", Font.ITALIC, 13));

        statusPanel.add(lblStatus, BorderLayout.NORTH);
        statusPanel.add(lblSaveDir, BorderLayout.SOUTH);
        add(statusPanel, BorderLayout.SOUTH);

        // ========== ACTIONS ==========
        btnConnect.addActionListener(e -> startServer());
        btnDisconnect.addActionListener(e -> stopServer());
        btnChooseDir.addActionListener(e -> chooseDirectory());
    }

    private void chooseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            saveDir = chooser.getSelectedFile();
            lblSaveDir.setText(" 📂 Thư mục lưu: " + saveDir.getAbsolutePath());
        }
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(txtPort.getText().trim());
            socket = new DatagramSocket(port);
            running = true;

            btnConnect.setEnabled(false);
            btnDisconnect.setEnabled(true);
            lblStatus.setText(" 🟢 Đang lắng nghe trên cổng " + port);
            lblStatus.setForeground(new Color(0, 200, 0));

            Thread t = new Thread(this::receiveFiles);
            t.start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi khởi động server: " + ex.getMessage());
        }
    }

    private void stopServer() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        lblStatus.setText(" 🔴 Đã ngắt kết nối");
        lblStatus.setForeground(Color.RED);
    }

    private void receiveFiles() {
        byte[] buffer = new byte[65535];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());

                if (received.startsWith("META::")) {
                    // Tách thông tin file
                    String[] metaParts = received.split("::");
                    String fileName = metaParts[1];
                    long fileSize = Long.parseLong(metaParts[2]);

                    if (saveDir == null) {
                        JOptionPane.showMessageDialog(this, "⚠️ Bạn chưa chọn thư mục lưu file!");
                        continue;
                    }

                    File savedFile = new File(saveDir, fileName);
                    FileOutputStream fos = new FileOutputStream(savedFile);

                    long totalReceived = 0;

                    // Bắt đầu nhận dữ liệu file
                    while (running && totalReceived < fileSize) {
                        byte[] dataBuffer = new byte[4096];
                        DatagramPacket dataPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
                        socket.receive(dataPacket);

                        String msg = new String(dataPacket.getData(), 0, dataPacket.getLength());
                        if (msg.equals("END")) {
                            break;
                        }

                        fos.write(dataPacket.getData(), 0, dataPacket.getLength());
                        totalReceived += dataPacket.getLength();
                    }

                    fos.close();

                    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

                    SwingUtilities.invokeLater(() -> {
                        modelHistory.addRow(new Object[]{
                                fileName,
                                fileSize,
                                packet.getAddress().getHostAddress(),
                                time,
                                savedFile.getAbsolutePath()
                        });
                    });
                }

            } catch (Exception ex) {
                if (running) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDPServer().setVisible(true));
    }
}
