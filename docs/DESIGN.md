# StudyMate — Hệ thống thiết kế giao diện

> **Quy ước bắt buộc cho cả nhóm:** Mọi màu trong CSS đều lấy từ token (biến CSS). Không viết mã hex trực tiếp trong file CSS của module.

---

## 1. Định hướng thiết kế

**"Bờ biển và giấy thư viện"** — chuyên nghiệp, hiện đại vừa phải, dịu và tập trung.

StudyMate phục vụ sinh viên Việt Nam đang học nhóm thực sự, không phải người dùng SaaS trả tiền tháng. Thế giới của họ là thời khóa biểu giấy, phiếu mượn thư viện, bảng phân công trên Google Docs và tin nhắn Zalo. Giao diện cần thân thuộc với ngữ cảnh đó — đủ chuyên nghiệp để dùng nghiêm túc, đủ dịu để không tạo áp lực.

Khác biệt chủ ý so với giao diện AI thông thường: không dùng xanh SaaS `#2563EB`, không gradient, không card bo tròn tràn lan, không số liệu giả "10,000+ users". Thứ bậc thông tin do đường kẻ và lưới tạo ra — không phải bóng đổ. Màu teal/aqua xám gợi nước biển mùa sáng, nền giấy thư viện hơi xanh; hai hình ảnh đó kết hợp đủ riêng biệt mà không phô trương.

Trên điện thoại (ưu tiên thiết kế trước cho STUDENT): chữ tối thiểu 16px, vùng bấm tối thiểu 44px, không phụ thuộc hover. Trên máy tính cho MANAGER/ADMIN: mật độ thông tin cao hơn, bảng và biểu đồ được ưu tiên.

---

## 2. Design Tokens

### 2.1 Phông chữ

**Be Vietnam Pro** (giao diện, tiêu đề, nhãn) — subset `vietnamese` xác nhận có trên Google Fonts (Subsets: latin, latin-ext, vietnamese; nhà thiết kế người Việt: Lâm Bảo, Tony Le, ViệtAnh Nguyễn). Phổ biến hạng 105.

**Noto Sans** làm phông dự phòng thứ hai — cũng có subset vietnamese, bảo đảm hiển thị đúng dấu khi Be Vietnam Pro chưa tải xong.

Không dùng serif cho nội dung. Không dùng @import — nạp bằng thẻ `<link>`.

```html
<!-- Đặt trong <head>, trước mọi file CSS -->
<link rel="preconnect" href="https://fonts.googleapis.com" />
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
<link
  href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700&family=Noto+Sans:wght@400;600&display=swap"
  rel="stylesheet"
/>
```

Stack dự phòng: `'Be Vietnam Pro', 'Noto Sans', system-ui, -apple-system, sans-serif`

### 2.2 Thang cỡ chữ (Type scale)

| Token             | Giá trị | Dùng cho                          |
| ----------------- | ------- | --------------------------------- |
| `--text-xs`       | 12px    | Nhãn phụ, timestamp, badge text   |
| `--text-sm`       | 14px    | Nhãn form, chú thích, metadata    |
| `--text-base`     | 16px    | Nội dung chính (tối thiểu mobile) |
| `--text-md`       | 18px    | Thân bài quan trọng, card title   |
| `--text-lg`       | 20px    | Tiêu đề mục, section heading      |
| `--text-xl`       | 24px    | Tiêu đề trang (mobile)            |
| `--text-2xl`      | 32px    | Tiêu đề trang (desktop)           |
| `--leading-tight` | 1.25    | Tiêu đề ngắn                      |
| `--leading-base`  | 1.5     | Nội dung đọc                      |
| `--leading-loose` | 1.75    | Nội dung dài trong trang chi tiết |

### 2.3 Tokens CSS — Bản sáng & tối

```css
/* ============================================================
   tokens.css  —  SOURCE OF TRUTH
   Mọi màu dùng trong module đều tham chiếu biến này.
   KHÔNG viết hex trực tiếp trong file CSS của module.
   ============================================================ */

:root {
  color-scheme: light;

  /* — Màu nền — */
  --bg: #eef6f7; /* Nền trang chính */
  --surface: #d6ecf0; /* Nền phụ: sidebar tóm tắt, header di động */
  --surface-raised: #ffffff; /* Card, modal, dropdown */
  --overlay: rgba(11, 93, 122, 0.08); /* Nền hover nhẹ */

  /* — Chữ — */
  --text: #0f2a33; /* Chữ chính                 13.69:1 trên --bg ✓ */
  --text-muted: #4a6670; /* Chữ phụ, placeholder       5.59:1 trên --bg ✓ */
  --text-inverse: #eef6f7; /* Chữ trên nút màu đậm */

  /* — Màu chính — */
  --primary: #0b5d7a; /* Nút chính, link, focus     6.70:1 trên --bg ✓ */
  --primary-hover: #094d66;
  --on-primary: #eef6f7; /* Chữ trên nút chính         6.70:1 trên --primary ✓ */

  /* — Màu nhấn ấm — */
  --accent: #c2410c; /* Hành động quan trọng       4.72:1 trên --bg ✓ */
  --accent-hover: #a33509;
  --on-accent: #ffffff; /* Chữ trên nút accent        5.18:1 trên --accent ✓ */

  /* — Màu nguy hiểm — */
  --danger: #b91c1c;
  --on-danger: #ffffff;

  /* — Đường kẻ & viền — */
  --border: #4a7a87; /* Viền ô nhập, đường phân cách  4.33:1 trên --bg ✓ */
  --border-light: #8fb8c4; /* Đường phân cách nhẹ, không mang thông tin */
  --border-focus: #0b5d7a; /* Viền focus (outline)       6.70:1 trên --bg ✓ */

  /* — Phông chữ — */
  --font-sans:
    "Be Vietnam Pro", "Noto Sans", system-ui, -apple-system, sans-serif;
  --text-xs: 12px;
  --text-sm: 14px;
  --text-base: 16px;
  --text-md: 18px;
  --text-lg: 20px;
  --text-xl: 24px;
  --text-2xl: 32px;
  --leading-tight: 1.25;
  --leading-base: 1.5;
  --leading-loose: 1.75;
  --weight-regular: 400;
  --weight-medium: 500;
  --weight-semibold: 600;
  --weight-bold: 700;

  /* — Khoảng cách (8dp rhythm) — */
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-8: 32px;
  --space-10: 40px;
  --space-12: 48px;
  --space-16: 64px;

  /* — Bo góc (nhỏ, có chủ đích) — */
  --radius-sm: 3px; /* Badge, tag */
  --radius-md: 6px; /* Nút, ô nhập */
  --radius-lg: 10px; /* Card, modal */
  --radius-full: 9999px; /* Chỉ cho pill badge */

  /* — Bóng đổ (rất tiết chế) — */
  --shadow-sm: 0 1px 3px rgba(11, 93, 122, 0.08);
  --shadow-md: 0 4px 12px rgba(11, 93, 122, 0.1);

  /* — Độ dày đường kẻ — */
  --border-width: 1px;
  --border-width-thick: 2px; /* Focus outline */

  /* — Thời lượng chuyển động — */
  --duration-fast: 120ms;
  --duration-base: 200ms;
  --duration-slow: 350ms;
  --easing-std: cubic-bezier(0.4, 0, 0.2, 1);
  --easing-out: cubic-bezier(0, 0, 0.2, 1);

  /* — Kích thước khung — */
  --nav-bottom-height: calc(
    56px + env(safe-area-inset-bottom)
  ); /* Thanh tab dưới (STUDENT, điện thoại) */
  --content-max-student: 960px; /* Chiều rộng nội dung tối đa, khung STUDENT trên máy tính */

  /* — Trạng thái đặt phòng (light) — */
  /* PENDING: vàng hổ phách */
  --status-pending-bg: #fef3c7;
  --status-pending-text: #7a5500; /* 6.03:1 ✓ */
  --status-pending-border: #d97706;

  /* APPROVED: xanh lục-biển thương hiệu (nền nhạt + viền) */
  --status-approved-bg: #ccf0eb;
  --status-approved-text: #0a5248; /* 7.45:1 ✓ */
  --status-approved-border: #0d7a6c;

  /* CHECKED_IN: tô đặc theo màu chính (khác APPROVED bằng kiểu, không chỉ bằng màu) */
  --status-checkin-bg: var(--primary);
  --status-checkin-text: var(--on-primary); /* sáng 6.70:1, tối 7.23:1 ✓ */
  --status-checkin-border: var(--primary);

  /* NO_SHOW: san hô (tránh tím) */
  --status-noshow-bg: #fddfd6;
  --status-noshow-text: #7a1f00; /* 8.25:1 ✓ */
  --status-noshow-border: #c2410c;

  /* CANCELLED: xám slate */
  --status-cancelled-bg: #e8edef;
  --status-cancelled-text: #3b464c; /* 8.21:1 ✓ */
  --status-cancelled-border: #6b7c82;
}

/* ============================================================
   Chế độ tối — data-theme="dark"
   ============================================================ */
[data-theme="dark"] {
  color-scheme: dark;

  --bg: #0e1b20;
  --surface: #14262d;
  --surface-raised: #1c3540;
  --overlay: rgba(91, 184, 208, 0.08);

  --text: #e6f1f3; /* 15.25:1 trên --bg ✓ */
  --text-muted: #9db5bc; /*  8.17:1 trên --bg ✓ */
  --text-inverse: #08222b;

  --primary: #5bb8d0; /* Sáng hơn bản sáng để đạt tương phản  7.70:1 trên --bg ✓ */
  --primary-hover: #74c5da;
  --on-primary: #08222b; /* 7.23:1 trên --primary ✓ */

  --accent: #f08a5d; /* 7.10:1 trên --bg ✓ */
  --accent-hover: #f5a07c;
  --on-accent: #1a0f0a; /* 7.61:1 trên --accent ✓ */

  --danger: #f87171;
  --on-danger: #1a0000;

  --border: #5090a0; /* 4.88:1 trên --bg ✓  4.34:1 trên --surface ✓  3.58:1 trên --surface-raised ✓ */
  --border-light: #2e4e58;
  --border-focus: #5bb8d0; /* 7.70:1 trên --bg ✓ */

  /* Trạng thái đặt phòng (dark) */
  /* PENDING: vàng hổ phách */
  --status-pending-bg: #2d1b00;
  --status-pending-text: #fde68a; /* 13.29:1 ✓ */
  --status-pending-border: #d97706;

  /* APPROVED: xanh lục-biển */
  --status-approved-bg: #052a26;
  --status-approved-text: #81d4ca; /*  8.94:1 ✓ */
  --status-approved-border: #34b8ae;

  /* CHECKED_IN: tô đặc theo màu chính (đã tự đổi theo chế độ vì dùng --primary) */
  --status-checkin-bg: var(--primary);
  --status-checkin-text: var(--on-primary); /* 7.23:1 ✓ */
  --status-checkin-border: var(--primary);

  /* NO_SHOW: san hô */
  --status-noshow-bg: #2d0e00;
  --status-noshow-text: #fdba9e; /* 10.81:1 ✓ */
  --status-noshow-border: #f08a5d;

  /* CANCELLED: xám slate */
  --status-cancelled-bg: #1a2326;
  --status-cancelled-text: #a8b8bc; /*  7.81:1 ✓ */
  --status-cancelled-border: #4a6670;
}

/* ============================================================
   Chế độ Hệ thống: được xử lý hoàn toàn bằng JavaScript
   (script trong <head> đọc localStorage và matchMedia, rồi
   gán data-theme="light" hoặc "dark" trên <html> trước khi
   trang vẽ).  Khi tắt JavaScript, trang hiển thị chế độ sáng.
   KHÔNG dùng @media prefers-color-scheme ở đây để tránh xung
   đột với data-theme do JS quản lý.
   ============================================================ */
```

