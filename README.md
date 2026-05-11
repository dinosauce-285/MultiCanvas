# MultiCanvas

Ung dung ve co ban da nen tang bang Kotlin Multiplatform va Compose Multiplatform, huong toi Android va Desktop JVM de chay tren Windows.

## Thanh vien

| Ho ten | Vai tro | Cong viec chinh |
| --- | --- | --- |
| Thang | Thanh vien | Cau truc du lieu doi tuong ve, Canvas tuong tac, tuy chinh mau/net/to mau, README, kiem tra cheo |
| Thanh | Thanh vien | Khoi tao project/repo, luu va nap file nhi phan, xuat anh, video demo, PAT nop bai |

> Cap nhat them MSSV/lop neu giang vien yeu cau trong bieu mau nop bai.

## Chuc nang da thuc hien

- Ve 6 loai doi tuong co ban: diem, duong thang, ellipse, hinh tron, hinh vuong, hinh chu nhat.
- Hien thi net ve nhap trong luc keo tha tren Canvas.
- Chon mau duong vien tu bang mau co san.
- Chon do day duong vien bang slider.
- Bat/tat to mau va chon mau to cho cac hinh co dien tich.
- Hoan tac doi tuong vua ve va xoa toan bo ban ve.
- Dung chung UI trong `commonMain` cho Android va Desktop.

## Cac phan dang cho tich hop

- Luu ban ve sang dinh dang nhi phan tu dinh nghia.
- Nap file nhi phan de tiep tuc ve.
- Xuat Canvas ra anh `.jpeg` hoac `.png`.
- Link video demo YouTube Unlisted.
- Kiem tra Personal Access Token cua GitHub.

## Video demo

- Link video: `TODO: Thanh bo sung link YouTube Unlisted sau khi quay demo Desktop va Mobile`.

## Cach chay

### Desktop Windows

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :composeApp:run
```

### Android

Can cau hinh Android SDK bang `ANDROID_HOME` hoac file `local.properties`:

```properties
sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk
```

Sau do build debug:

```powershell
.\gradlew.bat :composeApp:assembleDebug
```

## Ghi chu nop bai

- Repo GitHub: <https://github.com/dinosauce-285/MultiCanvas.git>
- Token nop bai can o quyen chi doc va dat ngay het han 30/01/2026 theo yeu cau de bai.
