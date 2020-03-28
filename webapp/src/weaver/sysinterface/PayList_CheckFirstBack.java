package weaver.sysinterface;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.io.IOException;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.formmode.view.ModeShareManager;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;

public class PayList_CheckFirstBack extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;

	private HttpServletResponse response;

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		try {

			BaseBean log_01 = new BaseBean();

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
			if ("getCheckFirstBack".equals(action)) { // 处理接口
				log_01.writeLog("银企直连开始：_____01_____");
				getCheckFirstBack(user);
				log_01.writeLog("银企直连结束：_____02_____");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 银企直连-处理接口
	 */
	private void getCheckFirstBack(User user) {
		BaseBean log_03 = new BaseBean();
		String id = Util.null2String(request.getParameter("id"));
		String userid = String.valueOf(user.getUID());
		String firstTrial=Util.null2String(Prop.getPropValue("EASPersonJuri",
				"firstTrial"));  //初审权限人
		if (userid.equals(firstTrial)) {
			log_03.writeLog("初审退回开始：_____01_____操作人误" + userid);
			log_03.writeLog("初审退回开始：_____02_____操作数据ID" + id);
			RecordSet rsd = new RecordSet();
			rsd.executeSql("select dealstatus,showOrHide from uf_payList where id='"
					+ id + "'");
			String status = "";
			String showHide = "";
			if (rsd.next()) {
				status = Util.null2String(rsd.getString("dealstatus"));
				showHide = Util.null2String(rsd.getString("showOrHide"));
			}
			if (status.equals("0") && showHide.equals("0")) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String FirstBackDateNew = dateFormat.format(new Date());

				RecordSet rs = new RecordSet();
				String sql = "update uf_payList set dealstatus='1',firstBackDate='"
						+ FirstBackDateNew + "' where id='" + id + "'";
				log_03.writeLog("初审退回开始：_____03_____updateSQL" + sql);
				rs.executeSql(sql);
				try {
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print("1");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				log_03.writeLog("初审退回开始：_____05_____操作人或状态错误" + userid
						+ "_____" + status);
				response.setContentType("application/json; charset=utf-8");
				try {
					response.getWriter().print("0");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			response.setContentType("application/json; charset=utf-8");
			try {
				response.getWriter().print("2");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
