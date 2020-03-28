package weaver.interfaces.workflow.action;

import java.util.Calendar;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

/**
 * 每天检查合同是否有效
 * 
 * @author lsq
 * @date 2019/5/20
 * @version 1.0
 */
public class CRM_CheckContract extends BaseCronJob {
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
			create();
		} catch (Exception e) {
		}
	}

	private void create() {
		lg.writeLog("开始检索许可库。。。");
		try {
			RecordSetDataSource exchangeDB = new RecordSetDataSource(
					"exchangeDB");
			String sqlcon = "select id from uf_crm_contract where enddate<convert(varchar(10),GETDATE(),120)";
			RecordSet rss = new RecordSet();
			rss.executeSql(sqlcon);
			while (rss.next()) {
				String id = Util.null2String(rss.getString("id"));
				RecordSet rsd=new RecordSet();
				String sqlyy = "update uf_crm_contract set status='1' where id='"
						+ id + "'";
				rsd.executeSql(sqlyy);
				lg.writeLog("update:" + sqlyy);

			}
			RecordSet rs = new RecordSet();
			String sql = "select id,uuid,customer,business,status,specialset from uf_crm_permitpub";
			rs.executeSql(sql);
			while (rs.next()) {
				// id
				String id = Util.null2String(rs.getString("id"));
				// uuid
				String uuid = Util.null2String(rs.getString("uuid"));
				// 客户
				String customer = Util.null2String(rs.getString("customer"));
				// 业务
				String businessid = Util.null2String(rs.getString("business"));
				// 状态
				String status = Util.null2String(rs.getString("status"));
				// 特殊字符
				String specialset = Util
						.null2String(rs.getString("specialset"));
				String sqlzjk = "";
				RecordSet rstype = new RecordSet();
				if (specialset.equals("0")) {
					sql = "update uf_crm_permitpub set status='0' where uuid='"
							+ uuid + "'";
					if (businessid.equals("1")) { // Level-1行情许可
						sqlzjk = "update sync_website_lv1_permit_list set status='0' where id='"
								+ uuid + "'";
					} else if (businessid.equals("2")) {// 期权行情展示许可
						sqlzjk = "update sync_website_opt_permit_list set status='0' where id='"
								+ uuid + "'";
					} else if (businessid.equals("3")) {// 固定收益行情许可
						sqlzjk = "update SYNC_FIXEDINCOME_PERMIT_LIST set status='0' where id='"
								+ uuid + "'";
					} else if (businessid.equals("4")) {// 指数授权
						sqlzjk = "update sync_website_sse_permit_list set status='0' where id='"
								+ uuid + "'";
					} else if (businessid.equals("5")) {// Level-2行情展示许可
						sqlzjk = "update sync_website_lv2_permit_list set status='0' where id='"
								+ uuid + "'";
					} else if (businessid.equals("6")) {// Level-2行情非展示许可
						sqlzjk = "update SYNC_LV2DATAFEED_PERMIT_LIST set status='0' where id='"
								+ uuid + "'";
					}

					rstype.executeSql(sql);
					exchangeDB.executeSql(sqlzjk);
				} else {
					String flag = "";
					sql = "select count(1) as flag from uf_crm_contract where "
							+ "startdate <=convert(varchar(10),GETDATE(),120) "
							+ "and enddate>=convert(varchar(10),GETDATE(),120) and status='0'"
							+ "and customer='"
							+ customer
							+ "' and business='"
							+ businessid + "'";
					RecordSet rscon = new RecordSet();
					rscon.executeSql(sql);
					if (rscon.next()) {
						flag = Util.null2String(rscon.getString("flag"));
					}
					lg.writeLog("flag---"+flag);
					String sqlsync = "";
					if (flag.equals("0")) {
						lg.writeLog("合同信息为终止");
						RecordSet rsva = new RecordSet();

						sql = "update uf_crm_permitpub set status='1' where customer='"
								+ customer + "'and business='"+businessid+"'";
						rsva.executeSql(sql);
						// 更新中间表0--level1 1--level2
						if (businessid.equals("1")) { // Level-1行情许可
							sqlsync = "update sync_website_lv1_permit_list set status='1' where id='"
									+ uuid + "'";
						} else if (businessid.equals("2")) {// 期权行情展示许可
							sqlsync = "update sync_website_opt_permit_list set status='1' where id='"
									+ uuid + "'";
						} else if (businessid.equals("3")) {// 固定收益行情许可
							sqlsync = "update SYNC_FIXEDINCOME_PERMIT_LIST set status='1' where id='"
									+ uuid + "'";
						} else if (businessid.equals("4")) {// 指数授权
							sqlsync = "update sync_website_sse_permit_list set status='1' where id='"
									+ uuid + "'";
						} else if (businessid.equals("5")) {// Level-2行情展示许可

							sqlsync = "update sync_website_lv2_permit_list set status='1' where id='"
									+ uuid + "'";
						} else if (businessid.equals("6")) {// 或Level-2行情非展示许可
							sqlsync = "update SYNC_LV2DATAFEED_PERMIT_LIST set status='1' where id='"
									+ uuid + "'";
						}

					} else {
						lg.writeLog("合同信息为有效");
						RecordSet rsva = new RecordSet();

						sql = "update uf_crm_permitpub set status='0' where customer='"
								+ customer + "' and business='"+businessid+"'";
						rsva.executeSql(sql);
						// 更新中间表0--level1 1--level2

						if (businessid.equals("1")) { // Level-1行情许可
							sqlsync = "update sync_website_lv1_permit_list set status='0' where id='"
									+ uuid + "'";
						} else if (businessid.equals("2")) {// 期权行情展示许可
							sqlsync = "update sync_website_opt_permit_list set status='0' where id='"
									+ uuid + "'";
						} else if (businessid.equals("3")) {// 固定收益行情许可
							sqlsync = "update SYNC_FIXEDINCOME_PERMIT_LIST set status='0' where id='"
									+ uuid + "'";
						} else if (businessid.equals("4")) {// 指数授权
							sqlsync = "update sync_website_sse_permit_list set status='0' where id='"
									+ uuid + "'";
						} else if (businessid.equals("5")) {// Level-2行情展示许可

							sqlsync = "update sync_website_lv2_permit_list set status='0' where id='"
									+ uuid + "'";
						} else if (businessid.equals("6")) {// 或Level-2行情非展示许可
							sqlsync = "update SYNC_LV2DATAFEED_PERMIT_LIST set status='0' where id='"
									+ uuid + "'";
						}
					}
					try {
						exchangeDB.executeSql(sqlsync);
						lg.writeLog("中间库更新成功");
					} catch (Exception e) {
						lg.writeLog("中间库更新异常e----" + e);
					}
				}
			}
		} catch (Exception e) {
			lg.writeLog("检索出现异常e---" + e);
		}

	}
}
