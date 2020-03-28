package weaver.sysinterface;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.OnLineMonitor;
import weaver.hrm.User;
import weaver.hrm.settings.RemindSettings;
import weaver.login.LicenseCheckLogin;
import weaver.systeminfo.SysMaintenanceLog;
public class BaseAction extends HttpServlet {
	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	BaseBean baseBean = new BaseBean();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){    
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		String action = Util.null2String(request.getParameter("action"));
		User user = this.getUser(request, response);
		response.setContentType("application/json; charset=utf-8");
		if(user==null||user.getUID()==-1||user.getUID()==0){
			try {
				JSONObject json = new JSONObject();
				json.put("code", "-1");
				json.put("message", "还未登陆");
				response.getWriter().print(json.toString());
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		DocDataService docDataService = new DocDataService(request,response);
		SendMessageService sendMessageService = new SendMessageService(request,response);
		MeetingDataService meetingDataService = new MeetingDataService(request,response);
		if(action.equals("logout")){
			try {
				HttpSession session =  request.getSession();// 获取session
		        ServletContext application = this.getServletContext();// 获取application
				RemindSettings settings0 = (RemindSettings) application.getAttribute("hrmsettings");
				Map logmessages = (Map) application.getAttribute("logmessages");
				String a_logmessage = "";
				if (logmessages != null)
					a_logmessage = Util.null2String((String)logmessages.get(""+ user.getUID()));
				String s_logmessage = Util.null2String((String)session.getAttribute("logmessage"));
				if (s_logmessage == null)
					s_logmessage = "";
				String relogin0 = Util.null2String(settings0.getRelogin());
				String fromPDA = Util.null2String((String)session.getAttribute("loginPAD"));
				//hubo 清除小窗口登录标识
				if (request.getSession(true).getAttribute("layoutStyle") != null)
					request.getSession(true).setAttribute("layoutStyle", null);
				
				//xiaofeng 有效的登入者在退出时清除登陆标记,踢出的用户直接退出
				if (!relogin0.equals("1") && !s_logmessage.equals(a_logmessage)) {
					if (fromPDA.equals("1")) {
					} else {
					}
					return;
				} else {
					logmessages = (Map) application.getAttribute("logmessages");
					if (logmessages != null)
						logmessages.remove("" + user.getUID());
				}
				String loginfile = Util.getCookie(request, "loginfileweaver");
				LicenseCheckLogin LicenseCheckLogin = new LicenseCheckLogin();
				LicenseCheckLogin.updateOnlinFlag(""+ user.getUID());
				request.getSession(true).removeValue("moniter");
				request.getSession(true).removeValue("WeaverMailSet");
				request.getSession(true).invalidate();
				JSONObject json = new JSONObject();
				json.put("code", "0");
				json.put("message", "注销成功");
				response.getWriter().print(json.toString());
				return;
			} catch (Exception e) {
				baseBean.writeLog("获取提醒异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("sendMessage")){
			try {
				String requestname = Util.null2String(request.getParameter("requestname"));
				String requestid = Util.null2String(request.getParameter("requestid"));
				String userIds = Util.null2String(request.getParameter("userids"));
				RecordSet rs = new RecordSet();
				rs.executeSql("select workflowid from workflow_requestbase where requestid='"+requestid+"'");
				if(rs.next()){
					String workflowid = rs.getString("workflowid");
					String sendmessageworkflowid=Util.null2String(Prop.getPropValue("logindoor","sendmessageworkflowid")).trim();
					if((","+sendmessageworkflowid+",").indexOf((","+workflowid+","))==-1){
						return ;
					}
				}
				sendMessageService.sendMessage(requestname, requestid, userIds);
			} catch (Exception e) {
				baseBean.writeLog("发送流程消息异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("getDocList")){
			try {
				docDataService.getDocList(user);
			} catch (Exception e) {
				baseBean.writeLog("获取文档列表异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("getDocCount")){
			try {
				docDataService.getDocCount(user);
			} catch (Exception e) {
				baseBean.writeLog("获取文档列表异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("getDoc")){
			
			try {
				docDataService.getDoc(user);
			} catch (Exception e) {
				baseBean.writeLog("获取文档异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("addDocReadTag")){
			
			try {
				docDataService.addDocReadTag(user);
			} catch (Exception e) {
				baseBean.writeLog("文档写入读取标致异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("createMeeting")){
			try {
				meetingDataService.createMeeting(user);
			} catch (Exception e) {
				baseBean.writeLog("创建会议异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("submintMeeting")){
			try {
				meetingDataService.submintMeeting(user);
			} catch (Exception e) {
				baseBean.writeLog("提交会议异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("deleteMeeting")){
			try {
				meetingDataService.deleteMeeting(user);
			} catch (Exception e) {
				baseBean.writeLog("删除会议异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("editMeeting")){
			try {
				meetingDataService.editMeeting(user);
			} catch (Exception e) {
				baseBean.writeLog("编辑会议异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("cancelMeeting")){
			try {
				meetingDataService.cancelMeeting(user);
			} catch (Exception e) {
				baseBean.writeLog("取消会议异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("getUnUseMeeting")){
			try {
				meetingDataService.getUnUseMeeting(user);
			} catch (Exception e) {
				baseBean.writeLog("获取时间范围内未使用会议异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("getMeetingView")){
			try {
				meetingDataService.getMeetingView(user);
			} catch (Exception e) {
				baseBean.writeLog("获取会议视图异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("getMeetingTypeBrowser")){
			try {
				meetingDataService.getMeetingTypeBrowser(user);
			} catch (Exception e) {
				baseBean.writeLog("获取会议类型异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("getMeetingRoomBrowser")){
			try {
				meetingDataService.getMeetingRoomBrowser(user);
			} catch (Exception e) {
				baseBean.writeLog("获取会议室异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("getMeetingRemindBrowser")){
			try {
				meetingDataService.getMeetingRemindBrowser(user);
			} catch (Exception e) {
				baseBean.writeLog("获取提醒异常"+e.getMessage());
				e.printStackTrace();
			}
		}else if(action.equals("chkRoom")){
			try {
				meetingDataService.chkRoom(user);
			} catch (Exception e) {
				baseBean.writeLog("检查会议室时候冲突异常"+e.getMessage());
				e.printStackTrace();
			}
		}
		
	}
	
	/**  判断user对象是否存在于session中。
	 *  @param  HttpServletRequest request
	 *  @param  HttpServletResponse response
	 *  @return  User
	 */
	public User getUser(HttpServletRequest request, HttpServletResponse response){
		User user = (User) request.getSession(true).getAttribute("weaver_user@bean");
		if (user == null) {
			String loginfile = Util.getCookie(request, "loginfileweaver");
			try {
				if (Util.null2String(loginfile).equals("") || Util.null2String(loginfile).toLowerCase().equals("null")){
					return null;
				}else{
					return null;
				}
			} catch (Exception er) {
			}
		}
		
		int f_weaver_belongto_userid = Util.getIntValue(request.getParameter("f_weaver_belongto_userid"),0);
		int f_weaver_belongto_usertype = Util.getIntValue(request.getParameter("f_weaver_belongto_usertype"),0);
		RecordSet rs = new RecordSet();
		if(user!=null&&f_weaver_belongto_userid!=user.getUID()&&f_weaver_belongto_userid>0){
			//判断是否为主次账号关系
			boolean isBelongto = false;
			String sql = " SELECT belongto FROM HrmResource WHERE id= "+user.getUID();
			rs.executeSql(sql);
			while(rs.next()){
				int belongto = rs.getInt("belongto");
				if(belongto == f_weaver_belongto_userid){
					isBelongto=true;
					break;
				}
			}
			
			sql = " SELECT id FROM HrmResource WHERE belongto= "+user.getUID();
			rs.executeSql(sql);
			while(rs.next()){
				int id = rs.getInt("id");
				if(id == f_weaver_belongto_userid){
					isBelongto=true;
					break;
				}
			}
			
			if(isBelongto)user= User.getUser(f_weaver_belongto_userid, f_weaver_belongto_usertype);
		}

		return user;
	}
	
	
}