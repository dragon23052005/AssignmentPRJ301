<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Quản lý Học sinh - BusTrack</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
            </head>

            <body>
                <div class="app-layout">
                    <%@ include file="../common/sidebar.jsp" %>
                        <div class="main-content">
                            <div class="topbar">
                                <div class="topbar-title">🎒 Quản lý Học sinh</div>
                                <div class="topbar-actions">
                                    <button class="btn btn-primary" onclick="showModal('addModal')">+ Thêm học
                                        sinh</button>
                                </div>
                            </div>
                            <div class="page-content">
                                <c:if test="${msg eq 'added'}">
                                    <div class="alert alert-success">✅ Đã thêm học sinh thành công!</div>
                                </c:if>
                                <c:if test="${msg eq 'updated'}">
                                    <div class="alert alert-success">✅ Đã cập nhật thành công!</div>
                                </c:if>

                                <div class="card mb-4">
                                    <div class="search-box">
                                        <span class="search-icon">🔍</span>
                                        <input type="text" id="searchInput" class="form-control"
                                            placeholder="Tìm tên học sinh, mã HS, lớp...">
                                    </div>
                                </div>

                                <div class="table-wrap">
                                    <table id="studentsTable">
                                        <thead>
                                            <tr>
                                                <th>#</th>
                                                <th>Học sinh</th>
                                                <th>Lớp</th>
                                                <th>Phụ huynh</th>
                                                <th>Tuyến đăng ký</th>
                                                <th>Điểm đón</th>
                                                <th>Hành động</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="s" items="${students}" varStatus="st">
                                                <tr
                                                    data-search="${s.fullName.toLowerCase()} ${s.studentCode} ${s.className.toLowerCase()}">
                                                    <td class="text-muted">${st.index+1}</td>
                                                    <td>
                                                        <div style="display:flex;align-items:center;gap:10px;">
                                                            <div class="student-avatar"
                                                                style="width:36px;height:36px;font-size:15px;">🎒</div>
                                                            <div>
                                                                <div style="font-weight:600;font-size:13px;">
                                                                    ${s.fullName}</div>
                                                                <div class="text-xs text-muted">${s.studentCode}</div>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td><span class="badge badge-info">${s.className}</span></td>
                                                    <td class="text-sm">
                                                        <div>${s.parentName}</div>
                                                        <div class="text-muted text-xs">${s.parentPhone}</div>
                                                    </td>
                                                    <td class="text-sm">${empty s.routeName ? '<span
                                                            class="text-muted">Chưa đăng ký</span>' : s.routeName}</td>
                                                    <td class="text-sm">${empty s.stopName ? '—' : s.stopName}</td>
                                                    <td>
                                                        <button class="btn btn-ghost btn-sm"
                                                            onclick="openEdit(${s.id},'${s.fullName.replace("'","\\'")}','${s.studentCode}','${s.className}',${s.routeId},${s.stopId})">✏️</button>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                            <c:if test="${empty students}">
                                                <tr>
                                                    <td colspan="7">
                                                        <div class="empty-state">
                                                            <div class="empty-icon">🎒</div>
                                                            <p>Chưa có học sinh nào</p>
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

                <!-- Add Modal -->
                <div class="modal-overlay" id="addModal">
                    <div class="modal" style="max-width:560px;">
                        <div class="modal-header">
                            <div class="modal-title">➕ Thêm học sinh</div>
                            <button class="modal-close" onclick="hideModal('addModal')">✕</button>
                        </div>
                        <form method="POST" action="${pageContext.request.contextPath}/admin/students">
                            <input type="hidden" name="action" value="add">
                            <div class="form-row">
                                <div class="form-group">
                                    <label class="form-label">Họ và tên *</label>
                                    <input name="fullName" class="form-control" required placeholder="Nguyễn Văn A">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Mã học sinh</label>
                                    <input name="studentCode" class="form-control" placeholder="HS001">
                                </div>
                            </div>
                            <div class="form-row">
                                <div class="form-group">
                                    <label class="form-label">Lớp</label>
                                    <input name="className" class="form-control" placeholder="10A1">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Ngày sinh</label>
                                    <input name="dateOfBirth" type="date" class="form-control">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Phụ huynh *</label>
                                <select name="parentId" class="form-control" required>
                                    <option value="">-- Chọn phụ huynh --</option>
                                    <c:forEach var="p" items="${parents}">
                                        <option value="${p.id}">${p.fullName} (${p.phone})</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <hr style="border-color:var(--border);margin:16px 0;">
                            <p class="text-sm text-muted mb-4">Đăng ký xe buýt (tùy chọn)</p>
                            <div class="form-row">
                                <div class="form-group">
                                    <label class="form-label">Tuyến xe</label>
                                    <select name="routeId" id="addRouteId" class="form-control"
                                        onchange="loadAddStops()">
                                        <option value="">-- Chọn tuyến --</option>
                                        <c:forEach var="r" items="${routes}">
                                            <option value="${r.id}">${r.routeCode} - ${r.routeName}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Điểm đón</label>
                                    <select name="stopId" id="addStopId" class="form-control">
                                        <option value="">-- Chọn tuyến trước --</option>
                                    </select>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-ghost" onclick="hideModal('addModal')">Hủy</button>
                                <button type="submit" class="btn btn-primary">Thêm học sinh</button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Edit Modal -->
                <div class="modal-overlay" id="editModal">
                    <div class="modal" style="max-width:560px;">
                        <div class="modal-header">
                            <div class="modal-title">✏️ Chỉnh sửa học sinh</div>
                            <button class="modal-close" onclick="hideModal('editModal')">✕</button>
                        </div>
                        <form method="POST" action="${pageContext.request.contextPath}/admin/students">
                            <input type="hidden" name="action" value="edit">
                            <input type="hidden" name="id" id="editId">
                            <div class="form-row">
                                <div class="form-group">
                                    <label class="form-label">Họ và tên *</label>
                                    <input name="fullName" id="editFullName" class="form-control" required>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Mã học sinh</label>
                                    <input name="studentCode" id="editCode" class="form-control">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Lớp</label>
                                <input name="className" id="editClass" class="form-control">
                            </div>
                            <hr style="border-color:var(--border);margin:16px 0;">
                            <p class="text-sm text-muted mb-4">Cập nhật đăng ký xe buýt</p>
                            <div class="form-row">
                                <div class="form-group">
                                    <label class="form-label">Tuyến xe</label>
                                    <select name="routeId" id="editRouteId" class="form-control"
                                        onchange="loadEditStops()">
                                        <option value="">-- Chọn tuyến --</option>
                                        <c:forEach var="r" items="${routes}">
                                            <option value="${r.id}">${r.routeCode} - ${r.routeName}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Điểm đón</label>
                                    <select name="stopId" id="editStopId" class="form-control"></select>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-ghost"
                                    onclick="hideModal('editModal')">Hủy</button>
                                <button type="submit" class="btn btn-primary">Lưu thay đổi</button>
                            </div>
                        </form>
                    </div>
                </div>

                <script>
                    const ctx = '${pageContext.request.contextPath}';
                    function showModal(id) { document.getElementById(id).classList.add('show'); }
                    function hideModal(id) { document.getElementById(id).classList.remove('show'); }

                    function loadAddStops() {
                        const rid = document.getElementById('addRouteId').value;
                        const sel = document.getElementById('addStopId');
                        loadStops(rid, sel, 0);
                    }
                    function loadEditStops() {
                        const rid = document.getElementById('editRouteId').value;
                        const sel = document.getElementById('editStopId');
                        loadStops(rid, sel, window._editStopId || 0);
                    }
                    function loadStops(rid, sel, selectedId) {
                        sel.innerHTML = '<option value="">Đang tải...</option>';
                        if (!rid) { sel.innerHTML = '<option value="">-- Chọn tuyến trước --</option>'; return; }
                        fetch(ctx + '/admin/students?action=stops&routeId=' + rid)
                            .then(r => r.json())
                            .then(stops => {
                                sel.innerHTML = '<option value="">-- Chọn điểm đón --</option>';
                                stops.forEach(s => {
                                    const opt = new Option(s.stopName + (s.address ? ' - ' + s.address : ''), s.id);
                                    if (s.id == selectedId) opt.selected = true;
                                    sel.add(opt);
                                });
                            });
                    }
                    function openEdit(id, name, code, cls, routeId, stopId) {
                        window._editStopId = stopId;
                        document.getElementById('editId').value = id;
                        document.getElementById('editFullName').value = name;
                        document.getElementById('editCode').value = code;
                        document.getElementById('editClass').value = cls;
                        document.getElementById('editRouteId').value = routeId;
                        if (routeId) loadEditStops();
                        showModal('editModal');
                    }
                    document.getElementById('searchInput').addEventListener('input', function () {
                        const q = this.value.toLowerCase();
                        document.querySelectorAll('#studentsTable tbody tr[data-search]').forEach(tr => {
                            tr.style.display = tr.dataset.search.includes(q) ? '' : 'none';
                        });
                    });
                    window.onclick = e => { if (e.target.classList.contains('modal-overlay')) e.target.classList.remove('show'); };
                </script>
            </body>

            </html>