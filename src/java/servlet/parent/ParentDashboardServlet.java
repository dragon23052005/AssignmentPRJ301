package servlet.parent;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.*;
import java.io.IOException;
import java.util.List;

/**
 * ParentDashboardServlet – Trang chủ của Phụ huynh.
 *
 * URL: GET /parent/dashboard
 *
 * Hiển thị thông tin theo dõi con em đi xe buýt:
 *   - Danh sách con em của phụ huynh
 *   - Thông tin con được chọn (mặc định là con đầu tiên)
 *   - Trạng thái chuyến sáng/chiều hôm nay của con
 *   - Điểm danh (đã lên xe chưa, điểm dừng nào)
 *   - Trạng thái báo nghỉ hôm nay
 *   - Lịch sử thông báo nghỉ gần đây
 *
 * Phụ huynh có thể có nhiều con → hỗ trợ chọn con qua param ?studentId=X
 *
 * Nếu học sinh chưa đăng ký tuyến (routeId=0) → không tìm chuyến (tránh crash).
 */
public class ParentDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StudentDAO            studentDAO = new StudentDAO();
        TripDAO               tripDAO    = new TripDAO();
        AttendanceDAO         attDAO     = new AttendanceDAO();
        AbsenceNotificationDAO anDAO     = new AbsenceNotificationDAO();

        // Lấy phụ huynh đang đăng nhập từ session
        User user = (User) request.getSession().getAttribute("loggedUser");

        // Load danh sách con em
        List<Student> students = studentDAO.getByParentId(user.getId());
        request.setAttribute("students", students);

        if (!students.isEmpty()) {
            // Mặc định chọn con đầu tiên; phụ huynh có thể chọn con khác qua param
            Student selectedStudent = students.get(0);
            String selId = request.getParameter("studentId");

            if (selId != null) {
                // Tìm con được chọn theo ID trong danh sách (tránh lấy con của người khác)
                int targetId = Integer.parseInt(selId);
                for (Student st : students) {
                    if (st.getId() == targetId) {
                        selectedStudent = st;
                        break;
                    }
                }
            }
            request.setAttribute("selectedStudent", selectedStudent);

            // CHỈ tìm chuyến nếu học sinh đã đăng ký tuyến (routeId > 0)
            if (selectedStudent.getRouteId() > 0) {
                int routeId = selectedStudent.getRouteId();

                // Lấy chuyến sáng và chiều hôm nay của tuyến học sinh
                Trip morningTrip   = tripDAO.getTodayTripByRoute(routeId, "MORNING");
                Trip afternoonTrip = tripDAO.getTodayTripByRoute(routeId, "AFTERNOON");
                request.setAttribute("morningTrip",   morningTrip);
                request.setAttribute("afternoonTrip", afternoonTrip);

                // Load điểm danh của học sinh trong từng chuyến (nếu chuyến đã được tạo)
                if (morningTrip != null) {
                    request.setAttribute("morningAtt",
                        attDAO.getByTripAndStudent(morningTrip.getId(), selectedStudent.getId()));
                }
                if (afternoonTrip != null) {
                    request.setAttribute("afternoonAtt",
                        attDAO.getByTripAndStudent(afternoonTrip.getId(), selectedStudent.getId()));
                }
            }

            // Kiểm tra học sinh có báo nghỉ hôm nay không (để hiển thị badge)
            request.setAttribute("isAbsentToday", anDAO.isAbsentToday(selectedStudent.getId()));
        }

        // Lịch sử thông báo nghỉ của tất cả con em (không chỉ con được chọn)
        request.setAttribute("recentAbsences", anDAO.getByParent(user.getId()));
        request.getRequestDispatcher("/WEB-INF/views/parent/dashboard.jsp").forward(request, response);
    }
}
