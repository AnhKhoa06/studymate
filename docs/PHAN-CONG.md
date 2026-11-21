# StudyMate: Phân công công việc và kế hoạch triển khai

Nhóm: Khoa, Khương, Huy, Lịch, Khang. Repo: github.com/AnhKhoa06/studymate. Tài liệu này đi kèm file AGENTS.md trong repo (quy ước code cho cả nhóm và cho agent).
Tên lớp trong tài liệu chỉ là gợi ý, không bắt buộc.

## 1. Thiết lập ban đầu (mỗi người làm trước khi code)

1. Chấp nhận lời mời vào repo GitHub (kiểm tra email).
2. Cài JDK 21, Git, MySQL 8 và MySQL Workbench, VS Code (kèm Extension Pack for Java và Spring Boot Extension Pack).
3. Clone repo vào thư mục C:\\PTPMHDT bằng lệnh `git clone https://github.com/AnhKhoa06/studymate.git`.
4. Tạo cơ sở dữ liệu trên máy mình: `CREATE DATABASE studymate_oop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
5. Tạo file `src/main/resources/application-local.properties` (file này đã nằm trong .gitignore, tuyệt đối không commit) và ghi mật khẩu MySQL của máy mình vào đó.
6. Chạy thử: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"`, mở http://localhost:8080. Thấy trang đăng nhập của Spring Security là khung đã chạy đúng.
7. Cài extension agent (Antigravity hoặc Copilot), mở đúng thư mục studymate để agent đọc AGENTS.md. Tắt tính năng tự chấp nhận thay đổi và đặt quyền ở mức hỏi trước khi chạy lệnh.

## 2. Quy ước chung cần thống nhất

| Mục                    | Quy ước                                                                                                                                                                                                                                          |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Tên CSDL               | `studymate_oop` trên máy của mỗi người                                                                                                                                                                                                           |
| Nhánh                  | `feature/<module>-<mo-ta>`, mỗi việc nhỏ một nhánh, merge sớm                                                                                                                                                                                    |
| Commit                 | `feat(module): ...`, `fix(module): ...`, `test: ...`, `docs: ...`                                                                                                                                                                                |
| Migration Flyway       | `V<yyyyMMddHHmm>__mo_ta.sql`, mỗi người chỉ tạo bảng của module mình, không sửa file đã merge                                                                                                                                                    |
| Tham chiếu giữa module | Dùng id (userId, groupId, roomId) thay cho quan hệ JPA chéo module; module chỉ gọi Service của module khác, không dùng Repository của module khác                                                                                                |
| Role                   | `STUDENT`, `MANAGER`, `ADMIN`                                                                                                                                                                                                                    |
| Đường dẫn              | user: `/login`, `/register`, `/profile`, `/admin/users`; matching: `/matching`; group: `/groups`; chat: `/chat`, `/notifications`, `/ws`; room: `/rooms`, `/bookings`, `/manager/rooms`; insight: `/insight`, `/manager/dashboard`, `/assistant` |
| File dùng chung        | `pom.xml`, `SecurityConfig`, `application.properties`, layout chung: chỉ Khoa sửa, người khác cần gì thì nhờ Khoa hoặc tạo PR nhỏ riêng                                                                                                          |
| Mỗi người              | Viết test cho module mình và phải giải thích được mọi dòng code trong module (kể cả code do AI sinh ra)                                                                                                                                          |

## 3. Các giai đoạn

| Giai đoạn          | Mục tiêu                                                                                                                                                                        | Ai làm                           |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------- |
| 0. Nền tảng        | Khoa merge sớm nhất: User, Role, bảng users, SecurityConfig, layout chung, xử lý lỗi chung. Mọi người thiết lập máy, vẽ ERD và giao diện tĩnh cho module của mình trong lúc chờ | Khoa làm, 4 bạn còn lại chuẩn bị |
| 1. Lõi từng module | Entity, migration, CRUD và logic chính của module. Huy merge bảng `study_groups` sớm vì Lịch cần                                                                                | Tất cả, song song                |
| 2. Tích hợp        | Matching với nhóm, nhóm với đặt phòng, gợi ý phòng, gửi thông báo qua event                                                                                                     | Tất cả, phối hợp                 |
| 3. Hoàn thiện      | Chat và thông báo realtime, chức năng nâng cao, rà soát bảo mật, tối ưu truy vấn, kiểm thử, báo cáo và demo                                                                     | Tất cả                           |

Các bạn khác chỉ cần clone và tạo nhánh từ `main` sau khi Khoa merge xong phần nền tảng của giai đoạn 0.

## 4. Phân công chi tiết

### 4.1 Khoa: Nền tảng, Bảo mật, Tài khoản, Hồ sơ

