<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Quản lý Chuyến xe - BusTrack</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
                <style>
                    .att-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
                        gap: 12px;
                    }

                    .att-card {
                        background: var(--bg-card);
                        border: 1px solid var(--border);
                        border-radius: 10px;
                        padding: 14px;
                        transition: all 0.2s;
                    }

                    .att-card.boarded {
                        border-color: rgba(16, 185, 129, 0.5);
                        background: rgba(16, 185, 129, 0.05);
                    }

                    .att-card.notified {
                        border-color: rgba(6, 182, 212, 0.4);
                        background: rgba(6, 182, 212, 0.04);
                    }

                    .att-card-header {
                        display: flex;
                        align-items: center;
                        gap: 10px;
                        margin-bottom: 10px;
                    }

                    .att-actions {
                        display: flex;
                        gap: 6px;
                        flex-wrap: wrap;
                    }
                </style>
            </head>

            <body>
                <div class="app-layout">
                    <%@ include file="../common/sidebar.jsp" %>
                        <div class="main-content">
                            <div class="topbar">
                                <div class="topbar-title">🚦 Quản lý Chuyến xe</div>
                                <div class="topbar-actions">
                                    <button class="btn btn-primary" onclick="showModal('createModal')">+ Tạo chuyến
                                        xe</button>
                                </div>
                            </div>
                            <div class="page-content">
                                <c:if test="${not empty error}">
                                    <div class="alert alert-danger">⚠️ ${error}</div>
                                </c:if>
                                <c:if test="${msg eq 'created'}">
                                    <div class="alert alert-success">✅ Tạo chuyến xe thành công! Điểm danh đã được khởi
                                        tạo.</div>
                                </c:if>
                                <c:if test="${msg eq 'departed'}">
                                    <div class="alert alert-warning">🚦 Xe đã khởi hành!</div>
                                </c:if>
                                <c:if test="${msg eq 'completed'}">
                                    <div class="alert alert-success">✅ Chuyến xe đã hoàn thành!</div>
                                </c:if>

                                <c:if test="${not empty vehicle}">
                                    <div class="grid-2">
                                        <!-- Trip List -->
                                        <div class="card">
                                            <div class="card-header">
                                                <div class="card-title">📅 Các chuyến xe hôm nay</div>
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
                                                        <a href="${pageContext.request.contextPath}/monitor/trip?tripId=${t.id}"
                                                            style="display:block;padding:14px;border-radius:10px;border:1px solid ${not empty selectedTrip and selectedTrip.id eq t.id ? 'var(--primary)' : 'var(--border)'};
                            background:${not empty selectedTrip and selectedTrip.id eq t.id ? 'rgba(79,70,229,0.1)' : 'rgba(255,255,255,0.02)'};
                            margin-bottom:8px;transition:all 0.2s;">
                                                            <div style="display:flex;justify-content:space-between;margin-bottom:8px;">
                                                                <span class="badge badge-neutral" style="font-weight:700;">🚌 Xe ID: ${t.vehicleId}</span>
                                                                <span
                                                                    class="badge ${t.tripType eq 'MORNING' ? 'badge-info' : 'badge-warning'}">${t.getTripTypeDisplay()}</span>
                                                                <span
                                                                    class="badge ${t.status eq 'COMPLETED' ? 'badge-success' : t.status eq 'DEPARTED' ? 'badge-warning' : 'badge-neutral'}">${t.getStatusDisplay()}</span>
                                                            </div>
                                                            <div class="text-sm" style="margin-top:8px;">👥
                                                                ${t.presentCount}/${t.totalCount} học sinh lên xe</div>
                                                        </a>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                        <!-- Trip Detail & Attendance -->
                                        <div>
                                            <c:choose>
                                                <c:when test="${empty selectedTrip}">
                                                    <div class="card">
                                                        <div class="empty-state">
                                                            <div class="empty-icon">📋</div>
                                                            <p>Chọn chuyến xe để xem điểm danh</p>
                                                        </div>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <!-- Trip Controls -->
                                                    <div class="card mb-4">
                                                        <div class="card-title" style="margin-bottom:12px;">
                                                            🚌 ${selectedTrip.getTripTypeDisplay()} -
                                                            ${selectedTrip.getStatusDisplay()}
                                                        </div>
                                                        <div style="display:flex;gap:10px;flex-wrap:wrap;">
                                                            <c:if test="${selectedTrip.status eq 'SCHEDULED'}">
                                                                <a href="${pageContext.request.contextPath}/monitor/trip?action=depart&tripId=${selectedTrip.id}"
                                                                    class="btn btn-warning"
                                                                    onclick="return confirm('Xác nhận xe đã khởi hành?')">
                                                                    🚦 Khởi hành
                                                                </a>
                                                            </c:if>
                                                            <c:if test="${selectedTrip.status eq 'DEPARTED'}">
                                                                <a href="${pageContext.request.contextPath}/monitor/trip?action=complete&tripId=${selectedTrip.id}"
                                                                    class="btn btn-success"
                                                                    onclick="return confirm('Xác nhận chuyến hoàn thành?')">
                                                                    ✅ Hoàn thành
                                                                </a>
                                                            </c:if>
                                                            <c:if test="${not empty selectedTrip.departedAt}">
                                                                <div class="trip-info text-sm"
                                                                    style="align-self:center;">
                                                                    ⏱️ Khởi hành:
                                                                    <strong>${selectedTrip.departedAt}</strong>
                                                                </div>
                                                            </c:if>
                                                        </div>
                                                        <div style="margin-top:12px;">
                                                            <div class="progress-bar-wrap">
                                                                <div class="progress-bar"
                                                                    style="width:${selectedTrip.totalCount > 0 ? (selectedTrip.presentCount * 100 / selectedTrip.totalCount) : 0}%;">
                                                                </div>
                                                            </div>
                                                            <div class="text-xs text-muted" style="margin-top:4px;">
                                                                ${selectedTrip.presentCount}/${selectedTrip.totalCount}
                                                                học sinh đã lên xe</div>
                                                        </div>
                                                    </div>

                                                    <!-- Attendance List -->
                                                    <div class="card">
                                                        <div class="card-header">
                                                            <div class="card-title">👥 Danh sách điểm danh</div>
                                                        </div>
                                                        <c:choose>
                                                            <c:when test="${empty attendances}">
                                                                <div class="empty-state">
                                                                    <div class="empty-icon">👥</div>
                                                                    <p>Không có học sinh nào</p>
                                                                </div>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="att-grid">
                                                                    <c:forEach var="a" items="${attendances}">
                                                                        <div
                                                                            class="att-card ${a.status eq 'PRESENT' ? 'boarded' : a.status eq 'NOTIFIED_ABSENT' ? 'notified' : ''}">
                                                                            <div class="att-card-header">
                                                                                <div class="student-avatar"
                                                                                    style="width:36px;height:36px;font-size:14px;">
                                                                                    🎒</div>
                                                                                <div style="flex:1;min-width:0;">
                                                                                    <div
                                                                                        style="font-weight:600;font-size:13px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
                                                                                        ${a.studentName}</div>
                                                                                    <div class="text-xs text-muted">
                                                                                        ${a.className}</div>
                                                                                </div>
                                                                                <span
                                                                                    class="badge ${a.status eq 'PRESENT' ? 'badge-success' : a.status eq 'NOTIFIED_ABSENT' ? 'badge-info' : 'badge-danger'}"
                                                                                    style="font-size:10px;">
                                                                                    ${a.getStatusDisplay()}
                                                                                </span>
                                                                            </div>
                                                                            <c:if test="${not empty a.boardedAt}">
                                                                                <div class="text-xs text-muted"
                                                                                    style="margin-bottom:8px;">
                                                                                    ⬆️ ${a.boardedStopName} lúc
                                                                                    ${a.boardedAt}
                                                                                    <c:if
                                                                                        test="${not empty a.alightedAt}">
                                                                                        | ⬇️ ${a.alightedAt}</c:if>
                                                                                </div>
                                                                            </c:if>
                                                                            <c:if
                                                                                test="${selectedTrip.status ne 'COMPLETED' and a.status ne 'NOTIFIED_ABSENT'}">
                                                                                <div class="att-actions">
                                                                                    <c:if
                                                                                        test="${a.status ne 'PRESENT'}">
                                                                                        <select id="stop_${a.studentId}"
                                                                                            class="form-control"
                                                                                            style="height:30px;font-size:12px;padding:2px 8px;">
                                                                                            <c:forEach var="s"
                                                                                                items="${stops}">
                                                                                                <option value="${s.id}">
                                                                                                    ${s.stopName}
                                                                                                </option>
                                                                                            </c:forEach>
                                                                                        </select>
                                                                                        <button
                                                                                            class="btn btn-success btn-sm"
                                                                                            onclick="markAttendance('board',${selectedTrip.id},${a.studentId},'stop_${a.studentId}',this)">⬆️
                                                                                            Lên xe</button>
                                                                                    </c:if>
                                                                                    <c:if
                                                                                        test="${a.status eq 'PRESENT' and empty a.alightedAt}">
                                                                                        <select
                                                                                            id="dstop_${a.studentId}"
                                                                                            class="form-control"
                                                                                            style="height:30px;font-size:12px;padding:2px 8px;">
                                                                                            <c:forEach var="s"
                                                                                                items="${stops}">
                                                                                                <option value="${s.id}">
                                                                                                    ${s.stopName}
                                                                                                </option>
                                                                                            </c:forEach>
                                                                                        </select>
                                                                                        <button
                                                                                            class="btn btn-warning btn-sm"
                                                                                            onclick="markAttendance('alight',${selectedTrip.id},${a.studentId},'dstop_${a.studentId}',this)">⬇️
                                                                                            Xuống xe</button>
                                                                                        <button
                                                                                            class="btn btn-danger btn-sm"
                                                                                            onclick="markAttendance('absent',${selectedTrip.id},${a.studentId},null,this)">❌</button>
                                                                                    </c:if>
                                                                                </div>
                                                                            </c:if>
                                                                        </div>
                                                                    </c:forEach>
                                                                </div>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                </div>

                <!-- Create Trip Modal -->
                <div class="modal-overlay" id="createModal">
                    <div class="modal">
                        <div class="modal-header">
                            <div class="modal-title">➕ Tạo chuyến xe mới</div>
                            <button class="modal-close" onclick="hideModal('createModal')">✕</button>
                        </div>
                        <form method="POST" action="${pageContext.request.contextPath}/monitor/trip">
                            <input type="hidden" name="action" value="createTrip">
                            <div class="form-group">
                                <label class="form-label">Chọn Xe (Tuyến) *</label>
                                <select name="vehicleId" class="form-control" required>
                                    <c:forEach var="v" items="${vehicles}">
                                        <option value="${v.id}">${v.plateNumber} - ${v.routeName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Ngày *</label>
                                <input name="tripDate" type="date" class="form-control" required id="tripDate">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Ca *</label>
                                <select name="tripType" class="form-control" required>
                                    <option value="MORNING">🌅 Buổi sáng</option>
                                    <option value="AFTERNOON">🌇 Buổi chiều</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Ghi chú</label>
                                <textarea name="notes" class="form-control" rows="2"
                                    placeholder="Ghi chú thêm..."></textarea>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-ghost"
                                    onclick="hideModal('createModal')">Hủy</button>
                                <button type="submit" class="btn btn-primary">Tạo chuyến</button>
                            </div>
                        </form>
                    </div>
                </div>

                <script>
                    const ctx = '${pageContext.request.contextPath}';
                    document.getElementById('tripDate').value = new Date().toISOString().split('T')[0];
                    function showModal(id) { document.getElementById(id).classList.add('show'); }
                    function hideModal(id) { document.getElementById(id).classList.remove('show'); }

                    function markAttendance(action, tripId, studentId, stopSelId, btn) {
                        const stopId = stopSelId ? document.getElementById(stopSelId).value : '';
                        btn.disabled = true; btn.textContent = '⏳';
                        fetch(ctx + '/monitor/attendance', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                            body: 'action=' + action + '&tripId=' + tripId + '&studentId=' + studentId + '&stopId=' + stopId
                        })
                            .then(r => r.json())
                            .then(data => {
                                if (data.success) {
                                    location.reload();
                                } else {
                                    alert('Có lỗi xảy ra!');
                                    btn.disabled = false;
                                }
                            });
                    }
                    window.onclick = e => { if (e.target.classList.contains('modal-overlay')) e.target.classList.remove('show'); };
                </script>
            </body>

            </html>