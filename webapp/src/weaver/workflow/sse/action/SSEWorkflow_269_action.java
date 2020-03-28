package weaver.workflow.sse.action;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weaver.soa.workflow.request.Cell;
import weaver.soa.workflow.request.DetailTable;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.Row;
import weaver.conn.RecordSet;
import weaver.conn.RecordSetTrans;
import weaver.general.*;
import weaver.interfaces.datasource.DataSource;
import weaver.interfaces.workflow.action.Action;

public class SSEWorkflow_269_action extends BaseBean implements Action {
    public String execute(RequestInfo request){
    	String result = "1";
    	Connection rs1 = conn();//获取自定义数据源
        try{
			RecordSet rs = new RecordSet();
			String requestid = Util.null2String(request.getRequestid());
			String nodetype = Util.null2String(request.getRequestManager().getNodetype());
			int formid = request.getRequestManager().getFormid();
			rs.executeSql("select tablename from workflow_bill where id = " + formid); // 查询工作流单据表的信息
			String billtablename = "";
			if(rs.next()){
				billtablename = Util.null2String(rs.getString("tablename"));          // 获得单据的主表
			}
			//获得流程主表数据
			rs.executeSql("select nd,id from "+billtablename+" where requestid = " + requestid);
			if(rs.next()){
				String nd = Util.null2String(rs.getString("nd"));//年度
				String id = Util.null2String(rs.getString("id"));//主表id
				//获得流程明细表数据-根据明细表数据做批量操作
				rs.executeSql("select * from "+billtablename+"_dt1 where mainid = " + id);
				int i = 0;
				while(rs.next()){
					String ssysnew = Util.null2String(rs.getString("ysxm"));
					String detailid = Util.null2String(rs.getString("id"));
					double bqlxje = Util.getDoubleValue(rs.getString("lxje"),0);
					ResultSet r = rs1.createStatement().executeQuery("select kyys from uf_yskp where id = "+ssysnew);
					if(r.next()){
						double kyys = Util.getDoubleValue(r.getString("kyys"), 0);
						r.close();
						if(bqlxje > kyys){
							request.getRequestManager().setMessage("-1");
							request.getRequestManager().setMessagecontent("第"+(i+1)+"行明细立项金额必须小于等于可用预算！");
							result = "0";
							rs1.rollback();//回滚
							break;
						}else{
							String PRNO = createPRNO(nd,rs1);
							//将项目编号写入当前流程
							rs1.createStatement().executeUpdate("update "+billtablename+"_dt1 set xmbh = '"+PRNO+"' where id = "+detailid);
							rs1.createStatement().executeUpdate("update uf_yskp set kyys = Isnull(kyys,0) - "+bqlxje+",lxje = Isnull(lxje,0) + "+bqlxje+" where id = "+ssysnew);
						}
					}
					rs1.commit();//验证无误提交事务
					i++;
				}
			}
			return result;
        }catch(Exception e){
            try {
				rs1.rollback();
			} catch (SQLException e1) {
	            writeLog("批量立项审批 流程出错："+e1);
			}//回滚
            return "0";
        }finally{            
    		try {                
    			rs1.close();            
    		} catch (SQLException e) {            
    			writeLog("批量立项JDBC数据源close失败！"); 
    		}        
    	}
    }
    //生成项目编号
    public String createPRNO(String nd,Connection rs1) throws SQLException{
		String qz = "PR";
		String nf = converND(nd);
		String lsh = "0001";
		ResultSet r = rs1.createStatement().executeQuery("select * from uf_xmbh where nf = '"+nf+"'");
    	if(r.next()){//如果当期年度已存在prno则在当期流水号上+1
    		lsh = Util.null2String(r.getString("lsh"));
    		int PRNO = Integer.parseInt(lsh); // 把String类型的lsh转化为int类型的PRNO
            int tmpNum = 10000 + PRNO + 1; // 结果类似10002
            lsh = (tmpNum+"").substring(1);// 结果类似0002
            rs1.createStatement().executeUpdate("update uf_xmbh set lsh = '"+lsh+"' where nf = '"+nf+"'");
    	}else{
    		rs1.createStatement().executeUpdate("insert into uf_xmbh(lsh,nf,qz) values('"+lsh+"','"+nf+"','"+qz+"')");
    	}
    	r.close();
    	return qz+nf+lsh;
    }
    //根据年度所选下拉框将值转换成2018这样
    public String converND(String selectvalue){
    	String nd = "";
    	if(selectvalue.equals("0")){
    		nd = "2018";
    	}else if(selectvalue.equals("1")){
    		nd = "2019";
    	}else if(selectvalue.equals("2")){
    		nd = "2020";
    	}else if(selectvalue.equals("3")){
    		nd = "2021";
    	}else if(selectvalue.equals("4")){
    		nd = "2022";
    	}else if(selectvalue.equals("5")){
    		nd = "2023";
    	}else if(selectvalue.equals("6")){
    		nd = "2024";
    	}else if(selectvalue.equals("7")){
    		nd = "2025";
    	}else if(selectvalue.equals("8")){
    		nd = "2026";
    	}else if(selectvalue.equals("9")){
    		nd = "2027";
    	}else if(selectvalue.equals("10")){
    		nd = "2028";
    	}else if(selectvalue.equals("11")){
    		nd = "2029";
    	}else if(selectvalue.equals("12")){
    		nd = "2030";
    	}
    	return nd;
    }
    public Connection conn(){
    	//调用数据源生成jdbc链接
    	DataSource ds = (DataSource)StaticObj.getServiceByFullname(("datasource.LocalHost"),DataSource.class);
    	//local为配置的数据源标识        
    	Connection conn = null ;        
    	try{            
    		conn = ds.getConnection();      
    		conn.setAutoCommit(false);
    		return conn;
    	}catch(Exception e){            
    		writeLog("批量立项JDBC数据源初始化失败！");   
    		return conn;     
    	}
    }
}
