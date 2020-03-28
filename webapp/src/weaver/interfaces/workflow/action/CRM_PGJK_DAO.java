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
import weaver.systeminfo.SystemEnv;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//配股缴款数据流程
public class CRM_PGJK_DAO extends BaseCronJob {
	BaseBean lg = new BaseBean();
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
	 * 配股缴款数据流程触发
	 * 
	 */
	public void create() {
		
		
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from sync_enterprise_info where status=0 and product_name='配股缴款统计'";
		rds.executeSql(sql);
		while (rds.next()) {
			String remarks = Util.null2String(rds.getString("remarks"));
			String syid = Util.null2String(rds.getString("ID"));
			String gsdm = Util.null2String(rds.getString("STOCK_CODE"));
			String gsqc = Util.null2String(rds.getString("COMPANY_FULLNAME"));
			String cpmc = Util.null2String(rds.getString("PRODUCT_NAME"));
			String gsjc = Util.null2String(rds.getString("COMPANY_NAME"));
			// String cpmc = Util.null2String(rds.getString("STOCK_CODE"));
			// 是否委托提交
			String sfwttj = Util.null2String(rds.getString("PLATFORM"));
			String ddbh = Util.null2String(rds.getString("ORDER_ID"));
			// 配股单天收费标准,我查数据库并不是1000/2000/3000，暂时设为3000
			// String pgdtsf = Util.null2String(rds.getString("NORM_PRICE"));
			String pgdtsf = "2";
			String gdsj = Util.null2String(rds.getString("ORDER_DATE"));
			// 订单类型，0新增；1续签；
			String ddlx = Util.null2String(rds.getString("ORDER_TYPE"));
			// 服务天数，数据库存的是1年、1次、2次，和我们流程的1天、2天不对应，暂时设为1天，
			// String fwts = Util.null2String(rds.getString("SERVICE_PERIOD"));
			String fwts = "0";
			String ddje = Util.null2String(rds.getString("ORDER_AMOUNT"));
			String lxr = Util.null2String(rds.getString("LINKMAN"));
			String lxdh = Util.null2String(rds.getString("LINKMAN_TEL"));
			String sjlxfs = Util.null2String(rds.getString("LINKMAN_MOBILE"));
			String yx = Util.null2String(rds.getString("LINKMAN_EMAIL"));
			String sjksrq = Util.null2String(rds.getString("DATA_STARTDATE"));
			String sjjsrq = Util.null2String(rds.getString("DATA_ENDDATE"));
			String fwksrq = Util
					.null2String(rds.getString("SERVICE_STARTDATE"));
			String fwjsrq = Util.null2String(rds.getString("SERVICE_ENDDATE"));
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
			String businessid = "18";// 配股缴款业务
			String sqlkh = "select * from uf_crm_customerinfo where name='"
					+ gsqc + "'";
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
					int requestids = createCustomerBusiness(gsqc, businessid);
					if (requestids > 0) {
						lg.writeLog("创建客户业务卡片流程触发工作流成功，流程requestid:"
								+ requestids);
					}
				} catch (Exception e) {
					lg.writeLog("创建客户业务卡片流程触发出错：" + e);
				}
			}

