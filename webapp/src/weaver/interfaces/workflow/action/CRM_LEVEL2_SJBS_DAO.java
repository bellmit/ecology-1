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
public class CRM_LEVEL2_SJBS_DAO extends BaseCronJob {
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
	 * level2每月用户数据报送流程触发
	 * 
	 */
	public void create() {
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		RecordSetDataSource rds1 = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_WEBSITE_LEVEL2_USERDATA where STATUS=0";
		rds.executeSql(sql);
		while (rds.next()) {
			String zzjgdm = Util
					.null2String(rds.getString("ORGANIZATION_CODE"));
			String xxsmc = Util.null2String(rds.getString("COMPANY_NAME"));
            //报告时间
			String bgsj = Util.null2String(rds.getString("REPORT_TIME"));
			String year="";
			String month="";
			if(bgsj!=""){
				String[] tempStr=bgsj.split("/");
				year=tempStr[0];
				month=tempStr[1];
			}
			
			String sjscsj = Util.null2String(rds.getString("UPLOAD_TIME"));

			String pcsl = Util.null2String(rds.getString("PC_TERMINAL_NUM"));
			String pcje = Util.null2String(rds.getString("PC_TERMINAL_FEE"));

			String bjzdsl = Util.null2String(rds
					.getString("HALF_PRICE_TERMINAL_NUM"));
			String bjzdje = Util.null2String(rds
					.getString("HALF_PRICE_TERMINAL_FEE"));

			String ydzdsl = Util.null2String(rds
					.getString("MOBILE_TERMINAL_NUM"));
			String ydzdje = Util.null2String(rds
					.getString("MOBILE_TERMINAL_FEE"));

			String pcpjbsl = Util.null2String(rds
					.getString("POPULARIZE_TERMINAL_NUM"));
			String pcpjbje = Util.null2String(rds
					.getString("POPULARIZE_TERMINAL_FEE"));
			String ydpjbsl = Util.null2String(rds
					.getString("MOBILE_COMMON_NUM"));
			String ydpjbje = Util.null2String(rds
					.getString("MOBILE_COMMON_FEE"));

			String qtsl = Util.null2String(rds.getString("OTHER_TERMINAL_NUM"));
			String qtje = Util.null2String(rds.getString("OTHER_TERMINAL_FEE"));

			String mfzdsl = Util
					.null2String(rds.getString("FREE_TERMINAL_NUM"));
			String yssyf = Util.null2String(rds.getString("ACCOUNTS_USE_FEE"));
			String tkzkf = Util.null2String(rds
					.getString("REFUND_DISCOUNT_AMOUNT"));
			String ssxxf = Util.null2String(rds.getString("REAL_PRICE"));

			String datafeedsl = Util.null2String(rds
					.getString("DATAFEED_CUSTOMER"));
			String datafeedje = Util.null2String(rds
					.getString("DATAFEED_CUSTOMER_FEE"));

			String bdxlsl = Util.null2String(rds.getString("LOCAL_LINE_NUM"));
			String bdxlje = Util.null2String(rds.getString("LOCAL_LINE_FEE"));

			String ydxlsl = Util.null2String(rds.getString("REMOTE_LINE_NUM"));
			String ydxlje = Util.null2String(rds.getString("REMOTE_LINE_FEE"));

			String gjbdsl = Util.null2String(rds.getString("LOCAL_CUT_NUM"));
			String gjbdje = Util.null2String(rds.getString("LOCAL_CUT_FEE"));

			String gjydsl = Util.null2String(rds.getString("REMOTE_CUT_NUM"));
			String gjydje = Util.null2String(rds.getString("REMOTE_CUT_FEE"));

			String vdesl = Util.null2String(rds.getString("VDE_NUM"));
			String vdeje = Util.null2String(rds.getString("VDE_FEE"));

			String syid = Util.null2String(rds.getString("ID"));
			String bt = xxsmc + bgsj.substring(0, 7) + " Level2数据报送";
			
			
			// 以前错误指正
			String yqcwzz = Util.null2String(rds.getString("BUG_FIXED"));
			if (yqcwzz != null) {
				if (yqcwzz.equals("0")) {
					yqcwzz = "没有";
				} else if (yqcwzz.equals("1")) {
					yqcwzz = "有";
				}
			} 
			
			Calendar cale = Calendar.getInstance();
			cale.add(Calendar.MONTH, 1);
	        cale.set(Calendar.DAY_OF_MONTH, 0);
	        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
			String ysrq=dateFormat.format(cale.getTime());    //应收日期
			lg.writeLog("应收日期----"+ysrq);
			
            String fplx="0";    //发票类型
			String unitname = ""; // 公司抬头
			String credit = ""; // 纳税登记号
			String creditcode="";
			String address = ""; // 单位地址电话
			String accountname = ""; // 开户行，银行账户
            
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			String customerid = "";
			String cusbusinessid = "";
			String businessid = "5";
			String sqlkh = "select * from uf_crm_customerinfo where name='"
					+ xxsmc + "'";
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
					createCustomerBusiness(xxsmc, businessid);
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
					accountname = Util
							.null2String(rs4.getString("accountname"));
				}
			}
			// 创建工作流开始
			try {
				int requestid = createWF(zzjgdm, xxsmc, bgsj, sjscsj, pcsl,
						pcje, bjzdsl, bjzdje, ydzdsl, ydzdje, pcpjbsl, pcpjbje,
						ydpjbsl, ydpjbje, qtsl, qtje, mfzdsl, yssyf, tkzkf,
						ssxxf, datafeedsl, datafeedje, bdxlsl, bdxlje, ydxlsl,
						ydxlje, gjbdsl, gjbdje, gjydsl, gjydje, vdesl, vdeje,
						syid, bt, yqcwzz, customerid, unitname, creditcode,
						address, accountname,fplx,ysrq,year,month);
				if (requestid > 0) {
					SimpleDateFormat dateFormat_now = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String createDate = dateFormat_now.format(new Date());
					String sqlupdate = "update SYNC_WEBSITE_LEVEL2_USERDATA set STATUS=1,STEP2_TIME=to_date('"
							+ createDate
							+ "','yyyy-mm-dd hh24:mi:ss')  where ID='"
							+ syid
							+ "'";
					rds1.executeSql(sqlupdate);
					lg.writeLog("level2每月用户数据报送流程触发工作流成功，流程requestid:"
							+ requestid);
				}
			} catch (Exception e) {
				lg.writeLog("level2每月用户数据报送流程触发出错：" + e);
			}
		}
		lg.writeLog("level2每月用户数据报送流程触发结束");
	}

	/**
	 * 创建level2每月用户数据报送流程
	 */
	public int createWF(String zzjgdm, String xxsmc, String bgsj,
			String sjscsj, String pcsl, String pcje, String bjzdsl,
			String bjzdje, String ydzdsl, String ydzdje, String pcpjbsl,
			String pcpjbje, String ydpjbsl, String ydpjbje, String qtsl,
			String qtje, String mfzdsl, String yssyf, String tkzkf,
			String ssxxf, String datafeedsl, String datafeedje, String bdxlsl,
			String bdxlje, String ydxlsl, String ydxlje, String gjbdsl,
			String gjbdje, String gjydsl, String gjydje, String vdesl,
			String vdeje, String syid, String bt, String yqcwzz,
			String customer, String unitname, String creditcode,
			String address, String accountname,String fplx,String ysrq,String year,String month) {
		String newrequestid = "";
		try {
			String workflowid = "475";
			String lcbt = "LEVEL-2 行情信息商用户数据报送";// 流程标题
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
			
			if (year != "") {
				field = new Property();
				field.setName("year");
				field.setValue(year);
				fields.add(field);
			}
			if (month != "") {
				field = new Property();
				field.setName("month");
				field.setValue(month);
				fields.add(field);
			}
			// 主表字段开始,1,String zzjgdm,String xxsmc,String bgsj,String
			// sjscsj,String pcsl,String pcje,
			if (zzjgdm != "") {
				field = new Property();
				field.setName("zzjgdm");
				field.setValue(zzjgdm);
				fields.add(field);
			}

			if (xxsmc != "") {
				field = new Property();
				field.setName("xxsmc");
				field.setValue(xxsmc);
				fields.add(field);
			}
			if (bgsj != "") {
				field = new Property();
				field.setName("bgsj");
				field.setValue(bgsj);
				fields.add(field);
			}
			if (sjscsj != "") {
				field = new Property();
				field.setName("sjscsj");
				field.setValue(sjscsj);
				fields.add(field);
			}
			if (pcsl != "") {
				field = new Property();
				field.setName("pcsl");
				field.setValue(pcsl);
				fields.add(field);
			}
			if (pcje != "") {
				field = new Property();
				field.setName("pcje");
				field.setValue(pcje);
				fields.add(field);
			}
			// 7,String bjzdsl,String bjzdje,String ydzdsl,String ydzdje,String
			// pcpjbsl,String pcpjbje,String ydpjbsl,String ydpjbje,
			if (bjzdsl != "") {
				field = new Property();
				field.setName("bjzdsl");
				field.setValue(bjzdsl);
				fields.add(field);
			}
			if (bjzdje != "") {
				field = new Property();
				field.setName("bjzdje");
				field.setValue(bjzdje);
				fields.add(field);
			}
			if (ydzdsl != "") {
				field = new Property();
				field.setName("ydzdsl");
				field.setValue(ydzdsl);
				fields.add(field);
			}
			if (ydzdje != "") {
				field = new Property();
				field.setName("ydzdje");
				field.setValue(ydzdje);
				fields.add(field);
			}
			if (pcpjbsl != "") {
				field = new Property();
				field.setName("pcpjbsl");
				field.setValue(pcpjbsl);
				fields.add(field);
			}
			if (pcpjbje != "") {
				field = new Property();
				field.setName("pcpjbje");
				field.setValue(pcpjbje);
				fields.add(field);
			}
			if (ydpjbsl != "") {
				field = new Property();
				field.setName("ydpjbsl");
				field.setValue(ydpjbsl);
				fields.add(field);
			}
			if (ydpjbje != "") {
				field = new Property();
				field.setName("ydpjbje");
				field.setValue(ydpjbje);
				fields.add(field);
			}
			// 13,String qtsl,String qtje,String mfzdsl,String yssyf,String
			// tkzkf,String ssxxf,
			if (qtsl != "") {
				field = new Property();
				field.setName("qtsl");
				field.setValue(qtsl);
				fields.add(field);
			}
			if (qtje != "") {
				field = new Property();
				field.setName("qtje");
				field.setValue(qtje);
				fields.add(field);
			}
			if (mfzdsl != "") {
				field = new Property();
				field.setName("mfzdsl");
				field.setValue(mfzdsl);
				fields.add(field);
			}
			if (yssyf != "") {
				field = new Property();
				field.setName("yssyf");
				field.setValue(yssyf);
				fields.add(field);
			}
			if (tkzkf != "") {
				field = new Property();
				field.setName("tkzkf");
				field.setValue(tkzkf);
				fields.add(field);
			}
			if (ssxxf != "") {
				field = new Property();
				field.setName("ssxxf");
				field.setValue(ssxxf);
				fields.add(field);
			}
			// 19,String datafeedsl,String datafeedje,String bdxlsl,String
			// bdxlje,String ydxlsl,String ydxlje,
			if (datafeedsl != "") {
				field = new Property();
				field.setName("datafeedsl");
				field.setValue(datafeedsl);
				fields.add(field);
			}
			if (datafeedje != "") {
				field = new Property();
				field.setName("datafeedje");
				field.setValue(datafeedje);
				fields.add(field);
			}
			if (bdxlsl != "") {
				field = new Property();
				field.setName("bdxlsl");
				field.setValue(bdxlsl);
				fields.add(field);
			}
			if (bdxlje != "") {
				field = new Property();
				field.setName("bdxlje");
				field.setValue(bdxlje);
				fields.add(field);
			}
			if (ydxlsl != "") {
				field = new Property();
				field.setName("ydxlsl");
				field.setValue(ydxlsl);
				fields.add(field);
			}
			if (ydxlje != "") {
				field = new Property();
				field.setName("ydxlje");
				field.setValue(ydxlje);
				fields.add(field);
			}
			// 25, String gjbdsl,String gjbdje,String gjydsl,String
			// gjydje,String vdesl,String vdeje
			if (gjbdsl != "") {
				field = new Property();
				field.setName("gjbdsl");
				field.setValue(gjbdsl);
				fields.add(field);
			}
			if (gjbdje != "") {
				field = new Property();
				field.setName("gjbdje");
				field.setValue(gjbdje);
				fields.add(field);
			}
			if (gjydsl != "") {
				field = new Property();
				field.setName("gjydsl");
				field.setValue(gjydsl);
				fields.add(field);
			}
			if (gjydje != "") {
				field = new Property();
				field.setName("gjydje");
				field.setValue(gjydje);
				fields.add(field);
			}
			if (vdesl != "") {
				field = new Property();
				field.setName("vdesl");
				field.setValue(vdesl);
				fields.add(field);
			}

			if (vdeje != "") {
				field = new Property();
				field.setName("vdeje");
				field.setValue(vdeje);
				fields.add(field);
			}
			if (syid != "") {
				field = new Property();
				field.setName("syid");
				field.setValue(syid);
				fields.add(field);
			}
			if (bt != "") {
				field = new Property();
				field.setName("bt");
				field.setValue(bt);
				fields.add(field);
			}
			if (yqcwzz != "") {
				field = new Property();
				field.setName("yqcwzz");
				field.setValue(yqcwzz);
				fields.add(field);
			}
			if (customer != "") {
				field = new Property();
				field.setName("customer");
				field.setValue(customer);
				fields.add(field);
			}

			if (creditcode != "") {
				field = new Property();
				field.setName("creditcode");
				field.setValue(creditcode);
				fields.add(field);
			}

			if (unitname != "") {
				field = new Property();
				field.setName("unitname");
				field.setValue(unitname);
				fields.add(field);
			}

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
			if (fplx != "") {
				field = new Property();
				field.setName("invoicetype");
				field.setValue(fplx);
				fields.add(field);
			}
			if (ysrq != "") {
				field = new Property();
				field.setName("ysrq");
				field.setValue(ysrq);
				fields.add(field);
			}
			

			// 上游附件,根据gsid来查询
			RecordSetDataSource fjss = new RecordSetDataSource("exchangeDB");
			String sqlfj = "select * from SYNC_ATTACH where info_id ='" + syid
					+ "'";
			fjss.executeSql(sqlfj);
			lg.writeLog("sqlfj------"+sqlfj);
			String docids = "";
			while (fjss.next()) {
				String id = Util.null2String(fjss.getString("id"));// 附件id
				String NRBT = Util.null2String(fjss.getString("ATTACH_NAME"));// 附件名称
				lg.writeLog("NRBT------"+NRBT); 				                 
				int aa = sc.getContent(id, "112", NRBT);
				if (aa != 0) {
					docids = docids + "," + aa;
				}
				
			}
			lg.writeLog("docids------"+docids); 
			if (docids == null || docids.length() <= 0) {

			} else {
				docids = docids.substring(1);
			}

			field = new Property();
			field.setName("fj");
			field.setValue(docids);
			fields.add(field);

			Property[] fieldarray = (Property[]) fields
					.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
		} catch (Exception e) {
			lg.writeLog("level2每月用户数据报送流程触发出错：" + e);
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
			Property[] fieldarray = (Property[]) fields
					.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);
		} catch (Exception e) {
			this.lg.writeLog("创建客户业务卡片流程触发出错：" + e);
		}
		return Util.getIntValue(newrequestid, 0);
	}
}
