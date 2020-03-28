package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class SPM_Blacklist extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("供应商列入黑名单--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();
      String billid = request.getRequestid();           
      String mainsql="select results,supplier,blacklistdate,enddate from uf_spm_blacklist where requestid="+billid;
      rs.executeSql(mainsql);
      if (rs.next()) {
        String  results = Util.null2String(rs.getString("results"));
        if(results.equals("0")){//通过
        	String  supplier = Util.null2String(rs.getString("supplier"));
        	String  blacklistdate = Util.null2String(rs.getString("blacklistdate"));
        	String  enddate = Util.null2String(rs.getString("enddate"));
        	String updatesql="update uf_spm_supplier set status=2,blacklistdate='"+blacklistdate+"',enddate='"+enddate+"' where id='"+supplier+"'";
        	 rs1.executeSql(updatesql); 
             writeLog("供应商列入黑名单--成功");
        }
       }      
    }
    catch (Exception e) {
      writeLog("供应商列入黑名单--出错" + e);
      return "0";
    }
    return "1";
  }
}