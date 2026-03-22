<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - Hệ thống Xe Buýt Trường Học</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
        body {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            background: radial-gradient(ellipse at 20% 50%, rgba(79, 70, 229, 0.15) 0%, transparent 50%),
                radial-gradient(ellipse at 80% 20%, rgba(14, 165, 233, 0.12) 0%, transparent 50%),
                var(--bg-dark);
            padding: 20px 0;
        }

        .login-wrap {
            width: 100%;
            max-width: 500px;
            padding: 24px;
        }

        .login-brand {
            text-align: center;
            margin-bottom: 30px;
        }

        .login-brand .bus-icon {
            width: 64px;
            height: 64px;
            border-radius: 18px;
            background: linear-gradient(135deg, var(--primary), var(--secondary));
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 30px;
            margin: 0 auto 12px;
            box-shadow: 0 10px 30px rgba(79, 70, 229, 0.4);
        }

        .login-brand h1 {
            font-size: 20px;
            font-weight: 800;
            line-height: 1.3;
        }

        .login-card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 32px;
            box-shadow: var(--shadow-lg);
        }

        .login-card h2 {
            font-size: 20px;
            font-weight: 700;
            margin-bottom: 6px;
        }

        .login-card .subtitle {
            color: var(--text-muted);
            font-size: 13px;
            margin-bottom: 24px;
        }

        .input-group {
            position: relative;
        }

        .input-group .input-icon {
            position: absolute;
            left: 12px;
            top: 50%;
            transform: translateY(-50%);
            pointer-events: none;
            font-size: 16px;
            color: var(--text-muted);
        }

        .input-group .form-control {
            padding-left: 38px;
        }

        .input-group .toggle-pw {
            position: absolute;
            right: 12px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            cursor: pointer;
            color: var(--text-muted);
            font-size: 15px;
            padding: 2px;
        }

        .login-btn {
            width: 100%;
            padding: 13px;
            background: linear-gradient(135deg, var(--primary), var(--primary-light));
            color: white;
            border: none;
            border-radius: var(--radius-sm);
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s ease;
            box-shadow: 0 4px 15px rgba(79, 70, 229, 0.35);
            margin-top: 10px;
        }

        .login-btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 8px 25px rgba(79, 70, 229, 0.5);
        }

        .login-btn:active {
            transform: translateY(0);
        }

        /* Grid layout cho form */
        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
        }
        
        @media (max-width: 500px) {
            .form-row {
                grid-template-columns: 1fr;
                gap: 0;
            }
        }
    </style>
</head>

<body>
    <div class="login-wrap">
        <div class="login-brand">
            <div class="bus-icon">🚌</div>
            <h1>BusTrack School</h1>
        </div>

        <div class="login-card">
            <h2>Đăng ký tài khoản</h2>
            <p class="subtitle">Dành riêng cho Phụ huynh học sinh</p>

            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
                <div class="alert alert-danger" style="margin-bottom: 20px;">
                    ⚠️ <%= error %>
                </div>
            <% } %>

            <form method="POST" action="${pageContext.request.contextPath}/register">
                <div class="form-group">
                    <label class="form-label">Họ và tên</label>
                    <div class="input-group">
                        <span class="input-icon">📝</span>
                        <input type="text" name="fullName" class="form-control" placeholder="Nguyễn Văn A" 
                               value="${not empty requestScope.fullName ? requestScope.fullName : ''}" required>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label">Email</label>
                        <div class="input-group">
                            <span class="input-icon">✉️</span>
                            <input type="email" name="email" class="form-control" placeholder="email@example.com"
                                   value="${not empty requestScope.email ? requestScope.email : ''}" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Số điện thoại</label>
                        <div class="input-group">
                            <span class="input-icon">📞</span>
                            <input type="tel" name="phone" class="form-control" placeholder="09xxxxxxxx"
                                   value="${not empty requestScope.phone ? requestScope.phone : ''}" required>
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label">Tên đăng nhập</label>
                    <div class="input-group">
                        <span class="input-icon">👤</span>
                        <input type="text" name="username" class="form-control" placeholder="Tên đăng nhập (trên 4 ký tự)"
                               value="${not empty requestScope.username ? requestScope.username : ''}" required minlength="4">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label">Mật khẩu</label>
                        <div class="input-group">
                            <span class="input-icon">🔒</span>
                            <input type="password" id="password" name="password" class="form-control" 
                                   placeholder="Tối thiểu 6 ký tự" required minlength="6">
                            <button type="button" class="toggle-pw" onclick="togglePw('password')">👁️</button>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Xác nhận mật khẩu</label>
                        <div class="input-group">
                            <span class="input-icon">🔐</span>
                            <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                                   placeholder="Nhập lại mật khẩu" required minlength="6">
                            <button type="button" class="toggle-pw" onclick="togglePw('confirmPassword')">👁️</button>
                        </div>
                    </div>
                </div>

                <button type="submit" class="login-btn">Đăng ký ngay →</button>
            </form>

            <div style="text-align: center; margin-top: 20px; font-size: 14px;">
                <span>Đã có tài khoản? <a href="${pageContext.request.contextPath}/login" style="color: var(--primary); font-weight: 600; text-decoration: none;">Đăng nhập</a></span>
            </div>
        </div>
    </div>

    <script>
        function togglePw(id) {
            const pw = document.getElementById(id);
            pw.type = pw.type === 'password' ? 'text' : 'password';
        }
    </script>
</body>

</html>
