package Client;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClientSwing extends JFrame {
    private JTextField txtIP, txtPort;
    private JButton btnChooseFile;
    private JTable tblFiles;
    private DefaultTableModel model;
    private JProgressBar progressBar;

    public UDPClientSwing() {
        setTitle("📤 UDP File Client");
        setSize(650, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ===== Panel cấu hình =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Cấu hình Client"));
        topPanel.add(new JLabel("Server IP:"));
        txtIP = new JTextField("localhost", 10);
        topPanel.add(txtIP);

        topPanel.add(new JLabel("Port:"));
        txtPort = new JTextField("8888", 5);
        topPanel.add(txtPort);

        btnChooseFile = new JButton("📂 Chọn file và gửi");
        topPanel.add(btnChooseFile);

        // ===== Bảng hiển thị file gửi =====
        model = new DefaultTableModel(new String[]{"Tên file", "Dung lượng", "Người nhận"}, 0);
        tblFiles = new JTable(model);
        JScrollPane scroll = new JScrollPane(tblFiles);
        scroll.setBorder(BorderFactory.createTitledBorder("Danh sách file đã gửi"));

        // ===== Thanh tiến trình =====
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(600, 30));
        progressBar.setForeground(new Color(52, 152, 219));
        JPanel progressPanel = new JPanel();
        progressPanel.setBorder(BorderFactory.createTitledBorder("Tiến trình gửi file"));
        progressPanel.add(progressBar);

        // ===== Thêm vào frame =====
        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);

        // ===== Sự kiện =====
        btnChooseFile.addActionListener(e -> chooseAndSendFile());
    }

    // Hàm chọn và gửi file
    private void chooseAndSendFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String fileName = file.getName();
            long fileSize = file.length();
            String serverIP = txtIP.getText();
            int port = Integer.parseInt(txtPort.getText());

            model.addRow(new Object[]{fileName, fileSize + " bytes", serverIP});
            new Thread(() -> sendFile(file, fileName, fileSize, serverIP, port)).start();
        }
    }

    // Hàm gửi file qua UDP
    private void sendFile(File file, String fileName, long fileSize, String ip, int port) {
        try (DatagramSocket socket = new DatagramSocket();
             FileInputStream fis = new FileInputStream(file)) {

            InetAddress address = InetAddress.getByName(ip);

            // Gửi tên file
            byte[] data = fileName.getBytes();
            socket.send(new DatagramPacket(data, data.length, address, port));

            // Gửi kích thước file
            data = String.valueOf(fileSize).getBytes();
            socket.send(new DatagramPacket(data, data.length, address, port));

            // Gửi dữ liệu file
            byte[] buffer = new byte[4096];
            int bytesRead;
            long sent = 0;
            while ((bytesRead = fis.read(buffer)) != -1) {
                socket.send(new DatagramPacket(buffer, bytesRead, address, port));
                sent += bytesRead;

                int percent = (int) ((sent * 100) / fileSize);
                progressBar.setValue(percent);
            }

            JOptionPane.showMessageDialog(this, "✅ Đã gửi xong file: " + fileName);
            progressBar.setValue(0);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDPClientSwing().setVisible(true));
    }
}