### 2.4 Bảng tương phản tổng hợp

| Cặp màu                            | Chế độ | Tỉ lệ   | Kết quả        |
| ---------------------------------- | ------ | ------- | -------------- |
| `--text` trên `--bg`               | Sáng   | 13.69:1 | ✅ PASS AA     |
| `--text` trên `--surface`          | Sáng   | 12.23:1 | ✅ PASS AA     |
| `--text-muted` trên `--bg`         | Sáng   | 5.59:1  | ✅ PASS AA     |
| `--text-muted` trên `--surface`    | Sáng   | 5.00:1  | ✅ PASS AA     |
| `--primary` trên `--bg`            | Sáng   | 6.70:1  | ✅ PASS AA     |
| `--on-primary` trên `--primary`    | Sáng   | 6.70:1  | ✅ PASS AA     |
| `--accent` trên `--bg`             | Sáng   | 4.72:1  | ✅ PASS AA     |
| `--on-accent` trên `--accent`      | Sáng   | 5.18:1  | ✅ PASS AA     |
| `--border` trên `--bg`             | Sáng   | 4.33:1  | ✅ PASS UI 3:1 |
| `--border` trên `--surface`        | Sáng   | 3.87:1  | ✅ PASS UI 3:1 |
| `--text` trên `--bg`               | Tối    | 15.25:1 | ✅ PASS AA     |
| `--text` trên `--surface`          | Tối    | 13.56:1 | ✅ PASS AA     |
| `--text` trên `--surface-raised`   | Tối    | 11.17:1 | ✅ PASS AA     |
| `--text-muted` trên `--bg`         | Tối    | 8.17:1  | ✅ PASS AA     |
| `--text-muted` trên `--surface`    | Tối    | 7.26:1  | ✅ PASS AA     |
| `--primary` trên `--bg`            | Tối    | 7.70:1  | ✅ PASS AA     |
| `--on-primary` trên `--primary`    | Tối    | 7.23:1  | ✅ PASS AA     |
| `--accent` trên `--bg`             | Tối    | 7.10:1  | ✅ PASS AA     |
| `--on-accent` trên `--accent`      | Tối    | 7.61:1  | ✅ PASS AA     |
| `--border` trên `--bg`             | Tối    | 4.88:1  | ✅ PASS UI 3:1 |
| `--border` trên `--surface`        | Tối    | 4.34:1  | ✅ PASS UI 3:1 |
| `--border` trên `--surface-raised` | Tối    | 3.58:1  | ✅ PASS UI 3:1 |

**Trạng thái đặt phòng:**

| Trạng thái | Chế độ | Màu chữ / nền                         | Tỉ lệ chữ | Kết quả    |
| ---------- | ------ | ------------------------------------- | --------- | ---------- |
| PENDING    | Sáng   | `#7A5500` / `#FEF3C7`                 | 6.03:1    | ✅ PASS AA |
| APPROVED   | Sáng   | `#0A5248` / `#CCF0EB`                 | 7.45:1    | ✅ PASS AA |
| CHECKED_IN | Sáng   | `--on-primary` / `--primary` (tô đặc) | 6.70:1    | ✅ PASS AA |
| NO_SHOW    | Sáng   | `#7A1F00` / `#FDDFD6`                 | 8.25:1    | ✅ PASS AA |
| CANCELLED  | Sáng   | `#3B464C` / `#E8EDEF`                 | 8.21:1    | ✅ PASS AA |
| PENDING    | Tối    | `#FDE68A` / `#2D1B00`                 | 13.29:1   | ✅ PASS AA |
| APPROVED   | Tối    | `#81D4CA` / `#052A26`                 | 8.94:1    | ✅ PASS AA |
| CHECKED_IN | Tối    | `--on-primary` / `--primary` (tô đặc) | 7.23:1    | ✅ PASS AA |
| NO_SHOW    | Tối    | `#FDBA9E` / `#2D0E00`                 | 10.81:1   | ✅ PASS AA |
| CANCELLED  | Tối    | `#A8B8BC` / `#1A2326`                 | 7.81:1    | ✅ PASS AA |

> **Lưu ý:** Trạng thái KHÔNG phân biệt chỉ bằng màu. Badge luôn kèm nhãn chữ: "Chờ duyệt", "Đã duyệt", "Đã vào", "Vắng mặt", "Đã hủy". Riêng CHECKED_IN được tô đặc (nền màu chính, chữ sáng) còn APPROVED dùng nền nhạt + viền, để hai trạng thái gần màu vẫn khác nhau về kiểu.

---

## 3. Khung bố cục theo role

### 3a. Khung STUDENT — Điện thoại (Mobile First)

#### Yếu tố đặc trưng StudyMate

Ba yếu tố lấy từ thế giới sinh viên, áp dụng có chủ đích:

**① Bảng chấm điểm phù hợp (Ledger)**
Không hiển thị một số điểm duy nhất, cũng không dùng thanh tiến trình. Mỗi tiêu chí là một dòng như mục lục sách: nhãn bên trái, dải chấm dẫn ở giữa, điểm "đạt/tối đa" bên phải. Tổng điểm ở dòng cuối. Điểm tối đa của từng tiêu chí chính là trọng số (tổng 100), nên cộng lại phải khớp.

```
Phù hợp với Nguyễn Văn A
Môn học chung ........... 32/40
Giờ rảnh chung .......... 24/30
Mục tiêu học ............ 14/20
Trình độ ................. 8/10
Tổng ..................... 78/100
```

CSS và HTML của Ledger nằm ở mục "Chữ ký hình ảnh của khung sinh viên" bên dưới.

**② Lưới giờ rảnh chung (Availability Grid)**
Hiển thị giờ rảnh của cả nhóm như bảng thời khóa biểu giấy: hàng = khung giờ, cột = thành viên. Ô giao thoa có người rảnh tô nhẹ `--surface`; ô tất cả rảnh tô `--primary` với độ mờ 20%. Không dùng màu gradient.

