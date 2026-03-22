<%@ page contentType="text/html;charset=UTF-8" %>
    <!DOCTYPE html>
    <html lang="vi">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Truy cập bị từ chối</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    </head>

    <body>
        <div style="display:flex;align-items:center;justify-content:center;min-height:100vh;">
            <div style="text-align:center;padding:48px;">
                <div style="font-size:72px;margin-bottom:16px;">🚫</div>
                <h1 style="font-size:28px;margin-bottom:8px;">Truy cập bị từ chối</h1>
                <p style="color:var(--text-muted);margin-bottom:24px;">Bạn không có quyền truy cập trang này.</p>
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary">← Về trang chủ</a>
            </div>
        </div>
    </body>

    </html>