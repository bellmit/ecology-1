package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;

public class CRM_ServiceFee_front2OA extends BaseCronJob {
	BaseBean lg = new BaseBean();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(this.todaycal.get(1), 4) + "-"
			+ Util.add0(this.todaycal.get(2) + 1, 2) + "-"
			+ Util.add0(this.todaycal.get(5), 2);

	public void execute() {
		try {
			create();
		} catch (Exception localException) {
		}
	}

	public void create() {
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_ENTERPRISE_INFO where STATUS=0 and PRODUCT_NAME like '%产品订购订单%' ";
		rds.executeSql(sql);
		while (rds.next()) {
			String remarks=Util.null2String(rds.getString("remarks"));
			String syid = Util.null2String(rds.getString("ID"));

			String companyName = Util
					.null2String(rds.getString("COMPANY_NAME"));

			String productName = Util
					.null2String(rds.getString("PRODUCT_NAME"));

			String ordeAmount = Util.null2String(rds.getString("ORDER_AMOUNT"));

			String linkMan = Util.null2String(rds.getString("LINKMAN"));

			String linkManTel = Util.null2String(rds.getString("LINKMAN_TEL"));

			String linkMobile = Util.null2String(rds
					.getString("LINKMAN_MOBILE"));

			String linkEmail = Util.null2String(rds.getString("LINKMAN_EMAIL"));

			String serviceStartDate = Util.null2String(rds
					.getString("SERVICE_STARTDATE"));

			String serviceEndDate = Util.null2String(rds
					.getString("SERVICE_ENDDATE"));

			String orderDate = Util.null2String(rds.getString("ORDER_DATE"));

			String companyId = Util.null2String(rds.getString("STOCK_CODE"));
			String companyFullName = Util.null2String(rds
					.getString("COMPANY_FULLNAME"));
			// 发票类型 0代表增值税专用发票 1代表增值税普通发票
			String fplx = Util.null2String(rds.getString("RECEIPT_TYPE"));
			int d = 0;
			if (fplx.equals("1")) {
				d = 1;
			}
			if (fplx.equals("0")) {
				d = 0;
			}
			
			
			// 纳税登记号
			String ns = Util.null2String(rds
					.getString("RECEIPT_TAXPAYER_CODE"));
			String nsdjh=ns.replace(" ", "");
			
			
			// 发票抬头
			String fptt = Util.null2String(rds.getString("RECEIPT_TITLE"));
			// 发票单位地址
			String fpdwdz = Util.null2String(rds
					.getString("RECEIPT_COMPANY_ADDR"));
			// 发票联系电话
			String fplxdh = Util.null2String(rds.getString("RECEIPT_PHONE"));
			// 开户银行名称
			String khyhmc = Util
					.null2String(rds.getString("RECEIPT_BANK_NAME"));
			// 银行账户
			String yhzh = Util.null2String(rds
					.getString("RECEIPT_BANK_ACCOUNT"));
			String dwdzdh = fpdwdz + " " + fplxdh;
			String khyhmczh = khyhmc + " " + yhzh;
			// 验证客户是否存在
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			String customerid = "";// 客户id
			String cusbusinessid = "";// 客户业务id
			String businessid = "17";// e服务专业版业务
			String sqlkh = "select * from uf_crm_customerinfo where name='"
					+ companyFullName + "'";
			rs.executeSql(sqlkh);
			if (rs.next()) {
				customerid = Util.null2String(rs.getString("id"));
				// 验证客户业务是否存在
				String sqlyw = "select * from uf_crm_custbusiness where customer='"
						+ customerid + "' and business='" + businessid + "'";
				rs1.executeSql(sqlyw);
				if (rs1.next()) {
					cusbusinessid = Util.null2String(rs1.getString("id"));
				}
			}	
			// 客户业务不存在创建流程
			if (cusbusinessid.equals("")) {
				customerid = "";// 业务不存在时，客户变为空
				try {
					int requestids = createCustomerBusiness(companyFullName,
							businessid);
					if (requestids > 0) {
						lg.writeLog("创建客户业务卡片流程触发工作流成功，流程requestid:"
								+ requestids);
					}
				} catch (Exception e) {
					lg.writeLog("创建客户业务卡片流程触发出错：" + e);
				}
			}
			try {
				int requestid = createWF(syid, companyName, productName,
						ordeAmount, linkMan, linkManTel, linkMobile, linkEmail,
						serviceStartDate, serviceEndDate, orderDate, companyId,
						companyFullName, customerid, Integer.toString(d),
						nsdjh, fptt, dwdzdh, khyhmczh,remarks);
				if (requestid > 0) {
					SimpleDateFormat dateFormat_now = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String createDate = dateFormat_now.format(new Date());
					String sqlupdate = "update SYNC_ENTERPRISE_INFO set STATUS=1,APPROVAL_STATUS=0,PAY_STATUS=0,STEP2_TIME=to_date('"
							+ createDate
							+ "','yyyy-mm-dd hh24:mi:ss')  where ID='"
							+ syid
							+ "'";
					rds.executeSql(sqlupdate);
					this.lg.writeLog("上市公司年费订购订单流程触发工作流成功，流程requestid:"
							+ requestid);
				}
			} catch (Exception e) {
				this.lg.writeLog("上市公司年费订购订单流程触发出错：" + e);
			}
		}
		this.lg.writeLog("上市公司年费订购订单流程触发结束");
	}