```
        Khoa  Huy  Lan  Minh
08–09h  [ ]   [x]  [ ]  [x]
09–10h  [x]   [x]  [x]  [x]  <- giờ chung
10–11h  [ ]   [ ]  [x]  [ ]
```

Ô `44×44px` tối thiểu, nhãn rõ, không dùng màu làm thông tin duy nhất (ô rảnh có dấu `x` và có chú thích bên dưới lưới).

**③ Bảng chọn giờ đặt phòng kiểu "tem phiếu" (Time Slot Picker)**
Các khung giờ hiển thị như lưới ô nhỏ. Ô còn trống: viền `--border`, nền `--surface-raised`. Ô đã đặt: nền `--status-pending-bg`, chữ "Đã đặt". Ô được chọn: viền `--border-width-thick` màu `--primary`. Hành động bấm rõ ràng, không dùng hover làm phản hồi duy nhất.

```
     07:00  08:00  09:00  10:00  11:00
P1   [  ]   [x]    [  ]   [Đặt]  [  ]
P2   [  ]   [  ]   [  ]   [  ]   [  ]
```

(`[x]` là ô đã được đặt, `[Đặt]` là ô đang chọn.)

---

#### Bố cục điện thoại — 5 màn hình chính

Quy ước ký hiệu trong sơ đồ: `[tên]` là một icon hoặc nút (ví dụ `[chuông]` là icon chuông thông báo). Thanh tab dưới gồm 5 mục, mỗi mục có icon ở trên và nhãn chữ ở dưới.

**Màn hình: Tìm bạn**

```
+---------------------------+
| Tìm bạn học      [chuông] |  <- Thanh trên: 56px
+---------------------------+
| [Môn] [Giờ] [Khoa] ...    |  <- Filter chip, cuộn ngang
+---------------------------+
| +-----------------------+ |
| | Nguyễn Văn A          | |
| | CNTT - K22            | |
| | Phù hợp: 78/100       | |
| | - - - - - - - - - - - | |
| | [Xem hồ sơ] [Kết nối] | |  <- Hành động trong tầm ngón cái
| +-----------------------+ |
| +-----------------------+ |
| |  ...                  | |
| +-----------------------+ |
+---------------------------+
| [Tìm bạn][Nhóm][Đặt phòng][Chat][Hồ sơ] |  <- Tab dưới: --nav-bottom-height
+---------------------------+
```

**Màn hình: Nhóm**

```
+---------------------------+
| Nhóm học         [chuông] |  <- Thanh trên: 56px
+---------------------------+
| [Nhóm của tôi] [Khám phá] |  <- Tab ngang
+---------------------------+
| Nhóm Giải tích 2          |
| 4 thành viên . Thứ 3, 4   |
| [Lịch chung] [Đặt phòng]  |  <- Hành động gần cuối thẻ
+---------------------------+
|              [+ Tạo nhóm] |  <- Nút chính, tầm ngón cái phải
+---------------------------+
| [Tìm bạn][Nhóm][Đặt phòng][Chat][Hồ sơ] |
+---------------------------+
```

**Màn hình: Đặt phòng**

```
+---------------------------+
| Đặt phòng        [chuông] |  <- Thanh trên: 56px
+---------------------------+
| T2   T3   [T4]   T5  ...  |  <- Chọn ngày ([T4] = đang chọn)
+---------------------------+
|      Phòng A101  A102     |  <- Lưới giờ trống
|  07h  [      ]  [      ]  |
|  08h  [ Đặt ]   [      ]  |  <- Ô bấm tối thiểu 44px
|  09h  [      ]  [ Đặt ]   |
+---------------------------+
| Phòng đang chọn: A101     |
| 09:00-11:00 . 2 tiếng     |
|     [Xác nhận đặt]        |  <- Nút accent, full-width, tầm ngón cái
+---------------------------+
| [Tìm bạn][Nhóm][Đặt phòng][Chat][Hồ sơ] |
+---------------------------+
```

**Màn hình: Chat**

```
+---------------------------+
| [quay lại] Nhóm Giải tích 2 [chuông] |  <- Chỉ màn con (Chat trong nhóm) mới có nút quay lại
+---------------------------+
| (Tin nhắn cuộn được)      |
|                           |
|  Huy: Mình rảnh 9h        |
|  Lan: OK mình cũng rảnh   |
|  Khoa: Đặt phòng chưa?    |
|                           |
| [thông báo: phòng A101    |
|  đã được duyệt]           |
+---------------------------+
| [Nhập tin nhắn...] [Gửi]  |  <- Cố định đáy, trên safe area
+---------------------------+
  (không có tab dưới - ẩn để nhường chỗ cho ô nhập)
```

Ô nhập cố định `position: sticky; bottom: 0; padding-bottom: env(safe-area-inset-bottom)`. Khi bàn phím hiện, `height: 100dvh` đảm bảo không bị che.

**Thanh tab dưới bị ẩn ở màn hình Chat trong nhóm** (`.layout-student--chat .nav-bottom { display: none }`) để nhường toàn bộ chiều cao cho vùng tin nhắn và ô nhập. Lý do: trên iOS/Android, khi bàn phím hiện, bố cục `100dvh` đã co lại đúng — thanh tab nếu còn sẽ chồng lên ô nhập. Người dùng thoát Chat qua nút quay lại `[quay lại]` trên thanh trên. Màn danh sách Chat (các cuộc trò chuyện) vẫn hiện thanh tab và không có nút quay lại, vì đó là màn gốc của tab.

**Màn hình: Hồ sơ**

```
+---------------------------+
| Hồ sơ            [chuông] |  <- Thanh trên: 56px
+---------------------------+
| [Ảnh] Nguyễn Văn A        |
|       CNTT . K22          |
+---------------------------+
| Môn đang học              |
| Giờ rảnh                  |
| Mục tiêu học              |
| Điểm uy tín: 92           |
+---------------------------+
| Chủ đề giao diện          |
| [Sáng] [Tối] [Hệ thống]   |  <- Nhóm 3 nút, xem mục 9
+---------------------------+
| [Chỉnh sửa hồ sơ]         |
| [Đăng xuất]               |
+---------------------------+
| [Tìm bạn][Nhóm][Đặt phòng][Chat][Hồ sơ] |
+---------------------------+
```

#### Thanh tab dưới (Bottom Navigation)

```css
.nav-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: var(
    --nav-bottom-height
  ); /* = 56px + vùng an toàn, khai báo trong tokens.css */
  padding-bottom: env(safe-area-inset-bottom);
  background: var(--surface-raised);
  border-top: var(--border-width) solid var(--border-light);
  display: flex;
  align-items: flex-start;
  padding-top: 4px;
}

.nav-bottom__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  min-height: 44px; /* tối thiểu vùng bấm */
  padding: 6px 4px;
  color: var(--text-muted);
  text-decoration: none;
  font-size: var(--text-xs);
  font-weight: var(--weight-medium);
}

.nav-bottom__item[aria-current="page"] {
  color: var(--primary);
}

.nav-bottom__icon {
  width: 24px;
  height: 24px;
}

/* Từ 768px: chuyển sang thanh nav ngang phía trên, bỏ tab dưới */
@media (min-width: 768px) {
  .nav-bottom {
    display: none;
  }
}

/* Màn Chat trong nhóm: ẩn tab dưới để nhường chỗ cho ô nhập */
.layout-student--chat .nav-bottom {
  display: none;
}
```

Nội dung chính của khung STUDENT phải chừa chỗ cho thanh tab dưới, nếu không cuối trang bị che:

```css
.layout-student__main {
  max-width: var(--content-max-student);
  margin-inline: auto;
  padding: var(--space-4);
  padding-bottom: calc(
    var(--nav-bottom-height) + var(--space-4)
  ); /* không bị thanh tab che */
}
.layout-student--chat .layout-student__main {
  padding-bottom: 0;
} /* màn Chat không có tab dưới */

@media (min-width: 768px) {
  .layout-student__main {
    padding-bottom: var(--space-6);
  } /* không còn tab dưới */
}
```

5 mục: **Tìm bạn · Nhóm · Đặt phòng · Chat · Hồ sơ**. Nhãn chữ luôn hiển thị, kèm icon SVG `aria-hidden="true"` vì nhãn đã đủ.

#### Bố cục máy tính (STUDENT, ≥768px)

```
+-------------------------------------------------------------------------------------------------+
| [icon-brand] StudyMate | [  Tìm bạn    Nhóm    Đặt phòng    Chat  ] | [Theme] [chuông] [avatar] | <- Thanh trên: 72px, sticky
+-------------------------------------------------------------------------------------------------+
|                                                                                                 |
|                          [nội dung, max-width: 960px, căn giữa]                                 |
|                                                                                                 |
+-------------------------------------------------------------------------------------------------+
```

