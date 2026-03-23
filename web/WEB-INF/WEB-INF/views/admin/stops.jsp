<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Điểm đón/trả - ${route.routeName} - BusTrack</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        </head>

        <body>
            <div class="app-layout">
                <%@ include file="../common/sidebar.jsp" %>
                    <div class="main-content">
                        <div class="topbar">
                            <div class="topbar-title">
                                📍 Điểm đón/trả - <span style="color:var(--primary-light)">${route.routeName}</span>
                            </div>
                            <div class="topbar-actions">
                                <a href="${pageContext.request.contextPath}/admin/routes" class="btn btn-ghost btn-sm">←
                                    Quay lại</a>
                                <button class="btn btn-primary" onclick="showModal('addStopModal')">+ Thêm điểm
                                    đón</button>
                            </div>
                        </div>
                        <div class="page-content">
                            <c:if test="${msg eq 'stopAdded'}">
                                <div class="alert alert-success">✅ Đã thêm điểm đón thành công!</div>
                            </c:if>
                            <c:if test="${msg eq 'stopDeleted'}">
                                <div class="alert alert-info">🗑️ Đã xóa điểm đón.</div>
                            </c:if>

                            <div class="table-wrap">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Thứ tự</th>
                                            <th>Tên điểm đón</th>
                                            <th>Địa chỉ</th>
                                            <th>Giờ sáng (dự kiến)</th>
                                            <th>Giờ chiều (dự kiến)</th>
                                            <th>Hành động</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="s" items="${stops}">
                                            <tr>
                                                <td>
                                                    <div
                                                        style="width:32px;height:32px;border-radius:50%;background:linear-gradient(135deg,var(--primary),var(--secondary));display:flex;align-items:center;justify-content:center;font-weight:700;font-size:14px;">
                                                        ${s.stopOrder}
                                                    </div>
                                                </td>
                                                <td><strong>${s.stopName}</strong></td>
                                                <td class="text-sm text-muted">${s.address}</td>
                                                <td class="text-sm">${s.estimatedMorningTime}</td>
                                                <td class="text-sm">${s.estimatedAfternoonTime}</td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/admin/routes?action=deleteStop&stopId=${s.id}&routeId=${route.id}"
                                                        class="btn btn-danger btn-sm"
                                                        onclick="return confirm('Xóa điểm đón này?')">🗑️ Xóa</a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty stops}">
                                            <tr>
                                                <td colspan="6">
                                                    <div class="empty-state">
                                                        <div class="empty-icon">📍</div>
                                                        <p>Chưa có điểm đón nào</p>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
            </div>

            <!-- Add Stop Modal -->
            <div class="modal-overlay" id="addStopModal">
                <div class="modal">
                    <div class="modal-header">
                        <div class="modal-title">➕ Thêm điểm đón/trả</div>
                        <button class="modal-close" onclick="hideModal('addStopModal')">✕</button>
                    </div>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/routes">
                        <input type="hidden" name="action" value="addStop">
                        <input type="hidden" name="routeId" value="${route.id}">
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Tên điểm *</label>
                                <input name="stopName" class="form-control" required
                                    placeholder="VD: Cầu Điện Biên Phủ">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Thứ tự *</label>
                                <input name="stopOrder" type="number" class="form-control" required min="1"
                                    value="${stops.size() + 1}">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Địa chỉ</label>
                            <input name="address" class="form-control" placeholder="Số nhà, đường, phường, quận...">
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Giờ đón buổi sáng</label>
                                <input name="estimatedMorningTime" type="time" class="form-control">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Giờ trả buổi chiều</label>
                                <input name="estimatedAfternoonTime" type="time" class="form-control">
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-ghost" onclick="hideModal('addStopModal')">Hủy</button>
                            <button type="submit" class="btn btn-primary">Thêm điểm</button>
                        </div>
                    </form>
                </div>
            </div>
            <script>
                function showModal(id) { document.getElementById(id).classList.add('show'); }
                function hideModal(id) { document.getElementById(id).classList.remove('show'); }
                window.onclick = e => { if (e.target.classList.contains('modal-overlay')) e.target.classList.remove('show'); };
            </script>
        </body>

        </html>