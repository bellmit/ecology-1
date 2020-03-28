package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 行情历史数据权限开通
 * 
 * @author lsq
 * @date 2019/8/1
 * @version 1.0
 */
public class CRM_HisDataPermissOpen extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
			RecordSet rs = new RecordSet();

			String requestid = request.getRequestid();
			String sql = "select customer,business,content1,content2,content3,timelimit1,timelimit2,timelimit3,timelimit4,timelimit5,timelimit6,startdate,enddate,reminddate,openfile from uf_crm_marketdata where requestid='"
					+ requestid + "'";
			if(rs.next()){
				String customer=Util.null2String(rs.getString("customer"));     //客户
				String business=Util.null2String(rs.getString("business"));     //业务
				String content1=Util.null2String(rs.getString("content1"));     //行情一
				String content2=Util.null2String(rs.getString("content2"));     //行情二
				String content3=Util.null2String(rs.getString("content3"));     //行情三
				String timelimit1=Util.null2String(rs.getString("timelimit1")); //
				String timelimit2=Util.null2String(rs.getString("timelimit2"));
				String timelimit3=Util.null2String(rs.getString("timelimit3"));
				String timelimit4=Util.null2String(rs.getString("timelimit4"));
				String timelimit5=Util.null2String(rs.getString("timelimit5"));
				String timelimit6=Util.null2String(rs.getString("timelimit6"));
				String startdate=Util.null2String(rs.getString("startdate"));   //权限开始时间
				String enddate=Util.null2String(rs.getString("enddate"));       //权限结束时间
				String reminddate=Util.null2String(rs.getString("reminddate")); //权限到期提醒时间 
				String openfile=Util.null2String(rs.getString("openfile"));     //相关文件
				//开始生成权限
				
				
			}

		} catch (Exception e) {
			writeLog("行情历史数据权限开通异常:" + e);
			return "0";
		}
		return "1";
	}

	public void createWF(){
		
		
		
		
		
	}
}