Không dùng sidebar. Chiều cao thanh trên 72px, sticky top, nền `--bg`, viền dưới 1px `--border-light`. Nội dung bên trong dùng chung `.container-student` (`max-width: var(--content-max-student)` = 960px, `margin-inline: auto`, `padding-inline: var(--space-4)`) để mép logo và mép nội dung thẳng hàng.
- **Trái:** Biểu tượng thương hiệu nằm trong hộp vuông 36px nền `--primary` bo `--radius-md` chứa icon `icon-brand` màu trắng + chữ "StudyMate" đậm; kế bên (cách khoảng 32px) là thanh ray điều hướng dạng pill `.navbar__rail` (nền `--surface-raised`, viền `--border-light`, bo `--radius-full`, padding 4px).
- **Thanh ray điều hướng (`.navbar__rail`):** CHỈ GỒM CHỮ 4 mục: Tìm bạn, Nhóm, Đặt phòng, Chat (`white-space: nowrap`), KHÔNG icon, KHÔNG xuống dòng. Mục "Phòng" đã được bỏ hoàn toàn khỏi menu sinh viên. Bên dưới các liên kết có vạch trượt mềm `.navbar__indicator` (bo `--radius-full`, nền `--primary`, chữ trắng) được điều khiển trượt động bởi `navbar.js` theo mục active hoặc vị trí hover/focus.
- **Phải:** Cụm chuyển chủ đề giao diện (hiển thị ở ≥1024px), chuông thông báo (44×44px, chỉ icon, không chấm báo khi chưa có số chưa đọc), nút avatar pill (vòng tròn chữ cái đầu viết hoa, họ tên cắt bằng ellipsis tối đa khoảng 160px, ẩn tên dưới 900px, kèm mũi tên `icon-chevron-down`).
- **Menu avatar dropdown:** rộng khoảng 260px, canh mép phải, bo `--radius-lg`, viền `--border-light`, bóng `--shadow-md`. Dùng mẫu nút `aria-expanded` (không `role="menu"`). Thứ tự nội dung: họ tên và huy hiệu vai trò, liên kết "Hồ sơ" (`/profile`), nhóm nhãn "Giao diện" kèm cụm 3 nút theme (hiển thị khi <1024px), vạch ngăn, nút "Đăng xuất" (form POST `th:action="@{/logout}"`).
- **Banner đầu trang (`.home-hero`):** Nền `--surface-raised`, viền `--border-light`, bo `--radius-lg`, phủ hoa văn kẻ ngang thư viện `paper-lines.svg` (28px lặp, độ mờ 10%), huy hiệu vai trò pill, tiêu đề chào và thông điệp học tập ấm áp.
- **Thẻ hành trình học nhóm (`.ticket.journey-card`):** Dạng thẻ phiếu thư viện 2 phần: phần thân chứa hộp icon 40px (nền `--primary` nhạt hoặc `--surface`, icon nét mảnh), tiêu đề và mô tả; phần cuống `.ticket__stub` chứa nhãn "Bắt đầu" kèm mũi tên `icon-caret-right`. Toàn bộ thẻ là liên kết `<a>`, hover nâng nhẹ 2px (`transform: translateY(-2px)`), viền chuyển sang `--primary`. Không dùng số bước giả lập 1, 2, 3, 4.

---

### Chữ ký hình ảnh của khung sinh viên

Đây là phần làm khung sinh viên khác khung quản lý. Chỉ có 4 chi tiết, không thêm. Khung quản lý (`layout-staff`) KHÔNG dùng các chi tiết này, giữ gọn và dày thông tin. Các chi tiết chỉ áp dụng khi `<body>` có class `layout-student`.

**1. Đường kẻ đôi dưới tiêu đề trang** (như dòng kẻ trên phiếu hoặc sổ cái)

```css
.layout-student .page-title {
  padding-bottom: var(--space-3);
  border-bottom: 3px double var(--border-light);
}
```

**2. Thẻ phiếu** cho thẻ bạn học và thẻ nhóm: phần đầu là thông tin, phần cuống là hành động, ngăn bằng nét đứt và hai khuyết nửa tròn hai bên như phiếu thư viện.

```css
.ticket {
  background: var(--surface-raised);
  border: var(--border-width) solid var(--border-light);
  border-radius: var(--radius-md);
}
.ticket__body {
  padding: var(--space-5);
}
.ticket__stub {
  position: relative;
  padding: var(--space-4) var(--space-5);
  border-top: var(--border-width) dashed var(--border);
}
.ticket__stub::before,
.ticket__stub::after {
  content: "";
  position: absolute;
  top: -9px;
  width: 16px;
  height: 16px;
  background: var(--bg);
  border: var(--border-width) solid var(--border-light);
  border-radius: 50%;
}
.ticket__stub::before {
  left: -9px;
  clip-path: inset(0 0 0 50%);
}
.ticket__stub::after {
  right: -9px;
  clip-path: inset(0 50% 0 0);
}
```

Chỉnh vài pixel (`top`, kích thước) sau khi xem thực tế trên điện thoại. Khuyết chỉ là trang trí, thông tin không phụ thuộc vào nó. Không lồng thẻ phiếu trong thẻ phiếu. Thẻ phiếu thay cho `.card` ở khung sinh viên; khung quản lý vẫn dùng `.card` thường.

**3. Dòng sổ chấm điểm (Ledger)** — dùng cho điểm phù hợp (xem ① ở trên).

```css
.ledger {
  list-style: none;
  margin: 0;
  padding: 0;
}
.ledger__row {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  padding: var(--space-2) 0;
}
.ledger__dots {
  flex: 1;
  border-bottom: var(--border-width) dotted var(--border);
  transform: translateY(-4px);
}
.ledger__value {
  font-weight: var(--weight-semibold);
}
.ledger__row--total {
  border-top: var(--border-width) solid var(--border);
  font-weight: var(--weight-bold);
}
```

HTML mỗi dòng:

```html
<li class="ledger__row">
  <span>Môn học chung</span>
  <span class="ledger__dots" aria-hidden="true"></span>
  <span class="ledger__value">32/40</span>
</li>
```

**4. Con số và giờ giấc thẳng hàng** như thời khóa biểu:

```css
.layout-student .time,
.layout-student .ledger__value {
  font-variant-numeric: tabular-nums;
}
```

Cần kiểm tra Be Vietnam Pro có hỗ trợ số cùng độ rộng (tabular) không. Nếu không, đặt `min-width` cho cột giờ và căn phải.

**Không dùng ở đây:** xoay chữ, gradient, icon trang trí, bóng đổ thêm. Khi thêm chi tiết mới, phải kiểm tra ở cả hai chế độ sáng/tối.

---

### 3b. Khung MANAGER & ADMIN

Dùng chung khung sidebar, chỉ khác menu.

```
+--------------+--------------------------------------------+
| [Logo]       | [=] Tiêu đề trang (<h1> duy nhất)  [hồ sơ] | <- Header: 64px
| StudyMate    +--------------------------------------------+
|              |                                            |
| MANAGER menu |   [Bảng / Biểu đồ / Danh sách]             |
|  - QL phòng  |                                            |
|  - Duyệt     |                                            |
|  - Thống kê  |                                            |
|              |                                            |
| ADMIN menu   |                                            |
|  - Tài khoản |                                            |
|  - Nhật ký   |                                            |
|              |                                            |
| [Sáng|Tối    |                                            |
|  |Hệ thống]  |                                            |
| [Đăng xuất]  |   © 2026 StudyMate                         | <- Footer trong wrapper
+--------------+--------------------------------------------+
```

**Menu MANAGER:** Quản lý phòng, Duyệt đặt phòng, Thống kê.

**Menu ADMIN:** Tài khoản, Nhật ký hệ thống, Quản lý phòng (xem).

Sidebar rộng cố định 240px, cao `100dvh` dính (`position: sticky; top: 0;`). Trên màn hình máy tính, bố cục dùng lưới 2 cột: cột 1 là sidebar (240px), cột 2 là khung bọc `.layout-staff__wrapper` (`minmax(0, 1fr)`). Toàn bộ header 64px, nội dung chính `<main>`, và `<footer>` nằm TRONG wrapper này để đảm bảo footer không rơi xuống dưới sidebar.
Trang chỉ có ĐÚNG MỘT thẻ `<h1>` duy nhất nằm ở dải tiêu đề header 64px; các thẻ nội dung bên dưới (như lời chào) không dùng `<h1>`.

Trên màn hình nhỏ (`< 768px`): sidebar ẩn khỏi màn hình và hoạt động như drawer overlay (`is-open`), mở bằng nút menu hamburger `[=]` (icon `icon-list`, `aria-controls="staffSidebar"`, `aria-expanded="false"`), có lớp phủ mờ `.layout-staff__backdrop`, đóng bằng phím Escape hoặc click ra ngoài, được quản lý độc lập bởi `static/js/staff-drawer.js`.

