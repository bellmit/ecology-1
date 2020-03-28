package weaver.interfaces.workflow.action;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//Level2申请--update表sync_website_lv2_permit_info
public class Level2_update extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
		  writeLog("Level2申请往中间表sync_website_lv2_permit_info更新数据开始");
			RecordSet rs = new RecordSet();
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select * from formtable_main_44 where id = "+requestid);
            
            if(rs.next()){

    			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");  
				String syid=Util.null2String(rs.getString("syid"));						
            	String zzjgdm = Util.null2String(rs.getString("zzjgdm"));
				String gsmc= Util.null2String(rs.getString("gsmc"));
				String sqsj = Util.null2String(rs.getString("sqsj"));	
				String tcsj = Util.null2String(rs.getString("tcsj"));	
				String cphfwmc=Util.null2String(rs.getString("cphfwmc"));
				//许可用途
				String xkyt="";
				String dnzd = Util.null2String(rs.getString("dnzd"));
				if(dnzd.equals("1")){
					xkyt = xkyt + "以电脑终端展示软件方式向最终用户提供.";
				}
				String ydzd= Util.null2String(rs.getString("ydzd"));
				if(ydzd.equals("1")){
					xkyt = xkyt + "以移动终端展示软件方式向最终用户提供.";
				}
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = sdf.parse(sqsj);
				Calendar cl = Calendar.getInstance();
				cl.setTime(date);
				cl.add(Calendar.DAY_OF_MONTH, 30);
				//加30的时间
				String nian=String.valueOf(cl.get(Calendar.YEAR));
				String yue=String.valueOf(cl.get(Calendar.MONTH)+1);
				String ri=String.valueOf(cl.get(Calendar.DAY_OF_MONTH));
				String d30 = nian + "-" + yue + "-" + ri;;
				String deadline = sqsj + "~" +d30;
				String status = "0";
				String gsid = Util.null2String(rs.getString("gsid"));
				
				//许可时间
				String xksj = "";
				String sqsxksrq = Util.null2String(rs.getString("sqsxksrq"));	
				String sqsxjsrq=Util.null2String(rs.getString("sqsxjsrq"));
				xksj = sqsxksrq + "~" +sqsxjsrq ;
				
				String gsbj = Util.null2String(rs.getString("gsbj"));
				//申请许可范围
				String xkfw = Util.null2String(rs.getString("sqxkfw"));
				if(xkfw.equals("0")){
					xkfw = "中国内地（不含港、澳、台地区）.";
				}
				if(xkfw.equals("1")){
					xkfw = "全球.";
				}
				
				String sql ="update SYNC_WEBSITE_LV2_PERMIT_INFO set ";
				if(!"".equals(syid)){
					sql = sql + " ID='" +syid +"',";
				}
				if(!"".equals(zzjgdm)){
					sql = sql + " ORGANIZATION_CODE='" +zzjgdm +"',";
				}
				if(!"".equals(gsmc)){
					sql = sql + " COMPANY_NAME='" +gsmc +"',";
				}
				if(!"".equals(sqsj)){
					sql = sql + " APPLICATION_DATE = to_date('"+sqsj+"','yyyy-mm-dd'),";
				}
				if(!"".equals(tcsj)){
					sql = sql + " UPLOAD_TIME = to_date('"+tcsj+"','yyyy-mm-dd'),";
				}
				if(!"".equals(cphfwmc)){
					sql = sql + " APPLICATION_ELEMENTS='" +cphfwmc +"',";
				}
				if(!"".equals(deadline)){
					sql = sql + " DEADLINE='" +deadline +"',";
				}
				if(!"".equals(status)){
					sql = sql + " STATUS='" +status +"',";
				}
				if(!"".equals(gsid)){
					sql = sql + " COMPANY_ID='" +gsid +"',";
				}
				if(!"".equals(xkyt)){
					sql = sql + " PERMISSION_USAGE='" +xkyt +"',";
				}
				if(!"".equals(xkfw)){
					sql = sql + " PERMISSION_RANGE='" +xkfw +"',";
				}
				if(!"".equals(xksj)){
					sql = sql + " PERMISSION_DEADLINE='" +xksj +"',";
				}
				if(!"".equals(gsbj)){
					sql = sql + " BASIC_SITUATION='" +gsbj +"',";
				}
				if(!"".equals(requestid)){
					sql = sql + " SHOWORDER='" +requestid +"',";
				}
				String updateSql = sql.substring(0, sql.length()-1);
				String zzsql = updateSql +" where ID ='" +syid+ "'";
				exchangeDB.executeSql(zzsql);
				writeLog(zzsql);
				writeLog("Level2申请往中间表sync_website_lv2_permit_info更新数据成功");
//				exchangeDB.executeSql("insert into SYNC_WEBSITE_LV2_PERMIT_INFO(ID,ORGANIZATION_CODE,"
//						+ "COMPANY_NAME,APPLICATION_DATE,UPLOAD_TIME,APPLICATION_ELEMENTS,DEADLINE,"
//						+ "STATUS,COMPANY_ID,PERMISSION_USAGE,PERMISSION_RANGE,PERMISSION_DEADLINE,BASIC_SITUATION,"
//						+ "SHOWORDER)values('"+syid+"','"+zzjgdm+
//						"','"+gsmc+"',"+"to_date('"+sqsj+"','yyyy-mm-dd'),"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
//						"'"+cphfwmc+"','"+deadline+"','"+status+"','"+gsid+
//						"','"+xkyt+"','"+xkfw+"','"+xksj+"','"+gsbj+"','"+requestid+"')");
            	
				
            }
        }catch(Exception e){
            writeLog("Level2申请往中间表更新数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
