package weaver.interfaces.workflow.action;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
import java.util.UUID;
//期权行情许可申请--update表sync_website_opt_permit_list
public class OptionShow extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
writeLog("测试开始1:");
			RecordSet rs = new RecordSet();
writeLog("获取开始2:");
            String requestid = Util.null2String(request.getRequestid());
writeLog("获取结束:"+requestid);            
            rs.executeSql("select * from uf_qqhqxkmd where id = "+requestid);
            
            if(rs.next()){

    			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");  
				String uuid4Change = Util.null2String(rs.getString("uuid4Change"));				
				//公司id
				String gsmc = Util.null2String(rs.getString("gsmc"));
writeLog("公司id:"+gsmc);				
				//许可用途  
				String cphfwmc = Util.null2String(rs.getString("cphfwmc"));
writeLog("许可用途:"+cphfwmc);

				//申请时间
				String sqsj = Util.null2String(rs.getString("sqsj"));	
writeLog("申请时间:"+sqsj);	

				String syid=Util.null2String(rs.getString("syid"));						
            	String zzjgdm = Util.null2String(rs.getString("zzjgdm"));
				String tcsj = Util.null2String(rs.getString("tcsj"));	
				
				//排序
				String showOrder = Util.null2String(rs.getString("pxnew"));

				//许可时间
				String xksj = "";
				String sqxknxks = Util.null2String(rs.getString("sqxknxks"));	
				String sqxknxjs=Util.null2String(rs.getString("sqxknxjs"));
				xksj = sqxknxks + "~" +sqxknxjs ;
writeLog("许可期限:"+xksj);	
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = sdf.parse(sqsj);
				Calendar cl = Calendar.getInstance();
				cl.setTime(date);
				cl.add(Calendar.DAY_OF_MONTH, 13);
				//加13的时间
				String nian=String.valueOf(cl.get(Calendar.YEAR));
				String yue=String.valueOf(cl.get(Calendar.MONTH)+1);
				String ri=String.valueOf(cl.get(Calendar.DAY_OF_MONTH));
				String d13 = nian + "-" + yue + "-" + ri;;
				String deadline = sqsj + "~" +d13;
				String gsid = Util.null2String(rs.getString("gsid"));
				String gsbj = Util.null2String(rs.getString("gsbj"));
				String order_type = "上海证券交易所股票期权行情";
				//许可内容
				String xknr = Util.null2String(rs.getString("sqxknr"));
				//许可范围
				String xkfw = Util.null2String(rs.getString("sqxkfw"));
				if(xkfw.equals("0")){
					xkfw = "中国内地（不含港、澳、台地区）.";
				}
				if(xkfw.equals("1")){
					xkfw = "全球.";
				}
writeLog("许可范围:"+xkfw);
				
				//
				String uuid4 = Util.null2String(rs.getString("uuid4Change"));
				if(uuid4.equals("")){
					uuid4 = UUID.randomUUID().toString();	
				}
writeLog("UUID:"+uuid4);

				
				//是否展示
				String isShow = Util.null2String(rs.getString("sfxs"));
				//中间表状态,表示展示。
				String status = "0";
				if(isShow.equals("1")){
					status = "-1" ;
				}
				
				
				if("".equals(uuid4Change)){
					String sql = " insert into sync_website_opt_permit_list "
							+ " (ID,ORGANIZATION_CODE,COMPANY_NAME,UPLOAD_TIME,CONTENT,DEADLINE,STATUS,COMPANY_ID,ORDER_TYPE,SHOWORDER) "
							+ " VALUES "					
							+ " ('"+uuid4+"','"+zzjgdm+"','"+gsmc+"',"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+"'"+cphfwmc+"','"+deadline+"','"+status+"','"+gsid+"','"+order_type+"','"+showOrder+"') ";
							
					try {
writeLog("写入中间库DB开始:");
						exchangeDB.executeSql(sql);
writeLog("写入中间库DB结束:");
						writeLog("insert语句："+ sql);
					} catch (Exception e) {
						writeLog("插入sync_website_opt_permit_list出错："+e);
						return Action.FAILURE_AND_CONTINUE;
					}
					
					RecordSet rs2 = new RecordSet();
					//将中间表id插入oa表保持唯一对应关系
writeLog("变更台账UUID开始:");
					rs2.executeSql(" update uf_qqhqxkmd set uuid4Change = '" + uuid4 +"' where id = "+requestid );
writeLog("变更台账UUID结束:");					
					
				}else if(status.equals("0")){
writeLog("第2个if判断开始状态=0:");
writeLog("状态:"+status);
					String sql ="update sync_website_opt_permit_list set "
							+ " ORGANIZATION_CODE = '"+zzjgdm+"'"	
							+ " ,COMPANY_NAME = '"+gsmc+"'"					
							+ " ,UPLOAD_TIME = '"+tcsj+"'"	
							+ " ,CONTENT = '"+cphfwmc+"'"
							+ " ,DEADLINE = '"+deadline+"'"
							+ " ,STATUS = '"+status+"'"
							+ " ,COMPANY_ID = '"+gsid+"'"
							+ " ,ORDER_TYPE = '"+order_type+"'"
							+ " ,SHOWORDER = '"+showOrder+"'"
							+ "  where id = '"+uuid4+"'";
					
					exchangeDB.executeSql(sql);
					writeLog("update语句："+ sql);
					writeLog("更新成功");
				}else{
writeLog("变更开始状态=-1");	
writeLog("状态:"+status);				
					String sql ="update sync_website_opt_permit_list set "
							+ " ORGANIZATION_CODE = '"+zzjgdm+"'"					
							+ " ,COMPANY_NAME = '"+gsmc+"'"					
							+ " ,UPLOAD_TIME = '"+tcsj+"'"
							+ " ,CONTENT = '"+cphfwmc+"'"
							+ " ,DEADLINE = '"+deadline+"'"
							+ " ,STATUS = '999'"
							+ " ,COMPANY_ID = '"+gsid+"'"
							+ " ,ORDER_TYPE = '"+order_type+"'"
							+ " ,SHOWORDER = '"+showOrder+"'"
							+ "  where id = '"+uuid4+"'";
					
					exchangeDB.executeSql(sql);
					writeLog("update语句："+ sql);
					writeLog("更新成功");

				}
            }
        }catch(Exception e){
            writeLog("期权行情申请往中间表sync_website_opt_permit_list更新数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