```css
.layout-staff {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  min-height: 100dvh;
}

.sidebar {
  grid-column: 1;
  width: 240px;
  height: 100dvh;
  position: sticky;
  top: 0;
  background: var(--surface);
  border-right: var(--border-width) solid var(--border-light);
  display: flex;
  flex-direction: column;
}

.layout-staff__wrapper {
  grid-column: 2;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 100dvh;
}
```

Bảng dữ liệu dùng mật độ cao (`--space-2`/`--space-3` padding ô), không bo tròn, viền mảnh. Trên màn hình nhỏ bảng chuyển thành danh sách thẻ hoặc cuộn ngang trong container có `overflow-x: auto`.

---

### 3c. Điểm gãy responsive

| Breakpoint | Chiều rộng | Mô tả                                                     |
| ---------- | ---------- | --------------------------------------------------------- |
| `--bp-sm`  | 375px      | Điện thoại nhỏ nhất cần hỗ trợ                            |
| `--bp-md`  | 768px      | Tablet / chuyển nav ngang (STUDENT), sidebar hiện (STAFF) |
| `--bp-lg`  | 1024px     | Desktop nhỏ                                               |
| `--bp-xl`  | 1440px     | Desktop rộng                                              |

**Thymeleaf layout fragments:**

- `templates/layout/student.html` — Khung STUDENT: thanh nav trên + tab dưới (mobile), nav ngang (desktop), script theme, head chung. `<body class="layout-student">`.
- `templates/layout/staff.html` — Khung MANAGER/ADMIN: sidebar + header, script theme, head chung. `<body class="layout-staff">`.
- `templates/layout/auth.html` — Khung AUTH hai cột (Đăng nhập, Đăng ký): từ 900px chia hai cột theo tỉ lệ 60/40 (cột trái 60% ảnh `campus.jpg` tự nhiên với scrim tối nhẹ ở đáy để đọc chữ; cột phải 40% `logo.png` nằm ngoài phía trên form + thẻ form gọn tối đa khoảng 420px căn giữa dọc + theme switcher icon góc phải). Dưới 900px ẩn cột ảnh, form chiếm toàn chiều rộng. Trang lỗi 403/404/500 dùng thẻ giữa trang căn giữa kèm icon lớn. `<body class="layout-auth">`.

> **Lưu ý bảo mật:** Ẩn mục menu trong HTML không thay thế phân quyền. Controller và Service vẫn phải có `@PreAuthorize`. Người dùng sai role bị từ chối ở server, không phải chỉ ẩn nút.

---

## 4. Component

### Form

- Nhãn (`<label>`) luôn hiển thị, không dùng placeholder làm nhãn.
- Thông báo lỗi bằng chữ, dưới ô nhập, màu `--danger`, kèm icon `warning-circle` (có trong danh sách mục 5) `aria-hidden="true"`.
- Dùng đúng loại input cho bàn phím điện thoại:
  - Email: `type="email"`
  - Số: `type="number"` hoặc `inputmode="numeric"`
  - Tìm kiếm: `type="search"`
  - Ngày: `type="date"`, `type="time"`
- Cỡ chữ ô nhập tối thiểu `16px` (tránh iOS tự zoom).
- `font-size: var(--text-base)` trên mọi `<input>`, `<select>`, `<textarea>`.

```html
<div class="form-field">
  <label class="form-field__label" for="subject">Môn học</label>
  <input
    class="form-field__input"
    id="subject"
    type="text"
    aria-describedby="subject-error"
    autocomplete="off"
  />
  <span class="form-field__error" id="subject-error" role="alert" hidden>
    Vui lòng nhập tên môn học.
  </span>
</div>
```

```css
.form-field__input {
  display: block;
  width: 100%;
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-base);
  font-family: var(--font-sans);
  color: var(--text);
  background: var(--surface-raised);
  border: var(--border-width) solid var(--border);
  border-radius: var(--radius-md);
  transition: border-color var(--duration-fast) var(--easing-std);
}

.form-field__input:focus {
  outline: var(--border-width-thick) solid var(--border-focus);
  outline-offset: 2px;
  border-color: var(--border-focus);
}

.form-field__input[aria-invalid="true"] {
  border-color: var(--danger);
}

.form-field__error {
  display: block;
  margin-top: var(--space-1);
  font-size: var(--text-sm);
  color: var(--danger);
}
```

### Nút

```css
/* Nút chính */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  min-height: 44px;
  min-width: 44px;
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  font-family: var(--font-sans);
  border-radius: var(--radius-md);
  border: var(--border-width) solid transparent;
  cursor: pointer;
  transition:
    background var(--duration-fast) var(--easing-std),
    border-color var(--duration-fast) var(--easing-std);
}
.btn:focus-visible {
  outline: var(--border-width-thick) solid var(--border-focus);
  outline-offset: 2px;
}

.btn--primary {
  background: var(--primary);
  color: var(--on-primary);
}
.btn--primary:hover {
  background: var(--primary-hover);
}

.btn--accent {
  background: var(--accent);
  color: var(--on-accent);
}
.btn--accent:hover {
  background: var(--accent-hover);
}

/* Nút phụ — outline */
.btn--secondary {
  background: transparent;
  color: var(--primary);
  border-color: var(--border);
}
.btn--secondary:hover {
  background: var(--overlay);
}

/* Nút nguy hiểm */
.btn--danger {
  background: var(--danger);
  color: var(--on-danger);
}

/* Trạng thái đang xử lý */
.btn[aria-busy="true"] {
  opacity: 0.75;
  cursor: wait;
  pointer-events: none;
}
.btn[aria-busy="true"]::after {
  content: "";
  display: inline-block;
  width: 14px;
  height: 14px;
  margin-left: var(--space-2);
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
@media (prefers-reduced-motion: reduce) {
  .btn[aria-busy="true"]::after {
    animation: none;
    opacity: 0.5;
  }
}
```

### Thẻ nhóm/bạn học

Khung quản lý và các nơi không phải khung sinh viên dùng `.card`. Ở khung sinh viên, thẻ bạn học và thẻ nhóm dùng `.ticket` (xem mục "Chữ ký hình ảnh"). Không bo góc lớn. Viền thay bóng. Hành động ở cuối thẻ, trong tầm ngón cái.

```css
.card {
  background: var(--surface-raised);
  border: var(--border-width) solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
/* Không lồng card trong card */
```

### Badge trạng thái đặt phòng

```html
<span class="badge badge--pending"> Chờ duyệt </span>
```

```css
.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  border-radius: var(--radius-sm);
  border: var(--border-width) solid;
  /* Không dùng icon trang trí trong badge */
}
.badge--pending {
  background: var(--status-pending-bg);
  color: var(--status-pending-text);
  border-color: var(--status-pending-border);
}
.badge--approved {
  background: var(--status-approved-bg);
  color: var(--status-approved-text);
  border-color: var(--status-approved-border);
}
.badge--checkin {
  background: var(--status-checkin-bg);
  color: var(--status-checkin-text);
  border-color: var(--status-checkin-border);
} /* tô đặc */
.badge--noshow {
  background: var(--status-noshow-bg);
  color: var(--status-noshow-text);
  border-color: var(--status-noshow-border);
}
.badge--cancelled {
  background: var(--status-cancelled-bg);
  color: var(--status-cancelled-text);
  border-color: var(--status-cancelled-border);
}
```

### Thông báo Toast

Xuất hiện từ dưới (mobile) hoặc góc phải trên (desktop). Tự biến mất sau 5 giây, có nút đóng.

```css
.toast {
  position: fixed;
  bottom: calc(var(--nav-bottom-height) + var(--space-4));
  left: var(--space-4);
  right: var(--space-4);
  background: var(--surface-raised);
  border: var(--border-width) solid var(--border);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  box-shadow: var(--shadow-md);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-sm);
}
.toast--success {
  border-left: 3px solid var(--status-approved-border);
}
.toast--error {
  border-left: 3px solid var(--danger);
}
```

### Khung chat

```css
.chat-layout {
  display: flex;
  flex-direction: column;
  height: 100dvh; /* dvh thay vh để tính đúng khi bàn phím hiện */
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
  overscroll-behavior: contain;
}
.chat-input-bar {
  flex-shrink: 0;
  padding: var(--space-3) var(--space-4);
  padding-bottom: calc(var(--space-3) + env(safe-area-inset-bottom));
  background: var(--surface-raised);
  border-top: var(--border-width) solid var(--border-light);
  display: flex;
  gap: var(--space-3);
  align-items: flex-end;
}
```

### Trạng thái rỗng

Dùng chữ làm trung tâm, không dùng emoji hay illustration lớn.

```html
<div class="empty-state">
  <p class="empty-state__title">Chưa có nhóm học nào</p>
  <p class="empty-state__desc">Tìm bạn học rồi lập nhóm để bắt đầu.</p>
  <a href="/matching" class="btn btn--primary">Tìm bạn học</a>
</div>
```

### Trạng thái đang tải

