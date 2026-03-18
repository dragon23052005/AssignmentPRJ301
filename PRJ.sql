-- ============================================================
-- HỆ THỐNG QUẢN LÝ XE BUÝT TRƯỜNG HỌC
-- Database: SchoolBusDB  (SQL Server / T-SQL)
-- ============================================================

USE master;
GO

IF DB_ID('SchoolBusDB') IS NOT NULL
    DROP DATABASE SchoolBusDB;
GO

CREATE DATABASE SchoolBusDB
    COLLATE Vietnamese_CI_AS;
GO

USE SchoolBusDB;
GO

-- ============================================================
-- BẢNG USERS
-- ============================================================
CREATE TABLE users (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    username     NVARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name    NVARCHAR(100) NOT NULL,
    email        NVARCHAR(100),
    phone        NVARCHAR(20),
    role         NVARCHAR(10)  NOT NULL DEFAULT 'PARENT'
                     CONSTRAINT chk_user_role CHECK (role IN ('ADMIN','PARENT','MONITOR','DRIVER')),
    is_active    BIT DEFAULT 1,
    created_at   DATETIME2 DEFAULT GETDATE(),
    updated_at   DATETIME2 DEFAULT GETDATE()
);
GO

-- ============================================================
-- BẢNG ROUTES
-- ============================================================
CREATE TABLE routes (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    route_name           NVARCHAR(100) NOT NULL,
    route_code           NVARCHAR(20)  NOT NULL UNIQUE,
    description          NVARCHAR(MAX),
    morning_departure    TIME,
    afternoon_departure  TIME,
    is_active            BIT DEFAULT 1,
    created_at           DATETIME2 DEFAULT GETDATE()
);
GO

