package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import weaver.conn.*;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//上证level2每月用户数据报送流程
public class CRM_LEVEL2_FZS_SJBS_DAO extends BaseCronJob {
	BaseBean lg = new BaseBean();
	SelectSYFJ sc = new SelectSYFJ();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
			+ Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-"
			+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);

	// 通过继承BaseCronJob类可以实现定时同步
	public void execute() {
		try {
			create();
		} catch (Exception e) {
		}
	}

	/**
	 * level2非展示数据报送流程触发
	 * 
	 */
	public void create() {
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_WEBSITE_LEVEL2_FORWARD where STATUS=0";
		//String sql = " select * from SYNC_WEBSITE_LEVEL2_FORWARD where rownum = 1 ";
		rds.executeSql(sql);
		while (rds.next()) {
			String organizationCode = Util.null2String(rds.getString("ORGANIZATION_CODE"));
			String companyName = Util.null2String(rds.getString("COMPANY_NAME"));
			String totalYearMonth = Util.null2String(rds.getString("REPORT_TIME"));
			String flowTitle = companyName + totalYearMonth.substring(0, 7) + " Level-2行情非展示数据报送";
			String syid = Util.null2String(rds.getString("ID"));
			
			String monthTransmitNum = Util.null2String(rds.getString("FORWARD_NUM"));
			String monthTransmitTotal = Util.null2String(rds.getString("FORWARD_TOTAL_COST"));
			String remark = Util.null2String(rds.getString("REMAKE"));
			
			String transmitWay_sy = Util.null2String(rds.getString("TRANSMIT"));//转发用途
			String transmitWay = "";
			if(transmitWay_sy == "1"){
				transmitWay = "0";
			}else if(transmitWay_sy == "2"){
				transmitWay = "1";
			}
lg.writeLog("转发用途----"+transmitWay);
			
			Calendar cale = Calendar.getInstance();
			cale.add(Calendar.MONTH, 1);
	        cale.set(Calendar.DAY_OF_MONTH, 0);
	        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
			String receivableDate=dateFormat.format(cale.getTime());    //应收日期
lg.writeLog("应收日期----"+receivableDate);
			
            String invoicetype="0";    //发票类型
			String unitname = ""; // 公司抬头
			String credit = ""; // 纳税登记号
			String creditcode="";
			String address = ""; // 单位地址电话
			String accountname = ""; // 开户行，银行账户
            
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			String customerid = "";
			String cusbusinessid = "";
			String businessid = "6";
			String sqlkh = "select * from uf_crm_customerinfo where name='"
					+ companyName + "'";
			rs.executeSql(sqlkh);
			if (rs.next()) {
				customerid = Util.null2String(rs.getString("id"));

				String sqlyw = "select * from uf_crm_custbusiness where customer='"
						+ customerid + "' and business='" + businessid + "'";
				rs1.executeSql(sqlyw);
				if (rs1.next()) {
					cusbusinessid = Util.null2String(rs1.getString("id"));
				}
			}
			if (cusbusinessid.equals("")) { // 客户业务不存在，则创建客户业务卡片
				customerid = "";
				try {
					createCustomerBusiness(companyName, businessid);
				} catch (Exception e) {
					}
			} else {          //客户业务存在，带出开票信息
				sql = " select unitname,creditcode,address,accountname from v_crm_customerinfo_kp where mainid='"
						+ customerid + "'";
				RecordSet rs4 = new RecordSet();
				rs4.executeSql(sql);
				if (rs4.next()) {
					unitname = Util.null2String(rs4.getString("unitname"));
					credit = Util.null2String(rs4.getString("creditcode"));
					creditcode=credit.replace(" ", "");
					address = Util.null2String(rs4.getString("address"));
					accountname = Util.null2String(rs4.getString("accountname"));
				}
			}
			// 创建工作流开始
			try {
				int requestid = createWF(organizationCode,companyName,totalYearMonth,
						syid,flowTitle,customerid,unitname,creditcode,
						address,accountname,invoicetype,receivableDate,
						monthTransmitNum,monthTransmitTotal,remark,transmitWay);
				if (requestid > 0) {
					SimpleDateFormat dateFormat_now = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String createDate = dateFormat_now.format(new Date());
					String sqlupdate = "update SYNC_WEBSITE_LEVEL2_FORWARD set STATUS=1,STEP2_TIME=to_date('"
							+ createDate
							+ "','yyyy-mm-dd hh24:mi:ss')  where ID='"
							+ syid
							+ "'";
					rds.executeSql(sqlupdate);
					lg.writeLog("Level-2行情非展示数据报送流程触发工作流成功，流程requestid:"
							+ requestid);
				}
			} catch (Exception e) {
				lg.writeLog("Level-2行情非展示数据报送流程触发出错：" + e);
			}
		}
		lg.writeLog("Level-2行情非展示数据报送流程触发结束");
	}

	/**
	 * 创建level2每月用户数据报送流程
	 */
	public int createWF(String organizationCode, String companyName, String totalYearMonth,
						String syid, String flowTitle, String customerid, String unitname, String creditcode,
						String address, String accountname,String invoicetype,String receivableDate,
						String monthTransmitNum,String monthTransmitTotal,String remark,String transmitWay) {
		String newrequestid = "";
		try {
			String workflowid = "506";
			String lcbt = "LEVEL-2 行情非展示数据报送";// 流程标题
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();

			requestInfo.setWorkflowid(workflowid);// 流程类型id
			requestInfo.setCreatorid("112");// 创建人
			requestInfo.setDescription(lcbt);// 设置流程标题
			requestInfo.setRequestlevel("0");// 0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("0");// 流转到下一节点

			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;


			// 主表字段开始,1,String organizationCode, String companyName, String totalYearMonth,
			
			if (organizationCode != "") {
				field = new Property();
				field.setName("organizationCode");
				field.setValue(organizationCode);
				fields.add(field);
			}
			
			if (companyName != "") {
				field = new Property();
				field.setName("companyName");
				field.setValue(companyName);
				fields.add(field);
			}

			if (totalYearMonth != "") {
				field = new Property();
				field.setName("totalYearMonth");
				field.setValue(totalYearMonth);
				fields.add(field);
			}
			
			
			// 主表字段开始,2,String syid, String flowTitle, String customerid, String unitname, String creditcode,

			if (syid != "") {
				field = new Property();
				field.setName("syid");
				field.setValue(syid);
				fields.add(field);
			}
			
			if (flowTitle != "") {
				field = new Property();
				field.setName("flowTitle");
				field.setValue(flowTitle);
				fields.add(field);
			}
				
			if (customerid != "") {
				field = new Property();
				field.setName("customerid");
				field.setValue(customerid);
				fields.add(field);
			}	
				
			if (unitname != "") {
				field = new Property();
				field.setName("unitname");
				field.setValue(unitname);
				fields.add(field);
			}	
				
			if (creditcode != "") {
				field = new Property();
				field.setName("creditcode");
				field.setValue(creditcode);
				fields.add(field);
			}	
			
			
			// 主表字段开始,3,String address, String accountname,String invoicetype,String receivableDate		

			if (address != "") {
				field = new Property();
				field.setName("address");
				field.setValue(address);
				fields.add(field);
			}
			
			if (accountname != "") {
				field = new Property();
				field.setName("accountname");
				field.setValue(accountname);
				fields.add(field);
			}
			
			if (invoicetype != "") {
				field = new Property();
				field.setName("invoicetype");
				field.setValue(invoicetype);
				fields.add(field);
			}
			
			if (receivableDate != "") {
				field = new Property();
				field.setName("receivableDate");
				field.setValue(receivableDate);
				fields.add(field);
			}
						

			// 主表字段开始,4,String monthTransmitNum,String monthTransmitTotal,String remark,String transmitWay	

			if (monthTransmitNum != "") {
				field = new Property();
				field.setName("monthTransmitNum");
				field.setValue(monthTransmitNum);
				fields.add(field);
			}
			
			if (monthTransmitTotal != "") {
				field = new Property();
				field.setName("monthTransmitTotal");
				field.setValue(monthTransmitTotal);
				fields.add(field);
			}		

			if (remark != "") {
				field = new Property();
				field.setName("remark");
				field.setValue(remark);
				fields.add(field);
			}	
			
			if (transmitWay != "") {
				field = new Property();
				field.setName("transmitWay");
				field.setValue(transmitWay);
				fields.add(field);
			}				
			
			// 上游附件,根据gsid来查询
			RecordSetDataSource fjss = new RecordSetDataSource("exchangeDB");
			String sqlfj = "select * from SYNC_ATTACH where info_id ='" + syid
					+ "'";
			fjss.executeSql(sqlfj);
			String docids = "";
			while (fjss.next()) {
				String id = Util.null2String(fjss.getString("id"));// 附件id
				String NRBT = Util.null2String(fjss.getString("ATTACH_NAME"));// 附件名称

				int aa = sc.getContent(id, "112", NRBT);
				if (aa != 0) {
					docids = docids + "," + aa;
				}

			}
			if (docids == null || docids.length() <= 0) {

			} else {
				docids = docids.substring(1);
			}

			field = new Property();
			field.setName("flowAttach");
			field.setValue(docids);
			fields.add(field);

			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
		} catch (Exception e) {
			lg.writeLog("Level-2行情非展示数据报送流程触发出错：" + e);
		}
		return Util.getIntValue(newrequestid, 0);
	}

	public int createCustomerBusiness(String name, String business) {
		String newrequestid = "";
		try {
			String workflowid = "430";
			String lcbt = "创建客户业务卡片:" + name;
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			requestInfo.setWorkflowid(workflowid);
			requestInfo.setCreatorid("112");
			requestInfo.setDescription(lcbt);
			requestInfo.setRequestlevel("0");
			requestInfo.setIsNextFlow("0");

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
			field.setValue("112");
			fields.add(field);

			field = new Property();
			field.setName("dept");
			field.setValue("23");
			fields.add(field);

			fields.add(field);
			Property[] fieldarray = (Property[]) fields.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
		} catch (Exception e) {
			this.lg.writeLog("创建客户业务卡片流程触发出错：" + e);
		}
		return Util.getIntValue(newrequestid, 0);
	}
}