Thanh skeleton đơn giản: hình chữ nhật `--surface` với animation mờ dần. Không dùng spinner xoay cho nội dung trang — chỉ dùng spinner cho nút đang submit.

```css
.skeleton {
  background: var(--surface);
  border-radius: var(--radius-sm);
  animation: shimmer 1.5s ease-in-out infinite;
}
@keyframes shimmer {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
@media (prefers-reduced-motion: reduce) {
  .skeleton {
    animation: none;
  }
}
```

### Trang lỗi 403 / 404 / 500

Tiêu đề lớn (mã lỗi), dòng mô tả ngắn, một nút hành động.

| Mã  | Tiêu đề                 | Mô tả                                   | Nút          |
| --- | ----------------------- | --------------------------------------- | ------------ |
| 403 | Không có quyền truy cập | Trang này yêu cầu quyền cao hơn.        | Về trang chủ |
| 404 | Không tìm thấy trang    | Đường dẫn không tồn tại hoặc đã bị xóa. | Về trang chủ |
| 500 | Lỗi hệ thống            | Đã có sự cố. Vui lòng thử lại sau.      | Thử lại      |

### Hộp xác nhận hành động nguy hiểm

```html
<dialog
  class="confirm-dialog"
  aria-labelledby="confirm-title"
  aria-describedby="confirm-desc"
>
  <h2 class="confirm-dialog__title" id="confirm-title">Hủy đặt phòng?</h2>
  <p class="confirm-dialog__desc" id="confirm-desc">
    Hành động này không thể hoàn tác. Phòng A101 lúc 09:00–11:00 sẽ được giải
    phóng.
  </p>
  <div class="confirm-dialog__actions">
    <button class="btn btn--secondary" data-action="cancel">Không hủy</button>
    <button class="btn btn--danger" data-action="confirm">Xác nhận hủy</button>
  </div>
</dialog>
```

Nút hủy hành động (thoát an toàn) đứng trước nút xác nhận (nguy hiểm). Focus mặc định vào nút an toàn.

---

## 5. Icon

**Triết lý:** Mặc định dùng chữ rõ ràng. Icon hỗ trợ nhận diện nhanh tại các điểm điều hướng, trạng thái, và hành động.

**Bộ icon duy nhất** — SVG sprite nội tuyến tập trung (`templates/fragments/icons.html`), ẩn bằng `width="0" height="0" style="position:absolute" aria-hidden="true"`, các symbol có viewBox `0 0 24 24`, nét đơn `1.5px`, dùng `currentColor`, kích thước mặc định 20px. Không dùng thư viện ngoài, không CDN, không emoji.

Sử dụng qua cú pháp: `<svg class="icon" aria-hidden="true"><use href="#id"/></svg>`.

| #   | Tên Symbol              | Ý nghĩa / Dùng cho               | Ngữ cảnh                                  |
| --- | ----------------------- | -------------------------------- | ----------------------------------------- |
| 1   | `icon-brand`            | Logo dự án StudyMate             | Thanh trên, Auth hero, Sidebar            |
| 2   | `icon-search`           | Tìm bạn học (hoặc `icon-magnifying-glass`) | Topbar, Tab bar, Thẻ hành trình |
| 3   | `icon-users`            | Nhóm học                         | Topbar, Tab bar, Thẻ hành trình           |
| 4   | `icon-door`             | Phòng học                        | Quản lý phòng                             |
| 5   | `icon-calendar`         | Đặt phòng (hoặc `icon-calendar-blank`) | Topbar, Tab bar, Thẻ hành trình     |
| 6   | `icon-clock`            | Chốt giờ rảnh chung              | Thẻ hành trình                            |
| 7   | `icon-chat`             | Chat                             | Topbar, Tab bar                           |
| 8   | `icon-bell`             | Thông báo                        | Thanh trên (icon chuông)                  |
| 9   | `icon-user`             | Người dùng                       | Ô nhập Họ tên                             |
| 10  | `icon-user-circle`      | Hồ sơ cá nhân                    | Menu avatar, Tab bar                      |
| 11  | `icon-mail`             | Thư / Email                      | Ô nhập Email                              |
| 12  | `icon-lock`             | Ổ khóa / Bảo mật                 | Ô nhập Mật khẩu, Trang lỗi 403            |
| 13  | `icon-eye`              | Hiện mật khẩu                    | Nút toggle mật khẩu                       |
| 14  | `icon-eye-slash`        | Ẩn mật khẩu                      | Nút toggle mật khẩu                       |
| 15  | `icon-sun`              | Giao diện Sáng                   | Theme switcher                            |
| 16  | `icon-moon`             | Giao diện Tối                    | Theme switcher                            |
| 17  | `icon-desktop`          | Giao diện Hệ thống               | Theme switcher                            |
| 18  | `icon-logout`           | Đăng xuất                        | Menu avatar, Sidebar footer               |
| 19  | `icon-chevron-down`     | Mũi tên xuống                    | Nút avatar trigger                        |
| 20  | `icon-arrow-right`      | Mũi tên phải (hoặc `icon-caret-right`) | Nút submit form, Thẻ hành trình     |
| 21  | `icon-warning`          | Cảnh báo, lỗi                    | Lỗi form-field, Alert error, Lỗi 500      |
| 22  | `icon-check`            | Thành công, xác nhận             | Alert success, Duyệt đặt phòng            |
| 23  | `icon-info`             | Thông tin                        | Alert info                                |
| 24  | `icon-home`             | Trang chủ                        | Nút về trang chủ ở các trang lỗi          |
| 25  | `icon-chart`            | Thống kê                         | Sidebar Staff                             |
| 26  | `icon-shield`           | Quản lý tài khoản Admin          | Sidebar Staff                             |
| 27  | `icon-list`             | Menu hamburger mobile            | Header Staff (<768px)                     |

**Quy tắc sử dụng:**

- Icon đi kèm nhãn chữ: dùng `aria-hidden="true"` trên thẻ `<svg>`.
- Icon chỉ có nút (không có chữ): PHẢI có `aria-label` và `title` trên phần tử cha (`<button>` hoặc `<a>`).
- Trong khung đã đăng nhập của sinh viên: icon chỉ xuất hiện ở logo thương hiệu, chuông thông báo, mũi tên avatar, cụm theme, thẻ hành trình và thanh tab dưới của điện thoại. Hàng menu điều hướng desktop (`.navbar__nav`) CHỈ GỒM CHỮ THUẦN TÚY, TUYỆT ĐỐI KHÔNG DÙNG ICON.
- Nhóm 3 nút theme trên khung sinh viên hiển thị trực tiếp ở khu vực tác vụ trên thanh trên khi màn hình rộng (≥1024px); trên màn hình hẹp (<1024px), cụm theme được đưa vào bên trong dropdown menu avatar để tránh chật chội.

---

## 6. Accessibility & UX

### Focus

```css
:focus-visible {
  outline: var(--border-width-thick) solid var(--border-focus);
  outline-offset: 2px;
  border-radius: var(--radius-sm);
}
/* Ẩn outline khi dùng chuột/cảm ứng */
:focus:not(:focus-visible) {
  outline: none;
}
```

### Nhãn form

Mọi `<input>` có `<label>` hoặc `aria-label`. Không dùng placeholder làm nhãn duy nhất. Nhóm radio/checkbox có `<fieldset>` + `<legend>`.

### Chỉ màu không đủ

Trạng thái, lỗi, kết quả luôn có nhãn chữ hoặc icon có nhãn, không chỉ màu.

### prefers-reduced-motion

```css
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
```

### Vùng chạm

Mọi phần tử tương tác tối thiểu `44×44px`. Nếu icon nhỏ hơn, dùng padding hoặc `min-width/min-height`.

### Không phụ thuộc hover

Mọi thông tin quan trọng và hành động phải truy cập được không cần hover. Hover chỉ là visual feedback thêm.

### Phản hồi khi gửi form

Nút chuyển `aria-busy="true"` và bị vô hiệu hóa khi đang submit. Hiển thị thông báo thành công hoặc lỗi sau khi nhận phản hồi.

### Chữ giao diện tiếng Việt — 10 mẫu nhãn/thông báo

| Tình huống        | Nhãn / Thông báo                            |
| ----------------- | ------------------------------------------- |
| Nút đặt phòng     | "Đặt phòng"                                 |
| Đang xử lý        | "Đang gửi yêu cầu…"                         |
| Thành công        | "Đặt phòng thành công. Chờ duyệt."          |
| Lỗi server        | "Không thể gửi yêu cầu. Thử lại sau."       |
| Ô nhập trống      | "Vui lòng nhập tên môn học."                |
| Xác nhận hủy      | "Bạn có chắc muốn hủy đặt phòng này không?" |
| Danh sách rỗng    | "Chưa có yêu cầu nào đang chờ duyệt."       |
| Kết nối           | "Gửi lời kết nối"                           |
| Chấp nhận lời mời | "Tham gia nhóm"                             |
| Khóa tài khoản    | "Khóa tài khoản" (trong hộp xác nhận)       |

