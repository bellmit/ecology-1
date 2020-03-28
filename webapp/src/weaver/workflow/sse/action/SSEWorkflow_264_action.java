package weaver.workflow.sse.action;

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

public class SSEWorkflow_264_action extends BaseBean implements Action {
    public String execute(RequestInfo request){
        try{
			RecordSet rs = new RecordSet();
			String nodetype = Util.null2String(request.getRequestManager().getNodetype());
			//获得流程主表数据
			Map<String,String> mainTableDataMap = new HashMap<String,String>(); 
			Property[] props = request.getMainTableInfo().getProperty();
			for(int i = 0; i<props.length; i++){
				String fieldname = props[i].getName().toLowerCase();
				String fieldval = Util.null2String(props[i].getValue());
				mainTableDataMap.put(fieldname, fieldval);
			}
			String sqlx = Util.null2String(mainTableDataMap.get("sqlx"));//申请类型 
			String fklx = Util.null2String(mainTableDataMap.get("fklx"));//付款类型
			String srctype = request.getRequestManager().getSrc();
			writeLog("srctype==========="+srctype);
			if((sqlx.equals("0") || sqlx.equals("1")) && fklx.equals("0")){//专题采购合同付款、常规采购-合同付款
				double bcsqje = Util.getDoubleValue(mainTableDataMap.get("bcsqje"),0.00);//有合同&非框架合同-本次申请金额
writeLog("有合同&非框架合同-本次申请金额："+bcsqje);		
				String htmc = Util.null2String(mainTableDataMap.get("htmc"));//合同卡片-合同名称
				//根据节点id执行不同的动作
				//创建节点-扣减合同卡片可支付金额/增加合同卡片已支付金额
				if(nodetype.equals("0") && "submit".equals(srctype)){
					rs.executeSql("update uf_htkp set sqzfje = Isnull(sqzfje,0) - "+bcsqje+"," +
							"zfje = Isnull(zfje,0) + "+bcsqje+" where id = "+htmc);
				}
				//财务出纳节点-修改付款计划卡片中支付状态为已支付-----明细表
				if((nodetype.equals("1") || nodetype.equals("2")) && "submit".equals(srctype)){
					//获得流程明细表数据-根据明细表数据做批量操作
					DetailTable dtltable =  request.getDetailTableInfo().getDetailTable(0);
					Row[] rows = dtltable.getRow();
					for(int i = 0; i< rows.length; i++){
						Row row = rows[i];
						Cell[] cells = row.getCell();
						String htmc_detal = "";
						for(int j=0; j<cells.length; j++){
							Cell cell = cells[j];
							//onerow.put(cell.getName(), Util.null2String(cell.getValue()));
							if(cell.getName().equals("htmc")){
								htmc_detal = cell.getValue();//付款计划-合同名称
								break;
							}
						}
						rs.executeSql("update uf_fkjhkp set zfzt = 0 where id = "+htmc_detal);
					}
				}
				//退回创建节点时-增加合同卡片可支付金额/扣减合同卡片已支付金额
				if("reject".equals(srctype)){
					rs.executeSql("update uf_htkp set sqzfje = Isnull(sqzfje,0) + "+bcsqje+",zfje = Isnull(zfje,0) - "+bcsqje+" where id = "+htmc);
				}
			}else if(sqlx.equals("1") && fklx.equals("1")){//常规采购&无合同付款
				double kjhtksqje = Util.getDoubleValue(mainTableDataMap.get("kjhtksqje"),0.00);//无合同&框架合同-本次申请金额
				String cgkp = Util.null2String(mainTableDataMap.get("cgkp"));//常规采购卡片
				//根据节点id执行不同的动作
				//创建节点-扣减常规采购卡片当前可用金额/增加常规采购卡片中冻结金额
				if(nodetype.equals("0") && "submit".equals(srctype)){
					rs.executeSql("update uf_cgcgkp set kcyje = Isnull(kcyje,0) - "+kjhtksqje+",djje = Isnull(djje,0) + "+kjhtksqje+" where id = "+cgkp);
				}
				//财务出纳节点-释放常规采购卡片中冻结金额/增加常规采购卡片中实际支付金额（无合同付款）
				if((nodetype.equals("1") || nodetype.equals("2")) && "submit".equals(srctype)){
					rs.executeSql("update uf_cgcgkp set sjfkwht = Isnull(sjfkwht,0) + "+kjhtksqje+",djje = Isnull(djje,0) - "+kjhtksqje+" where id = "+cgkp);
				}
				//退回创建节点时-增加常规采购卡片当前可用金额/扣减常规采购卡片中冻结金额
				if("reject".equals(srctype)){
					rs.executeSql("update uf_cgcgkp set kcyje = Isnull(kcyje,0) + "+kjhtksqje+",djje = Isnull(djje,0) - "+kjhtksqje+" where id = "+cgkp);
				}
			}else if(sqlx.equals("2") && fklx.equals("2")){//框架合同付款-框架合同付款
				double kjhtksqje = Util.getDoubleValue(mainTableDataMap.get("kjhtksqje"),0.00);//无合同&框架合同-本次申请金额
				String htmc = Util.null2String(mainTableDataMap.get("htmc"));//合同卡片-合同名称
				//根据节点id执行不同的动作
				//创建节点-扣减预算卡片当前可用金额/增加预算卡片中冻结金额
				if(nodetype.equals("0") && "submit".equals(srctype)){
					rs.executeSql("update uf_htkp set djje = Isnull(djje,0) + "+kjhtksqje+",sqzfje = Isnull(sqzfje,0) - "+kjhtksqje+" where id = "+htmc);
				}
				//财务出纳节点-释放预算卡片中冻结金额/增加预算卡片中实际发生预算金额
				if((nodetype.equals("1") || nodetype.equals("2")) && "submit".equals(srctype)){
					rs.executeSql("update uf_htkp set djje = Isnull(djje,0) - "+kjhtksqje+",zfje = Isnull(zfje,0) + "+kjhtksqje+" where id = "+htmc);
				}
				//退回创建节点时-增加预算卡片当前可用金额/扣减预算卡片中冻结金额
				if("reject".equals(srctype)){
					rs.executeSql("update uf_htkp set djje = Isnull(djje,0) - "+kjhtksqje+",sqzfje = Isnull(sqzfje,0) + "+kjhtksqje+" where id = "+htmc);
				}
			}
        }catch(Exception e){
            writeLog("付款审批流程new 出错："+e);
            return "0";
        }
        return Action.SUCCESS;
    }
}
