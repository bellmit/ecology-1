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

public class CRM_FIXEDINCOME_SQ_DAO extends BaseCronJob {
	BaseBean lg = new BaseBean();
	SelectSYFJ sc = new SelectSYFJ();
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
		lg.writeLog("开始执行流程。。。。");
		RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
		String sql = "select * from SYNC_WEBSITE_FIXEDINCOME where STATUS=0";
		rds.executeSql(sql);
		while (rds.next()) {
			String syid = Util.null2String(rds.getString("ID"));
			String gsid = Util.null2String(rds.getString("COMPANY_ID"));

			String gsmc = "";
			String zcdz = "";
			String zzjgdm = "";
			String frdb = "";
			String gswz = "";
			String glfzr = "";
			String fzrdh = "";
			String ywlxr = "";
			String lxrdh = "";
			String gscz = "";
			String Email = "";
			String bgdz = "";
			String jsfzr = "";
			String fzrdh1 = "";
			String sjkfwq = "";
			String gsbj = "";
			String mgs = "";
			String wxzz = "";
			String xzgly = "";

			RecordSetDataSource rds1 = new RecordSetDataSource("exchangeDB");
			String sqlcom = "select * from SYNC_COMPANY where ID='" + gsid
					+ "'";
			rds1.executeSql(sqlcom);
			while (rds1.next()) {
				gsmc = Util.null2String(rds1.getString("COMPANY_NAME"));
				zcdz = Util.null2String(rds1.getString("REGISTERED_ADDRESS"));
				zzjgdm = Util.null2String(rds1.getString("ORGANIZATION_CODE"));
				frdb = Util.null2String(rds1.getString("LEGAL_PERSON"));
				gswz = Util.null2String(rds1.getString("WEBSITE"));
				glfzr = Util.null2String(rds1.getString("MANAGER_PRINCIPAL"));
				fzrdh = Util.null2String(rds1.getString("MANAGER_TEL"));
				ywlxr = Util.null2String(rds1.getString("BUSINESS_LINKMAN"));
				lxrdh = Util.null2String(rds1.getString("BUSINESS_TEL"));
				gscz = Util.null2String(rds1.getString("FAX"));
				Email = Util.null2String(rds1.getString("EMAIL"));
				bgdz = Util.null2String(rds1.getString("ADDRESS"));
				jsfzr = Util.null2String(rds1.getString("TECH_PRINCIPAL"));
				fzrdh1 = Util.null2String(rds1.getString("TECH_TEL"));
				sjkfwq = Util.null2String(rds1.getString("SYSTEM_ADDRESS"));
				wxzz = Util.null2String(rds1.getString("SATELLITE_ADDRESS"));
				xzgly = Util.null2String(rds1.getString("SATELLITE_MANAGER"));
				gsbj = Util.null2String(rds1.getString("MAIN_BUSINESS"));
				mgs = Util.null2String(rds1.getString("SHAREHOLDING_RATIO"));
			}
			String sqxknr = Util.null2String(rds.getString("APPLY_CONTENT"));
			if (sqxknr != "") {
				Boolean a = Boolean.valueOf(sqxknr.contains("实时行情"));
				Boolean b = Boolean.valueOf(sqxknr.contains("延时行情"));
				if (a.booleanValue()) {
					sqxknr = "0";
				}
				if (b.booleanValue()) {
					sqxknr = "1";
				}
			}
			String sqxkyt = Util.null2String(rds.getString("APPLY_PURPOSE"));
			String dnrjzs = "";
			String hlwwzzs = "";
			String sjzs = "";
			String gbdszs = "";
			String qt1 = "";
			if (sqxkyt != "") {
				Boolean a = Boolean.valueOf(sqxkyt.contains("电脑软件"));
				Boolean b = Boolean.valueOf(sqxkyt.contains("互联网网站"));
				Boolean c = Boolean.valueOf(sqxkyt.contains("手机"));
				Boolean d = Boolean.valueOf(sqxkyt.contains("广播电视"));
				Boolean e = Boolean.valueOf(sqxkyt.contains("其他"));
				if (a.booleanValue()) {
					dnrjzs = "1";
				}
				if (b.booleanValue()) {
					hlwwzzs = "1";
				}
				if (c.booleanValue()) {
					sjzs = "1";
				}
				if (d.booleanValue()) {
					gbdszs = "1";
				}
				if (e.booleanValue()) {
					qt1 = "1";
				}
			}
			String sqxkfw = Util.null2String(rds.getString("APPLY_RANGE"));
			String xjsdjs = "";
			String shcs = "";
			String zgnd = "";
			String jhqs = "";
			String qq = "";
			if (sqxkfw != null) {
				Boolean a = Boolean.valueOf(sqxkfw.contains("县级市/地级市"));
				Boolean b = Boolean.valueOf(sqxkfw.contains("省级城市"));
				Boolean c = Boolean.valueOf(sqxkfw.contains("中国内地"));
				Boolean d = Boolean.valueOf(sqxkfw.contains("京沪/全省"));
				Boolean e = Boolean.valueOf(sqxkfw.contains("全球"));
				if (a.booleanValue()) {
					xjsdjs = "1";
				}
				if (b.booleanValue()) {
					shcs = "1";
				}
				if (c.booleanValue()) {
					zgnd = "1";
				}
				if (d.booleanValue()) {
					jhqs = "1";
				}
				if (e.booleanValue()) {
					qq = "1";
				}
			}
			String sqxknxbegin = Util.null2String(rds
					.getString("APPLY_START_TIME"));

			String sqxknxend = Util
					.null2String(rds.getString("APPLY_END_TIME"));

			String sqsj = Util.null2String(rds.getString("APPLY_TIME"));

			String hqjsfs = Util.null2String(rds.getString("RECEIVE_MODE"));
			String shztkd = "";
			String hlw1 = "";
			String zx1 = "";
			if (hqjsfs != null) {
				Boolean a = Boolean.valueOf(hqjsfs.contains("上海证通宽带"));
				Boolean b = Boolean.valueOf(hqjsfs.contains("互联网"));
				Boolean c = Boolean.valueOf(hqjsfs.contains("专线"));
				if (a.booleanValue()) {
					shztkd = "1";
				}
				if (b.booleanValue()) {
					hlw1 = "1";
				}
				if (c.booleanValue()) {
					zx1 = "1";
				}
			}
			String cphfwmc = Util.null2String(rds.getString("PRODUCT"));

			String mbkh = Util.null2String(rds.getString("CUSTOMER"));

			String dj = Util.null2String(rds.getString("PRICE"));

			String tcsj = Util.null2String(rds.getString("LAUNCH_TIME"));

			String zzyhjslb = Util.null2String(rds.getString("RECEIVE_DEVICE"));
			String pc = "";
			String wife = "";
			String dsjjdh = "";
			String other = "";
			if (zzyhjslb != "") {
				Boolean a = Boolean.valueOf(zzyhjslb.contains("电脑"));
				Boolean b = Boolean.valueOf(zzyhjslb.contains("无线"));
				Boolean c = Boolean.valueOf(zzyhjslb.contains("电视机/机顶盒"));
				Boolean d = Boolean.valueOf(zzyhjslb.contains("其他"));
				if (a.booleanValue()) {
					pc = "1";
				}
				if (b.booleanValue()) {
					wife = "1";
				}
				if (c.booleanValue()) {
					dsjjdh = "1";
				}
				if (d.booleanValue()) {
					other = "1";
				}
			}
			String csfs = Util.null2String(rds.getString("TRANSMISSION_MODE"));
			String zx = "";
			String wx = "";
			String jyw = "";
			String hlw = "";
			String qt = "";
			if (csfs != "") {
				Boolean a = Boolean.valueOf(csfs.contains("专线"));
				Boolean b = Boolean.valueOf(csfs.contains("无线网络"));
				Boolean c = Boolean.valueOf(csfs.contains("局域网"));
				Boolean d = Boolean.valueOf(csfs.contains("互联网"));
				Boolean e = Boolean.valueOf(csfs.contains("其他"));
				if (a.booleanValue()) {
					zx = "1";
				}
				if (b.booleanValue()) {
					wx = "1";
				}
				if (c.booleanValue()) {
					jyw = "1";
				}
				if (d.booleanValue()) {
					hlw = "1";
				}
				if (e.booleanValue()) {
					qt = "1";
				}
			}
			String qtgnhxx = Util.null2String(rds.getString("MEMO"));

			String othrebz = Util.null2String(rds
					.getString("RECEIVE_DEVICE_OTHER"));

			String qtsrk = Util.null2String(rds
					.getString("TRANSMISSION_MODE_OTHER"));

			// 验证客户是否存在
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			String customerid = "";// 客户id
			String cusbusinessid = "";// 客户业务id
			String businessid = "3";// 固定收益业务
			String sqlkh = "select * from uf_crm_customerinfo where name='"
					+ gsmc + "'";
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
					int requestids = createCustomerBusiness(gsmc, businessid);
					if (requestids > 0) {
						lg.writeLog("创建客户业务卡片流程触发工作流成功，流程requestid:"
								+ requestids);
					}
				} catch (Exception e) {
					lg.writeLog("创建客户业务卡片流程触发出错：" + e);
				}
			}
			try {
				int requestid = createWF(syid, gsid, gsmc, zcdz, zzjgdm, frdb,
						gswz, glfzr, fzrdh, ywlxr, lxrdh, gscz, Email, bgdz,
						jsfzr, fzrdh1, sjkfwq, gsbj, mgs, dnrjzs, hlwwzzs,
						sjzs, gbdszs, qt1, xjsdjs, shcs, zgnd, jhqs, qq,
						sqxknxbegin, sqxknxend, sqsj, shztkd, hlw1, zx1,
						cphfwmc, mbkh, dj, tcsj, pc, wife, dsjjdh, other, zx,
						wx, jyw, hlw, qt, qtgnhxx, sqxknr, wxzz, xzgly,
						othrebz, qtsrk, customerid);
				if (requestid > 0) {
					SimpleDateFormat dateFormat_now = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String createDate = dateFormat_now.format(new Date());
					String sqlupdate = "update SYNC_WEBSITE_FIXEDINCOME set STATUS=1,STEP2_TIME=to_date('"
							+ createDate
							+ "','yyyy-mm-dd hh24:mi:ss')  where ID='"
							+ syid
							+ "'";
					rds.executeSql(sqlupdate);
					this.lg.writeLog("固定收益申请流程触发工作流成功,流程requestid:" + requestid);
				}
			} catch (Exception e) {
				this.lg.writeLog("固定收益申请流程触发出错:" + e);
			}
		}
		this.lg.writeLog("固定收益申请流程触发结束");
	}

	public int createWF(String syid, String gsid, String gsmc, String zcdz,
			String zzjgdm, String frdb, String gswz, String glfzr,
			String fzrdh, String ywlxr, String lxrdh, String gscz,
			String Email, String bgdz, String jsfzr, String fzrdh1,
			String sjkfwq, String gsbj, String mgs, String dnrjzs,
			String hlwwzzs, String sjzs, String gbdszs, String qt1,
			String xjsdjs, String shcs, String zgnd, String jhqs, String qq,
			String sqxknxbegin, String sqxknxend, String sqsj, String shztkd,
			String hlw1, String zx1, String cphfwmc, String mbkh, String dj,
			String tcsj, String pc, String wife, String dsjjdh, String other,
			String zx, String wx, String jyw, String hlw, String qt,
			String qtgnhxx, String sqxknr, String wxzz, String xzgly,
			String othrebz, String qtsrk, String customerid) {
		String newrequestid = "";
		try {
			String workflowid = "478";
			String lcbt = "固定收益许可申请";
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();

			requestInfo.setWorkflowid(workflowid);
			requestInfo.setCreatorid("124");
			requestInfo.setDescription(lcbt);
			requestInfo.setRequestlevel("0");
			requestInfo.setIsNextFlow("1");

			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;
			if (sqxknr != "") {
				field = new Property();
				field.setName("sqxknr");
				field.setValue(sqxknr);
				fields.add(field);
			}
			if (syid != "") {
				field = new Property();
				field.setName("syid");
				field.setValue(syid);
				fields.add(field);
			}
			if (gsid != "") {
				field = new Property();
				field.setName("gsid");
				field.setValue(gsid);
				fields.add(field);
			}
			if (gsmc != "") {
				field = new Property();
				field.setName("gsmc");
				field.setValue(gsmc);
				fields.add(field);
			}
			if (zcdz != "") {
				field = new Property();
				field.setName("zcdz");
				field.setValue(zcdz);
				fields.add(field);
			}
			if (zzjgdm != "") {
				field = new Property();
				field.setName("zzjgdm");
				field.setValue(zzjgdm);
				fields.add(field);
			}
			if (frdb != "") {
				field = new Property();
				field.setName("frdb");
				field.setValue(frdb);
				fields.add(field);
			}
			if (gswz != "") {
				field = new Property();
				field.setName("gswz");
				field.setValue(gswz);
				fields.add(field);
			}
			if (glfzr != "") {
				field = new Property();
				field.setName("glfzr");
				field.setValue(glfzr);
				fields.add(field);
			}
			if (fzrdh != "") {
				field = new Property();
				field.setName("fzrdh");
				field.setValue(fzrdh);
				fields.add(field);
			}
			if (ywlxr != "") {
				field = new Property();
				field.setName("ywlxr");
				field.setValue(ywlxr);
				fields.add(field);
			}
			if (lxrdh != "") {
				field = new Property();
				field.setName("lxrdh");
				field.setValue(lxrdh);
				fields.add(field);
			}
			if (gscz != "") {
				field = new Property();
				field.setName("gscz");
				field.setValue(gscz);
				fields.add(field);
			}
			if (Email != "") {
				field = new Property();
				field.setName("Email");
				field.setValue(Email);
				fields.add(field);
			}
			if (bgdz != "") {
				field = new Property();
				field.setName("bgdz");
				field.setValue(bgdz);
				fields.add(field);
			}
			if (jsfzr != "") {
				field = new Property();
				field.setName("jsfzr");
				field.setValue(jsfzr);
				fields.add(field);
			}
			if (fzrdh1 != "") {
				field = new Property();
				field.setName("fzrdh1");
				field.setValue(fzrdh1);
				fields.add(field);
			}
			if (sjkfwq != "") {
				field = new Property();
				field.setName("sjkfwq");
				field.setValue(sjkfwq);
				fields.add(field);
			}
			if (gsbj != "") {
				field = new Property();
				field.setName("gsbj");
				field.setValue(gsbj);
				fields.add(field);
			}
			if (mgs != "") {
				field = new Property();
				field.setName("mgs");
				field.setValue(mgs);
				fields.add(field);
			}
			if (dnrjzs != "") {
				field = new Property();
				field.setName("dnrjzs");
				field.setValue(dnrjzs);
				fields.add(field);
			}
			if (hlwwzzs != "") {
				field = new Property();
				field.setName("hlwwzzs");
				field.setValue(hlwwzzs);
				fields.add(field);
			}
			if (sjzs != "") {
				field = new Property();
				field.setName("sjzs");
				field.setValue(sjzs);
				fields.add(field);
			}
			if (gbdszs != "") {
				field = new Property();
				field.setName("gbdszs");
				field.setValue(gbdszs);
				fields.add(field);
			}
			if (qt1 != "") {
				field = new Property();
				field.setName("qt1");
				field.setValue(qt1);
				fields.add(field);
			}
			if (xjsdjs != "") {
				field = new Property();
				field.setName("xjsdjs");
				field.setValue(xjsdjs);
				fields.add(field);
			}
			if (shcs != "") {
				field = new Property();
				field.setName("shcs");
				field.setValue(shcs);
				fields.add(field);
			}
			if (zgnd != "") {
				field = new Property();
				field.setName("zgnd");
				field.setValue(zgnd);
				fields.add(field);
			}
			if (jhqs != "") {
				field = new Property();
				field.setName("jhqs");
				field.setValue(jhqs);
				fields.add(field);
			}
			if (qq != "") {
				field = new Property();
				field.setName("qq");
				field.setValue(qq);
				fields.add(field);
			}
			if (sqxknxbegin != "") {
				field = new Property();
				field.setName("sqxknxbegin");
				field.setValue(sqxknxbegin);
				fields.add(field);
			}
			if (sqxknxend != "") {
				field = new Property();
				field.setName("sqxknxend");
				field.setValue(sqxknxend);
				fields.add(field);
			}
			if (sqsj != "") {
				field = new Property();
				field.setName("sqsj");
				field.setValue(sqsj);
				fields.add(field);
			}
			if (shztkd != "") {
				field = new Property();
				field.setName("shztkd");
				field.setValue(shztkd);
				fields.add(field);
			}
			if (hlw1 != "") {
				field = new Property();
				field.setName("hlw1");
				field.setValue(hlw1);
				fields.add(field);
			}
			if (zx1 != "") {
				field = new Property();
				field.setName("zx1");
				field.setValue(zx1);
				fields.add(field);
			}
			if (cphfwmc != "") {
				field = new Property();
				field.setName("cphfwmc");
				field.setValue(cphfwmc);
				fields.add(field);
			}
			if (mbkh != "") {
				field = new Property();
				field.setName("mbkh");
				field.setValue(mbkh);
				fields.add(field);
			}
			if (dj != "") {
				field = new Property();
				field.setName("dj");
				field.setValue(dj);
				fields.add(field);
			}
			if (tcsj != "") {
				field = new Property();
				field.setName("tcsj");
				field.setValue(tcsj);
				fields.add(field);
			}
			if (pc != "") {
				field = new Property();
				field.setName("pc");
				field.setValue(pc);
				fields.add(field);
			}
			if (wife != "") {
				field = new Property();
				field.setName("wifi");
				field.setValue(wife);
				fields.add(field);
			}
			if (dsjjdh != "") {
				field = new Property();
				field.setName("dsjjdh");
				field.setValue(dsjjdh);
				fields.add(field);
			}
			if (other != "") {
				field = new Property();
				field.setName("other");
				field.setValue(other);
				fields.add(field);
			}
			if (zx != "") {
				field = new Property();
				field.setName("zx");
				field.setValue(zx);
				fields.add(field);
			}
			if (wx != "") {
				field = new Property();
				field.setName("wx");
				field.setValue(wx);
				fields.add(field);
			}
			if (jyw != "") {
				field = new Property();
				field.setName("jyw");
				field.setValue(jyw);
				fields.add(field);
			}
			if (hlw != "") {
				field = new Property();
				field.setName("hlw");
				field.setValue(hlw);
				fields.add(field);
			}
			if (qt != "") {
				field = new Property();
				field.setName("qt");
				field.setValue(qt);
				fields.add(field);
			}
			if (qtgnhxx != "") {
				field = new Property();
				field.setName("qtgnhxx");
				field.setValue(qtgnhxx);
				fields.add(field);
			}
			if (wxzz != "") {
				field = new Property();
				field.setName("wxzz");
				field.setValue(wxzz);
				fields.add(field);
			}
			if (xzgly != "") {
				field = new Property();
				field.setName("xzgly");
				field.setValue(xzgly);
				fields.add(field);
			}
			if (othrebz != "") {
				field = new Property();
				field.setName("othrebz");
				field.setValue(othrebz);
				fields.add(field);
			}
			if (qtsrk != "") {
				field = new Property();
				field.setName("qtsrk");
				field.setValue(qtsrk);
				fields.add(field);
			}
			if (customerid != "") {
				field = new Property();
				field.setName("customer");
				field.setValue(customerid);
				fields.add(field);
			}
			RecordSetDataSource fjss = new RecordSetDataSource("exchangeDB");
			String sqlfj = "select * from SYNC_ATTACH where info_id ='" +syid+ "'";
			fjss.executeSql(sqlfj);
			String docids = "";
			while (fjss.next()) {
				String id = Util.null2String(fjss.getString("id"));
				String NRBT = Util.null2String(fjss.getString("ATTACH_NAME"));
				int aa = this.sc.getContent(id, "124", NRBT);
				if (aa != 0) {
					docids = docids + "," + aa;
				}
			}
			if ((docids != null) && (docids.length() > 0)) {
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
			this.lg.writeLog("固定收益申请流程触发出错:" + e);
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
			String lcbt = "创建客户业务卡片:" + name;// 流程标题
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			lg.writeLog("创建客户业务卡片程触发start：");
			requestInfo.setWorkflowid(workflowid);// 流程类型id
			requestInfo.setCreatorid("124");// 创建人
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
			field.setValue("124");
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