---

## 7. Danh sách cấm

| ❌ Cấm                                            | Lý do                                                  |
| ------------------------------------------------- | ------------------------------------------------------ |
| Gradient tím–hồng, gradient bất kỳ dạng trang trí | Không phù hợp định hướng                               |
| Glassmorphism (`backdrop-filter: blur`) tràn lan  | Chi phí render cao, khó đọc                            |
| Emoji làm icon (`🔔 🏠 ✅`)                       | Không nhất quán, không kiểm soát được                  |
| Spam icon: icon trước mọi nhãn, dòng, nút         | Tăng nhiễu thị giác, không thêm nghĩa                  |
| Card lồng trong card                              | Tạo độ sâu giả tạo, khó đọc                            |
| `box-shadow` nhiều lớp                            | Dùng `--shadow-sm` hoặc `--shadow-md`, không nhiều hơn |
| Chữ xám nhạt dưới 4.5:1                           | Vi phạm WCAG AA                                        |
| Dùng hex trực tiếp trong file CSS module          | Vi phạm quy ước token                                  |
| Phông mặc định `system-ui` không lý do            | Phải dùng Be Vietnam Pro                               |
| Văn mẫu "Chào mừng đến với tương lai…"            | Không phù hợp sản phẩm thực                            |
| Số liệu thống kê giả                              | Không trung thực                                       |
| Animation rườm rà: fade+slide trên mọi section    | Nhiễu, chậm (Ngoại lệ duy nhất: vạch trượt mềm `.navbar__indicator` của rail menu sinh viên, có tôn trọng prefers-reduced-motion) |
| `border-radius: 20px+` cho mọi thứ                | Không phù hợp định hướng (Ngoại lệ: dùng `--radius-full` cho thanh ray `.navbar__rail`, nút avatar pill và các pill badge) |
| `th:utext` với dữ liệu người dùng nhập            | Lỗ hổng XSS                                            |
| Dùng `hover` làm phản hồi duy nhất                | Điện thoại không có hover                              |

---

## 8. Quy ước đặt tên

### File CSS

```
static/css/
├── tokens.css        ← Biến CSS toàn hệ thống (import đầu tiên)
├── base.css          ← Reset, kiểu chữ cơ bản, layout helper
├── user.css          ← Module: tài khoản, hồ sơ (Khoa)
├── matching.css      ← Module: tìm bạn, match score (Khương)
├── group.css         ← Module: nhóm học, lời mời (Huy)
├── room.css          ← Module: phòng, đặt phòng, check-in (Lịch)
└── insight.css       ← Module: gợi ý, dashboard, AI (Khang)
```

**Quy tắc import trong Thymeleaf layout:**

```html
<!-- layout/student.html và layout/staff.html -->
<link rel="stylesheet" th:href="@{/css/tokens.css}" />
<link rel="stylesheet" th:href="@{/css/base.css}" />
<!-- Module CSS được include bởi từng trang con qua th:replace -->
```

### Tên class CSS — BEM đơn giản

```
.block
.block__element
.block--modifier
```

Ví dụ: `.form-field`, `.form-field__label`, `.form-field__input`, `.form-field__error`, `.btn`, `.btn--primary`, `.card`, `.ticket`, `.ledger`, `.badge`, `.badge--pending`, `.nav-bottom`, `.nav-bottom__item`.

Không dùng `camelCase` hay `snake_case` cho class CSS.

### Tên Thymeleaf fragment

```
templates/
├── layout/
│   ├── student.html          ← layout cho STUDENT
│   │   fragments: head, header, nav-bottom, scripts
│   └── staff.html            ← layout cho MANAGER + ADMIN
│       fragments: head, sidebar, header, scripts
├── fragments/
│   ├── form-field.html       ← th:fragment="field(id, label, type)"
│   ├── badge-status.html     ← th:fragment="badge(status)"
│   ├── toast.html            ← th:fragment="toast"
│   └── confirm-dialog.html   ← th:fragment="confirm(title, desc)"
├── user/                     ← Khoa
├── matching/                 ← Khương
├── group/                    ← Huy
├── room/                     ← Lịch
└── insight/                  ← Khang
```

Fragment được dùng bằng `th:replace="~{fragments/badge-status :: badge(${booking.status})}"`.

---

## 9. Chuyển đổi giao diện sáng / tối

### Vị trí nút chọn

**Khung STUDENT:** Hiển thị trực tiếp ở khu vực tác vụ trên thanh trên (bên cạnh chuông thông báo) khi màn hình rộng (`≥1024px`). Trên màn hình hẹp (`<1024px`), cụm theme được ẩn khỏi thanh trên và đưa vào bên trong dropdown menu avatar dưới nhóm nhãn "Giao diện".

**Khung MANAGER/ADMIN:** cuối sidebar, trên nút Đăng xuất.

### Dạng nút — Nhóm 3 lựa chọn bằng Icon

Nút chuyển chủ đề không hiển thị chữ, sử dụng 3 icon (mặt trời, mặt trăng, màn hình) có `aria-label`, `title`, và `aria-pressed`:

```html
<div class="theme-switcher" role="group" aria-label="Chủ đề giao diện">
  <button
    class="theme-switcher__btn"
    data-theme-value="light"
    aria-pressed="true"
    aria-label="Giao diện sáng"
    title="Giao diện sáng"
  >
    <svg class="icon" aria-hidden="true"><use href="#icon-sun"/></svg>
  </button>
  <button
    class="theme-switcher__btn"
    data-theme-value="dark"
    aria-pressed="false"
    aria-label="Giao diện tối"
    title="Giao diện tối"
  >
    <svg class="icon" aria-hidden="true"><use href="#icon-moon"/></svg>
  </button>
  <button
    class="theme-switcher__btn"
    data-theme-value="system"
    aria-pressed="false"
    aria-label="Theo hệ thống"
    title="Theo hệ thống"
  >
    <svg class="icon" aria-hidden="true"><use href="#icon-desktop"/></svg>
  </button>
</div>
```

```css
.theme-switcher {
  display: inline-flex;
  align-items: center;
  border: var(--border-width) solid var(--border-light);
  border-radius: var(--radius-full);
  padding: 3px;
  gap: 2px;
  background: var(--surface);
}
.theme-switcher__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  min-width: 32px;
  min-height: 32px;
  padding: 0;
  border-radius: var(--radius-full);
  font-family: var(--font-sans);
  background: transparent;
  color: var(--text-muted);
  border: none;
  cursor: pointer;
  transition:
    background var(--duration-fast) var(--easing-std),
    color var(--duration-fast) var(--easing-std);
}
.theme-switcher__btn svg {
  width: 16px;
  height: 16px;
}
    color var(--duration-fast) var(--easing-std);
}
.theme-switcher__btn[aria-pressed="true"] {
  background: var(--primary);
  color: var(--on-primary);
}
.theme-switcher__btn:focus-visible {
  outline: var(--border-width-thick) solid var(--border-focus);
  outline-offset: -2px;
}
```

### Thẻ meta và script áp theme — đặt trong `<head>`

Thứ tự bắt buộc trong fragment `head`: các thẻ `<meta>` trước, script áp theme sau (script cần tìm thẻ `theme-color` đã có sẵn). Script chạy đồng bộ, trước khi trang vẽ, tránh nháy màu (FOUC).

```html
<!-- fragment: layout/fragments/head.html -->
<!-- Dùng chung cho cả student.html và staff.html qua th:replace -->
<meta name="color-scheme" content="light dark" />
<meta name="theme-color" content="#EEF6F7" />

<script>
  (function () {
    "use strict";
    var STORAGE_KEY = "studymate-theme";
    var stored, resolved;

    try {
      stored = localStorage.getItem(STORAGE_KEY);
    } catch (e) {}

    // Chuẩn hóa: chỉ 'dark' và 'system' là hợp lệ có lưu.
    // Mọi trường hợp còn lại (chưa lưu, lỗi, giá trị lạ) đều là 'light'.
    if (stored === "dark") {
      resolved = "dark";
    } else if (stored === "system") {
      resolved = window.matchMedia("(prefers-color-scheme: dark)").matches
        ? "dark"
        : "light";
    } else {
      resolved = "light";
    }

    // Luôn gán data-theme — KHÔNG gỡ thuộc tính.
    // Khi tắt JavaScript, trang hiển thị chế độ sáng (CSS :root mặc định).
    document.documentElement.setAttribute("data-theme", resolved);

    // Đồng bộ màu thanh trình duyệt trên điện thoại ngay từ lần vẽ đầu.
    var meta = document.querySelector('meta[name="theme-color"]');
    if (meta)
      meta.setAttribute("content", resolved === "dark" ? "#0E1B20" : "#EEF6F7");
  })();
</script>
```

