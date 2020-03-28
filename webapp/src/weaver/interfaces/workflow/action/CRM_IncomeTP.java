package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class CRM_IncomeTP extends BaseBean
        implements Action {
    public String execute(RequestInfo request) {
        try {
            writeLog("订单变更申请--开始");
            RecordSet rs = new RecordSet();
            RecordSet rs1 = new RecordSet();
            RecordSet rs2 = new RecordSet();
            RecordSet rs3 = new RecordSet();
            RecordSet rs4 = new RecordSet();
            String billid = request.getRequestid();
            String sql = "";
            String business = "";
            String customer = "";
            String orderlist = "";
            sql = "select orderlist,customer,business from uf_crm_invoiceret where requestid=" + billid;
            rs.executeSql(sql);
            if (rs.next()) {
                business = Util.null2String(rs.getString("business"));//所属业务
                customer = Util.null2String(rs.getString("customer"));//客户
                orderlist = Util.null2String(rs.getString("orderlist"));//原订单

                //更新原订单的订单状态
                sql = "update uf_crm_orderinfo set status='1',tpflowid='" + billid + "' where id= '" + orderlist + "'";
                rs1.executeSql(sql);

                //将原订单信息更新到新订单信息上
                sql = "select contract,product,-isnull(ordermoney,0) as ordermoney," +
                        " unitname,creditcode,address,accountname,invoicetype,-isnull(currentmoney,0) as currentmoney   from uf_crm_orderinfo where id=" + orderlist;
                rs2.executeSql(sql);
                if (rs2.next()) {
                    String contract = Util.null2String(rs2.getString("contract"));//
                    String product = Util.null2String(rs2.getString("product"));//
                    String ordermoney = Util.null2String(rs2.getString("ordermoney"));//
                    String unitname = Util.null2String(rs2.getString("unitname"));//
                    String creditcode = Util.null2String(rs2.getString("creditcode"));//
                    String address = Util.null2String(rs2.getString("address"));//
                    String accountname = Util.null2String(rs2.getString("accountname"));//
                    String invoicetype = Util.null2String(rs2.getString("invoicetype"));//
                    String currentmoney = Util.null2String(rs2.getString("currentmoney"));//

                    sql = "update uf_crm_orderinfo set contract='" + contract + "',product='" + product + "',ordermoney='" + ordermoney + "'," +
                            " paydate=convert(varchar(10),dateadd(ms,-3,DATEADD(mm, DATEDIFF(m,0,orderdate)+1, 0)),120), status='0',kpstatus='0',skstatus='0',unitname='" + unitname + "',creditcode='" + creditcode + "',address='" + address + "',accountname='" + accountname + "'," +
                            " invoicetype='" + invoicetype + "',currentmoney='" + currentmoney + "'  where flowid='" + billid + "'";
                    rs3.executeSql(sql);

                    //更新客户业务卡片中的开票金额
                    sql = "update uf_crm_custbusiness set invoicemoney= isnull(invoicemoney,0)+isnull((select -currentmoney from uf_crm_orderinfo where id='" + orderlist + "'),0) where customer='" + customer + "' and business='" + business + "'";
                    rs4.executeSql(sql);
                }
            }

            writeLog("订单变更--结束");
        } catch (Exception e) {
            writeLog("订单变更--出错" + e);
            return "0";
        }
        return "1";
    }
}