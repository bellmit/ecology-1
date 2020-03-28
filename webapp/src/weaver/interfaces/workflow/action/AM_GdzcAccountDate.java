package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.soa.workflow.request.RequestInfo;

public class AM_GdzcAccountDate extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("固定资产财务入账日期更新--开始");
      RecordSet rs = new RecordSet();
      String billid = request.getRequestid();    
      
      String sql="update uf_am_fixedassets set accountdate=b.accountdate  from uf_am_storage a,uf_am_storage_dt1 b,uf_am_fixedassets c where a.id=b.mainid and a.requestid=c.flowid and b.uuid=c.uuid and a.requestid="+billid;
      rs.executeSql(sql);      
      writeLog("固定资产财务入账日期更新--成功");
    }
    catch (Exception e) {
      writeLog("固定资产财务入账日期更新--出错" + e);
      return "0";
    }
    return "1";
  }  
}