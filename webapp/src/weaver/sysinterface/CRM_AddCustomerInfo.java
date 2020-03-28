package weaver.sysinterface;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.Util;
import weaver.hrm.User;

import com.weaver.formmodel.mobile.manager.MobileUserInit;
import com.weaver.formmodel.util.StringHelper;

/**
 * 检索客户信息表,新增客户业务信息
 * 
 * @author lsq
 * @date 2019/7/11
 * @version 1.0
 */
public class CRM_AddCustomerInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private HttpServletRequest request;
	private HttpServletResponse response;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.request = request;
		this.response = response;
		try {
			String action = StringHelper.null2String(request
					.getParameter("action"));
			User user = MobileUserInit.getUser(request, response);
			if (user == null) {
				JSONObject checkUser = new JSONObject();
				checkUser.put("msgstatus", "0");
				checkUser.put("msg", "服务器端重置了登录信息，请重新登录");
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(checkUser.toString());
				return;
			} else if ("add".equals(action)) {
				add();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void add() {
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		RecordSet rs3 = new RecordSet();
		RecordSet rs4 = new RecordSet();
		RecordSet rs5 = new RecordSet();
		RecordSet rs6 = new RecordSet();
		RecordSet rs7 = new RecordSet();
		RecordSet rs8 = new RecordSet();
		RecordSet rs9 = new RecordSet();
		RecordSet rs10 = new RecordSet();
		String sql = " select id,iflist,listaddress,ifbond,ifmarket from uf_crm_customerinfo where (iflist='0' and listaddress='0')or ifbond='0' or ifmarket='0'";
		rs.executeSql(sql);
		while (rs.next()) {
			String id = Util.null2String(rs.getString("id")); // 客户id
			String iflist = Util.null2String(rs.getString("iflist")); // 是否上市
			String listaddress = Util.null2String(rs.getString("listaddress")); // 上市地点是否为上海证券交易所
			String ifbond = Util.null2String(rs.getString("ifbond")); // 是否为证券公司
			String ifmarket = Util.null2String(rs.getString("ifmarket")); // 是否为科创板
			int modeId = 127;
			if (iflist.equals("0") && listaddress.equals("0")
					&& ifmarket.equals("1")) { // 是上市公司 ,地点为上海证券交易所
				String sql1 = "SELECT  business,businessmanager FROM  Matrixtable_7 where business in( select id from  uf_crm_businessinfo where name in('网投统计服务','e服务专业版','配股缴款统计服务'))";
				rs1.executeSql(sql1);
				while (rs1.next()) {
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					String business = Util.null2String(rs1
							.getString("business"));// 业务
					String businessmanager = Util.null2String(rs1
							.getString("businessmanager"));// 业务经理
					String sql10 = "select count(*) as flag from uf_crm_custbusiness where customer='"
							+ id + "' and business='" + business + "'";
					rs10.executeSql(sql10);
					String flag = "";
					if (rs10.next()) {
						flag = Util.null2String(rs10.getString("flag"));
					}
					if (flag.equals("0")) { // 0 则表示客户业务信息表中不存在 该条数据
						String sql2 = "insert into uf_crm_custbusiness(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+ "customer,business,businessmanager,uuid) values ("
								+ modeId
								+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
								+ id
								+ "','"
								+ business
								+ "','"
								+ businessmanager + "','" + wyid + "')";
						rs2.executeSql(sql2);
						String sql5 = "select id from uf_crm_custbusiness where uuid='"
								+ wyid + "'";
						rs5.executeSql(sql5);
						if (rs5.next()) {
							String ids = Util.null2String(rs5.getString("id"));
							int logid = Integer.parseInt(ids);
							ModeRightInfo ModeRightInfo = new ModeRightInfo();
							ModeRightInfo.editModeDataShare(5, modeId, logid);
						}
					}
				}
			}
			if (ifbond.equals("0")) {// 是证券公司
				String sql3 = "SELECT  business,businessmanager FROM  Matrixtable_7 where business in( select id from  uf_crm_businessinfo where name in('云平台','会员信息服务平台','融资融券投票代征集'))";
				rs3.executeSql(sql3);
				while (rs3.next()) {
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					String business = Util.null2String(rs3
							.getString("business"));// 业务
					String businessmanager = Util.null2String(rs3
							.getString("businessmanager"));// 业务经理
					String sql10 = "select count(*) as flag from uf_crm_custbusiness where customer='"
							+ id + "' and business='" + business + "'";
					rs10.executeSql(sql10);
					String flag = "";
					if (rs10.next()) {
						flag = Util.null2String(rs10.getString("flag"));
					}
					if (flag.equals("0")) { // 0 则表示客户业务信息表中不存在 该条数据
						String sql4 = "insert into uf_crm_custbusiness(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+ "customer,business,businessmanager,uuid) values ("
								+ modeId
								+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
								+ id
								+ "','"
								+ business
								+ "','"
								+ businessmanager + "','" + wyid + "')";
						rs4.executeSql(sql4);
						String sql6 = "select id from uf_crm_custbusiness where uuid='"
								+ wyid + "'";
						rs6.executeSql(sql6);
						if (rs6.next()) {
							String ids = Util.null2String(rs6.getString("id"));
							int logid = Integer.parseInt(ids);
							ModeRightInfo ModeRightInfo = new ModeRightInfo();
							ModeRightInfo.editModeDataShare(5, modeId, logid);
						}
					}
				}
			}
			if (ifmarket.equals("0")) {// 是科创板公司
				String sql7 = "SELECT  business,businessmanager FROM  Matrixtable_7 where business in( select id from  uf_crm_businessinfo where name ='e服务科创专板')";
				rs7.executeSql(sql7);
				if (rs7.next()) {
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码
					String business = Util.null2String(rs7
							.getString("business"));// 业务
					String businessmanager = Util.null2String(rs7
							.getString("businessmanager"));// 业务经理
					String sql10 = "select count(*) as flag from uf_crm_custbusiness where customer='"
							+ id + "' and business='" + business + "'";
					rs10.executeSql(sql10);
					String flag = "";
					if (rs10.next()) {
						flag = Util.null2String(rs10.getString("flag"));
					}
					if (flag.equals("0")) { // 0 则表示客户业务信息表中不存在 该条数据
						String sql8 = "insert into uf_crm_custbusiness(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
								+ "customer,business,businessmanager,uuid) values ("
								+ modeId
								+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
								+ id
								+ "','"
								+ business
								+ "','"
								+ businessmanager + "','" + wyid + "')";
						rs8.executeSql(sql8);
						String sql9 = "select id from uf_crm_custbusiness where uuid='"
								+ wyid + "'";
						rs9.executeSql(sql9);
						if (rs9.next()) {
							String ids = Util.null2String(rs9.getString("id"));
							int logid = Integer.parseInt(ids);
							ModeRightInfo ModeRightInfo = new ModeRightInfo();
							ModeRightInfo.editModeDataShare(5, modeId, logid);
						}
					}
				}
			}

		}
		try {
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().print("1");
		} catch (Exception e) {

		}

	}
}
