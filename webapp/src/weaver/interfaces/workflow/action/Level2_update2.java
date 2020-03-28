package weaver.interfaces.workflow.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//Level2申请--update表sync_website_lv2_permit_list
public class Level2_update2 extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
		  writeLog("Level2申请往中间表sync_website_lv2_permit_list更新数据开始");
			RecordSet ds = new RecordSet();
			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");  
            String requestid = Util.null2String(request.getRequestid());
           
                	 ds.executeSql("select * from uf_leveltwo where id = "+requestid);
                     //查询Level1许可名单的值
                     if(ds.next()){
                    	 String syid=Util.null2String(ds.getString("syid"));						
                     	String zzjgdm = Util.null2String(ds.getString("zzjgdm"));
         				String gsmc= Util.null2String(ds.getString("gsmc"));
         				String tcsj = Util.null2String(ds.getString("tcsj"));	
         				String cphfwmc=Util.null2String(ds.getString("cphfwmc"));
         				String fwkssj = Util.null2String(ds.getString("fwkssj"));	
        				String fwjssj=Util.null2String(ds.getString("fwjssj"));
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
        				String sqxknx=Util.null2String(ds.getString("sqxknx"));
        				//根据申请许可年限加上申请时间得到申请许可期限
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
        				
        				String gsbj=Util.null2String(ds.getString("gsbj"));
        				String px=Util.null2String(ds.getString("px"));
        				
        				String sql ="update sync_website_lv2_permit_list set ";
        				if(!"".equals(syid)){
        					sql = sql + " ID='" +syid +"',";
        				}
        				if(!"".equals(zzjgdm)){
        					sql = sql + " ORGANIZATION_CODE='" +zzjgdm +"',";
        				}
        				if(!"".equals(gsmc)){
        					sql = sql + " COMPANY_NAME='" +gsmc +"',";
        				}
        				if(!"".equals(tcsj)){
        					sql = sql + " UPLOAD_TIME = to_date('"+tcsj+"','yyyy-mm-dd'),";
        				}
        				if(!"".equals(status)){
        					sql = sql + " STATUS='" +status +"',";
        				}
        				if(!"".equals(gsid)){
        					sql = sql + " COMPANY_ID='" +gsid +"',";
        				}
        				if(!"".equals(cphfwmc)){
        					sql = sql + " AUDITED_PRODUCT='" +cphfwmc +"',";
        				}
        				if(!"".equals(xkyt)){
        					sql = sql + " PERMISSION_USAGE='" +xkyt +"',";
        				}
        				if(!"".equals(sqxkfw)){
        					sql = sql + " PERMISSION_RANGE='" +sqxkfw +"',";
        				}
        				if(!"".equals(PERMISSION_DEADLINE)){
        					sql = sql + " PERMISSION_DEADLINE='" +PERMISSION_DEADLINE +"',";
        				}
        				if(!"".equals(px)){
        					sql = sql + " SHOWORDER='" +px +"',";
        				}
        				
        				String updateSql = sql.substring(0, sql.length()-1);
        				String zzsql = updateSql +" where ID ='" +syid+ "'";
        				exchangeDB.executeSql(zzsql);
        				writeLog(zzsql);
        				writeLog("Level2申请往中间表sync_website_lv2_permit_list更新数据成功");
//                    	 exchangeDB.executeSql("insert into sync_website_lv2_permit_list(ID,ORGANIZATION_CODE,"
//          						+ "COMPANY_NAME,UPLOAD_TIME,"
//          						+ "STATUS,COMPANY_ID,PERMISSION_USAGE,PERMISSION_RANGE,PERMISSION_DEADLINE,SHOWORDER)values('"+
//          						syid+"','"+zzjgdm+"','"+gsmc+"',"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
//          					"','"+status+"','"+gsid+"','"+xkyt+"','"+sqxkfw+"','"+sqsj+"','"+px+"')");
                     
                	 
                 
				
            	
				
            }
        }catch(Exception e){
            writeLog("Level2申请往中间表sync_website_lv2_permit_list更新数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
