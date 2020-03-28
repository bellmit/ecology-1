package weaver.workflow.sse.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import weaver.soa.workflow.request.Cell;
import weaver.soa.workflow.request.DetailTable;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.Row;
import weaver.conn.RecordSet;
import weaver.general.*;
import weaver.interfaces.workflow.action.Action;

public class SSEWorkflow_277_action extends BaseBean implements Action {
    public String execute(RequestInfo request){
        try{
			RecordSet rs = new RecordSet();
			String requestid = Util.null2String(request.getRequestid());
			String nodetype = Util.null2String(request.getRequestManager().getNodetype());
			int formid = request.getRequestManager().getFormid();
			//获得流程主表数据
			Map<String,String> mainTableDataMap = new HashMap<String,String>(); 
			Property[] props = request.getMainTableInfo().getProperty();
			for(int i = 0; i<props.length; i++){
				String fieldname = props[i].getName().toLowerCase();
				String fieldval = Util.null2String(props[i].getValue());
				mainTableDataMap.put(fieldname, fieldval);
			}
			String dcys = mainTableDataMap.get("dcys");//调出预算
			double dcje = Util.getDoubleValue(mainTableDataMap.get("dcje"),0.00);//调出金额
			String drys = mainTableDataMap.get("drys");//调入预算
			//根据节点id执行不同的动作
			//创建节点-冻结预算卡片当前调出金额，防止当前流程没有审批完成，后续重复提交申请
			String srctype = request.getRequestManager().getSrc();
			if(nodetype.equals("0") && "submit".equals(srctype)){
				//创建节点提交时校验 调出预算是否大于可用预算
				rs.executeSql("select kyys from uf_yskp where id = " + dcys);
				if(rs.next()){
					double kyys = Util.getDoubleValue(rs.getString("kyys"),0);
					if(dcje > kyys){
			            request.getRequestManager().setMessage("-1");
						request.getRequestManager().setMessagecontent("当前调出预算【调出金额】大于【可用预算金额】");
						return "0";
					}
				}
				rs.executeSql("update uf_yskp set kyys = Isnull(kyys,0) - "+dcje+",djje = Isnull(djje,0) + "+dcje+" where id = "+dcys);
			}
			//预算员节点
			if((nodetype.equals("1") || nodetype.equals("2")) && "submit".equals(srctype)){
				//调出预算	冻结金额=冻结金额-调出金额
				rs.executeSql("update uf_yskp set djje = Isnull(djje,0) - "+dcje+",yszje = Isnull(yszje,0) - "+dcje+" where id = "+dcys);
				//调入预算	可用预算=可用预算+调出金额
				rs.executeSql("update uf_yskp set kyys = Isnull(kyys,0) + "+dcje+",yszje = Isnull(yszje,0) + "+dcje+" where id = "+drys);
			}
			//退回创建节点时释放预算
			if("reject".equals(srctype)){
				rs.executeSql("update uf_yskp set kyys = Isnull(kyys,0) + "+dcje+",djje = Isnull(djje,0) - "+dcje+" where id = "+dcys);
			}
        }catch(Exception e){
            writeLog("项目预算调拨 流程出错："+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
