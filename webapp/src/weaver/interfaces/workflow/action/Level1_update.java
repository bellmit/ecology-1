package weaver.interfaces.workflow.action;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//Level1--update表sync_website_lv1_permit_info
public class Level1_update extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
		  writeLog("Level1申请往中间表sync_website_lv1_permit_info更新数据开始");
			RecordSet rs = new RecordSet();
			String billid  = request.getRequestid();
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select * from formtable_main_34 where id = "+billid);
            
            if(rs.next()){

    			RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");  
				String syid=Util.null2String(rs.getString("syid"));						
            	String zzjgdm = Util.null2String(rs.getString("zzjgdm"));
				String gsmc= Util.null2String(rs.getString("gsmc"));
				String sqsj = Util.null2String(rs.getString("sqsj"));	
				String tcsj = Util.null2String(rs.getString("tcsj"));	
				String mgs=Util.null2String(rs.getString("mgs"));
				//许可用途
				String xkyt="";
				String dnrjzs = Util.null2String(rs.getString("dnrjzs"));
				if(dnrjzs.equals("1")){
					xkyt = xkyt + "电脑软件展示.";
				}
				String hlwwzzs= Util.null2String(rs.getString("hlwwzzs"));
				if(hlwwzzs.equals("1")){
					xkyt = xkyt + "互联网网站展示.";
				}
				String sjzs = Util.null2String(rs.getString("sjzs"));	
				if(sjzs.equals("1")){
					xkyt = xkyt + "无线展示.";
				}
				String gbdszs = Util.null2String(rs.getString("gbdszs"));
				if(gbdszs.equals("1")){
					xkyt = xkyt + "广播电视展示.";
				}
				String qt1=Util.null2String(rs.getString("qt1"));
				if(qt1.equals("1")){
					xkyt = xkyt + "其他.";
				}
				//许可范围
				String xkfw="";
				String xjsdjs = Util.null2String(rs.getString("xjsdjs"));
				if(xjsdjs.equals("1")){
					xkfw = xkfw + "县级市/地级市.";
				}
				String shcs= Util.null2String(rs.getString("shcs"));
				if(shcs.equals("1")){
					xkfw = xkfw + "省会城市.";
				}
				String zgnd = Util.null2String(rs.getString("zgnd"));	
				if(zgnd.equals("1")){
					xkfw = xkfw + "中国内地（不含港、澳、台地区）.";
				}
				String jhqs = Util.null2String(rs.getString("jhqs"));
				if(jhqs.equals("1")){
					xkfw = xkfw + "京沪/全省.";
				}
				String qq=Util.null2String(rs.getString("qq"));
				if(qq.equals("1")){
					xkfw = xkfw + "全球.";
				}
				//许可时间
				String xksj = "";
				String sqxknxbegin = Util.null2String(rs.getString("sqxknxbegin"));	
				String sqxknxend=Util.null2String(rs.getString("sqxknxend"));
				xksj = sqxknxbegin + "~" +sqxknxend ;
				
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
				String order_type = "实时行情";
				//许可内容
				String xknr = Util.null2String(rs.getString("sqxknr"));
				if(xknr.equals("0")){
					xknr = "实时行情.";
				}
				if(xknr.equals("1")){
					xknr = "延时行情（30分钟以上）.";
				}
				String application_elements = "许可内容：" + xknr + "\n许可用途：" + xkyt + "\n许可范围：" + xkfw + "\n许可时间:" +xksj ;
				
//				exchangeDB.executeSql("insert into sync_website_lv1_permit_info(ID,ORGANIZATION_CODE,"
//						+ "COMPANY_NAME,APPLICATION_DATE,UPLOAD_TIME,CONTENT,APPLICATION_ELEMENTS,DEADLINE,"
//						+ "STATUS,COMPANY_ID,BASIC_SITUATION,ORDER_TYPE,SHOWORDER)values('"+syid+"','"+zzjgdm+
//						"','"+gsmc+"',"+"to_date('"+sqsj+"','yyyy-mm-dd'),"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
//						"'"+mgs+"','"+application_elements+"','"+deadline+"','"+status+"','"+gsid+
//						"','"+gsbj+"','"+order_type+"','"+requestid+"')");
				String sql ="update sync_website_lv1_permit_info set ";
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
				if(!"".equals(mgs)){
					sql = sql + " CONTENT='" +mgs +"',";
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
				writeLog("Level1申请往中间表sync_website_lv1_permit_info更新数据成功");
				
            }
        }catch(Exception e){
            writeLog("Level1申请往中间表sync_website_lv1_permit_info更新数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
