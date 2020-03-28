package weaver.interfaces.workflow.action;

import java.util.Calendar;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

/**
 * 每月提醒客户经理
 * 
 * @author lsq
 * @date 2019/4/26
 * @version 1.0
 */
public class RemindManagerMonth extends BaseCronJob {
	BaseBean lg = new BaseBean();
	SelectSYFJ sc = new SelectSYFJ();
	RecordSet rs = new RecordSet();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
			+ Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-"
			+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);

	// 0 0 0 30 12 ? * 每年12月30日 0点 执行一次
	// 0 0 0 1/1 * ? * 每天执行一次
	// 0 0 0 27 1/1 ? * 每月27号执行一次
	// 通过继承BaseCronJob类可以实现定时同步
	public void execute() {
		try {
			create();
		} catch (Exception e) {
			lg.writeLog("创建流程异常e:" + e);
		}
	}

	private void create() {

		String sql = "SELECT  distinct  custommanager FROM  Matrixtable_7 WHERE custommanager not in(SELECT  custommanager FROM  Matrixtable_7 WHERE custommanager='')";
		rs.executeSql(sql);
		while (rs.next()) {
			String title = "请及时创建下月走访计划";
			String status = "0";
			String custommanager = Util.null2String(rs
					.getString("custommanager"));
			String[] strarr = custommanager.split(",");
			for (int i = 0; i < strarr.length; i++) {
				RecordSet rs4 = new RecordSet();
				sql = "SELECT count(*) AS 'count' FROM uf_crm_notice WHERE title='"+title+"' AND  person='"+strarr[i]+"' AND noticedate='"+syndate+"'";
				rs4.executeSql(sql);
				String count = "";
				if (rs4.next()) {
					count = Util.null2String(rs4.getString("count"));
				}
				if (count.equals("0")) {
					RecordSet rs1 = new RecordSet();
					int modeId = 131; // 建模id
					UUID uuid = UUID.randomUUID();
					String wyid = uuid.toString();// 生成唯一的标识码

					// 插入变更记录
					sql = "insert into uf_crm_notice(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
							+ "title,business,customer,person,noticedate,status,uuid) values ("
							+ modeId
							+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
							+ "'"
							+ title
							+ "',null,null,'"
							+ strarr[i]
							+ "','"
							+ syndate
							+ "','"
							+ status
							+ "','"
							+ wyid
							+ "')";
					rs1.executeSql(sql);
					RecordSet rs2 = new RecordSet();
					sql = "select id from uf_crm_notice where uuid='" + wyid
							+ "'";
					rs2.executeSql(sql);
					if (rs2.next()) {
						String id = Util.null2String(rs2.getString("id"));// 查询出资产变更记录
						int logid = Integer.parseInt(id);
						ModeRightInfo ModeRightInfo = new ModeRightInfo();
						ModeRightInfo.editModeDataShare(5, modeId, logid);// 新建的时候添加共享-所有人
					}

				}

				lg.writeLog("新增提醒信息sql:" + sql);
			}

		}
	}
}