-- ============================================================
-- BẢNG STOPS
-- ============================================================
CREATE TABLE stops (
    id                       INT IDENTITY(1,1) PRIMARY KEY,
    route_id                 INT NOT NULL,
    stop_name                NVARCHAR(100) NOT NULL,
    stop_order               INT NOT NULL,
    address                  NVARCHAR(MAX),
    estimated_morning_time   TIME,
    estimated_afternoon_time TIME,
    created_at               DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_stop_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- BẢNG VEHICLES
-- ============================================================
CREATE TABLE vehicles (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    plate_number NVARCHAR(20) NOT NULL UNIQUE,
    brand        NVARCHAR(50),
    model        NVARCHAR(50),
    capacity     INT DEFAULT 45,
    driver_id    INT,
    monitor_id   INT,
    route_id     INT,
    is_active    BIT DEFAULT 1,
    created_at   DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_veh_driver  FOREIGN KEY (driver_id)  REFERENCES users(id),
    CONSTRAINT fk_veh_monitor FOREIGN KEY (monitor_id) REFERENCES users(id),
    CONSTRAINT fk_veh_route   FOREIGN KEY (route_id)   REFERENCES routes(id)
);
GO

-- ============================================================
-- BẢNG STUDENTS
-- ============================================================
CREATE TABLE students (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    full_name     NVARCHAR(100) NOT NULL,
    student_code  NVARCHAR(20) UNIQUE,
    class_name    NVARCHAR(20),
    date_of_birth DATE,
    parent_id     INT NOT NULL,
    created_at    DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_stu_parent FOREIGN KEY (parent_id) REFERENCES users(id)
);
GO

-- ============================================================
-- BẢNG REGISTRATIONS
-- ============================================================
CREATE TABLE registrations (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    student_id    INT NOT NULL,
    route_id      INT NOT NULL,
    stop_id       INT NOT NULL,
    start_date    DATE NOT NULL,
    end_date      DATE,
    is_active     BIT DEFAULT 1,
    registered_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_reg_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_reg_route   FOREIGN KEY (route_id)   REFERENCES routes(id),
    CONSTRAINT fk_reg_stop    FOREIGN KEY (stop_id)    REFERENCES stops(id)
);
GO

-- ============================================================
-- BẢNG TRIPS
-- ============================================================
CREATE TABLE trips (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    vehicle_id  INT NOT NULL,
    route_id    INT NOT NULL,
    trip_date   DATE NOT NULL,
    trip_type   NVARCHAR(10) NOT NULL
                    CONSTRAINT chk_trip_type CHECK (trip_type IN ('MORNING','AFTERNOON')),
    status      NVARCHAR(15) DEFAULT 'SCHEDULED'
                    CONSTRAINT chk_trip_status CHECK (status IN ('SCHEDULED','DEPARTED','COMPLETED','CANCELLED')),
    departed_at DATETIME2,
    arrived_at  DATETIME2,
    notes       NVARCHAR(MAX),
    created_by  INT,
    created_at  DATETIME2 DEFAULT GETDATE(),
    updated_at  DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_trip_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_trip_route   FOREIGN KEY (route_id)   REFERENCES routes(id),
    CONSTRAINT fk_trip_creator FOREIGN KEY (created_by) REFERENCES users(id)
);
GO

-- ============================================================
-- BẢNG ATTENDANCES
-- ============================================================
CREATE TABLE attendances (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    trip_id          INT NOT NULL,
    student_id       INT NOT NULL,
    status           NVARCHAR(20) DEFAULT 'ABSENT'
                         CONSTRAINT chk_att_status CHECK (status IN ('PRESENT','ABSENT','NOTIFIED_ABSENT')),
    boarded_at       DATETIME2,
    boarded_stop_id  INT,
    alighted_at      DATETIME2,
    alighted_stop_id INT,
    notes            NVARCHAR(MAX),
    updated_by       INT,
    updated_at       DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_att_trip    FOREIGN KEY (trip_id)          REFERENCES trips(id),
    CONSTRAINT fk_att_student FOREIGN KEY (student_id)       REFERENCES students(id),
    CONSTRAINT fk_att_bstop   FOREIGN KEY (boarded_stop_id)  REFERENCES stops(id),
    CONSTRAINT fk_att_astop   FOREIGN KEY (alighted_stop_id) REFERENCES stops(id),
    CONSTRAINT fk_att_editor  FOREIGN KEY (updated_by)       REFERENCES users(id),
    CONSTRAINT uk_trip_student UNIQUE (trip_id, student_id)
);
GO

-- ============================================================
-- BẢNG ABSENCE_NOTIFICATIONS
-- ============================================================
CREATE TABLE absence_notifications (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    student_id       INT NOT NULL,
    absence_date     DATE NOT NULL,
    reason           NVARCHAR(MAX),
    notified_at      DATETIME2 DEFAULT GETDATE(),
    status           NVARCHAR(15) DEFAULT 'PENDING'
                         CONSTRAINT chk_an_status CHECK (status IN ('PENDING','ACKNOWLEDGED')),
    acknowledged_by  INT,
    acknowledged_at  DATETIME2,
    CONSTRAINT fk_an_student FOREIGN KEY (student_id)      REFERENCES students(id),
    CONSTRAINT fk_an_ack     FOREIGN KEY (acknowledged_by) REFERENCES users(id)
);
GO

-- ============================================================
-- BẢNG VEHICLE_REPORTS
-- ============================================================
CREATE TABLE vehicle_reports (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    vehicle_id   INT NOT NULL,
    reported_by  INT NOT NULL,
    report_date  DATE NOT NULL,
    issue_type   NVARCHAR(15) NOT NULL
                     CONSTRAINT chk_vr_type CHECK (issue_type IN ('MECHANICAL','TIRE','BRAKE','ENGINE','ELECTRICAL','BODY','OTHER')),
    severity     NVARCHAR(10) DEFAULT 'LOW'
                     CONSTRAINT chk_vr_sev CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    description  NVARCHAR(MAX) NOT NULL,
    status       NVARCHAR(15) DEFAULT 'OPEN'
                     CONSTRAINT chk_vr_status CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED')),
    resolved_at  DATETIME2,
    created_at   DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_vr_vehicle  FOREIGN KEY (vehicle_id)  REFERENCES vehicles(id),
    CONSTRAINT fk_vr_reporter FOREIGN KEY (reported_by) REFERENCES users(id)
);
GO

-- ============================================================
-- DỮ LIỆU MẪU
-- Mật khẩu mặc định: Admin@123
-- SHA-1("Admin@123") = a29c57c6894dee6e8251510d58c07078ee3f49bf
-- ============================================================

-- Users
INSERT INTO users (username, password_hash, full_name, email, phone, role) VALUES
('admin',     'a29c57c6894dee6e8251510d58c07078ee3f49bf', N'Quản trị viên',   'admin@school.edu.vn',   '0901234567', 'ADMIN'),
('driver01',  'a29c57c6894dee6e8251510d58c07078ee3f49bf', N'Nguyễn Văn Tài',  'nvtai@school.edu.vn',   '0912345678', 'DRIVER'),
('driver02',  'a29c57c6894dee6e8251510d58c07078ee3f49bf', N'Trần Minh Hùng',  'tmhung@school.edu.vn',  '0923456789', 'DRIVER'),
('monitor01', 'a29c57c6894dee6e8251510d58c07078ee3f49bf', N'Lê Thị Hoa',      'lthoa@school.edu.vn',   '0934567890', 'MONITOR'),
('monitor02', 'a29c57c6894dee6e8251510d58c07078ee3f49bf', N'Phạm Thị Mai',    'ptmai@school.edu.vn',   '0945678901', 'MONITOR'),
('parent01',  'a29c57c6894dee6e8251510d58c07078ee3f49bf', N'Nguyễn Thị Lan',  'ntlan@gmail.com',       '0956789012', 'PARENT'),
('parent02',  'a29c57c6894dee6e8251510d58c07078ee3f49bf', N'Trần Văn Bình',   'tvbinh@gmail.com',      '0967890123', 'PARENT');
GO

-- Routes
INSERT INTO routes (route_name, route_code, description, morning_departure, afternoon_departure) VALUES
(N'Tuyến Bình Thạnh - Quận 1', 'R001', N'Đón HS khu vực Bình Thạnh, qua Quận 3, đến Quận 1', '06:30', '17:00'),
(N'Tuyến Thủ Đức - Quận 9',    'R002', N'Đón HS khu vực Thủ Đức và Quận 9',                  '06:15', '17:00'),
(N'Tuyến Gò Vấp - Quận 12',    'R003', N'Đón HS khu vực Gò Vấp và Quận 12',                  '06:20', '17:00');
GO

-- Stops tuyến R001
INSERT INTO stops (route_id, stop_name, stop_order, address, estimated_morning_time, estimated_afternoon_time) VALUES
(1, N'Bến xe Bình Thạnh',   1, N'8 Đinh Bộ Lĩnh, P.24, Q.Bình Thạnh', '06:30', '18:30'),
(1, N'Cầu Điện Biên Phủ',   2, N'Điện Biên Phủ, P.15, Q.Bình Thạnh',  '06:40', '18:20'),
(1, N'Hồ Con Rùa',          3, N'Trần Cao Vân, Q.3',                   '06:55', '18:05'),
(1, N'Trường học (Q.1)',     4, N'123 Nguyễn Đình Chiểu, Q.1',          '07:10', '17:00');
GO

-- Stops tuyến R002
INSERT INTO stops (route_id, stop_name, stop_order, address, estimated_morning_time, estimated_afternoon_time) VALUES
(2, N'Khu dân cư Thủ Đức', 1, N'Kha Vạn Cân, P.Linh Đông, Thủ Đức', '06:15', '18:45'),
(2, N'ĐHQG TP.HCM',         2, N'Đường Hàn Thuyên, Thủ Đức',          '06:30', '18:30'),
(2, N'Cầu Sài Gòn',         3, N'Xa lộ Hà Nội, Q.Bình Thạnh',         '06:50', '18:10'),
(2, N'Trường học (Q.1)',     4, N'123 Nguyễn Đình Chiểu, Q.1',          '07:15', '17:00');
GO

-- Vehicles
INSERT INTO vehicles (plate_number, brand, model, capacity, driver_id, monitor_id, route_id) VALUES
('51B-12345', N'Mercedes', N'Sprinter', 35, 2, 4, 1),
('51B-67890', N'Ford',     N'Transit',  16, 3, 5, 2);
GO

-- Students
INSERT INTO students (full_name, student_code, class_name, parent_id) VALUES
(N'Nguyễn Minh Khoa', 'HS001', '10A1', 6),
(N'Nguyễn Hà Linh',   'HS002', '11B2', 6),
(N'Trần Bảo Nam',      'HS003', '9C3',  7);
GO

-- Registrations
INSERT INTO registrations (student_id, route_id, stop_id, start_date, is_active) VALUES
(1, 1, 2, '2024-09-01', 1),
(2, 1, 1, '2024-09-01', 1),
(3, 2, 5, '2024-09-01', 1);
GO
