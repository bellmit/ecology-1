package weaver.workflow.sse.action;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.conn.RecordSet;
import weaver.general.*;
import weaver.interfaces.datasource.DataSource;
import weaver.interfaces.workflow.action.Action;

public class SSEWorkflow_278_action extends BaseBean implements Action {
    public String execute(RequestInfo request){
    	String result = "1";
    	Connection rs1 = conn();//获取自定义数据源
        try{
			RecordSet rs = new RecordSet();
			String requestid = Util.null2String(request.getRequestid());
			String nodetype = Util.null2String(request.getRequestManager().getNodetype());
			String srctype = request.getRequestManager().getSrc();
			int formid = request.getRequestManager().getFormid();
			//获得流程主表数据
			Map<String,String> mainTableDataMap = new HashMap<String,String>(); 
			Property[] props = request.getMainTableInfo().getProperty();
			for(int i = 0; i<props.length; i++){
				String fieldname = props[i].getName().toLowerCase();
				String fieldval = Util.null2String(props[i].getValue());
				mainTableDataMap.put(fieldname, fieldval);
			}
			String nd = Util.null2String(mainTableDataMap.get("nd"));//年度
			String PRNO = createPRNO(nd,rs1);
			rs.executeSql("select tablename from workflow_bill where id = " + formid); // 查询工作流单据表的信息
			String billtablename = "";
			if(rs.next()){
				billtablename = Util.null2String(rs.getString("tablename"));          // 获得单据的主表
			}
			//将项目编号写入当前流程
			rs1.createStatement().executeUpdate("update "+billtablename+" set xmbh = '"+PRNO+"' where requestid = "+requestid);
			rs1.commit();//验证无误提交事务
			return result;
        }catch(Exception e){
            try {
				rs1.rollback();
			} catch (SQLException e1) {
	            writeLog("001管理类项目立项审批 流程出错："+e1);
			}//回滚
            return "0";
        }finally{            
    		try {                
    			rs1.close();            
    		} catch (SQLException e) {            
    			writeLog("001管理类项目立项JDBC数据源close失败！"); 
    		}        
    	}
    }
    //生成项目编号
    public String createPRNO(String nd,Connection rs1) throws SQLException{
		String qz = "MGR";
		String nf = converND(nd);
		String lsh = "001";
		ResultSet r = rs1.createStatement().executeQuery("select * from formtable_main_123 where nf = '"+nf+"'");
    	if(r.next()){//如果当期年度已存在prno则在当期流水号上+1
    		lsh = Util.null2String(r.getString("lsh"));
    		int PRNO = Integer.parseInt(lsh); // 把String类型的lsh转化为int类型的PRNO
            int tmpNum = 1000 + PRNO + 1; // 结果类似1002
            lsh = (tmpNum+"").substring(1);// 结果类似002
            rs1.createStatement().executeUpdate("update formtable_main_123 set lsh = '"+lsh+"' where nf = '"+nf+"'");
    	}else{
    		rs1.createStatement().executeUpdate("insert into formtable_main_123(lsh,nf,qz) values('"+lsh+"','"+nf+"','"+qz+"')");
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
    		writeLog("001管理类项目立项JDBC数据源初始化失败！");   
    		return conn;     
    	}
    }
}
