# StudyMate: Hệ thống ghép nhóm học tập và đặt phòng học trống

Đề cương dự án cuối kỳ, môn Phát triển phần mềm hướng đối tượng. Nhóm thực hiện: Khoa, Khương, Huy, Lịch, Khang.

## 1. Bài toán

Sinh viên thường tìm bạn học chung và tìm phòng học trống qua Zalo, Facebook hoặc hỏi trực tiếp. Cách này tốn thời gian, dễ đặt trùng phòng, nhóm khó chốt giờ vì mỗi người rảnh một khung khác nhau, và phòng thường được đặt nhưng không ai đến. StudyMate gom các việc đó thành một hành trình liền mạch: tìm bạn học phù hợp, lập nhóm, chốt giờ rảnh chung, nhận gợi ý phòng, đặt phòng và check-in.

## 2. Vai trò người dùng

| Vai trò | Quyền chính                                                                             |
| ------- | --------------------------------------------------------------------------------------- |
| STUDENT | Tạo hồ sơ học tập, tìm bạn, lập và tham gia nhóm, chat nhóm, đặt và hủy phòng, check-in |
| MANAGER | Quản lý phòng học, duyệt hoặc từ chối yêu cầu đặt phòng, xem thống kê sử dụng phòng     |
| ADMIN   | Quản lý tài khoản, khóa tài khoản vi phạm, xem nhật ký hệ thống                         |

Trong mỗi nhóm học có thêm hai vai trò theo dữ liệu: trưởng nhóm (mời và xóa thành viên, đặt phòng cho nhóm) và thành viên.

## 3. Chức năng

**Mức bắt buộc**

| #   | Chức năng                       | Nội dung chính                                                                   |
| --- | ------------------------------- | -------------------------------------------------------------------------------- |
| 1   | Tài khoản và hồ sơ              | Đăng ký, đăng nhập, hồ sơ học tập: khoa, lớp, môn đang học, mục tiêu, giờ rảnh   |
| 2   | Tìm bạn và match score          | Điểm phù hợp 0-100, hiển thị chi tiết từng tiêu chí để giải thích được cách tính |
| 3   | Nhóm học tập                    | Lập nhóm, gửi và nhận lời mời, tìm giờ rảnh chung của cả nhóm                    |
| 4   | Phòng và đặt phòng              | Quản lý phòng, kiểm tra lịch trống, đặt và hủy, chống đặt trùng, duyệt yêu cầu   |
| 5   | Gợi ý phòng                     | Gợi ý phòng theo sức chứa, thiết bị và giờ rảnh chung của nhóm                   |
| 6   | Chat nhóm và thông báo realtime | Chat nhóm, thông báo lời mời, kết quả duyệt phòng (WebSocket)                    |

**Mức nâng cao (làm khi hoàn thành mức bắt buộc)**

| #   | Chức năng                             | Nội dung chính                                                                                                    |
| --- | ------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| 7   | Check-in QR và điểm uy tín            | Check-in bằng mã QR, tự hủy đặt phòng sau 15 phút nếu không đến, vắng mặt thì giảm điểm uy tín                    |
| 8   | Trợ lý AI (LLM)                       | Giải thích vì sao hai bạn hợp nhau, tóm tắt chat nhóm thành checklist buổi học; có phương án dự phòng khi API lỗi |
| 9   | Nhắc lịch                             | Nhắc buổi học và lịch đặt phòng qua email, hiển thị lịch tuần                                                     |
| 10  | Dashboard thống kê                    | Tỷ lệ lấp đầy phòng, giờ cao điểm, phòng bị bỏ trống (cho MANAGER)                                                |
| 11  | Ghép nhiều nhóm nhỏ vào một phòng lớn | Tối ưu việc dùng phòng khi nhiều nhóm có giờ trùng nhau                                                           |

Match score là tổng có trọng số của các tiêu chí, ví dụ: môn học chung, giờ rảnh chung, mục tiêu học tập, trình độ. Trọng số chỉ là giá trị ban đầu, nhóm sẽ tinh chỉnh khi thử với dữ liệu mẫu.

## 4. Công nghệ

| Thành phần       | Công nghệ                                                            |
| ---------------- | -------------------------------------------------------------------- |
| Ngôn ngữ         | Java 21 (LTS)                                                        |
| Framework        | Spring Boot (khởi tạo bằng start.spring.io, quản lý bằng Maven)      |
| Web              | Spring MVC và Thymeleaf; JavaScript cho phần realtime                |
| Dữ liệu          | Spring Data JPA và MySQL 8                                           |
| Bảo mật          | Spring Security                                                      |
| Realtime         | Spring WebSocket (STOMP)                                             |
| Email            | Spring Mail                                                          |
| Kiểm thử         | JUnit 5, MockMvc, Spring Security Test                               |
| Quản lý mã nguồn | Git và GitHub: mỗi người một nhánh, tạo pull request, merge vào main |

## 5. Thiết kế hướng đối tượng

- **Kiến trúc nhiều lớp:** Controller, Service, Repository, Entity; dùng DTO để truyền dữ liệu giữa Controller và giao diện.
- **Strategy:** mỗi tiêu chí tính điểm là một lớp cài đặt chung một interface; thêm hoặc đổi tiêu chí không phải sửa code cũ.
- **State:** vòng đời đặt phòng gồm PENDING, APPROVED, CHECKED_IN, NO_SHOW, CANCELLED; mỗi trạng thái quy định các hành động hợp lệ.
- **Observer:** phát sự kiện bằng ApplicationEventPublisher khi có lời mời, kết quả duyệt phòng hoặc nhắc lịch; các bộ lắng nghe gửi thông báo realtime và email.
- **Dependency Injection và SOLID:** các lớp phụ thuộc vào interface, Spring cung cấp đối tượng cần dùng.
- **Tài liệu thiết kế:** use case, class diagram, ERD và sequence diagram cho luồng đặt phòng.

