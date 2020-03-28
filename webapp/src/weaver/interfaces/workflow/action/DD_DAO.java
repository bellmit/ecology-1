package weaver.interfaces.workflow.action;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//订单推送到mysql中的订单表sync_finance_voucher
public class DD_DAO extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
			RecordSet rs = new RecordSet();
			RecordSet lqys = new RecordSet();
            rs.executeSql("select * from uf_ddcx where status =0 and djje !=0 ");
            
            while(rs.next()){
            	UUID uuid = UUID.randomUUID();
        		String wyid =uuid.toString();
        		String id = Util.null2String(rs.getString("id"));
        		String CUSTOMER_CODE= Util.null2String(rs.getString("gsdm"));
        		String CUSTOMER= Util.null2String(rs.getString("khmc"));
        		String name="";
        		lqys.executeSql("select * from CRM_CustomerInfo where id ="+CUSTOMER);
       		 if(lqys.next()){
       			name=Util.null2String(lqys.getString("name"));
  				}
        		
        		//产品名称 ==业务类型
       		 String ORDER_DATE=Util.null2String(rs.getString("dgsj"));
				String PRODUCT = Util.null2String(rs.getString("yelx"));
				String gmcp = Util.null2String(rs.getString("gmcp"));
				if(PRODUCT.equals("0")){
					//level-1申请、
					PRODUCT = "L1授权费用";
				}else if(PRODUCT.equals("1")){
					//Level1自动延展
					PRODUCT = "L1授权费用";
				}else if(PRODUCT.equals("2")){
					//level-2申请、
					PRODUCT = "L2授权费用";
				}else if(PRODUCT.equals("3")){
					//Level2自动延展
					PRODUCT = "L2授权费用";
				}else if(PRODUCT.equals("4")){
					//Level2每月数据报送，还要根据购买产品区分3种金额
					if(gmcp.contains("实收信息费")){
						PRODUCT = "用户使用费";
						name = name + ORDER_DATE.substring(0, 7)+" Level2数据报送";;
					}
					if(gmcp.contains("DATAFEED金额")){
						PRODUCT = "DATAFEED金额";
						name = name + ORDER_DATE.substring(0, 7)+" Level2数据报送";;
					}
					if(gmcp.contains("VDE金额")){
						PRODUCT = "VDE金额";
						name = name + ORDER_DATE.substring(0, 7)+" Level2数据报送";;
					}
					
				}else if(PRODUCT.equals("5")){
					//CA证书申请
					PRODUCT = "CA服务费";
				}else if(PRODUCT.equals("6")){
					//网络投票
					PRODUCT = "股东大会网络投票";
				}else if(PRODUCT.equals("7")){
					//融资融券待征集
					PRODUCT = "融资融券业务";
				}else if(PRODUCT.equals("8")){
					//配股缴款数据
					PRODUCT = "其他";
				}else if(PRODUCT.equals("9")){
					//订购订单流转
					PRODUCT = "专线业务";
				}else if(PRODUCT.equals("10")){
					//期权行情经营许可申请
					PRODUCT = "其他";
				}else if(PRODUCT.equals("11")){
					//期权自动延展
					PRODUCT = "其他";
				}else if(PRODUCT.equals("12")){
					//互联网行情订购订单申请
					PRODUCT = "互联网行情托管";
				}else if(PRODUCT.equals("13")){
					//互联网行情托管业务订单申请，不生成订单
					PRODUCT = "其他";
				}else if(PRODUCT.equals("14")){
					//港股行情订单申请
					PRODUCT = "其他";
				}else if(PRODUCT.equals("15")){
					//会员业务
					PRODUCT = "会员信息服务平台";
				}else if(PRODUCT.equals("16")){
					//其他
					PRODUCT = "其他";
				}else if(PRODUCT.equals("18")){
					//level-2非展示数据
					PRODUCT = "DATAFEED金额";
				}else if(PRODUCT.equals("19")){
					//期权非展示数据
					PRODUCT = "DATAFEED金额";
				}else{
					//20180823新增else（目前EAS无法新增产品类型，OA新增类型退至EAS为“其他”)
					PRODUCT = "其他";
				}
				
            	 String BILL_AMOUNT=Util.null2String(rs.getString("djje"));
            	 Double dd=Double.valueOf(BILL_AMOUNT);
            	 String cc=String.format("%.2f", dd);
            	 String CONTRACT_CODE=Util.null2String(rs.getString("htbh"));
            	//有无合同
            	 String VOUCHER_TYPE ="0";
            	 if(CONTRACT_CODE !=""){
            		 lqys.executeSql("selct * from uf_htxx where htbh ='"+CONTRACT_CODE+"'");
            		 if(lqys.next()){
            			 VOUCHER_TYPE="1";
       				}
            	 }
            	 
            	 //订单编号，以YSID年月日4位流水
            	 String ddbh=Util.null2String(rs.getString("ddbh"));
            	 
            	 String gsid=Util.null2String(rs.getString("gsid"));
            	 RecordSetDataSource ss = new RecordSetDataSource("exchangeDB");		
 				RecordSetDataSource sdd = new RecordSetDataSource("financeTest");
 				String sql = "select * from SYNC_COMPANY where ID='"+gsid+"'";
 				String OPERATOR="";
 				String OPERATOR_TEL="";
 				String LINKMAN="";
 				String LINKMAN_TEL="";
 				String LINKMAN_MOBILE="";
 				String LINKMAN_EMAIL="";
 				ss.executeSql(sql);
 				while(ss.next()){
 					OPERATOR=Util.null2String(ss.getString("OPERATOR"));
 					OPERATOR_TEL=Util.null2String(ss.getString("OPERATOR_TEL"));
 					LINKMAN=Util.null2String(ss.getString("LINKMAN"));
 					LINKMAN_TEL=Util.null2String(ss.getString("LINKMAN_TEL"));
 					LINKMAN_MOBILE=Util.null2String(ss.getString("LINKMAN_MOBILE"));
 					LINKMAN_EMAIL=Util.null2String(ss.getString("LINKMAN_EMAIL"));
 				}
 				 
            	 String DEADLINE=Util.null2String(rs.getString("fkqx"));
            	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 				String createD = sdf.format(new Date());
// 				String sss=createD.substring(11, 19);
// 				DEADLINE = DEADLINE + " " +sss;
// 				ORDER_DATE= ORDER_DATE + " " +sss;
            	 String STATUS="0";
            	String sqlaa="insert into SYNC_FINANCE_VOUCHER (";
				String sqlbb=") values (";
				if(wyid !=""){
					sqlaa +="ID,";
					sqlbb +="'"+wyid+"',";
				}
				if(name !=""){
					sqlaa +="TITLE,";
					sqlbb +="'"+name+"',";
				}
				if(CUSTOMER !=""){
					sqlaa +="CUSTOMER,";
					sqlbb +="'"+name+"',";
				}
				if(CUSTOMER_CODE !=""){
					sqlaa +="CUSTOMER_CODE,";
					sqlbb +="'"+CUSTOMER_CODE+"',";
				}
				if(PRODUCT !=""){
					sqlaa +="PRODUCT,";
					sqlbb +="'"+PRODUCT+"',";
				}
				if(cc !=""){
					sqlaa +="BILL_AMOUNT,";
					sqlbb +="'"+cc+"',";
				}
				if(VOUCHER_TYPE !=""){
					sqlaa +="VOUCHER_TYPE,";
					sqlbb +="'"+VOUCHER_TYPE+"',";
					if(VOUCHER_TYPE.equals("1")){
						sqlaa +="CONTRACT_CODE,";
						sqlbb +="'"+CONTRACT_CODE+"',";
					}
				}
				
				if(ddbh !=""){
					sqlaa +="SID,";
					sqlbb +="'"+ddbh+"',";
				}
				if(OPERATOR !=""){
					sqlaa +="OPERATOR,";
					sqlbb +="'"+OPERATOR+"',";
				}
				if(OPERATOR_TEL !=""){
					sqlaa +="OPERATOR_TEL,";
					sqlbb +="'"+OPERATOR_TEL+"',";
				}
				if(LINKMAN !=""){
					sqlaa +="LINKMAN,";
					sqlbb +="'"+LINKMAN+"',";
				}
				if(LINKMAN_TEL !=""){
					sqlaa +="LINKMAN_TEL,";
					sqlbb +="'"+LINKMAN_TEL+"',";
				}
				if(LINKMAN_MOBILE !=""){
					sqlaa +="LINKMAN_MOBILE,";
					sqlbb +="'"+LINKMAN_MOBILE+"',";
				}
				if(LINKMAN_EMAIL !=""){
					sqlaa +="LINKMAN_EMAIL,";
					sqlbb +="'"+LINKMAN_EMAIL+"',";
				}
				sqlaa +="HX_INVOICE,";
				sqlbb +="'0',";
				sqlaa +="HX_RECORD,";
				sqlbb +="'0',";
				if(ORDER_DATE !=""){
					sqlaa +="ORDER_DATE,";
					sqlbb +="'"+ORDER_DATE+"',";
				}
				SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String createDate = dt.format(new Date());
				if(createDate !=""){
					sqlaa +="STEP1_TIME,";
					sqlbb +="'"+createDate+"',";
				}
				if(DEADLINE !=""){
					sqlaa +="DEADLINE,";
					sqlbb +="'"+DEADLINE+"',";
				}
				sqlaa +="STATUS1,STATUS_PRINT,STATUS,JD_STATUS";
				sqlbb +="'"+STATUS+"','"+STATUS+"','"+STATUS+"','"+STATUS+"')";
				
				//往中间表写入数据
            	sdd.executeSql(sqlaa+sqlbb);
            	writeLog("订单已经成功推送到财务，订单流水号为："+ddbh+"，订单id为："+wyid);
            	writeLog(sqlaa+sqlbb);
            	//回写OA订单表，与mysql数据库的订单id保持一致,并更新status为2，表示订单信息已经成功插入mysql订单表中
            	sdd.executeSql("select * from SYNC_FINANCE_VOUCHER where ID ='"+wyid+"'");
            	if(sdd.next()){
            		lqys.executeSql("update uf_ddcx set status = '2',sh_status = '1',wyid ='"+wyid+"' where id ="+id);
                	writeLog("update uf_ddcx set status = '2',sh_status = '1',wyid ='"+wyid+"' where id ="+id);
	            }
            	
            }
        }catch(Exception e){
            writeLog("订单推送到mysql中的订单表sync_finance_voucher出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }

}