- **Sở hữu:** package `user`, `common`; bảng `users`, `student_profiles`, `profile_subjects`, `availability_slots`; các file dùng chung.
- **Giai đoạn 0 (làm đầu tiên):** entity `User` (email duy nhất, passwordHash, fullName, role, enabled), enum `Role`, migration tạo `users`, `SecurityConfig` (form login, BCrypt, mặc định chặn mọi request, chỉ mở trang đăng nhập, đăng ký và tài nguyên tĩnh), layout Thymeleaf chung với menu theo role, `@ControllerAdvice` xử lý lỗi, lớp tiện ích lấy người dùng hiện tại.
- **Bắt buộc:** đăng ký, đăng nhập, đăng xuất có validation; hồ sơ học tập (khoa, lớp, trình độ, mục tiêu, môn đang học, giờ rảnh theo tuần); trang ADMIN quản lý tài khoản (phân trang, tìm kiếm, khóa hoặc mở khóa, đổi role); dữ liệu mẫu cho ADMIN, MANAGER và vài STUDENT (chỉ ở profile local).
- **Nâng cao:** khóa tạm khi đăng nhập sai nhiều lần, nhật ký hoạt động, quên mật khẩu qua email.
- **Hoàn thành khi:** chưa đăng nhập không vào được trang nào ngoài trang công khai; sai role bị 403; mật khẩu trong CSDL là chuỗi BCrypt; có test MockMvc cho phân quyền.
- **Câu hỏi thầy có thể hỏi:** vì sao dùng BCrypt thay vì MD5 hoặc SHA; CSRF và session hoạt động thế nào; khác nhau giữa phân quyền theo URL và `@PreAuthorize`.

### 4.2 Khương: Hồ sơ gợi ý và Matching

- **Sở hữu:** package `matching`. Đọc dữ liệu hồ sơ từ module của Khoa qua Service.
- **Bắt buộc:**
  - Trang tìm bạn học có lọc theo môn và khoa, danh sách phân trang kèm match score.
  - Interface `MatchCriterion` (Strategy) cùng các lớp tiêu chí: môn học chung, giờ rảnh chung, mục tiêu, trình độ; `MatchService` cộng điểm có trọng số và trả `MatchResultDto` kèm chi tiết từng tiêu chí.
  - `CommonAvailabilityService`: nhận danh sách người dùng, trả các khung giờ chung dài nhất (dùng cho nhóm).
  - Dùng truy vấn JPA để lọc ứng viên trước rồi mới tính điểm, không tải cả bảng.
- **Nâng cao:** bảng `match_feedback` để người dùng đánh giá độ phù hợp và điều chỉnh trọng số; phối hợp Khang để giải thích kết quả bằng ngôn ngữ tự nhiên.
- **Hoàn thành khi:** thêm một tiêu chí mới chỉ cần thêm một lớp, không sửa `MatchService`; có unit test cho từng tiêu chí và tổng điểm; có `@DataJpaTest` cho truy vấn lọc.
- **Phụ thuộc:** User và hồ sơ của Khoa. Trong lúc chờ, dùng dữ liệu mẫu.

### 4.3 Huy: Nhóm học tập, Chat và Thông báo realtime

- **Sở hữu:** package `group`, `chat`; bảng `study_groups`, `group_members`, `group_invitations`, `chat_messages`, `notifications`.
- **Bắt buộc:**
  - Tạo nhóm (tên, môn, sức chứa, mô tả), người tạo là trưởng nhóm. Merge bảng `study_groups` sớm.
  - Mời, chấp nhận, từ chối lời mời; rời nhóm; trưởng nhóm xóa thành viên; kiểm tra sức chứa và lời mời trùng.
  - Chat nhóm realtime bằng Spring WebSocket (STOMP): chỉ thành viên nhóm gửi và nhận được, lưu lịch sử có phân trang.
  - Thông báo realtime và trang thông báo (đã đọc, chưa đọc). Định nghĩa các event (ví dụ `InvitationSentEvent`, `BookingApprovedEvent`) trong `common.event` ở PR đầu tiên để module khác phát event, Huy lắng nghe bằng `ApplicationEventPublisher` (Observer).
  - Bảo mật WebSocket: kênh chat yêu cầu đăng nhập, không đăng ký nghe được kênh của nhóm khác.
- **Nâng cao:** hiển thị trạng thái đang gõ, tóm tắt chat bằng AI (phối hợp Khang).
- **Hoàn thành khi:** người ngoài nhóm không đọc hay gửi được tin; mở lại trang vẫn thấy lịch sử chat; có test cho quy tắc nhóm.
- **Phụ thuộc:** User của Khoa.

### 4.4 Lịch: Phòng học và Đặt phòng

