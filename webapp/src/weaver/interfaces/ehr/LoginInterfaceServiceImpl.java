package weaver.interfaces.ehr;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.jws.WebService;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weaver.conn.RecordSet;
import weaver.hrm.User;

import com.alibaba.fastjson.JSONObject;
import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.StringHelper;

public class LoginInterfaceServiceImpl extends HttpServlet {
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	  public void doGet(HttpServletRequest request, HttpServletResponse response){
		  this.request = request;
		    this.response = response;
		    try {
		    	String action = StringHelper.null2String(request.getParameter("action"));
		        User user = MobileUserInit.getUser(request, response);
		        System.out.println("++++++++++++++++++++++++++++++++++++");
		        if (user == null) {
		            JSONObject checkUser = new JSONObject();
		            checkUser.put("msgstatus", "0");
		            checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
		            response.setContentType("application/json; charset=utf-8");
		            response.getWriter().print(checkUser.toString());
		            return;
		          }
		        if("loginInterface".equals(action)){
		        	loginInterface(user);
		        }
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
	  }
	  
	//单点登录接口
		public void loginInterface(User user){
			RecordSet rs = new RecordSet();
			JSONObject jsonLogin = new JSONObject();
		    String userid = String.valueOf(user.getUID());
		    String sqlworkCode = "select workcode from hrmresource where id ='"+userid+"'";
		    System.out.println(sqlworkCode);
		    rs.executeSql(sqlworkCode);
		    String workCode =null;//工号
		    if(rs.next()){
		    	workCode = StringHelper.null2String(rs.getString("workcode"));
		    }
		    String timestamp = StringHelper.null2String(System.currentTimeMillis());//当前时间戳
		    String secret = workCode + "szs_eHR" + timestamp;//加密串
		    secret = md5Password(secret);
		    jsonLogin.put("workCode", workCode);
		    jsonLogin.put("secret", secret);
		    jsonLogin.put("timestamp", timestamp);
		    try {
		    	this.response.setContentType("application/json; charset=utf-8");
		    	this.response.getWriter().print(StringHelper.null2String(jsonLogin));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		  public static String md5Password(String password)
		  {
		    try
		    {
		      MessageDigest digest = MessageDigest.getInstance("md5");
		      byte[] result = digest.digest(password.getBytes());
		      StringBuffer buffer = new StringBuffer();

		      for (byte b : result)
		      {
		        int number = b & 0xFF;
		        String str = Integer.toHexString(number);
		        if (str.length() == 1) {
		          buffer.append("0");
		        }
		        buffer.append(str);
		      }

		      return buffer.toString();
		    } catch (Exception e) {
		      e.printStackTrace();
		    }return "";
		  }
		
}
