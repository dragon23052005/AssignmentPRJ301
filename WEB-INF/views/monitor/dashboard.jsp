<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Quản lý xe - Dashboard - BusTrack</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            </head>

            <body>
                <div class="app-layout">
                    <%@ include file="../common/sidebar.jsp" %>
                        <div class="main-content">
                            <div class="topbar">
                                <div class="topbar-title">📊 Dashboard Quản lý xe</div>
                                <div class="topbar-actions">
                                    <a href="${pageContext.request.contextPath}/monitor/trip"
                                        class="btn btn-primary btn-sm">🚦 Quản lý chuyến</a>
                                </div>
                            </div>
                            <div class="page-content">
                                <c:if test="${param.msg eq 'ack'}">
                                    <div class="alert alert-success">✅ Đã xác nhận thông báo nghỉ của học sinh!</div>
                                </c:if>
                                <c:choose>
                                    <c:when test="${empty vehicle}">
                                        <div class="card text-center">
                                            <div class="empty-state">
                                                <div class="empty-icon">🚌</div>
                                                <p>Bạn chưa được phân công xe nào. Vui lòng liên hệ Admin.</p>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <!-- Vehicle Info -->
                                        <div class="card mb-4">
                                            <div style="display:flex;align-items:center;gap:20px;flex-wrap:wrap;">
                                                <div
                                                    style="width:64px;height:64px;border-radius:16px;background:linear-gradient(135deg,var(--primary),var(--secondary));display:flex;align-items:center;justify-content:center;font-size:32px;">
                                                    🚌</div>
                                                <div style="flex:1;">
                                                    <div style="font-size:22px;font-weight:800;letter-spacing:1px;">
                                                        ${vehicle.plateNumber}</div>
                                                    <div class="text-muted text-sm">${vehicle.brand} ${vehicle.model} •
                                                        ${vehicle.capacity} chỗ</div>
                                                    <div class="text-sm" style="margin-top:4px;">Tuyến:
                                                        <strong>${vehicle.routeName}</strong></div>
                                                </div>
                                                <div>
                                                    <span
                                                        class="badge ${vehicle.active ? 'badge-success' : 'badge-danger'}"
                                                        style="font-size:13px;padding:6px 14px;">
                                                        ${vehicle.active ? '✅ Hoạt động' : '⛔ Ngừng'}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="grid-2">
                                            <!-- Today Trips -->
                                            <div class="card">
                                                <div class="card-header">
                                                    <div class="card-title">📅 Chuyến hôm nay</div>
                                                    <a href="${pageContext.request.contextPath}/monitor/trip"
                                                        class="btn btn-primary btn-sm">+ Tạo chuyến</a>
                                                </div>
                                                <c:choose>
                                                    <c:when test="${empty todayTrips}">
                                                        <div class="empty-state">
                                                            <div class="empty-icon">📅</div>
                                                            <p>Chưa có chuyến nào hôm nay</p>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:forEach var="t" items="${todayTrips}">
                                                            <div
                                                                style="padding:14px;background:rgba(255,255,255,0.03);border-radius:10px;border:1px solid var(--border);margin-bottom:10px;">
                                                                <div
                                                                    style="display:flex;justify-content:space-between;align-items:flex-start;">
                                                                    <div>
                                                                        <span
                                                                            class="badge ${t.tripType eq 'MORNING' ? 'badge-info' : 'badge-warning'}">${t.getTripTypeDisplay()}</span>
                                                                        <div class="text-sm" style="margin-top:8px;">
                                                                            Điểm danh:
                                                                            <strong>${t.presentCount}/${t.totalCount}</strong>
                                                                            học sinh</div>
                                                                        <c:if test="${not empty t.departedAt}">
                                                                            <div class="text-xs text-muted">Khởi hành:
                                                                                ${t.departedAt}</div>
                                                                        </c:if>
                                                                    </div>
                                                                    <span
                                                                        class="badge ${t.status eq 'COMPLETED' ? 'badge-success' : t.status eq 'DEPARTED' ? 'badge-warning' : 'badge-neutral'}">${t.getStatusDisplay()}</span>
                                                                </div>
                                                                <div style="margin-top:10px;">
                                                                    <a href="${pageContext.request.contextPath}/monitor/trip?tripId=${t.id}"
                                                                        class="btn btn-ghost btn-sm">📋 Chi tiết</a>
                                                                </div>
                                                            </div>
                                                        </c:forEach>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <!-- Today Absences -->
                                            <div class="card">
                                                <div class="card-header">
                                                    <div class="card-title">📣 Báo nghỉ hôm nay</div>
                                                </div>
                                                <c:choose>
                                                    <c:when test="${empty todayAbsences}">
                                                        <div class="empty-state">
                                                            <div class="empty-icon">✅</div>
                                                            <p>Không có học sinh báo nghỉ</p>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:forEach var="an" items="${todayAbsences}">
                                                            <div
                                                                style="display:flex;align-items:center;gap:12px;padding:10px;background:rgba(239,68,68,0.05);border-radius:8px;border:1px solid rgba(239,68,68,0.2);margin-bottom:8px;">
                                                                <span style="font-size:24px;">🎒</span>
                                                                <div style="flex:1;">
                                                                    <div style="font-weight:600;font-size:13px;">
                                                                        ${an.studentName}</div>
                                                                    <div class="text-xs text-muted">${an.className} •
                                                                        ${an.reason}</div>
                                                                </div>
                                                                <div style="display:flex;flex-direction:column;align-items:flex-end;gap:5px;">
                                                                    <span
                                                                        class="badge ${an.status eq 'ACKNOWLEDGED' ? 'badge-success' : 'badge-warning'}">${an.getStatusDisplay()}</span>
                                                                    <c:if test="${an.status eq 'PENDING'}">
                                                                        <a href="${pageContext.request.contextPath}/monitor/dashboard?action=acknowledge&id=${an.id}" 
                                                                           class="btn btn-primary btn-sm" style="font-size:11px;padding:3px 8px;">Xác nhận</a>
                                                                    </c:if>
                                                                </div>
                                                            </div>
                                                        </c:forEach>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>

                                        <!-- Active Trip Attendance Preview -->
                                        <c:if test="${not empty activeTrip and not empty attendances}">
                                            <div class="card mt-4">
                                                <div class="card-header">
                                                    <div>
                                                        <div class="card-title">👥 Điểm danh -
                                                            ${activeTrip.getTripTypeDisplay()}</div>
                                                        <div class="card-subtitle">${activeTrip.getStatusDisplay()} •
                                                            ${activeTrip.presentCount}/${activeTrip.totalCount} lên xe
                                                        </div>
                                                    </div>
                                                    <a href="${pageContext.request.contextPath}/monitor/trip?tripId=${activeTrip.id}"
                                                        class="btn btn-primary btn-sm">📋 Xem chi tiết</a>
                                                </div>
                                                <div
                                                    style="display:grid;grid-template-columns:repeat(auto-fill,minmax(220px,1fr));gap:10px;">
                                                    <c:forEach var="a" items="${attendances}" end="7">
                                                        <div
                                                            class="student-card ${a.status.toLowerCase().replace('_','-')}">
                                                            <div class="student-avatar">🎒</div>
                                                            <div style="flex:1;">
                                                                <div class="student-name">${a.studentName}</div>
                                                                <div class="student-meta">${a.className}</div>
                                                            </div>
                                                            <span
                                                                class="badge ${a.status eq 'PRESENT' ? 'badge-success' : a.status eq 'NOTIFIED_ABSENT' ? 'badge-info' : 'badge-danger'}"
                                                                style="font-size:11px;">
                                                                ${a.getStatusDisplay()}
                                                            </span>
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                </div>
            </body>

            </html>