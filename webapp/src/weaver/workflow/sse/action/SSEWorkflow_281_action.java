package weaver.workflow.sse.action;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import weaver.soa.workflow.request.RequestInfo;
import weaver.conn.RecordSet;
import weaver.general.*;
import weaver.interfaces.datasource.DataSource;
import weaver.interfaces.workflow.action.Action;
/**
 * 2018年7月20日发布版本，针对合同编号去除
 * @author dennis
 *
 */
public class SSEWorkflow_281_action extends BaseBean implements Action {
    public String execute(RequestInfo request){
    	String result = "1";
    	Connection rs1 = conn();//获取自定义数据源
        try{
			RecordSet rs = new RecordSet();
			String requestid = Util.null2String(request.getRequestid());
			String nodetype = Util.null2String(request.getRequestManager().getNodetype());
			String srctype = request.getRequestManager().getSrc();
			int formid = request.getRequestManager().getFormid();
			if((nodetype.equals("1") || nodetype.equals("2")) && "submit".equals(srctype)){
				rs.executeSql("select tablename from workflow_bill where id = " + formid); // 查询工作流单据表的信息
				String billtablename = "";
				if(rs.next()){
					billtablename = Util.null2String(rs.getString("tablename"));          // 获得单据的主表
				}
				//获得流程主表数据
				rs.executeSql("select id from "+billtablename+" where requestid = " + requestid);
				if(rs.next()){
					String id = Util.null2String(rs.getString("id"));//主表id
					//获得流程明细表数据-根据明细表数据做批量操作
					rs.executeSql("select * from "+billtablename+"_dt1 where mainid = " + id);
					while(rs.next()){
						String detailid = Util.null2String(rs.getString("id"));
						//String PRNO = createPRNO(rs1);
						//将项目编号写入当前流程
						//rs1.createStatement().executeUpdate("update "+billtablename+"_dt1 set zhbhtbh = '"+PRNO+"' where id = "+detailid);
						//rs1.commit();//验证无误提交事务
					}
				}
			}
			return result;
        }catch(Exception e){
            try {
				rs1.rollback();
			} catch (SQLException e1) {
	            writeLog("004管理类项目采购_常规采购 流程出错："+e1);
			}//回滚
            return "0";
        }finally{            
    		try {                
    			rs1.close();            
    		} catch (SQLException e) {            
    			writeLog("004管理类项目采购_常规采购JDBC数据源close失败！"); 
    		}        
    	}
    }
    //生成合同编号
/*      public String createPRNO(Connection rs1) throws SQLException{
		String qz = "证信司（";
		String zjz = "）FK";
		SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy");
		String nf = dateFormat_now.format(new Date());
		String lsh = "001";
		ResultSet r = rs1.createStatement().executeQuery("select * from formtable_main_144 where nf = '"+nf+"'");
    	if(r.next()){//如果当期年度已存在prno则在当期流水号上+1
    		lsh = Util.null2String(r.getString("lsh"));
    		int PRNO = Integer.parseInt(lsh); // 把String类型的lsh转化为int类型的PRNO
            int tmpNum = 1000 + PRNO + 1; // 结果类似1002
            lsh = (tmpNum+"").substring(1);// 结果类似002
            rs1.createStatement().executeUpdate("update formtable_main_144 set lsh = '"+lsh+"' where nf = '"+nf+"'");
    	}else{
    		rs1.createStatement().executeUpdate("insert into formtable_main_144(lsh,nf,qz,zjz) values('"+lsh+"','"+nf+"','"+qz+"','"+zjz+"')");
    	}
    	r.close();
    	return qz+nf+zjz+lsh;
    }*/
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
    		writeLog("004管理类项目采购_常规采购JDBC数据源初始化失败！");   
    		return conn;     
    	}
    }
}
