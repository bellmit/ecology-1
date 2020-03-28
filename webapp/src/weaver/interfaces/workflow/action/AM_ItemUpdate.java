package weaver.interfaces.workflow.action;

import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class AM_ItemUpdate extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      writeLog("消耗品库存变更--开始");
      RecordSet rs = new RecordSet(); 
      RecordSet rs1 = new RecordSet(); 
      RecordSet rs2 = new RecordSet(); 
      RecordSet rs3 = new RecordSet();
      RecordSet rs4 = new RecordSet();
      RecordSet rs5 = new RecordSet();
      RecordSet rs6 = new RecordSet();
      String billid = request.getRequestid(); 
      int modeId=92; //建模id
      String itemname="";//物品名称
      String bgtype="";//变更类型
      int amount=0;//变更数量      
      String sql="select * from uf_am_itemchange where requestid="+billid;
      rs.executeSql(sql);
      if (rs.next()) {
    	  itemname=Util.null2String(rs.getString("itemname")); 
    	  bgtype=Util.null2String(rs.getString("bgtype"));  
    	  amount =Integer.parseInt(Util.null2String(rs.getString("amount")));
    	  
    	  if(bgtype.equals("0")){//增加库存
    		  int flag=0;//是否存在
        	  sql="select count(1) as flag from uf_am_lowitem where itemname='"+itemname+"'";
              rs3.executeSql(sql);
              if (rs3.next()) {
            	  flag =Integer.parseInt(Util.null2String(rs3.getString("flag")));          
              }
              if(flag>0){
            	  //更新
            	  sql="update uf_am_lowitem set amount=amount+"+amount+",remaindamount=remaindamount+"+amount+" where itemname ='"+itemname+"'";
            	  rs2.executeSql(sql);
              }else{
                  //插入
             	 UUID uuid = UUID.randomUUID(); 
             	 String  wyid =uuid.toString();//生成唯一的标识码 
             	 
                  sql="insert into uf_am_lowitem(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
                 	+"itemname,amount,useamount,remaindamount,storagedate,uuid) values "
                 	+"("+modeId+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"
                 	+"'"+itemname+"',"+amount+",0,"+amount+",convert(varchar(10),getdate(),120),'"+wyid+"')";
                 	rs4.executeSql(sql);  
                 	
                 	sql="select id from uf_am_lowitem where uuid='"+wyid+"'";
                    rs5.executeSql(sql);
                     
                   if (rs5.next()) {
             			String id = Util.null2String(rs5.getString("id"));
             			
             			int itemid=Integer.parseInt(id); 
             			
             			ModeRightInfo ModeRightInfo = new ModeRightInfo();
             			ModeRightInfo.editModeDataShare(5,modeId,itemid);//新建的时候添加共享-所有人
  	        
             		 }
              }
    		  
          }else{
        	  int remaindamount=0;//剩余数量
        	  sql="select remaindamount from uf_am_lowitem where itemname='"+itemname+"'";
              rs1.executeSql(sql);
              if (rs1.next()) {
            	  remaindamount =Integer.parseInt(Util.null2String(rs1.getString("remaindamount")));          
              }
              
              if(amount>remaindamount){
            	  writeLog("消耗品库存不足");
                  return "0";       	  
              }else{
            	  sql="update uf_am_lowitem set useamount=useamount+"+amount+",remaindamount=remaindamount-"+amount+" where itemname ='"+itemname+"'";  
            	  rs2.executeSql(sql);
              }
          }
    	  
       }
      
      writeLog("消耗品库存变更--成功");
    }
    catch (Exception e) {
      writeLog("消耗品库存变更--出错" + e);
      return "0";
    }
    return "1";
  }  
}