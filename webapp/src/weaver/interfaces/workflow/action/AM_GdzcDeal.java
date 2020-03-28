package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class AM_GdzcDeal extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("固定资产处置--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();        
      String billid = request.getRequestid();      
      String dealtype="";//处置类型      
      String status="";//资产状态
      String sql="select dealtype from uf_am_assetdeal where requestid="+billid;
      rs.executeSql(sql);
      if (rs.next()) {
    	  dealtype=Util.null2String(rs.getString("dealtype"));    	  
       }      
      if(dealtype.equals("0")){//报废
    	  status="3";
      }else if(dealtype.equals("1")){//出售
    	  status="4";
      }else if(dealtype.equals("2")){//回购
    	  status="5";
      }      
      sql="update uf_am_fixedassets set usestatus='"+status+"'  from uf_am_assetdeal a,uf_am_assetdeal_dt1 b,uf_am_fixedassets c where a.id=b.mainid and b.assetname=c.id and a.requestid="+billid;
      
      rs1.executeSql(sql);
      writeLog("固定资产处置--成功");
    }
    catch (Exception e) {
      writeLog("固定资产处置--出错" + e);
      return "0";
    }
    return "1";
  }  
}