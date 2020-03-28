package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;
/**
 * 基础费用付款 --财务出纳节点创建台账日期
 * @author lsq
 * @version 1.0
 * @dare 2019/9/4
 */
public class CRM_baseResourecePay extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
			RecordSet rs=new RecordSet();
			String requestid=Util.null2String(request.getRequestid());
			String sql="update formtable_main_354_dt2 set createDate=convert(varchar(10),getdate(),120) " +
					"where mainid=(select id from formtable_main_354 where requestid='"+requestid+"')";
			rs.executeSql(sql);

		} catch (Exception e) {
			writeLog("基础费用付款 --财务出纳节点创建台账日期异常:"+e);
			return "0";
		}
		return "1";
	}

}
