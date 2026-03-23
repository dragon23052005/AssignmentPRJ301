<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Báo cáo sự cố xe - BusTrack</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            </head>

            <body>
                <div class="app-layout">
                    <%@ include file="../common/sidebar.jsp" %>
                        <div class="main-content">
                            <div class="topbar">
                                <div class="topbar-title">⚠️ Báo cáo Sự cố Xe</div>
                            </div>
                            <div class="page-content">
                                <c:if test="${msg eq 'reported'}">
                                    <div class="alert alert-success">✅ Đã gửi báo cáo sự cố thành công!</div>
                                </c:if>
                                <c:if test="${msg eq 'error'}">
                                    <div class="alert alert-danger">❌ Gửi báo cáo thất bại!</div>
                                </c:if>
                                <c:if test="${msg eq 'noVehicle'}">
                                    <div class="alert alert-warning">⚠️ Bạn chưa được phân công xe!</div>
                                </c:if>

                                <div class="grid-2">
                                    <!-- Report Form -->
                                    <div class="card">
                                        <div class="card-title" style="margin-bottom:20px;">🔧 Báo cáo sự cố mới</div>
                                        <c:choose>
                                            <c:when test="${empty vehicle}">
                                                <div class="alert alert-warning">Bạn chưa được phân công xe nào.</div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="alert alert-info" style="margin-bottom:20px;">
                                                    Xe: <strong>${vehicle.plateNumber}</strong> - ${vehicle.brand}
                                                    ${vehicle.model}
                                                </div>
                                                <form method="POST"
                                                    action="${pageContext.request.contextPath}/driver/report">
                                                    <input type="hidden" name="action" value="add">
                                                    <div class="form-group">
                                                        <label class="form-label">Loại sự cố *</label>
                                                        <select name="issueType" class="form-control" required>
                                                            <option value="MECHANICAL">⚙️ Cơ học</option>
                                                            <option value="TIRE">🔄 Lốp xe</option>
                                                            <option value="BRAKE">🛑 Phanh</option>
                                                            <option value="ENGINE">🔧 Động cơ</option>
                                                            <option value="ELECTRICAL">⚡ Điện</option>
                                                            <option value="BODY">🚗 Thân xe</option>
                                                            <option value="OTHER">📋 Khác</option>
                                                        </select>
                                                    </div>
                                                    <div class="form-group">
                                                        <label class="form-label">Mức độ nghiêm trọng *</label>
                                                        <select name="severity" class="form-control" required>
                                                            <option value="LOW">🟢 Thấp - Vẫn có thể hoạt động</option>
                                                            <option value="MEDIUM">🟡 Trung bình - Cần sửa sớm</option>
                                                            <option value="HIGH">🔴 Cao - Cần sửa ngay</option>
                                                            <option value="CRITICAL">⛔ Nghiêm trọng - Không thể hoạt
                                                                động</option>
                                                        </select>
                                                    </div>
                                                    <div class="form-group">
                                                        <label class="form-label">Mô tả chi tiết *</label>
                                                        <textarea name="description" class="form-control" rows="4"
                                                            required
                                                            placeholder="Mô tả vấn đề gặp phải, vị trí sự cố, thời điểm phát hiện..."></textarea>
                                                    </div>
                                                    <button type="submit" class="btn btn-warning w-full">⚠️ Gửi báo
                                                        cáo</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <!-- Reports List -->
                                    <div class="card">
                                        <div class="card-title" style="margin-bottom:16px;">📋 Lịch sử báo cáo</div>
                                        <c:choose>
                                            <c:when test="${empty reports}">
                                                <div class="empty-state">
                                                    <div class="empty-icon">✅</div>
                                                    <p>Chưa có báo cáo nào</p>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div
                                                    style="display:flex;flex-direction:column;gap:10px;max-height:600px;overflow-y:auto;">
                                                    <c:forEach var="r" items="${reports}">
                                                        <div
                                                            style="padding:14px;border-radius:10px;border:1px solid var(--border);
                            border-left:4px solid ${r.severity eq 'CRITICAL' ? '#EF4444' : r.severity eq 'HIGH' ? '#F59E0B' : r.severity eq 'MEDIUM' ? '#06B6D4' : '#10B981'};">
                                                            <div
                                                                style="display:flex;justify-content:space-between;align-items:flex-start;">
                                                                <div>
                                                                    <div style="font-weight:700;">
                                                                        ${r.getIssueTypeDisplay()}</div>
                                                                    <div class="text-xs text-muted">
                                                                        <fmt:formatDate value="${r.reportDate}"
                                                                            pattern="dd/MM/yyyy" />
                                                                    </div>
                                                                </div>
                                                                <div
                                                                    style="display:flex;gap:6px;flex-direction:column;align-items:flex-end;">
                                                                    <span
                                                                        class="${r.severityClass} text-xs fw-bold">${r.getSeverityDisplay()}</span>
                                                                    <span
                                                                        class="badge ${r.status eq 'RESOLVED' ? 'badge-success' : r.status eq 'IN_PROGRESS' ? 'badge-warning' : 'badge-danger'}"
                                                                        style="font-size:10px;">${r.getStatusDisplay()}</span>
                                                                </div>
                                                            </div>
                                                            <div class="text-sm text-muted" style="margin-top:8px;">
                                                                ${r.description}</div>
                                                            <c:if test="${not empty r.resolvedAt}">
                                                                <div class="text-xs text-success"
                                                                    style="margin-top:4px;">✅ Giải quyết lúc:
                                                                    ${r.resolvedAt}</div>
                                                            </c:if>
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
            </body>

            </html>