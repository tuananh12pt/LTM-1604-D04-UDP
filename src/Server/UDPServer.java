package Server;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer extends JFrame {
    private JTextField txtPort;
    private JButton btnStart, btnChooseDir;
    private JTable tblFiles;
    private DefaultTableModel model;
    private File saveDir = new File(".");
    private JProgressBar progressBar;

    public UDPServer() {
        setTitle("📥 UDP File Server");
        setSize(650, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ===== Panel cấu hình =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Cấu hình Server"));
        topPanel.add(new JLabel("Port:"));
        txtPort = new JTextField("8888", 8);
        topPanel.add(txtPort);

        btnStart = new JButton("▶ Bắt đầu");
        btnChooseDir = new JButton("📂 Chọn thư mục lưu");
        topPanel.add(btnStart);
        topPanel.add(btnChooseDir);

        // ===== Bảng hiển thị file nhận =====
        model = new DefaultTableModel(new String[]{"Tên file", "Dung lượng", "Người gửi"}, 0);
        tblFiles = new JTable(model);
        JScrollPane scroll = new JScrollPane(tblFiles);
        scroll.setBorder(BorderFactory.createTitledBorder("Danh sách file đã nhận"));

        // ===== Thanh tiến trình =====
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(600, 30));
        progressBar.setForeground(new Color(46, 204, 113));
        JPanel progressPanel = new JPanel();
        progressPanel.setBorder(BorderFactory.createTitledBorder("Tiến trình nhận file"));
        progressPanel.add(progressBar);

        // ===== Thêm vào frame =====
        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);

        // ===== Sự kiện =====
        btnStart.addActionListener(e -> new Thread(this::startServer).start());
        btnChooseDir.addActionListener(e -> chooseDirectory());
    }

    // Hàm chọn thư mục lưu file
    private void chooseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            saveDir = chooser.getSelectedFile();
        }
    }

    // Hàm chạy server
    private void startServer() {
        try {
            int port = Integer.parseInt(txtPort.getText());
            DatagramSocket socket = new DatagramSocket(port);
            byte[] buffer = new byte[65535];
            JOptionPane.showMessageDialog(this, "Server đang lắng nghe tại port " + port);

            while (true) {
                // Nhận tên file
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String fileName = new String(packet.getData(), 0, packet.getLength());

                // Nhận kích thước file
                socket.receive(packet);
                long fileSize = Long.parseLong(new String(packet.getData(), 0, packet.getLength()));

                // Nhận dữ liệu file
                File outFile = new File(saveDir, fileName);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    long received = 0;
                    while (received < fileSize) {
                        socket.receive(packet);
                        int len = packet.getLength();
                        fos.write(packet.getData(), 0, len);
                        received += len;

                        int percent = (int) ((received * 100) / fileSize);
                        progressBar.setValue(percent);
                    }
                }

                model.addRow(new Object[]{fileName, fileSize + " bytes", packet.getAddress().toString()});
                JOptionPane.showMessageDialog(this, "Đã nhận file: " + fileName);
                progressBar.setValue(0);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDPServer().setVisible(true));
    }
}
