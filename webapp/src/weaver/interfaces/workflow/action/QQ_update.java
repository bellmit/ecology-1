package weaver.interfaces.workflow.action;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//期权行情许可申请--update表SYNC_WEBSITE_OPT_PERMIT_INFO
public class QQ_update extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
			RecordSet rs = new RecordSet();
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select * from formtable_main_51 where id = "+requestid);
            
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
				String dnrjzs = Util.null2String(rs.getString("dnrjzs"));
				if(dnrjzs.equals("1")){
					xkyt = xkyt + "电脑软件展示.";
				}
				String wzzs= Util.null2String(rs.getString("wzzs"));
				if(wzzs.equals("1")){
					xkyt = xkyt + "网站展示.";
				}
				String sjyyzs = Util.null2String(rs.getString("sjyyzs"));	
				if(sjyyzs.equals("1")){
					xkyt = xkyt + "手机应用展示.";
				}
				String qt2=Util.null2String(rs.getString("qt2"));
				if(qt2.equals("1")){
					xkyt = xkyt + "其他.";
				}
				
				
				//许可时间
				String xksj = "";
				String sqxknxks = Util.null2String(rs.getString("sqxknxks"));	
				String sqxknxjs=Util.null2String(rs.getString("sqxknxjs"));
				xksj = sqxknxks + "~" +sqxknxjs ;
				
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
				String status = "0";
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
				String application_elements = "许可内容：" + xknr + "\n许可用途：" + xkyt + "\n许可范围：" + xkfw + "\n许可时间:" +xksj ;
				
				String sql ="update SYNC_WEBSITE_OPT_PERMIT_INFO set ";
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
					sql = sql + " CONTENT='" +cphfwmc +"',";
				}
				if(!"".equals(application_elements)){
					sql = sql + " APPLICATION_ELEMENTS='" +application_elements +"',";
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
				if(!"".equals(gsbj)){
					sql = sql + " BASIC_SITUATION='" +gsbj +"',";
				}
				if(!"".equals(order_type)){
					sql = sql + " ORDER_TYPE='" +order_type +"',";
				}
				if(!"".equals(requestid)){
					sql = sql + " SHOWORDER='" +requestid +"',";
				}
				
				String updateSql = sql.substring(0, sql.length()-1);
				String zzsql = updateSql +" where ID ='" +syid+ "'";
				exchangeDB.executeSql(zzsql);
				writeLog(zzsql);
//				exchangeDB.executeSql("insert into SYNC_WEBSITE_OPT_PERMIT_INFO(ID,ORGANIZATION_CODE,"
//						+ "COMPANY_NAME,APPLICATION_DATE,UPLOAD_TIME,CONTENT,APPLICATION_ELEMENTS,DEADLINE,"
//						+ "STATUS,COMPANY_ID,BASIC_SITUATION,ORDER_TYPE,SHOWORDER)values('"+syid+"','"+zzjgdm+
//						"','"+gsmc+"',"+"to_date('"+sqsj+"','yyyy-mm-dd'),"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
//						"'"+cphfwmc+"','"+application_elements+"','"+deadline+"','"+status+"','"+gsid+
//						"','"+gsbj+"','"+order_type+"','"+requestid+"')");
            	
				
            }
        }catch(Exception e){
            writeLog("期权行情申请往中间表SYNC_WEBSITE_OPT_PERMIT_INFO更新数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
