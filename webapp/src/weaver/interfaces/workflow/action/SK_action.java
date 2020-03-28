package weaver.interfaces.workflow.action;
//import java.text.SimpleDateFormat;
//import java.util.Date;

import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//收款
public class SK_action extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
			RecordSet rs = new RecordSet();
			String billid  = request.getRequestid();
			writeLog("收款接口的OA表id为："+billid);
            rs.executeSql("select * from formtable_main_61 where id = "+billid);
            
            if(rs.next()){				
            	writeLog("收款接口开始");
            	//因为有的流程没有上游id，如果没有id，中间表插入不进去，所以我自己创建唯一id（uuid）
            	UUID uuid = UUID.randomUUID();
        		String wyid =uuid.toString();
            	String ID=Util.null2String(rs.getString("syid"));	
            	String BILL_ID = Util.null2String(rs.getString("skpzh"));
            	String RECEIVED_AMOUNT = Util.null2String(rs.getString("skje"));
            	String RECEIVED_DATE = Util.null2String(rs.getString("sksj"));
				String CUSTOMER= Util.null2String(rs.getString("khmc"));
				String CUSTOMER_CODE=Util.null2String(rs.getString("gsid"));
				String PRODUCT = Util.null2String(rs.getString("cpmc"));	
				
				//String CONTRACT_CODE=Util.null2String(rs.getString("htbh"));
				//String CONTRACT_PERSONER=Util.null2String(rs.getString("htfzr"));
				//String sid=Util.null2String(rs.getString("id"));
				
				RecordSetDataSource ss = new RecordSetDataSource("exchangeDB");				
				
				String STATUS="1";
//				String sqlcom="insert into SYNC_FINANCE_RECORD" +
//						"(ID,BILL_ID,RECEIVED_AMOUNT,RECEIVED_DATE,CUSTOMER,CUSTOMER_CODE,PRODUCT," +
//						"STATUS) values ('" +
//						ID+"','"+BILL_ID+"','"+RECEIVED_AMOUNT+"','"+RECEIVED_DATE+"','"+CUSTOMER+"','"+CUSTOMER_CODE+"','"+PRODUCT
//						+"','"+STATUS+"')";
				String sqlaa="insert into SYNC_FINANCE_RECORD (";
				String sqlbb=") values (";
				if(wyid !=""){
					sqlaa +="ID,";
					sqlbb +="'"+wyid+"',";
				}
				if(BILL_ID !=""){
					sqlaa +="BILL_ID,";
					sqlbb +="'"+BILL_ID+"',";
				}
				if(RECEIVED_AMOUNT !=""){
					sqlaa +="RECEIVED_AMOUNT,";
					sqlbb +="'"+RECEIVED_AMOUNT+"',";
				}
				if(RECEIVED_DATE !=""){
					sqlaa +="RECEIVED_DATE,";
					sqlbb +="to_date('"+RECEIVED_DATE+"','yyyy-mm-dd hh24:mi:ss'),";
				}
				if(CUSTOMER !=""){
					sqlaa +="CUSTOMER,";
					sqlbb +="'"+CUSTOMER+"',";
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
            	ss.executeSql(sqlaa+sqlbb);
            	writeLog(sqlaa+sqlbb);
            	//财务收款后，修改网络投票和配股缴款的付款状态，因为两个流程共用一个表，所以UPDATE一次，如果不是这两个流程，ID就为空
            	String fkztsql = "update sync_enterprise_info set PAY_STATUS=1  where ID='"+ID+"'";
            	ss.executeSql(fkztsql);
            	writeLog(fkztsql);
            	
            	//OA表中赋值唯一id
            	rs.executeSql("update formtable_main_61 set wyid='"+wyid+"' where id = "+billid);
            	writeLog("update formtable_main_61 set wyid='"+wyid+"' where id = "+billid);
            }
        }catch(Exception e){
            writeLog("收款接口出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
