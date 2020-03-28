package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//收款
public class CASK_action extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
			RecordSet rs = new RecordSet();
			RecordSet lqys = new RecordSet();
			RecordSet xjhs = new RecordSet();
			String billid  = request.getRequestid();
            rs.executeSql("select * from uf_CASK where id = "+billid);
            
            if(rs.next()){				
        		
        		//收款时，选择订单号，带出上游id,根据收款表的syid查出订单表的wyid，并将wyid插入到SYNC_FINANCE_RECORD的bill_id中
            	//String syid=Util.null2String(rs.getString("syid"));	
            	String caxq=Util.null2String(rs.getString("caxq"));	
            	//String PRODUCT = Util.null2String(rs.getString("cpmc"));
            	String wyid =Util.null2String(rs.getString("wyid"));	
            	lqys.executeSql("select * from uf_CAOrderOne where id = '"+caxq+"'");
            	
            	String syid="";
            	String CUSTOMER_CODE="";
            	String PRODUCT = "";
            	//订单表的唯一id
            	String ddwyid="";
            	Boolean ff=true;
            	if(lqys.next()){	
            		CUSTOMER_CODE= Util.null2String(lqys.getString("customerCode"));
            		syid= Util.null2String(lqys.getString("syid"));
            		ddwyid= Util.null2String(lqys.getString("payNum"));
            		if(wyid.equals("")){
            			wyid=ddwyid;
            		}else{
            			ff=false;
            		}
//            		String id = Util.null2String(lqys.getString("id"));
//            		if(wyid.equals("")){
//            			xjhs.executeSql("select * from uf_ddcx_dt1 where mainid ='"+id+"'");
//            		}
            		PRODUCT=Util.null2String(lqys.getString("bizType"));
            		
            	}
            	
            	UUID uuid = UUID.randomUUID();
        		String hhid =uuid.toString();
        		
            	String BILL_ORDER = Util.null2String(rs.getString("skpzh"));
            	String RECEIVED_AMOUNT = Util.null2String(rs.getString("skje"));
            	Double dd=Double.valueOf(RECEIVED_AMOUNT);
           	    String cc=String.format("%.2f", dd);
            	String RECEIVED_DATE = Util.null2String(rs.getString("sksj"));
            	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String createDate = sdf.format(new Date());
				String sss=createDate.substring(11, 19);
				RECEIVED_DATE = RECEIVED_DATE + " " +sss;
				
				String name= Util.null2String(rs.getString("khmc"));
				/*String name="";
        		lqys.executeSql("select * from CRM_CustomerInfo where id ="+CUSTOMER);
       		    if(lqys.next()){
       			name=Util.null2String(lqys.getString("name"));
  				}*/
				
				
				String SID=Util.null2String(rs.getString("sid1"));
				
				RecordSetDataSource ss = new RecordSetDataSource("exchangeDB");				
				RecordSetDataSource sdd = new RecordSetDataSource("financeTest");
				String STATUS="0";
				String sqlaa="insert into SYNC_FINANCE_RECORD (";
				String sqlbb=") values (";
				if(hhid !=""){
					sqlaa +="ID,";
					sqlbb +="'"+hhid+"',";
				}
				sqlaa +="BILL_ORDER,";
				sqlbb +="'1',";
				if(wyid !=""){
					sqlaa +="BILL_ID,";
					sqlbb +="'"+wyid+"',";
				}
				if(BILL_ORDER !=""){
					sqlaa +="RECEIVED_NO,";
					sqlbb +="'"+BILL_ORDER+"',";
				}
				if(cc !=""){
					sqlaa +="RECEIVED_AMOUNT,";
					sqlbb += cc+",";
				}
				if(RECEIVED_DATE !=""){
					sqlaa +="RECEIVED_DATE,";
					sqlbb +="'"+RECEIVED_DATE+"',";
					sqlaa +="STEP1_TIME,";
					sqlbb +="'"+RECEIVED_DATE+"',";
				}
				if(SID !=""){
					sqlaa +="SID,";
					sqlbb +="'"+SID+"',";
				}
				if(name !=""){
					sqlaa +="CUSTOMER,";
					sqlbb +="'"+name+"',";
				}
				if(CUSTOMER_CODE !=""){
					sqlaa +="CUSTOMER_CODE,";
					sqlbb +="'"+CUSTOMER_CODE+"',";
				}
				if(PRODUCT !=""){
					sqlaa +="PRODUCT,";
					sqlbb +="'"+PRODUCT+"',";
				}
				sqlaa +="STATUS";
				sqlbb +="'"+STATUS+"')";
				sdd.executeSql(sqlaa+sqlbb);
            	writeLog(sqlaa+sqlbb);
            	//财务收款后，修改网络投票和配股缴款的付款状态，因为两个流程共用一个表，所以UPDATE一次，如果不是这两个流程，ID就为空
            	String fkztsql = "update sync_enterprise_info set PAY_STATUS=1,STATUS=2  where ID='"+syid+"'";
            	ss.executeSql(fkztsql);
            	writeLog(fkztsql);
            	sdd.executeSql("select * from SYNC_FINANCE_RECORD where BILL_ID ='"+wyid+"'");
            	if(sdd.next()){
            		//根据ff的值判断本次收款是订单表还是订单明细表
            		//如果ff==true，表示为订单表，则无需操作，如果ff==false，表示为收款计划，则需要更新收款计划的字段sfysk的值
            
            		
            		//OA表中赋值唯一id,并更新status为1，表示收款信息已经成功插入mysql收款表中
            		lqys.executeSql("update uf_CASK set status = '1',wyid='"+wyid+"' where id = "+billid);
                	writeLog("update uf_CASK set status = '1',wyid='"+wyid+"' where id = "+billid);
	            }
            	
            }
        }catch(Exception e){
            writeLog("收款接口出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
