package weaver.interfaces.workflow.action;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class CRM_ContractExten extends BaseBean
implements Action
{
	public String execute(RequestInfo request)
	{
		try
		{
			writeLog("自动顺延合同--开始");
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet(); 
			RecordSet rs3 = new RecordSet(); 
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			RecordSet rs6 = new RecordSet();
			RecordSet rs7 = new RecordSet();
			int modeId=112; //建模id
			String billid = request.getRequestid();
			String sql="";
			String sql2="";
			sql="select contract,contracttype from uf_crm_extension where requestid="+billid;
			rs.executeSql(sql);
			if (rs.next()) {     
				String  type = Util.null2String(rs.getString("contracttype"));//类型	
				String  contract = Util.null2String(rs.getString("contract"));//合同
				
				if(type.equals("1")){//终止      	    
					sql="update uf_crm_contract set status='1' where id='"+contract+"'";
					rs1.executeSql(sql);    			
				}else{//自动顺延  
					//查合同信息
					sql="select extendtype,extendcycle from uf_crm_contract where id='"+contract+"'";
					rs2.executeSql(sql);
					if (rs2.next()) {     
						String  extendtype = Util.null2String(rs2.getString("extendtype"));//延展方式	
						String  extendcycle = Util.null2String(rs2.getString("extendcycle"));//周期
						
						if(extendtype.equals("1")){//自动顺延							
							
				  			UUID uuid = UUID.randomUUID();
				  	      	String  wyid =uuid.toString();//生成唯一的标识码     
				  	      	//复制合同记录
				  	      	sql="insert into uf_crm_contract(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,"
				  	      	+"customer,business,contractname,contractno,contracttype,contractrelation,extendtype,extendcycle" +
				  	      	",formcontract,contract,iffixed,contractmoney,finalizedfile,contractperiod,startdate,enddate,signdate,contractfile,expressfile,remark,status,flowid,uuid) " +
				  	      	"select "+modeId+",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"+
				  	      	"customer,business,contractname,contractno,contracttype,contractrelation,extendtype,extendcycle " +
				  	      	",formcontract,'"+contract+"',iffixed,contractmoney,finalizedfile,contractperiod,startdate,enddate,signdate,contractfile,expressfile,remark,0,'"+billid+"','"+wyid+"'" +
				  	      	" from uf_crm_contract where id='"+contract+"' ";
				  	        writeLog("自动顺延合同复制合同sql="+sql);
				  	      	rs3.executeSql(sql);      	
				  	      	sql="select id from uf_crm_contract where uuid='"+wyid+"'";
				  	      	rs4.executeSql(sql);
				  	        if (rs4.next()){
				  	  			String id = Util.null2String(rs4.getString("id"));//查询合同id
				  	  			int logid=Integer.parseInt(id);  			
				  	  			ModeRightInfo ModeRightInfo = new ModeRightInfo();
				  	  			ModeRightInfo.editModeDataShare(5,modeId,logid);//新建的时候添加共享-所有人
				  	  			
				  	  		    //复制收款计划
				  	  	        sql="insert into uf_crm_contract_dt1(mainid,money,usernumber,limitdate,cycle,currentmoney,remark) " +
				  	  	        " select "+logid+",money,usernumber,limitdate,cycle,currentmoney,remark " +
				  	  	        " from uf_crm_contract_dt1 where (isnocancel is null or isnocancel='' or isnocancel='1') and mainid="+contract;
				  	    	    rs5.executeSql(sql);
				  	    	    
				  	    	    
				  	    	   if(extendcycle.equals("0")){//每年
									sql="update uf_crm_contract set enddate=convert(varchar(10),DATEADD(MONTH,12,enddate),120) where id='"+logid+"'";
									sql2="update uf_crm_contract_dt1 set limitdate=convert(varchar(10),DATEADD(MONTH,12,limitdate),120) where mainid='"+logid+"'";
									
								}else if(extendcycle.equals("1")){//每半年
									sql="update uf_crm_contract set enddate=convert(varchar(10),DATEADD(MONTH,6,enddate),120) where id='"+logid+"'";
									sql2="update uf_crm_contract_dt1 set limitdate=convert(varchar(10),DATEADD(MONTH,6,limitdate),120) where mainid='"+logid+"'";
									
								}else if(extendcycle.equals("2")){//每季度
									sql="update uf_crm_contract set enddate=convert(varchar(10),DATEADD(MONTH,3,enddate),120) where id='"+logid+"'"; 
									sql2="update uf_crm_contract_dt1 set limitdate=convert(varchar(10),DATEADD(MONTH,3,limitdate),120) where mainid='"+logid+"'";
									
								}else if(extendcycle.equals("3")){//每月
									sql="update uf_crm_contract set enddate=convert(varchar(10),DATEADD(MONTH,1,enddate),120) where id='"+logid+"'"; 
									sql2="update uf_crm_contract_dt1 set limitdate=convert(varchar(10),DATEADD(MONTH,1,limitdate),120) where mainid='"+logid+"'";
									
								}else if(extendcycle.equals("4")){//无
									writeLog("自动顺延合同--无周期");																
								}         	
								rs6.executeSql(sql);								
				  	    	    rs7.executeSql(sql2);								
				  	  		 }	
				  	        
				  	         //更新原合同为终止
				  	        sql="update uf_crm_contract set status='1' where id='"+contract+"'";
				  	        writeLog("更新原合同为终止sql="+sql);
							rs1.executeSql(sql);
							
							
						}else{
							writeLog("自动顺延合同--不是自动顺延合同");
							return "0";							
						}            	
					} 	
					
				}      
				writeLog("自动顺延合同--结束");
			}    
		}
		catch (Exception e) {
			writeLog("自动顺延合同--出错" + e);
			return "0";
		}
		return "1";
	}
}