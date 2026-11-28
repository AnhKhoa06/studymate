# StudyMate — Hệ thống ghép nhóm học tập và đặt phòng học trống

Ứng dụng web hỗ trợ sinh viên tìm bạn học phù hợp, lập nhóm, chốt khung giờ rảnh chung, gợi ý và đặt phòng học trống, đồng thời hỗ trợ quản lý phòng và duyệt yêu cầu đặt phòng. Đây là đồ án môn **Phát triển phần mềm hướng đối tượng**.

---

## Mục lục

- [Bài toán](#bài-toán)
- [Vai trò người dùng](#vai-trò-người-dùng)
- [Tính năng chính](#tính-năng-chính)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Kiến trúc & Mẫu thiết kế](#kiến-trúc--mẫu-thiết-kế)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Cài đặt & Chạy dự án](#cài-đặt--chạy-dự-án)
- [Kiểm thử](#kiểm-thử)
- [Quy ước Git & Bảo mật](#quy-ước-git--bảo-mật)
- [Tài liệu tham khảo](#tài-liệu-tham-khảo)
- [Nhóm thực hiện](#nhóm-thực-hiện)

---

## Bài toán

Sinh viên thường tìm bạn học chung và tìm phòng học trống qua nhiều kênh rời rạc (Zalo, Facebook, hỏi trực tiếp), dẫn đến tốn thời gian, khó thống nhất khung giờ rảnh, dễ đặt trùng phòng hoặc đặt phòng nhưng không sử dụng. **StudyMate** hợp nhất toàn bộ quy trình thành một luồng liền mạch:

1. Tạo hồ sơ học tập (môn học, mục tiêu, khung giờ rảnh).
2. Tìm bạn học phù hợp theo điểm tương thích (Match Score).
3. Lập nhóm học tập và xác định giờ rảnh chung.
4. Nhận gợi ý phòng học phù hợp, đặt phòng chống trùng và check-in.

---

## Vai trò người dùng

| Vai trò   | Quyền chính                                                                             |
| --------- | --------------------------------------------------------------------------------------- |
| `STUDENT` | Quản lý hồ sơ học tập, tìm bạn, lập/tham gia nhóm, chat nhóm, đặt/hủy phòng, check-in   |
| `MANAGER` | Quản lý phòng học, duyệt hoặc từ chối yêu cầu đặt phòng, xem thống kê sử dụng phòng     |
| `ADMIN`   | Quản lý tài khoản, khóa/mở khóa tài khoản vi phạm, xem nhật ký hoạt động hệ thống       |

Trong mỗi nhóm học tập có phân biệt thêm **trưởng nhóm** (mời/xóa thành viên, đặt phòng cho nhóm) và **thành viên**.

---

## Tính năng chính

### Mức bắt buộc
- **Tài khoản & Hồ sơ**: Đăng ký, đăng nhập, quên/đặt lại mật khẩu, khóa tạm thời khi đăng nhập sai nhiều lần; hồ sơ học tập (khoa, lớp, môn đang học, mục tiêu, giờ rảnh).
- **Tìm bạn & Match Score**: Tính điểm phù hợp (0–100) kèm giải thích chi tiết từng tiêu chí.
- **Nhóm học tập**: Tạo nhóm, gửi/nhận lời mời, tìm giờ rảnh chung của cả nhóm.
- **Phòng & Đặt phòng**: Quản lý phòng học, kiểm tra lịch trống, đặt/hủy phòng, chống đặt trùng đồng thời, duyệt yêu cầu đặt phòng.
- **Gợi ý phòng**: Gợi ý phòng dựa trên sức chứa, thiết bị và khung giờ rảnh chung của nhóm.
- **Chat nhóm & Thông báo realtime**: Nhắn tin nhóm, thông báo lời mời và kết quả duyệt phòng qua WebSocket.

### Mức nâng cao
- **Check-in QR & Điểm uy tín**: Check-in bằng mã QR, tự động hủy đặt phòng sau 15 phút nếu vắng mặt và trừ điểm uy tín.
- **Trợ lý AI (LLM)**: Gợi ý học tập, giải thích độ phù hợp, tóm tắt thảo luận nhóm (có cơ chế dự phòng và giới hạn tần suất).
- **Nhắc lịch**: Nhắc buổi học và lịch đặt phòng qua email, hiển thị lịch tuần.
- **Dashboard thống kê**: Tỷ lệ lấp đầy phòng, giờ cao điểm, phòng bị bỏ trống dành cho `MANAGER`.

---

## Công nghệ sử dụng

| Thành phần       | Công nghệ                                                  |
| ---------------- | ---------------------------------------------------------- |
| Ngôn ngữ         | Java 21 (LTS)                                              |
| Framework        | Spring Boot 4.1.2 (Maven Wrapper `./mvnw` / `.\mvnw.cmd`)  |
| Web & Giao diện  | Spring MVC, Thymeleaf, CSS Design Tokens (Dark/Light Mode) |
| Cơ sở dữ liệu    | MySQL 8, Spring Data JPA, Flyway Migration                 |
| Bảo mật          | Spring Security (BCrypt, CSRF, RBAC, `@PreAuthorize`)      |
| Realtime & Mail  | Spring WebSocket (STOMP), Spring Mail                      |
| Kiểm thử         | JUnit 5, MockMvc, `@DataJpaTest`, Spring Security Test     |

---

## Kiến trúc & Mẫu thiết kế

- **Kiến trúc phân lớp theo module**: `controller` → `service` → `repository` → `entity`, sử dụng `dto` để giao tiếp giữa Controller và View.
- **Strategy Pattern**: Mỗi tiêu chí tính Match Score là một chiến lược độc lập, dễ mở rộng.
- **State Pattern**: Quản lý vòng đời đặt phòng (`PENDING`, `APPROVED`, `CHECKED_IN`, `NO_SHOW`, `CANCELLED`).
- **Observer Pattern**: Phát sự kiện qua `ApplicationEventPublisher` để gửi thông báo realtime và email.
- **Dependency Injection & SOLID**: Tiêm phụ thuộc qua constructor, tách biệt trách nhiệm rõ ràng.

---

## Cấu trúc thư mục

```text
studymate/
├── docs/                                # Tài liệu dự án (PROPOSAL, PHAN-CONG, DESIGN)
├── src/
│   ├── main/
│   │   ├── java/com/studymate/
│   │   │   ├── common/                  # Cấu hình chung, Security, Layout, MenuRegistry
│   │   │   ├── user/                    # Tài khoản, hồ sơ, bảo mật, admin, trợ lý AI
│   │   │   ├── matching/                # Tìm bạn & tính Match Score
│   │   │   ├── group/                   # Nhóm học tập & giờ rảnh chung
│   │   │   ├── chat/                    # Chat nhóm & thông báo WebSocket
│   │   │   ├── room/                    # Phòng học & đặt phòng
│   │   │   └── insight/                 # Thống kê & nhắc lịch
│   │   └── resources/
│   │       ├── db/migration/            # Flyway SQL migrations (V<yyyyMMddHHmm>__*.sql)
│   │       ├── static/                  # CSS, JS, hình ảnh
│   │       ├── templates/               # Thymeleaf views & layout fragments
│   │       ├── application.properties
│   │       └── application-local.properties.example
│   └── test/                            # Unit test, Repository test, Security integration test
├── AGENTS.md                            # Quy ước phát triển cho thành viên & AI agent
└── pom.xml
```

---

## Cài đặt & Chạy dự án

### 1. Yêu cầu môi trường
- **JDK 21** trở lên
- **MySQL 8**
- **Git**

### 2. Tạo cơ sở dữ liệu MySQL
Mở MySQL và tạo 2 database (dùng cho chạy ứng dụng và chạy test):

```sql
CREATE DATABASE studymate_oop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE studymate_oop_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Cấu hình môi trường local
Sao chép file mẫu cấu hình local:

```powershell
Copy-Item src\main\resources\application-local.properties.example src\main\resources\application-local.properties
```

Mở `src\main\resources\application-local.properties` và điền thông tin MySQL của bạn (file này đã nằm trong `.gitignore`, **không commit** lên Git).

Các biến môi trường tùy chọn:
- `DB_USERNAME`: Tên đăng nhập MySQL (mặc định `root`)
- `DB_PASSWORD`: Mật khẩu MySQL
- `GEMINI_API_KEY`: API key cho tính năng Trợ lý AI (nếu dùng)
- `GEMINI_LLM_MODEL`: Tên model Gemini (mặc định `gemini-3.5-flash-lite`)

### 4. Khởi chạy ứng dụng

Trên Windows (PowerShell):
```powershell
.\mvnw.cmd spring-boot:run
```

Trên macOS / Linux:
```bash
./mvnw spring-boot:run
```

Truy cập ứng dụng tại: `http://localhost:8080`

---

## Kiểm thử

Đảm bảo MySQL đang chạy và đã tạo database `studymate_oop_test`. Nếu tài khoản MySQL `root` có mật khẩu, thiết lập `DB_PASSWORD` trước khi chạy test:

```powershell
$env:DB_PASSWORD="mat_khau_mysql_cua_ban"
.\mvnw.cmd test
```

---

## Quy ước Git & Bảo mật

- **Không commit trực tiếp vào `main`**. Mỗi tính năng/sửa lỗi tạo nhánh riêng theo dạng `feature/<module>-<mo-ta>` và mở Pull Request vào `main`.
- **Commit message**: `feat(module): ...`, `fix(module): ...`, `test: ...`, `docs: ...`.
- **Bảo mật**: Không commit mật khẩu, token hoặc API key. Luôn chạy `.\mvnw.cmd test` trước khi tạo PR.

---

## Tài liệu tham khảo

- [docs/PROPOSAL.md](docs/PROPOSAL.md) — Đề cương, yêu cầu chức năng, bảo mật và dữ liệu
- [docs/PHAN-CONG.md](docs/PHAN-CONG.md) — Phân công module và tiến độ thực hiện
- [docs/DESIGN.md](docs/DESIGN.md) — Hệ thống thiết kế giao diện (Design System)
- [AGENTS.md](AGENTS.md) — Quy ước code, kiểm thử và làm việc nhóm

---

## Nhóm thực hiện

Khoa, Khương, Huy, Lịch, Khang.
