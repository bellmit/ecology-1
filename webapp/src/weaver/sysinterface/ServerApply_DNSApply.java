package weaver.sysinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
 * 域名解析,IP申请
 * @author lsq
 * @date 2019/10/20
 * @version 1.0
 */
public class ServerApply_DNSApply extends HttpServlet {
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
			if ("create_DNS".equals(action)) { // 创建dns
				create_DNS();
			} else if ("search_Internet".equals(action)) {
				search_Internet();
			} else if ("update_ip".equals(action)) {
				update_ip(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询实列IP
	 * 
	 * @param user
	 */
	public List<Integer> search_IP(String analysisAddress) {
		result_dns = true; // 初始化查询结果
		List<Integer> instid = new ArrayList<Integer>(); // 用于存放查询返回的 实列id
															// 为创建关联做准备
		String[] analysis = analysisAddress.split(",");
		lg.writeLog("analysis.length-----" + analysis.length);
		for (int i = 0; i < analysis.length; i++) {
			String value = analysis[i]; // 流程中的解析地址 需要与对方库中的 IP做比较
			JSONObject jsonpage = new JSONObject();
			JSONObject jsonroom = new JSONObject();
			JSONArray jsonArrayroom = new JSONArray();
			JSONObject jsonobj = new JSONObject();
			JSONObject jsonobject = new JSONObject();
			String bk_supplier_account = "0";
			String bk_obj_id = "ip"; // 模型id
			int start = 0; // 分页开始位置
			int limit = 10; // 每行记录数
			String operator = "$regex"; // 操作类型
			jsonpage.put("start", start);
			jsonpage.put("limit", limit);
			String PREFIX_URL = "http://10.10.20.102:33031/api/v3";
			String url = PREFIX_URL + "/inst/association/search/owner/"
					+ bk_supplier_account + "/object/" + bk_obj_id + "";
			jsonroom.put("field", "bk_inst_name");
			jsonroom.put("operator", operator);
			jsonroom.put("value", value);
			jsonArrayroom.add(jsonroom);
			jsonobj.put(bk_obj_id, jsonArrayroom.toString());

			jsonobject.put("bk_obj_id", bk_obj_id);
			jsonobject.put("bk_supplier_account", bk_supplier_account);

			jsonobject.put("page", jsonpage);
			// jsonobject.put("fields", field);
			jsonobject.put("condition", jsonobj);

			lg.writeLog("search_IP发送的json数据为:" + jsonobject.toString());
			String Ulocal = this.sendPost(url, jsonobject.toString(), "post");
			lg.writeLog("search_IP接受的json数据为:" + Ulocal);

			JSONObject json = JSONObject.fromObject(Ulocal);
			boolean resultjson = json.getBoolean("result");
			String bk_error_msg = json.getString("bk_error_msg");
			lg.writeLog("resultjsonsearch-------" + resultjson);
			lg.writeLog("bk_error_msg-------" + bk_error_msg);
			if (resultjson) {
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

			}
		}
		return instid;
	}

	/**
	 * 效验是否存在重复域名
	 * 
	 * @param DNSAddress
	 * @return boolean
	 */
	public boolean search_DNS(String DNSAddress) {
		boolean result = true;
		String[] dns = DNSAddress.split(",");
		for (int i = 0; i < dns.length; i++) {
			JSONObject jsonpage = new JSONObject();
			JSONObject jsonroom = new JSONObject();
			JSONArray jsonArrayroom = new JSONArray();
			JSONObject jsonobj = new JSONObject();
			JSONObject jsonobject = new JSONObject();
			String bk_supplier_account = "0";
			String bk_obj_id = "dns"; // 模型id
			int start = 0; // 分页开始位置
			int limit = 10; // 每行记录数
			String operator = "$regex"; // 操作类型
			jsonpage.put("start", start);
			jsonpage.put("limit", limit);
			String PREFIX_URL = "http://10.10.20.102:33031/api/v3";
			String url = PREFIX_URL + "/inst/association/search/owner/"
					+ bk_supplier_account + "/object/" + bk_obj_id + "";
			jsonroom.put("field", "bk_inst_name");
			jsonroom.put("operator", operator);
			jsonroom.put("value", dns[i]);
			jsonArrayroom.add(jsonroom);
			jsonobj.put(bk_obj_id, jsonArrayroom.toString());

			jsonobject.put("bk_obj_id", bk_obj_id);
			jsonobject.put("bk_supplier_account", bk_supplier_account);

			jsonobject.put("page", jsonpage);
			jsonobject.put("condition", jsonobj);

			lg.writeLog("search_DNS发送的json数据为:" + jsonobject.toString());
			String Ulocal = this.sendPost(url, jsonobject.toString(), "post");
			lg.writeLog("search_DNS接受的json数据为:" + Ulocal);
			JSONObject json = JSONObject.fromObject(Ulocal);
			boolean resultjson = json.getBoolean("result");
			if (resultjson) {
				String data = json.getString("data");
				JSONObject datajson = JSONObject.fromObject(data);
				int count = datajson.getInt("count");
				if (count > 0) { // 说明 cmdb中存在 该域名;而我需要的是不存在
					result = false;
				}
			}
		}
		return result;
	}

	/**
	 * 创建DNS实列并关联IP
	 */
	public void create_DNS() {
		String analysisAddress = Util.null2String(request.getParameter("analysisAddress")); // 解析地址
		String DNSAddress = Util.null2String(request.getParameter("DNSAddress")); // 域名地址
		List<Integer> result = search_IP(analysisAddress); // 查询解析地址 则给DNS创建实例
		boolean searchdnsresult = search_DNS(DNSAddress); // 效验域名
		lg.writeLog("result_dns------" + result_dns);
		lg.writeLog("searchdnsresult------" + searchdnsresult);
		if (!result_dns) {
			try {
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print("0"); // 解析地址错误,请重新填写
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (searchdnsresult) {
				String systemName = Util.null2String(request.getParameter("systemName")); // 系统名称
				String[] system = systemName.split(",");
				String[] DNS = DNSAddress.split(",");
				boolean dnsresult = true;
				boolean sysresult = true;
				for (int i = 0; i < DNS.length; i++) {
					String bk_supplier_account = "0";
					String bk_obj_id = "dns"; // 模型id
					String bk_inst_name = DNS[i]; // 实列名DNS
					String system_name = system[i]; // 实列名 系统名称
					lg.writeLog("实列名DNS----" + bk_inst_name);
					lg.writeLog("实列名 系统名称-----" + system_name);
					String PREFIX_URL = "http://10.10.20.102:33031/api/v3";
					String url = PREFIX_URL + "/inst/" + bk_supplier_account+ "/" + bk_obj_id + "";
					JSONObject jsonobject = new JSONObject();
					jsonobject.put("bk_inst_name", bk_inst_name);
					jsonobject.put("system_name", system_name);
					jsonobject.put("bk_supplier_account", bk_supplier_account);
					jsonobject.put("owner", bk_supplier_account);
					jsonobject.put("bk_comment", bk_supplier_account);
					lg.writeLog("create_DNS发送的json数据为:" + jsonobject.toString());
					String Ulocal = this.sendPost(url, jsonobject.toString(),"post");
					lg.writeLog("create_DNS接受的json数据为:" + Ulocal);
					JSONObject json = JSONObject.fromObject(Ulocal);
					boolean resultjson = json.getBoolean("result");
					int bk_error_code = json.getInt("bk_error_code");
					lg.writeLog("resultjson-------" + resultjson);
					lg.writeLog("bk_error_code-------" + bk_error_code);
					if (resultjson) {
						String data = json.getString("data");
						JSONObject datajson = JSONObject.fromObject(data);
						int bk_inst_id = datajson.getInt("bk_inst_id"); // 源实列id后面创建关联需要用到
						int bk_asst_inst_id = result.get(i); // 目标实列id
						String bk_obj_asst_id = "dns_default_ip"; // 关联模型id
						lg.writeLog("bk_asst_inst_id------" + bk_asst_inst_id);
						String TEMP_URL = "http://10.10.20.102:33031/api/v3";
						String Turl = TEMP_URL
								+ "/inst/association/action/create";

						JSONObject jsoncreate_DNS = new JSONObject();
						jsoncreate_DNS.put("bk_obj_asst_id", bk_obj_asst_id);
						jsoncreate_DNS.put("bk_inst_id", bk_inst_id);
						jsoncreate_DNS.put("bk_asst_inst_id", bk_asst_inst_id);

						lg.writeLog("create_DNS_relation发送的json数据为:"
								+ jsoncreate_DNS.toString());
						String DNS_Relation = this.sendPost(Turl,
								jsoncreate_DNS.toString(), "post");
						lg.writeLog("create_DNS_relation接受的json数据为:"
								+ DNS_Relation);

						JSONObject jsoncreate_DNSResult = new JSONObject();
						boolean resultdns = jsoncreate_DNSResult
								.getBoolean("result");
						if (resultdns) {
							dnsresult = false;
						}
					} else {
						sysresult = false;
					}
				}
				if (dnsresult && sysresult) {
					try {
						response.setContentType("application/json; charset=utf-8");
						response.getWriter().print("1"); // 域名写入成功
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print("2"); // 域名效验重复,请重新填写!
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

	}

	/**
	 * 通过IP的两个状态和属性 查询符合条件的IP
	 */
	public void search_Internet() {
		String Internet = Util.null2String(request.getParameter("Internet")); // 网段数据id
		String ipsz = Util.null2String(request.getParameter("arr")); // 网段数据id
		lg.writeLog("ipsz:" + ipsz);
		lg.writeLog("Internet:" + Internet);
		String resultip = "";
		int result = 0;
		RecordSet rs = new RecordSet();
		String Interneting = ""; // 网段
		String sql = "select IPaddress from uf_accuracyIP where id='"
				+ Internet + "'";
		lg.writeLog("网段sql:" + sql);
		rs.executeSql(sql);
		if (rs.next()) {
			Interneting = Util.null2String(rs.getString("IPaddress"));
		}
		String[] intnet = Interneting.split("\\.");
		String intnets = "";
		for (int c = 0; c < intnet.length - 1; c++) {
			if (c == 0) {
				intnets = intnet[c];
			} else {
				intnets += "." + intnet[c];
			}
		}
		lg.writeLog("网段:" + intnets);
		JSONObject jsonpage = new JSONObject();
		JSONObject jsonuse_state = new JSONObject();
		JSONObject jsoninter = new JSONObject();
		JSONArray jsonArrayroom = new JSONArray();
		JSONObject jsonobj = new JSONObject();
		JSONObject jsonobject = new JSONObject();
		String bk_supplier_account = "0";
		String bk_obj_id = "ip"; // 模型id
		int start = 0; // 分页开始位置
		int limit = 200; // 每行记录数
		String operator = "$regex"; // 操作类型
		jsonpage.put("start", start);
		jsonpage.put("limit", limit);
		String PREFIX_URL = "http://10.10.20.102:33031/api/v3";
		String url = PREFIX_URL + "/inst/association/search/owner/"
				+ bk_supplier_account + "/object/" + bk_obj_id + "";

		jsoninter.put("field", "bk_inst_name");
		jsoninter.put("operator", operator);
		jsoninter.put("value", intnets);
		jsonArrayroom.add(jsoninter);

		jsonuse_state.put("field", "use_state");
		jsonuse_state.put("operator", operator);
		jsonuse_state.put("value", "3");
		jsonArrayroom.add(jsonuse_state);

		jsonobj.put(bk_obj_id, jsonArrayroom.toString());

		jsonobject.put("bk_obj_id", bk_obj_id);
		jsonobject.put("bk_supplier_account", bk_supplier_account);

		jsonobject.put("page", jsonpage);
		jsonobject.put("condition", jsonobj);

		lg.writeLog("search_IP发送的json数据为:" + jsonobject.toString());
		String Ulocal = this.sendPost(url, jsonobject.toString(), "post");
		lg.writeLog("search_IP接受的json数据为:" + Ulocal);

		JSONObject json = JSONObject.fromObject(Ulocal);
		boolean resultjson = json.getBoolean("result");
		String bk_error_msg = json.getString("bk_error_msg");
		lg.writeLog("resultjsonsearch-------" + resultjson);
		lg.writeLog("bk_error_msg-------" + bk_error_msg);
		if (resultjson) {
			String data = json.getString("data");
			JSONObject datajson = JSONObject.fromObject(data);
			int count = datajson.getInt("count");
			String[] strip=ipsz.split(",");
			if (count != 0) {
				String info = datajson.getString("info");
				JSONArray infojson = JSONArray.fromObject(info);
				lg.writeLog("infojson-----" + infojson);
				for (int j = 0; j < count; j++) {
					boolean ipcheck=true;
					JSONObject ob = (JSONObject) infojson.get(j);
					lg.writeLog("ob-----" + ob);
					String cmdb_ip_state = ob.getString("ip_state");
					lg.writeLog("cmdb_ip_state-----" + cmdb_ip_state);
					if (cmdb_ip_state.equals("可用")) {
						int bk_inst_id = ob.getInt("bk_inst_id");
						String bk_inst_name = ob.getString("bk_inst_name");
						lg.writeLog("bk_inst_id-----" + bk_inst_id);
						lg.writeLog("bk_inst_name-----" + bk_inst_name);
						for(int k=0;k<strip.length;k++){  //查询出的IP要与oa流程中已分配的IP进行比较,若相等,则该IP不能使用
							lg.writeLog("strip["+k+"]:"+strip[k]);
							if(bk_inst_name.equals(strip[k])){
								ipcheck=false;
							}
						}
						if(!ipcheck){
							if(j==count-1){  //cmdb查询出来的ip,当最后一个分配IP也在oa流程上显示了
								result=3;
							}
							continue;
						}else{
							resultip = bk_inst_name;
							break;							
						}
					}
				}
			} else {
				result = 1;
			}
		} else {
			result = 2;
		}

		if (result == 0) {
			JSONObject resultjsons = new JSONObject();
			resultjsons.put("key", "2");
			resultjsons.put("value", resultip);
			lg.writeLog("resultip:" + resultip);
			try {
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(resultjsons); //
			} catch (Exception e) {
				lg.writeLog("通过IP的两个状态和属性  查询符合条件的IP异常:" + e);
				e.printStackTrace();
			}
		} else if (result == 1) {
			try {
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print("1"); // 未查询到符合条件的IP
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (result == 2) {
			try {
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print("0"); // ip查询异常!
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(result == 3){
			try {
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print("3"); // 网段可用IP已用完,请选其他网段!
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}

	}

	/**
	 * 更新cmdb前需要验证IP
	 * 
	 * @param user
	 */
	public List<Integer> searchapply_IP(String analysisAddress) {
		result_dns2 = true; // 初始化查询结果
		List<Integer> instid = new ArrayList<Integer>(); // 用于存放查询返回的 实列id
															// 为创建关联做准备
		String[] analysis = analysisAddress.split(",");
		lg.writeLog("analysis.length-----" + analysis.length);
		String bk_supplier_account = "0";
		String bk_obj_id = "ip"; // 模型id
		int start = 0; // 分页开始位置
		int limit = 10; // 每行记录数
		String operator = "$regex"; // 操作类型
		JSONObject jsonpage = new JSONObject();
		jsonpage.put("start", start);
		jsonpage.put("limit", limit);
		for (int i = 0; i < analysis.length; i++) {

			String value = analysis[i]; // 流程中的解析地址 需要与对方库中的 IP做比较
			JSONObject jsonroom = new JSONObject();
			JSONArray jsonArrayroom = new JSONArray();
			JSONObject jsonobj = new JSONObject();
			JSONObject jsonobject = new JSONObject();
			String PREFIX_URL = "http://10.10.20.102:33031/api/v3";
			String url = PREFIX_URL + "/inst/association/search/owner/"
					+ bk_supplier_account + "/object/" + bk_obj_id + "";
			jsonroom.put("field", "bk_inst_name");
			jsonroom.put("operator", operator);
			jsonroom.put("value", value);
			jsonArrayroom.add(jsonroom);
			jsonobj.put(bk_obj_id, jsonArrayroom.toString());

			jsonobject.put("bk_obj_id", bk_obj_id);
			jsonobject.put("bk_supplier_account", bk_supplier_account);

			jsonobject.put("page", jsonpage);
			// jsonobject.put("fields", field);
			jsonobject.put("condition", jsonobj);

			lg.writeLog("searchapply_IP发送的json数据为:" + jsonobject.toString());
			String Ulocal = this.sendPost(url, jsonobject.toString(), "post");
			lg.writeLog("searchapply_IP接受的json数据为:" + Ulocal);

			JSONObject json = JSONObject.fromObject(Ulocal);
			boolean resultjson = json.getBoolean("result");
			String bk_error_msg = json.getString("bk_error_msg");
			lg.writeLog("resultjsonsearch-------" + resultjson);
			lg.writeLog("bk_error_msg-------" + bk_error_msg);
			if (resultjson) {
				String data = json.getString("data");
				JSONObject datajson = JSONObject.fromObject(data);
				int count = datajson.getInt("count");
				if (count != 0) {
					String info = datajson.getString("info");
					JSONArray infojson = JSONArray.fromObject(info);
					lg.writeLog("infojson-----" + infojson);
					JSONObject ob = (JSONObject) infojson.get(0);
					lg.writeLog("ob-----" + ob);
					String ip_property = ob.getString("ip_property"); // ip属性
					lg.writeLog("ip_property-----" + ip_property);
					String ip_state = ob.getString("ip_state"); // ip状态
					lg.writeLog("ip_state-----" + ip_state);
					String use_state = ob.getString("use_state"); // ip使用状态
					lg.writeLog("use_state-----" + use_state);
					if ((!ip_state.equals("已使用")) && use_state.equals("3")) {
						int bk_inst_id = ob.getInt("bk_inst_id");
						lg.writeLog("bk_inst_id-----" + bk_inst_id);
						instid.add(bk_inst_id);
					} else {
						result_dns2 = false;
						lg.writeLog("result_dns2-----:" + result_dns2);
						break;
					}
				} else {
					result_dns2 = false;
					lg.writeLog("result_dns2-----:" + result_dns2);
					break;
				}
			} else {
				result_dns2 = false;
				lg.writeLog("result_dns2-----:" + result_dns2);
				break;

			}
		}
		return instid;
	}

	/**
	 * 查询实列IP
	 * 
	 * @param user
	 */
	public int search_IPde(String analysisAddress) {
		result_dns3 = true; // 初始化查询结果
		int bk_inst_id = 0;
		String value = analysisAddress; // 流程中的解析地址 需要与对方库中的 IP做比较
		JSONObject jsonpage = new JSONObject();
		JSONObject jsonroom = new JSONObject();
		JSONArray jsonArrayroom = new JSONArray();
		JSONObject jsonobj = new JSONObject();
		JSONObject jsonobject = new JSONObject();
		String bk_supplier_account = "0";
		String bk_obj_id = "ip"; // 模型id
		int start = 0; // 分页开始位置
		int limit = 10; // 每行记录数
		String operator = "$regex"; // 操作类型
		jsonpage.put("start", start);
		jsonpage.put("limit", limit);
		String PREFIX_URL = "http://10.10.20.102:33031/api/v3";
		String url = PREFIX_URL + "/inst/association/search/owner/"
				+ bk_supplier_account + "/object/" + bk_obj_id + "";
		jsonroom.put("field", "bk_inst_name");
		jsonroom.put("operator", operator);
		jsonroom.put("value", value);
		jsonArrayroom.add(jsonroom);
		jsonobj.put(bk_obj_id, jsonArrayroom.toString());

		jsonobject.put("bk_obj_id", bk_obj_id);
		jsonobject.put("bk_supplier_account", bk_supplier_account);

		jsonobject.put("page", jsonpage);
		// jsonobject.put("fields", field);
		jsonobject.put("condition", jsonobj);

		lg.writeLog("search_IPde发送的json数据为:" + jsonobject.toString());
		String Ulocal = this.sendPost(url, jsonobject.toString(), "post");
		lg.writeLog("search_IPde接受的json数据为:" + Ulocal);

		JSONObject json = JSONObject.fromObject(Ulocal);
		boolean resultjson = json.getBoolean("result");
		String bk_error_msg = json.getString("bk_error_msg");
		lg.writeLog("resultjsonsearch-------" + resultjson);
		lg.writeLog("bk_error_msg-------" + bk_error_msg);
		if (resultjson) {
			String data = json.getString("data");
			JSONObject datajson = JSONObject.fromObject(data);
			int count = datajson.getInt("count");
			if (count != 0) {
				String info = datajson.getString("info");
				JSONArray infojson = JSONArray.fromObject(info);
				lg.writeLog("infojson-----" + infojson);
				for (int j = 0; j < count; j++) {
					JSONObject ob = (JSONObject) infojson.get(j);
					lg.writeLog("ob-----" + ob);
					String bk_inst_ipname = ob.getString("bk_inst_name");
					lg.writeLog("bk_inst_ipname-----" + bk_inst_ipname);
					if (bk_inst_ipname.equals(value)) {
						bk_inst_id = ob.getInt("bk_inst_id");
						lg.writeLog("bk_inst_id-----" + bk_inst_id);
						break;
					}
				}
			} else {
				result_dns3 = false;
				lg.writeLog("result_dns3-----:" + result_dns3);
			}
		} else {
			result_dns3 = false;
			lg.writeLog("result_dns3-----:" + result_dns3);
		}
		return bk_inst_id;
	}

	/**
	 * 更新cmdb中的IP信息并关联内网IP
	 */
	public void update_ip(User user) {
		try {
			String Internet = Util.null2String(request.getParameter("Internet")); // 网段数据id
			String getpageip = Util.null2String(request.getParameter("IP")); // 分配IP实列
			lg.writeLog("Internet----" + Internet);
			lg.writeLog("getpageip----" + getpageip);
			String[] Internet_sz = Internet.split(",");
			String[] ipstr = getpageip.split(",");
			boolean flag = true; // 标记,若为false则说明填写的分配IP网段不正确,则一律不执行
			for (int j = 0; j < Internet_sz.length; j++) {
				String ip = ipstr[j];
				lg.writeLog("ip-----" + ip);
				String[] ipintnet = ip.split("\\.");
				lg.writeLog("ipintnet[]------" + ipintnet);
				String inte = ""; // 分配IP网段
				for (int c = 0; c < ipintnet.length - 1; c++) {
					if (c == 0) {
						inte = ipintnet[c];
					} else {
						inte += "." + ipintnet[c];
					}
					lg.writeLog("ipintnet[" + c + "]" + ipintnet[c]);
				}
				lg.writeLog("inte------" + inte);
				RecordSet rs = new RecordSet();
				String Interneting = ""; // 网段
				String sql = "select IPaddress from uf_accuracyIP where id='"
						+ Internet_sz[j] + "'";
				lg.writeLog("网段sql:" + sql);
				String intnetoa = "";
				rs.executeSql(sql);
				if (rs.next()) {
					Interneting = Util.null2String(rs.getString("IPaddress"));
				}
				lg.writeLog("Interneting---" + Interneting);
				String[] intoa = Interneting.split("\\.");
				for (int c = 0; c < intoa.length - 1; c++) {
					if (c == 0) {
						intnetoa = intoa[c];
					} else {
						intnetoa += "." + intoa[c];
					}
				}
				lg.writeLog("intnetoa:" + intnetoa);
				if (!inte.equals(intnetoa)) {
					flag = false;
					break;
				}

			}
			if (flag) {
				List<Integer> ip_id = searchapply_IP(getpageip); // 实列IP id
				lg.writeLog("全局变量result_dns2----" + result_dns2);
				lg.writeLog("ip_id.size()----" + ip_id.size());
				if (result_dns2) {
					RecordSet rs = new RecordSet();
					String userid = String.valueOf(user.getUID());// 当前用户Id
					String sql = "select lastname from hrmresource where id='"
							+ userid + "'";
					rs.executeSql(sql);
					lg.writeLog("sql------" + sql);
					String owner = "";
					if (rs.next()) {
						owner = Util.null2String(rs.getString("lastname"));
					}
					String PREFIX_URL = "http://10.10.20.102:33031/api/v3";
					String bk_supplier_account = "0";
					String bk_obj_id = "ip"; // 模型id
					String use_status = "4"; // 使用状态
					//String ip_status = "已使用"; // IP状态
					boolean dnsresult = true;
					String ipproperty = Util.null2String(request
							.getParameter("ipproperty")); // IP属性
					String remark = Util.null2String(request
							.getParameter("remark")); // 备注
					lg.writeLog("remark----" + remark);
					String[] remarks = remark.split(",");
					for (int i = 0; i < ip_id.size(); i++) {
						lg.writeLog("循环次数---" + i + 1);
						// 更新操作
						String[] IPproperty_sz = ipproperty.split(",");
						Integer ip_property = Integer
								.parseInt(IPproperty_sz[i]); // IP属性
						JSONObject jsonobject = new JSONObject();
						int bk_inst_id = ip_id.get(i);
						String url = PREFIX_URL + "/inst/"
								+ bk_supplier_account + "/" + bk_obj_id + "/"
								+ bk_inst_id + "";
						jsonobject.put("bk_supplier_account",
								bk_supplier_account);
						jsonobject.put("ip_property", ip_property + "");
						jsonobject.put("bk_inst_id", bk_inst_id);
						jsonobject.put("use_state", use_status);
						//jsonobject.put("ip_state", ip_status);
						jsonobject.put("owner", owner);
						String bk_comment = remarks[i];
						lg.writeLog("bk_comment-----" + bk_comment);
						if (!bk_comment.equals("00000")) {
							jsonobject.put("bk_comment", bk_comment);
						}
						if (ip_property == 3) {
							RecordSet rs2 = new RecordSet();
							String[] device_type = Util.null2String(
									request.getParameter("type")).split(","); // 设备类型,当IP属性为业务IP时,需要新增
							String sql2 = "select joinEquipmentTypName from uf_joinEquipmentTyp where id='"
									+ device_type[i] + "'";
							rs2.executeSql(sql2);
							lg.writeLog("更新操作sql2----" + sql2);
							if (rs2.next()) {
								jsonobject
										.put("device_type",
												Util.null2String(rs2
														.getString("joinEquipmentTypName")));
								lg.writeLog("joinEquipmentTypName-----"
										+ Util.null2String(rs2
												.getString("joinEquipmentTypName")));
							}
						}
						lg.writeLog("update_ip发送的json数据为:"
								+ jsonobject.toString());
						String Ulocal = this.sendPost(url,
								jsonobject.toString(), "put");
						lg.writeLog("update_ip接受的json数据为:" + Ulocal);
						if (ip_property == 6) { // 公网IP则需要与内网IP进行关联
							// 关联操作
							String inip = Util.null2String(request
									.getParameter("inip")); // 内网IP,用于与公网IP关联
							String[] inip_name = inip.split(",");
							int bk_asst_inst_id = search_IPde(inip_name[i]); // 内网IP的实列id
							String bk_obj_asst_id = "ip_default_ip"; // 关联模型id
							lg.writeLog("bk_asst_inst_id------"
									+ bk_asst_inst_id);
							String TEMP_URL = "http://10.10.20.102:33031/api/v3";
							String Turl = TEMP_URL
									+ "/inst/association/action/create";

							JSONObject jsoncreate_DNS = new JSONObject();
							jsoncreate_DNS
									.put("bk_obj_asst_id", bk_obj_asst_id);
							jsoncreate_DNS.put("bk_inst_id", bk_inst_id);
							jsoncreate_DNS.put("bk_asst_inst_id",
									bk_asst_inst_id);

							lg.writeLog("create_ip_relation发送的json数据为:"
									+ jsoncreate_DNS.toString());
							String DNS_Relation = this.sendPost(Turl,
									jsoncreate_DNS.toString(), "post");
							lg.writeLog("create_ip_relation接受的json数据为:"
									+ DNS_Relation);
							JSONObject jsoncreate_DNSResult = JSONObject
									.fromObject(DNS_Relation);
							if (!jsoncreate_DNSResult.getBoolean("result")) {
								dnsresult = false;
							}
						}
					}
					if (!dnsresult) {
						try {
							response.setContentType("application/json; charset=utf-8");
							response.getWriter().print("2"); // 关联内网IP失败!
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					lg.writeLog("分配IP错误或不存在");
					try {
						response.setContentType("application/json; charset=utf-8");
						response.getWriter().print("1"); // 分配IP错误或不存在
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print("0"); // 填写分配IP网段与接入网络不一致
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			lg.writeLog("更新cmdb中的IP信息并关联内网IP异常:" + e);
		}
	}

	/**
	 * 发送post请求
	 * 
	 * @param httpUrl
	 * @param param
	 * @return
	 */
	public String sendPost(String url, String param, String type) {
		OutputStreamWriter out = null;
		BufferedReader in = null;
		String result = "";

		try {

			URL realUrl = new URL(url);
			HttpURLConnection conn = null;
			conn = (HttpURLConnection) realUrl.openConnection();// 打开和URL之间的连接
			// 发送POST请求必须设置如下两行
			if (type.equals("put")) {
				conn.setRequestMethod("PUT");
			} else if (type.equals("post")) {
				conn.setRequestMethod("POST");
			}
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

	/**
	 * 发送post请求
	 * 
	 * @param httpUrl
	 * @param param
	 * @return
	 */
	/*
	 * public String doPost(String httpUrl, String param,String type) {
	 * 
	 * HttpURLConnection connection = null; InputStream is = null; OutputStream
	 * os = null; BufferedReader br = null; String result = null; try { URL url
	 * = new URL(httpUrl); // 通过远程url连接对象打开连接 connection = (HttpURLConnection)
	 * url.openConnection(); // 设置连接请求方式 if(type.equals("put")){
	 * connection.setRequestMethod("PUT"); }else if(type.equals("post")){
	 * connection.setRequestMethod("POST"); }
	 * 
	 * // 设置连接主机服务器超时时间：15000毫秒 connection.setConnectTimeout(15000); //
	 * 设置读取主机服务器返回数据超时时间：60000毫秒 connection.setReadTimeout(60000);
	 * 
	 * // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true connection.setDoOutput(true); //
	 * 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无 connection.setDoInput(true); //
	 * 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。 // 设置通用的请求属性
	 * connection.setRequestProperty("BK_USER", "admin");
	 * connection.setRequestProperty("HTTP_BLUEKING_SUPPLIER_ID", "0");
	 * connection.setRequestProperty("Content-Type", "application/json"); //
	 * 通过连接对象获取一个输出流 os = connection.getOutputStream(); //
	 * 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的 os.write(param.getBytes()); //
	 * 通过连接对象获取一个输入流，向远程读取 //if (connection.getResponseCode() == 200) { is =
	 * connection.getInputStream(); // 对输入流对象进行包装:charset根据工作项目组的要求来设置 br = new
	 * BufferedReader(new InputStreamReader(is, "UTF-8"));
	 * 
	 * StringBuffer sbf = new StringBuffer(); String temp = null; //
	 * 循环遍历一行一行读取数据 while ((temp = br.readLine()) != null) { sbf.append(temp);
	 * sbf.append("\r\n"); } result = sbf.toString(); //} } catch
	 * (MalformedURLException e) { System.out.println("发送 POST 请求出现异常！" + e);
	 * lg.writeLog("发送 POST 请求出现异常！" + e);
	 * 
	 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
	 * finally { // 关闭资源 if (null != br) { try { br.close(); } catch
	 * (IOException e) { e.printStackTrace(); } } if (null != os) { try {
	 * os.close(); } catch (IOException e) { e.printStackTrace(); } } if (null
	 * != is) { try { is.close(); } catch (IOException e) { e.printStackTrace();
	 * } } // 断开与远程地址url的连接 connection.disconnect(); } return result; }
	 */
}
