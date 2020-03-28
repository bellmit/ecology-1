package weaver.interfaces.workflow.action;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class CRM_SsgsPgjkHtbh extends BaseBean
implements Action
{
	public String execute(RequestInfo request)
	{
		try
		{
			writeLog("配股缴款统计服务订单申请流程--开始");
			RecordSet rs = new RecordSet();	
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			String billid = request.getRequestid();
			String sql="";
			String sql2="";
			String sql3="";
			
			sql="select uuid from formtable_main_55 where requestid="+billid;
			writeLog("配股缴款统计服务订单申请流程--sql___"+sql);
			rs.executeSql(sql);
			if (rs.next()) {
				String  uuid = Util.null2String(rs.getString("uuid"));
				
				if(!uuid.equals("")){
					
					sql2="select ordernumber from uf_crm_orderinfo where uuid='"+ uuid + "' and flowid='"+ billid + "' ";
					writeLog("配股缴款统计服务订单申请流程--sql___"+sql2);
					rs2.executeSql(sql2);
					if(rs2.next()){
						String  ordernumber = Util.null2String(rs2.getString("ordernumber"));
						if(!ordernumber.equals("")){
							//更新合同信息表合同编号=订单编号
							sql3="update uf_crm_contract set "
								+ " contractno = '"+ordernumber+"'"
								+ "  where uuid = '"+uuid+"'"
								+ " and flowid = '"+billid+"'";
							rs3.executeSql(sql3);
							writeLog("更新配股缴款统计服务合同编号结束___"+sql3);
						}
						
					}
				}
				
				
			}   
		}
		catch (Exception e) {
			writeLog("配股缴款统计服务订单申请流程--出错" + e);
			return "0";
		}
		return "1";
	}
}