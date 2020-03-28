package weaver.interfaces.workflow.action;

import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 请假申请日志
 * 
 * @author lsq
 * @date 2019/5/22
 * @version 1.0
 */
public class VacateLog extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
			String requestid = Util.null2String(request.getRequestid());
			RecordSet rs = new RecordSet();
			String sql = "select * from formtable_main_19 where requestid='"
					+ requestid + "'";
			rs.executeSql(sql);
			if (rs.next()) {
				// 编号
				String bh = Util.null2String(rs.getString("bh"));
				// 开始日期
				String ksrq = Util.null2String(rs.getString("ksrq"));
				// 结束日期
				String jsrq = Util.null2String(rs.getString("jsrq"));
				// 开始时间
				String qjkssj = Util.null2String(rs.getString("qjkssj"));
				// 结束时间
				String qjsjsj = Util.null2String(rs.getString("qjsjsj"));
				String AMPM1 = Util.null2String(rs.getString("AMPM1"));
				String AMPM2 = Util.null2String(rs.getString("AMPM2"));
				UUID uuid = UUID.randomUUID();
				String wyid = uuid.toString();// 生成唯一的标识码
				sql = "insert into uf_vacatelog(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime," +
						"uuid,workflowId,vacateStartDate,vacateEndDate,vacateStartTime,vacateEndTime,startTime,endTime,flowReqid)" +
						" values(137,1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),'"
						+ wyid
						+ "','"
						+ bh
						+ "','"
						+ ksrq
						+ "','"
						+ jsrq
						+ "','"
						+ qjkssj
						+ "','"
						+ qjsjsj
						+ "','"
						+ AMPM1
						+ "','" 
						+ AMPM2 
						+ "','"
						+ requestid + "')";
				RecordSet rsd = new RecordSet();
				rsd.executeSql(sql);
				RecordSet rsds = new RecordSet();
				sql = "select id from uf_vacatelog where uuid='" + wyid + "'";
                   
				rsds.executeSql(sql);
				if (rsds.next()) {
					String id = Util.null2String(rsds.getString("id"));// 
					int logid = Integer.parseInt(id);
					ModeRightInfo ModeRightInfo = new ModeRightInfo();
					ModeRightInfo.editModeDataShare(5, 137, logid);// 新建的时候添加共享-所有人
				}
			}
		} catch (Exception e) {
			writeLog("请假申请日志出错e:" + e);
			return "0";
		}

		return "1";
	}

}
