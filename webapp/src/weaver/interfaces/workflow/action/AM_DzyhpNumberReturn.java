package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.soa.workflow.request.RequestInfo;

public class AM_DzyhpNumberReturn extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("低值易耗品入库编号反写--开始");
      RecordSet rs = new RecordSet();
      String billid = request.getRequestid();    
      
      String sql="update uf_am_artcles_dt1 set assetno=c.assetno  from uf_am_artcles a,uf_am_artcles_dt1 b,uf_am_articles c where a.id=b.mainid and a.requestid=c.flowid and b.uuid=c.uuid and a.requestid="+billid;
      rs.executeSql(sql);      
      writeLog("低值易耗品入库编号反写--成功");
    }
    catch (Exception e) {
      writeLog("低值易耗品入库编号反写--出错" + e);
      return "0";
    }
    return "1";
  }  
}