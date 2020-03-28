package weaver.sysinterface;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.formmode.setup.ModeRightInfo;
import weaver.formmode.view.ModeShareManager;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.DateHelper;

public class PayList_CheckSecondBack extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		this.request = request;
		this.response = response;
		try {
			String action = Util.null2String(request.getParameter("action"));
			User user = MobileUserInit.getUser(request, response);
			if(user == null){
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");  
				response.getWriter().print(checkUser.toString());
				return;
			}
			if("getCheckSecondBack".equals(action)){   //处理接口
				getCheckSecondBack(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取数据共享权限 sql
	 * @param formmodeid	建模id
	 * @param userid	用户id
	 * @return
	 */
	public String getShareSql(int formmodeid,int userid){
		RecordSet rs = new RecordSet();
		User user = new User();
		String sql = "";
		if(userid==1){
			sql = "select * from HrmResourceManager where id="+userid;
		}else{
			sql="select * from HrmResource where id="+userid;
		}
		rs.executeSql(sql);
        rs.next();
		user.setUid(rs.getInt("id"));
        user.setLoginid(rs.getString("loginid"));
        user.setSeclevel(rs.getString("seclevel"));
        user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
        user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
        user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
        user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
        user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
        user.setManagerid(rs.getString("managerid"));
        user.setLogintype("1");
		
        ModeShareManager modeshare = new ModeShareManager();
        modeshare.setModeId(formmodeid);
        String rightsql = modeshare.getShareDetailTableByUser("formmode",user);
        return rightsql;
	}
	/**
	 * 银企直连-退回接口
	 */
	private void getCheckSecondBack(User user){
		String id=Util.null2String(request.getParameter("id"));
		String userid = String.valueOf(user.getUID());
		String recheck=Util.null2String(Prop.getPropValue("EASPersonJuri",
				"recheck"));    //复核权限人
		if(recheck.equals(userid)){
			RecordSet rsd=new RecordSet();
			rsd.executeSql("select * from uf_payList where id='"+id+"'");
			String status="";//处理状态
			if(rsd.next()){
				status=Util.null2String(rsd.getString("dealstatus"));//处理状态
			}
			
			if("2".equals(status)){
				RecordSet rs=new RecordSet();
				String sql="update uf_payList set dealstatus='0' where id='"+id+"'";
				rs.executeSql(sql);
				try {
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print("2");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				try {
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print("1");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			try {
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print("0");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
