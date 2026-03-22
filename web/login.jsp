<%@ page contentType="text/html;charset=UTF-8" %>
  <!DOCTYPE html>
  <html lang="vi">

  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Hệ thống Xe Buýt Trường Học</title>
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
      }

      .login-wrap {
        width: 100%;
        max-width: 420px;
        padding: 24px;
      }

      .login-brand {
        text-align: center;
        margin-bottom: 40px;
      }

      .login-brand .bus-icon {
        width: 72px;
        height: 72px;
        border-radius: 20px;
        background: linear-gradient(135deg, var(--primary), var(--secondary));
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 36px;
        margin: 0 auto 16px;
        box-shadow: 0 12px 40px rgba(79, 70, 229, 0.4);
      }

      .login-brand h1 {
        font-size: 22px;
        font-weight: 800;
        line-height: 1.3;
      }

      .login-brand p {
        color: var(--text-muted);
        font-size: 13px;
        margin-top: 6px;
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
        margin-bottom: 28px;
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
      }

      .login-btn:hover {
        transform: translateY(-1px);
        box-shadow: 0 8px 25px rgba(79, 70, 229, 0.5);
      }

      .login-btn:active {
        transform: translateY(0);
      }

      .demo-accounts {
        margin-top: 20px;
        padding: 16px;
        background: rgba(255, 255, 255, 0.03);
        border: 1px dashed var(--border);
        border-radius: var(--radius-sm);
      }

      .demo-accounts .title {
        font-size: 11px;
        color: var(--text-muted);
        text-transform: uppercase;
        letter-spacing: 1px;
        margin-bottom: 10px;
        font-weight: 600;
      }

      .demo-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 12px;
        padding: 4px 0;
      }

      .demo-item .role {
        color: var(--text-muted);
      }

      .demo-item .cred {
        color: var(--text-secondary);
        font-family: monospace;
      }

      .demo-item .fill-btn {
        background: none;
        border: none;
        color: var(--primary-light);
        font-size: 11px;
        cursor: pointer;
        padding: 2px 6px;
        border-radius: 4px;
        transition: background 0.2s;
      }

      .demo-item .fill-btn:hover {
        background: rgba(99, 102, 241, 0.15);
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

      .input-group .toggle-pw:hover {
        color: var(--text-primary);
      }
    </style>
  </head>

  <body>
    <div class="login-wrap">
      <div class="login-brand">
        <div class="bus-icon">🚌</div>
        <h1>BusTrack School</h1>
        <p>Hệ thống quản lý xe buýt trường học</p>
      </div>

      <div class="login-card">
        <h2>Chào mừng trở lại!</h2>
        <p class="subtitle">Vui lòng đăng nhập để tiếp tục</p>

        <% String error=(String) request.getAttribute("error"); %>
          <% if (error !=null) { %>
            <div class="alert alert-danger">⚠️ <%= error %>
            </div>
            <% } %>
              <% String msg=request.getParameter("msg"); %>
              <% String registered=request.getParameter("registered"); %>
                <% if ("logout".equals(msg)) { %>
                  <div class="alert alert-success">✅ Đã đăng xuất thành công!</div>
                  <% } %>
                <% if ("true".equals(registered)) { %>
                  <div class="alert alert-success">✅ Đăng ký thành công! Vui lòng đăng nhập.</div>
                  <% } %>

                    <form method="POST" action="${pageContext.request.contextPath}/login">
                      <div class="form-group">
                        <label class="form-label">Tên đăng nhập</label>
                        <div class="input-group">
                          <span class="input-icon">👤</span>
                          <input type="text" id="username" name="username" class="form-control"
                            value="${not empty requestScope.rememberedUsername ? requestScope.rememberedUsername : ''}"
                            placeholder="Nhập tên đăng nhập" required autocomplete="username">
                        </div>
                      </div>
                      <div class="form-group">
                        <label class="form-label">Mật khẩu</label>
                        <div class="input-group">
                          <span class="input-icon">🔒</span>
                          <input type="password" id="password" name="password" class="form-control"
                            value="${not empty requestScope.rememberedPassword ? requestScope.rememberedPassword : ''}"
                            placeholder="Nhập mật khẩu" required autocomplete="current-password">
                          <button type="button" class="toggle-pw" onclick="togglePw()">👁️</button>
                        </div>
                      </div>
                      <div class="form-group" style="display: flex; align-items: center; gap: 8px; margin-top: -5px; margin-bottom: 15px;">
                        <input type="checkbox" name="rememberMe" id="rememberMe" style="width: auto;">
                        <label for="rememberMe" style="margin: 0; font-size: 14px; cursor: pointer;">Nhớ tài khoản và mật khẩu</label>
                      </div>
                      <button type="submit" class="login-btn">Đăng nhập →</button>
                    </form>
                    
                    <div style="text-align: center; margin-top: 20px; font-size: 14px;">
                        <span>Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register" style="color: var(--primary); font-weight: 600; text-decoration: none;">Đăng ký ngay</a></span>
                    </div>

                    <div class="demo-accounts">
                      <div class="title">Tài khoản demo &nbsp;|&nbsp; Mật khẩu: <b>Admin@123</b></div>
                      <div class="demo-item">
                        <span class="role">🛡️ Quản trị viên</span>
                        <span class="cred">admin</span>
                        <button class="fill-btn" onclick="fillLogin('admin')">Dùng</button>
                      </div>
                      <div class="demo-item">
                        <span class="role">👨‍👩‍👧 Phụ huynh</span>
                        <span class="cred">parent01</span>
                        <button class="fill-btn" onclick="fillLogin('parent01')">Dùng</button>
                      </div>
                      <div class="demo-item">
                        <span class="role">📋 Quản lý xe</span>
                        <span class="cred">monitor01</span>
                        <button class="fill-btn" onclick="fillLogin('monitor01')">Dùng</button>
                      </div>
                      <div class="demo-item">
                        <span class="role">🚗 Lái xe</span>
                        <span class="cred">driver01</span>
                        <button class="fill-btn" onclick="fillLogin('driver01')">Dùng</button>
                      </div>
                    </div>
      </div>
    </div>

    <script>
      function fillLogin(username) {
        document.getElementById('username').value = username;
        document.getElementById('password').value = 'Admin@123';
        document.getElementById('username').focus();
      }
      function togglePw() {
        const pw = document.getElementById('password');
        pw.type = pw.type === 'password' ? 'text' : 'password';
      }
    </script>
  </body>

  </html>