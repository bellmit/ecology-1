package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class SPM_Glcg1 extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("管理候选供应商插入报价名细表--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();
      RecordSet rs2 = new RecordSet();
      String billid = request.getRequestid();
      String id ="";
      String suppliers ="";      
      String sql="select id,supplier from formtable_main_136 where requestid="+billid;
      rs.executeSql(sql);
      if (rs.next()) {
          id = Util.null2String(rs.getString("id"));
          suppliers = Util.null2String(rs.getString("supplier"));
          String[] supplier = suppliers.split(",");
          
          for (int i = 0; i < supplier.length; i++) {
           int flag=0;	  
           sql="select count(1) as flag from formtable_main_136_dt2  where mainid='"+id+"' and supplier='"+supplier[i]+"'";
           rs1.executeSql(sql);
           if (rs1.next()) {
         	  flag =Integer.parseInt(Util.null2String(rs1.getString("flag")));          
           }
           if(flag==0){
        	   sql="insert into formtable_main_136_dt2(mainid,supplier) values('"+id+"','"+supplier[i]+"')";
        	   rs2.executeSql(sql);
           }
          }
          writeLog("管理候选供应商插入报价名细表--成功");
       }      
    }
    catch (Exception e) {
      writeLog("管理候选供应商插入报价名细表--出错" + e);
      return "0";
    }
    return "1";
  }
}