**Script điều khiển nút (đặt cuối `<body>` hoặc trong `scripts` fragment):**

```javascript
(function () {
  "use strict";
  var STORAGE_KEY = "studymate-theme";
  var mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");

  function resolveTheme(stored) {
    // 'dark' và 'system' là hai giá trị hợp lệ có lưu.
    // Mọi trường hợp còn lại (chưa lưu, lỗi, giá trị lạ) là 'light'.
    if (stored === "dark") return "dark";
    if (stored === "system") return mediaQuery.matches ? "dark" : "light";
    return "light";
  }

  function applyTheme(stored) {
    var resolved = resolveTheme(stored);
    // Luôn gán data-theme — KHÔNG gỡ thuộc tính.
    document.documentElement.setAttribute("data-theme", resolved);
    updateMeta(resolved === "dark");
  }

  function updateMeta(isDark) {
    var color = isDark ? "#0E1B20" : "#EEF6F7";
    var meta = document.querySelector('meta[name="theme-color"]');
    if (meta) meta.setAttribute("content", color);
  }

  function saveTheme(value) {
    try {
      localStorage.setItem(STORAGE_KEY, value);
    } catch (e) {}
  }

  function updateButtons(stored) {
    document.querySelectorAll(".theme-switcher__btn").forEach(function (btn) {
      btn.setAttribute(
        "aria-pressed",
        btn.dataset.themeValue === stored ? "true" : "false",
      );
    });
  }

  document.querySelectorAll(".theme-switcher__btn").forEach(function (btn) {
    btn.addEventListener("click", function () {
      var val = btn.dataset.themeValue;
      saveTheme(val);
      applyTheme(val);
      updateButtons(val);
    });
  });

  // Listener matchMedia chỉ tự đổi khi giá trị lưu là 'system'.
  mediaQuery.addEventListener("change", function () {
    var stored;
    try {
      stored = localStorage.getItem(STORAGE_KEY);
    } catch (e) {}
    if (stored === "system") applyTheme("system");
  });

  // Khởi tạo trạng thái nút theo giá trị đang lưu (không phải resolved).
  var current;
  try {
    current = localStorage.getItem(STORAGE_KEY) || "light";
  } catch (e) {
    current = "light";
  }
  updateButtons(current);
})();
```

### Bảng kiểm tra tương phản — hai chế độ

| Thành phần               | Cặp cần kiểm tra                   | Yêu cầu | Tình trạng                              |
| ------------------------ | ---------------------------------- | ------- | --------------------------------------- |
| Chữ chính                | `--text` trên `--bg`               | ≥4.5:1  | ✅ Đã tính (sáng 13.69, tối 15.25)      |
| Chữ trên thẻ tối         | `--text` trên `--surface-raised`   | ≥4.5:1  | ✅ Đã tính (tối 11.17)                  |
| Chữ phụ                  | `--text-muted` trên `--bg`         | ≥4.5:1  | ✅ Đã tính (sáng 5.59, tối 8.17)        |
| Nút chính                | `--on-primary` trên `--primary`    | ≥4.5:1  | ✅ Đã tính (sáng 6.70, tối 7.23)        |
| Nút accent               | `--on-accent` trên `--accent`      | ≥4.5:1  | ✅ Đã tính (sáng/tối đều ✓)             |
| Viền ô nhập              | `--border` trên `--bg`             | ≥3:1    | ✅ Đã tính (sáng 4.33, tối 4.88)        |
| Viền ô nhập trên thẻ tối | `--border` trên `--surface-raised` | ≥3:1    | ✅ Đã tính (tối 3.58)                   |
| Badge trạng thái         | chữ trên nền badge                 | ≥4.5:1  | ✅ Đã tính (cả 5 trạng thái × 2 chế độ) |
| Focus ring               | `--border-focus` trên `--bg`       | ≥3:1    | ✅ Đã tính (sáng 6.70, tối 7.70)        |

### Thành phần dễ hỏng khi sang tối — cần kiểm tra thủ công

- **Viền ô nhập:** Màu `--border` tối (`#5090A0`) — xem bằng mắt trên `--surface-raised` tối (`#1C3540`), tỉ lệ đã tính nhưng cần xem thực tế.
- **Badge trạng thái:** Màu nền badge tối (màu đậm) — đảm bảo không bị lẫn vào nền trang. Badge CHECKED_IN tô đặc bằng `--primary` sáng, kiểm tra không chói.
- **Bảng lịch phòng (time grid):** Ô trống và ô đã đặt phải phân biệt được; kiểm tra cả trạng thái ô được chọn.
- **Availability Grid (lưới giờ rảnh):** Ô có người rảnh và ô tất cả rảnh phải đủ tương phản với nền.
- **Khung chat:** Màu bong bóng tin nhắn gửi đi vs. nhận về trên nền tối.
- **Trạng thái rỗng:** Chữ mô tả (`--text-muted`) trên `--bg` tối — đã tính (8.17:1) nhưng cần xem bố cục thực tế.
- **Skeleton loading:** Animation mờ dần trên `--surface` tối.
- **Biểu đồ / heatmap (insight module):** Màu palette biểu đồ cần bộ token riêng tối — không dùng màu sáng nguyên si.
- **Hộp xác nhận (dialog):** `--surface-raised` tối (`#1C3540`) trên overlay — kiểm tra độ tương phản overlay.
- **Toast:** Viền trái màu trạng thái trên nền `--surface-raised` tối.
- **Thẻ phiếu (`.ticket`):** Hai khuyết tròn dùng `--bg` làm màu nền, kiểm tra không lộ viền sai ở cả hai chế độ.

---

## Đã kiểm tra / Chưa kiểm tra

### ✅ Đã kiểm tra / xác nhận

- **Be Vietnam Pro có subset vietnamese:** Xác nhận qua Google Fonts database (search.py, field Subsets: vietnamese). Nên tự thử gõ "Nguyễn Thị Hồng Ánh" ở fonts.google.com để nhìn tận mắt.
- **Noto Sans có subset vietnamese:** Xác nhận — được dùng làm fallback.
- **Tỉ lệ tương phản chữ chính, phụ, nút, badge:** Tính bằng script Python theo công thức WCAG 2.1 relative luminance. Tất cả cặp màu dùng thực tế đều PASS.
- **Chữ trên `--surface-raised` tối:** `--text` đạt 11.17:1.
- **Viền ô nhập:** Điều chỉnh từ gợi ý ban đầu để đạt 3:1; trên `--bg` tối 4.88:1, trên `--surface` tối 4.34:1, trên `--surface-raised` tối 3.58:1.
- **Trạng thái đặt phòng × 2 chế độ:** Tất cả 10 cặp PASS AA (CHECKED_IN dùng `--on-primary` trên `--primary`).
- **Lỗi script chủ đề đã sửa:** Mặc định luôn là sáng; chọn "Sáng" khi máy ở chế độ tối vẫn hiện sáng; đã xóa khối `@media (prefers-color-scheme: dark)` trùng lặp. (Mới đọc logic, chưa chạy trên trình duyệt: xem các bước kiểm tra bên dưới.)
- **Điểm phù hợp (Ledger):** Tổng điểm tối đa các tiêu chí bằng 100, ví dụ khớp (32+24+14+8 = 78).

### ⚠️ Chưa xác minh — cần kiểm tra thủ công

- **Script sáng/tối trên trình duyệt thật:** (1) xóa localStorage, máy đang ở chế độ tối thì web phải hiện sáng; (2) chọn "Sáng" khi máy ở chế độ tối thì web vẫn sáng; (3) chọn "Hệ thống" khi máy ở chế độ tối thì web tối, đổi cài đặt máy thì web đổi theo; (4) tải lại trang không bị nháy màu; (5) thanh trình duyệt trên điện thoại đổi màu theo theme.
- **Hiển thị Be Vietnam Pro trên thiết bị thực:** Chưa render thực tế. Cần kiểm tra trên iOS Safari, Android Chrome.
- **Be Vietnam Pro có số cùng độ rộng (`tabular-nums`) không:** Chưa kiểm tra; nếu không, đặt `min-width` cho cột giờ và căn phải.
- **Thẻ phiếu (`.ticket`):** Giá trị pixel của khuyết tròn chưa chạy thử trên trình duyệt; cần xem và chỉnh trên điện thoại thật.
- **Font fallback khi offline / Google Fonts bị chặn:** Cần test với network throttling.
- **Biểu đồ insight module:** Chưa định nghĩa palette biểu đồ cho chế độ tối.
- **Keyboard navigation trên bottom tab bar:** Cần test `tabindex` và focus trap khi mở dialog từ tab bar.
- **iOS safe area với notch động (Dynamic Island):** `env(safe-area-inset-bottom)` đã có trong CSS nhưng chưa test trên thiết bị thực.
- **Bong bóng chat chế độ tối:** Màu chưa được định nghĩa trong token — module chat cần thêm token riêng.
