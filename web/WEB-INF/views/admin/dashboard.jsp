<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Dashboard Admin - BusTrack School</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            </head>

            <body>
                <div class="app-layout">
                    <%@ include file="../common/sidebar.jsp" %>

                        <div class="main-content">
                            <div class="topbar">
                                <div class="topbar-title">📊 Tổng quan hệ thống</div>
                                <div class="topbar-actions">
                                    <span class="text-sm text-muted">
                                        <fmt:formatDate value="<%= new java.util.Date() %>"
                                            pattern="EEEE, dd/MM/yyyy" />
                                    </span>
                                </div>
                            </div>

                            <div class="page-content">
                                <!-- Stats Grid -->
                                <div class="stats-grid">
                                    <div class="stat-card">
                                        <div class="stat-icon blue">🗺️</div>
                                        <div>
                                            <div class="stat-value">${totalRoutes}</div>
                                            <div class="stat-label">Tuyến xe</div>
                                        </div>
                                    </div>
                                    <div class="stat-card">
                                        <div class="stat-icon green">🚌</div>
                                        <div>
                                            <div class="stat-value">${totalVehicles}</div>
                                            <div class="stat-label">Xe buýt</div>
                                        </div>
                                    </div>
                                    <div class="stat-card">
                                        <div class="stat-icon cyan">🎒</div>
                                        <div>
                                            <div class="stat-value">${totalStudents}</div>
                                            <div class="stat-label">Học sinh</div>
                                        </div>
                                    </div>
                                    <div class="stat-card">
                                        <div class="stat-icon purple">👨‍👩‍👧</div>
                                        <div>
                                            <div class="stat-value">${totalParents}</div>
                                            <div class="stat-label">Phụ huynh</div>
                                        </div>
                                    </div>
                                    <div class="stat-card">
                                        <div class="stat-icon yellow">📅</div>
                                        <div>
                                            <div class="stat-value">${todayTrips}</div>
                                            <div class="stat-label">Chuyến hôm nay</div>
                                        </div>
                                    </div>
                                    <div class="stat-card">
                                        <div class="stat-icon red">📣</div>
                                        <div>
                                            <div class="stat-value">${pendingAbsences}</div>
                                            <div class="stat-label">Báo nghỉ chờ xử lý</div>
                                        </div>
                                    </div>
                                    <div class="stat-card">
                                        <div class="stat-icon blue">🚗</div>
                                        <div>
                                            <div class="stat-value">${totalDrivers}</div>
                                            <div class="stat-label">Lái xe</div>
                                        </div>
                                    </div>
                                    <div class="stat-card">
                                        <div class="stat-icon red">⚠️</div>
                                        <div>
                                            <div class="stat-value">${openReports}</div>
                                            <div class="stat-label">Sự cố xe đang mở</div>
                                        </div>
                                    </div>
                                </div>

                                <div class="grid-2">
                                    <!-- Recent Trips -->
                                    <div class="card">
                                        <div class="card-header">
                                            <div>
                                                <div class="card-title">🚦 Chuyến xe gần đây</div>
                                                <div class="card-subtitle">10 chuyến mới nhất</div>
                                            </div>
                                            <a href="${pageContext.request.contextPath}/admin/trips"
                                                class="btn btn-ghost btn-sm">Xem tất cả</a>
                                        </div>
                                        <c:choose>
                                            <c:when test="${empty recentTrips}">
                                                <div class="empty-state">
                                                    <div class="empty-icon">🚌</div>
                                                    <p>Chưa có chuyến xe nào</p>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="table-wrap">
                                                    <table>
                                                        <thead>
                                                            <tr>
                                                                <th>Tuyến</th>
                                                                <th>Ngày</th>
                                                                <th>Ca</th>
                                                                <th>Trạng thái</th>
                                                                <th>Điểm danh</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <c:forEach var="t" items="${recentTrips}">
                                                                <tr>
                                                                    <td><strong>${t.routeName}</strong><br><span
                                                                            class="text-muted text-xs">${t.plateNumber}</span>
                                                                    </td>
                                                                    <td class="text-sm">
                                                                        <fmt:formatDate value="${t.tripDate}"
                                                                            pattern="dd/MM" />
                                                                    </td>
                                                                    <td><span
                                                                            class="badge ${t.tripType eq 'MORNING' ? 'badge-info' : 'badge-warning'}">${t.getTripTypeDisplay()}</span>
                                                                    </td>
                                                                    <td><span
                                                                            class="badge ${t.status eq 'COMPLETED' ? 'badge-success' : t.status eq 'DEPARTED' ? 'badge-warning' : 'badge-neutral'}">${t.getStatusDisplay()}</span>
                                                                    </td>
                                                                    <td class="text-sm">
                                                                        ${t.presentCount}/${t.totalCount}</td>
                                                                </tr>
                                                            </c:forEach>
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <!-- Today Absences -->
                                    <div class="card">
                                        <div class="card-header">
                                            <div>
                                                <div class="card-title">📣 Báo nghỉ hôm nay</div>
                                                <div class="card-subtitle">Học sinh báo vắng</div>
                                            </div>
                                        </div>
                                        <c:choose>
                                            <c:when test="${empty todayAbsences}">
                                                <div class="empty-state">
                                                    <div class="empty-icon">✅</div>
                                                    <p>Không có học sinh báo nghỉ hôm nay</p>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="table-wrap">
                                                    <table>
                                                        <thead>
                                                            <tr>
                                                                <th>Học sinh</th>
                                                                <th>Lớp</th>
                                                                <th>Tuyến</th>
                                                                <th>Trạng thái</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <c:forEach var="an" items="${todayAbsences}">
                                                                <tr>
                                                                    <td><strong>${an.studentName}</strong><br><span
                                                                            class="text-muted text-xs">${an.studentCode}</span>
                                                                    </td>
                                                                    <td class="text-sm">${an.className}</td>
                                                                    <td class="text-sm">${an.routeName}</td>
                                                                    <td><span
                                                                            class="badge ${an.status eq 'ACKNOWLEDGED' ? 'badge-success' : 'badge-warning'}">${an.getStatusDisplay()}</span>
                                                                    </td>
                                                                </tr>
                                                            </c:forEach>
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <!-- Quick Links -->
                                <div class="card mt-4">
                                    <div class="card-header">
                                        <div class="card-title">⚡ Truy cập nhanh</div>
                                    </div>
                                    <div style="display:flex;gap:12px;flex-wrap:wrap;">
                                        <a href="${pageContext.request.contextPath}/admin/users"
                                            class="btn btn-primary">👥 Thêm người dùng</a>
                                        <a href="${pageContext.request.contextPath}/admin/routes"
                                            class="btn btn-info">🗺️ Quản lý tuyến xe</a>
                                        <a href="${pageContext.request.contextPath}/admin/vehicles"
                                            class="btn btn-success">🚌 Quản lý xe</a>
                                        <a href="${pageContext.request.contextPath}/admin/students"
                                            class="btn btn-warning">🎒 Quản lý học sinh</a>
                                        <a href="${pageContext.request.contextPath}/admin/trips"
                                            class="btn btn-ghost">📅 Xem chuyến xe</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                </div>
            </body>

            </html>