	public int createWF(String syid, String companyName, String productName,
			String ordeAmount, String linkMan, String linkManTel,
			String linkMobile, String linkEmail, String serviceStartDate,
			String serviceEndDate, String orderDate, String companyId,
			String companyFullName, String customerid, String fplx,
			String nsdjh, String fptt, String dwdzdh, String khyhmczh,String remarks) {
		String newrequestid = "";
		try {
			// String workflowid = "205";
			String workflowid = "470";
			String lcbt = "上市公司年费订购订单";
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();

			requestInfo.setWorkflowid(workflowid);
			requestInfo.setCreatorid("149");
			requestInfo.setDescription(lcbt);

			requestInfo.setIsNextFlow("1");

			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;

			if (syid != "") {
				field = new Property();
				field.setName("syid");
				field.setValue(syid);
				fields.add(field);
			}
			if (companyName != "") {
				field = new Property();
				field.setName("companyName");
				field.setValue(companyName);
				fields.add(field);
			}
			if (productName != "") {
				field = new Property();
				field.setName("productName");
				field.setValue(productName);
				fields.add(field);
			}
			if (ordeAmount != "") {
				field = new Property();
				field.setName("ordeAmount");
				field.setValue(ordeAmount);
				fields.add(field);
			}
			if (linkMan != "") {
				field = new Property();
				field.setName("linkMan");
				field.setValue(linkMan);
				fields.add(field);
			}
			if (linkManTel != "") {
				field = new Property();
				field.setName("linkManTel");
				field.setValue(linkManTel);
				fields.add(field);
			}
			if (linkMobile != "") {
				field = new Property();
				field.setName("linkMobile");
				field.setValue(linkMobile);
				fields.add(field);
			}

			if (linkEmail != "") {
				field = new Property();
				field.setName("linkEmail");
				field.setValue(linkEmail);
				fields.add(field);
			}

			if (serviceStartDate != "") {
				field = new Property();
				field.setName("serviceStartDate");
				field.setValue(serviceStartDate);
				fields.add(field);
			}

			if (serviceEndDate != "") {
				field = new Property();
				field.setName("serviceEndDate");
				field.setValue(serviceEndDate);
				fields.add(field);
			}

			if (orderDate != "") {
				field = new Property();
				field.setName("orderDate");
				field.setValue(orderDate);
				fields.add(field);
			}

			if (companyId != "") {
				field = new Property();
				field.setName("companyId");
				field.setValue(companyId);
				fields.add(field);
			}

			if (companyFullName != "") {
				field = new Property();
				field.setName("companyFullName");
				field.setValue(companyFullName);
				fields.add(field);
			}
			if (customerid != "") {
				field = new Property();
				field.setName("customer");
				field.setValue(customerid);
				fields.add(field);
			}
			if (fplx != "") {
				field = new Property();
				field.setName("invoicetype");
				field.setValue(fplx);
				fields.add(field);
			}
			if (nsdjh != "") {
				field = new Property();
				field.setName("creditcode");
				field.setValue(nsdjh);
				fields.add(field);
			}
			if (fptt != "") {
				field = new Property();
				field.setName("unitname");
				field.setValue(fptt);
				fields.add(field);
			}
			if (dwdzdh != "") {
				field = new Property();
				field.setName("address");
				field.setValue(dwdzdh);
				fields.add(field);
			}
			if (khyhmczh != "") {
				field = new Property();
				field.setName("accountname");
				field.setValue(khyhmczh);
				fields.add(field);
			}	
			if (remarks != "") {
				field = new Property();
				field.setName("remarks");
				field.setValue(remarks);
				fields.add(field);
			}	
			Property[] fieldarray = (Property[]) fields
					.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
		} catch (Exception e) {
			this.lg.writeLog("上市公司年费订购订单触发出错：" + e);
		}
		return Util.getIntValue(newrequestid, 0);
	}

	/**
	 * 创建客户卡片流程
	 */
	public int createCustomerBusiness(String name, String business) {
		String newrequestid = "";
		try {
			String workflowid = "430"; //
			String lcbt = "创建客户业务卡片";// 流程标题
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			lg.writeLog("创建客户业务卡片程触发start：");
			requestInfo.setWorkflowid(workflowid);// 流程类型id
			requestInfo.setCreatorid("149");// 创建人
			requestInfo.setDescription(lcbt);// 设置流程标题
			requestInfo.setRequestlevel("0");// 0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("0");// 保存在发起节点

			SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd");
			String createDate = dateFormat_now.format(new Date());

			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;

			field = new Property();
			field.setName("name");
			field.setValue(name);
			fields.add(field);

			field = new Property();
			field.setName("business");
			field.setValue(business);
			fields.add(field);

			field = new Property();
			field.setName("createdate");
			field.setValue(createDate);
			fields.add(field);

			field = new Property();
			field.setName("creator");
			field.setValue("149");
			fields.add(field);

			field = new Property();
			field.setName("dept");
			field.setValue("23");
			fields.add(field);

			fields.add(field);
			Property[] fieldarray = (Property[]) fields
					.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
			lg.writeLog("创建客户业务卡片流程触发end：");
		} catch (Exception e) {
			lg.writeLog("创建客户业务卡片流程触发出错：" + e);
		}
		return Util.getIntValue(newrequestid, 0);
	}

}