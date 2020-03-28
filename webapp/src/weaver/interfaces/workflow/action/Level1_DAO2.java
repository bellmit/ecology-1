package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//Level1推送到sync_website_lv1_permit_list表中
public class Level1_DAO2 extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
		  writeLog("Level1申请往中间表sync_website_lv1_permit_list写数据开始");
			RecordSet rs = new RecordSet();
			RecordSet ss = new RecordSet();
			RecordSet ds = new RecordSet();
			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");  
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select * from formtable_main_56 where requestid = "+requestid);
            
            if(rs.next()){
            	String mainid=Util.null2String(rs.getString("id"));	
            	 rs.executeSql("select * from formtable_main_56_dt1 where mainid = "+mainid);
                 //查询明细表的多个申请订单值
                 while(rs.next()){
                	 String xqdd=Util.null2String(rs.getString("xqdd"));	
                	 ss.executeSql("select * from uf_ddcx where id = "+xqdd);
                     
                     if(ss.next()){
                    	 String sssid=Util.null2String(ss.getString("syid"));	
                    	 ds.executeSql("select * from formtable_main_34 where syid = '"+sssid +"' and requestId is null");
                    	//查询Level1许可名单的值
                         if(ds.next()){
                        	 String syid=Util.null2String(ds.getString("syid"));						
                         	String zzjgdm = Util.null2String(ds.getString("zzjgdm"));
             				String gsmc= Util.null2String(ds.getString("gsmc"));
             				String tcsj = Util.null2String(ds.getString("tcsj"));	
             				String gsbj=Util.null2String(ds.getString("gsbj"));
             				String sqxknxbegin = Util.null2String(ds.getString("sqxknxbegin"));	
            				String sqxknxend=Util.null2String(ds.getString("sqxknxend"));
            				String xksj = sqxknxbegin + "~" +sqxknxend ;
            				String status = "0";
            				String gsid=Util.null2String(ds.getString("gsid"));
            				String order_type = "实时行情";
            				String px=Util.null2String(ds.getString("px"));
                        	 exchangeDB.executeSql("insert into sync_website_lv1_permit_list(ID,ORGANIZATION_CODE,"
              						+ "COMPANY_NAME,UPLOAD_TIME,CONTENT,DEADLINE,"
              						+ "STATUS,COMPANY_ID,ORDER_TYPE,SHOWORDER)values('"+syid+"','"+zzjgdm+
              						"','"+gsmc+"',"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
              						"'"+gsbj+"','"+xksj+"','"+status+"','"+gsid+
              						"','"+order_type+"','"+px+"')");
                        	 writeLog("insert into sync_website_lv1_permit_list(ID,ORGANIZATION_CODE,"
               						+ "COMPANY_NAME,UPLOAD_TIME,CONTENT,DEADLINE,"
               						+ "STATUS,COMPANY_ID,ORDER_TYPE,SHOWORDER)values('"+syid+"','"+zzjgdm+
               						"','"+gsmc+"',"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
               						"'"+gsbj+"','"+xksj+"','"+status+"','"+gsid+
               						"','"+order_type+"','"+px+"')");
                     }
                	 
                     
                     }
                	 
                 }
				
            	
				
            }
        }catch(Exception e){
            writeLog("Level1申请往中间表sync_website_lv1_permit_list写数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
