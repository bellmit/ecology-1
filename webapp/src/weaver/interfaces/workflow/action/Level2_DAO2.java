package weaver.interfaces.workflow.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//Level2申请推送到sync_website_lv2_permit_list表中
public class Level2_DAO2 extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
		  writeLog("Level2申请往中间表sync_website_lv2_permit_list写数据开始");
			RecordSet rs = new RecordSet();
			RecordSet ss = new RecordSet();
			RecordSet ds = new RecordSet();
			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");  
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select * from formtable_main_56 where requestid = "+requestid);
            
            if(rs.next()){
            	String mainid=Util.null2String(rs.getString("id"));	
            	 rs.executeSql("select * from formtable_main_56_dt1 where mainid = "+mainid);
                 //查询明细表的多个申请订单值
                 while(rs.next()){
                	 String xqdd=Util.null2String(rs.getString("xqdd"));
                	 ss.executeSql("select * from uf_ddcx where id = "+xqdd);
                	 if(ss.next()){
                    	 String sssid=Util.null2String(ss.getString("syid"));	
                    	 ds.executeSql("select * from formtable_main_44 where syid = '"+sssid +"' and requestId is null");
                    	//查询Level1许可名单的值
                     if(ds.next()){
                    	 String syid=Util.null2String(ds.getString("syid"));						
                     	String zzjgdm = Util.null2String(ds.getString("zzjgdm"));
         				String gsmc= Util.null2String(ds.getString("gsmc"));
         				String tcsj = Util.null2String(ds.getString("tcsj"));	
         				String cphfwmc=Util.null2String(ds.getString("cphfwmc"));
         				String fwkssj = Util.null2String(ds.getString("sqsxksrq"));	
        				String fwjssj=Util.null2String(ds.getString("sqsxjsrq"));
        				String xksj = fwkssj + "~" +fwjssj ;
        				String status = "0";
        				String gsid=Util.null2String(ds.getString("gsid"));
        				//许可用途
        				String xkyt="";
        				String dnzd = Util.null2String(ds.getString("dnzd"));
        				if(dnzd.equals("1")){
        					xkyt = xkyt + "以电脑终端展示软件方式向最终用户提供.";
        				}
        				String ydzd= Util.null2String(ds.getString("ydzd"));
        				if(ydzd.equals("1")){
        					xkyt = xkyt + "以移动终端展示软件方式向最终用户提供.";
        				}
        				
        				//申请许可范围
        				String sqxkfw = Util.null2String(ds.getString("sqxkfw"));
        				if(sqxkfw.equals("0")){
        					sqxkfw = "中国内地（不含港、澳、台地区）";
        				}
        				if(sqxkfw.equals("1")){
        					sqxkfw = "全球";
        				}
        				String sqsj=Util.null2String(ds.getString("sqsj"));
        				String gsbj=Util.null2String(ds.getString("gsbj"));
        				String px=Util.null2String(ds.getString("px"));
        				String sqxknx=Util.null2String(ds.getString("sqxknx"));
        				String d13="";
        				if((!"".equals(sqsj))&&(!"".equals(sqxknx))){
        					DateFormat aa= new SimpleDateFormat("yyyy-MM-dd");
            				Date date = aa.parse(sqsj);
            				Calendar cl = Calendar.getInstance();
            				cl.setTime(date);
            				cl.add(Calendar.YEAR, Integer.parseInt(sqxknx)+1);
            				String nian=String.valueOf(cl.get(Calendar.YEAR));
            				String yue=String.valueOf(cl.get(Calendar.MONTH)+1);
            				String ri=String.valueOf(cl.get(Calendar.DAY_OF_MONTH));
            			    d13 = nian + "-" + yue + "-" + ri;
        				}
        				String PERMISSION_DEADLINE = sqsj + "~" +d13;
        				
                    	 exchangeDB.executeSql("insert into sync_website_lv2_permit_list(ID,ORGANIZATION_CODE,"
          						+ "COMPANY_NAME,UPLOAD_TIME,AUDITED_PRODUCT,PERMISSION_DEADLINE,"
          						+ "STATUS,COMPANY_ID,PERMISSION_USAGE,PERMISSION_RANGE,SHOWORDER)values('"+
          						syid+"','"+zzjgdm+"','"+gsmc+"',"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+"'"+cphfwmc+"','"+
          						PERMISSION_DEADLINE+"','"+status+"','"+gsid+"','"+xkyt+"','"+sqxkfw+"','"+px+"')");
                    	 writeLog("insert into sync_website_lv2_permit_list(ID,ORGANIZATION_CODE,"
           						+ "COMPANY_NAME,UPLOAD_TIME,AUDITED_PRODUCT,PERMISSION_DEADLINE,"
           						+ "STATUS,COMPANY_ID,PERMISSION_USAGE,PERMISSION_RANGE,SHOWORDER)values('"+
           						syid+"','"+zzjgdm+"','"+gsmc+"',"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+"'"+cphfwmc+"','"+
           						PERMISSION_DEADLINE+"','"+status+"','"+gsid+"','"+xkyt+"','"+sqxkfw+"','"+px+"')");
                     }
                     
                	 
                 }
				
                 }
				
            }
        }catch(Exception e){
            writeLog("Level2申请往中间表sync_website_lv2_permit_list写数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
