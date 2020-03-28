package weaver.interfaces.workflow.action;

import java.util.Calendar;
import weaver.conn.*;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;
//配股缴款数据流程
//修改审核状态
public class PGJK_DAO3 extends BaseBean implements Action{
	BaseBean lg = new BaseBean();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-" + Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);
	// 通过继承BaseCronJob类可以实现定时同步
	public String execute(RequestInfo request){ 
		try {
			RecordSet rs = new RecordSet();
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select * from formtable_main_50 where requestid = "+requestid);
            RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB"); 
            if(rs.next()){
            	 String syid=Util.null2String(rs.getString("syid"));
            	 String fwksrq=Util.null2String(rs.getString("fwksrq"));
            	 String fwjsrq=Util.null2String(rs.getString("fwjsrq"));
            	 String deadline = fwksrq +"~" +fwjsrq;
            	 String ddje=Util.null2String(rs.getString("ddje"));
            	 String sqlupdate ="";
            	 if(!"".equals(ddje)){
                	sqlupdate = "update SYNC_ENTERPRISE_INFO set APPROVAL_STATUS=1,STATUS=2,SERVICE_PERIOD='"+deadline+"',ORDER_AMOUNT="+ddje+" where ID='"+syid+"'";

            	 }else{
                	 sqlupdate = "update SYNC_ENTERPRISE_INFO set APPROVAL_STATUS=1,STATUS=2,SERVICE_PERIOD='"+deadline+"' where ID='"+syid+"'";

            	 }
            	 exchangeDB.executeSql(sqlupdate);
            	 writeLog(sqlupdate);
            	 lg.writeLog("配股缴款更新中间表审核状态成功：");
            }
			
			
		} catch (Exception e) {
			lg.writeLog("配股缴款更新中间表审核状态出错："+e);
			return "0";
		}
		return Action.SUCCESS;
	}
	
}
