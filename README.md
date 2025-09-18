<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
    TRUYỀN FILE QUA UDP 
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>
# 1. Giới thiệu

Trong lĩnh vực mạng máy tính, **UDP (User Datagram Protocol)** là một trong những giao thức quan trọng thuộc tầng Transport trong mô hình TCP/IP.  
UDP được sử dụng để truyền dữ liệu giữa các thiết bị trong mạng một cách **nhanh chóng, gọn nhẹ** mà không cần nhiều cơ chế kiểm soát phức tạp.

## 🔹 Đặc điểm chính của UDP
- **Không kết nối (Connectionless):** UDP không cần thiết lập kết nối giữa client và server như TCP → truyền tải nhanh hơn.
- **Không đảm bảo (Unreliable):** không đảm bảo dữ liệu đến nơi, không có kiểm tra toàn vẹn dữ liệu hay xác nhận gói tin.
- **Đơn giản và hiệu quả:** UDP có header chỉ 8 byte (nhỏ hơn TCP 20 byte) → tiết kiệm băng thông.
- **Hỗ trợ broadcast/multicast:** gửi dữ liệu tới nhiều thiết bị cùng lúc.

## 🔹 Cấu trúc gói tin UDP
Một gói tin UDP bao gồm 4 phần chính:
1. **Source Port (16 bit):** Cổng nguồn.  
2. **Destination Port (16 bit):** Cổng đích.  
3. **Length (16 bit):** Độ dài toàn bộ gói UDP.  
4. **Checksum (16 bit):** Kiểm tra lỗi cơ bản.  

## 🔹 Ứng dụng của UDP
UDP thường được sử dụng trong các ứng dụng cần tốc độ hơn độ tin cậy tuyệt đối:
- Truyền phát video/audio trực tuyến (YouTube, Zoom, VoIP).  
- Game online (yêu cầu phản hồi nhanh, chấp nhận mất gói).  
- DNS (Domain Name System).  
- Streaming đa phương tiện, IPTV.  

---

# 2. Ngôn ngữ lập trình sử dụng
- **Java**

---

# 3. Công nghệ sử dụng
- **Java**: ngôn ngữ lập trình chính.  
- **UDP (User Datagram Protocol):** giao thức truyền nhanh, không kết nối.  
- **Socket lập trình mạng:** `DatagramSocket`, `DatagramPacket`.  
- **Java IO:** đọc file (`FileInputStream`), ghi file (`FileOutputStream`).  
- **Header Sequence Number:** số thứ tự gói tin để ghép file đúng.  

---
## 3. Mô hình hoạt động
1. **Client**: Đọc file → chia nhỏ thành gói tin → gửi qua UDP.  
2. **Server**: Nhận gói tin → ghép lại thành file gốc.  

---

## 4. Cài đặt & chạy

### 🔧 Yêu cầu môi trường
- Cài **Java JDK 8+**  
- IDE: IntelliJ IDEA / Eclipse (khuyến nghị)  
- Github để quản lý mã nguồn  

### ▶️ Biên dịch
```bash
cd src
javac UDPServer.java
javac UDPClient.java

📌 *Nguồn:DaiNam University. Nguyễn Tuấn Anh Gmail: tuananh12cpt@gmail.com


