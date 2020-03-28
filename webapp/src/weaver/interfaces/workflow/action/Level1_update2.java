package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//Level1--update表sync_website_lv1_permit_list
public class Level1_update2 extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
		  writeLog("Level1申请往中间表sync_website_lv1_permit_list更新数据开始");
			RecordSet ds = new RecordSet();
			String billid  = request.getRequestid();
			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");  
            String requestid = Util.null2String(request.getRequestid());
            
            ds.executeSql("select * from uf_levelone where id = "+billid);
            
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
        				String order_type = Util.null2String(ds.getString("sqxknr"));
        				if(order_type.equals("0")){
        					order_type="实时行情";
        					}
        				if(order_type.equals("1")){
        					order_type="延时行情（30分钟以上）";
        					}
        				
        				String px=Util.null2String(ds.getString("px"));
        				
        				String sql ="update sync_website_lv1_permit_list set ";
        				if(!"".equals(syid)){
        					sql = sql + " ID='" +syid +"',";
        				}
        				if(!"".equals(zzjgdm)){
        					sql = sql + " ORGANIZATION_CODE='" +zzjgdm +"',";
        				}
        				if(!"".equals(gsmc)){
        					sql = sql + " COMPANY_NAME='" +gsmc +"',";
        				}
        				if(!"".equals(tcsj)){
        					sql = sql + " UPLOAD_TIME = to_date('"+tcsj+"','yyyy-mm-dd'),";
        				}
        				if(!"".equals(gsbj)){
        					sql = sql + " CONTENT='" +gsbj +"',";
        				}
        				if(!"".equals(xksj)){
        					sql = sql + " DEADLINE='" +xksj +"',";
        				}
        				if(!"".equals(status)){
        					sql = sql + " STATUS='" +status +"',";
        				}
        				if(!"".equals(gsid)){
        					sql = sql + " COMPANY_ID='" +gsid +"',";
        				}
        				if(!"".equals(order_type)){
        					sql = sql + " ORDER_TYPE='" +order_type +"',";
        				}
        				if(!"".equals(px)){
        					sql = sql + " SHOWORDER='" +px +"',";
        				}
        				
        				String updateSql = sql.substring(0, sql.length()-1);
        				String zzsql = updateSql +" where ID ='" +syid+ "'";
        				exchangeDB.executeSql(zzsql);
        				writeLog(zzsql);
        				writeLog("Level1申请往中间表sync_website_lv1_permit_list更新数据成功");
//        				 exchangeDB.executeSql("insert into sync_website_lv1_permit_list(ID,ORGANIZATION_CODE,"
//           						+ "COMPANY_NAME,UPLOAD_TIME,CONTENT,DEADLINE,"
//           						+ "STATUS,COMPANY_ID,ORDER_TYPE,SHOWORDER)values('"+syid+"','"+zzjgdm+
//           						"','"+gsmc+"',"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
//           						"'"+gsbj+"','"+xksj+"','"+status+"','"+gsid+
//           						"','"+order_type+"','"+px+"')");
                     
                	 
                 
				
            	
				
            }
        }catch(Exception e){
            writeLog("Level1申请往中间表sync_website_lv1_permit_list更新数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
