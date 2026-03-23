<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Dashboard Lái xe - BusTrack</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            </head>

            <body>
                <div class="app-layout">
                    <%@ include file="../common/sidebar.jsp" %>
                        <div class="main-content">
                            <div class="topbar">
                                <div class="topbar-title">🚗 Dashboard Lái xe</div>
                                <div class="topbar-actions">
                                    <a href="${pageContext.request.contextPath}/driver/report"
                                        class="btn btn-warning btn-sm">⚠️ Báo cáo sự cố</a>
                                </div>
                            </div>
                            <div class="page-content">
                                <c:if test="${msg eq 'reported'}">
                                    <div class="alert alert-success">✅ Đã gửi báo cáo sự cố!</div>
                                </c:if>

                                <c:choose>
                                    <c:when test="${empty vehicle}">
                                        <div class="card">
                                            <div class="empty-state">
                                                <div class="empty-icon">🚌</div>
                                                <p>Bạn chưa được phân công xe. Liên hệ Admin.</p>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <!-- Vehicle Info -->
                                        <div class="card mb-4"
                                            style="background:linear-gradient(135deg,rgba(79,70,229,0.1),rgba(14,165,233,0.08));border-color:rgba(79,70,229,0.3);">
                                            <div style="display:flex;align-items:center;gap:20px;flex-wrap:wrap;">
                                                <div style="font-size:56px;">🚌</div>
                                                <div style="flex:1;">
                                                    <div style="font-size:28px;font-weight:800;letter-spacing:2px;">
                                                        ${vehicle.plateNumber}</div>
                                                    <div class="text-muted">${vehicle.brand} ${vehicle.model} •
                                                        ${vehicle.capacity} chỗ ngồi</div>
                                                    <div style="margin-top:8px;">
                                                        Tuyến: <strong
                                                            style="color:var(--primary-light);">${vehicle.routeName}</strong>
                                                    </div>
                                                    <div class="text-sm text-muted">Quản lý xe: ${vehicle.monitorName}
                                                    </div>
                                                </div>
                                                <span class="badge ${vehicle.active ? 'badge-success' : 'badge-danger'}"
                                                    style="font-size:14px;padding:8px 18px;">
                                                    ${vehicle.active ? '✅ Đang hoạt động' : '⛔ Ngừng hoạt động'}
                                                </span>
                                            </div>
                                        </div>

                                        <!-- Today Trips -->
                                        <div class="card mb-4">
                                            <div class="card-header">
                                                <div class="card-title">📅 Lịch trình hôm nay</div>
                                                <span class="text-muted text-sm">
                                                    <fmt:formatDate value="<%= new java.util.Date() %>"
                                                        pattern="EEEE, dd/MM/yyyy" />
                                                </span>
                                            </div>
                                            <c:choose>
                                                <c:when test="${empty todayTrips}">
                                                    <div class="empty-state">
                                                        <div class="empty-icon">📅</div>
                                                        <p>Chưa có lịch trình hôm nay</p>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div style="display:flex;gap:16px;flex-wrap:wrap;">
                                                        <c:forEach var="t" items="${todayTrips}">
                                                            <div
                                                                style="flex:1;min-width:200px;padding:20px;background:rgba(255,255,255,0.04);border-radius:12px;border:1px solid var(--border);text-align:center;">
                                                                <div style="font-size:32px;margin-bottom:8px;">
                                                                    ${t.tripType eq 'MORNING' ? '🌅' : '🌇'}</div>
                                                                <div style="font-size:16px;font-weight:700;">
                                                                    ${t.getTripTypeDisplay()}</div>
                                                                <span
                                                                    class="badge ${t.status eq 'COMPLETED' ? 'badge-success' : t.status eq 'DEPARTED' ? 'badge-warning' : 'badge-neutral'}"
                                                                    style="margin-top:8px;">
                                                                    ${t.getStatusDisplay()}
                                                                </span>
                                                                <c:if test="${not empty t.departedAt}">
                                                                    <div class="text-xs text-muted"
                                                                        style="margin-top:8px;">Khởi hành:
                                                                        ${t.departedAt}</div>
                                                                </c:if>
                                                                <c:if test="${not empty t.arrivedAt}">
                                                                    <div class="text-xs text-success"
                                                                        style="margin-top:4px;">Đến: ${t.arrivedAt}
                                                                    </div>
                                                                </c:if>
                                                                <div class="text-sm" style="margin-top:8px;">👥
                                                                    ${t.presentCount}/${t.totalCount}</div>
                                                            </div>
                                                        </c:forEach>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                        <!-- Vehicle Reports -->
                                        <div class="card">
                                            <div class="card-header">
                                                <div class="card-title">📋 Báo cáo sự cố gần đây</div>
                                                <a href="${pageContext.request.contextPath}/driver/report"
                                                    class="btn btn-warning btn-sm">+ Báo cáo mới</a>
                                            </div>
                                            <c:choose>
                                                <c:when test="${empty vehicleReports}">
                                                    <div class="empty-state">
                                                        <div class="empty-icon">✅</div>
                                                        <p>Chưa có sự cố nào</p>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="table-wrap">
                                                        <table>
                                                            <thead>
                                                                <tr>
                                                                    <th>Ngày</th>
                                                                    <th>Loại sự cố</th>
                                                                    <th>Mức độ</th>
                                                                    <th>Mô tả</th>
                                                                    <th>Trạng thái</th>
                                                                </tr>
                                                            </thead>
                                                            <tbody>
                                                                <c:forEach var="r" items="${vehicleReports}">
                                                                    <tr>
                                                                        <td class="text-sm">
                                                                            <fmt:formatDate value="${r.reportDate}"
                                                                                pattern="dd/MM" />
                                                                        </td>
                                                                        <td>${r.getIssueTypeDisplay()}</td>
                                                                        <td><span
                                                                                class="${r.severityClass}">${r.getSeverityDisplay()}</span>
                                                                        </td>
                                                                        <td class="text-sm text-muted">${r.description}
                                                                        </td>
                                                                        <td><span
                                                                                class="badge ${r.status eq 'RESOLVED' ? 'badge-success' : r.status eq 'IN_PROGRESS' ? 'badge-warning' : 'badge-danger'}">${r.getStatusDisplay()}</span>
                                                                        </td>
                                                                    </tr>
                                                                </c:forEach>
                                                            </tbody>
                                                        </table>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                </div>
            </body>

            </html>