package weaver.interfaces.workflow.action;

import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class AM_ItemStorage extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("消耗品入库--开始");
      RecordSet rs = new RecordSet();
      RecordSet rs1 = new RecordSet();
      RecordSet rs2 = new RecordSet();
      
      String billid = request.getRequestid();
      int modeId=92; //建模id 
      String itemname="";//物品名称      
      String brand="";//品牌
      String specs="";//型号
      int amount=0;//数量
      String sql="select * from uf_am_itemstorage_dt1 where mainid =(select id from uf_am_itemstorage where requestid="+billid+")";
      rs1.executeSql(sql);
      while (rs1.next()) {
    	  
    	  itemname=Util.null2String(rs1.getString("itemname"));    	  
    	  brand=Util.null2String(rs1.getString("brand"));
    	  specs=Util.null2String(rs1.getString("specs"));
    	  amount =Integer.parseInt(Util.null2String(rs1.getString("amount")));
    	  
    	  
    	  int flag=0;//是否存在
    	  sql="select count(1) as flag from uf_am_lowitem where itemname='"+itemname+"'";
          rs.executeSql(sql);
          if (rs.next()) {
        	  flag =Integer.parseInt(Util.null2String(rs.getString("flag")));          
          }
          
          
          
          if(flag>0){
        	//更新
        	 sql="update uf_am_lowitem set amount=amount+"+amount+",remaindamount=remaindamount+"+amount+",brand='"+brand+"',specs='"+specs+"',storagedate=convert(varchar(10),getdate(),120) where itemname='"+itemname+"'";
             rs.executeSql(sql);         	  
          }else{
            //插入
        	 UUID uuid = UUID.randomUUID(); 
        	 String  wyid =uuid.toString();//生成唯一的标识码 
        	 
             sql="insert into uf_am_lowitem(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
            	+"itemname,brand,specs,amount,useamount,remaindamount,storagedate,uuid) values "
            	+"("+modeId+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
            	+"'"+itemname+"','"+brand+"','"+specs+"',"+amount+",0,"+amount+",convert(varchar(10),getdate(),120),'"+wyid+"')";
            	rs.executeSql(sql);  
            	
            	sql="select id from uf_am_lowitem where uuid='"+wyid+"'";
                rs.executeSql(sql);
                
              if (rs.next()) {
        			String id = Util.null2String(rs.getString("id"));
        			
        			int itemid=Integer.parseInt(id); 
        			
        			ModeRightInfo ModeRightInfo = new ModeRightInfo();
        			ModeRightInfo.editModeDataShare(5,modeId,itemid);//新建的时候添加共享-所有人       			
  	        
        		 }        	  
          }
          
       }     
      
      writeLog("消耗品入库--成功");
    }
    catch (Exception e) {
      writeLog("消耗品入库--出错" + e);
      return "0";
    }
    return "1";
  }  
}