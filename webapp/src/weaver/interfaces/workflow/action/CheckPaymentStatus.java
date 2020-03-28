package weaver.interfaces.workflow.action;

import weaver.interfaces.schedule.BaseCronJob;
import weaver.sysinterface.EASWSDispose;

import java.util.Calendar;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.Util;

public class CheckPaymentStatus extends BaseCronJob {
	BaseBean lg = new BaseBean();
	SelectSYFJ sc = new SelectSYFJ();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
			+ Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-"
			+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);

	// 通过继承BaseCronJob类可以实现定时同步
	// 0 0 1 1/1 * ? 每天凌晨一点执行一次
	public void execute() {
		try {
			check();
		} catch (Exception e) {
		}
	}

	private void check() {
		lg.writeLog("开始检查金蝶付款详情....");
		String sql = "select * from uf_payList where dealStatus='3' and (payStatus='0' or payStatus is null)";
		RecordSet rs = new RecordSet();
		RecordSet rs2 = new RecordSet();
		rs.executeSql(sql);
		EASWSDispose dispose = new EASWSDispose();
		String back = dispose.EASLogin();
		lg.writeLog("金蝶登录返回:" + back);
		if ("1".equals(back)) {
			while (rs.next()) {
				String MyBillGUID = Util.null2String(rs.getString("uuid"));// 异构系统单据ID
				String flowNum = Util.null2String(rs.getString("flowNum"));// 文号
				String SourceSys = Prop.getPropValue("addfkd", "SourceSys");// 异构系统代码
				SourceSys += flowNum + ":" + MyBillGUID;
				String xml = "<KingdeeWsDataSet><KingdeeWsData>"
						+ "<SourceSys>" + SourceSys + "</SourceSys>"
						+ "<MyBillGUID>" + MyBillGUID + "</MyBillGUID>"
						+ "</KingdeeWsData></KingdeeWsDataSet>";
				lg.writeLog("xml:" + xml);
				String backvalue = dispose.Query(xml);
				if (!"".equals(backvalue)) {
					if ("1".equals(backvalue.substring(0, 1))) {
						String staus = backvalue.substring(
								backvalue.indexOf("<billStatus>") + 12,
								backvalue.indexOf("</billStatus>"));
						if ("1".equals(staus)) {
							rs2.executeSql("update uf_payList set payStatus='1' where uuid='"
									+ MyBillGUID + "'");
						} else if ("0".equals(staus)) {
							rs2.executeSql("update uf_payList set payStatus='0' where uuid='"
									+ MyBillGUID + "'");
						} else {
							rs2.executeSql("update uf_payList set payStatus='2' where uuid='"
									+ MyBillGUID + "'");
						}
					} else {
						lg.writeLog("金蝶查询付款失败,返回:" + backvalue);
					}
				}
			}
		} else {
			lg.writeLog("登录金蝶接口失败....");
		}
	}
}
