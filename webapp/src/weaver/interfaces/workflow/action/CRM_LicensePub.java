package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import com.sun.corba.se.impl.oa.poa.AOMEntry;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

public class CRM_LicensePub extends BaseCronJob {
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
		try {
			lg.writeLog("开始检索许可表。。。。");
			RecordSetDataSource exchangeDB = new RecordSetDataSource(
					"exchangeDB");
			RecordSetDataSource exchangeDBs = new RecordSetDataSource(
					"exchangeDB");
			RecordSetDataSource eDB = new RecordSetDataSource(
					"exchangeDB");
			RecordSet rs = new RecordSet();
			String sql = "select  customer,business,startdate,enddate,status,dsporder,uuid,syid,gsid from uf_crm_permitpub";
			rs.executeSql(sql);
			while (rs.next()) {
				lg.writeLog("进入结果集。。。");
				String customer = Util.null2String(rs.getString("customer"));
				String businessid = Util.null2String(rs.getString("business"));
				String startdate = Util.null2String(rs.getString("startdate"));
				String enddate = Util.null2String(rs.getString("enddate"));
				String status = Util.null2String(rs.getString("status"));
				String dsporder = Util.null2String(rs.getString("dsporder"));
				String uuid = Util.null2String(rs.getString("uuid"));
				String syid = Util.null2String(rs.getString("syid"));
				String gsid = Util.null2String(rs.getString("gsid"));
				// 中间表许可期限
				String deadLine = startdate + "~" + enddate;

				RecordSet rscompany = new RecordSet();
				rscompany
						.executeSql("select name,creditcode from uf_CRM_CustomerInfo where id='"
								+ customer + "'");
				String organizationCode = "";
				String companyName = "";
				if (rscompany.next()) {
					// 公司名称
					companyName = Util.null2String(rscompany.getString("name"));
					// 组织机构代码
					organizationCode = Util.null2String(rscompany
							.getString("creditcode"));
				}
				SimpleDateFormat dateFormat_now = new SimpleDateFormat(
						"yyyy/MM/dd");
				String tcsj = dateFormat_now.format(new Date());
				String count = "";
				if (businessid.equals("1")) {
					exchangeDB
							.executeSql("select count(*) as count from sync_website_lv1_permit_list where id='"
									+ uuid + "'");
				} else if (businessid.equals("2")) {
					exchangeDB
							.executeSql("select count(*) as count from sync_website_opt_permit_list where id='"
									+ uuid + "'");
				} else if (businessid.equals("3")) {
					exchangeDB
							.executeSql("select count(*) as count from SYNC_FIXEDINCOME_PERMIT_LIST where id='"
									+ uuid + "'");
				} else if (businessid.equals("4")) {
					exchangeDB
							.executeSql("select count(*) as count from sync_website_sse_permit_list where id='"
									+ uuid + "'");
				} else if (businessid.equals("5")) {
					exchangeDB
							.executeSql("select count(*) as count from sync_website_lv2_permit_list where id='"
									+ uuid + "'");
				} else if (businessid.equals("6")) {
					exchangeDB
							.executeSql("select count(*) as count from SYNC_LV2DATAFEED_PERMIT_LIST where id='"
									+ uuid + "'");
				}
				if (exchangeDB.next()) {
					count = Util.null2String(exchangeDB.getString("count"));
				}
				lg.writeLog("count-------" + count);
				if (count.equals("0")&&uuid.length()>0) {
					if (businessid.equals("1")) { // Level-1行情许可
						sql = "insert into sync_website_lv1_permit_list(id,organization_code,company_name,upload_time,deadline,status,company_id,showorder)  values('"
								+ uuid
								+ "','"
								+ organizationCode
								+ "','"
								+ companyName
								+ "',to_date('"
								+ tcsj
								+ "','yyyy/MM/dd'),'"
								+ deadLine
								+ "','"
								+ status
								+ "','"
								+ gsid
								+ "','"
								+ dsporder
								+ "')";
					} else if (businessid.equals("2")) {// 期权行情展示许可
						sql = " insert into sync_website_opt_permit_list "
								+ " (ID,ORGANIZATION_CODE,COMPANY_NAME,UPLOAD_TIME,DEADLINE,STATUS,COMPANY_ID,SHOWORDER) "
								+ " VALUES " + " ('" + uuid + "','"
								+ organizationCode + "','" + companyName
								+ "',to_date('" + tcsj + "','yyyy/MM/dd'),'"
								+ deadLine + "','" + status + "','" + gsid
								+ "','" + dsporder + "') ";
					} else if (businessid.equals("3")) {// 固定收益行情许可

						sql = "insert into SYNC_FIXEDINCOME_PERMIT_LIST  (id,ORGANIZATION_CODE,COMPANY_ID,COMPANY_NAME,DEADLINE,STATUS,SHOWORDER)  VALUES  ('"
								+ uuid
								+ "' , '"
								+ organizationCode
								+ "' , '"
								+ gsid
								+ "' , '"
								+ companyName
								+ "' ,  '"
								+ deadLine
								+ "' , '"
								+ status
								+ "' , '"
								+ dsporder + "' ) ";

					} else if (businessid.equals("4")) {// 指数授权
						sql = "insert into sync_website_sse_permit_list (ID,ORGANIZATION_CODE,COMPANY_NAME,DEADLINE,UPLOAD_TIME,STATUS,company_id,SHOWORDER) values('"
								+ uuid
								+ "','"
								+ organizationCode
								+ "','"
								+ companyName
								+ "','"
								+ deadLine
								+ "',to_date('"
								+ tcsj
								+ "','yyyy/MM/dd'),'"
								+ status
								+ "','"
								+ gsid
								+ "','"
								+ dsporder
								+ "')";
					} else if (businessid.equals("5")) {// Level-2行情展示许可

						sql = "insert into sync_website_lv2_permit_list(id,organization_code,company_name,upload_time,permission_deadline,status,company_id,showorder)  values('"
								+ uuid
								+ "','"
								+ organizationCode
								+ "','"
								+ companyName
								+ "',to_date('"
								+ tcsj
								+ "','yyyy/MM/dd'),'"
								+ deadLine
								+ "','"
								+ status
								+ "','"
								+ gsid
								+ "','"
								+ dsporder
								+ "')";
					} else if (businessid.equals("6")) {// 或Level-2行情非展示许可
						sql = "insert into SYNC_LV2DATAFEED_PERMIT_LIST "
								+ " (id,ORGANIZATION_CODE,COMPANY_NAME ,permission_deadline,STATUS,SHOWORDER) "
								+ " VALUES " + " ('" + uuid + "' , '"
								+ organizationCode + "' , '" + companyName
								+ "' ,'" + deadLine + "' ,'" + status + "' , '"
								+ dsporder + "' ) ";
					}
					try {
						exchangeDBs.executeSql(sql);
					} catch (Exception e) {
						lg.writeLog("中间库插入出错e----" + e);
					}

				} else {    //中间库存在 ,则更新  中间库的名称
					String  sqls="";
					if (businessid.equals("1")) {
						sqls = "update sync_website_lv1_permit_list set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id='"
								+ uuid + "'";
					} else if (businessid.equals("2")) {
						sqls = "update sync_website_opt_permit_list set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id='"
								+ uuid + "'";
					} else if (businessid.equals("3")) {
						sqls = " update SYNC_FIXEDINCOME_PERMIT_LIST set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id = '" + uuid + "'";
					} else if (businessid.equals("4")) {
						sqls = "update sync_website_sse_permit_list set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id='"
								+ uuid + "'";

					} else if (businessid.equals("5")) {
						sqls = "update sync_website_lv2_permit_list set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id='"
								+ uuid + "'";
					} else if (businessid.equals("6")) {
						sqls = "update SYNC_LV2DATAFEED_PERMIT_LIST set company_name='"
								+ companyName
								+ "',status='"
								+ status
								+ "' where id='"
								+ uuid + "'";
					}

					// 更新中间表
					try {
						eDB.executeSql(sqls);
						lg.writeLog("更新成功----" + sqls);
					} catch (Exception e) {
						lg.writeLog("中间库更新异常e" + e);
					}
					
				}

			}

		} catch (Exception e) {
		}

	}
}
