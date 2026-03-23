<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <% model.User loggedUser=(model.User) session.getAttribute("loggedUser"); String
            currentPath=request.getServletPath(); %>

            <div class="sidebar">
                <div class="sidebar-logo">
                    <div class="logo-icon">🚌</div>
                    <div>
                        <div class="logo-text">BusTrack</div>
                        <div class="logo-sub">Quản lý Xe Buýt</div>
                    </div>
                </div>

                <nav class="sidebar-nav">
                    <% if ("ADMIN".equals(loggedUser.getRole())) { %>
                        <div class="nav-section">
                            <div class="nav-section-title">Quản trị</div>
                            <a href="${pageContext.request.contextPath}/admin/dashboard"
                                class="nav-item <%= currentPath.contains(" /admin/dashboard") ? "active" : "" %>">
                                <span class="nav-icon">📊</span> Tổng quan
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/users"
                                class="nav-item <%= currentPath.contains(" /admin/users") ? "active" : "" %>">
                                <span class="nav-icon">👥</span> Người dùng
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/routes"
                                class="nav-item <%= currentPath.contains(" /admin/routes") ? "active" : "" %>">
                                <span class="nav-icon">🗺️</span> Tuyến xe
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/vehicles"
                                class="nav-item <%= currentPath.contains(" /admin/vehicles") ? "active" : "" %>">
                                <span class="nav-icon">🚌</span> Xe buýt
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/students"
                                class="nav-item <%= currentPath.contains(" /admin/students") ? "active" : "" %>">
                                <span class="nav-icon">🎒</span> Học sinh
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/trips"
                                class="nav-item <%= currentPath.contains(" /admin/trips") ? "active" : "" %>">
                                <span class="nav-icon">📅</span> Chuyến xe
                            </a>
                        </div>
                        <% } %>

                            <% if ("PARENT".equals(loggedUser.getRole())) { %>
                                <div class="nav-section">
                                    <div class="nav-section-title">Phụ huynh</div>
                                    <a href="${pageContext.request.contextPath}/parent/dashboard"
                                        class="nav-item <%= currentPath.contains(" /parent/dashboard") ? "active" : ""
                                        %>">
                                        <span class="nav-icon">🏠</span> Trang chủ
                                    </a>
                                    <a href="${pageContext.request.contextPath}/parent/register"
                                        class="nav-item <%= currentPath.contains(" /parent/register") ? "active" : ""
                                        %>">
                                        <span class="nav-icon">📝</span> Đăng ký xe
                                    </a>
                                    <a href="${pageContext.request.contextPath}/parent/absence"
                                        class="nav-item <%= currentPath.contains(" /parent/absence") ? "active" : ""
                                        %>">
                                        <span class="nav-icon">📣</span> Báo nghỉ
                                    </a>
                                </div>
                                <% } %>

                                    <% if ("MONITOR".equals(loggedUser.getRole())) { %>
                                        <div class="nav-section">
                                            <div class="nav-section-title">Quản lý xe</div>
                                            <a href="${pageContext.request.contextPath}/monitor/dashboard"
                                                class="nav-item <%= currentPath.contains(" /monitor/dashboard")
                                                ? "active" : "" %>">
                                                <span class="nav-icon">📊</span> Tổng quan
                                            </a>
                                            <a href="${pageContext.request.contextPath}/monitor/trip"
                                                class="nav-item <%= currentPath.contains("/monitor/trip") ? "active"
                                                : "" %>">
                                                <span class="nav-icon">🚦</span> Quản lý chuyến
                                            </a>
                                            <a href="${pageContext.request.contextPath}/monitor/absence"
                                                class="nav-item <%= currentPath.contains("/monitor/absence") ? "active"
                                                : "" %>">
                                                <span class="nav-icon">📣</span> Quản lý nghỉ học
                                            </a>
                                        </div>
                                        <% } %>

                                            <% if ("DRIVER".equals(loggedUser.getRole())) { %>
                                                <div class="nav-section">
                                                    <div class="nav-section-title">Lái xe</div>
                                                    <a href="${pageContext.request.contextPath}/driver/dashboard"
                                                        class="nav-item <%= currentPath.contains(" /driver/dashboard")
                                                        ? "active" : "" %>">
                                                        <span class="nav-icon">🚗</span> Dashboard
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/driver/report"
                                                        class="nav-item <%= currentPath.contains(" /driver/report")
                                                        ? "active" : "" %>">
                                                        <span class="nav-icon">⚠️</span> Báo cáo xe
                                                    </a>
                                                </div>
                                                <% } %>
                </nav>

                <div class="sidebar-footer">
                    <div class="user-info">
                        <div class="user-avatar">
                            <%= loggedUser.getFullName().charAt(0) %>
                        </div>
                        <div>
                            <div class="user-name">
                                <%= loggedUser.getFullName() %>
                            </div>
                            <div class="user-role">
                                <%= loggedUser.getRoleDisplay() %>
                            </div>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/logout" class="btn-logout">
                        🚪 Đăng xuất
                    </a>
                </div>
            </div>