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

public class MajorProcurement  extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{

			RecordSet rs = new RecordSet();
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select * from uf_xmcgztcg where id = "+requestid);
            if(rs.next()){

				RecordSetDataSource SSEWebDB = new RecordSetDataSource("SSEWebDB"); 
				//uuid
				String uuid4Change = Util.null2String(rs.getString("uuid"));				
				//合同名称
				String CONTRACT_NAME = Util.null2String(rs.getString("CONTRACT_NAME"));
				//供应商
				String SUPPLIER = Util.null2String(rs.getString("SUPPLIER"));
				//签约方
				String MAIN_PARTY = Util.null2String(rs.getString("MAIN_PARTY"));
				//合同签订日期
				String CONTACT_DATE = Util.null2String(rs.getString("CONTACT_DATE"));
				//是否网站展示
				String IS_SHOW = Util.null2String(rs.getString("IS_SHOW"));
				
				//采购方式
				String PUR_METHOD = Util.null2String(rs.getString("BuyTypeConvertDetail"));	
				
				//数据来源
				String DATA_SOURCE = "SSEINFO";
				
				//金额
				String CONTRACT_MONEY=Util.null2String(rs.getString("je"));
				String je = Util.null2String(rs.getString("je"));			
				BigDecimal je0 = new BigDecimal(je);	
				BigDecimal je1 = new BigDecimal(1000000.00);
			
				//uuid若为空，生成新uuid
				String uuid4 = Util.null2String(rs.getString("uuid"));
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

            	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 				String createD = sdf.format(new Date());

					String sql = " insert into MATERIAL_CONTRACT_PUB "
							+ " (APPLY_ID,CONTRACT_NAME,SUPPLIER,MAIN_PARTY,CONTACT_DATE,PUR_METHOD,CONTRACT_MONEY,IS_SHOW,DATA_SOURCE,STATUS,WRITE_TIME) "
							+ " VALUES "					
							+ " ('"+uuid4+"','"+CONTRACT_NAME+"','"+SUPPLIER+"','"+MAIN_PARTY+"',"+"to_date('"+CONTACT_DATE+"','yyyy-mm-dd'),'"+PUR_METHOD+"','"+CONTRACT_MONEY+"','"+IS_SHOW+"','"+DATA_SOURCE+"',0,'"+createD+"') ";
					try {
						SSEWebDB.executeSql(sql);
						writeLog("insert语句："+ sql);
						writeLog("insert语句写入结束");
					} catch (Exception e) {
						writeLog("插入MATERIAL_CONTRACT_PUB出错："+e);
						return Action.FAILURE_AND_CONTINUE;
					}
							
writeLog("进入2_往OA的uf_xmcgztcg表内回写UUID_开始____"+uuid4);
					RecordSet rs2 = new RecordSet();
					//将中间表id插入oa表保持唯一对应关系
					rs2.executeSql(" update uf_xmcgztcg set uuid = '" + uuid4 +"' where id = "+requestid );				
					
				}else if(status.equals("0")){
//显示，编辑时，修改
writeLog("状态1____:"+status);

				SimpleDateFormat sdfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 				String createDateT = sdfs.format(new Date());

				String sql ="update MATERIAL_CONTRACT_PUB set ";
				if(!"".equals(CONTRACT_NAME)){
					sql = sql + " CONTRACT_NAME='" +CONTRACT_NAME+"',";
				}
				if(!"".equals(SUPPLIER)){
					sql = sql + " SUPPLIER='" +SUPPLIER+"',";
				}
				if(!"".equals(CONTACT_DATE)){
					sql = sql + " CONTACT_DATE = to_date('"+CONTACT_DATE+"','yyyy-MM-dd'),";
				}
				if(!"".equals(MAIN_PARTY)){
					sql = sql + " MAIN_PARTY='" +MAIN_PARTY+"',";
				}
				if(!"".equals(IS_SHOW)){
					sql = sql + " IS_SHOW='" +IS_SHOW+"',";
				}
				if(!"".equals(PUR_METHOD)){
					sql = sql + " PUR_METHOD='" +PUR_METHOD+"',";
				}
				if(!"".equals(CONTRACT_MONEY)){
					sql = sql + " CONTRACT_MONEY='" +CONTRACT_MONEY+"',";
				}
				/*if(!"".equals(createDateT)){
					sql = sql + " ALTER_TIME= to_date('"+createDateT+"','yyyy-MM-dd HH:mm:ss'),";
				}*/
				if(!"".equals(createDateT)){
					sql = sql + " ALTER_TIME='" +createDateT+"',";
				}
					sql = sql + " STATUS='1',";
				
				String updateSql = sql.substring(0, sql.length()-1);
				String zzsql = updateSql +" where APPLY_ID ='" +uuid4+ "'";
				SSEWebDB.executeSql(zzsql);
				writeLog(zzsql);
				writeLog("更新成功");

				}else{
//不显示，编辑时
writeLog("状态2:____"+status);	

				SimpleDateFormat sdfsd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 				String createDateTS = sdfsd.format(new Date());

				String sql ="update MATERIAL_CONTRACT_PUB set ";

				if(!"".equals(IS_SHOW)){
					sql = sql + " IS_SHOW='" +IS_SHOW+"',";
				}
				if(!"".equals(createDateTS)){
					sql = sql + " ALTER_TIME='" +createDateTS+"',";
				}
					sql = sql + " STATUS='2',";
				
				String updateSql = sql.substring(0, sql.length()-1);
				String zzsql = updateSql +" where APPLY_ID ='" +uuid4+ "'";
				SSEWebDB.executeSql(zzsql);
				writeLog(zzsql);
				writeLog("更新成功");

				}
			  }else{
				writeLog("当前客户未签订大于等于100万项目"+CONTRACT_NAME+"__"+je);
			  }
            }
        }catch(Exception e){
            writeLog("项目采购_专题采购往中间表MATERIAL_CONTRACT_PUB更新数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
