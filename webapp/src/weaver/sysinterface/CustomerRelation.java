package weaver.sysinterface;

import java.util.Arrays;
import java.util.Calendar;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.formmode.view.ModeShareManager;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.StringHelper;

public class CustomerRelation extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;

	private HttpServletResponse response;
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
			if ("getAllinfo".equals(action)) {// 获取所有信息接口
				getAllinfo(user);
			} else if ("getbusinessOptions".equals(action)) {// 获取业务选项接口
				getbusinessOptions(user);
			} else if ("searchBegin".equals(action)) {// 搜索接口
				searchBegin(user);
			} else if ("getAllkhInfo".equals(action)) {// 获取所有客户信息接口
				getAllkhInfo(user);
			} else if ("searchbykh".equals(action)) {// 客户搜索接口
				searchbykh(user);
			} else if ("getkhglrole".equals(action)) {// 获取客户管理角色接口
				getkhglrole(user);
			} else if ("getywInfo".equals(action)) {// 获取业务信息接口
				getywInfo(user);
			} else if ("getmenuInfo".equals(action)) {// 获取菜单接口
				getmenuInfo(user);
			} else if ("getTodayRemind".equals(action)) {
				getTodayRemind(user); // 获取每日提醒信息接口
			} else if ("getUser".equals(action)) {
				getUser(user); // 判断用户是否能登录CRM系统接口
			} else if ("getcrm_mark".equals(action)) {
				getcrm_mark(user); // 获取客户标签接口
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取数据共享权限 sql
	 * 
	 * @param formmodeid
	 *            建模id
	 * @param userid
	 *            用户id
	 * @return
	 */
	public String getShareSql(int formmodeid, int userid) {
		RecordSet rs = new RecordSet();
		User user = new User();
		String sql = "";
		if (userid == 1) {
			sql = "select * from HrmResourceManager where id=" + userid;
		} else {
			sql = "select * from HrmResource where id=" + userid;
		}
		rs.executeSql(sql);
		rs.next();
		user.setUid(rs.getInt("id"));
		user.setLoginid(rs.getString("loginid"));
		user.setSeclevel(rs.getString("seclevel"));
		user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
		user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"),
				0));
		user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"),
				0));
		user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"),
				0));
		user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"),
				0));
		user.setManagerid(rs.getString("managerid"));
		user.setLogintype("1");

		ModeShareManager modeshare = new ModeShareManager();
		modeshare.setModeId(formmodeid);
		String rightsql = modeshare.getShareDetailTableByUser("formmode", user);
		return rightsql;
	}

