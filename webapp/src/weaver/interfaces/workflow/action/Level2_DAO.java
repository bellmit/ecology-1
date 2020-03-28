package weaver.interfaces.workflow.action;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//Level2申请推送到sync_website_lv2_permit_info表中
public class Level2_DAO extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
			RecordSet rs = new RecordSet();
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select * from formtable_main_44 where requestid = "+requestid);
            
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
				String xkfw = "";
				rs.executeSql("select selectname from workflow_selectitem where fieldid=6601 and "
						+ "selectvalue=(select sqxknr from formtable_main_44 where requestId="+requestid+")");
				if(rs.next()){
					xkfw = Util.null2String(rs.getString("selectname"));
				}
				
				exchangeDB.executeSql("insert into SYNC_WEBSITE_LV2_PERMIT_INFO(ID,ORGANIZATION_CODE,"
						+ "COMPANY_NAME,APPLICATION_DATE,UPLOAD_TIME,APPLICATION_ELEMENTS,DEADLINE,"
						+ "STATUS,COMPANY_ID,PERMISSION_USAGE,PERMISSION_RANGE,PERMISSION_DEADLINE,BASIC_SITUATION,"
						+ "SHOWORDER)values('"+syid+"','"+zzjgdm+
						"','"+gsmc+"',"+"to_date('"+sqsj+"','yyyy-mm-dd'),"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
						"'"+cphfwmc+"','"+deadline+"','"+status+"','"+gsid+
						"','"+xkyt+"','"+xkfw+"','"+xksj+"','"+gsbj+"','"+requestid+"')");
				writeLog("insert into SYNC_WEBSITE_LV2_PERMIT_INFO(ID,ORGANIZATION_CODE,"
						+ "COMPANY_NAME,APPLICATION_DATE,UPLOAD_TIME,APPLICATION_ELEMENTS,DEADLINE,"
						+ "STATUS,COMPANY_ID,PERMISSION_USAGE,PERMISSION_RANGE,PERMISSION_DEADLINE,BASIC_SITUATION,"
						+ "SHOWORDER)values('"+syid+"','"+zzjgdm+
						"','"+gsmc+"',"+"to_date('"+sqsj+"','yyyy-mm-dd'),"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
						"'"+cphfwmc+"','"+deadline+"','"+status+"','"+gsid+
						"','"+xkyt+"','"+xkfw+"','"+xksj+"','"+gsbj+"','"+requestid+"')");
				
            }
        }catch(Exception e){
            writeLog("Level2申请往中间表写数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
