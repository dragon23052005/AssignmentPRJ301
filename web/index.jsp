<%@ page contentType="text/html;charset=UTF-8" %>
    <% Object userObj=session.getAttribute("loggedUser"); if (userObj==null) {
        response.sendRedirect(request.getContextPath() + "/login" ); } else { model.User u=(model.User) userObj; String
        role=u.getRole(); String redirect; if ("ADMIN".equals(role)) { redirect=request.getContextPath()
        + "/admin/dashboard" ; } else if ("PARENT".equals(role)) { redirect=request.getContextPath()
        + "/parent/dashboard" ; } else if ("MONITOR".equals(role)) { redirect=request.getContextPath()
        + "/monitor/dashboard" ; } else if ("DRIVER".equals(role)) { redirect=request.getContextPath()
        + "/driver/dashboard" ; } else { redirect=request.getContextPath() + "/login" ; }
        response.sendRedirect(redirect); } %>