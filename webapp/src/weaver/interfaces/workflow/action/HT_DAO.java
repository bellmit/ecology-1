package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
//业务合同审批流程到用章确认，推送到金蝶财务系统sync_finance_contract表中
//Level1、Level2、期权行情走合同审批流程
public class HT_DAO extends BaseBean implements Action {

public String execute(RequestInfo request){
	  try{
			RecordSet rs = new RecordSet();
			RecordSet sss = new RecordSet();
			RecordSet ds = new RecordSet();
			RecordSet ddss = new RecordSet();
			RecordSet aa = new RecordSet();
			RecordSet lqys = new RecordSet();
			RecordSet llqqyy = new RecordSet();
			RecordSet yyjj = new RecordSet();
			RecordSetDataSource financeTest = new RecordSetDataSource("financeTest");  
            String requestid = Util.null2String(request.getRequestid());
            rs.executeSql("select * from formtable_main_56 where requestid = "+requestid);
            
            SimpleDateFormat sdfll = new SimpleDateFormat("yyyy-MM-dd");
    		String createDatell = sdfll.format(new Date());
            if(rs.next()){
            	String sqr=Util.null2String(rs.getString("sqr"));	
                ds.executeSql("select * from Hrmresource where id = "+sqr);
                String lastname = "";
                if(ds.next()){
                	lastname=Util.null2String(ds.getString("lastname"));	
                }	    		
            	String mainid=Util.null2String(rs.getString("id"));	
            	
            	 rs.executeSql("select * from formtable_main_56_dt1 where mainid = "+mainid);
                 //查询明细表的多个申请订单值
                 while(rs.next()){
             		String id = Util.null2String(rs.getString("id"));
                	 //合同金额
                	 String htje = Util.null2String(rs.getString("htje"));	
                	 Double dd=Double.valueOf(htje);
                	 String cc=String.format("%.2f", dd);
                	//合同开始时间
                	 String htksrq = Util.null2String(rs.getString("htksrq"));	
                	//合同结束时间
                	 String htyxqend = Util.null2String(rs.getString("htyxqend"));	
                	 
                	 String htbh=Util.null2String(rs.getString("htbh"));
                	//合同名称
                	 String gsmc="";
                	 String xgkhkp=Util.null2String(rs.getString("xgkhkp"));	
                	 sss.executeSql("select * from CRM_CustomerInfo where id = "+xgkhkp);
                     
                     if(sss.next()){
             			 gsmc= Util.null2String(sss.getString("name"));
             				
                     }
                     
                     //如果要签合同，比如LEVEL1申请、level2申请、期权行情许可申请，这三个流程走完后并没有插入mysql的订单，所以在合同审批中插入订单
                     String xqdd=Util.null2String(rs.getString("xqdd"));
                     lqys.executeSql("select * from uf_ddcx where id = '"+xqdd+"'");
                 	String wyid ="";
                 	String PRODUCT ="";
                 	String ppdd="";
                 	String khbh ="";
                    String dgsj="";
                 	if(lqys.next()){     		
                    	UUID uuid = UUID.randomUUID();
                    	wyid= Util.null2String(lqys.getString("wyid"));
                    	if(wyid.length()==0||wyid.equals("")){
                    		wyid =uuid.toString();
                    	}
           	     	
                		khbh= Util.null2String(lqys.getString("gsdm"));
                   	    dgsj= Util.null2String(lqys.getString("dgsj"));
                		String lqyid = Util.null2String(lqys.getString("id"));
                		
                		//查询出OA的wyid，如果有值，说明已经推送过mysql订单，比如订购订单流转单
                		String xjhid= Util.null2String(lqys.getString("wyid"));
                		String CUSTOMER_CODE= Util.null2String(lqys.getString("gsdm"));
                		String CUSTOMER= Util.null2String(lqys.getString("khmc"));
                		String name="";
                		ddss.executeSql("select * from CRM_CustomerInfo where id ="+CUSTOMER);
               		 if(ddss.next()){
               			name=Util.null2String(ddss.getString("name"));
          				}  		
                		//产品名称 ==业务类型
               		    String ORDER_DATE=Util.null2String(lqys.getString("dgsj"));
        				PRODUCT = Util.null2String(lqys.getString("yelx"));
        				ppdd = Util.null2String(lqys.getString("yelx"));
        				String gmcp = Util.null2String(lqys.getString("gmcp"));
writeLog("第0次"+PRODUCT);
writeLog("第1次"+ppdd);
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
        					PRODUCT = "DATAFEED金额";
        				}else if(PRODUCT.equals("9")){
        					//订购订单流转
        					PRODUCT = "DATAFEED金额";
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
        				}else if(PRODUCT.equals("18")){
        					PRODUCT = "DATAFEED金额";
        				}
        				

writeLog("第2次"+PRODUCT);
        				 
                    	//有无合同
                    	 String VOUCHER_TYPE ="1";
                    	  
                    	 //订单编号，以YSID年月日4位流水
                    	 String ddbh=Util.null2String(lqys.getString("ddbh"));
                    	 
                    	 String gsid=Util.null2String(lqys.getString("gsid"));
                    	 
                    	 
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
         				 
                    	 String DEADLINE=Util.null2String(lqys.getString("fkqx"));
                    	 
                    	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         				String createDate = sdf.format(new Date());
//         				String sssss=createDate.substring(11, 19);
//         				ORDER_DATE = ORDER_DATE + " " +sssss;
//         				DEADLINE = DEADLINE + " " +sssss;
//         				htksrq = htksrq + " " +sssss;
//          				htyxqend= htyxqend + " " +sssss;
                    	 String STATUS="0";
                    	 
                    	 //如果不是订购订单流转单，则需要推订单（订购订单流转单的业务类型==15）
                    	 if(!ppdd.equals("15")){
                    	//根据收款计划的金额生成订单，推送到财务订单表
                    	 String ddmxid=Util.null2String(lqys.getString("id"));
                    	 llqqyy.executeSql("select * from uf_ddcx_dt1 where mainid = '"+ddmxid+"'");
                    	 //如果添加收款计划
                    	 while(llqqyy.next()){
                    		 String iiiddd=Util.null2String(llqqyy.getString("id"));
                    		 String skje=Util.null2String(llqqyy.getString("skje"));
                    		 Double dddd=Double.valueOf(skje);
                        	 String cccc=String.format("%.2f", dddd);
                        	 
                        	 UUID uuuuid = UUID.randomUUID();
                     		String wwwyid =uuuuid.toString();
                    		 String sqlaa="insert into SYNC_FINANCE_VOUCHER (";
             				String sqlbb=") values (";
             				if(wwwyid !=""){
             					sqlaa +="ID,";
             					sqlbb +="'"+wwwyid+"',";
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
             					sqlbb +="'"+cccc+"',";
             				}
             				if(VOUCHER_TYPE !=""){
             					sqlaa +="VOUCHER_TYPE,";
             					sqlbb +="'"+VOUCHER_TYPE+"',";
             					if(VOUCHER_TYPE.equals("1")){
             						sqlaa +="CONTRACT_CODE,";
             						sqlbb +="'"+htbh+"',";
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
             				if(xjhid.equals("")){
             					sdd.executeSql(sqlaa+sqlbb);
                             	writeLog("订单已经成功推送到财务，订单流水号为："+ddbh+"，订单id为："+wwwyid);
                             	writeLog(sqlaa+sqlbb);
                             	//回写OA订单表，与mysql数据库的订单id保持一致
                             	lqys.executeSql("update uf_ddcx_dt1 set wyid ='"+wwwyid+"' where id ="+iiiddd);
                             	writeLog("update uf_ddcx_dt1 set wyid ='"+wwwyid+"' where id ="+iiiddd);
             				
//                             	lqys.executeSql("update uf_ddcx set status = '2',sh_status = '1',wyid ='"+wyid+"' where id ="+lqyid);
//                             	writeLog("update uf_ddcx set status = '2',sh_status = '1',wyid ='"+wyid+"' where id ="+lqyid);
             				//OA订单和财务只能根据订单编号来进行对接了         OA:ddbh   财务：SID
                             	lqys.executeSql("update uf_ddcx set status = '2',sh_status = '1' where id ="+lqyid);
                             	writeLog("update uf_ddcx set status = '2',sh_status = '1' where id ="+lqyid);
             				}
                    	 }
                    	//如果不添加收款计划
                    	 yyjj.executeSql("select count(*) a from uf_ddcx_dt1 where mainid = '"+ddmxid+"'");
                    	while(yyjj.next()){
                    		String sl=Util.null2String(yyjj.getString("a"));
                    		int ssll=Integer.valueOf(sl);
                    		if(ssll == 0){                              	 
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
                    						sqlbb +="'"+htbh+"',";
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
                    				if(xjhid.equals("")){
                    					sdd.executeSql(sqlaa+sqlbb);
                                    	writeLog("订单已经成功推送到财务，订单流水号为："+ddbh+"，订单id为："+wyid);
                                    	writeLog(sqlaa+sqlbb);
                                    	//回写OA订单表，与mysql数据库的订单id保持一致
                    				
                                    	lqys.executeSql("update uf_ddcx set status = '2',sh_status = '1',wyid ='"+wyid+"' where id ="+lqyid);
                                    	writeLog("update uf_ddcx set status = '2',sh_status = '1',wyid ='"+wyid+"' where id ="+lqyid);
                    				
                    				}
                    		}
                   		
                    	}
                    	 }
                    	financeTest.executeSql("insert into SYNC_FINANCE_CONTRACT (ID,CONTRACT_NAME,"
           						+ "CONTRACT_AMOUNT,START_DATE,END_DATE,PRODUCT,"
           						+ "CUSTOMER,CUSTOMER_CODE,STEP1_TIME,CONTRACT_CODE,CONTRACT_PERSONER)values('"+wyid+"','"+gsmc+
           						"',"+cc+",'"+htksrq+"','"+htyxqend+
           						"','"+PRODUCT
           						+"','"+gsmc+"','"+khbh+"','"+createDatell+
           						"','"+htbh+"','"+lastname+"')");
                         writeLog("insert into SYNC_FINANCE_CONTRACT (ID,CONTRACT_NAME,"
            						+ "CONTRACT_AMOUNT,START_DATE,END_DATE,PRODUCT,"
               						+ "CUSTOMER,CUSTOMER_CODE,STEP1_TIME,CONTRACT_CODE,CONTRACT_PERSONER)values('"+wyid+"','"+gsmc+
               						"',"+cc+",'"+htksrq+"','"+htyxqend+
               						"','"+PRODUCT
               						+"','"+gsmc+"','"+khbh+"','"+createDatell+
               						"','"+htbh+"','"+lastname+"')");
                         financeTest.executeSql("select * from sync_finance_contract where ID ='"+wyid+"'");
                     	if(financeTest.next()){
                     		//给流程赋值，匹配财务id，,并更新status为1，表示合同信息已经成功插入mysql合同表中
                            aa.executeSql("update formtable_main_56_dt1 set status = '1',wyid='"+wyid+"' where id ="+id);
                            writeLog("update formtable_main_56_dt1 set status = '1',wyid='"+wyid+"' where id ="+id);
         	            }
                         
                    }
                     
                     
                 }
				
                 }
				
            
        }catch(Exception e){
            writeLog("业务合同审批推送到金蝶财务系统sync_finance_contract表中写数据出错"+e);
            return "0";
        }
        return Action.SUCCESS;
    }

}
