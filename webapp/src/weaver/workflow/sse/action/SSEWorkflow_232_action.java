package weaver.workflow.sse.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.conn.RecordSet;
import weaver.general.*;
import weaver.interfaces.workflow.action.Action;

public class SSEWorkflow_232_action extends BaseBean implements Action {
    public String execute(RequestInfo request){
    	String result = "1";
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
			Map<String,String> mainTableDataMap = new HashMap<String,String>(); 
			Property[] props = request.getMainTableInfo().getProperty();
			for(int i = 0; i<props.length; i++){
				String fieldname = props[i].getName().toLowerCase();
				String fieldval = Util.null2String(props[i].getValue());
				mainTableDataMap.put(fieldname, fieldval);
			}
			String xmmc = mainTableDataMap.get("xmmc");//所属项目id
			String zfzhcg = mainTableDataMap.get("zfzhcg");//是否最后一次采购
			double dqkyje = Util.getDoubleValue(mainTableDataMap.get("dqkyje"),0.00);//项目当前可用金额
			double htzzje = Util.getDoubleValue(mainTableDataMap.get("htzzje"),0.00);//合同最终金额
			double cgsqje = Util.getDoubleValue(mainTableDataMap.get("applymoney"),0.00);//采购申请金额（元）
			writeLog("项目采购专题采购:cgsqje="+cgsqje);
			String ssys1 = mainTableDataMap.get("ssys1");//所属预算id
			//根据节点id执行不同的动作
			//创建节点-冻结项目卡片当前可用金额，防止当前流程没有审批完成，后续重复提交申请
			String srctype = request.getRequestManager().getSrc();
			if(nodetype.equals("0") && "submit".equals(srctype)){
				rs.executeSql("select xmkyje from uf_xmkp where id = "+xmmc);
				if(rs.next()){
					double xmkyje = Util.getDoubleValue(rs.getString("xmkyje"), 0);
					if(cgsqje > xmkyje){
						request.getRequestManager().setMessage("-1");
						request.getRequestManager().setMessagecontent("采购申请金额不能大于项目当前可用金额！");
						result = "0";
					}else{
						writeLog("项目采购专题采购提交冻结SQL:update uf_xmkp set xmkyje = Isnull(xmkyje,0) - "+cgsqje+",djje = Isnull(djje,0) + "+cgsqje+" where id = "+xmmc);
						rs.executeSql("update uf_xmkp set xmkyje = Isnull(xmkyje,0) - "+cgsqje+",djje = Isnull(djje,0) + "+cgsqje+" where id = "+xmmc);
					}
				}
			}
			//被授权人确认节点-释放项目卡片 冻结金额、当前可用金额=流程中项目当前可用金额-合同金额
			//更新项目卡片中专题采购金额=合同金额累加
			if((nodetype.equals("1") || nodetype.equals("2")) && "submit".equals(srctype)){
				//如果是最后一次采购申请,截止本次采购项目可用金额还原到预算卡片中当前可用预算
				//生成明细表里面的合同编号
				//获得流程主表数据
				//rs.executeSql("select id from "+billtablename+" where requestid = " + requestid);
				/*if(rs.next()){
					String mainid = Util.null2String(rs.getString("id"));
					rs.executeSql("select id from "+billtablename+"_dt1 where mainid = " + mainid);
					while(rs.next()){
						//String detailid = Util.null2String(rs.getString("id"));
						//rs.executeSql("update "+billtablename+"_dt1 set zhbhtbh = '"+createHTNO()+"' where id = "+detailid);
					}
				}*/
				if(zfzhcg.equals("0")){
					rs.executeSql("select xmkyje from uf_xmkp where id = " + xmmc);
					if(rs.next()){
						double xmkyje = Util.getDoubleValue(rs.getString("xmkyje"),0);
						rs.executeSql("update uf_yskp set kyys = Isnull(kyys,0) + "+(cgsqje-htzzje+xmkyje)+",sjcgje = Isnull(sjcgje,0) + "+htzzje+" where id = "+ssys1);
					}
					rs.executeSql("update uf_xmkp set djje = Isnull(djje,0) - "+cgsqje+",xmkyje = 0,cgsqje = Isnull(cgsqje,0) + "+htzzje+" where id = "+xmmc);
				}else{
					rs.executeSql("update uf_yskp set sjcgje = Isnull(sjcgje,0) + "+htzzje+" where id = "+ssys1);
					//writeLog("=========================update uf_xmkp set djje = 0,xmkyje = "+(dqkyje-htzzje)+",cgsqje = Isnull(cgsqje,0) + "+htzzje+" where id = "+xmmc);
					rs.executeSql("update uf_xmkp set djje = Isnull(djje,0) - "+cgsqje+",xmkyje = Isnull(xmkyje,0) + "+(cgsqje-htzzje)+",cgsqje = Isnull(cgsqje,0) + "+htzzje+" where id = "+xmmc);
				}
			}
			//退回创建节点时释放预算
			if("reject".equals(srctype)){
				rs.executeSql("update uf_xmkp set xmkyje = Isnull(xmkyje,0) + "+cgsqje+",djje = Isnull(djje,0) - "+cgsqje+" where id = "+xmmc);
			}
			return result;
        }catch(Exception e){
            writeLog("项目采购专题采购 流程出错："+e);
            return "0";
        }
    }
  //生成合同编号  
/*    public String createHTNO(){
		String qz = "证信司（";
		String zjz = "）FK";
		SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy");
		String nf = dateFormat_now.format(new Date());;
		String lsh = "001";
    	RecordSet rs = new RecordSet();
    	rs.executeSql("select * from uf_htbh where nf = '"+nf+"'");
    	if(rs.next()){//如果当期年度已存在prno则在当期流水号上+1
    		lsh = Util.null2String(rs.getString("lsh"));
    		int PRNO = Integer.parseInt(lsh); // 把String类型的lsh转化为int类型的PRNO
            int tmpNum = 1000 + PRNO + 1; // 结果类似1002
            lsh = (tmpNum+"").substring(1);// 结果类似002
            rs.executeSql("update uf_htbh set lsh = '"+lsh+"' where nf = '"+nf+"'");
    	}else{
    		rs.executeSql("insert into uf_htbh(lsh,nf,qz,zjz) values('"+lsh+"','"+nf+"','"+qz+"','"+zjz+"')");
    	}
    	return qz+nf+zjz+lsh;
    }*/
}
