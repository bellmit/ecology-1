package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.formmode.data.RequestInfoForAction;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 修改合同状态
 * 
 * @author lsq
 * @date 2019/3/5
 */
public class CompactStutaUpdate extends RequestInfoForAction implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
			RecordSet rs = new RecordSet();
			String requestId = Util.null2String(request.getRequestid());
			writeLog("requestid:____" + requestId);
			rs.executeSql("select * from uf_crm_contractend where requestId="
					+ requestId);
			if (rs.next()) {

				String contract = Util.null2String(rs.getString("contract"));
				writeLog("contract:_____" + contract);

				String sql = "update uf_crm_contract set status=1 where id='"
						+ contract + "'";
				rs.executeSql(sql);
				writeLog("update语句:" + sql);
				writeLog("修改状态成功");
			}

		} catch (Exception e) {
			writeLog("修改状态失败,异常是" + e);
			return Action.FAILURE_AND_CONTINUE;
		}

		return Action.SUCCESS;
	}

}
