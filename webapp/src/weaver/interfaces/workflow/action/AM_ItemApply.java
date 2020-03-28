package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class AM_ItemApply extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("消耗品领用--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();
      RecordSet rs2 = new RecordSet();
      String billid = request.getRequestid();      
      String itemname="";//物品名称
      int amount=0;//领用数量
      int flag=0;//判断是否有库存不足的物品
      String sql="select * from uf_am_itemapply_dt1 where mainid =(select id from uf_am_itemapply where requestid="+billid+")";
      rs.executeSql(sql);
      while (rs.next()) {
    	  itemname=Util.null2String(rs.getString("itemname"));  
    	  amount =Integer.parseInt(Util.null2String(rs.getString("amount")));
    	  
    	  int remaindamount=0;//剩余数量
    	  sql="select remaindamount from uf_am_lowitem where itemname='"+itemname+"'";
          rs1.executeSql(sql);
          if (rs1.next()) {
        	  remaindamount =Integer.parseInt(Util.null2String(rs1.getString("remaindamount")));          
          }
          
          if(amount>remaindamount){
        	  flag++;         	  
          }         
       }
      
      if(flag>0){
    	  writeLog("消耗品库存不足");
          return "0";                   	  
      }else{
    	  sql="update uf_am_lowitem set useamount=a.useamount+c.amount,remaindamount=a.remaindamount-c.amount from uf_am_lowitem a,uf_am_itemapply b,uf_am_itemapply_dt1 c where b.id=c.mainid and c.itemname=a.itemname and b.requestid="+billid;
    	  rs2.executeSql(sql); 
      } 
      
      writeLog("消耗品领用--成功");
    }
    catch (Exception e) {
      writeLog("消耗品领用--出错" + e);
      return "0";
    }
    return "1";
  }  
}