/*	*//**
	 * 客户关系-获取所有信息接口
	 * 
	 * @param user
	 *            用户 pageno 当前页数 pagesize 每页显示数
	 *//*
	private void getAllinfo(User user) {
		long startTime = System.currentTimeMillis();
		lg.writeLog("程序运行开始时间： " + startTime + "ms");
		String userid = String.valueOf(user.getUID());
		// userid="29";
		RecordSet rs = new RecordSet();
		String sql = "select b.id,b.name,b.dsporder from uf_crm_custbusiness a,uf_crm_businessinfo b,Matrixtable_7 as c "
				+ "where a.business=b.id and b.id=c.business and  ("
				+ userid
				+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) "
				+ "or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.businessleader,',')) "
				+ "or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.businessmanager,',')) "
				+ "or ("
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.regionleader,',')) "
				+ "and a.regionalcenter is not null) or "
				+ userid
				+ " =a.accountmanager) group by b.id,b.name,b.dsporder  order by b.dsporder";

		rs.executeSql(sql);
		RecordSet rs2 = new RecordSet();

		String content = "";
		while (rs.next()) {
			long startTime1 = System.currentTimeMillis();
			lg.writeLog("程序运行子开始时间： " + startTime1 + "ms");
			String id = Util.null2String(rs.getString("id"));
			String name = Util.null2String(rs.getString("name"));
			String sql2 = "";
			sql2 = "select a.id,a.name,b.linkid,b.linkdate,"
					+ "(select top 1 id from uf_crm_contract where customer=a.id and business='"
					+ id
					+ "' order by status,id) as htid,"
					+ "(select top 1 status from uf_crm_contract where customer=a.id and business='"
					+ id
					+ "' order by status,id) as htstatus,"
					+ "(select sum(contractmoney) as contractmoney from uf_crm_contract where customer=a.id and business='"
					+ id
					+ "' and status='0') as contractmoney,"
					+ " b.invoicemoney,b.reallymoney,b.qxid,b.qxstatus,b.isaudit,b.isrequirement "
					+ " from uf_crm_customerinfo a,uf_crm_custbusiness b,Matrixtable_7 as c  "
					+ " where a.id=b.customer and b.business=c.business and b.business="
					+ id
					+ " "
					+ " and ("
					+ userid
					+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) "
					+ " or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.businessleader,',')) "
					+ " or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.businessmanager,',')) "
					+ " or ("
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.regionleader,',')) and b.regionalcenter is not null) "
					+ " or " + userid + " =b.accountmanager)";
			lg.writeLog("客户业务统计数据sql----" + sql2);
			rs2.executeSql(sql2);
			String xcontent = "";
			int Serialnumber = 0;
			while (rs2.next()) {
				Serialnumber++;
				String xid = Util.null2String(rs2.getString("id"));
				String xname = Util.null2String(rs2.getString("name"));
				String linkid = Util.null2String(rs2.getString("linkid"));// 联系记录id
				String linkdate = Util.null2String(rs2.getString("linkdate"));// 联系记录
				String htid = Util.null2String(rs2.getString("htid"));// 合同id
				String status = Util.null2String(rs2.getString("htstatus"));// 合同状态
				if (status.equals("0")) {
					status = "有效";
				} else if (status.equals("1")) {
					status = "终止";
				}
				String contractmoney = Util.null2String(rs2
						.getString("contractmoney"));// 合同金额
				String invoicemoney = Util.null2String(rs2
						.getString("invoicemoney"));// 开票金额
				String reallymoney = Util.null2String(rs2
						.getString("reallymoney"));// 账款金额
				String qxid = Util.null2String(rs2.getString("qxid"));// 权限id
				String qxstatus = Util.null2String(rs2.getString("qxstatus"));// 权限
				String audit = Util.null2String(rs2.getString("isaudit"));// 审计
				String requirement = Util.null2String(rs2
						.getString("isrequirement"));// 需求

				xcontent += "<tr>" + "<td class=\"second-child\">"
						+ Serialnumber
						+ "</td>"
						+ "<td><a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=102&formId=-256&billid="
						+ xid
						+ "&opentype=0&customid=139&viewfrom=fromsearchlist&mainid=0\" target= \"_blank\" title=\""
						+ xname
						+ "\">"
						+ xname
						+ "</a></td>"
						+ "<td class=\"second-child\"><a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=109&formId=-266&billid="
						+ linkid
						+ "\" target= \"_blank\">"
						+ linkdate
						+ "</a></td>"
						+ "<td class=\"second-child\"><a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=112&formId=-268&billid="
						+ htid
						+ "\" target= \"_blank\">"
						+ status
						+ "</a></td>"
						+ "<td class=\"second-child\">"
						+ contractmoney
						+ "</td>"
						+ "<td class=\"second-child\">"
						+ invoicemoney
						+ "</td>"
						+ "<td class=\"second-child\">"
						+ reallymoney
						+ "</td>"
						+ "<td class=\"second-child\"><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=195&businessid="
						+ id
						+ "&customerid="
						+ xid
						+ "\" target= \"_blank\">"
						+ qxstatus
						+ "</a></td>"
						+ "<td class=\"second-child\"><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=149\" target= \"_blank\">"
						+ audit
						+ "</a></td>"
						+ "<td class=\"second-child\" style=\"border-right: none;\"><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=158\" target= \"_blank\">"
						+ requirement + "</a></td>" + "</tr>";
			}

			content += "<tr>"
					+ "<td colspan=\"10\" style=\"background-color: rgb(248,248,248);border-right: none;color:#d44632;font-size:16px;\"><a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=104&formId=-257&billid="
					+ id + "&customid=140\" target= \"_blank\">" + name
					+ "</a></td>" + "</tr>" + "" + xcontent + "";
			long endTime1 = System.currentTimeMillis(); // 获取结束时间
			lg.writeLog("程序运行子结束时间： " + endTime1 + "ms");
			lg.writeLog(name + "--程序运行子时间： " + (endTime1 - startTime1) + "ms");
		}
		long endTime = System.currentTimeMillis(); // 获取结束时间
		lg.writeLog("程序运行结束时间： " + endTime + "ms");
		lg.writeLog("程序运行总时间： " + (endTime - startTime) + "ms");
		JSONObject jsonobject = new JSONObject();
		jsonobject.put("contents", content);
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(jsonobject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
	/**
	 * 客户关系-获取业务选项接口
	 * 
	 * @param user
	 *            用户 pageno 当前页数 pagesize 每页显示数
	 */
	private void getbusinessOptions(User user) {

		String userid = String.valueOf(user.getUID());
		RecordSet rs = new RecordSet();
		//String sql = " select * from uf_crm_businessinfo ";
		String sql = "select distinct b.id,b.name,b.dsporder from  uf_crm_businessinfo b,Matrixtable_7 as c where b.id=c.business "
				+ "and  ("
				+ userid
				+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.businessleader,',')) "
				+ "or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.businessmanager,',')) "
				+ "or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.regionleader,',')) "
				+ "or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.custommanager,',')) "
				+ "or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.regionmanager,',')) "
				+ " ) order by b.dsporder";
		rs.executeSql(sql);
		String Alloptions = "<option value=\"0\">无</option>";
		while (rs.next()) {
			String id = Util.null2String(rs.getString("id"));
			String name = Util.null2String(rs.getString("name"));
			Alloptions += "<option value=\"" + id + "\">" + name + "</option>";
		}
		JSONObject jsonobject = new JSONObject();
		jsonobject.put("alloption", Alloptions);
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(jsonobject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 客户关系-获取客户标签接口
	 * 
	 * @param user
	 *            用户 pageno 当前页数 pagesize 每页显示数
	 */
	private void getcrm_mark(User user) {
		
		String mark = Util.null2String(request.getParameter("mark")).trim();// 搜索的字符串
		String userid = String.valueOf(user.getUID());
		RecordSet rs = new RecordSet();
		JSONArray jsonArray = new JSONArray();
		String sql = " select * from uf_crm_mark where status = 0 and modedatacreater = '"+userid+"' ";
		if(!StringHelper.isEmpty(mark)){
			sql += " and name LIKE '%" + mark + "%'";
		}
		sql += " ORDER BY dsporder ";
		rs.executeSql(sql);
		while (rs.next()) {
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("id", Util.null2String(rs.getString("id")));
			jsonobject.put("name", Util.null2String(rs.getString("name")));
			jsonArray.add(jsonobject);
		}
		JSONObject result = new JSONObject();
		
		result.put("customer", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 客户关系-搜索接口
	 * 
	 * @param user
	 *            用户 pageno 当前页数 pagesize 每页显示数
	 */
	private void searchBegin(User user) {
		String khname = Util.null2String(request.getParameter("searchStr"));// 搜索的字符串
		String ywid = Util.null2String(request.getParameter("searchOptionid"));// 搜索的字符串
		String crm_mark = Util.null2String(request.getParameter("crm_mark")).trim();// 搜索的字符串
		//lg.writeLog("搜索的字符串khname----"+khname);
		//lg.writeLog("搜索的字符串ywid----"+ywid);
		String userid = String.valueOf(user.getUID());
		RecordSet rs = new RecordSet();
		RecordSet rs2 = new RecordSet();
		RecordSet rs11 = new RecordSet();
		RecordSet rs12 = new RecordSet();
		RecordSet rs13 = new RecordSet();
		RecordSet rs14 = new RecordSet();
		RecordSet rs15 = new RecordSet();
		String sqlwhere = "";
		if ("".equals(khname)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and a.oldname like '%" + khname + "%' ";
		}
		if ("0".equals(ywid)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and b.business='" + ywid + "' ";
		}
		String sql = "select DISTINCT c.name as busname,b.business "
				+ "from uf_crm_customerinfo a,uf_crm_custbusiness b,uf_crm_businessinfo c,Matrixtable_7 as d "
				+ "where a.id=b.customer and c.id=d.business "
				+ "and b.business=c.id  "
				+ sqlwhere
				+ " "
				+ "and  ("
				+ userid
				+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(d.businessleader,',')) or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(d.businessmanager,',')) or ("
				+ userid
				+ " in (select F1 from vfn_Splitstr(d.regionleader,',')) and b.regionalcenter is not null) or "
				+ userid + " =b.accountmanager) "
				+ "order by c.name,b.business";
		
		lg.writeLog("sql-----"+sql);
		rs.executeSql(sql);
		JSONArray jsonArrays = new JSONArray();
		JSONArray jsonArray = new JSONArray();
		while (rs.next()) {
			JSONObject json = new JSONObject();
			String id = Util.null2String(rs.getString("business"));
			String name = Util.null2String(rs.getString("busname"));
			json.put("id", id);
			json.put("name", name);
			jsonArrays.add(json);
			String sql2 = "";			
			sql2 = "select *,(select sum(contractmoney) as contractmoney from uf_crm_contract where customer=ts.id and business='"+id+"' and status='0') as contractmoney  " 
					+"from (select ROW_NUMBER() over(partition by  id order by htstatus,htid) as num,t.* from "
			        +"(select a.id,a.name,b.linkid,b.linkdate,d.id as htid,d.status as htstatus, b.invoicemoney,b.kpmoney,b.reallymoney,b.qxid,b.qxstatus,b.isaudit,b.isrequirement,b.mark "
					+ " from uf_crm_customerinfo  a "
                    +"  join uf_crm_custbusiness b on a.id=b.customer "
                    +"  join Matrixtable_7 c on  b.business=c.business "
                    +"  left join uf_crm_contract d on b.customer=d.customer and b.business=d.business "
					+ " where b.business='"+id+"'" 
					+sqlwhere
					+ " and ("
					+ userid
					+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) "
					+ " or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.businessleader,',')) "
					+ " or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.businessmanager,',')) "
					+ " or ("
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.regionleader,',')) and b.regionalcenter is not null) "
					+ " or " + userid + " =b.accountmanager)) t) ts where num=1";
			if(!StringHelper.isEmpty(crm_mark) && !crm_mark.equals("0")){
				sql2 += " and ','+ CAST(mark AS VARCHAR)  + ',' like '%,"+crm_mark+",%' ";
			}
			rs2.executeSql(sql2);
			int Serialnumber = 0;
			while (rs2.next()) {
				JSONObject jsonobject = new JSONObject();
				Serialnumber++;
				String status =Util.null2String(rs2.getString("htstatus"));// 合同状态
				if (status.equals("0")) {
					status = "有效";
				} else if (status.equals("1")) {
					status = "终止";
				}			
				jsonobject.put("id", id);
				jsonobject.put("Serialnumber", Serialnumber);
				jsonobject.put("xid", Util.null2String(rs2.getString("id")));
				jsonobject.put("xname", Util.null2String(rs2.getString("name")));
				jsonobject.put("linkid", Util.null2String(rs2.getString("linkid")));// 联系记录id
				jsonobject.put("linkdate", Util.null2String(rs2.getString("linkdate")));// 联系记录
				jsonobject.put("htid", Util.null2String(rs2.getString("htid")));// 合同id
				jsonobject.put("status",  status);
				jsonobject.put("contractmoney", Util.null2String(rs2
						.getString("contractmoney")));// 合同金额
				jsonobject.put("invoicemoney", Util.null2String(rs2
						.getString("invoicemoney")));// 金额
				jsonobject.put("reallymoney", Util.null2String(rs2
						.getString("reallymoney")));// 账款金额			
				jsonobject.put("kpmoney", Util.null2String(rs2
						.getString("kpmoney")));//开票金额	
				jsonobject.put("qxid", Util.null2String(rs2.getString("qxid")));// 权限id
				jsonobject.put("qxstatus",  Util.null2String(rs2.getString("qxstatus")));// 权限
				jsonobject.put("audit", Util.null2String(rs2.getString("isaudit")));// 审计
				jsonobject.put("requirement", Util.null2String(rs2
						.getString("isrequirement")));// 需求
				jsonArray.add(jsonobject);
				
			}


		}
		JSONObject result = new JSONObject();
		result.put("customer", jsonArrays.toString());
		result.put("business", jsonArray.toString());
		lg.writeLog("搜索结果-----"+result);
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 客户关系-获取所有客户信息接口
	 * 
	 * @param user
	 *            用户 pageno 当前页数 pagesize 每页显示数
	 */
	private void getAllkhInfo(User user) {

		String userid = String.valueOf(user.getUID());
		RecordSet rs = new RecordSet();
		// RecordSet rs2 = new RecordSet();
		int recordCount = 0;// 总记录数
		int pageno = Util.getIntValue(request.getParameter("pageno"), 1);// 当前页
		int pagesize = Util.getIntValue(request.getParameter("pagesize"), 10);// 每页显示数

		rs.executeSql("select count(1) from uf_crm_customerinfo");
		if (rs.next()) {
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;// 第几页的总数
		int ipageset = pagesize;// 第几页的显示条数

		if ((recordCount - iNextNum + pagesize) > 0) {
			if ((recordCount - iNextNum + pagesize < pagesize)) {
				ipageset = recordCount - iNextNum + pagesize;
			} else {
				ipageset = pagesize;
			}
		} else {
			ipageset = 0;
		}
		// System.out.println("iNextNum:"+iNextNum);
		String selSql = "";
		// 取按时间倒序排序的数据的前iNextNum条倒序排序
		selSql = "select top "
				+ iNextNum
				+ " id,name,creditcode,corporatdelegate,registeredcapital,founddate,address,createdate from uf_crm_customerinfo order by createdate desc";
		/*
		 * //将上面得到的数据取按顺序排序的ipageset条数据顺序排序 selSql = "select top " + ipageset
		 * +" t1.* from (" + selSql + ") t1 order by t1.createdate asc";
		 * //将得到的数据倒序排序显示 selSql = "select t2.* from (" + selSql +
		 * ") t2 order by t2.createdate desc";
		 */

		JSONArray jsonArray = new JSONArray();

		rs.executeSql(selSql);

		while (rs.next()) {

			JSONObject json = new JSONObject();
			json.put("id", Util.null2String(rs.getString("id")));
			json.put("name", Util.null2String(rs.getString("name")));
			json.put("creditcode", Util.null2String(rs.getString("creditcode")));
			json.put("corporatdelegate",
					Util.null2String(rs.getString("corporatdelegate")));
			json.put("registeredcapital",
					Util.null2String(rs.getString("registeredcapital")));
			json.put("founddate", Util.null2String(rs.getString("founddate")));
			json.put("address", Util.null2String(rs.getString("address")));
			json.put("createdate", Util.null2String(rs.getString("createdate")));
			jsonArray.add(json);
		}

		JSONObject result = new JSONObject();
		result.put("totalSize", recordCount);
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 客户关系-客户搜索接口
	 * 
	 * @param user
	 *            用户 pageno 当前页数 pagesize 每页显示数
	 */
	private void searchbykh(User user) {
		String khname = Util.null2String(request.getParameter("searchStr"));// 搜索的字符串
		String userid = String.valueOf(user.getUID());
		int recordCount = 0;// 总记录数
		int pageno = Util.getIntValue(request.getParameter("pageno"), 1);// 当前页
		int pagesize = Util.getIntValue(request.getParameter("pagesize"), 10);// 每页显示数
		RecordSet rs = new RecordSet();
		String sqlwhere = "";
		if ("".equals(khname)) {
			sqlwhere += "";
		} else {
			sqlwhere += " where oldname like '%" + khname + "%' ";
		}
		this.lg.writeLog("sqlwhere=" + sqlwhere);
		rs.executeSql("select count(1) from uf_crm_customerinfo " + sqlwhere
				+ "");
		if (rs.next()) {
			recordCount = rs.getInt(1);
		}
		int iNextNum = pageno * pagesize;// 第几页的总数
		int ipageset = pagesize;// 第几页的显示条数

		if ((recordCount - iNextNum + pagesize) > 0) {
			if ((recordCount - iNextNum + pagesize < pagesize)) {
				ipageset = recordCount - iNextNum + pagesize;
			} else {
				ipageset = pagesize;
			}
		} else {
			ipageset = 0;
		}
		// System.out.println("iNextNum:"+iNextNum);
		String selSql = "";
		// 取按时间倒序排序的数据的前iNextNum条倒序排序
		selSql = "select top "
				+ iNextNum
				+ " id,name,creditcode,corporatdelegate,registeredcapital,founddate,address,createdate from uf_crm_customerinfo "
				+ sqlwhere + " order by createdate desc,id desc";
		// 将上面得到的数据取按顺序排序的ipageset条数据顺序排序
		selSql = "select top " + ipageset + " t1.* from (" + selSql
				+ ") t1 order by t1.createdate asc,t1.id asc";
		// 将得到的数据倒序排序显示
		selSql = "select t2.* from (" + selSql
				+ ") t2 order by t2.createdate desc,t2.id desc";

		JSONArray jsonArray = new JSONArray();
		this.lg.writeLog("selSql=" + selSql);
		rs.executeSql(selSql);

		while (rs.next()) {

			JSONObject json = new JSONObject();
			json.put("id", Util.null2String(rs.getString("id")));
			json.put("name", Util.null2String(rs.getString("name")));
			json.put("creditcode", Util.null2String(rs.getString("creditcode")));
			json.put("corporatdelegate",
					Util.null2String(rs.getString("corporatdelegate")));
			json.put("registeredcapital",
					Util.null2String(rs.getString("registeredcapital")));
			json.put("founddate", Util.null2String(rs.getString("founddate")));
			json.put("address", Util.null2String(rs.getString("address")));
			json.put("createdate", Util.null2String(rs.getString("createdate")));
			jsonArray.add(json);
		}

		JSONObject result = new JSONObject();
		result.put("totalSize", recordCount);
		result.put("datas", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取管理角色接口
	 * 
	 * @param user
	 */
	private void getkhglrole(User user) {
		RecordSet rs = new RecordSet();
		String userid = String.valueOf(user.getUID());
		String sql = "select resourceid from hrmrolemembers where roleid=161 and resourceid='"
				+ userid + "'";
		rs.executeSql(sql);
		String role = "";
		String leaderRole = "";
		String businessleaderRole = "";
		String custommanagerRole = "";
		String businessmanagerRole = "";
		String regionleaderRole = "";
		String regionmanagerRole = "";
		String customadminRole = "";
		if (rs.getCounts() > 0) {
			leaderRole = "市场部负责人";
		}
		role += leaderRole;
		String selSql = "select * from  Matrixtable_7";
		int flag1 = 0;
		int flag2 = 0;
		int flag3 = 0;
		int flag4 = 0;
		int flag5 = 0;
		int flag6 = 0;
		rs.executeSql(selSql);
		while (rs.next()) {
			if (flag1 == 0) {
				String businessleaderid = Util.null2String(rs
						.getString("businessleader"));
				if (userid.equals(businessleaderid)) {
					businessleaderRole = "业务负责人";
					flag1 = 1;
				}
			}
			if (flag2 == 0) {
				String custommanagerids = Util.null2String(rs
						.getString("custommanager"));
				String[] ids = custommanagerids.split(",");
				boolean flag = Arrays.asList(ids).contains(userid);
				if (flag) {
					custommanagerRole = "客户经理";
					flag2 = 1;
				}
			}
			if (flag3 == 0) {
				String businessmanagerids = Util.null2String(rs
						.getString("businessmanager"));
				String[] businessmanagerid = businessmanagerids.split(",");
				boolean businessmanagerflag = Arrays.asList(businessmanagerid).contains(userid);
				if (businessmanagerflag) {
					businessmanagerRole = "业务经理";
					flag3 = 1;
				}
			}
			if (flag4 == 0) {
				String regionleaderid = Util.null2String(rs
						.getString("regionleader"));
				if (userid.equals(regionleaderid)) {
					regionleaderRole = "区域中心首代";
					flag4 = 1;
				}
			}
			if (flag5 == 0) {
				String regionmanagerids = Util.null2String(rs
						.getString("regionmanager"));
				String[] regionmanagerid = regionmanagerids.split(",");
				boolean regionmanagerflag = Arrays.asList(regionmanagerid).contains(userid);
				if (regionmanagerflag) {
					regionmanagerRole = "区域中心客户经理";
					flag5 = 1;
				}				
			}
			if (flag6 == 0) {
				String customadminid = Util.null2String(rs
						.getString("customadmin"));
				if (userid.equals(customadminid)) {
					customadminRole = "客户管理员";
					flag6 = 1;
				}
			}
		}
		if (!"".equals(role) && !"".equals(businessleaderRole)) {
			role += "," + businessleaderRole;
		} else {
			role += businessleaderRole;
		}
		if (!"".equals(role) && !"".equals(custommanagerRole)) {
			role += "," + custommanagerRole;
		} else {
			role += custommanagerRole;
		}
		if (!"".equals(role) && !"".equals(businessmanagerRole)) {
			role += "," + businessmanagerRole;
		} else {
			role += businessmanagerRole;
		}
		if (!"".equals(role) && !"".equals(regionleaderRole)) {
			role += "," + regionleaderRole;
		} else {
			role += regionleaderRole;
		}
		if (!"".equals(role) && !"".equals(regionmanagerRole)) {
			role += "," + regionmanagerRole;
		} else {
			role += regionmanagerRole;
		}
		if (!"".equals(role) && !"".equals(customadminRole)) {
			role += "," + customadminRole;
		} else {
			role += customadminRole;
		}
		JSONObject result = new JSONObject();
		result.put("data", role);
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 客户关系-获取业务信息接口
	 * 
	 * @param user
	 *            用户
	 */
	private void getywInfo(User user) {

		String userid = String.valueOf(user.getUID());
		RecordSet rs = new RecordSet();
		String sql = "select b.id,b.name,b.dsporder "
				+ "from  uf_crm_custbusiness a,uf_crm_businessinfo b,Matrixtable_7 as c "
				+ "where a.business=b.id and b.id=c.business "
				+ "and ("
				+ userid
				+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) "
				+ "or " + userid
				+ " in (select F1 from vfn_Splitstr(c.businessleader,',')) "
				+ "or " + userid
				+ " in (select F1 from vfn_Splitstr(c.businessmanager,',')) "
				+ "or (" + userid
				+ " in (select F1 from vfn_Splitstr(c.regionleader,',')) "
				+ "and a.regionalcenter is not null) or " + userid
				+ " =a.accountmanager) "
				+ "group by b.id,b.name,b.dsporder  order by b.dsporder";
		rs.executeSql(sql);
		RecordSet rs2 = new RecordSet();
		boolean flag = true;
		String contentleft = "";
		String contentright = "";
		while (rs.next()) {
			String id = Util.null2String(rs.getString("id"));
			String name = Util.null2String(rs.getString("name"));
			String sql2 = "select a.id,a.name "
					+ "from uf_crm_customerinfo a,uf_crm_custbusiness b,Matrixtable_7 as c  "
					+ "where a.id=b.customer and b.business=c.business and b.business="
					+ id
					+ " "
					+ "and ("
					+ userid
					+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) "
					+ "or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.businessleader,',')) "
					+ "or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.businessmanager,',')) "
					+ "or (" + userid
					+ " in (select F1 from vfn_Splitstr(c.regionleader,',')) "
					+ "and b.regionalcenter is not null) or " + userid
					+ " =b.accountmanager)";
			rs2.executeSql(sql2);
			int total = rs2.getCounts();
			if (flag) {
				contentleft += "<div class=\"info-r\">"
						+ "<p class=\"info-title\">"
						+ name
						+ "</p>"
						+ "<div>"
						+ "<table style=\"border-collapse:separate; border-spacing:0px 20px;width:100%\">"
						+ "<tbody id=\"flowList\">"
						+ "<tr>"
						+ "<td style=\"border-right: none;color:#333;font-size:18px;text-align:center;\">目标客户数</td>"
						+ "<td style=\"border-right: none;color:#333;font-size:18px;text-align:center;\">在约客户数</td>"
						+ "<td style=\"border-right: none;color:#333;font-size:18px;text-align:center;\">历史客户数</td>"
						+ "</tr>"
						+ "<tr>"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\">"
						+ total
						+ "</td>"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\"></td>"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\"></td>"
						+ "</tr>"
						+ "<tr>"
						+ "<td style=\"border-right: none;border-top: 0px solid red;color:#333;font-size:18px;text-align:center;\">合同数</td>"
						+ "<td style=\"border-right: none;border-top: 0px solid red;color:#333;font-size:18px;text-align:center;\">合同总金额</td>"
						+ "<td style=\"border-right: none;border-top: 0px solid red;color:#333;font-size:18px;text-align:center;\">当前收入情况</td>"
						+ "</tr>"
						+ "<tr >"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\"></td>"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\"></td>"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\"></td>"
						+ "</tr>"
						+ "</tbody>"
						+ "</table>"
						+ "</div>"
						+ "</div>";
				flag = false;
			} else {
				contentright += "<div class=\"info-r\">"
						+ "<p class=\"info-title\">"
						+ name
						+ "</p>"
						+ "<div>"
						+ "<table style=\"border-collapse:separate; border-spacing:0px 20px;width:100%\">"
						+ "<tbody id=\"flowList\">"
						+ "<tr>"
						+ "<td style=\"border-right: none;color:#333;font-size:18px;text-align:center;\">目标客户数</td>"
						+ "<td style=\"border-right: none;color:#333;font-size:18px;text-align:center;\">在约客户数</td>"
						+ "<td style=\"border-right: none;color:#333;font-size:18px;text-align:center;\">历史客户数</td>"
						+ "</tr>"
						+ "<tr>"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\">"
						+ total
						+ "</td>"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\"></td>"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\"></td>"
						+ "</tr>"
						+ "<tr>"
						+ "<td style=\"border-right: none;border-top: 0px solid red;color:#333;font-size:18px;text-align:center;\">合同数</td>"
						+ "<td style=\"border-right: none;border-top: 0px solid red;color:#333;font-size:18px;text-align:center;\">合同总金额</td>"
						+ "<td style=\"border-right: none;border-top: 0px solid red;color:#333;font-size:18px;text-align:center;\">当前收入情况</td>"
						+ "</tr>"
						+ "<tr >"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\"></td>"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\"></td>"
						+ "<td style=\"border-right: none;color:#0e90d2;font-size:16px;text-align:center;\"></td>"
						+ "</tr>"
						+ "</tbody>"
						+ "</table>"
						+ "</div>"
						+ "</div>";
				flag = true;
			}

		}
		JSONObject jsonobject = new JSONObject();
		jsonobject.put("left", contentleft);
		jsonobject.put("right", contentright);
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(jsonobject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 客户关系-获取菜单接口
	 * 
	 * @param user
	 *            用户
	 */
	private void getmenuInfo(User user) {
		RecordSet rs = new RecordSet();
		String userid = String.valueOf(user.getUID());
		String sql = "select resourceid from hrmrolemembers where roleid=161 and resourceid='"
				+ userid + "'";
		rs.executeSql(sql);
		String role = "";
		String leaderRole = "";
		String businessleaderRole = "";
		String custommanagerRole = "";
		String businessmanagerRole = "";
		String regionleaderRole = "";
		String regionmanagerRole = "";
		String customadminRole = "";
		String businessadminRole = "";
		String content = "";
		if (rs.getCounts() > 0) {
			leaderRole = "市场部负责人";
		}
		role += leaderRole;
		String selSql = "select * from  Matrixtable_7";
		int flag1 = 0;
		int flag2 = 0;
		int flag3 = 0;
		int flag4 = 0;
		int flag5 = 0;
		int flag6 = 0;
		int flag7 = 0;
		rs.executeSql(selSql);
		while (rs.next()) {
			if (flag1 == 0) {
				String businessleaderid = Util.null2String(rs
						.getString("businessleader"));
				if (userid.equals(businessleaderid)) {
					businessleaderRole = "业务负责人";
					flag1 = 1;
				}
			}
			if (flag2 == 0) {
				String custommanagerids = Util.null2String(rs
						.getString("custommanager"));
				String[] ids = custommanagerids.split(",");
				boolean flag = Arrays.asList(ids).contains(userid);
				if (flag) {
					custommanagerRole = "客户经理";
					flag2 = 1;
				}
			}
			if (flag3 == 0) {
				String businessmanagerids = Util.null2String(rs
						.getString("businessmanager"));
				String[] businessmanagerid = businessmanagerids.split(",");
				boolean businessmanagerflag = Arrays.asList(businessmanagerid).contains(userid);
				if (businessmanagerflag) {
					businessmanagerRole = "业务经理";
					flag3 = 1;
				}				
			}
			if (flag4 == 0) {
				String regionleaderid = Util.null2String(rs
						.getString("regionleader"));
				if (userid.equals(regionleaderid)) {
					regionleaderRole = "区域中心首代";
					flag4 = 1;
				}
			}
			if (flag5 == 0) {
				String regionmanagerids = Util.null2String(rs
						.getString("regionmanager"));
				String[] regionmanagerid = regionmanagerids.split(",");
				boolean regionmanagerflag = Arrays.asList(regionmanagerid).contains(userid);
				if (regionmanagerflag) {
					regionmanagerRole = "区域中心客户经理";
					flag5 = 1;
				}				
			}
			if (flag6 == 0) {
				String customadminid = Util.null2String(rs
						.getString("customadmin"));
				if (userid.equals(customadminid)) {
					customadminRole = "客户管理员";
					flag6 = 1;
				}
			}
			if (flag7 == 0) {
				String businessadminid = Util.null2String(rs
						.getString("businessadmin"));
				if (userid.equals(businessadminid)) {
					businessadminRole = "业务管理员";
					flag7 = 1;
				}
			}
		}
		if (!"".equals(role) && !"".equals(businessleaderRole)) {
			role += "," + businessleaderRole;
		} else {
			role += businessleaderRole;
		}
		if (!"".equals(role) && !"".equals(custommanagerRole)) {
			role += "," + custommanagerRole;
		} else {
			role += custommanagerRole;
		}
		if (!"".equals(role) && !"".equals(businessmanagerRole)) {
			role += "," + businessmanagerRole;
		} else {
			role += businessmanagerRole;
		}
		if (!"".equals(role) && !"".equals(regionleaderRole)) {
			role += "," + regionleaderRole;
		} else {
			role += regionleaderRole;
		}
		if (!"".equals(role) && !"".equals(regionmanagerRole)) {
			role += "," + regionmanagerRole;
		} else {
			role += regionmanagerRole;
		}
		if (!"".equals(role) && !"".equals(customadminRole)) {
			role += "," + customadminRole;
		} else {
			role += customadminRole;
		}
		if (!"".equals(role) && !"".equals(businessadminRole)) {
			role += "," + businessadminRole;
		} else {
			role += businessadminRole;
		}

		// 业务管理员有维护业务的权限
		if (role.indexOf("业务管理员") > -1) {
			content += "<div class=\"yewu-item\">"
					+ "<div class=\"yewu-header\">业务 <i class=\"am-icon am-icon-chevron-right\"></i></div>"
					+ "<ul>"
					+ "<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=453\" target=\"_blank\">新建业务</a></li>"
					+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=140\" target=\"_blank\">业务查询</a></li>"
					+ "</ul>" + "</div>";
		}

		content += "<div class=\"yewu-item\">"
				+ "<div class=\"yewu-header\">客户 <i class=\"am-icon am-icon-chevron-right\"></i></div>"
				+ "<ul>"
				+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=184\" target=\"_blank\">我的客户</a></li>"
				+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=170\" target=\"_blank\">客户标签</a></li>"
				+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=183\" target=\"_blank\">联系人</a></li>"
				+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=200\" target=\"_blank\">联系记录</a></li>"
				+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=201\" target=\"_blank\">走访计划</a></li>"
				+ "</ul>" + "</div>";
		content += "<div class=\"yewu-item\">"
				+ "<div class=\"yewu-header\">合同 <i class=\"am-icon am-icon-chevron-right\"></i></div>"
				+ "<ul>"
				+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=196\" target=\"_blank\">合同信息</a></li>"
				+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=197\" target=\"_blank\">格式合同</a></li>"
				+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=195\" target=\"_blank\">收款情况</a></li>"
				+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=192\" target=\"_blank\">许可公示查询</a></li>"
				+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=150\" target=\"_blank\">开通权限查询</a></li>"
				+ "</ul>" + "</div>";
		content += "<div class=\"yewu-item\">"
				+ "<div class=\"yewu-header\">收入 <i class=\"am-icon am-icon-chevron-right\"></i></div>"
				+ "<ul>"
				+ "<li><a>|</a><a href=\"/workflow/request/AddRequest.jsp?workflowid=452\" target=\"_blank\">新建收入计划</a></li>"
				+ "<li><a>|</a><a href=\"/ReportServer?reportlet=SzxReport/Crm/Incomeplan.cpt\" target=\"_blank\">收入计划汇总表 </a></li>"
				+ "</ul>" + "</div>";
		content += "<div class=\"yewu-item\">"
				+ "<div class=\"yewu-header\">统计 <i class=\"am-icon am-icon-chevron-right\"></i></div>"
				+ "<ul>"
				+ "<li><a>|</a><a href=\"/ReportServer?reportlet=SzxReport/Crm/visitplan.cpt\" target=\"_blank\">走访统计</a></li>"
				+ "<li><a>|</a><a href=\"/ReportServer?reportlet=SzxReport/Crm/IncomeDone.cpt\" target=\"_blank\">收入统计</a></li>"

				+ "</ul>" + "</div>";
		// 市场部负责人有维护角色的权限
		if (role.indexOf("市场部负责人") > -1) {
			content += "<div class=\"yewu-item\">"
					+ "<div class=\"yewu-header\">角色 <i class=\"am-icon am-icon-chevron-right\"></i></div>"
					+ "<ul>"
					+ "<li><a>|</a><a href=\"/matrixmanage/pages/matrixdesign.jsp?matrixid=7&showtype=1&issystem=\" target=\"_blank\">角色设置</a></li>"
					+ "<li><a>|</a><a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=142\" target=\"_blank\">区域中心设置</a></li>"
					+ "</ul>" + "</div>";
		}

		JSONObject result = new JSONObject();
		result.put("data", content);
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 客戶关系-获取今日提醒信息接口
	 * 
	 * @param user
	 *            用户 pageno 当前页数 pagesize 每页显示数
	 */
	private void getTodayRemind(User user) {
		Calendar todaycal = Calendar.getInstance();
		String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
				+ Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-"
				+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);

		String userid = String.valueOf(user.getUID());

		lg.writeLog("userid----" + userid);
		lg.writeLog("syndate----" + syndate);
		RecordSet rs = new RecordSet();
		// RecordSet rs2 = new RecordSet();
		int recordCount = 0;// 总记录数		
		String sql = "SELECT count(1) FROM uf_crm_notice WHERE person='"
				+ userid + "' AND noticedate='" + syndate
				+ "' AND status='0'";
		rs.executeSql(sql);
		if (rs.next()) {
			recordCount = rs.getInt(1);
		}
		
		String selSql = "";
		// 取按时间倒序排序的数据的前iNextNum条倒序排序
		selSql = "SELECT TOP 5 * FROM uf_crm_notice WHERE  person='" + userid
				+ "' AND  noticedate='" + syndate + "' AND status='0'  order by noticedate desc";
		String id = "";
		String title = "";
		String customer = "";		
		String business = "";
		String totalcount = "<a href=\"/formmode/search/CustomSearchBySimple.jsp?customid=182\"  target= \"_blank\">"
				+ recordCount + "</a>";
		String contents = "";
		int number = 1;
		JSONArray jsonArray = new JSONArray();

		rs.executeSql(selSql);

		while (rs.next()) {
			id = Util.null2String(rs.getString("id"));
			title = Util.null2String(rs.getString("title"));
			customer = Util.null2String(rs.getString("customer"));			
			business = Util.null2String(rs.getString("business"));
			String businessname = "";
			String customername = "";
			sql = "SELECT name FROM uf_crm_businessinfo WHERE id='" + business
					+ "'";
			RecordSet rs1 = new RecordSet();
			rs1.executeSql(sql);
			if (rs1.next()) {
				businessname = Util.null2String(rs1.getString("name"));

			}
			sql = "SELECT name FROM uf_crm_customerinfo WHERE id='" + customer
					+ "'";
			RecordSet rs2 = new RecordSet();
			rs2.executeSql(sql);
			if (rs2.next()) {
				customername = Util.null2String(rs2.getString("name"));
			}

			lg.writeLog("title----" + title);
			contents += "<tr>"
					+ "<td class=\"second-child\">"
					+ number
					+ ". "
					+ "</td>"
					+ "<td align=\"left\" width=\"450px\" ><a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=134&formId=-326&billid="
					+ id
					+ "&opentype=0&customid=182&viewfrom=fromsearchlist&mainid=0\"  target= \"_blank\" >"
					+ title
					+ "</a></td>"
					+ "<td  style=\"color:#d44632;font-size:16px;\" class=\"second-child\" width=\"450px\"><a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=104&formId=-257&billid="
					+ business
					+ "&customid=140\" target= \"_blank\">"
					+ businessname
					+ "</a></td>"
					+ "<td  style=\"color:#d44632;font-size:16px;\" class=\"second-child\" width=\"450px\"><a href=\"/formmode/view/AddFormMode.jsp?type=0&modeId=102&formId=-256&billid="
					+ customer
					+ "&opentype=0&customid=139&viewfrom=fromsearchlist&mainid=0\" target= \"_blank\" title=\""
					+ customername + "\">" + customername + "</a></td>"
					+ "</tr>";
			number++;
			
		}

		lg.writeLog("contents----" + contents);
		JSONObject result = new JSONObject();
		result.put("remind", contents);
		result.put("count", totalcount);
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 判断当前用户是否有权限登录CRM系统接口
	 * 
	 * @param user
	 *            用户
	 */
	public void getUser(User user) {
		String userid = String.valueOf(user.getUID()); // 用户id
		RecordSet rs = new RecordSet();
		String rolelevel = "";
		String sqlUser = " select hrs.id,'1' as rolelevel from hrmrolemembers hrme left join hrmroles hrs on hrme.roleid = hrs.id "
				+ " where hrs.id = 150  and hrme.resourceid = '"
				+ userid
				+ "'"
				+ // 公司领导角色
				" union all "
				+ " select hrs.id,'2' as rolelevel from hrmrolemembers hrme left join hrmroles hrs on hrme.roleid = hrs.id "
				+ " where hrs.id = 163  and hrme.resourceid = '"
				+ userid
				+ "'"
				+ // 客户管理员角色
				" union all "
				+ " select id,'3' as rolelevel from hrmresource where departmentid =3 and id = '"
				+ userid
				+ "'"
				+ // 市场部人员;
				" union all "
				+ " select hrs.id,'3' as rolelevel from hrmrolemembers hrme left join hrmroles hrs on hrme.roleid = hrs.id "
				+ " where hrs.id = 2  and hrme.resourceid = '" + userid + "'"; // 系统管理员
		lg.writeLog("判断用户角色sql----" + sqlUser);

		rs.executeSql(sqlUser);
		if (rs.next()) {
			rolelevel = Util.null2String(rs.getString("rolelevel"));
		}
		JSONObject result = new JSONObject();
		result.put("rolelevel", rolelevel);

		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 客户关系-获取所有信息接口
	 * 
	 * @param user
	 *            用户 pageno 当前页数 pagesize 每页显示数
	 */
	private void getAllinfo(User user) {
		long startTime = System.currentTimeMillis();
		lg.writeLog("程序运行开始时间： " + startTime + "ms");
		String userid = String.valueOf(user.getUID());
		// userid="29";
		RecordSet rs = new RecordSet();
		String sql = "select b.id,b.name,b.dsporder from uf_crm_custbusiness a,uf_crm_businessinfo b,Matrixtable_7 as c where a.business=b.id and b.id=c.business "
				+ "and  ("
				+ userid
				+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.businessleader,',')) "
				+ "or "
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.businessmanager,',')) "
				+ "or ("
				+ userid
				+ " in (select F1 from vfn_Splitstr(c.regionleader,',')) "
				+ "and a.regionalcenter is not null) or "
				+ userid
				+ " =a.accountmanager) group by b.id,b.name,b.dsporder  order by b.dsporder";

		rs.executeSql(sql);
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArrays = new JSONArray();

		JSONArray jsonArray = new JSONArray();
		while (rs.next()) {
			JSONObject json = new JSONObject();
			long startTime1 = System.currentTimeMillis();
			lg.writeLog("程序运行子开始时间： " + startTime1 + "ms");
			String id = Util.null2String(rs.getString("id"));
			String name = Util.null2String(rs.getString("name"));
			json.put("id", id);
			json.put("name", name);
			jsonArrays.add(json);
			String sql2 = "";
			/*
			sql2 = "select a.id,a.name,b.linkid,b.linkdate,"
					+ "(select top 1 id from uf_crm_contract where customer=a.id and business='"
					+ id
					+ "' order by status,id) as htid,"
					+ "(select top 1 status from uf_crm_contract where customer=a.id and business='"
					+ id
					+ "' order by status,id) as htstatus,"
					+ "(select sum(contractmoney) as contractmoney from uf_crm_contract where customer=a.id and business='"
					+ id
					+ "' and status='0') as contractmoney,"
					+ " b.invoicemoney,b.kpmoney,b.reallymoney,b.qxid,b.qxstatus,b.isaudit,b.isrequirement "
					+ " from uf_crm_customerinfo a,uf_crm_custbusiness b,Matrixtable_7 as c  "
					+ " where a.id=b.customer and b.business=c.business and b.business="
					+ id
					+ " "
					+ " and ("
					+ userid
					+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) "
					+ " or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.businessleader,',')) "
					+ " or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.businessmanager,',')) "
					+ " or ("
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.regionleader,',')) and b.regionalcenter is not null) "
					+ " or " + userid + " =b.accountmanager)";
					*/
			sql2 = "select *,(select sum(contractmoney) as contractmoney from uf_crm_contract where customer=ts.id and business='"+id+"' and status='0') as contractmoney  " 
					+"from (select ROW_NUMBER() over(partition by  id order by htstatus,htid) as num,t.* from "
			        +"(select a.id,a.name,b.linkid,b.linkdate,d.id as htid,d.status as htstatus, b.invoicemoney,b.kpmoney,b.reallymoney,b.qxid,b.qxstatus,b.isaudit,b.isrequirement "
					+ " from uf_crm_customerinfo  a "
                    +"  join uf_crm_custbusiness b on a.id=b.customer "
                    +"  join Matrixtable_7 c on  b.business=c.business "
                    +"  left join uf_crm_contract d on b.customer=d.customer and b.business=d.business "
					+ " where b.business='"+id+"'" 					
					+ " and ("
					+ userid
					+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) "
					+ " or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.businessleader,',')) "
					+ " or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.businessmanager,',')) "
					+ " or ("
					+ userid
					+ " in (select F1 from vfn_Splitstr(c.regionleader,',')) and b.regionalcenter is not null) "
					+ " or " + userid + " =b.accountmanager)) t) ts where num=1";
			//lg.writeLog("客户业务【"+name+"】统计数据sql----" + sql2);
			rs2.executeSql(sql2);

			int Serialnumber = 0;
			while (rs2.next()) {
				JSONObject jsonobject = new JSONObject();
				Serialnumber++;
				String status = Util.null2String(rs2.getString("htstatus"));
				if (status.equals("0")) {
					status = "有效";
				} else if (status.equals("1")) {
					status = "终止";
				}
				jsonobject.put("id", id);
				jsonobject.put("Serialnumber", Serialnumber);
				jsonobject.put("xid", Util.null2String(rs2.getString("id")));
				jsonobject
						.put("xname", Util.null2String(rs2.getString("name")));
				jsonobject.put("linkid",
						Util.null2String(rs2.getString("linkid")));// 联系记录id
				jsonobject.put("linkdate",
						Util.null2String(rs2.getString("linkdate")));// 联系记录
				jsonobject.put("htid", Util.null2String(rs2.getString("htid")));// 合同id
				jsonobject.put("status", status);// 合同状态
				jsonobject.put("contractmoney",
						Util.null2String(rs2.getString("contractmoney")));// 合同金额
				jsonobject.put("invoicemoney",
						Util.null2String(rs2.getString("invoicemoney")));// 开票金额  应收
				jsonobject.put("reallymoney",
						Util.null2String(rs2.getString("reallymoney")));// 账款金额 收款
				
				jsonobject.put("kpmoney",
						Util.null2String(rs2.getString("kpmoney")));// 开票

				jsonobject.put("qxid", Util.null2String(rs2.getString("qxid")));// 权限id
				jsonobject.put("qxstatus",
						Util.null2String(rs2.getString("qxstatus")));// 权限
				jsonobject.put("audit",
						Util.null2String(rs2.getString("isaudit")));// 审计
				jsonobject.put("requirement",
						Util.null2String(rs2.getString("isrequirement")));// 需求
				jsonArray.add(jsonobject);

			}

			long endTime1 = System.currentTimeMillis(); // 获取结束时间
			lg.writeLog("程序运行子结束时间： " + endTime1 + "ms");
			lg.writeLog(name + "--程序运行子时间： " + (endTime1 - startTime1) + "ms");
		}
		long endTime = System.currentTimeMillis(); // 获取结束时间
		lg.writeLog("程序运行结束时间： " + endTime + "ms");
		lg.writeLog("程序运行总时间： " + (endTime - startTime) + "ms");
		JSONObject result = new JSONObject();
		result.put("business", jsonArrays.toString());
		result.put("customer", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
