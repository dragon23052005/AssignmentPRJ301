<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Báo nghỉ - BusTrack</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            </head>

            <body>
                <div class="app-layout">
                    <%@ include file="../common/sidebar.jsp" %>
                        <div class="main-content">
                            <div class="topbar">
                                <div class="topbar-title">📣 Thông báo nghỉ xe buýt</div>
                            </div>
                            <div class="page-content">
                                <c:if test="${msg eq 'sent'}">
                                    <div class="alert alert-success">✅ Đã gửi thông báo nghỉ thành công!</div>
                                </c:if>
                                <c:if test="${msg eq 'error'}">
                                    <div class="alert alert-danger">❌ Gửi thông báo thất bại!</div>
                                </c:if>

                                <div class="grid-2">
                                    <div class="card">
                                        <div class="card-title" style="margin-bottom:20px;">📣 Gửi thông báo nghỉ</div>
                                        <div class="alert alert-info" style="margin-bottom:20px;">
                                            💡 Vui lòng báo nghỉ trước giờ xe khởi hành để quản lý xe kịp cập nhật danh
                                            sách.
                                        </div>
                                        <form method="POST" action="${pageContext.request.contextPath}/parent/absence">
                                            <div class="form-group">
                                                <label class="form-label">Học sinh *</label>
                                                <select name="studentId" class="form-control" required>
                                                    <c:forEach var="s" items="${students}">
                                                        <option value="${s.id}">${s.fullName} - Lớp ${s.className}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="form-group">
                                                <label class="form-label">Ngày nghỉ *</label>
                                                <input name="absenceDate" type="date" class="form-control" required
                                                    id="dateInput">
                                            </div>
                                            <div class="form-group">
                                                <label class="form-label">Lý do</label>
                                                <textarea name="reason" class="form-control" rows="3"
                                                    placeholder="VD: Ốm bệnh, bận việc gia đình..."></textarea>
                                            </div>
                                            <button type="submit" class="btn btn-warning w-full">📣 Gửi thông báo
                                                nghỉ</button>
                                        </form>
                                    </div>

                                    <div class="card">
                                        <div class="card-title" style="margin-bottom:16px;">📋 Lịch sử thông báo</div>
                                        <c:choose>
                                            <c:when test="${empty notifications}">
                                                <div class="empty-state">
                                                    <div class="empty-icon">📋</div>
                                                    <p>Chưa có thông báo nghỉ nào</p>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div
                                                    style="display:flex;flex-direction:column;gap:10px;max-height:480px;overflow-y:auto;">
                                                    <c:forEach var="an" items="${notifications}">
                                                        <div
                                                            style="padding:14px;background:rgba(255,255,255,0.03);border-radius:10px;border:1px solid var(--border);">
                                                            <div
                                                                style="display:flex;justify-content:space-between;align-items:flex-start;">
                                                                <div>
                                                                    <div style="font-weight:600;">${an.studentName}
                                                                    </div>
                                                                    <div class="text-sm text-muted">
                                                                        📅
                                                                        <fmt:formatDate value="${an.absenceDate}"
                                                                            pattern="dd/MM/yyyy" />
                                                                    </div>
                                                                    <c:if test="${not empty an.reason}">
                                                                        <div class="text-sm"
                                                                            style="margin-top:4px;color:var(--text-secondary);">
                                                                            ${an.reason}</div>
                                                                    </c:if>
                                                                </div>
                                                                <span
                                                                    class="badge ${an.status eq 'ACKNOWLEDGED' ? 'badge-success' : 'badge-warning'}">
                                                                    ${an.getStatusDisplay()}
                                                                </span>
                                                            </div>
                                                            <div class="text-xs text-muted" style="margin-top:8px;">Báo
                                                                lúc: ${an.notifiedAt}</div>
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                </div>
                <script>
                    const today = new Date().toISOString().split('T')[0];
                    document.getElementById('dateInput').value = today;
                    document.getElementById('dateInput').min = today;
                </script>
            </body>

            </html>