- **Sở hữu:** package `room`; bảng `rooms`, `bookings`.
- **Bắt buộc:**
  - MANAGER quản lý phòng: mã, tòa, sức chứa, thiết bị, trạng thái, có tìm kiếm và phân trang.
  - Xem lịch trống theo ngày; trưởng nhóm đặt phòng cho nhóm; hủy đặt phòng.
  - Chống đặt trùng: truy vấn khoảng thời gian chồng lấn trong transaction, kết hợp `@Version` hoặc ràng buộc ở CSDL.
  - Vòng đời đặt phòng bằng State pattern với tối thiểu các trạng thái PENDING, APPROVED, REJECTED, CANCELLED.
  - MANAGER duyệt hoặc từ chối, và phát event để Huy gửi thông báo.
- **Nâng cao:** check-in bằng mã QR, `@Scheduled` tự hủy sau 15 phút không đến, điểm uy tín, ghép nhiều nhóm nhỏ vào phòng lớn.
- **Hoàn thành khi:** không đặt trùng được kể cả khi hai yêu cầu đến cùng lúc (có test); chuyển trạng thái sai (ví dụ đã hủy rồi duyệt) bị từ chối; người dùng chỉ hủy được đặt phòng của nhóm mình.
- **Phụ thuộc:** User của Khoa, `study_groups` của Huy.

### 4.5 Khang: Gợi ý, Thống kê, Nhắc lịch và Trợ lý AI

- **Sở hữu:** package `insight`; bảng `reminder_logs` (và `ai_requests` nếu cần).
- **Bắt buộc:**
  - Gợi ý phòng cho nhóm: nhận nhóm và ngày, lấy giờ chung từ Khương, sức chứa và thiết bị từ nhóm, danh sách phòng trống từ Lịch; trả top phòng phù hợp kèm lý do.
  - Dashboard MANAGER: tỷ lệ lấp đầy phòng, giờ cao điểm, phòng ít dùng. Dùng truy vấn tổng hợp trả thẳng DTO, vẽ biểu đồ bằng Chart.js.
  - Nhắc lịch qua email: `JavaMailSender` kết hợp `@Scheduled`, gửi trước giờ học và ghi log để không gửi trùng.
- **Nâng cao:** Trợ lý AI gọi API LLM (giải thích vì sao hai bạn hợp nhau, tóm tắt chat thành checklist buổi học). API key để trong biến môi trường, có xử lý lỗi và hết thời gian chờ, có phương án dự phòng khi API không dùng được, giới hạn tần suất gọi.
- **Hoàn thành khi:** dashboard không bị lỗi N+1 (kiểm tra bằng log SQL); gợi ý phòng có test; AI lỗi không làm hỏng trang.
- **Phụ thuộc:** Khương (giờ chung), Lịch (phòng), Huy (nhóm). Giai đoạn đầu làm dashboard và email trên dữ liệu mẫu, tích hợp sau.

## 5. Hợp đồng giữa các module

| Ai gọi     | Gọi gì                                  | Của ai                               |
| ---------- | --------------------------------------- | ------------------------------------ |
| Mọi module | Lớp tiện ích lấy người dùng hiện tại    | Khoa (`common`)                      |
| Khương     | Dữ liệu hồ sơ: môn học, giờ rảnh        | Khoa (`ProfileService`)              |
| Lịch       | Kiểm tra người dùng có phải trưởng nhóm | Huy (`GroupService`)                 |
| Khang      | Giờ rảnh chung của nhóm                 | Khương (`CommonAvailabilityService`) |
| Khang      | Danh sách phòng trống theo giờ          | Lịch (`RoomService`)                 |
| Khang      | Danh sách thành viên nhóm               | Huy (`GroupService`)                 |
| Mọi module | Phát event để gửi thông báo             | Huy lắng nghe                        |

Tên lớp và phương thức ở trên là đề xuất, mỗi người chốt chữ ký phương thức trong PR đầu tiên của module mình rồi báo cả nhóm để người khác gọi đúng.

## 6. Review chéo

- Khương và Huy duyệt PR của nhau; Lịch và Khang duyệt PR của nhau.
- Khoa duyệt mọi PR đụng tới bảo mật hoặc file dùng chung; Khương duyệt PR của Khoa.
- Mỗi PR cần ít nhất 1 người duyệt và phải điền đầy đủ checklist trong mẫu PR (đã chạy test, không commit bí mật, endpoint có phân quyền, đã đọc lại code do AI sinh ra).

## 7. Kiểm tra trước khi demo

- Mọi endpoint chưa đăng nhập đều bị chặn; sai role bị từ chối; dữ liệu người khác không truy cập được bằng cách đổi id trên URL.
- Không có mật khẩu hay API key nào trong repo (kiểm tra cả lịch sử commit).
- Không có lỗi N+1 ở các danh sách chính (xem log SQL); danh sách có phân trang.
- Đặt phòng không bị trùng khi hai người đặt cùng lúc.
- Mỗi người giải thích được module của mình: luồng xử lý, phân quyền, truy vấn và pattern đã dùng.
- Dữ liệu mẫu đủ để demo trọn luồng: tìm bạn, lập nhóm, chốt giờ chung, nhận gợi ý phòng, đặt và duyệt phòng, chat và thông báo.
