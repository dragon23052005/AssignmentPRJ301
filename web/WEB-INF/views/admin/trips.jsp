<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Tổng quan Chuyến xe - BusTrack</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            </head>

            <body>
                <div class="app-layout">
                    <%@ include file="../common/sidebar.jsp" %>
                        <div class="main-content">
                            <div class="topbar">
                                <div class="topbar-title">📅 Tổng quan Chuyến xe</div>
                                <div class="topbar-actions">
                                    <span class="text-sm text-muted">Tổng: ${trips.size()} chuyến</span>
                                </div>
                            </div>
                            <div class="page-content">
                                <div class="grid-2 mb-4">
                                    <div class="card">
                                        <div class="card-header">
                                            <div class="card-title">📣 Báo nghỉ hôm nay</div>
                                        </div>
                                        <c:choose>
                                            <c:when test="${empty absences}">
                                                <div class="empty-state">
                                                    <div class="empty-icon">✅</div>
                                                    <p>Không có báo nghỉ</p>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div style="display:flex;flex-direction:column;gap:10px;">
                                                    <c:forEach var="an" items="${absences}">
                                                        <div
                                                            style="display:flex;align-items:center;gap:12px;padding:10px;background:rgba(255,255,255,0.03);border-radius:8px;">
                                                            <div class="student-avatar"
                                                                style="width:36px;height:36px;font-size:14px;">🎒</div>
                                                            <div style="flex:1;">
                                                                <div style="font-weight:600;font-size:13px;">
                                                                    ${an.studentName}</div>
                                                                <div class="text-xs text-muted">${an.className} •
                                                                    ${an.routeName}</div>
                                                                <div class="text-xs text-muted">${an.reason}</div>
                                                            </div>
                                                            <span
                                                                class="badge ${an.status eq 'ACKNOWLEDGED' ? 'badge-success' : 'badge-warning'}">${an.getStatusDisplay()}</span>
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="card">
                                        <div class="card-header">
                                            <div class="card-title">⚠️ Sự cố xe đang mở</div>
                                        </div>
                                        <c:choose>
                                            <c:when test="${empty reports}">
                                                <div class="empty-state">
                                                    <div class="empty-icon">✅</div>
                                                    <p>Không có sự cố</p>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div style="display:flex;flex-direction:column;gap:8px;">
                                                    <c:forEach var="r" items="${reports}">
                                                        <div
                                                            style="padding:10px;background:rgba(255,255,255,0.03);border-radius:8px;border-left:3px solid var(--warning);">
                                                            <div
                                                                style="display:flex;justify-content:space-between;align-items:flex-start;">
                                                                <div>
                                                                    <div style="font-weight:600;font-size:13px;">
                                                                        ${r.plateNumber} - ${r.getIssueTypeDisplay()}
                                                                    </div>
                                                                    <div class="text-xs text-muted">${r.reportedByName}
                                                                        • ${r.reportDate}</div>
                                                                </div>
                                                                <span
                                                                    class="badge ${r.severity eq 'CRITICAL' ? 'badge-danger' : r.severity eq 'HIGH' ? 'badge-danger' : 'badge-warning'}">${r.getSeverityDisplay()}</span>
                                                            </div>
                                                            <div class="text-xs text-muted" style="margin-top:6px;">
                                                                ${r.description}</div>
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <div class="card">
                                    <div class="card-header">
                                        <div class="card-title">🚌 Tất cả chuyến xe</div>
                                    </div>
                                    <div class="table-wrap">
                                        <table>
                                            <thead>
                                                <tr>
                                                    <th>Ngày</th>
                                                    <th>Tuyến</th>
                                                    <th>Xe</th>
                                                    <th>Ca</th>
                                                    <th>Khởi hành</th>
                                                    <th>Đến nơi</th>
                                                    <th>Điểm danh</th>
                                                    <th>Trạng thái</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:choose>
                                                    <c:when test="${empty trips}">
                                                        <tr>
                                                            <td colspan="8">
                                                                <div class="empty-state">
                                                                    <div class="empty-icon">📅</div>
                                                                    <p>Chưa có chuyến xe nào</p>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:forEach var="t" items="${trips}">
                                                            <tr>
                                                                <td class="text-sm">
                                                                    <fmt:formatDate value="${t.tripDate}"
                                                                        pattern="dd/MM/yyyy" />
                                                                </td>
                                                                <td><strong>${t.routeName}</strong></td>
                                                                <td class="text-sm">${t.plateNumber}</td>
                                                                <td><span
                                                                        class="badge ${t.tripType eq 'MORNING' ? 'badge-info' : 'badge-warning'}">${t.getTripTypeDisplay()}</span>
                                                                </td>
                                                                <td class="text-sm">${empty t.departedAt ? '—' :
                                                                    t.departedAt}</td>
                                                                <td class="text-sm">${empty t.arrivedAt ? '—' :
                                                                    t.arrivedAt}</td>
                                                                <td>
                                                                    <div
                                                                        style="display:flex;align-items:center;gap:8px;">
                                                                        <span
                                                                            class="text-sm">${t.presentCount}/${t.totalCount}</span>
                                                                        <c:if test="${t.totalCount > 0}">
                                                                            <div class="progress-bar-wrap"
                                                                                style="width:60px;">
                                                                                <div class="progress-bar"
                                                                                    style="width:${t.totalCount > 0 ? (t.presentCount * 100 / t.totalCount) : 0}%;">
                                                                                </div>
                                                                            </div>
                                                                        </c:if>
                                                                    </div>
                                                                </td>
                                                                <td><span
                                                                        class="badge ${t.status eq 'COMPLETED' ? 'badge-success' : t.status eq 'DEPARTED' ? 'badge-warning' : t.status eq 'CANCELLED' ? 'badge-danger' : 'badge-neutral'}">${t.getStatusDisplay()}</span>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </c:otherwise>
                                                </c:choose>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                </div>
            </body>

            </html>