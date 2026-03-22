package servlet.admin;

import dal.RouteDAO;
import dal.StopDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.Route;
import model.Stop;
import java.io.IOException;
import java.sql.Time;

/**
 * RouteManagementServlet – Quản lý tuyến đường và điểm dừng (Admin).
 *
 * URL: /admin/routes
 *
 * GET /admin/routes → Danh sách tuyến đường
 * GET /admin/routes?action=stops&routeId=X → Xem điểm dừng của tuyến X
 * GET /admin/routes?action=delete&id=X → Vô hiệu hóa tuyến X (soft delete)
 * POST /admin/routes action=add → Thêm tuyến mới
 * POST /admin/routes action=edit → Sửa tuyến
 * POST /admin/routes action=addStop → Thêm điểm dừng vào tuyến
 * POST /admin/routes action=deleteStop → Xóa điểm dừng
 */
public class RouteManagementServlet extends HttpServlet {

    /**
     * GET: Hiển thị danh sách tuyến hoặc danh sách điểm dừng của 1 tuyến.
     * Cũng xử lý action delete (soft delete tuyến).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Khởi tạo DAO cục bộ (tránh connection leak với instance field)
        RouteDAO routeDAO = new RouteDAO();
        StopDAO stopDAO = new StopDAO();
        String action = request.getParameter("action");

        if ("stops".equals(action)) {
            // Xem điểm dừng của tuyến cụ thể
            int routeId = Integer.parseInt(request.getParameter("routeId"));
            request.setAttribute("route", routeDAO.getById(routeId));
            request.setAttribute("stops", stopDAO.getByRoute(routeId));
            request.getRequestDispatcher("/WEB-INF/views/admin/stops.jsp").forward(request, response);
            return;
        }

        if ("delete".equals(action)) {
            // Soft delete: set is_active=0 thay vì xóa cứng
            routeDAO.delete(Integer.parseInt(request.getParameter("id")));
            response.sendRedirect(request.getContextPath() + "/admin/routes?msg=deleted");
            return;
        }

        // Mặc định: hiển thị danh sách tuyến
        request.setAttribute("routes", routeDAO.getAll());
        request.setAttribute("msg", request.getParameter("msg"));
        request.getRequestDispatcher("/WEB-INF/views/admin/routes.jsp").forward(request, response);
    }

    /**
     * POST: Xử lý thêm/sửa tuyến và thêm/xóa điểm dừng.
     *
     * Lưu ý chuyển đổi giờ (Time parsing):
     * Input HTML type="time" trả về "HH:mm" (vd: "06:30")
     * SQL Server cần "HH:mm:ss" → phải append ":00"
     * Helper: parseTime(String) xử lý null-safe.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        RouteDAO routeDAO = new RouteDAO();
        StopDAO stopDAO = new StopDAO();
        String action = request.getParameter("action");

        if ("add".equals(action)) {
            // Thêm tuyến mới
            Route route = new Route();
            route.setRouteName(request.getParameter("routeName"));
            route.setRouteCode(request.getParameter("routeCode"));
            route.setDescription(request.getParameter("description"));
            route.setMorningDeparture(parseTime(request.getParameter("morningDeparture")));
            route.setAfternoonDeparture(parseTime(request.getParameter("afternoonDeparture")));
            routeDAO.insert(route);
            response.sendRedirect(request.getContextPath() + "/admin/routes?msg=added");

        } else if ("edit".equals(action)) {
            // Sửa tuyến hiện có (không thay đổi route_code)
            Route route = new Route();
            route.setId(Integer.parseInt(request.getParameter("id")));
            route.setRouteName(request.getParameter("routeName"));
            route.setDescription(request.getParameter("description"));
            route.setMorningDeparture(parseTime(request.getParameter("morningDeparture")));
            route.setAfternoonDeparture(parseTime(request.getParameter("afternoonDeparture")));
            route.setActive("1".equals(request.getParameter("isActive")));
            routeDAO.update(route);
            response.sendRedirect(request.getContextPath() + "/admin/routes?msg=updated");

        } else if ("addStop".equals(action)) {
            // Thêm điểm dừng mới vào tuyến
            Stop stop = new Stop();
            stop.setRouteId(Integer.parseInt(request.getParameter("routeId")));
            stop.setStopName(request.getParameter("stopName"));
            stop.setStopOrder(Integer.parseInt(request.getParameter("stopOrder")));
            stop.setAddress(request.getParameter("address"));
            stop.setEstimatedMorningTime(parseTime(request.getParameter("estimatedMorningTime")));
            stop.setEstimatedAfternoonTime(parseTime(request.getParameter("estimatedAfternoonTime")));
            stopDAO.insert(stop);
            response.sendRedirect(request.getContextPath()
                    + "/admin/routes?action=stops&routeId=" + stop.getRouteId() + "&msg=stopAdded");

        } else if ("deleteStop".equals(action)) {
            // Xóa cứng điểm dừng (cẩn thận FK constraint nếu đang được tham chiếu)
            int stopId = Integer.parseInt(request.getParameter("stopId"));
            int routeId = Integer.parseInt(request.getParameter("routeId"));
            stopDAO.delete(stopId);
            response.sendRedirect(request.getContextPath()
                    + "/admin/routes?action=stops&routeId=" + routeId + "&msg=stopDeleted");
        }
    }

    /**
     * Chuyển đổi chuỗi "HH:mm" thành java.sql.Time, trả về null nếu không hợp lệ.
     * HTML input[type=time] trả về "HH:mm", nhưng Time.valueOf() cần "HH:mm:ss".
     *
     * Tái sử dụng ở cả add và edit để tránh lặp code.
     *
     * @param timeStr Chuỗi giờ dạng "HH:mm", hoặc null/rỗng
     * @return java.sql.Time hoặc null
     */
    private Time parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty())
            return null;
        try {
            return Time.valueOf(timeStr + ":00"); // "06:30" → "06:30:00"
        } catch (IllegalArgumentException exception) {
            System.err.println("[RouteManagementServlet] Giờ không hợp lệ: " + timeStr);
            return null;
        }
    }
}
