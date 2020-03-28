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

import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.OnLineMonitor;
import weaver.hrm.User;
import weaver.systeminfo.SysMaintenanceLog;
public class LoginLinkSSOAction extends HttpServlet {
	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	BaseBean baseBean = new BaseBean();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){    
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){    
		this.request = request;
		this.response = response;
		ServletContext application = request.getSession().getServletContext();
		RecordSet rs = new RecordSet();
		String randoms=Util.null2String(Prop.getPropValue("logindoor","random")).trim();
        int SSOTimeOut=Util.getIntValue(Prop.getPropValue("logindoor","SSOTimeOut"),60000);
        String link_service_ip=Util.null2String(Prop.getPropValue("logindoor","link_service_ip")).trim();
        String clientIP = this.getIpAddr(request);
        JSONObject json = new JSONObject();
        if(!"".equals(link_service_ip)&&!link_service_ip.equals(clientIP)){
        	baseBean.writeLog("白名单错误! OA白名单配置IP："+link_service_ip+" 单点登录客户端真实IP："+clientIP);
        	json.put("code", 1);
        	json.put("message", "白名单错误! OA白名单配置IP："+link_service_ip+" 单点登录客户端真实IP："+clientIP);
        	returnValue(json, response);
        	return ;
        }
		try{
	        String username=Util.null2String(request.getParameter("username")).trim();
	        rs.executeSql(" select * from HrmResource where loginid='"+username+"' ");
	        if(!rs.next()){
	        	baseBean.writeLog("OA中查无此人，必须重新登录 loginid:"+username);
//	        	response.sendRedirect(ssoLogoutPage) ;
	        	json.put("code", 4);
	        	json.put("message", "OA中查无此人，必须重新登录 username:"+username);
	        	returnValue(json, response);
	        	return ;
	        }
	        String timestamp=Util.null2String(request.getParameter("timestamp")).trim();
	        String secret=Util.null2String(request.getParameter("secret")).trim();
	       
	        //key+username+timestamp
	        String token = randoms+username+timestamp;
	        String secret_oa = this.md5Password(token);
	        if(!secret.equals(secret_oa)){
	        	baseBean.writeLog("secret匹配错误  key+username+timestamp");
//	        	response.sendRedirect(ssoLogoutPage) ;
	        	json.put("code", 2);
	        	json.put("message", "secret匹配错误 key+username+timestamp");
	        	returnValue(json, response);
	        	return ;
	        }
	        //秒
	        long currenttime=System.currentTimeMillis();
	        long gettime=0;
	        try{
	        	gettime = Long.parseLong(timestamp);
	        }catch(Exception e){
	        	gettime = 0;
	        }
	        baseBean.writeLog("OA系统当前时间是:"+currenttime+"毫秒 传入timestamp的是："+gettime+"毫秒 时间戳差值："+(currenttime-gettime)+"秒 正常差值范围："+SSOTimeOut+"秒");
	        if(currenttime-gettime>SSOTimeOut){
	        	baseBean.writeLog("timestamp传入错误！ OA系统的是:"+currenttime+"毫秒 传入timestamp的是："+gettime+"毫秒 时间戳差值："+(currenttime-gettime)+"毫秒 正常差值范围："+SSOTimeOut+"毫秒");
//	        	response.sendRedirect(ssoLogoutPage) ;
	        	json.put("code", 3);
	        	json.put("message", "timestamp传入错误！ OA系统的是:"+currenttime+"秒 传入timestamp的是："+gettime+"秒 时间戳差值："+(currenttime-gettime)+"秒 正常差值范围："+SSOTimeOut+"秒");
	        	returnValue(json, response);
	        	return ;
	        }
	        rs.executeSql(" select * from HrmResource where loginid='"+username+"' ");
	        User user_new = null;
	        if(rs.next()){ 
	        	baseBean.writeLog("存在用户id:"+rs.getInt("id")+" lastname:"+rs.getString("lastname"));
	        	user_new = new User();
				user_new.setUid(rs.getInt("id"));
				user_new.setLoginid(rs.getString("loginid"));
				user_new.setFirstname(rs.getString("firstname"));
				user_new.setLastname(rs.getString("lastname"));
				user_new.setAliasname(rs.getString("aliasname"));
				user_new.setTitle(rs.getString("title"));
				user_new.setTitlelocation(rs.getString("titlelocation"));
				user_new.setSex(rs.getString("sex"));
				user_new.setPwd(rs.getString("password"));
//				baseBean.writeLog("password:"+rs.getString("password"));
				String languageidweaver = rs.getString("systemlanguage");
				user_new.setLanguage(Util.getIntValue(languageidweaver, 0));
				user_new.setTelephone(rs.getString("telephone"));
				user_new.setMobile(rs.getString("mobile"));
				user_new.setMobilecall(rs.getString("mobilecall"));
				user_new.setEmail(rs.getString("email"));
				user_new.setCountryid(rs.getString("countryid"));
				user_new.setLocationid(rs.getString("locationid"));
				user_new.setResourcetype(rs.getString("resourcetype"));
				user_new.setStartdate(rs.getString("startdate"));
				user_new.setEnddate(rs.getString("enddate"));
				user_new.setContractdate(rs.getString("contractdate"));
				user_new.setJobtitle(rs.getString("jobtitle"));
				user_new.setJobgroup(rs.getString("jobgroup"));
				user_new.setJobactivity(rs.getString("jobactivity"));
				user_new.setJoblevel(rs.getString("joblevel"));
				user_new.setSeclevel(rs.getString("seclevel"));
				user_new.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
				user_new.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
				user_new.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
				user_new.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
				user_new.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
				user_new.setManagerid(rs.getString("managerid"));
				user_new.setAssistantid(rs.getString("assistantid"));
				user_new.setPurchaselimit(rs.getString("purchaselimit"));
				user_new.setCurrencyid(rs.getString("currencyid"));
				user_new.setLastlogindate(rs.getString("currentdate"));
				user_new.setLogintype("1");
				user_new.setAccount(rs.getString("account"));
	
				user_new.setLoginip(request.getRemoteAddr());
				request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
				request.getSession(true).setAttribute("weaver_user@bean", user_new);
	
				//多帐号登陆
				if (user_new.getUID() != 1) {  //is not sysadmin
					weaver.login.VerifyLogin VerifyLogin = new weaver.login.VerifyLogin();
					java.util.List accounts = VerifyLogin.getAccountsById(user_new.getUID());
					request.getSession(true).setAttribute("accounts", accounts);
				}
				
				request.getSession(true).setAttribute("moniter", new OnLineMonitor("" + user_new.getUID(),user_new.getLoginip()));
				Util.setCookie(response, "loginfileweaver", "/login/Login.jsp", 172800);
				Util.setCookie(response, "loginidweaver", ""+user_new.getUID(), 172800);
				Util.setCookie(response, "languageidweaver", languageidweaver, 172800);
				Map logmessages=(Map)application.getAttribute("logmessages");
	            if(logmessages==null){
	                logmessages=new HashMap();
	                logmessages.put(""+user_new.getUID(),"");
	                application.setAttribute("logmessages",logmessages);
	            }
	            request.getSession(true).setAttribute("logmessage",getLogMessage(user_new.getUID()+""));
	            
	            char separater = Util.getSeparator();
	            Calendar today = Calendar.getInstance();
	            String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
	            rs.execute("HrmResource_UpdateLoginDate", rs.getString("id") + separater + currentdate);
	
	            SysMaintenanceLog log = new SysMaintenanceLog();
	            log.resetParameter();
	            log.setRelatedId(rs.getInt("id"));
	            log.setRelatedName((rs.getString("firstname") + " " + rs.getString("lastname")).trim());
	            log.setOperateType("6");
	            log.setOperateDesc("");
	            log.setOperateItem("60");
	            log.setOperateUserid(rs.getInt("id"));
	            log.setClientAddress(request.getRemoteAddr());
	            try {
					log.setSysLogInfo();
				} catch (Exception e) {
					e.printStackTrace();
				}
	            
	            json.put("code", 0);
	        	json.put("message", "登录成功");
	        	returnValue(json, response);
				
					//用户的登录后的页面
//					weaver.systeminfo.template.UserTemplate ut=new weaver.systeminfo.template.UserTemplate();				
//					ut.getTemplateByUID(user_new.getUID(),user_new.getUserSubCompany1());
//					int templateId=ut.getTemplateId();
//					int extendTempletid=ut.getExtendtempletid();
//					String defaultHp = ut.getDefaultHp();
//					request.getSession(true).setAttribute("defaultHp",defaultHp);
//				
//					String tourl ="/wui/main.jsp";
//					if(gopage.length()>0){
//						tourl = gopage;
//					}
//					String mec_id="";
//					String jsScript = "<script>"
//							+ "$(document).ready(function(){"
//								+"top.location.href="+tourl
//							+ "});"
//						  + "</script>";
//					response.getWriter().print(jsScript);
//					System.out.println("tourl>"+tourl);
//					request.getRequestDispatcher(tourl).forward(request, response);
//					response.sendRedirect("/test.jsp?gopage="+tourl);
	        }else{//OA中查无此人，必须重新登录
	        	baseBean.writeLog("OA中查无此人，必须重新登录 loginid:"+username);
//	        	response.sendRedirect(ssoLogoutPage) ;
	        	json.put("code", 4);
	        	json.put("message", "OA中查无此人，必须重新登录 username:"+username);
	        	returnValue(json, response);
	        	return ;
	        }
	        }
	        catch(Exception e){//调用接口异常
        		json.put("code", 5);
	        	json.put("message", "OA单点登录未知异常:"+e.getMessage());
	        	returnValue(json, response);
	    		return ;
	    	}
	}
	
	public String getLogMessage(String uid){
		String message = "";
		RecordSet rs = new RecordSet();
		String sqltmp = "";
	    if (rs.getDBType().equals("oracle")) {
	        sqltmp = "select * from (select * from SysMaintenanceLog where relatedid = " + uid + " and operatetype='6' and operateitem='60' order by id desc ) where rownum=1 ";
	    }else if(rs.getDBType().equals("db2")){
	        sqltmp = "select * from SysMaintenanceLog where relatedid = "+uid +" and operatetype='6' and operateitem='60' order by id desc fetch first 1 rows only ";
	    } else {
	        sqltmp = "select top 1 * from SysMaintenanceLog where relatedid = " + uid + " and operatetype='6' and operateitem='60' order by id desc";
	    }

	    rs.executeSql(sqltmp);
	    if (rs.next()){
	        message = rs.getString("clientaddress") + " " + rs.getString("operatedate") + " " + rs.getString("operatetime");
	    }
	    
	    return message;
	}
	
	/**
     * 生成32位md5码
     * @param password
     * @return
     */
    public static String md5Password(String password) {

        try {
            // 得到一个信息摘要器
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(password.getBytes());
            StringBuffer buffer = new StringBuffer();
            // 把每一个byte 做一个与运算 0xff;
            for (byte b : result) {
                // 与运算
                int number = b & 0xff;// 加盐
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    buffer.append("0");
                }
                buffer.append(str);
            }

            // 标准的md5加密后的结果
            return buffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }

    }
    
    /**
     * 获取客户端真实ip
     * @param request
     * @return
     */
    public String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
             ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    public void returnValue(JSONObject json ,HttpServletResponse response){
    	response.setContentType("application/json; charset=utf-8");  
		try {
			response.getWriter().print(json.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args){
    	long currenttime=System.currentTimeMillis()/1000;//毫秒转为秒
    	String str="szecology"+"pengwang"+currenttime;
    	System.out.println(md5Password(str));
    	
    }
	
	
}