package servlet.admin;

import dal.StopDAO;
import dal.RouteDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.Stop;
import java.io.IOException;
import java.sql.Time;

/**
 * StopManagementServlet – Quản lý điểm dừng (Admin).
 *
 * URL: /admin/stops
 *
 * GET /admin/stops → Danh sách tất cả điểm dừng
 * GET /admin/stops?routeId=X → Điểm dừng của tuyến X
 * POST /admin/stops action=add → Thêm điểm dừng mới
 * POST /admin/stops action=edit → Cập nhật điểm dừng
 * POST /admin/stops action=delete → Xóa điểm dừng
 */
public class StopManagementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StopDAO stopDAO = new StopDAO();
        RouteDAO routeDAO = new RouteDAO();

        String routeIdStr = request.getParameter("routeId");

        if (routeIdStr != null && !routeIdStr.isEmpty()) {
            int routeId = Integer.parseInt(routeIdStr);
            request.setAttribute("stops", stopDAO.getByRoute(routeId));
            request.setAttribute("route", routeDAO.getById(routeId));
            request.setAttribute("routeId", routeId);
        } else {
            request.setAttribute("stops", stopDAO.getAll());
        }

        request.setAttribute("routes", routeDAO.getAll());
        request.setAttribute("msg", request.getParameter("msg"));
        request.getRequestDispatcher("/WEB-INF/views/admin/stops.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        StopDAO stopDAO = new StopDAO();
        String action = request.getParameter("action");
        String routeIdParam = request.getParameter("routeId");

        if ("add".equals(action)) {
            Stop stop = new Stop();
            stop.setRouteId(Integer.parseInt(routeIdParam));
            stop.setStopName(request.getParameter("stopName"));
            stop.setStopOrder(parseIntSafe(request.getParameter("stopOrder"), 1));
            stop.setAddress(request.getParameter("address"));
            stop.setEstimatedMorningTime(parseTime(request.getParameter("estimatedMorningTime")));
            stop.setEstimatedAfternoonTime(parseTime(request.getParameter("estimatedAfternoonTime")));
            stopDAO.insert(stop);
            response.sendRedirect(request.getContextPath()
                    + "/admin/stops?routeId=" + routeIdParam + "&msg=added");

        } else if ("edit".equals(action)) {
            Stop stop = new Stop();
            stop.setId(Integer.parseInt(request.getParameter("id")));
            stop.setRouteId(Integer.parseInt(routeIdParam));
            stop.setStopName(request.getParameter("stopName"));
            stop.setStopOrder(parseIntSafe(request.getParameter("stopOrder"), 1));
            stop.setAddress(request.getParameter("address"));
            stop.setEstimatedMorningTime(parseTime(request.getParameter("estimatedMorningTime")));
            stop.setEstimatedAfternoonTime(parseTime(request.getParameter("estimatedAfternoonTime")));
            stopDAO.update(stop);
            response.sendRedirect(request.getContextPath()
                    + "/admin/stops?routeId=" + routeIdParam + "&msg=updated");

        } else if ("delete".equals(action)) {
            stopDAO.delete(Integer.parseInt(request.getParameter("id")));
            response.sendRedirect(request.getContextPath()
                    + "/admin/stops?routeId=" + routeIdParam + "&msg=deleted");
        }
    }

    /**
     * Chuyển đổi chuỗi "HH:mm" thành java.sql.Time, trả về null nếu không hợp lệ.
     *
     * @param timeStr Chuỗi giờ dạng "HH:mm", hoặc null/rỗng
     * @return java.sql.Time hoặc null
     */
    private Time parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty())
            return null;
        try {
            return Time.valueOf(timeStr + ":00"); // "HH:mm" → "HH:mm:ss"
        } catch (IllegalArgumentException exception) {
            System.err.println("[StopManagementServlet] Giờ không hợp lệ: " + timeStr);
            return null;
        }
    }

    /**
     * Chuyển đổi chuỗi thành int an toàn, trả về defaultValue nếu không hợp lệ.
     *
     * @param value        Chuỗi cần chuyển
     * @param defaultValue Giá trị mặc định nếu parse thất bại
     */
    private int parseIntSafe(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception exception) {
            return defaultValue;
        }
    }
}
