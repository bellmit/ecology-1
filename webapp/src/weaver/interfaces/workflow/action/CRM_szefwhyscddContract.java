package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 流程归档根据客户业务修改合同状态
 * 
 * @author lsq
 * @date 2019/7/29
 * @version 1.0
 */
public class CRM_szefwhyscddContract extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			String requestid = request.getRequestid();
			String sql = "select ifcover,contract from 	formtable_main_102 where requestId='"
					+ requestid + "'";
			rs.executeSql(sql);
			if (rs.next()) {
				String ifcover = Util.null2String(rs.getString("ifcover")); // 是否覆盖历史数据
				if (ifcover.equals("0")) {
					String contract = Util.null2String(rs
							.getString("contract")); // 关联合同
					String sql1 = "select count(*) as 'count' from uf_crm_contract where id='"
							+ contract + "'";
					rs1.executeSql(sql1);
					String count = "";
					if (rs1.next()) {
						count = Util.null2String(rs1.getString("count"));
					}
					if (!count.equals("0")) {
						String sql2 = "update uf_crm_contract set status='1' where id='"+contract+"'";
						rs2.executeSql(sql2);
					} else {
						writeLog("合同信息库中未有该合同信息");
					}
				} else {
					writeLog("不修改历史合同订单");
				}
			}
		} catch (Exception e) {
			writeLog("流程归档根据客户业务修改合同状态异常:" + e);
			return "0";
		}
		return "1";
	}
}
