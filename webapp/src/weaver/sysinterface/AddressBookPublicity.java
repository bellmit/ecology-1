package weaver.sysinterface;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;

/**
 * 通讯录公示
 * @author lsq
 * @date 2019/6/17
 * @version 1.0
 */
public class AddressBookPublicity extends HttpServlet{
	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;

	private HttpServletResponse response;

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		try {
			String action = Util.null2String(request.getParameter("action"));
			User user = MobileUserInit.getUser(request, response);
			if (user == null) {
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(checkUser.toString());
				return;
			}
			if ("getPublicity".equals(action)) { // 处理接口
				getPublicity(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    /**
     * 公示
     * @param user
     */
	private void getPublicity(User user) {
		RecordSet rs=new RecordSet();
		String pub=Util.null2String(request.getParameter("pub"));
		String userid = String.valueOf(user.getUID());
		String sql="update hrmresource set publicity='"+pub+"' where id='"+userid+"'";	
		rs.executeSql(sql);
	}
}
