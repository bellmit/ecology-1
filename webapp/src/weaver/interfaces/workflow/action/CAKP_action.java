package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//开票，数据提送到金蝶（中间表）
public class CAKP_action extends BaseBean implements Action {
public String execute(RequestInfo request){
	  try{
			RecordSet rs = new RecordSet();
			RecordSet lqys = new RecordSet();
			RecordSet ddss = new RecordSet();
			String billid  = request.getRequestid();
            rs.executeSql("select * from uf_CAKP1 where id = "+billid);
            writeLog("执行的语句为：select * from uf_CAKP1 where id = "+billid );
            if(rs.next()){		
            	//收款时，选择订单号，带出上游id,根据收款表的syid查出订单表的wyid，并将wyid插入到SYNC_FINANCE_RECORD的bill_id中
            	String caxq=Util.null2String(rs.getString("caxq"));	
            	String wyid =Util.null2String(rs.getString("wyid"));
            	lqys.executeSql("select * from uf_CAOrderOne where id = '"+caxq+"'");
            	//订单表的唯一id
            	String ddwyid="";
            	Boolean ff=true;
            	String PRODUCT ="CA授权服务费";
            	if(lqys.next()){	
            		ddwyid= Util.null2String(lqys.getString("payNum"));
            		if(wyid.equals("")){
            			wyid=ddwyid;
            		}else{
            			ff=false;
            		}
            		PRODUCT = Util.null2String(lqys.getString("bizType"));
    				
            	}
            	
                String ORDER_DATE=Util.null2String(rs.getString("dgsj"));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String createDate = sdf.format(new Date());
				String ss=createDate.substring(11, 19);
				ORDER_DATE = ORDER_DATE + " " +ss;
				
				String BILL_AMOUNT = Util.null2String(rs.getString("fpje"));	
				Double dd=Double.valueOf(BILL_AMOUNT);
           	 String cc=String.format("%.2f", dd);
				String fppzh = Util.null2String(rs.getString("fppzh"));
				//sid1为voucher的流水号，格式为YSID年月日+4位流水号
				String SID=Util.null2String(rs.getString("sid1"));
				String fptt=Util.null2String(rs.getString("fptt"));
				String fpsj=Util.null2String(rs.getString("fpsj"));
				fpsj = fpsj + " " +ss;
				//发票内容不传
				//String fpnr=Util.null2String(rs.getString("fpnr"));
				//查询公司信息	
				RecordSetDataSource sdd = new RecordSetDataSource("financeTest");
				
				
				
				String STATUS="0";


            	//往sync_finance_invoice表写入数据
            	String sqlcc="insert into SYNC_FINANCE_INVOICE (";
				String sqldd=") values (";
				//ssid为插入invoice的流水号
				if(SID !=""){
					sqlcc +="ID,";
					sqldd +="'"+SID+"',";
				}
				//收款单编号
				if(wyid !=""){
					sqlcc +="BILL_ID,";
					sqldd +="'"+wyid+"',";
				}
				//发票金额
				if(cc !=""){
					sqlcc +="INVOICE_AMOUNT,";
					sqldd +="'"+cc+"',";
				}
				//发票抬头
				if(fptt !=""){
					sqlcc +="CUSTOMER,";
					sqldd +="'"+fptt+"',";
				}
				//发票时间
				if(fpsj !=""){
					sqlcc +="INVOICE_DATE,";
					sqldd +="'"+fpsj+"',";
				}
				//发票内容
				if(PRODUCT !=""){
					sqlcc +="PRODUCT,";
					sqldd +="'"+PRODUCT+"',";
				}
				//状态
				
				if(ORDER_DATE !=""){
					sqlcc +="STEP1_TIME,";
					sqldd +="'"+ORDER_DATE+"',";
				}
				//发票凭证号
				if(fppzh !=""){
					sqlcc +="INVOICE_NO,";
					sqldd +="'"+fppzh+"',";
				}
				if(STATUS !=""){
					sqlcc +="STATUS";
					sqldd +="'"+STATUS+"')";
				}
				//发票总金额
				sdd.executeSql(sqlcc+sqldd);
            	writeLog(sqlcc+sqldd);
            	sdd.executeSql("select * from SYNC_FINANCE_INVOICE where BILL_ID ='"+wyid+"'");
            	if(sdd.next()){
            		//根据ff的值判断本次开票是订单表还是订单明细表
            		//如果ff==true，表示为订单表，则无需操作，如果ff==false，表示为收款计划，则需要更新收款计划的字段sfykp的值
            		/*if(ff==false){
            			lqys.executeSql("update uf_ddcx_dt1 set sfykp = '1' where wyid = '"+wyid+"'");
                    	writeLog("update uf_ddcx_dt1 set sfykp = '1' where wyid = '"+wyid+"'");
            		}*/
            		//OA表中赋值唯一id,并更新status为1，表示开票信息已经成功插入mysql开票表中
            		ddss.executeSql("update uf_CAKP1 set status = '1',wyid='"+wyid+"' where id = "+billid);
                	writeLog("update uf_CAKP1 set status = '1',wyid='"+wyid+"' where id = "+billid);
	            }
            	
				
            }
        }catch(Exception e){
            writeLog("开票接口出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }

}
