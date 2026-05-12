# MultiCanvas

Ứng dụng vẽ cơ bản đa nền tảng được xây dựng bằng Kotlin Multiplatform và Compose Multiplatform, hướng tới Android và Desktop JVM để chạy trên Windows.

## Thành viên

| MSSV | Họ tên | Công việc chính |
| --- | --- | --- |
| 23127260 | Tô Minh Thắng | Định nghĩa cấu trúc dữ liệu cho các đối tượng vẽ, cài đặt Canvas tương tác, hỗ trợ chọn màu viền, độ dày viền, tô màu, soạn README và kiểm tra chéo tính năng. |
| 23127262 | Lý Quốc Thạnh | Khởi tạo project/repo, serialize/deserialize dữ liệu bản vẽ, tích hợp lưu/nạp file nhị phân, xuất ảnh, quay video demo và chuẩn bị PAT nộp bài. |

## Chức năng đã thực hiện

- Vẽ 6 loại đối tượng cơ bản: điểm, đường thẳng, hình ellipse, hình tròn, hình vuông và hình chữ nhật.
- Hiển thị nét vẽ nháp trong lúc kéo thả trên Canvas.
- Chọn màu đường viền từ bảng màu có sẵn.
- Chọn độ dày đường viền bằng slider.
- Bật/tắt tô màu và chọn màu tô cho các hình có diện tích.
- Hoàn tác đối tượng vừa vẽ và xóa toàn bộ bản vẽ.
- Dùng chung giao diện trong `commonMain` cho Android và Desktop.

## Các phần đang chờ tích hợp

- Lưu bản vẽ sang định dạng nhị phân tự định nghĩa.
- Nạp file nhị phân để tiếp tục vẽ.
- Xuất Canvas ra ảnh `.jpeg` hoặc `.png`.
- Bổ sung link video demo YouTube Unlisted.
- Kiểm tra Personal Access Token của GitHub.

## Video demo

- Link video: `TODO: Lý Quốc Thạnh bổ sung link YouTube Unlisted sau khi quay demo Desktop và Mobile`.

## Cách chạy

### Desktop Windows

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :composeApp:run
```

### Android

Cần cấu hình Android SDK bằng `ANDROID_HOME` hoặc file `local.properties`:

```properties
sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk
```

Sau đó build debug:

```powershell
.\gradlew.bat :composeApp:assembleDebug
```

## Ghi chú nộp bài

- Repo GitHub: <https://github.com/dinosauce-285/MultiCanvas.git>
- Token nộp bài cần có quyền chỉ đọc và đặt ngày hết hạn 30/01/2026 theo yêu cầu đề bài.
