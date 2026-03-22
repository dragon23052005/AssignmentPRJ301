<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Quản lý Tuyến xe - BusTrack</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        </head>

        <body>
            <div class="app-layout">
                <%@ include file="../common/sidebar.jsp" %>
                    <div class="main-content">
                        <div class="topbar">
                            <div class="topbar-title">🗺️ Quản lý Tuyến xe</div>
                            <div class="topbar-actions">
                                <button class="btn btn-primary" onclick="showModal('addModal')">+ Thêm tuyến xe</button>
                            </div>
                        </div>
                        <div class="page-content">
                            <c:if test="${msg eq 'added'}">
                                <div class="alert alert-success">✅ Đã thêm tuyến xe thành công!</div>
                            </c:if>
                            <c:if test="${msg eq 'updated'}">
                                <div class="alert alert-success">✅ Đã cập nhật thành công!</div>
                            </c:if>
                            <c:if test="${msg eq 'deleted'}">
                                <div class="alert alert-info">🗑️ Đã vô hiệu hóa tuyến xe.</div>
                            </c:if>

                            <div class="table-wrap">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>#</th>
                                            <th>Mã tuyến</th>
                                            <th>Tên tuyến</th>
                                            <th>Giờ sáng</th>
                                            <th>Giờ chiều</th>
                                            <th>Điểm đón</th>
                                            <th>HS đăng ký</th>
                                            <th>Trạng thái</th>
                                            <th>Hành động</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="r" items="${routes}" varStatus="st">
                                            <tr>
                                                <td class="text-muted">${st.index+1}</td>
                                                <td><code
                                                        style="background:rgba(99,102,241,0.15);color:#a5b4fc;padding:2px 8px;border-radius:4px;">${r.routeCode}</code>
                                                </td>
                                                <td><strong>${r.routeName}</strong><br><span
                                                        class="text-xs text-muted">${r.description}</span></td>
                                                <td class="text-sm">${r.morningDeparture}</td>
                                                <td class="text-sm">${r.afternoonDeparture}</td>
                                                <td><span class="badge badge-info">${r.stopCount} điểm</span></td>
                                                <td><span class="badge badge-success">${r.studentCount} HS</span></td>
                                                <td><span
                                                        class="badge ${r.active ? 'badge-success' : 'badge-danger'}">${r.active
                                                        ? 'Hoạt động' : 'Tạm dừng'}</span></td>
                                                <td>
                                                    <div style="display:flex;gap:6px;">
                                                        <a href="${pageContext.request.contextPath}/admin/routes?action=stops&routeId=${r.id}"
                                                            class="btn btn-info btn-sm">📍 Điểm đón</a>
                                                        <button class="btn btn-ghost btn-sm"
                                                            onclick="openEdit(${r.id},'${r.routeName.replace("'","\\'")}','${r.description}','${r.morningDeparture}','${r.afternoonDeparture}',${r.active ? 1 : 0})">✏️</button>
                                                        <a href="${pageContext.request.contextPath}/admin/routes?action=delete&id=${r.id}"
                                                            class="btn btn-danger btn-sm"
                                                            onclick="return confirm('Vô hiệu hóa tuyến này?')">🗑️</a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty routes}">
                                            <tr>
                                                <td colspan="9">
                                                    <div class="empty-state">
                                                        <div class="empty-icon">🗺️</div>
                                                        <p>Chưa có tuyến xe nào</p>
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
                <div class="modal">
                    <div class="modal-header">
                        <div class="modal-title">➕ Thêm tuyến xe</div>
                        <button class="modal-close" onclick="hideModal('addModal')">✕</button>
                    </div>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/routes">
                        <input type="hidden" name="action" value="add">
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Tên tuyến *</label>
                                <input name="routeName" class="form-control" required
                                    placeholder="VD: Tuyến Bình Thạnh">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Mã tuyến *</label>
                                <input name="routeCode" class="form-control" required placeholder="VD: R001">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Mô tả</label>
                            <textarea name="description" class="form-control" rows="2"
                                placeholder="Mô tả lộ trình..."></textarea>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Giờ khởi hành sáng</label>
                                <input name="morningDeparture" type="time" class="form-control">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Giờ khởi hành chiều</label>
                                <input name="afternoonDeparture" type="time" class="form-control">
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-ghost" onclick="hideModal('addModal')">Hủy</button>
                            <button type="submit" class="btn btn-primary">Thêm tuyến xe</button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Edit Modal -->
            <div class="modal-overlay" id="editModal">
                <div class="modal">
                    <div class="modal-header">
                        <div class="modal-title">✏️ Chỉnh sửa tuyến xe</div>
                        <button class="modal-close" onclick="hideModal('editModal')">✕</button>
                    </div>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/routes">
                        <input type="hidden" name="action" value="edit">
                        <input type="hidden" name="id" id="editId">
                        <div class="form-group">
                            <label class="form-label">Tên tuyến *</label>
                            <input name="routeName" id="editRouteName" class="form-control" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Mô tả</label>
                            <textarea name="description" id="editDesc" class="form-control" rows="2"></textarea>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Giờ sáng</label>
                                <input name="morningDeparture" id="editMorning" type="time" class="form-control">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Giờ chiều</label>
                                <input name="afternoonDeparture" id="editAfternoon" type="time" class="form-control">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Trạng thái</label>
                            <select name="isActive" id="editActive" class="form-control">
                                <option value="1">Hoạt động</option>
                                <option value="0">Tạm dừng</option>
                            </select>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-ghost" onclick="hideModal('editModal')">Hủy</button>
                            <button type="submit" class="btn btn-primary">Lưu thay đổi</button>
                        </div>
                    </form>
                </div>
            </div>

            <script>
                function showModal(id) { document.getElementById(id).classList.add('show'); }
                function hideModal(id) { document.getElementById(id).classList.remove('show'); }
                function openEdit(id, name, desc, morning, afternoon, active) {
                    document.getElementById('editId').value = id;
                    document.getElementById('editRouteName').value = name;
                    document.getElementById('editDesc').value = desc;
                    document.getElementById('editMorning').value = morning;
                    document.getElementById('editAfternoon').value = afternoon;
                    document.getElementById('editActive').value = active;
                    showModal('editModal');
                }
                window.onclick = e => { if (e.target.classList.contains('modal-overlay')) e.target.classList.remove('show'); };
            </script>
        </body>

        </html>