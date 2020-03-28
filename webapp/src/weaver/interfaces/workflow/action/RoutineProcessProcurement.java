package weaver.interfaces.workflow.action;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.math.BigDecimal;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
import java.util.UUID;

public class RoutineProcessProcurement  extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{

			RecordSet rs = new RecordSet();
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select a.id as ZBid,b.* from formtable_main_136 a,formtable_main_136_dt1 b where a.id=b.mainid and a.requestid = "+requestid );
            while(rs.next()){
    			RecordSetDataSource SSEWebDB = new RecordSetDataSource("SSEWebDB"); 
					
				
				//主表id
				String id = Util.null2String(rs.getString("ZBid"));
				//明细表id
				String mxid = Util.null2String(rs.getString("id"));	
				//uuid
				String uuid4Change = Util.null2String(rs.getString("uuid_importantBussiness"));				
				//合同名称
				String CONTRACT_NAME = Util.null2String(rs.getString("htmc"));				
				//供应商
				String SUPPLIER = Util.null2String(rs.getString("htdf"));		
				//签约方
				String MAIN_PARTY = Util.null2String(rs.getString("MAIN_PARTY"));		
				//合同签订日期
				String CONTACT_DATE = Util.null2String(rs.getString("htqdrq"));
				//是否网站展示
				String IS_SHOW = Util.null2String(rs.getString("IS_SHOW"));	
				String IS_SHOWS = "0";
				
				//采购方式
				String BuyTypeConvertDetail = Util.null2String(rs.getString("BuyTypeConvertDetail"));	
writeLog("采购方式:____"+BuyTypeConvertDetail);
				
				//数据来源
				String DATA_SOURCE = "SSEINFO";
				//金额
				String je = Util.null2String(rs.getString("je"));			
				BigDecimal je0 = new BigDecimal(je);			
				BigDecimal je1 = new BigDecimal(1000000.00);
			
				//为日志打印准备
				String Worlds=SUPPLIER+"_"+je;
			
				//uuid若为空，生成新uuid
				String uuid4 = Util.null2String(rs.getString("uuid_importantBussiness"));
writeLog("uuid4_1:____"+uuid4);
				if(uuid4.equals("")){
					uuid4 = UUID.randomUUID().toString();	
				}
writeLog("uuid4_2:____"+uuid4);
				//是否展示
				String isShow = Util.null2String(rs.getString("IS_SHOW"));
				//中间表状态,表示展示。
				String status = "0";
				if(isShow.equals("1")){
					status = "-1" ;
				}
				
writeLog("准备进入1_金额判断");
			 if(je0.compareTo(je1)>-1){
writeLog("金额判断成功大于等于100万____"+je0);
writeLog("准备进入2_UUID判断");
				if("".equals(uuid4Change)){
writeLog("UUID判断为空____"+uuid4Change);
writeLog("准备进入3_是否显示判断");
					if(IS_SHOW.equals(IS_SHOWS)){
writeLog("是显示____"+IS_SHOW);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String createD = sdf.format(new Date());

							String sql = " insert into MATERIAL_CONTRACT_PUB "
									+ " (APPLY_ID,CONTRACT_NAME,SUPPLIER,MAIN_PARTY,CONTACT_DATE,PUR_METHOD,CONTRACT_MONEY,IS_SHOW,DATA_SOURCE,STATUS,WRITE_TIME) "
									+ " VALUES "					
									+ " ('"+uuid4+"','"+CONTRACT_NAME+"','"+SUPPLIER+"','"+MAIN_PARTY+"',"+"to_date('"+CONTACT_DATE+"','yyyy-mm-dd'),'"+BuyTypeConvertDetail+"','"+je+"','"+IS_SHOW+"','"+DATA_SOURCE+"',0,'"+createD+"') ";
							try {
								SSEWebDB.executeSql(sql);
								writeLog("insert语句："+ sql);
								writeLog("insert语句写入结束");
							} catch (Exception e) {
								writeLog("插入MATERIAL_CONTRACT_PUB出错："+e);
								return Action.FAILURE_AND_CONTINUE;
							}
							

writeLog("进入5_往OA的formtable_main_136_dt1明细表内回写UUID_开始");
							RecordSet rs2 = new RecordSet();
							String sql1 ="update formtable_main_136_dt1 set ";
							if(!"".equals(uuid4)){
							sql1 = sql1 + " uuid_importantBussiness='" +uuid4+"',";
							}
							String updateSql = sql1.substring(0, sql1.length()-1);
							
							String zzsql = updateSql +" where mainid ='" +id+ "' and id='" +mxid+ "' ";
							rs2.executeSql(zzsql);
							writeLog(zzsql);
							writeLog("update语句回写结束");
					
					}else{
						writeLog("不显示,"+"____"+IS_SHOW+"_"+Worlds);
				  }
				}else{
					writeLog("UUID不为空____"+uuid4);
				}
			  }else{
				writeLog("金额判断小于100万元____"+Worlds);  
			  }
            }
        }catch(Exception e){
            writeLog("项目采购_专题采购往中间表MATERIAL_CONTRACT_PUB更新数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
