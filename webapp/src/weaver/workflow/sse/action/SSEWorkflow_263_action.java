package weaver.workflow.sse.action;

import java.util.HashMap;
import java.util.Map;

import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.conn.RecordSet;
import weaver.general.*;
import weaver.interfaces.workflow.action.Action;

public class SSEWorkflow_263_action extends BaseBean implements Action {
    public String execute(RequestInfo request){
    	String result = "1";
        try{
writeLog("第1次");
			RecordSet rs = new RecordSet();
			String requestid = Util.null2String(request.getRequestid());
			String nodetype = Util.null2String(request.getRequestManager().getNodetype());
			int formid = request.getRequestManager().getFormid();
			rs.executeSql("select tablename from workflow_bill where id = " + formid); // 查询工作流单据表的信息
			String billtablename = "";
			if(rs.next()){
				billtablename = Util.null2String(rs.getString("tablename"));          // 获得单据的主表
			}
writeLog("第2次");
			//获得流程主表数据
			Map<String,String> mainTableDataMap = new HashMap<String,String>(); 
			Property[] props = request.getMainTableInfo().getProperty();
			for(int i = 0; i<props.length; i++){
				String fieldname = props[i].getName().toLowerCase();
				String fieldval = Util.null2String(props[i].getValue());
				mainTableDataMap.put(fieldname, fieldval);
			}
writeLog("第3次");
			String ssysnew = Util.null2String(mainTableDataMap.get("ssysnew"));//所属预算id
			String shjg = Util.null2String(mainTableDataMap.get("shjg"));//审核结果
			double bqlxje = Util.getDoubleValue(mainTableDataMap.get("bqlxje"),0);//本期立项金额
			String nd = mainTableDataMap.get("nd");//年度
			//根据节点id执行不同的动作
			//创建节点-冻结预算、扣减可用预算
			String srctype = request.getRequestManager().getSrc();
			writeLog("第4次srctype==========="+srctype);
			writeLog("第5次nodetype==========="+nodetype);
			if(nodetype.equals("0") && "submit".equals(srctype)){
				rs.executeSql("select kyys from uf_yskp where id = "+ssysnew);
				if(rs.next()){
					double kyys = Util.getDoubleValue(rs.getString("kyys"), 0);
					if(bqlxje > kyys){
						request.getRequestManager().setMessage("-1");
						request.getRequestManager().setMessagecontent("本期立项金额不能大于预算可用金额！");
						result = "0";
					}else{
						rs.executeSql("update uf_yskp set kyys = Isnull(kyys,0) - "+bqlxje+",djje = Isnull(djje,0) + "+bqlxje+" where id = "+ssysnew);
					}
				}
			}
writeLog("第6次");
writeLog("第7次srctype==========="+srctype);
writeLog("第8次nodetype==========="+nodetype);
			//部门负责人确认节点,如果审核通过-扣减预算、释放冻结预算
			if((nodetype.equals("1") || nodetype.equals("2")) && "submit".equals(srctype) && shjg.equals("0")){
writeLog("第9次");
				//部门负责人确认时将项目编号字段写入项目预算卡片
				String PRNO = createPRNO(nd);
				//将项目编号写入当前流程
				writeLog("采购立项生成项目编号SQL:update "+billtablename+" set xmbh = '"+PRNO+"' where requestid = "+requestid);
				rs.executeSql("update "+billtablename+" set xmbh = '"+PRNO+"' where requestid = "+requestid);
				rs.executeSql("update uf_yskp set djje = Isnull(djje,0) - "+bqlxje+",lxje = Isnull(lxje,0) + "+bqlxje+" where id = "+ssysnew);
			}
writeLog("第10次");
			//部门负责人确认节点,如果审核不通过-释放冻结预算
			if((nodetype.equals("1") || nodetype.equals("2")) && "submit".equals(srctype) && shjg.equals("1")){
writeLog("第11次");
				rs.executeSql("update uf_yskp set djje = Isnull(djje,0) - "+bqlxje+",kyys = Isnull(kyys,0) + "+bqlxje+" where id = "+ssysnew);
			}
writeLog("第12次");
			//退回创建节点时释放预算
writeLog("第12.1次srctype==========="+srctype);
writeLog("第12.2次nodetype==========="+nodetype);
			/*if("reject".equals(srctype)){ 
writeLog("第13次");
				rs.executeSql("update uf_yskp set djje = Isnull(djje,0) - "+bqlxje+",kyys = Isnull(kyys,0) + "+bqlxje+" where id = "+ssysnew);
			}*/
			if("reject".equals(srctype)&&nodetype.equals("0")){ 
writeLog("第13次");
				rs.executeSql("update uf_yskp set djje = Isnull(djje,0) - "+bqlxje+",kyys = Isnull(kyys,0) + "+bqlxje+" where id = "+ssysnew);
			}
			return result;
        }catch(Exception e){
            writeLog("采购立项 流程出错："+e);
            return "0";
        }
    }
    //生成项目编号
    public String createPRNO(String nd){
		String qz = "PR";
		String nf = converND(nd);
		String lsh = "0001";
    	RecordSet rs = new RecordSet();
    	rs.executeSql("select * from uf_xmbh where nf = '"+nf+"'");
    	if(rs.next()){//如果当期年度已存在prno则在当期流水号上+1
    		lsh = Util.null2String(rs.getString("lsh"));
    		int PRNO = Integer.parseInt(lsh); // 把String类型的lsh转化为int类型的PRNO
            int tmpNum = 10000 + PRNO + 1; // 结果类似10002
            lsh = (tmpNum+"").substring(1);// 结果类似0002
            rs.executeSql("update uf_xmbh set lsh = '"+lsh+"' where nf = '"+nf+"'");
    	}else{
    		rs.executeSql("insert into uf_xmbh(lsh,nf,qz) values('"+lsh+"','"+nf+"','"+qz+"')");
    	}
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
}
