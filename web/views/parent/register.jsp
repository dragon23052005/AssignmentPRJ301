<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Đăng ký xe buýt - BusTrack</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            </head>

            <body>
                <div class="app-layout">
                    <%@ include file="../common/sidebar.jsp" %>
                        <div class="main-content">
                            <div class="topbar">
                                <div class="topbar-title">📝 Đăng ký xe buýt</div>
                            </div>
                            <div class="page-content">
                                <c:if test="${msg eq 'registered'}">
                                    <div class="alert alert-success">✅ Đăng ký xe buýt thành công!</div>
                                </c:if>
                                <c:if test="${msg eq 'cancelled'}">
                                    <div class="alert alert-info">🔕 Đã hủy đăng ký.</div>
                                </c:if>
                                <c:if test="${msg eq 'error'}">
                                    <div class="alert alert-danger">❌ Có lỗi xảy ra. Vui lòng thử lại.</div>
                                </c:if>

                                <div class="grid-2">
                                    <!-- Register Form -->
                                    <div class="card">
                                        <div class="card-title" style="margin-bottom:20px;">🚌 Đăng ký tuyến xe</div>
                                        <form method="POST" action="${pageContext.request.contextPath}/parent/register">
                                            <div class="form-group">
                                                <label class="form-label">Chọn học sinh *</label>
                                                <select name="studentId" class="form-control" required>
                                                    <option value="">-- Chọn học sinh --</option>
                                                    <c:forEach var="s" items="${students}">
                                                        <option value="${s.id}">${s.fullName} (${s.className})</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="form-group">
                                                <label class="form-label">Tuyến xe *</label>
                                                <select name="routeId" id="routeSelect" class="form-control" required
                                                    onchange="loadStops()">
                                                    <option value="">-- Chọn tuyến xe --</option>
                                                    <c:forEach var="r" items="${routes}">
                                                        <option value="${r.id}">${r.routeCode} - ${r.routeName}
                                                            (Sáng: ${r.morningDeparture} / Chiều:
                                                            ${r.afternoonDeparture})</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="form-group">
                                                <label class="form-label">Điểm đón/trả *</label>
                                                <select name="stopId" id="stopSelect" class="form-control" required>
                                                    <option value="">-- Chọn tuyến trước --</option>
                                                </select>
                                            </div>
                                            <div class="form-group">
                                                <label class="form-label">Ngày bắt đầu *</label>
                                                <input name="startDate" type="date" class="form-control" required
                                                    value="${pageContext.request.contextPath}">
                                            </div>
                                            <button type="submit" class="btn btn-primary w-full">Đăng ký xe
                                                buýt</button>
                                        </form>
                                    </div>

                                    <!-- Current Registration -->
                                    <div class="card">
                                        <div class="card-title" style="margin-bottom:16px;">📋 Đăng ký hiện tại</div>
                                        <c:choose>
                                            <c:when test="${empty students}">
                                                <div class="empty-state">
                                                    <div class="empty-icon">📝</div>
                                                    <p>Không có học sinh nào</p>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach var="s" items="${students}">
                                                    <div
                                                        style="background:rgba(255,255,255,0.03);border-radius:10px;padding:16px;margin-bottom:12px;border:1px solid var(--border);">
                                                        <div
                                                            style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:12px;">
                                                            <div>
                                                                <div style="font-weight:700;font-size:15px;">
                                                                    ${s.fullName}</div>
                                                                <div class="text-xs text-muted">Lớp ${s.className} •
                                                                    ${s.studentCode}</div>
                                                            </div>
                                                            <span
                                                                class="badge ${s.registrationActive ? 'badge-success' : 'badge-neutral'}">
                                                                ${s.registrationActive ? '✅ Đang đăng ký' : '⭕ Chưa đăng
                                                                ký'}
                                                            </span>
                                                        </div>
                                                        <c:if test="${s.routeId > 0}">
                                                            <div
                                                                style="display:flex;flex-direction:column;gap:6px;font-size:13px;">
                                                                <div>🗺️ <strong>${s.routeName}</strong></div>
                                                                <div>📍 ${s.stopName}</div>
                                                            </div>
                                                            <a href="${pageContext.request.contextPath}/parent/register?action=cancel&studentId=${s.id}"
                                                                class="btn btn-danger btn-sm" style="margin-top:12px;"
                                                                onclick="return confirm('Hủy đăng ký xe buýt cho ${s.fullName}?')">
                                                                🚫 Hủy đăng ký
                                                            </a>
                                                        </c:if>
                                                    </div>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                </div>
                <script>
                    const ctx = '${pageContext.request.contextPath}';
                    const today = new Date().toISOString().split('T')[0];
                    document.querySelector('input[name=startDate]').value = today;

                    function loadStops() {
                        const rid = document.getElementById('routeSelect').value;
                        const sel = document.getElementById('stopSelect');
                        sel.innerHTML = '<option>Đang tải...</option>';
                        if (!rid) { sel.innerHTML = '<option value="">-- Chọn tuyến trước --</option>'; return; }
                        fetch(ctx + '/parent/register?action=stops&routeId=' + rid)
                            .then(r => r.json())
                            .then(stops => {
                                sel.innerHTML = '<option value="">-- Chọn điểm đón --</option>';
                                stops.forEach(s => sel.add(new Option(s.stopName + (s.address ? ' - ' + s.address : ''), s.id)));
                            });
                    }
                </script>
            </body>

            </html>