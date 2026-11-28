# StudyMate: quy ước cho AI agent và thành viên nhóm

## Dự án

Ứng dụng web ghép nhóm học tập và đặt phòng học (đồ án môn Phát triển phần mềm hướng đối tượng).
Công nghệ: Java 21, Spring Boot 4.1.x, Maven, Spring MVC + Thymeleaf, Spring Data JPA (MySQL 8), Spring Security, Spring WebSocket, Flyway.

## Cấu trúc package (theo module)

- Gốc: `com.studymate`. Các module: `common`, `user`, `matching`, `group`, `chat`, `room`, `insight`.
- Mỗi module có các package con: `controller`, `service`, `repository`, `entity`, `dto`.
- Giao diện ở `src/main/resources/templates/<module>/`, tài nguyên tĩnh ở `static/`.
- Chỉ sửa trong module được giao. Không tự sửa file dùng chung (`pom.xml`, `SecurityConfig`, `application.properties`) khi chưa được đồng ý.

## Quy tắc code

- Controller mỏng: nhận request, validate, gọi Service, trả View. Logic nghiệp vụ ở Service, truy vấn ở Repository.
- Dùng DTO giữa Controller và giao diện, không đưa Entity ra View.
- Dependency Injection qua constructor.
- Match score dùng Strategy (mỗi tiêu chí một lớp); vòng đời đặt phòng dùng State; thông báo dùng Observer (`ApplicationEventPublisher`).
- Đặt tên: class PascalCase, method và biến camelCase, bảng snake_case số nhiều (`users`, `rooms`). Tên trong code bằng tiếng Anh, comment ngắn bằng tiếng Việt cho đoạn khó.

## Bảo mật (bắt buộc)

- Mật khẩu băm bằng BCrypt qua `PasswordEncoder`. Không ghi log mật khẩu hay token.
- Mọi endpoint mặc định yêu cầu đăng nhập; trang công khai chỉ được liệt kê rõ trong `SecurityConfig`.
- Phân quyền theo role (STUDENT, MANAGER, ADMIN) và dùng `@PreAuthorize` ở service hoặc method.
- Luôn kiểm tra quyền sở hữu dữ liệu: người dùng chỉ thao tác với dữ liệu của mình hoặc nhóm mình, kể cả khi đổi id trên URL.
- Truy vấn có tham số, không nối chuỗi SQL. Validate đầu vào bằng Bean Validation. Giữ CSRF bật. Trong Thymeleaf dùng `th:text`, không dùng `th:utext` với dữ liệu người dùng nhập.
- Không commit mật khẩu hay API key. Bí mật để trong biến môi trường hoặc `application-local.properties` (đã nằm trong `.gitignore`).

## Dữ liệu và truy vấn

- Thay đổi schema bằng Flyway, file đặt tên `V<yyyyMMddHHmm>__mo_ta.sql` trong `src/main/resources/db/migration`. Không sửa file migration đã merge.
- Quan hệ JPA dùng LAZY; tránh N+1 bằng JOIN FETCH hoặc `@EntityGraph`; danh sách dùng `Pageable`; truy vấn thống kê trả thẳng DTO.
- Đặt phòng: kiểm tra trùng giờ trong transaction và dùng `@Version`.
- Thêm index cho các cột hay tìm kiếm hoặc lọc.

## Kiểm thử

- Query tự viết: `@DataJpaTest`. Thuật toán: unit test. Phân quyền: MockMvc và Spring Security Test (chưa đăng nhập bị chặn, sai role bị từ chối, truy cập dữ liệu người khác bị từ chối).
- Chỉ chạy `.\mvnw.cmd test` khi sửa phần backend hoặc dữ liệu (`controller`, `service`, `repository`, `entity`, `config`, `db/migration`, `pom.xml`); khi chỉ sửa giao diện/UI-UX (Thymeleaf HTML, CSS, JS tĩnh, tài liệu) thì không chạy `mvnw`.

## Git

- Không commit trực tiếp vào `main`. Mỗi việc một nhánh `feature/<module>-<mo-ta>`, tạo Pull Request vào `main`, cần ít nhất 1 người duyệt.
- Commit message: `feat(module): ...`, `fix(module): ...`, `test: ...`, `docs: ...`.
- Chạy `.\mvnw.cmd test` trước khi tạo PR.

## Cách làm việc của agent

- Trước khi sửa nhiều file, nêu kế hoạch ngắn và chờ người dùng đồng ý.
- Chỉ sửa file trong phạm vi được yêu cầu; không tự đổi `pom.xml`, cấu hình bảo mật hay migration cũ.
- Sau khi sửa, giải thích ngắn gọn thay đổi và cách kiểm tra.
- Không hard-code khóa bí mật hay mật khẩu.

## Tài liệu tham chiếu

- docs/PROPOSAL.md: chức năng, vai trò, bảo mật, dữ liệu (đọc khi cần làm rõ yêu cầu).
- docs/PHAN-CONG.md: thứ tự và phạm vi từng module (chỉ đọc phần module được giao).
- docs/DESIGN.md: hệ thống thiết kế giao diện (bắt buộc đọc khi làm template hoặc CSS).
- Chỉ đọc các file này khi việc đang làm liên quan, không đọc hết mỗi lần.
