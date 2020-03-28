package weaver.sysinterface;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.file.FileUpload;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.MobileFileUpload;
import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.StringHelper;

public class mobileCustomerRelation extends HttpServlet {

	private HttpServletRequest request;

	private HttpServletResponse response;

	BaseBean lg = new BaseBean();

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

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
			if ("searchBegin".equals(action)) {// 搜索接口
				searchBegin(user);
			} else if ("Begininfo".equals(action)) { // 获取指定客户业务详情信息
				Begininfo(user);
			} else if ("linkmanList".equals(action)) { // 获取联系人列表
				linkmanList(user);
			} else if ("linkmanInfo".equals(action)) { // 获取联系人详细
				linkmanInfo(user);
			} else if ("insertOrUpdateLinkman".equals(action)) { // 修改/新增联系人详细
				insertOrUpdateLinkman(user);
			} else if ("calllogList".equals(action)) { // 获取联系记录列表
				calllogList(user);
			} else if ("calllogInfo".equals(action)) { // 获取联系记录详细
				calllogInfo(user);
			} else if ("contractList".equals(action)) { // 获取合同列表
				contractList(user);
			} else if ("contractInfo".equals(action)) { // 获取合同详细
				contractInfo(user);
			} else if ("permissionList".equals(action)) { // 获取业金额列表
				permissionList(user);
			} else if ("businessMoneyList".equals(action)) { // 获取权限内容列表
				businessMoneyList(user);
			} else if ("contractList_two".equals(action)) { // 获取合同列表
				contractList_Two(user);
			} else if ("linkmanList_two".equals(action)) { // 获取联系人列表
				linkmanList_Two(user);
			} else if ("customerList".equals(action)) { // 获取客户列表
				customerList(user);
			} else if ("businessList".equals(action)) { // 获取业务列表
				businessList(user);
			} else if ("calllogList_two".equals(action)) { // 获取联系记录列表
				calllogList_Two(user);
			} else if ("historycontractList".equals(action)) { // 获取历史合同
				historycontractList(user);
			}else if("removelinkmanInfo".equals(action)){
				removelinkmanInfo(user);
			}
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
		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));
		lg.writeLog("搜索的字符串khname----" + khname);
		lg.writeLog("搜索的字符串ywid----" + ywid);
		String userid = String.valueOf(user.getUID());
		RecordSet rs2 = new RecordSet();
		String sqlwhere = "";
		if ("".equals(khname)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and a.oldname like '%" + khname + "%' ";
		}
		String flag = "0";
		if ("0".equals(ywid) || StringHelper.isEmpty(ywid)) {
			sqlwhere += "";
			flag = "1";
		} else {
			sqlwhere += " and b.business='" + ywid + "' ";
		}
		JSONArray jsonArrays = new JSONArray();
		JSONArray jsonArray = new JSONArray();
		JSONObject result = new JSONObject();
		JSONObject json = new JSONObject();
		jsonArrays.add(json);
		String sql2 = "";
		if (flag.equals("1")) {
			sql2 = "select ROW_NUMBER() OVER(Order By custid) as rowid,m.custid,m.name,m.htstatus "
					+ "from (select distinct a.id as custid,a.name,(select top 1 status from uf_crm_contract where customer=a.id order by status,id) as htstatus "
					+ "from uf_crm_customerinfo a,uf_crm_custbusiness b,uf_crm_businessinfo c,Matrixtable_7 as d "
					+ "where a.id=b.customer and b.business=c.id and c.id=d.business  "
					+ sqlwhere
					+ ""
					+ "and ('"
					+ userid
					+ "' in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) "
					+ " or '"
					+ userid
					+ "' in (select F1 from vfn_Splitstr(d.businessleader,',')) "
					+ " or '"
					+ userid
					+ "' in (select F1 from vfn_Splitstr(d.businessmanager,',')) "
					+ " or ('"
					+ userid
					+ "' in (select F1 from vfn_Splitstr(d.regionleader,','))"
					+ " and b.regionalcenter is not null) "
					+ " or '"
					+ userid
					+ "' =b.accountmanager) ) m ";
			result.put("flag", "1");
		} else {
			sql2 = "select ROW_NUMBER() OVER(Order By c.dsporder) as rowid,a.id as custid,a.name,b.id,c.name as busname,b.business,b.linkid,b.linkdate,"
					+ "(select top 1 id from uf_crm_contract where customer=a.id "
					+ " order by status,id) as htid,"
					+ "(select top 1 status from uf_crm_contract where customer=a.id "
					+ " order by status,id) as htstatus,"
					+ "(select sum(contractmoney) as contractmoney from uf_crm_contract where customer=a.id "
					+ " and status='0') as contractmoney,"
					+ "b.invoicemoney,b.kpmoney,b.reallymoney,b.qxid,b.qxstatus,b.isaudit,b.isrequirement "
					+ "from uf_crm_customerinfo a,uf_crm_custbusiness b,uf_crm_businessinfo c,Matrixtable_7 as d "
					+ "where a.id=b.customer and b.business=c.id and c.id=d.business "
					+ sqlwhere
					+ " "
					+ "and  ("
					+ userid
					+ " in(select resourceid from hrmrolemembers where  roleid=161 or roleid=2) "
					+ "or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(d.businessleader,',')) "
					+ "or "
					+ userid
					+ " in (select F1 from vfn_Splitstr(d.businessmanager,',')) "
					+ "or ("
					+ userid
					+ " in (select F1 from vfn_Splitstr(d.regionleader,',')) and b.regionalcenter is not null) "
					+ "or " + userid + " =b.accountmanager) ";
		}
		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ (pageNo - 1) * pageSize + " and " + (pageNo) * pageSize + "";
		rs2.executeSql(sql2);
		while (rs2.next()) {
			JSONObject jsonobject = new JSONObject();
			String status = Util.null2String(rs2.getString("htstatus"));// 合同状态
			if (status.equals("0")) {
				status = "有效";
			} else if (status.equals("1")) {
				status = "终止";
			}
			jsonobject.put("xid", Util.null2String(rs2.getString("custid")));
			jsonobject.put("xname", Util.null2String(rs2.getString("name")));
			if (flag.equals("0")) {
				jsonobject.put("busname",
						Util.null2String(rs2.getString("busname")));
				jsonobject.put("business",
						Util.null2String(rs2.getString("business")));
			}
			jsonobject.put("status", status);
			jsonArray.add(jsonobject);

		}
		result.put("customer", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取指定客户业务详情信息
	 * 
	 * @param user
	 */
	private void Begininfo(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs2 = new RecordSet();
		RecordSet rs3 = new RecordSet();
		JSONArray jsonArray = new JSONArray();
		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		String sql2 = "";
		String sql3="";
		JSONObject result = new JSONObject();
		if (business.equals("undefined")) {
			sql2 = "select a.id as custid,a.name,b.id,c.name as busname,b.business,b.linkid,b.linkdate,a.corporatdelegate,a.registeredcapital,a.founddate,a.address,a.createdate,"
					+ "(case a.iflist when '1' then '否' when '0' then '是' END) as iflist,"
					+ "(case a.ifbond when '1' then '否' when '0' then '是' END) as ifbond,"
					+ "(select top 1 id from uf_crm_contract where customer=a.id order by status,id) as htid,"
					+ "(select top 1 status from uf_crm_contract where customer=a.id order by status,id) as htstatus,"
					+ "(select sum(contractmoney) as contractmoney from uf_crm_contract where customer=a.id and status='0') as contractmoney,"
					+ "b.invoicemoney,b.kpmoney,b.reallymoney,b.qxid,b.qxstatus,b.isaudit,b.isrequirement "
					+ "from uf_crm_customerinfo a,uf_crm_custbusiness b,uf_crm_businessinfo c,Matrixtable_7 as d "
					+ "where a.id=b.customer and b.business=c.id and c.id=d.business and a.id = '"
					+ custid + "'";
			sql3="select a.id as custid,a.name,b.id,c.name as busname,b.business" +
					" from uf_crm_customerinfo a,uf_crm_custbusiness b,uf_crm_businessinfo c,Matrixtable_7 as d " +
					" where a.id=b.customer and b.business=c.id and c.id=d.business and a.id = '"+custid+"' " +
					" and (b.business in(select business from Matrixtable_7 where " +
					"'"+userid+"' in (select F1 from vfn_Splitstr(businessleader,',')) union all select business from Matrixtable_7 where " +
					"'"+userid+"' in (select F1 from vfn_Splitstr(businessmanager,',')) union all select business from Matrixtable_7 where " +
					"'"+userid+"' in (select F1 from vfn_Splitstr(custommanager,','))) or  " +
					"'"+userid+"' in (select resourceid from hrmrolemembers where roleid=161 or roleid=2)) ";
			result.put("flag", "1");
		} else {
			sql2 = "select a.id as custid,a.name,b.id,c.name as busname,b.business,b.linkid,b.linkdate,a.corporatdelegate,a.registeredcapital,a.founddate,a.address,a.createdate,"
					+ "(case a.iflist when '1' then '否' when '0' then '是' END) as iflist,"
					+ "(case a.ifbond when '1' then '否' when '0' then '是' END) as ifbond,"
					+ "(select top 1 id from uf_crm_contract where customer=a.id and business='"
					+ business
					+ "' order by status,id) as htid,"
					+ "(select top 1 status from uf_crm_contract where customer=a.id and business='"
					+ business
					+ "' order by status,id) as htstatus,"
					+ "(select sum(contractmoney) as contractmoney from uf_crm_contract where customer=a.id and business='"
					+ business
					+ "' and status='0') as contractmoney,"
					+ "b.invoicemoney,b.kpmoney,b.reallymoney,b.qxid,b.qxstatus,b.isaudit,b.isrequirement "
					+ "from uf_crm_customerinfo a,uf_crm_custbusiness b,uf_crm_businessinfo c,Matrixtable_7 as d "
					+ "where a.id=b.customer and b.business=c.id and c.id=d.business and b.business='"
					+ business + "' and a.id = '" + custid + "' ";
		}

		rs2.executeSql(sql2);
		int Serialnumber = 0;
		String businessname = "";
		while (rs2.next()) {
			JSONObject jsonobject = new JSONObject();
			String status = Util.null2String(rs2.getString("htstatus"));// 合同状态
			if (status.equals("0")) {
				status = "有效";
			} else if (status.equals("1")) {
				status = "终止";
			}
			String bsname2=Util.null2String(rs2.getString("busname"));
			String str="0";
			rs3.executeSql(sql3);
			while(rs3.next()){
				String bsname3=Util.null2String(rs3.getString("busname"));
				if(bsname2.equals(bsname3)){
					
					if (Serialnumber == 0) {
						businessname += "<span style='color:gray;'>"+bsname2+"</span>";
					} else {
						businessname += ","
								+ "<span style='color:gray;'>"+bsname2+"</span>";
					}
					str="1";
					break;
				}
			}
			if(str.equals("0")){
				if (Serialnumber == 0) {
					businessname += Util.null2String(rs2.getString("busname"));
				} else {
					businessname += ","
							+ Util.null2String(rs2.getString("busname"));
				}
			}
			
			Serialnumber++;
			jsonobject.put("xid", Util.null2String(rs2.getString("custid")));
			jsonobject.put("xname", Util.null2String(rs2.getString("name"))); // 公司
			jsonobject.put("busname", businessname); // 业务
			jsonobject.put("iflist", Util.null2String(rs2.getString("iflist"))); // 上市
			jsonobject.put("ifbond", Util.null2String(rs2.getString("ifbond"))); // 证券
			jsonobject.put("corporatdelegate",
					Util.null2String(rs2.getString("corporatdelegate"))); // 法定代表人
			jsonobject.put("registeredcapital",
					Util.null2String(rs2.getString("registeredcapital"))); // 注册资本
			jsonobject.put("founddate",
					Util.null2String(rs2.getString("founddate"))); // 成立日期
			jsonobject.put("address",
					Util.null2String(rs2.getString("address"))); // 注册地址
			jsonobject.put("createdate",
					Util.null2String(rs2.getString("createdate"))); // 客户创建日期

			jsonobject.put("linkid", Util.null2String(rs2.getString("linkid")));// 联系记录id
			jsonobject.put("linkdate",
					Util.null2String(rs2.getString("linkdate")));// 联系记录
			jsonobject.put("htid", Util.null2String(rs2.getString("htid")));// 合同id
			jsonobject.put("status", status);
			jsonobject.put("contractmoney",
					Util.null2String(rs2.getString("contractmoney")));// 合同金额
			jsonobject.put("invoicemoney",
					Util.null2String(rs2.getString("invoicemoney")));// 应收金额
			jsonobject.put("reallymoney",
					Util.null2String(rs2.getString("reallymoney")));// 收款 账款金额
			jsonobject.put("kpmoney",
					Util.null2String(rs2.getString("kpmoney")));// 开票金额
			jsonobject.put("qxid", Util.null2String(rs2.getString("qxid")));// 权限id
			jsonobject.put("qxstatus",
					Util.null2String(rs2.getString("qxstatus")));// 权限
			jsonobject.put("audit", Util.null2String(rs2.getString("isaudit")));// 审计
			jsonobject.put("requirement",
					Util.null2String(rs2.getString("isrequirement")));// 需求
			jsonArray.add(jsonobject);

		}

		result.put("customer", jsonArray.toString());
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String numbercode1 = "";
	public static String numbercode2 = "";

	/**
	 * 获取联系人列表
	 * 
	 * @param user
	 */

	private void linkmanList(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs2 = new RecordSet();
		RecordSet rs3 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));
		String sql2 = "";
		if (business.equals("undefined")) {
			sql2 = "  select  (select count(*) from uf_crm_linkman a,uf_crm_businessinfo b where a.business=b.id and customer = '"
					+ custid
					+ "') as number,"
					+ "a.id,a.business,a.name,a.phone,a.businessrole,ROW_NUMBER() OVER(Order By a.business) as rowid,"
					+ "b.name as businessname from uf_crm_linkman a,uf_crm_businessinfo b where a.business=b.id and customer ='"
					+ custid + "'";
		} else {
			sql2 = " select *,ROW_NUMBER() OVER(Order By id) as rowid from uf_crm_linkman where customer = '"
					+ custid + "' and business = '" + business + "' ";
		}

		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		JSONObject jsonobject = new JSONObject();
		String flagname1 = "";
		String flagname2 = "";
		while (rs2.next()) {
			int number = rs2.getInt("number"); // 联系人总记录数
			if (number > 5) { // 是否需要分页
				numbercode1 = Util.null2String(rs2.getString("business"));
				if (numbercode1.equals(numbercode2)) {
					jsonobject.put("businessname", "");

				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));

				}
				numbercode2 = numbercode1;
			} else {
				flagname1 = Util.null2String(rs2.getString("business"));
				if (flagname1.equals(flagname2)) {
					jsonobject.put("businessname", "");

				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));

				}
				flagname2 = flagname1;
			}
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("name", Util.null2String(rs2.getString("name")));
			jsonobject.put("phone", Util.null2String(rs2.getString("phone")));
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("businessrole",
					Util.null2String(rs2.getString("businessrole")));
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
	 * 获取联系人列表
	 * 
	 * @param user
	 */

	private void linkmanList_Two(User user) {
		String userid = String.valueOf(user.getUID());

		String customer = Util.null2String(request.getParameter("customer"))
				.trim();// 搜索的字符串
		String person = Util.null2String(request.getParameter("person"))
				.trim();
		String mobile = Util.null2String(request.getParameter("mobile"))
				.trim();
		String ywid = Util.null2String(request.getParameter("searchOptionid"));// 搜索的字符串

		String sqlwhere = "";
		if ("".equals(customer)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and customer in (select id from uf_crm_customerinfo where oldname like '%"
					+ customer + "%' )";
		}
		if ("".equals(person)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and name like '%"
					+ person + "%' ";
		}
		if ("".equals(mobile)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and phone like '"
					+ mobile + "%' ";
		}
		if ("0".equals(ywid) || StringHelper.isEmpty(ywid)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and business='" + ywid + "' ";
		}

		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));

		String sql2 = " select ROW_NUMBER() OVER(ORDER BY id) as rowid	,(select name from uf_crm_customerinfo where id= a.customer) as 'customername' ,* from uf_crm_linkman a where 1=1 ";
		sql2 += sqlwhere;
		sql2 += " and (business in(select business from Matrixtable_7 where '"
				+ userid
				+ "' in (select F1 from vfn_Splitstr(businessleader,',')) union all select business from Matrixtable_7 where '"
				+ userid
				+ "' in (select F1 from vfn_Splitstr(businessmanager,',')) union all select business from Matrixtable_7 where '"
				+ userid
				+ "' in (select F1 from vfn_Splitstr(custommanager,','))) or  '"
				+ userid
				+ "' in (select resourceid from hrmrolemembers where roleid=161 or roleid=2)) ";
		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " ORDER BY id ";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		while (rs2.next()) {
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("customer",
					Util.null2String(rs2.getString("customer")));
			jsonobject.put("customername",
					Util.null2String(rs2.getString("customername")));
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("name", Util.null2String(rs2.getString("name")));
			jsonobject.put("phone", Util.null2String(rs2.getString("phone")));
			jsonobject.put("postname",
					Util.null2String(rs2.getString("postname")));
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

	public static String numberline1 = "";
	public static String numberline2 = "";

	/**
	 * 获取联系记录列表
	 * 
	 * @param user
	 */
	private void calllogList(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));
		String sql2 = "";
		if (business.equals("undefined")) {
			sql2 = " select  b.name as businessname,(select count(*) from uf_crm_calllog where customer='1724') as number"
					+ ",a.business,ROW_NUMBER() OVER(Order By business,linkdate desc) as rowid,a.id,linkdate,staff,linktitle"
					+ ",( select selectname from workflow_SelectItem where fieldid='12218' and selectvalue = linktype  ) as linktype_ "
					+ "from uf_crm_calllog a,uf_crm_businessinfo b where a.business=b.id and customer = '"
					+ custid + "'";
		} else {
			sql2 = " select ROW_NUMBER() OVER(Order By linkdate desc) as rowid,id,linkdate,staff,linktitle,"
					+ "( select selectname from workflow_SelectItem where fieldid='12218' and selectvalue = linktype  ) as linktype_ "
					+ "from uf_crm_calllog where customer = '"
					+ custid
					+ "' and business = '" + business + "' ";
		}
		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		JSONObject jsonobject = new JSONObject();
		String flagname1 = "";
		String flagname2 = "";
		while (rs2.next()) {
			int number = rs2.getInt("number"); // 联系记录总数
			if (number > 5) { // 是否需要分页
				numberline1 = Util.null2String(rs2.getString("business"));
				if (numberline1.equals(numberline2)) {
					jsonobject.put("businessname", "");
				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));
				}
				numberline2 = numberline1;
			} else {
				flagname1 = Util.null2String(rs2.getString("business"));
				if (flagname1.equals(flagname2)) {
					jsonobject.put("businessname", "");

				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));

				}
				flagname2 = flagname1;
			}
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("linkdate",
					Util.null2String(rs2.getString("linkdate")));
			String[] staffs = Util.null2String(rs2.getString("staff")).split(
					",");
			String staff = "";
			String sql3 = "";
			for (int i = 0; i < staffs.length; i++) {
				if (!StringHelper.isEmpty(sql3)) {
					sql3 += "  union all  ";
				}
				sql3 += "select lastname from hrmresource where id ="
						+ staffs[i];
			}
			rs1.execute(sql3);
			while (rs1.next()) {
				if (!StringHelper.isEmpty(staff)) {
					staff += ",";
				}
				staff += Util.null2String(rs1.getString("lastname"));
			}
			jsonobject.put("staff", staff);
			jsonobject.put("linktype",
					Util.null2String(rs2.getString("linktype_")));
			jsonobject.put("linkcontent",
					Util.null2String(rs2.getString("linktitle")));

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
	 * 获取联系记录列表
	 * 
	 * @param user
	 */

	private void calllogList_Two(User user) {
		String userid = String.valueOf(user.getUID());

		String customer = Util.null2String(request.getParameter("customer"))
				.trim();// 搜索的字符串
		String ywid = Util.null2String(request.getParameter("searchOptionid"));// 搜索的字符串
		String sqlwhere = "";
		if ("".equals(customer)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and customer in (select id from uf_crm_customerinfo where oldname like '%"
					+ customer + "%' )";
		}
		if ("0".equals(ywid) || StringHelper.isEmpty(ywid)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and business='" + ywid + "' ";
		}

		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));

		String sql2 = "select ROW_NUMBER() OVER(ORDER BY linkdate desc) as rowid,id,linkdate,staff,linktitle,business,customer,"
				+ "(select name from uf_crm_businessinfo where id=a.business) as businessname,"
				+ "(select name from uf_crm_customerinfo where id=a.customer) as customername "
				+ "from uf_crm_calllog a where 1=1 ";
		sql2 += sqlwhere;
		sql2 += " and (business in(select business from Matrixtable_7 where '"
				+ userid
				+ "' in (select F1 from vfn_Splitstr(businessleader,',')) union all select business from Matrixtable_7 where '"
				+ userid
				+ "' in (select F1 from vfn_Splitstr(businessmanager,',')) union all select business from Matrixtable_7 where '"
				+ userid
				+ "' in (select F1 from vfn_Splitstr(custommanager,','))) or  '"
				+ userid
				+ "' in (select resourceid from hrmrolemembers where roleid=161 or roleid=2)) ";
		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " ORDER BY linkdate desc ";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		while (rs2.next()) {
			JSONObject jsonobject = new JSONObject();

			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("customer",
					Util.null2String(rs2.getString("customer")));
			jsonobject.put("linkdate",
					Util.null2String(rs2.getString("linkdate")));
			jsonobject.put("businessname",
					Util.null2String(rs2.getString("businessname")));
			jsonobject.put("customername",
					Util.null2String(rs2.getString("customername")));
			String[] staffs = Util.null2String(rs2.getString("staff")).split(
					",");
			String staff = "";
			String sql3 = "";
			for (int i = 0; i < staffs.length; i++) {
				if (!StringHelper.isEmpty(sql3)) {
					sql3 += "  union all  ";
				}
				sql3 += "select lastname from hrmresource where id ="
						+ staffs[i];
			}
			rs1.execute(sql3);
			while (rs1.next()) {
				if (!StringHelper.isEmpty(staff)) {
					staff += ",";
				}
				staff += Util.null2String(rs1.getString("lastname"));
			}
			jsonobject.put("staff", staff);
			jsonobject.put("linkcontent",
					Util.null2String(rs2.getString("linktitle")));

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

	public static String numbercon1 = "";
	public static String numbercon2 = "";

	/**
	 * 获取合同列表
	 * 
	 * @param user
	 */
	private void contractList(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));
		String sql2 = "";
		if (business.equals("undefined")) {
			sql2 = " select  a.status,b.name as businessname,(select count(*) from  uf_crm_contract where customer = '"
					+ custid
					+ "') as number"
					+ ",ROW_NUMBER() OVER(ORDER BY business,status,startdate desc) as rowid"
					+ ",a.id,a.customer,a.business,a.contractno,a.contractname,a.contracttype,a.contractmoney "
					+ "from uf_crm_contract a ,uf_crm_businessinfo b where status='0' and a.business=b.id and customer = '"
					+ custid + "'";
		} else {
			sql2 = " select ROW_NUMBER() OVER(ORDER BY status,startdate desc) as rowid,* from uf_crm_contract where status='0' and  customer = '"
					+ custid + "' and business = '" + business + "' ";
		}

		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		JSONObject jsonobject = new JSONObject();
		String flagname1 = "";
		String flagname2 = "";
		while (rs2.next()) {

			int number = rs2.getInt("number"); // 联系人总记录数
			if (number > 5) { // 是否需要分页
				numbercon1 = Util.null2String(rs2.getString("business"));
				if (numbercon1.equals(numbercon2)) {
					jsonobject.put("businessname", "");

				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));

				}
				numbercon2 = numbercon1;
			} else {
				flagname1 = Util.null2String(rs2.getString("business"));
				if (flagname1.equals(flagname2)) {
					jsonobject.put("businessname", "");

				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));

				}
				flagname2 = flagname1;
			}
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("contractno",
					Util.null2String(rs2.getString("contractno")));
			jsonobject.put("contractname",
					Util.null2String(rs2.getString("contractname")));
			jsonobject.put("contractmoney",
					Util.null2String(rs2.getString("contractmoney")));
			String status = Util.null2String(rs2.getString("status"));// 合同状态
			if (status.equals("0")) {
				status = "有效";
			} else if (status.equals("1")) {
				status = "终止";
			}
			jsonobject.put("status", status);
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
	 * 获取合同列表
	 * 
	 * @param user
	 */

	private void contractList_Two(User user) {
		String userid = String.valueOf(user.getUID());

		String customer = Util.null2String(request.getParameter("customer"))
				.trim();// 搜索的字符串
		String contractname = Util.null2String(
				request.getParameter("contractname")).trim();// 搜索的字符串
		String ywid = Util.null2String(request.getParameter("searchOptionid"));// 搜索的字符串

		String sqlwhere = "";
		if ("".equals(customer)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and customer in (select id from uf_crm_customerinfo where oldname like '%"
					+ customer + "%' )";
		}
		if ("0".equals(ywid) || StringHelper.isEmpty(ywid)) {
			sqlwhere += "";
		} else {
			sqlwhere += " and business='" + ywid + "' ";
		}

		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));

		String sql2 = " select ROW_NUMBER() OVER(ORDER BY status,startdate desc ) as rowid,(select name from uf_crm_customerinfo where id=a.customer) as customername,* from uf_crm_contract a where 1=1";
		sql2 += sqlwhere;
		sql2 += " and (business in(select business from Matrixtable_7 where '"
				+ userid
				+ "' in (select F1 from vfn_Splitstr(businessleader,',')) union all select business from Matrixtable_7 where '"
				+ userid
				+ "' in (select F1 from vfn_Splitstr(businessmanager,',')))"
				+ " or  '"
				+ userid
				+ "' in (select resourceid from hrmrolemembers where roleid=161 or roleid=2)) ";
		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " ORDER BY status,startdate desc ";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		while (rs2.next()) {
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("customer",
					Util.null2String(rs2.getString("customer")));
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("contractno",
					Util.null2String(rs2.getString("contractno")));
			jsonobject.put("contractname",
					Util.null2String(rs2.getString("contractname")));
			jsonobject.put("customername",
					Util.null2String(rs2.getString("customername")));
			String status = Util.null2String(rs2.getString("status"));// 合同状态
			if (status.equals("0")) {
				status = "有效";
			} else if (status.equals("1")) {
				status = "终止";
			}
			jsonobject.put("status", status);
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

	public static String numberbus1 = "";
	public static String numberbus2 = "";

	/**
	 * 获取权限内容列表
	 * 
	 * @param user
	 */
	private void businessMoneyList(User user) {
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();
		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));
		String sql2 = "";
		if (business.equals("undefined")) {
			sql2 = "select ROW_NUMBER() OVER(Order By business) as rowid,"
					+ "(select count(*) from uf_crm_orderinfo where customer='"
					+ custid
					+ "') as number,"
					+ "(select name from uf_crm_businessinfo where id=a.business) as businessname,* from uf_crm_orderinfo a where customer='"
					+ custid + "'";
		} else {
			sql2 = "select ROW_NUMBER() OVER(Order By business) as rowid,"
					+ "(select count(*) from uf_crm_orderinfo where customer='"
					+ custid
					+ "' and business='"
					+ business
					+ "') as number,"
					+ "(select name from uf_crm_businessinfo where id=a.business) as businessname,* from uf_crm_orderinfo a where customer='"
					+ custid + "' and business='" + business + "'";
		}
		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		JSONObject jsonobject = new JSONObject();
		String flagname1 = "";
		String flagname2 = "";
		while (rs2.next()) {
			int number = rs2.getInt("number"); // 总记录数
			if (number > 5) { // 是否需要分页
				numberbus1 = Util.null2String(rs2.getString("business"));
				if (numberbus1.equals(numberbus2)) {
					jsonobject.put("businessname", "");
				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));
				}
				numberbus2 = numberbus1;
			} else {
				flagname1 = Util.null2String(rs2.getString("business"));
				if (flagname1.equals(flagname2)) {
					jsonobject.put("businessname", "");
				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));
				}
				flagname2 = flagname1;
			}
            String status= Util.null2String(rs2.getString("status"));
            if(status.equals("0")){
            	status="正常";
            }else{
            	status="取消";
            }
			jsonobject.put("ordernumber",
					Util.null2String(rs2.getString("ordernumber")));
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("status",status);
			jsonobject.put("product",
					Util.null2String(rs2.getString("product")));
			jsonobject.put("ordermoney",
					Util.null2String(rs2.getString("ordermoney")));
			jsonobject.put("invoicemoney",
					Util.null2String(rs2.getString("invoicemoney")));
			jsonobject.put("proceedsmoney",
					Util.null2String(rs2.getString("proceedsmoney")));
			jsonobject.put("orderdate",
					Util.null2String(rs2.getString("orderdate")));
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

	public static String numberper1 = "";
	public static String numberper2 = "";

	/**
	 * 获取业金额列表
	 * 
	 * @param user
	 */
	private void permissionList(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));
		String sql2 = "";
		if (business.equals("undefined")) {
			sql2 = "  select (select count(*) from uf_crm_permission where  customer = '"
					+ custid
					+ "') as number"
					+ ",b.name as businessname,ROW_NUMBER() OVER(ORDER BY business,a.id) as rowid,a.id,a.customer,a.business,a.startdate,a.enddate,a.status,a.title "
					+ "from uf_crm_permission a,uf_crm_businessinfo b where a.business=b.id and customer = '"
					+ custid + "'";
		} else {
			sql2 = " select ROW_NUMBER() OVER(ORDER BY id) as rowid,* from uf_crm_permission where customer = '"
					+ custid + "' and business = '" + business + "' ";
		}

		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		JSONObject jsonobject = new JSONObject();
		String flagname1 = "";
		String flagname2 = "";
		while (rs2.next()) {
			int number = rs2.getInt("number"); // 权限总记录数
			if (number > 5) { // 是否需要分页
				numberper1 = Util.null2String(rs2.getString("business"));
				if (numberper1.equals(numberper2)) {
					jsonobject.put("businessname", "");
				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));
				}
				numberper2 = numberper1;
			} else {
				flagname1 = Util.null2String(rs2.getString("business"));
				if (flagname1.equals(flagname2)) {
					jsonobject.put("businessname", "");
				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));
				}
				flagname2 = flagname1;
			}
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("title", Util.null2String(rs2.getString("title")));
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
	 * 获取客户列表
	 * 
	 * @param user
	 */

	private void customerList(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));
		String tempStr = Util.null2String(request.getParameter("sqlwhere"));

		String sql2 = " select ROW_NUMBER() OVER(ORDER BY id) as rowid	,* from uf_crm_customerinfo where 1=1 ";
		sql2 += tempStr;
		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " ORDER BY id ";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		while (rs2.next()) {
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("name", Util.null2String(rs2.getString("name")));
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
	 * 获取业务列表
	 * 
	 * @param user
	 */

	private void businessList(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));
		String tempStr = Util.null2String(request.getParameter("sqlwhere"));

		String sql2 = " select ROW_NUMBER() OVER(ORDER BY id) as rowid,* from uf_crm_businessinfo where 1=1 ";
		sql2 += tempStr;
		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " ORDER BY id ";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		while (rs2.next()) {
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("name", Util.null2String(rs2.getString("name")));
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
	 * 获取联系人详细
	 * 
	 * @param user
	 */

	private void linkmanInfo(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		String pid = Util.null2String(request.getParameter("pid"));

		String sql2 = " select a.*,b.name as bname,c.name as cname from uf_crm_linkman a "
				+ "LEFT JOIN uf_crm_customerinfo b ON b.id = a.customer "
				+ "LEFT JOIN uf_crm_businessinfo c ON c.id = a.business "
				+ "where "
				+ "a.customer = '"
				+ custid
				+ "' and a.business = '"
				+ business + "' and a.id = '" + pid + "'";
		rs2.executeSql(sql2);
		while (rs2.next()) {
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("custid",
					Util.null2String(rs2.getString("customer")));
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("name", Util.null2String(rs2.getString("name")));
			jsonobject.put("bname", Util.null2String(rs2.getString("bname")));
			jsonobject.put("cname", Util.null2String(rs2.getString("cname")));
			jsonobject.put("orgname",
					Util.null2String(rs2.getString("orgname")));
			jsonobject.put("postname",
					Util.null2String(rs2.getString("postname")));
			jsonobject.put("address",
					Util.null2String(rs2.getString("address")));
			jsonobject.put("zipcode",
					Util.null2String(rs2.getString("zipcode")));
			jsonobject.put("tel", Util.null2String(rs2.getString("tel")));
			jsonobject.put("phone", Util.null2String(rs2.getString("phone")));
			jsonobject.put("email", Util.null2String(rs2.getString("email")));
			jsonobject.put("fax", Util.null2String(rs2.getString("fax")));
			jsonobject.put("businessrole",
					Util.null2String(rs2.getString("businessrole")));
			jsonobject.put("remark", Util.null2String(rs2.getString("remark")));
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
     * 删除联系人
     * @param user
     */
	public void removelinkmanInfo(User user){
		RecordSet rs = new RecordSet();
		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		String pid = Util.null2String(request.getParameter("pid"));
		String sql="delete from uf_crm_linkman where customer = '"+custid+"' and business = '"+business+"' and id = '"+pid+"' ";
		boolean re=rs.executeSql(sql);
		JSONObject result = new JSONObject();
		if(re){
			result.put("result", "1");
		}else{
			result.put("result", "0");
		}
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 修改/新增联系人详细
	 * 
	 * @param user
	 */

	private void insertOrUpdateLinkman(User user) {
		try {
			String userid = String.valueOf(user.getUID());
			RecordSet rs2 = new RecordSet();

			FileUpload fileUpload = new MobileFileUpload(request, "UTF-8",
					false);
			String pid = Util.null2String(fileUpload.getParameter("pid"));
			String custid = Util.null2String(fileUpload.getParameter("custid"));
			String business = Util.null2String(fileUpload
					.getParameter("business"));
			String name = Util.null2String(fileUpload.getParameter("name"))
					.trim();
			String bname = Util.null2String(fileUpload.getParameter("bname"))
					.trim();
			String cname = Util.null2String(fileUpload.getParameter("cname"))
					.trim();
			String orgname = Util.null2String(
					fileUpload.getParameter("orgname")).trim();
			String postname = Util.null2String(
					fileUpload.getParameter("postname")).trim();
			String address = Util.null2String(
					fileUpload.getParameter("address")).trim();
			String zipcode = Util.null2String(
					fileUpload.getParameter("zipcode")).trim();
			String tel = Util.null2String(fileUpload.getParameter("tel"))
					.trim();
			String phone = Util.null2String(fileUpload.getParameter("phone"))
					.trim();
			String email = Util.null2String(fileUpload.getParameter("email"))
					.trim();
			String fax = Util.null2String(fileUpload.getParameter("fax"))
					.trim();
			String businessrole = Util.null2String(
					fileUpload.getParameter("businessrole")).trim();
			String remark = Util.null2String(fileUpload.getParameter("remark"))
					.trim();

			String Sql = "";
			String msg = "";
			if (StringHelper.isEmpty(custid)) {
				msg = "客户名称不能为空";
			} else if (StringHelper.isEmpty(business)) {
				msg = "所属业务不能为空";
			} else if (StringHelper.isEmpty(name)) {
				msg = "姓名不能为空";
			} else if (!StringHelper.isEmpty(email)) {
				if (!email.matches("^[\\w-]+@[\\w-]+(\\.[\\w-]+)+$")) {
					msg = "邮箱格式不正确";
				}
			} else if (StringHelper.isEmpty(phone)) {
				msg = "移动电话不能为空";
			} else if (!StringHelper.isEmpty(phone)) {
				if (!phone
						.matches("^(\\+86|86)?1((3[0-9])|(4[5|7])|(5([0-3]|[5-9]))|(8[0,5-9]))\\d{8}$")) {
					msg = "移动电话格式不正确";
				}
			}
			if (StringHelper.isEmpty(msg)) {
				if (!StringHelper.isEmpty(pid)) {
					Sql = "update uf_crm_linkman set name = '" + name
							+ "',orgname = '" + orgname + "',postname = '"
							+ postname + "',address= '" + address + "',"
							+ "zipcode = '" + zipcode + "',tel = '" + tel
							+ "',phone = '" + phone + "',email = '" + email
							+ "',fax = '" + fax + "'," + "businessrole = '"
							+ businessrole + "',remark = '" + remark
							+ "' where id = '" + pid + "'";
				} else {
					Sql = "insert into uf_crm_linkman (customer,business,name,orgname,postname,address,zipcode,tel,phone,email,fax,businessrole,remark) values "
							+ "('"
							+ custid
							+ "','"
							+ business
							+ "','"
							+ name
							+ "','"
							+ orgname
							+ "','"
							+ postname
							+ "','"
							+ address
							+ "','"
							+ zipcode
							+ "','"
							+ tel
							+ "',"
							+ "'"
							+ phone
							+ "','"
							+ email
							+ "','"
							+ fax
							+ "','"
							+ businessrole + "','" + remark + "')";
				}
				rs2.execute(Sql);
			}

			response.setContentType("application/text; charset=utf-8");
			response.getWriter().print(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取联系人详细
	 * 
	 * @param user
	 */

	private void calllogInfo(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		String pid = Util.null2String(request.getParameter("pid"));

		String sql2 = " select a.*,b.name as bname,c.name as cname,( select selectname from workflow_SelectItem where fieldid='12218' and selectvalue = a.linktype  ) as linktype_ from uf_crm_calllog a "
				+ "LEFT JOIN uf_crm_customerinfo b ON b.id = a.customer "
				+ "LEFT JOIN uf_crm_businessinfo c ON c.id = a.business "
				+ "where "
				+ "a.customer = '"
				+ custid
				+ "' and a.business = '"
				+ business + "' and a.id = '" + pid + "'";
		rs2.executeSql(sql2);
		while (rs2.next()) {
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("custid",
					Util.null2String(rs2.getString("customer")));
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("bname", Util.null2String(rs2.getString("bname")));
			jsonobject.put("cname", Util.null2String(rs2.getString("cname")));

			String[] staffs = Util.null2String(rs2.getString("staff")).split(
					",");
			String staff = "";
			String sql3 = "";
			for (int i = 0; i < staffs.length; i++) {
				if (!StringHelper.isEmpty(sql3)) {
					sql3 += "  union all  ";
				}
				sql3 += "select lastname from hrmresource where id ="
						+ staffs[i];
			}
			rs1.execute(sql3);
			while (rs1.next()) {
				if (!StringHelper.isEmpty(staff)) {
					staff += ",";
				}
				staff += Util.null2String(rs1.getString("lastname"));
			}
			jsonobject.put("staff", staff);

			jsonobject.put("linkman",
					Util.null2String(rs2.getString("linkman")));
			jsonobject.put("participants",
					Util.null2String(rs2.getString("participants")));
			jsonobject.put("linkdate",
					Util.null2String(rs2.getString("linkdate")));
			jsonobject.put("linktype",
					Util.null2String(rs2.getString("linktype_")));
			jsonobject.put("address",
					Util.null2String(rs2.getString("address")));
			jsonobject.put("linktitle",
					Util.null2String(rs2.getString("linktitle")));
			jsonobject.put("linkcontent",
					Util.null2String(rs2.getString("linkcontent")));
			jsonobject.put("analysis",
					Util.null2String(rs2.getString("analysis")));
			jsonobject.put("nextplan",
					Util.null2String(rs2.getString("nextplan")));
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
	 * 获取合同详细
	 * 
	 * @param user
	 */

	private void contractInfo(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		String pid = Util.null2String(request.getParameter("pid"));

		String sql2 = " select a.*,b.name as bname,c.name as cname,"
				+ "( select selectname from workflow_SelectItem where fieldid='11992' and selectvalue = a.contractrelation  ) as contractrelation_, "
				+ "( select selectname from workflow_SelectItem where fieldid='12040' and selectvalue = a.extendtype  ) as extendtype_, "
				+ "( select selectname from workflow_SelectItem where fieldid='12041' and selectvalue = a.extendcycle  ) as extendcycle_ "
				+ "from uf_crm_contract a "
				+ "LEFT JOIN uf_crm_customerinfo b ON b.id = a.customer "
				+ "LEFT JOIN uf_crm_businessinfo c ON c.id = a.business "
				+ "where " + "a.customer = '" + custid + "' and a.business = '"
				+ business + "' and a.id = '" + pid + "'";
		rs2.executeSql(sql2);
		while (rs2.next()) {
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("custid",
					Util.null2String(rs2.getString("customer")));
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("bname", Util.null2String(rs2.getString("bname")));
			jsonobject.put("cname", Util.null2String(rs2.getString("cname")));

			jsonobject.put("contractname",
					Util.null2String(rs2.getString("contractname")));
			jsonobject.put("contractno",
					Util.null2String(rs2.getString("contractno")));
			jsonobject.put("contractrelation",
					Util.null2String(rs2.getString("contractrelation_")));
			jsonobject.put("extendtype",
					Util.null2String(rs2.getString("extendtype_")));
			jsonobject.put("extendcycle",
					Util.null2String(rs2.getString("extendcycle_")));
			jsonobject.put("contractmoney",
					Util.null2String(rs2.getString("contractmoney")));
			jsonobject.put("startdate",
					Util.null2String(rs2.getString("startdate")));
			jsonobject.put("enddate",
					Util.null2String(rs2.getString("enddate")));
			jsonobject.put("signdate",
					Util.null2String(rs2.getString("signdate")));
			String status = Util.null2String(rs2.getString("status"));// 合同状态
			if (status.equals("0")) {
				status = "有效";
			} else if (status.equals("1")) {
				status = "终止";
			}
			jsonobject.put("status", status);
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
	 * 获取历史数据合同
	 * 
	 * @param user
	 */
	public void historycontractList(User user) {
		String userid = String.valueOf(user.getUID());
		RecordSet rs2 = new RecordSet();
		JSONArray jsonArray = new JSONArray();

		String business = Util.null2String(request.getParameter("business"));
		String custid = Util.null2String(request.getParameter("custid"));
		int pageNo = Integer.parseInt(Util.null2String(request
				.getParameter("pageno")));
		int pageSize = Integer.parseInt(Util.null2String(request
				.getParameter("pagesize")));
		String sql2 = "";
		if (business.equals("undefined")) {
			sql2 = " select  a.status,b.name as businessname,(select count(*) from  uf_crm_contract where customer = '"
					+ custid
					+ "') as number"
					+ ",ROW_NUMBER() OVER(ORDER BY business,status,startdate desc) as rowid"
					+ ",a.id,a.customer,a.business,a.contractno,a.contractname,a.contracttype,a.contractmoney "
					+ "from uf_crm_contract a ,uf_crm_businessinfo b where status='1' and a.business=b.id and customer = '"
					+ custid + "'";
		} else {
			sql2 = " select ROW_NUMBER() OVER(ORDER BY status,startdate desc) as rowid,* from uf_crm_contract where status='1' and customer = '"
					+ custid + "' and business = '" + business + "' ";
		}

		sql2 = " select * from (" + sql2 + ") t where  rowid between "
				+ ((pageNo - 1) * pageSize + 1) + " and " + (pageNo) * pageSize
				+ "";
		// sql2 += " offset " + (pageNo -1)*pageSize + " rows fetch next " +
		// pageSize + " rows only ";
		rs2.executeSql(sql2);
		JSONObject jsonobject = new JSONObject();
		String flagname1 = "";
		String flagname2 = "";
		while (rs2.next()) {

			int number = rs2.getInt("number"); // 联系人总记录数
			if (number > 5) { // 是否需要分页
				numbercon1 = Util.null2String(rs2.getString("business"));
				if (numbercon1.equals(numbercon2)) {
					jsonobject.put("businessname", "");

				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));

				}
				numbercon2 = numbercon1;
			} else {
				flagname1 = Util.null2String(rs2.getString("business"));
				if (flagname1.equals(flagname2)) {
					jsonobject.put("businessname", "");

				} else {
					jsonobject.put("businessname",
							Util.null2String(rs2.getString("businessname")));

				}
				flagname2 = flagname1;
			}
			jsonobject.put("business",
					Util.null2String(rs2.getString("business")));
			jsonobject.put("id", Util.null2String(rs2.getString("id")));
			jsonobject.put("contractno",
					Util.null2String(rs2.getString("contractno")));
			jsonobject.put("contractname",
					Util.null2String(rs2.getString("contractname")));
			jsonobject.put("contractmoney",
					Util.null2String(rs2.getString("contractmoney")));
			String status = Util.null2String(rs2.getString("status"));// 合同状态
			if (status.equals("0")) {
				status = "有效";
			} else if (status.equals("1")) {
				status = "终止";
			}
			jsonobject.put("status", status);
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
}
