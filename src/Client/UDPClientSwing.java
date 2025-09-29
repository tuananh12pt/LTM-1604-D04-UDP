package Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UDPClientSwing extends JFrame {
    private JTextField txtServerIP, txtPort;
    private JButton btnChooseFile, btnSend;
    private JTable tblHistory;
    private DefaultTableModel modelHistory;
    private JLabel lblStatus, lblFile;
    private File selectedFile;

    public UDPClientSwing() {
        setTitle("📤 UDP File Client");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ========== TOP PANEL ==========
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(52, 152, 219));

        JLabel lblIP = new JLabel("Server IP:");
        lblIP.setForeground(Color.WHITE);
        lblIP.setFont(new Font("Arial", Font.BOLD, 14));

        this.txtServerIP = new JTextField("127.0.0.1", 10);

        JLabel lblPort = new JLabel("Cổng:");
        lblPort.setForeground(Color.WHITE);
        lblPort.setFont(new Font("Arial", Font.BOLD, 14));

        this.txtPort = new JTextField("8888", 6);

        this.btnChooseFile = new JButton("Chọn file");
        btnChooseFile.setBackground(new Color(241, 196, 15));

        this.btnSend = new JButton("Gửi file");
        btnSend.setBackground(new Color(46, 204, 113));
        btnSend.setForeground(Color.WHITE);

        topPanel.add(lblIP);
        topPanel.add(txtServerIP);
        topPanel.add(lblPort);
        topPanel.add(txtPort);
        topPanel.add(btnChooseFile);
        topPanel.add(btnSend);

        add(topPanel, BorderLayout.NORTH);

        // ========== TABLE LỊCH SỬ ==========
        String[] cols = {"Tên file", "Kích thước (bytes)", "Thời gian", "Trạng thái"};
        this.modelHistory = new DefaultTableModel(cols, 0);
        this.tblHistory = new JTable(modelHistory);
        tblHistory.setRowHeight(25);
        tblHistory.getTableHeader().setBackground(new Color(44, 62, 80));
        tblHistory.getTableHeader().setForeground(Color.WHITE);
        tblHistory.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(tblHistory);
        add(scroll, BorderLayout.CENTER);

        // ========== STATUS PANEL ==========
        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        this.lblStatus = new JLabel(" 🟡 Chưa gửi file nào", SwingConstants.LEFT);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatus.setForeground(Color.ORANGE);

        this.lblFile = new JLabel(" 📂 File đã chọn: (Chưa có)", SwingConstants.LEFT);
        lblFile.setFont(new Font("Arial", Font.ITALIC, 13));

        statusPanel.add(lblStatus);
        statusPanel.add(lblFile);
        add(statusPanel, BorderLayout.SOUTH);

        // ========== ACTIONS ==========
        btnChooseFile.addActionListener(e -> chooseFile());
        btnSend.addActionListener(e -> new Thread(this::sendFile).start());
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            lblFile.setText(" 📂 File đã chọn: " + selectedFile.getAbsolutePath());
        }
    }

    private void sendFile() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Bạn chưa chọn file!");
            return;
        }
        try {
            String serverIP = txtServerIP.getText().trim();
            int port = Integer.parseInt(txtPort.getText().trim());
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(serverIP);

            // Gửi metadata
            String meta = "META::" + selectedFile.getName() + "::" + selectedFile.length();
            byte[] metaData = meta.getBytes();
            socket.send(new DatagramPacket(metaData, metaData.length, address, port));

            // Gửi dữ liệu file theo từng gói
            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, address, port);
                    socket.send(packet);
                }
            }

            // Gửi thông điệp kết thúc
            String endMsg = "END";
            byte[] endBytes = endMsg.getBytes();
            DatagramPacket endPacket = new DatagramPacket(endBytes, endBytes.length, address, port);
            socket.send(endPacket);

            socket.close();

            // Cập nhật lịch sử
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            SwingUtilities.invokeLater(() -> {
                modelHistory.addRow(new Object[]{
                        selectedFile.getName(),
                        selectedFile.length(),
                        time,
                        "✅ Đã gửi"
                });
                lblStatus.setText(" 🟢 File đã được gửi tới server " + serverIP + ":" + port);
                lblStatus.setForeground(new Color(0, 200, 0));
            });

        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "❌ Lỗi gửi file: " + ex.getMessage());
                lblStatus.setText(" 🔴 Lỗi gửi file");
                lblStatus.setForeground(Color.RED);
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDPClientSwing().setVisible(true));
    }
}
