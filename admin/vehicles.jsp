<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Quản lý Xe buýt - BusTrack</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        </head>

        <body>
            <div class="app-layout">
                <%@ include file="../common/sidebar.jsp" %>
                    <div class="main-content">
                        <div class="topbar">
                            <div class="topbar-title">🚌 Quản lý Xe buýt</div>
                            <div class="topbar-actions">
                                <button class="btn btn-primary" onclick="showModal('addModal')">+ Thêm xe</button>
                            </div>
                        </div>
                        <div class="page-content">
                            <c:if test="${msg eq 'added'}">
                                <div class="alert alert-success">✅ Đã thêm xe thành công!</div>
                            </c:if>
                            <c:if test="${msg eq 'updated'}">
                                <div class="alert alert-success">✅ Đã cập nhật thành công!</div>
                            </c:if>
                            <div class="table-wrap">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Biển số</th>
                                            <th>Xe</th>
                                            <th>Sức chứa</th>
                                            <th>Tuyến</th>
                                            <th>Lái xe</th>
                                            <th>Quản lý xe</th>
                                            <th>Trạng thái</th>
                                            <th>Hành động</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="v" items="${vehicles}">
                                            <tr>
                                                <td><strong
                                                        style="font-family:monospace;font-size:15px;letter-spacing:1px;">${v.plateNumber}</strong>
                                                </td>
                                                <td class="text-sm">${v.brand} ${v.model}</td>
                                                <td><span class="badge badge-info">${v.capacity} chỗ</span></td>
                                                <td class="text-sm">${empty v.routeName ? '—' : v.routeName}</td>
                                                <td class="text-sm">${empty v.driverName ? '—' : v.driverName}</td>
                                                <td class="text-sm">${empty v.monitorName ? '—' : v.monitorName}</td>
                                                <td><span
                                                        class="badge ${v.active ? 'badge-success' : 'badge-danger'}">${v.active
                                                        ? 'Hoạt động' : 'Ngừng'}</span></td>
                                                <td>
                                                    <button class="btn btn-ghost btn-sm"
                                                        onclick="openEdit(${v.id},'${v.plateNumber}','${v.brand}','${v.model}',${v.capacity},${v.driverId},${v.monitorId},${v.routeId},${v.active ? 1 : 0})">✏️</button>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty vehicles}">
                                            <tr>
                                                <td colspan="8">
                                                    <div class="empty-state">
                                                        <div class="empty-icon">🚌</div>
                                                        <p>Chưa có xe nào</p>
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
                        <div class="modal-title">➕ Thêm xe buýt</div>
                        <button class="modal-close" onclick="hideModal('addModal')">✕</button>
                    </div>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/vehicles">
                        <input type="hidden" name="action" value="add">
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Biển số xe *</label>
                                <input name="plateNumber" class="form-control" required placeholder="51B-12345">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Sức chứa</label>
                                <input name="capacity" type="number" class="form-control" value="45" min="1">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Hãng xe</label>
                                <input name="brand" class="form-control" placeholder="Mercedes, Ford...">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Model</label>
                                <input name="model" class="form-control" placeholder="Sprinter, Transit...">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Tuyến xe</label>
                            <select name="routeId" class="form-control">
                                <option value="">-- Chọn tuyến --</option>
                                <c:forEach var="r" items="${routes}">
                                    <option value="${r.id}">${r.routeCode} - ${r.routeName}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Lái xe</label>
                                <select name="driverId" class="form-control">
                                    <option value="">-- Chọn lái xe --</option>
                                    <c:forEach var="d" items="${drivers}">
                                        <option value="${d.id}">${d.fullName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Quản lý xe</label>
                                <select name="monitorId" class="form-control">
                                    <option value="">-- Chọn QLX --</option>
                                    <c:forEach var="m" items="${monitors}">
                                        <option value="${m.id}">${m.fullName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-ghost" onclick="hideModal('addModal')">Hủy</button>
                            <button type="submit" class="btn btn-primary">Thêm xe</button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Edit Modal -->
            <div class="modal-overlay" id="editModal">
                <div class="modal" style="max-width:560px;">
                    <div class="modal-header">
                        <div class="modal-title">✏️ Chỉnh sửa xe buýt</div>
                        <button class="modal-close" onclick="hideModal('editModal')">✕</button>
                    </div>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/vehicles">
                        <input type="hidden" name="action" value="edit">
                        <input type="hidden" name="id" id="editId">
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Biển số xe *</label>
                                <input name="plateNumber" id="editPlate" class="form-control" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Sức chứa</label>
                                <input name="capacity" id="editCap" type="number" class="form-control" min="1">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Hãng xe</label>
                                <input name="brand" id="editBrand" class="form-control">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Model</label>
                                <input name="model" id="editModel" class="form-control">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Tuyến xe</label>
                            <select name="routeId" id="editRoute" class="form-control">
                                <option value="">-- Chọn tuyến --</option>
                                <c:forEach var="r" items="${routes}">
                                    <option value="${r.id}">${r.routeCode} - ${r.routeName}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Lái xe</label>
                                <select name="driverId" id="editDriver" class="form-control">
                                    <option value="">-- Chọn lái xe --</option>
                                    <c:forEach var="d" items="${drivers}">
                                        <option value="${d.id}">${d.fullName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Quản lý xe</label>
                                <select name="monitorId" id="editMonitor" class="form-control">
                                    <option value="">-- Chọn QLX --</option>
                                    <c:forEach var="m" items="${monitors}">
                                        <option value="${m.id}">${m.fullName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Trạng thái</label>
                            <select name="isActive" id="editActive" class="form-control">
                                <option value="1">Hoạt động</option>
                                <option value="0">Ngừng hoạt động</option>
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
                function openEdit(id, plate, brand, model, cap, dId, mId, rId, active) {
                    document.getElementById('editId').value = id;
                    document.getElementById('editPlate').value = plate;
                    document.getElementById('editBrand').value = brand;
                    document.getElementById('editModel').value = model;
                    document.getElementById('editCap').value = cap;
                    document.getElementById('editDriver').value = dId;
                    document.getElementById('editMonitor').value = mId;
                    document.getElementById('editRoute').value = rId;
                    document.getElementById('editActive').value = active;
                    showModal('editModal');
                }
                window.onclick = e => { if (e.target.classList.contains('modal-overlay')) e.target.classList.remove('show'); };
            </script>
        </body>

        </html>