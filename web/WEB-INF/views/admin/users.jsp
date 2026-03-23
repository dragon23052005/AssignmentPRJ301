<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Quản lý Người dùng - BusTrack</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
        </head>

        <body>
            <div class="app-layout">
                <%@ include file="../common/sidebar.jsp" %>
                    <div class="main-content">
                        <div class="topbar">
                            <div class="topbar-title">👥 Quản lý Người dùng</div>
                            <div class="topbar-actions">
                                <button class="btn btn-primary" onclick="showModal('addModal')">+ Thêm người
                                    dùng</button>
                            </div>
                        </div>
                        <div class="page-content">
                            <c:if test="${msg eq 'added'}">
                                <div class="alert alert-success">✅ Đã thêm người dùng thành công!</div>
                            </c:if>
                            <c:if test="${msg eq 'updated'}">
                                <div class="alert alert-success">✅ Đã cập nhật thành công!</div>
                            </c:if>
                            <c:if test="${msg eq 'reset'}">
                                <div class="alert alert-info">🔑 Đã reset mật khẩu về Admin@123</div>
                            </c:if>
                            <c:if test="${not empty error}">
                                <div class="alert alert-danger">⚠️ ${error}</div>
                            </c:if>

                            <!-- Search+Filter bar -->
                            <div class="card mb-4">
                                <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap;">
                                    <div class="search-box" style="flex:1;min-width:200px;">
                                        <span class="search-icon">🔍</span>
                                        <input type="text" id="searchInput" class="form-control"
                                            placeholder="Tìm kiếm tên, username...">
                                    </div>
                                    <select id="roleFilter" class="form-control" style="width:160px;">
                                        <option value="">Tất cả vai trò</option>
                                        <option value="ADMIN">Quản trị viên</option>
                                        <option value="PARENT">Phụ huynh</option>
                                        <option value="MONITOR">Quản lý xe</option>
                                        <option value="DRIVER">Lái xe</option>
                                    </select>
                                </div>
                            </div>

                            <div class="table-wrap">
                                <table id="usersTable">
                                    <thead>
                                        <tr>
                                            <th>#</th>
                                            <th>Tên</th>
                                            <th>Tài khoản</th>
                                            <th>Email / SĐT</th>
                                            <th>Vai trò</th>
                                            <th>Trạng thái</th>
                                            <th>Hành động</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="u" items="${users}" varStatus="st">
                                            <tr data-name="${u.fullName.toLowerCase()}"
                                                data-username="${u.username.toLowerCase()}" data-role="${u.role}">
                                                <td class="text-muted">${st.index + 1}</td>
                                                <td>
                                                    <div style="display:flex;align-items:center;gap:10px;">
                                                        <div class="user-avatar"
                                                            style="width:32px;height:32px;font-size:14px;">
                                                            ${u.fullName.charAt(0)}
                                                        </div>
                                                        <div>
                                                            <div style="font-weight:600;font-size:13px;">${u.fullName}
                                                            </div>
                                                            <div class="text-xs text-muted">Tạo:
                                                                <c:out value="${u.createdAt}" />
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td class="text-sm"><code
                                                        style="background:rgba(255,255,255,0.06);padding:2px 6px;border-radius:4px;">${u.username}</code>
                                                </td>
                                                <td class="text-sm">
                                                    <div>${empty u.email ? '—' : u.email}</div>
                                                    <div class="text-muted">${empty u.phone ? '' : u.phone}</div>
                                                </td>
                                                <td><span class="badge ${u.roleBadgeClass}">${u.roleDisplay}</span></td>
                                                <td>
                                                    <span class="badge ${u.active ? 'badge-success' : 'badge-danger'}">
                                                        ${u.active ? '✅ Hoạt động' : '❌ Vô hiệu'}
                                                    </span>
                                                </td>
                                                <td>
                                                    <div style="display:flex;gap:6px;">
                                                        <button class="btn btn-ghost btn-sm"
                                                            onclick="openEdit(${u.id},'${u.fullName.replace("'","\\'")}','${u.email}','${u.phone}','${u.role}',${u.active ? 1 : 0})">
                                                            ✏️
                                                        </button>
                                                        <a href="${pageContext.request.contextPath}/admin/users?action=toggle&id=${u.id}"
                                                            class="btn ${u.active ? 'btn-danger' : 'btn-success'} btn-sm"
                                                            onclick="return confirm('Xác nhận thay đổi trạng thái?')">
                                                            ${u.active ? '🚫' : '✅'}
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/admin/users?action=resetpwd&id=${u.id}"
                                                            class="btn btn-warning btn-sm"
                                                            onclick="return confirm('Reset mật khẩu về Admin@123?')">🔑</a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
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
                        <div class="modal-title">➕ Thêm người dùng mới</div>
                        <button class="modal-close" onclick="hideModal('addModal')">✕</button>
                    </div>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/users">
                        <input type="hidden" name="action" value="add">
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Tên đăng nhập *</label>
                                <input name="username" class="form-control" required placeholder="username">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Vai trò *</label>
                                <select name="role" class="form-control" required>
                                    <option value="PARENT">Phụ huynh</option>
                                    <option value="DRIVER">Lái xe</option>
                                    <option value="MONITOR">Quản lý xe</option>
                                    <option value="ADMIN">Quản trị viên</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Họ và tên *</label>
                            <input name="fullName" class="form-control" required placeholder="Nguyễn Văn A">
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Email</label>
                                <input name="email" type="email" class="form-control" placeholder="email@example.com">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Số điện thoại</label>
                                <input name="phone" class="form-control" placeholder="09xx...">
                            </div>
                        </div>
                        <p class="text-sm text-muted mb-4">Mật khẩu mặc định: <strong>Admin@123</strong></p>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-ghost" onclick="hideModal('addModal')">Hủy</button>
                            <button type="submit" class="btn btn-primary">Thêm người dùng</button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Edit Modal -->
            <div class="modal-overlay" id="editModal">
                <div class="modal">
                    <div class="modal-header">
                        <div class="modal-title">✏️ Chỉnh sửa người dùng</div>
                        <button class="modal-close" onclick="hideModal('editModal')">✕</button>
                    </div>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/users">
                        <input type="hidden" name="action" value="edit">
                        <input type="hidden" name="id" id="editId">
                        <div class="form-group">
                            <label class="form-label">Họ và tên *</label>
                            <input name="fullName" id="editFullName" class="form-control" required>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Email</label>
                                <input name="email" id="editEmail" type="email" class="form-control">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Số điện thoại</label>
                                <input name="phone" id="editPhone" class="form-control">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Vai trò</label>
                                <select name="role" id="editRole" class="form-control">
                                    <option value="PARENT">Phụ huynh</option>
                                    <option value="DRIVER">Lái xe</option>
                                    <option value="MONITOR">Quản lý xe</option>
                                    <option value="ADMIN">Quản trị viên</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Trạng thái</label>
                                <select name="isActive" id="editActive" class="form-control">
                                    <option value="1">Hoạt động</option>
                                    <option value="0">Vô hiệu hóa</option>
                                </select>
                            </div>
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
                function openEdit(id, name, email, phone, role, active) {
                    document.getElementById('editId').value = id;
                    document.getElementById('editFullName').value = name;
                    document.getElementById('editEmail').value = email;
                    document.getElementById('editPhone').value = phone;
                    document.getElementById('editRole').value = role;
                    document.getElementById('editActive').value = active;
                    showModal('editModal');
                }
                // Search/Filter
                document.getElementById('searchInput').addEventListener('input', filterTable);
                document.getElementById('roleFilter').addEventListener('change', filterTable);
                function filterTable() {
                    const q = document.getElementById('searchInput').value.toLowerCase();
                    const role = document.getElementById('roleFilter').value;
                    document.querySelectorAll('#usersTable tbody tr').forEach(tr => {
                        const name = tr.dataset.name || '';
                        const uname = tr.dataset.username || '';
                        const r = tr.dataset.role || '';
                        const matchQ = name.includes(q) || uname.includes(q);
                        const matchR = !role || r === role;
                        tr.style.display = matchQ && matchR ? '' : 'none';
                    });
                }
                window.onclick = e => { if (e.target.classList.contains('modal-overlay')) e.target.classList.remove('show'); };
            </script>
        </body>

        </html>