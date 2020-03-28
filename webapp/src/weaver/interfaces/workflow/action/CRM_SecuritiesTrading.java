package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 融资融券代征集订单编号
 * 
 * @author lsq
 * @date 2019/7/26
 * @version 1.0
 */
public class CRM_SecuritiesTrading extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
			writeLog("融资融券代征集订单流程--开始");
			RecordSet rs = new RecordSet();	
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			String billid = request.getRequestid();
			String sql="select uuid from formtable_main_53 a inner join formtable_main_53_dt1 b on a.id=b.mainid where requestid='"+billid+"'";
			rs.executeSql(sql);
			while(rs.next()){
				String uuid=Util.null2String(rs.getString("uuid"));
				if(!"".equals(uuid)){
					String sql1="select ordernumber from uf_crm_orderinfo where uuid='"+ uuid + "' and flowid='"+ billid + "' ";
					rs1.executeSql(sql1);
					if(rs1.next()){
						String ordernumber=Util.null2String(rs1.getString("ordernumber"));
						String sql2="update uf_crm_contract set  contractno = '"+ordernumber+"'  where uuid = '"+uuid+"' and flowid = '"+billid+"'";
						rs2.executeSql(sql2);
					}
				}
			}
		} catch (Exception e) {
			writeLog("配股缴款统计服务订单申请流程--出错" + e);
			return "0";
		}
		return "1";
	}

}
