package weaver.sysinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;

/**
 * 宿主机
 * @author lsq
 * @date 2019/10/30
 * @version 1.0
 */
public class ServerApply_HostApply extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;
	private HttpServletResponse response;

	private boolean result_dns = true;
	private boolean result_dns2 = true;
	private boolean result_dns3 = true;
	BaseBean lg = new BaseBean();

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
			if ("getHostRoom".equals(action)) { 
				getHostRoom();
			} else if ("showUposition".equals(action)) {
				showUposition();
			} else if ("addInfo".equals(action)) {

			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询机房
	 */
	public void getHostRoom(){
		try {
			String url="http://10.9.7.130:8000/getMachineRooms/";	
			String Ulocal = this.sendPost(url,"");
			lg.writeLog("getHostRoom接受的json数据为:" + Ulocal);
			JSONObject jsonobj=JSONObject.fromObject(Ulocal);
			int resultcode=jsonobj.getInt("code");
			if(resultcode==200){  //说明查询机房成功,写入机房表
				
			}else{
				try {
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print("1"); //查询失败,请联系管理员!
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
            lg.writeLog("查询机房异常:"+e);
		}
	}
	
	/**
	 * 查询机柜
	 */
	public void getCabinet(){
		String url = "http://10.9.7.130:8000/getRoomAsstCabinet/";
		String bk_inst_id = ""; // 模型id
		
		
		
	}
	
	/**
	 * 查询U位
	 */
	public void getULocal(){
		String url = "http://10.9.7.130:8000/getFreeUinfo/";
		
		
		
	}
	/**
	 * 查询可用IP
	 */
	public void getAvailableIp(){
		String url = "http://10.9.7.130:8000/getAvailableIps/";
		
		
		
	}
	
	/**
	 * 写入或更新网络设备
	 */
    private void addInfoInternet() {
    	String type= Util.null2String(request.getParameter("type")); // 设备类型
    	String name= Util.null2String(request.getParameter("name")); // 设备名称
    	String sysname= Util.null2String(request.getParameter("sysname")); // 系统名称
    	String managerIP= Util.null2String(request.getParameter("managerIP")); // 管理IP
    	String bk_obj_id="";
    	if(type.equals("0")){
    		bk_obj_id="bk_switch";
    		String maintenance="2019-15-01";  //过保日期
    		
    	}else if(type.equals("1")){
    		bk_obj_id="bk_firewall";
    		String maintenance=""; //过保日期
    	}else if(type.equals("2")){
    		bk_obj_id="bk_router";
    		String maintenance_time=""; //过保日期
    	}
        
    	
    	JSONObject jsonobject = new JSONObject();
    	
		int[] suzu={32775};
		
		String bk_inst_name = "11.95.16.0"; // 模型id
		jsonobject.put("bk_inst_name", bk_inst_name); //设备名称
		jsonobject.put("bk_obj_id", "nas"); //集中式存储
		
		jsonobject.put("maintenance_time", "2019-11-01");  //过保日期
		
		jsonobject.put("bk_admin_ip", "10.95.16.8");  //管理IP
		
		jsonobject.put("bk_room_inst_id", 25324);  //机房id
		jsonobject.put("bk_cabinet_inst_id", 25409); //机柜id
		jsonobject.put("bk_u_inst_id", suzu);    //u位id
		jsonobject.put("bk_biz_inst_id", 14);    //业务id  云平台14 (系统名称)
		jsonobject.put("bk_ip_inst", "10.95.16.8");  //
		

		jsonobject.put("owner", "王鹏test7");
		jsonobject.put("bk_comment", "test7");
		jsonobject.put("subnet_mask", "255.222.222.0"); //子网掩码
    	
		String url = "http://10.9.7.130:8000/applyDevice/";
		String Ulocal = this.sendPost(url,jsonobject.toString());
		lg.writeLog("addInfo接受的json数据为:" + Ulocal);
	}
    /**
     * 写入或更新宿主机上架
     */
    public void addInfoServer(){

    	JSONObject jsonobject = new JSONObject();
    	
    	
    	int[] suzu={32775};
		String bk_inst_name = "11.95.16.0"; // 模型id
		jsonobject.put("bk_inst_name", bk_inst_name); //设备名称
		jsonobject.put("bk_obj_id", "server");   //设备类型

		
		jsonobject.put("bk_room_inst_id", 25324);  //机房id
		jsonobject.put("bk_cabinet_inst_id", 25409); //机柜id
		jsonobject.put("bk_u_inst_id", suzu);    //u位id
		jsonobject.put("bk_biz_inst_id", 14);    //业务id  云平台14 (系统名称)
		jsonobject.put("bk_ip_inst", "10.95.16.8");  //
		

		jsonobject.put("bk_admin_ip", "10.95.16.8");  //管理IP
		jsonobject.put("bk_host_name", "test"); //主机名称
		jsonobject.put("serial_number", "test12");  //序列号,设备类型为服务器 
		jsonobject.put("bk_vendor", "test");   //设备厂商
		jsonobject.put("maintenance_time", "2019-14-01");  //过保日期
		jsonobject.put("owner", "王鹏");
		jsonobject.put("bk_comment", "test备注");
		jsonobject.put("subnet_mask", "255.255.122.0");  
    	
		String url = "http://10.9.7.130:8000/applyDevice/";
		String Ulocal = this.sendPost(url,jsonobject.toString());
		lg.writeLog("addInfo接受的json数据为:" + Ulocal);
    }
    
    /**
     * 写入或更新存储资源
     */
    public void addInfoNas(){
    	
    	JSONObject jsonobject = new JSONObject();
		
    	int[] suzu={32775};
		String bk_inst_name = "11.95.16.0"; // 模型id
		jsonobject.put("bk_inst_name", bk_inst_name); //设备名称
		jsonobject.put("bk_obj_id", "nas"); //集中式存储
		
		jsonobject.put("maintenance_time", "2019-11-01");  //过保日期
		jsonobject.put("capcity", "11G");  //容量
		jsonobject.put("serial_number", "10011");  //序列号
		jsonobject.put("model", "存储101");  //型号
		
		jsonobject.put("bk_admin_ip", "10.95.16.8");  //管理IP
		
		jsonobject.put("bk_room_inst_id", 25324);  //机房id
		jsonobject.put("bk_cabinet_inst_id", 25409); //机柜id
		jsonobject.put("bk_u_inst_id", suzu);    //u位id
		jsonobject.put("bk_biz_inst_id", 14);    //业务id  云平台14 (系统名称)
		jsonobject.put("bk_ip_inst", "10.95.16.8");  //
		

		jsonobject.put("owner", "王鹏test7");
		jsonobject.put("bk_comment", "test7");
		jsonobject.put("subnet_mask", "255.222.222.0"); //子网掩码
    	
    	
		String url = "http://10.9.7.130:8000/applyDevice/";
		String Ulocal = this.sendPost(url,jsonobject.toString());
		lg.writeLog("addInfo接受的json数据为:" + Ulocal);
    }
    
    
    public void getHostRoom2(){
    	String Internet = Util.null2String(request.getParameter("host")); // 网段数据id
    	
		String[] analysis = Internet.split(",");
		lg.writeLog("analysis.length-----" + analysis.length);
		for (int i = 0; i < analysis.length; i++) {
			JSONObject jsonpage = new JSONObject();
			/*JSONObject jsonroom = new JSONObject();
			JSONArray jsonArrayroom = new JSONArray();
			JSONObject jsonobj = new JSONObject();
			JSONObject jsonobject = new JSONObject();*/
			//String bk_supplier_account = "0";
			int[] suzu={32775};
		
			String bk_inst_name = "11.95.16.0"; // 模型id
			jsonpage.put("bk_inst_name", bk_inst_name); //设备名称
			jsonpage.put("bk_obj_id", "nas"); //集中式存储
			
			jsonpage.put("maintenance_time", "2019-11-01");  //过保日期
			jsonpage.put("capcity", "11G");  //容量
			jsonpage.put("serial_number", "10011");  //序列号
			jsonpage.put("model", "存储101");  //型号
			
			jsonpage.put("bk_admin_ip", "10.95.16.8");  //管理IP
			
			jsonpage.put("bk_room_inst_id", 25324);  //机房id
			jsonpage.put("bk_cabinet_inst_id", 25409); //机柜id
			jsonpage.put("bk_u_inst_id", suzu);    //u位id
			jsonpage.put("bk_biz_inst_id", 14);    //业务id  云平台14 (系统名称)
			jsonpage.put("bk_ip_inst", "10.95.16.8");  //
			

			jsonpage.put("owner", "王鹏test7");
			jsonpage.put("bk_comment", "test7");
			jsonpage.put("subnet_mask", "255.222.222.0"); //子网掩码
			
			
			
			//jsonpage.put("bk_obj_id", "bk_firewall"); //防火墙
			//jsonpage.put("maintenance", "2019-11-01");  //过保日期
			
			/*
			jsonpage.put("bk_obj_id", "bk_router"); //路由器
			jsonpage.put("maintenance_time", "2019-10-01");  //过保日期
			 */
			
			//jsonpage.put("bk_obj_id", "bk_switch"); 交换机
			//jsonpage.put("maintenance", "2019-15-01");  //过保日期
			
			/*jsonpage.put("bk_obj_id", "server");   //设备类型
			jsonpage.put("bk_host_name", "test"); //设备名称
			jsonpage.put("serial_number", "test12");  //序列号,设备类型为服务器 
			jsonpage.put("bk_vendor", "test");   //设备厂商
			jsonpage.put("maintenance_time", "2019-14-01");  //过保日期
			jsonpage.put("owner", "王鹏");
			jsonpage.put("bk_comment", "test备注");
			jsonpage.put("subnet_mask", "255.255.122.0");  //掩码*/
			
			
			
			
			/*int start = 0; // 分页开始位置
			int limit = 10; // 每行记录数
			String operator = "$regex"; // 操作类型
			//jsonpage.put("start", start);
			//jsonpage.put("limit", limit);*/
			//String url = "http://10.9.7.130:8000/getRoomAsstCabinet/";	
			//String url = "http://10.9.7.130:8000/getFreeUinfo/";
			//String url = "http://10.9.7.130:8000/getAvailableIps/";
			String url = "http://10.9.7.130:8000/applyDevice/";
			String Ulocal = this.sendPost(url,jsonpage.toString());
			lg.writeLog("getHostRoom接受的json数据为:" + Ulocal);
			
			/*jsonroom.put("field", "bk_inst_name");
			jsonroom.put("operator", operator);
			jsonroom.put("value", value);
			jsonArrayroom.add(jsonroom);
			jsonobj.put(bk_obj_id, jsonArrayroom.toString());

			jsonobject.put("bk_obj_id", bk_obj_id);
			jsonobject.put("bk_supplier_account", bk_supplier_account);

			jsonobject.put("page", jsonpage);
			// jsonobject.put("fields", field);
			jsonobject.put("condition", jsonobj);

			JSONObject json = JSONObject.fromObject(Ulocal);
			boolean resultjson = json.getBoolean("result");
			String bk_error_msg = json.getString("bk_error_msg");
			lg.writeLog("resultjsonsearch-------" + resultjson);*/
			//lg.writeLog("bk_error_msg-------" + bk_error_msg);
			/*if (resultjson) {
				String data = json.getString("data");
				JSONObject datajson = JSONObject.fromObject(data);
				int count = datajson.getInt("count");
				if (count != 0) {
					String info = datajson.getString("info");
					JSONArray infojson = JSONArray.fromObject(info);
					lg.writeLog("infojson-----" + infojson);
					JSONObject ob = (JSONObject) infojson.get(0);
					lg.writeLog("ob-----" + ob);
					int bk_inst_id = ob.getInt("bk_inst_id");
					lg.writeLog("bk_inst_id-----" + bk_inst_id);
					instid.add(bk_inst_id);
				} else {
					result_dns = false;
					lg.writeLog("result_dns-----:" + result_dns);
					break;
				}
			} else {
				result_dns = false;
				lg.writeLog("result_dns-----:" + result_dns);
				break;

			}*/
		}
		
    }
	/**
     * 刷新U位
     */
	private void showUposition() {
		
	}

	
	/**
	 * 发送post请求
	 * 
	 * @param httpUrl
	 * @param param
	 * @return
	 */
	public String sendPost(String url,String param) {
		OutputStreamWriter out = null;
		BufferedReader in = null;
		String result = "";

		try {

			URL realUrl = new URL(url);
			HttpURLConnection conn = null;
			conn = (HttpURLConnection) realUrl.openConnection();// 打开和URL之间的连接
			// 发送POST请求必须设置如下两行
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 设置通用的请求属性
			conn.setRequestProperty("BK_USER", "admin");
			conn.setRequestProperty("HTTP_BLUEKING_SUPPLIER_ID", "0");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.connect();
			out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");// 获取URLConnection对象对应的输出流
			out.write(param);// 发送请求参数
			out.flush();// flush输出流的缓冲
			in = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "UTF-8"));// 定义BufferedReader输入流来读取URL的响应
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
	
	
}