## 6. Bảo mật

- **Mật khẩu:** băm bằng BCrypt qua PasswordEncoder; không lưu và không ghi log mật khẩu thô.
- **Xác thực đầy đủ:** mọi trang và API đều yêu cầu đăng nhập, chỉ các trang công khai được liệt kê rõ (đăng nhập, đăng ký, tài nguyên tĩnh); mặc định từ chối.
- **Phân quyền hai tầng:** theo đường dẫn và role, và theo từng phương thức bằng @PreAuthorize.
- **Kiểm tra quyền sở hữu dữ liệu:** sinh viên chỉ xem, sửa, hủy dữ liệu của mình hoặc của nhóm mình, kể cả khi tự đổi id trên đường dẫn.
- **WebSocket:** kênh chat và thông báo cũng yêu cầu xác thực; chỉ thành viên nhóm mới nhận tin nhắn của nhóm đó.
- **Chống tấn công phổ biến:** CSRF (token), XSS (Thymeleaf tự escape nội dung), SQL injection (truy vấn có tham số, không nối chuỗi SQL).
- **Dữ liệu đầu vào:** kiểm tra bằng Bean Validation; dùng DTO, không đưa entity trực tiếp ra giao diện.
- **Phiên làm việc và lỗi:** cookie phiên có HttpOnly và SameSite; thông báo lỗi không lộ chi tiết nội bộ.
- **Khóa bí mật:** mật khẩu cơ sở dữ liệu, API key của LLM, cấu hình email để trong biến môi trường, không đưa lên GitHub.
- **Quyền riêng tư:** không hiển thị email, số điện thoại của bạn học cho đến khi hai bên chấp nhận kết nối.

## 7. Tổ chức dữ liệu và tối ưu truy vấn

- **Thiết kế CSDL:** chuẩn hóa đến dạng chuẩn 3, có khóa ngoại và ràng buộc UNIQUE, NOT NULL; vẽ ERD.
- **Index:** tạo cho các cột hay tìm kiếm và lọc, như email, môn học, phòng cùng thời gian bắt đầu.
- **Tránh truy vấn thừa:** dùng fetch LAZY mặc định; tránh lỗi N+1 query bằng JOIN FETCH hoặc @EntityGraph.
- **Phân trang và chỉ lấy cột cần:** dùng Pageable cho mọi danh sách; truy vấn thống kê trả thẳng DTO.
- **Chống đặt trùng phòng:** kiểm tra khoảng thời gian chồng lấn trong một transaction, kết hợp @Version (khóa lạc quan) hoặc ràng buộc ở CSDL để hai người đặt cùng lúc không bị trùng.
- **Quản lý thay đổi schema:** dùng Flyway (đề xuất, nhóm xác nhận).
- **Đo hiệu năng:** bật log SQL ở môi trường phát triển để đếm số câu truy vấn mỗi request, dùng EXPLAIN cho truy vấn nặng, nạp dữ liệu mẫu lớn (vài nghìn sinh viên) để thử.

## 8. Kiểm thử

- **Repository:** @DataJpaTest cho các truy vấn tự viết (tìm bạn, kiểm tra trùng phòng).
- **Service:** unit test cho thuật toán match score, tìm giờ rảnh chung và các trạng thái đặt phòng.
- **Bảo mật:** MockMvc và Spring Security Test kiểm tra từng endpoint: chưa đăng nhập bị chặn, sai role bị từ chối, truy cập dữ liệu của người khác bị từ chối.

## 9. Phân công (đề xuất, nhóm chốt lại)

| Module                 | Nội dung                                                        | Phụ trách |
| ---------------------- | --------------------------------------------------------------- | --------- |
| 1. Nền tảng và Bảo mật | Khung dự án, Spring Security, tài khoản, hồ sơ                  | Khoa      |
| 2. Matching            | Tìm bạn, match score, giờ rảnh chung                            | Khương    |
| 3. Nhóm và Realtime    | Nhóm học, lời mời, chat, thông báo                              | Huy       |
| 4. Phòng và Đặt phòng  | Quản lý phòng, đặt và hủy, chống trùng, duyệt, check-in, tự hủy | Lịch      |
| 5. Gợi ý và Thống kê   | Gợi ý phòng, dashboard, trợ lý AI, nhắc lịch                    | Khang     |

## 10. Các mốc thực hiện

1. Dựng khung dự án, cơ sở dữ liệu, đăng nhập và phân quyền.
2. Hồ sơ, matching, nhóm học, phòng và đặt phòng chống trùng.
3. Gợi ý phòng, chat và thông báo realtime, rà soát bảo mật, tối ưu truy vấn.
4. Chức năng nâng cao, kiểm thử, hoàn thiện báo cáo và demo.

## 11. Kết quả mong đợi

- Ứng dụng web chạy được với dữ liệu mẫu thuyết phục.
- Match score và gợi ý phòng cho kết quả đúng và giải thích được.
- Không xảy ra đặt trùng phòng, kể cả khi hai người đặt cùng lúc.
- Mọi endpoint đều có kiểm thử xác thực và phân quyền.
- Mã nguồn được quản lý trên GitHub theo nhánh riêng, pull request và review.
