package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.formmode.data.RequestInfoForAction;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 修改ca合同状态
 * 
 * @author lsq
 * @date 2019/3/6
 * @version 1.0
 */
public class CAStatusUpdate extends RequestInfoForAction implements Action {

	@Override
	public String execute(RequestInfo request) {

		try {
			RecordSet rs = new RecordSet();
			String requestId = Util.null2String(request.getRequestid());
			writeLog("requestId__"+requestId);
			rs.executeSql("select * from uf_CAInsideCancel where requestId="+requestId);
			if(rs.next()){
				String certificateid=Util.null2String(rs.getString("certificateId"));
				writeLog("certificateId__"+certificateid);
				String sql = "update uf_CAInsideApplyLsq set state=1 where id="+certificateid;		
				rs.executeSql(sql);
				writeLog("update语句:"+sql);
				writeLog("修改成功");
			}
			
		} catch (Exception e) {
            writeLog("修改失败，异常为:"+e);
            return Action.FAILURE_AND_CONTINUE;
		}

		return Action.SUCCESS;
	}

}
