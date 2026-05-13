# MultiCanvas

MultiCanvas là ứng dụng vẽ cơ bản đa nền tảng được xây dựng bằng Kotlin Multiplatform và Compose Multiplatform. Ứng dụng hỗ trợ chạy trên Desktop Windows và Mobile Android.

## Thành viên nhóm

| MSSV     | Họ tên        | Công việc chính                                                                                                                                                |
| -------- | ------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 23127260 | Tô Minh Thắng | Định nghĩa cấu trúc dữ liệu cho các đối tượng vẽ, cài đặt Canvas tương tác, hỗ trợ chọn màu viền, độ dày viền, tô màu, soạn README và kiểm tra chéo tính năng. |
| 23127262 | Lý Quốc Thạnh | Khởi tạo project/repo, serialize/deserialize dữ liệu bản vẽ, tích hợp lưu/nạp file nhị phân, xuất ảnh, quay video demo và chuẩn bị PAT nộp bài.                |

## Chức năng đã thực hiện

- Vẽ các đối tượng cơ bản: điểm, đường thẳng, hình ellipse, hình tròn, hình vuông và hình chữ nhật.
- Hiển thị nét vẽ nháp trong lúc kéo thả trên Canvas.
- Hỗ trợ tô màu cho các hình có vùng diện tích.
- Hỗ trợ công cụ tô màu trực tiếp vào hình đã vẽ.
- Hỗ trợ chọn màu đường viền và màu tô từ bảng màu có sẵn.
- Hỗ trợ chọn độ dày đường viền bằng slider.
- Hỗ trợ hoàn tác đối tượng vừa vẽ và xóa toàn bộ bản vẽ.
- Hỗ trợ lưu bản vẽ sang file nhị phân tự định nghĩa với phần mở rộng `.mcv`.
- Hỗ trợ nạp lại file `.mcv` để tiếp tục chỉnh sửa bản vẽ.
- Hỗ trợ xuất bản vẽ ra ảnh `.png` hoặc `.jpeg`.
- Dùng chung giao diện trong `commonMain` cho Desktop Windows và Android.

## Video demo

- Link video demo: <https://www.youtube.com/watch?v=FzMW9ryKE-M>
- Video demo thể hiện ứng dụng chạy trên Desktop Windows và Mobile Android.
- Video được chuẩn bị theo yêu cầu: thời lượng dưới 5 phút, không lồng tiếng, không lồng nhạc; phần giải thích thao tác được gõ bằng Notepad.
- Video cần được đặt ở chế độ YouTube Unlisted để chỉ người có link mới xem được.

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

Hoặc mở project bằng Android Studio, chọn emulator/thiết bị Android và chạy cấu hình `composeApp`.

## Ghi chú

- Repo GitHub: <https://github.com/dinosauce-285/MultiCanvas.git>
