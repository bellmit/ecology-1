package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;
import java.text.SimpleDateFormat;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CRM_ContractPlanToinvoice extends BaseBean implements Action {
	public String execute(RequestInfo request) {
		try {

			writeLog("创建开票流程--开始");
			RecordSet rs = new RecordSet();
			String requestid = request.getRequestid();
			// sql=" select top 1 b.*,a.*,(select name from uf_crm_customerinfo where id=a.customer) as customerName from uf_crm_contractplan a,uf_crm_contractplan_dt1 b where a.id = b.mainid and a.requestId='"+requestid+"' ";
			String sql = "select *,(select name from uf_crm_customerinfo where id=customer) as customerName from uf_crm_contractplan where requestid='"
					+ requestid + "'";
			rs.executeSql(sql);
			if (rs.next()) {
				String customer = Util.null2String(rs.getString("customer"));// 客户名称id
				String customerName = Util.null2String(rs
						.getString("customerName"));// 客户名称
				String business = Util.null2String(rs.getString("business"));// 所属业务
				String contract = Util.null2String(rs.getString("contract"));// 合同
				String creator = Util.null2String(rs.getString("creator"));// 申请人
				String dept = Util.null2String(rs.getString("dept"));// 申请部门
				String zb_id = Util.null2String(rs.getString("id"));// 流程主表ID

				sql = " select * from v_crm_customerinfo_kp where mainid='"
						+ customer + "'";
				String unitid = "";// 单位id
				String unitname = ""; // 单位名称
				String credit = ""; // 纳税登记号
				String address = ""; // 单位地址电话
				String accountname = ""; // 开户银行名称账号
				RecordSet resd = new RecordSet();
				resd.executeSql(sql);
				if (resd.next()) {
					unitid = Util.null2String(resd.getString("id"));
					unitname = Util.null2String(resd.getString("unitname"));
					credit = Util.null2String(resd.getString("creditcode"));
					address = Util.null2String(resd.getString("address"));
					accountname = Util.null2String(resd
							.getString("accountname"));
					writeLog("unitname----" + unitname);
				}
				String creditcode=credit.replace(" ", "");
				List<String> money = new ArrayList<String>();
				List<String> currentmoney = new ArrayList<String>();
				int count = 0; // 明细表记录数
				// 查询收款计划明细表
				RecordSet rsd = new RecordSet();
				sql = "select count(1) as 'count' from uf_crm_contractplan_dt1 where mainid='"
						+ zb_id + "'";
				rsd.executeSql(sql);
				if (rsd.next()) {
					count = rsd.getInt("count");
					writeLog("收款计划明细表count------" + count);
				}
				sql = "select * from uf_crm_contractplan_dt1 where mainid='"
						+ zb_id + "'";
				rsd.executeSql(sql);
				while (rsd.next()) {
					money.add(Util.null2String(rsd.getString("money")));// 流程明细金额
					currentmoney.add(Util.null2String(rsd
							.getString("currentmoney"))); // 流程明细分摊金额
				}
				try {
					int requestids = createFormContract(requestid,
							customerName, creator, dept, customer, business,
							contract, money, currentmoney, count,
							unitname, creditcode, address, accountname, unitid);
					if (requestids > 0) {
						writeLog("创建开票流程触发工作流成功，流程requestid:" + requestids);
					}
				} catch (Exception e) {
					writeLog("创建开票流程触发出错：" + e);
				}
				writeLog("创建开票流程--结束");
			}
		} catch (Exception e) {
			writeLog("创建开票流程--出错" + e);
			return "0";
		}
		return "1";
	}

	/**
	 * 创建开票申请流程
	 */
	public int createFormContract(String reqid, String titleDetail,
			String creator, String dept, String customer, String business,
			String contract, List<String> money,
			List<String> currentmoney, int count, String unitname,
			String creditcode, String address, String accountname, String unitid) {
		String newrequestid = "";
		try {
			String workflowid = "446"; //
			String lcbt = "开票申请:" + titleDetail;// 流程标题
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			writeLog("创建开票申请触发start：");
			requestInfo.setWorkflowid(workflowid);// 流程类型id
			requestInfo.setCreatorid(creator);// 创建人
			requestInfo.setDescription(lcbt);// 设置流程标题
			requestInfo.setRequestlevel("0");// 0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("0");// 保存在发起节点
			SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd");
			String createDate = dateFormat_now.format(new Date());
			RecordSet rs = new RecordSet();

			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;

			field = new Property();
			field.setName("createdate");
			field.setValue(createDate);
			fields.add(field);

			field = new Property();
			field.setName("creator");
			field.setValue(creator);
			fields.add(field);

			field = new Property();
			field.setName("dept");
			field.setValue(dept);
			fields.add(field);
			
			field = new Property();
			field.setName("business");
			field.setValue(business);
			fields.add(field);

			fields.add(field);
			Property[] fieldarray = (Property[]) fields
					.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);

			String sqldet = "select id from uf_crm_invoice where requestid='"
					+ newrequestid + "'";
			String mainid = "";
			rs.executeSql(sqldet);
			if (rs.next()) {
				mainid = Util.null2String(rs.getString("id"));
			}
			// 往明细表中写数据
			for (int i = 0; i < count; i++) {
				String sql = "insert into uf_crm_invoice_dt1(mainid,customer,invoicetype,contract,invoicemoney,currentmoney,unitid,creditcode,address,accountname,unitname) values('"
						+ mainid
						+ "','"
						+ customer
						+ "','0','"
						+ contract
						+ "','"
						+ money.get(i)
						+ "','"
						+ currentmoney.get(i)
						+ "','"
						+ unitid
						+ "','"
						+ creditcode
						+ "','"
						+ address
						+ "','" + accountname + "','" + unitname + "')";
				rs.executeSql(sql);
				writeLog("insert into-----"+sql);
			}

			writeLog("开票申请触发end：");
		} catch (Exception e) {
			writeLog("开票申请触发出错：" + e);
		}
		return Util.getIntValue(newrequestid, 0);
	}

}