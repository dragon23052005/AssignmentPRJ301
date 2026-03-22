package servlet.monitor;

import dal.AbsenceNotificationDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.AbsenceNotification;
import model.User;
import java.io.IOException;
import java.util.List;

public class AbsenceManagementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        AbsenceNotificationDAO anDAO = new AbsenceNotificationDAO();

        String action = request.getParameter("action");
        String idStr = request.getParameter("id");

        if ("acknowledge".equals(action) && idStr != null) {
            User user = (User) request.getSession().getAttribute("loggedUser");
            int id = Integer.parseInt(idStr);
            anDAO.acknowledge(id, user.getId());
            response.sendRedirect(request.getContextPath() + "/monitor/absence?msg=ack");
            return;
        }

        List<AbsenceNotification> todayAbsences = anDAO.getToday();
        List<AbsenceNotification> allAbsences = anDAO.getAll();

        request.setAttribute("todayAbsences", todayAbsences);
        request.setAttribute("allAbsences", allAbsences);
        request.setAttribute("msg", request.getParameter("msg"));

        request.getRequestDispatcher("/WEB-INF/views/monitor/absence.jsp").forward(request, response);
    }
}
