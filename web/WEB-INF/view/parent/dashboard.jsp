<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Dashboard Phụ huynh - BusTrack</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                <style>
                    .status-card {
                        padding: 20px;
                        border-radius: 12px;
                        border: 1px solid var(--border);
                        margin-bottom: 16px;
                    }

                    .status-card.on-bus {
                        border-color: rgba(16, 185, 129, 0.5);
                        background: rgba(16, 185, 129, 0.05);
                    }

                    .status-card.not-boarding {
                        border-color: rgba(239, 68, 68, 0.3);
                        background: rgba(239, 68, 68, 0.04);
                    }

                    .status-card.notified {
                        border-color: rgba(6, 182, 212, 0.4);
                        background: rgba(6, 182, 212, 0.05);
                    }

                    .big-status-icon {
                        font-size: 52px;
                        margin-bottom: 8px;
                    }

                    .status-title {
                        font-size: 20px;
                        font-weight: 700;
                    }

                    .status-sub {
                        font-size: 13px;
                        color: var(--text-muted);
                        margin-top: 4px;
                    }

                    .child-tab {
                        display: flex;
                        gap: 10px;
                        margin-bottom: 20px;
                        flex-wrap: wrap;
                    }

                    .child-tab-item {
                        padding: 8px 16px;
                        border-radius: 20px;
                        cursor: pointer;
                        border: 1px solid var(--border);
                        font-size: 13px;
                        font-weight: 500;
                        transition: all 0.2s;
                    }

                    .child-tab-item.active {
                        background: var(--primary);
                        border-color: var(--primary);
                        color: white;
                    }

                    .child-tab-item:hover:not(.active) {
                        border-color: var(--border-light);
                    }

                    .trip-time-badge {
                        display: flex;
                        align-items: center;
                        gap: 8px;
                        padding: 10px 14px;
                        background: rgba(255, 255, 255, 0.04);
                        border-radius: 8px;
                    }
                </style>
            </head>

            <body>
                <div class="app-layout">
                    <%@ include file="../common/sidebar.jsp" %>
                        <div class="main-content">
                            <div class="topbar">
                                <div class="topbar-title">🏠 Theo dõi xe buýt</div>
                                <div class="topbar-actions">
                                    <a href="${pageContext.request.contextPath}/parent/absence"
                                        class="btn btn-warning btn-sm">📣 Báo nghỉ</a>
                                    <a href="${pageContext.request.contextPath}/parent/register"
                                        class="btn btn-ghost btn-sm">📝 Đăng ký xe</a>
                                </div>
                            </div>
                            <div class="page-content">

                                <!-- Child Selector -->
                                <c:if test="${not empty students}">
                                    <div class="child-tab">
                                        <c:forEach var="s" items="${students}">
                                            <a href="${pageContext.request.contextPath}/parent/dashboard?studentId=${s.id}"
                                                class="child-tab-item ${selectedStudent.id eq s.id ? 'active' : ''}">
                                                🧒 ${s.fullName}
                                            </a>
                                        </c:forEach>
                                    </div>
                                </c:if>

                                <c:choose>
                                    <c:when test="${empty students}">
                                        <div class="card text-center">
                                            <div class="empty-state">
                                                <div class="empty-icon">👨‍👩‍👧</div>
                                                <p>Chưa có thông tin học sinh. Vui lòng liên hệ nhà trường.</p>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="grid-2">
                                            <!-- Student Info -->
                                            <div class="card">
                                                <div class="card-title" style="margin-bottom:16px;">👤 Thông tin học
                                                    sinh</div>
                                                <div
                                                    style="display:flex;align-items:center;gap:16px;margin-bottom:20px;">
                                                    <div class="student-avatar"
                                                        style="width:56px;height:56px;font-size:24px;border-radius:14px;">
                                                        🎒</div>
                                                    <div>
                                                        <div style="font-size:18px;font-weight:700;">
                                                            ${selectedStudent.fullName}</div>
                                                        <div class="text-muted text-sm">${selectedStudent.studentCode} •
                                                            Lớp ${selectedStudent.className}</div>
                                                    </div>
                                                </div>
                                                <c:choose>
                                                    <c:when test="${selectedStudent.routeId > 0}">
                                                        <div style="display:flex;flex-direction:column;gap:10px;">
                                                            <div class="trip-time-badge">
                                                                <span>🚌</span>
                                                                <div>
                                                                    <div class="text-xs text-muted">Tuyến xe</div>
                                                                    <div style="font-weight:600;font-size:13px;">
                                                                        ${selectedStudent.routeName}</div>
                                                                </div>
                                                            </div>
                                                            <div class="trip-time-badge">
                                                                <span>📍</span>
                                                                <div>
                                                                    <div class="text-xs text-muted">Điểm đón</div>
                                                                    <div style="font-weight:600;font-size:13px;">
                                                                        ${selectedStudent.stopName}</div>
                                                                </div>
                                                            </div>
                                                            <div class="trip-time-badge">
                                                                <span>🌅</span>
                                                                <div>
                                                                    <div class="text-xs text-muted">Giờ đón buổi
                                                                        sáng (dự kiến)</div>
                                                                    <div
                                                                        style="font-weight:700;font-size:15px;color:var(--warning);">
                                                                        ${empty selectedStudent.estimatedMorningTime ? 'Chưa rõ' : selectedStudent.estimatedMorningTime}
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <div class="trip-time-badge">
                                                                <span>🌇</span>
                                                                <div>
                                                                    <div class="text-xs text-muted">Giờ trả buổi
                                                                        chiều (dự kiến)</div>
                                                                    <div
                                                                        style="font-weight:700;font-size:15px;color:var(--warning);">
                                                                        ${empty selectedStudent.estimatedAfternoonTime ? 'Chưa rõ' : selectedStudent.estimatedAfternoonTime}
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <c:if test="${not empty morningTrip and not empty morningTrip.departedAt}">
                                                                <div class="trip-time-badge" style="background: rgba(16, 185, 129, 0.1);">
                                                                    <span>🚌</span>
                                                                    <div>
                                                                        <div class="text-xs text-muted">Trạng thái xe đi</div>
                                                                        <div
                                                                            style="font-weight:700;font-size:13px;color:var(--success);">
                                                                            Đã rời bến lúc ${morningTrip.departedAt}
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </c:if>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="alert alert-warning">⚠️ Học sinh chưa đăng ký tuyến
                                                            xe. <a
                                                                href="${pageContext.request.contextPath}/parent/register"
                                                                style="color:var(--warning);text-decoration:underline;">Đăng
                                                                ký ngay</a></div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <!-- Today Status -->
                                            <div>
                                                <!-- Morning Status -->
                                                <c:choose>
                                                    <c:when test="${isAbsentToday}">
                                                        <div class="status-card notified">
                                                            <div class="big-status-icon">📣</div>
                                                            <div class="status-title" style="color:var(--info);">Đã báo
                                                                nghỉ hôm nay</div>
                                                            <div class="status-sub">Nhà trường đã được thông báo</div>
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${not empty morningAtt}">
                                                        <c:choose>
                                                            <c:when test="${morningAtt.status eq 'PRESENT'}">
                                                                <div class="status-card on-bus">
                                                                    <div class="big-status-icon">✅</div>
                                                                    <div class="status-title"
                                                                        style="color:var(--success);">Đã lên xe buổi
                                                                        sáng</div>
                                                                    <div class="status-sub">
                                                                        Lên tại: ${morningAtt.boardedStopName}<br>
                                                                        Lúc: ${morningAtt.boardedAt}
                                                                        <c:if test="${not empty morningAtt.alightedAt}">
                                                                            <br>Đến trường lúc: ${morningAtt.alightedAt}
                                                                        </c:if>
                                                                    </div>
                                                                </div>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="status-card not-boarding">
                                                                    <div class="big-status-icon">❓</div>
                                                                    <div class="status-title"
                                                                        style="color:var(--text-secondary);">Chưa lên xe
                                                                        buổi sáng</div>
                                                                    <div class="status-sub">Trạng thái:
                                                                        ${morningAtt.getStatusDisplay()}</div>
                                                                </div>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="status-card">
                                                            <div class="big-status-icon">⏳</div>
                                                            <div class="status-title">Chưa có thông tin hôm nay</div>
                                                            <div class="status-sub">Chuyến xe sáng chưa được tạo hoặc
                                                                chưa bắt đầu</div>
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>

                                                <!-- Afternoon Status -->
                                                <c:if test="${not empty afternoonAtt}">
                                                    <div
                                                        class="status-card ${afternoonAtt.status eq 'PRESENT' ? 'on-bus' : ''}">
                                                        <div style="font-weight:700;margin-bottom:8px;">🌇 Buổi chiều
                                                        </div>
                                                        <span
                                                            class="badge ${afternoonAtt.status eq 'PRESENT' ? 'badge-success' : 'badge-neutral'}">
                                                            ${afternoonAtt.getStatusDisplay()}
                                                        </span>
                                                        <c:if test="${not empty afternoonAtt.boardedAt}">
                                                            <div class="text-sm text-muted" style="margin-top:6px;">Lên
                                                                xe lúc: ${afternoonAtt.boardedAt}</div>
                                                        </c:if>
                                                    </div>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <!-- Recent Notifications -->
                                <div class="card mt-4">
                                    <div class="card-header">
                                        <div class="card-title">📋 Lịch sử báo nghỉ gần đây</div>
                                        <a href="${pageContext.request.contextPath}/parent/absence"
                                            class="btn btn-primary btn-sm">+ Báo nghỉ mới</a>
                                    </div>
                                    <c:choose>
                                        <c:when test="${empty recentAbsences}">
                                            <div class="empty-state">
                                                <div class="empty-icon">📋</div>
                                                <p>Chưa có lịch sử báo nghỉ</p>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="table-wrap">
                                                <table>
                                                    <thead>
                                                        <tr>
                                                            <th>Học sinh</th>
                                                            <th>Ngày nghỉ</th>
                                                            <th>Lý do</th>
                                                            <th>Trạng thái</th>
                                                            <th>Thời gian báo</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="an" items="${recentAbsences}">
                                                            <tr>
                                                                <td><strong>${an.studentName}</strong></td>
                                                                <td>
                                                                    <fmt:formatDate value="${an.absenceDate}"
                                                                        pattern="dd/MM/yyyy" />
                                                                </td>
                                                                <td class="text-sm text-muted">${an.reason}</td>
                                                                <td><span
                                                                        class="badge ${an.status eq 'ACKNOWLEDGED' ? 'badge-success' : 'badge-warning'}">${an.getStatusDisplay()}</span>
                                                                </td>
                                                                <td class="text-xs text-muted">${an.notifiedAt}</td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                </div>
            </body>

            </html>