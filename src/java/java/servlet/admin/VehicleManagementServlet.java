package servlet.admin;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.Vehicle;
import java.io.IOException;

/**
 * VehicleManagementServlet – Quản lý xe buýt (Admin).
 *
 * URL: /admin/vehicles
 *
 * GET /admin/vehicles → Danh sách xe + dropdown chọn driver/monitor/route
 * GET /admin/vehicles?action=toggle&id=X → Bật/tắt trạng thái xe
 * POST /admin/vehicles action=add → Thêm xe mới
 * POST /admin/vehicles action=edit → Cập nhật thông tin xe
 *
 * Lưu ý khi parse form:
 * - capacity: có thể rỗng → mặc định 45 chỗ
 * - driverId, monitorId, routeId: có thể rỗng (chưa phân công) → 0 → DAO lưu
 * NULL
 */
public class VehicleManagementServlet extends HttpServlet {

    /**
     * GET: Hiển thị danh sách xe và xử lý action toggle trạng thái.
     * Load thêm danh sách Driver, Monitor, Route để tạo dropdown.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        VehicleDAO vehicleDAO = new VehicleDAO();
        UserDAO userDAO = new UserDAO();
        RouteDAO routeDAO = new RouteDAO();

        String action = request.getParameter("action");

        if ("toggle".equals(action)) {
            // Đảo trạng thái active của xe
            Vehicle vehicle = vehicleDAO.getById(Integer.parseInt(request.getParameter("id")));
            if (vehicle != null) {
                vehicle.setActive(!vehicle.isActive()); // Đảo boolean
                vehicleDAO.update(vehicle);
            }
            response.sendRedirect(request.getContextPath() + "/admin/vehicles?msg=updated");
            return;
        }

        // Danh sách xe để hiển thị
        request.setAttribute("vehicles", vehicleDAO.getAll());
        // Dropdown phân công: chỉ lấy user đang active theo role
        request.setAttribute("drivers", userDAO.getByRole("DRIVER"));
        request.setAttribute("monitors", userDAO.getByRole("MONITOR"));
        request.setAttribute("routes", routeDAO.getActive()); // Chỉ tuyến đang active
        request.setAttribute("msg", request.getParameter("msg"));
        request.getRequestDispatcher("/WEB-INF/views/admin/vehicles.jsp").forward(request, response);
    }

    /**
     * POST: Xử lý thêm mới và cập nhật xe.
     *
     * Cả "add" và "edit" đều parse cùng các trường → dùng helper
     * parseVehicleFromRequest()
     * để tránh lặp code (DRY principle).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        VehicleDAO vehicleDAO = new VehicleDAO();
        String action = request.getParameter("action");

        // Parse dữ liệu xe từ form (dùng chung cho add và edit)
        Vehicle vehicle = parseVehicleFromRequest(request);

        if ("add".equals(action)) {
            vehicleDAO.insert(vehicle);
            response.sendRedirect(request.getContextPath() + "/admin/vehicles?msg=added");

        } else if ("edit".equals(action)) {
            // Khi edit cần biết ID xe và trạng thái active
            vehicle.setId(Integer.parseInt(request.getParameter("id")));
            vehicle.setActive("1".equals(request.getParameter("isActive")));
            vehicleDAO.update(vehicle);
            response.sendRedirect(request.getContextPath() + "/admin/vehicles?msg=updated");
        }
    }

    /**
     * Parse dữ liệu xe từ HttpServletRequest, xử lý null/empty an toàn.
     *
     * Tách ra thành method riêng để doPost() không bị lặp code khi có 2 action
     * (add và edit) cùng parse các trường giống nhau.
     *
     * Quy tắc parse:
     * - capacity rỗng → 45 (sức chứa mặc định)
     * - driverId/monitorId/routeId rỗng → 0 (DAO sẽ lưu NULL vào DB)
     *
     * @param request HttpServletRequest chứa dữ liệu form
     * @return Đối tượng Vehicle được điền dữ liệu từ form
     */
    private Vehicle parseVehicleFromRequest(HttpServletRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(request.getParameter("plateNumber"));
        vehicle.setBrand(request.getParameter("brand"));
        vehicle.setModel(request.getParameter("model"));

        String capacityStr = request.getParameter("capacity");
        String driverIdStr = request.getParameter("driverId");
        String monitorIdStr = request.getParameter("monitorId");
        String routeIdStr = request.getParameter("routeId");

        vehicle.setCapacity(
                capacityStr != null && !capacityStr.isEmpty() ? Integer.parseInt(capacityStr) : 45);
        // 0 = chưa phân công; DAO sẽ chuyển thành NULL trong DB
        vehicle.setDriverId(driverIdStr != null && !driverIdStr.isEmpty() ? Integer.parseInt(driverIdStr) : 0);
        vehicle.setMonitorId(monitorIdStr != null && !monitorIdStr.isEmpty() ? Integer.parseInt(monitorIdStr) : 0);
        vehicle.setRouteId(routeIdStr != null && !routeIdStr.isEmpty() ? Integer.parseInt(routeIdStr) : 0);
        return vehicle;
    }
}
