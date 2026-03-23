<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý nghỉ học - BusTrack</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>

<body>
    <div class="app-layout">
        <%@ include file="../common/sidebar.jsp" %>
        <div class="main-content">
            <div class="topbar">
                <div class="topbar-title">📣 Quản lý nghỉ học</div>
            </div>
            <div class="page-content">
                <c:if test="${msg eq 'ack'}">
                    <div class="alert alert-success">✅ Đã xác nhận nghỉ học thành công!</div>
                </c:if>

                <div class="grid-2">
                    <div class="card">
                        <div class="card-title" style="margin-bottom:16px;">📅 Báo nghỉ hôm nay</div>
                        <c:choose>
                            <c:when test="${empty todayAbsences}">
                                <div class="empty-state">
                                    <div class="empty-icon">📅</div>
                                    <p>Không có báo nghỉ nào hôm nay</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="display:flex;flex-direction:column;gap:10px;">
                                    <c:forEach var="an" items="${todayAbsences}">
                                        <div style="padding:14px;border-radius:10px;border:1px solid ${an.status eq 'PENDING' ? 'var(--warning)' : 'var(--border)'};background:rgba(255,255,255,0.02);">
                                            <div style="display:flex;justify-content:space-between;align-items:flex-start;">
                                                <div>
                                                    <div style="font-weight:600;font-size:15px;">${an.studentName}</div>
                                                    <div class="text-sm text-muted">Mã HS: ${an.studentCode} | Lớp: ${an.className}</div>
                                                    <div class="text-sm text-muted" style="margin-top:2px;">Tuyến: ${an.routeName}</div>
                                                    <c:if test="${not empty an.reason}">
                                                        <div class="text-sm" style="margin-top:6px;color:var(--text-secondary);">
                                                            <strong>Lý do:</strong> ${an.reason}
                                                        </div>
                                                    </c:if>
                                                    <div class="text-xs text-muted" style="margin-top:6px;">Báo lúc: ${an.notifiedAt}</div>
                                                </div>
                                                <div style="text-align:right;">
                                                    <span class="badge ${an.status eq 'ACKNOWLEDGED' ? 'badge-success' : 'badge-warning'}">
                                                        ${an.getStatusDisplay()}
                                                    </span>
                                                    <c:if test="${an.status eq 'PENDING'}">
                                                        <div style="margin-top:10px;">
                                                            <a href="${pageContext.request.contextPath}/monitor/absence?action=acknowledge&id=${an.id}" 
                                                               class="btn btn-primary btn-sm"
                                                               onclick="return confirm('Xác nhận đã tiếp nhận thông báo nghỉ?')">
                                                               ✅ Xác nhận
                                                            </a>
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="card">
                        <div class="card-title" style="margin-bottom:16px;">📋 Lịch sử báo nghỉ</div>
                        <c:choose>
                            <c:when test="${empty allAbsences}">
                                <div class="empty-state">
                                    <div class="empty-icon">📋</div>
                                    <p>Chưa có thông báo nghỉ nào</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="display:flex;flex-direction:column;gap:10px;max-height:600px;overflow-y:auto;">
                                    <c:forEach var="an" items="${allAbsences}">
                                        <div style="padding:14px;background:rgba(255,255,255,0.02);border-radius:10px;border:1px solid var(--border);">
                                            <div style="display:flex;justify-content:space-between;align-items:flex-start;">
                                                <div>
                                                    <div style="font-weight:600;">${an.studentName}</div>
                                                    <div class="text-sm text-muted">
                                                        📅 <fmt:formatDate value="${an.absenceDate}" pattern="dd/MM/yyyy" />
                                                        | Lớp: ${an.className}
                                                    </div>
                                                </div>
                                                <span class="badge ${an.status eq 'ACKNOWLEDGED' ? 'badge-success' : 'badge-warning'}">
                                                    ${an.getStatusDisplay()}
                                                </span>
                                            </div>
                                            <c:if test="${not empty an.reason}">
                                                <div class="text-sm" style="margin-top:4px;color:var(--text-secondary);">${an.reason}</div>
                                            </c:if>
                                            <div class="text-xs text-muted" style="margin-top:8px;">Báo lúc: ${an.notifiedAt}</div>
                                            <c:if test="${an.status eq 'ACKNOWLEDGED'}">
                                                <div class="text-xs text-success" style="margin-top:2px;">
                                                    Xác nhận bởi: ${an.acknowledgedByName} lúc ${an.acknowledgedAt}
                                                </div>
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