			// 创建工作流开始
			try {
				int requestid = createWF(syid, gsdm, gsqc, gsjc, sfwttj, ddbh,
						pgdtsf, gdsj, ddlx, fwts, ddje, lxr, lxdh, sjlxfs, yx,
						sjksrq, sjjsrq, fwksrq, fwjsrq, cpmc, customerid,
						Integer.toString(d), nsdjh, fptt, dwdzdh, khyhmczh,remarks);
				if (requestid > 0) {
					SimpleDateFormat dateFormat_now = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String createDate = dateFormat_now.format(new Date());
					String sqlupdate = "update sync_enterprise_info set STATUS=1,APPROVAL_STATUS=0,PAY_STATUS=0,STEP2_TIME=to_date('"
							+ createDate
							+ "','yyyy-mm-dd hh24:mi:ss')  where ID='"
							+ syid
							+ "'";
					rds.executeSql(sqlupdate);
					lg.writeLog("配股缴款数据流程触发工作流成功，流程requestid:" + requestid);
				}
			} catch (Exception e) {
				lg.writeLog("配股缴款数据流程触发出错：" + e);
			}
		}
		lg.writeLog("配股缴款数据流程触发结束");
	}

	/**
	 * 创建配股缴款数据流程
	 */
	public int createWF(String syid, String gsdm, String gsqc, String gsjc,
			String sfwttj, String ddbh, String pgdtsf, String gdsj,
			String ddlx, String fwts, String ddje, String lxr, String lxdh,
			String sjlxfs, String yx, String sjksrq, String sjjsrq,
			String fwksrq, String fwjsrq, String cpmc, String customerid,
			String fplx, String nsdjh, String fptt, String dwdzdh,
			String khyhmczh,String remarks) {
		String newrequestid = "";
		try {
			String workflowid = "469";
			String lcbt = "配股缴款统计服务订单申请";// 流程标题
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();

			requestInfo.setWorkflowid(workflowid);// 流程类型id
			requestInfo.setCreatorid("149");// 创建人
			requestInfo.setDescription(lcbt);// 设置流程标题
			requestInfo.setRequestlevel("0");// 0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("1");// 流转到下一节点

			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;

			// 主表字段开始,1,syid,gsdm,gsqc,gsjc,sfwttj,ddbh,
			if (syid != "") {
				field = new Property();
				field.setName("syid");
				field.setValue(syid);
				fields.add(field);
			}
			if (gsdm != "") {
				field = new Property();
				field.setName("gsdm");
				field.setValue(gsdm);
				fields.add(field);
			}
			if (gsqc != "") {
				field = new Property();
				field.setName("gsqc");
				field.setValue(gsqc);
				fields.add(field);
			}
			if (gsjc != "") {
				field = new Property();
				field.setName("gsjc");
				field.setValue(gsjc);
				fields.add(field);
			}
			if (sfwttj != "") {
				field = new Property();
				field.setName("sfwttj");
				field.setValue(sfwttj);
				fields.add(field);
			}
			if (ddbh != "") {
				field = new Property();
				field.setName("ddbh");
				field.setValue(ddbh);
				fields.add(field);
			}

			// 7,pgdtsf,gdsj,ddlx,fwts,ddje
			if (pgdtsf != "") {
				field = new Property();
				field.setName("pgdtsf");
				field.setValue(pgdtsf);
				fields.add(field);
			}
			if (gdsj != "") {
				field = new Property();
				field.setName("dgsj");
				field.setValue(gdsj);
				fields.add(field);
			}
			if (ddlx != "") {
				field = new Property();
				field.setName("ddlx");
				field.setValue(ddlx);
				fields.add(field);
			}
			if (fwts != "") {
				field = new Property();
				field.setName("fwts");
				field.setValue(fwts);
				fields.add(field);
			}
			if (ddje != "") {
				field = new Property();
				field.setName("ddje");
				field.setValue(ddje);
				fields.add(field);
			}

			// 13,lxr,lxdh,sjlxfs,yx,sjksrq,sjjsrq,fwksrq,fwjsrq
			if (lxr != "") {
				field = new Property();
				field.setName("lxr");
				field.setValue(lxr);
				fields.add(field);
			}
			if (lxdh != "") {
				field = new Property();
				field.setName("lxdh");
				field.setValue(lxdh);
				fields.add(field);
			}
			if (sjlxfs != "") {
				field = new Property();
				field.setName("sjlxfs");
				field.setValue(sjlxfs);
				fields.add(field);
			}
			if (yx != "") {
				field = new Property();
				field.setName("yx");
				field.setValue(yx);
				fields.add(field);
			}
			if (sjksrq != "") {
				field = new Property();
				field.setName("sjksrq");
				field.setValue(sjksrq);
				fields.add(field);
			}
			if (sjjsrq != "") {
				field = new Property();
				field.setName("sjjsrq");
				field.setValue(sjjsrq);
				fields.add(field);
			}
			if (fwksrq != "") {
				field = new Property();
				field.setName("fwksrq");
				field.setValue(fwksrq);
				fields.add(field);
			}
			if (fwjsrq != "") {
				field = new Property();
				field.setName("fwjsrq");
				field.setValue(fwjsrq);
				fields.add(field);
			}
			if (cpmc != "") {
				field = new Property();
				field.setName("cpmc");
				field.setValue(cpmc);
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
			lg.writeLog("配股缴款数据流程触发出错：" + e);
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