package weaver.interfaces.workflow.action;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;


public class InformationShow_LC extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
			RecordSet rs = new RecordSet();
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select * from formtable_main_120 where requestid = "+requestid);
            Double ContractMoney3=1000000.00;
			
            if(rs.next()){

    			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");  
				String LCid=Util.null2String(rs.getString("id"));
writeLog("流程id:"+LCid);
				
				RecordSet rscontract = new RecordSet();
				rscontract.executeSql("select * from formtable_main_120_dt1 where id='"+LCid+"'");
				
				while(rscontract.next()){
					//合同最终金额
					String ContractMoney = Util.null2String(rscontract.getString("je"));
writeLog("合同最终金额:"+ContractMoney);
					Double ContractMoney1=Double.valueOf(ContractMoney);
					//String ContractMoney2=String.format("%.2f", ContractMoney1);	
writeLog("合同最终金额:"+ContractMoney1);


					if(ContractMoney1>ContractMoney3){
						//合同名称
						String ContractName = Util.null2String(rscontract.getString("htmc"));
writeLog("合同名称:"+ContractName);

						//合同对方
						String ContractOther = Util.null2String(rscontract.getString("htdf"));
writeLog("合同名称:"+ContractOther);	

						//合同签订时间
						String ContractSignedTime = Util.null2String(rscontract.getString("htdf"));
writeLog("合同名称:"+ContractOther);
			
						//签约方(OA内只有信息公司)
						String ContractInformationCompany = "上证所信息网络有限公司";
						
						//获取系统时间，作为写入数据时间
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						
						//生成UUID作为ID
						String id = UUID.randomUUID().toString();
						
						String sql = "insert into sync_website_lv2_permit_list "
							+ " (ID,A1,A2,A3,A4,A5) "
							+ " VALUES "
							+ " ('"+id+"' , '"+ContractName+"' , '"+ContractOther+"', '"+ContractSignedTime+"' , '"+ContractInformationCompany+"' , '"+sdf+"' ) ";
							
						//将数据插入中间表
						try {
							exchangeDB.executeSql(sql);
							writeLog("insert语句："+ sql);
							
							
						} catch (Exception e) {
							writeLog("插入sync_website_lv2_permit_list出错："+e);
						}
						
						
						
					}
				}
			}			
		}catch (Exception e) {
			writeLog("未插入数据："+e);
		}
	return null;
			
	}
}
