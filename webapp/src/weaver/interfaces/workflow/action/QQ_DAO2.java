package weaver.interfaces.workflow.action;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//期权行情许可申请推送到sync_website_opt_permit_list表中
public class QQ_DAO2 extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
		  RecordSet rs = new RecordSet();
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
              	 ds.executeSql("select * from uf_qqhqxkmd where id = "+xqdd); 
                 if(ds.next()){
				String syid=Util.null2String(ds.getString("syid"));						
            	String zzjgdm = Util.null2String(ds.getString("zzjgdm"));
				String gsmc= Util.null2String(ds.getString("gsmc"));
				String sqsj = Util.null2String(ds.getString("sqsj"));	
				String tcsj = Util.null2String(ds.getString("tcsj"));	
				String cphfwmc=Util.null2String(ds.getString("cphfwmc"));
				//许可用途
				String xkyt="";
				String dnrjzs = Util.null2String(ds.getString("dnrjzs"));
				if(dnrjzs.equals("1")){
					xkyt = xkyt + "电脑软件展示.";
				}
				String wzzs= Util.null2String(ds.getString("wzzs"));
				if(wzzs.equals("1")){
					xkyt = xkyt + "网站展示.";
				}
				String sjyyzs = Util.null2String(ds.getString("sjyyzs"));	
				if(sjyyzs.equals("1")){
					xkyt = xkyt + "手机应用展示.";
				}
				String qt2=Util.null2String(ds.getString("qt2"));
				if(qt2.equals("1")){
					xkyt = xkyt + "其他.";
				}
				
				
				//许可时间
				String xksj = "";
				String sqxknxks = Util.null2String(ds.getString("sqxknxks"));	
				String sqxknxjs=Util.null2String(ds.getString("sqxknxjs"));
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
				String gsid = Util.null2String(ds.getString("gsid"));
				String gsbj = Util.null2String(ds.getString("gsbj"));
				String order_type = "上海证券交易所股票期权行情";
				//许可内容
				String xknr = Util.null2String(ds.getString("sqxknr"));
				//许可范围
				String xkfw ="";
				rs.executeSql("select selectname from workflow_selectitem where fieldid=6736 and "
						+ "selectvalue=(select sqxknr from formtable_main_51 where requestId="+requestid+")");
				if(rs.next()){
					xkfw = Util.null2String(rs.getString("selectname"));
				}
				String application_elements = "许可内容：" + xknr + "\n许可用途：" + xkyt + "\n许可范围：" + xkfw + "\n许可时间:" +xksj ;
				String px=Util.null2String(ds.getString("px"));
				exchangeDB.executeSql("insert into sync_website_opt_permit_list(ID,ORGANIZATION_CODE,"
						+ "COMPANY_NAME,UPLOAD_TIME,CONTENT,DEADLINE,"
						+ "STATUS,COMPANY_ID,ORDER_TYPE,SHOWORDER)values('"+syid+"','"+zzjgdm+
						"','"+gsmc+"',"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
						"'"+cphfwmc+"','"+deadline+"','"+status+"','"+gsid+
						"','"+order_type+"','"+px+"')");
				writeLog("insert into sync_website_opt_permit_list(ID,ORGANIZATION_CODE,"
						+ "COMPANY_NAME,UPLOAD_TIME,CONTENT,DEADLINE,"
						+ "STATUS,COMPANY_ID,ORDER_TYPE,SHOWORDER)values('"+syid+"','"+zzjgdm+
						"','"+gsmc+"',"+"to_date('"+tcsj+"','yyyy-mm-dd'),"+
						"'"+cphfwmc+"','"+deadline+"','"+status+"','"+gsid+
						"','"+order_type+"','"+px+"')");
               
                 }
                 }
            }
        }catch(Exception e){
            writeLog("期权行情申请往中间表sync_website_opt_permit_list写数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
