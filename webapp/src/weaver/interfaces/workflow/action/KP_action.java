package weaver.interfaces.workflow.action;
//import java.text.SimpleDateFormat;
//import java.util.Date;

import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//开票，数据提送到金蝶（中间表）
public class KP_action extends BaseBean implements Action {
public String execute(RequestInfo request){
	writeLog("开票接口执行execute方法" );
	  try{
		  writeLog("开票接口执行try代码块" );
			RecordSet rs = new RecordSet();
			//
			String billid  = request.getRequestid();
			writeLog("开票接口的OA表id为："+billid );
            rs.executeSql("select * from formtable_main_60 where id = "+billid);
            writeLog("执行的语句为：select * from formtable_main_60 where id = "+billid );
            if(rs.next()){		
            	writeLog("开票接口开始");
            	String DEADLINE=Util.null2String(rs.getString("ysrq"));
            	//因为有的流程没有上游id，如果没有id，中间表插入不进去，所以我自己创建唯一id（uuid）
            	UUID uuid = UUID.randomUUID();
        		String wyid =uuid.toString();
            	//String ID=Util.null2String(rs.getString("syid"));	
            	String TITLE = Util.null2String(rs.getString("bt"));
            	String CONTRACT_CODE=Util.null2String(rs.getString("htbh"));
                String ORDER_DATE=Util.null2String(rs.getString("dgsj"));
				String RECEIVED_AMOUNT=Util.null2String(rs.getString("ysdje"));
				//业务类型，新增开票显示模板不显示，值不传
				//订单选取，只是一个触发字段，带出对应的订单信息
				String CUSTOMER= Util.null2String(rs.getString("khmc"));	
				//String PRODUCT = Util.null2String(rs.getString("gmcp"));
				String BILL_AMOUNT = Util.null2String(rs.getString("fpje"));		
				
				
				String SID=Util.null2String(rs.getString("id"));
				String CUSTOMER_CODE=Util.null2String(rs.getString("gsid"));
				//查询公司信息
				RecordSetDataSource ss = new RecordSetDataSource("exchangeDB");				
				String sql = "select * from SYNC_COMPANY where ID='"+CUSTOMER_CODE+"'";
				String OPERATOR="";
				String OPERATOR_TEL="";
				String LINKMAN="";
				String LINKMAN_TEL="";
				String LINKMAN_MOBILE="";
				String LINKMAN_EMAIL="";
				ss.executeSql(sql);
				while(ss.next()){
					OPERATOR=Util.null2String(ss.getString("OPERATOR"));
					OPERATOR_TEL=Util.null2String(ss.getString("OPERATOR_TEL"));
					LINKMAN=Util.null2String(ss.getString("LINKMAN"));
					LINKMAN_TEL=Util.null2String(ss.getString("LINKMAN_TEL"));
					LINKMAN_MOBILE=Util.null2String(ss.getString("LINKMAN_MOBILE"));
					LINKMAN_EMAIL=Util.null2String(ss.getString("LINKMAN_EMAIL"));
				}
				
				
				
				String STATUS="1";
//				String sqlcom="insert into SYNC_FINANCE_VOUCHER" +
//						"(ID,TITLE,CUSTOMER,PRODUCT,BILL_AMOUNT,CONTRACT_CODE,SID," +
//						"CUSTOMER_CODE,OPERATOR,OPERATOR_TEL,LINKMAN,LINKMAN_TEL," +
//						"LINKMAN_MOBILE,LINKMAN_EMAIL,ORDER_DATE,DEADLINE,RECEIVED_AMOUNT," +
//						"STATUS1,STATUS2,STATUS) values ('" +
//						ID+"','"+TITLE+"','"+CUSTOMER+"','"+PRODUCT+"','"+BILL_AMOUNT+"','"+CONTRACT_CODE+"','"+SID
//						+"','"+CUSTOMER_CODE+"','"+OPERATOR+"','"+OPERATOR_TEL+"','"+LINKMAN+"','"+LINKMAN_TEL
//						+"','"+LINKMAN_MOBILE+"','"+LINKMAN_EMAIL+"','"+ORDER_DATE+"','"+DEADLINE+"','"+RECEIVED_AMOUNT
//						+"','"+STATUS+"','"+STATUS+"','"+STATUS+"')";
				String sqlaa="insert into SYNC_FINANCE_VOUCHER (";
				String sqlbb=") values (";
				if(wyid !=""){
					sqlaa +="ID,";
					sqlbb +="'"+wyid+"',";
				}
				if(TITLE !=""){
					sqlaa +="TITLE,";
					sqlbb +="'"+TITLE+"',";
				}
				if(CUSTOMER !=""){
					sqlaa +="CUSTOMER,";
					sqlbb +="'"+CUSTOMER+"',";
				}
//				if(PRODUCT !=""){
//					sqlaa +="PRODUCT,";
//					sqlbb +="'"+PRODUCT+"',";
//				}
				if(BILL_AMOUNT !=""){
					sqlaa +="BILL_AMOUNT,";
					sqlbb +="'"+BILL_AMOUNT+"',";
				}
				if(CONTRACT_CODE !=""){
					sqlaa +="CONTRACT_CODE,";
					sqlbb +="'"+CONTRACT_CODE+"',";
				}
				if(SID !=""){
					sqlaa +="SID,";
					sqlbb +="'"+SID+"',";
				}
				if(CUSTOMER_CODE !=""){
					sqlaa +="CUSTOMER_CODE,";
					sqlbb +="'"+CUSTOMER_CODE+"',";
				}
				if(OPERATOR !=""){
					sqlaa +="OPERATOR,";
					sqlbb +="'"+OPERATOR+"',";
				}
				if(OPERATOR_TEL !=""){
					sqlaa +="OPERATOR_TEL,";
					sqlbb +="'"+OPERATOR_TEL+"',";
				}
				if(LINKMAN !=""){
					sqlaa +="LINKMAN,";
					sqlbb +="'"+LINKMAN+"',";
				}
				if(LINKMAN_TEL !=""){
					sqlaa +="LINKMAN_TEL,";
					sqlbb +="'"+LINKMAN_TEL+"',";
				}
				if(LINKMAN_MOBILE !=""){
					sqlaa +="LINKMAN_MOBILE,";
					sqlbb +="'"+LINKMAN_MOBILE+"',";
				}
				if(LINKMAN_EMAIL !=""){
					sqlaa +="LINKMAN_EMAIL,";
					sqlbb +="'"+LINKMAN_EMAIL+"',";
				}
				if(ORDER_DATE !=""){
					sqlaa +="ORDER_DATE,";
					sqlbb +="to_date('"+ORDER_DATE+"','yyyy-mm-dd hh24:mi:ss'),";
				}
				if(DEADLINE !=""){
					sqlaa +="DEADLINE,";
					sqlbb +="to_date('"+DEADLINE+"','yyyy-mm-dd hh24:mi:ss'),";
				}
				if(RECEIVED_AMOUNT !=""){
					sqlaa +="RECEIVED_AMOUNT,";
					sqlbb +="'"+RECEIVED_AMOUNT+"',";
				}
				sqlaa +="STATUS1,STATUS2,STATUS";
				sqlbb +="'"+STATUS+"','"+STATUS+"','"+STATUS+"')";
				
				//往中间表写入数据
            	ss.executeSql(sqlaa+sqlbb);
            	writeLog(sqlaa+sqlbb);
            	//OA表中赋值唯一id
            	rs.executeSql("update formtable_main_60 set wyid='"+wyid+"' where id = "+billid);
            	writeLog("update formtable_main_60 set wyid='"+wyid+"' where id = "+billid);

            }
        }catch(Exception e){
            writeLog("开票接口出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }

}
