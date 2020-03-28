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

//网络投票流程
public class CRM_WLTP_DAO extends BaseCronJob {
	BaseBean lg = new BaseBean();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
			+ Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-"
			+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);

	public void execute() {
		try {
			create();
		} catch (Exception e) {
		}
	}

	/**
	 * 网络投票流程触发
	 * 
	 */
	public void create() {
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_ENTERPRISE_INFO where STATUS=0 and PRODUCT_NAME='股东大会网络投票'";
		rds.executeSql(sql);
		while (rds.next()) {
			String remarks = Util.null2String(rds.getString("remarks"));
			String syid = Util.null2String(rds.getString("ID"));
			// 公司股票代码
			String agdm = Util.null2String(rds.getString("STOCK_CODE"));
			String bgdm = Util.null2String(rds.getString("STOCK_B_CODE"));
			// 公司全称
			String gsqc = Util.null2String(rds.getString("COMPANY_FULLNAME"));
			// 公司简称
			String gsjc = Util.null2String(rds.getString("COMPANY_NAME"));
			String gsdm = Util.null2String(rds.getString("COMPANY_ID"));
			// 产品名称
			String cpmc = Util.null2String(rds.getString("PRODUCT_NAME"));
			// 订单金额
			String ddje = Util.null2String(rds.getString("ORDER_AMOUNT"));
			// 联系人
			String lxr = Util.null2String(rds.getString("LINKMAN"));

			// 联系人电话
			String lxdh = Util.null2String(rds.getString("LINKMAN_TEL"));
			// 联系人手机
			String lxfs = Util.null2String(rds.getString("LINKMAN_MOBILE"));
			// 联系人电子邮箱
			String dzyx = Util.null2String(rds.getString("LINKMAN_EMAIL"));
			// 订购时间
			String dgrq = Util.null2String(rds.getString("ORDER_DATE"));

			// 股东大会名称
			String gddhmc = Util.null2String(rds.getString("MEETING_NAME"));
			// 股东大会召开日期
			String zkrq = Util.null2String(rds.getString("MEETING_DATE"));
			// 网络投票日期
			String tprq = Util
					.null2String(rds.getString("NETWORK_VOTING_DATE"));

			// A股股权登记日
			String agdjr = Util.null2String(rds.getString("DATE_OF_RECORD_A"));
			// B股股权登记日
			String bgdjr = Util.null2String(rds.getString("DATE_OF_RECORD_B"));
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

			// 中小投资者表决单独统计
			String zxddtj = Util.null2String(rds.getString("IS_MS_SPLIT"));
			int c = 0;
			if (zxddtj.equals("0")) {
				c = 1;
			}else if (zxddtj.equals("1")) {
				c = 0;
			}else if (zxddtj.equals("")) {
				c = 1;
			}
			// 是否订制午间统计服务,0代表否，1代表是
			String sfdzwj = Util.null2String(rds.getString("NOON_STATISTICS"));
			int a = 0;
			if (sfdzwj.equals("0")) {
				a = 1;
			}else if (sfdzwj.equals("1")) {
				a = 0;
			}else if (sfdzwj.equals("")) {
				a = 1;
			}
			// 是否需现金分红分段统计,0代表否，1代表是
			String sfxjfh = Util.null2String(rds.getString("ISCASHBOND"));
			int b = 0;
			if (sfxjfh.equals("0")) {
				b = 1;
			}else if (sfxjfh.equals("1")) {
				b = 0;
			}else if (sfxjfh.equals("")) {
				b = 1;
			}
			// 分段统计议案号
			String fdtjyah = Util.null2String(rds
					.getString("SEPARATE_TOPIC_SERIES"));

			// 验证客户是否存在
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			String customerid = "";// 客户id
			String cusbusinessid = "";// 客户业务id
			String businessid = "16";// 网络统计服务业务
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
				int requestid = createWF(syid, gsqc, gsjc, cpmc, ddje, lxr,
						lxdh, lxfs, dzyx, dgrq, gddhmc, zkrq, tprq,
						Integer.toString(a), agdjr, bgdjr, Integer.toString(c),
						Integer.toString(b), agdm, bgdm, gsdm, fdtjyah,
						customerid, Integer.toString(d), nsdjh, fptt, dwdzdh,
						khyhmczh,remarks);
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
					lg.writeLog("网络投票流程触发工作流成功，流程requestid:" + requestid);
				}
			} catch (Exception e) {
				lg.writeLog("网络投票流程触发出错：" + e);
			}
		}
		lg.writeLog("网络投票流程触发结束");
	}

	/**
	 * 创建网络投票流程
	 */
	public int createWF(String syid, String gsqc, String gsjc, String cpmc,
			String ddje, String lxr, String lxdh, String lxfs, String dzyx,
			String dgrq, String gddhmc, String zkrq, String tprq,
			String sfdzwj, String agdjr, String bgdjr, String zxddtj,
			String sfxjfh, String agdm, String bgdm, String gsdm,
			String fdtjyah, String customerid, String fplx, String nsdjh,
			String fptt, String dwdzdh, String khyhmczh,String remarks) {
		String newrequestid = "";
		try {
			String workflowid = "468";
			String lcbt = "股东大会网络投票统计服务订单申请";// 流程标题
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			lg.writeLog("网络投票流程触发start：");
			requestInfo.setWorkflowid(workflowid);// 流程类型id
			requestInfo.setCreatorid("149");// 创建人
			requestInfo.setDescription(lcbt);// 设置流程标题
			requestInfo.setRequestlevel("0");// 0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("1");// 流转到下一节点

			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;

			// 主表字段开始,1,syid,gsqc,cpmc,ddje,lxr,lxdh,
			if (syid != "") {
				field = new Property();
				field.setName("syid");
				field.setValue(syid);
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
			if (cpmc != "") {
				field = new Property();
				field.setName("cpmc");
				field.setValue(cpmc);
				fields.add(field);
			}
			if (ddje != "") {
				field = new Property();
				field.setName("ddje");
				field.setValue(ddje);
				fields.add(field);
			}
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

			// 7,lxfs,dzyx,dgrq,gddhmc,zkrq,tprq,
			if (lxfs != "") {
				field = new Property();
				field.setName("lxfs");
				field.setValue(lxfs);
				fields.add(field);
			}
			if (dzyx != "") {
				field = new Property();
				field.setName("dzyx");
				field.setValue(dzyx);
				fields.add(field);
			}
			if (dgrq != "") {
				field = new Property();
				field.setName("dgrq");
				field.setValue(dgrq);
				fields.add(field);
			}
			if (gddhmc != "") {
				field = new Property();
				field.setName("gddhmc");
				field.setValue(gddhmc);
				fields.add(field);
			}
			if (zkrq != "") {
				field = new Property();
				field.setName("zkrq");
				field.setValue(zkrq);
				fields.add(field);
			}
			if (tprq != "") {
				field = new Property();
				field.setName("tprq");
				field.setValue(tprq);
				fields.add(field);
			}

			// 13,sjdzwj,agdjr,bgdjr
			if (sfdzwj != "") {
				field = new Property();
				field.setName("sfdzwj");
				field.setValue(sfdzwj);
				fields.add(field);
			}
			if (agdjr != "") {
				field = new Property();
				field.setName("agdjr");
				field.setValue(agdjr);
				fields.add(field);
			}
			if (bgdjr != "") {
				field = new Property();
				field.setName("bgdjr");
				field.setValue(bgdjr);
				fields.add(field);
			}
			if (zxddtj != "") {
				field = new Property();
				field.setName("zxddtj");
				field.setValue(zxddtj);
				fields.add(field);
			}

			if (sfxjfh != "") {
				field = new Property();
				field.setName("sfxjfh");
				field.setValue(sfxjfh);
				fields.add(field);
			}
			if (agdm != "") {
				field = new Property();
				field.setName("agdm");
				field.setValue(agdm);
				fields.add(field);
			}
			if (bgdm != "") {
				field = new Property();
				field.setName("bgdm");
				field.setValue(bgdm);
				fields.add(field);
			}
			if (gsdm != "") {
				field = new Property();
				field.setName("gsdm");
				field.setValue(gsdm);
				fields.add(field);
			}

			if (fdtjyah != "") {
				field = new Property();
				field.setName("fdtjyah");
				field.setValue(fdtjyah);
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
			lg.writeLog("网络投票流程触发end：");
		} catch (Exception e) {
			lg.writeLog("网络投票流程触发出错：" + e);
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
