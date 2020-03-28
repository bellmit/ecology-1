package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;
/**
 * 资金退回财务出纳时生成uuid和台账日期
 * @author lsq
 * @date 2019/8/6
 * @version 1.0
 */
public class PayList_projectPay_ZJTK extends BaseBean implements Action{

	@Override
	public String execute(RequestInfo request) {
        try {
        	RecordSet rs=new RecordSet();
        	RecordSet rs1=new RecordSet();
        	RecordSet rs2=new RecordSet();
			String requestid=request.getRequestid();
    		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
    		String date=format.format(new Date());
        	String sql="select u.proposer,u.department,u.flowTitle," +
        			"d.id,d.dealStatus,d.accountName,d.bankAccount,d.bankBench,d.money," +
        			"d.localProvence,d.belongProvince,d.belongCity,d.showOrHide,d.usePay,u.payStatus,u.accountBankBrow from uf_CapitalRefund u inner join uf_CapitalRefund_dt1 d on u.id=d.mainid where requestid='"+requestid+"'";
        	rs.executeSql(sql);
        	while(rs.next()){
        		String detailid=Util.null2String(rs.getString("id"));   //明细表id
        		String sql1="update uf_CapitalRefund_dt1 set standingDate='"+date+"',uuid=(select SUBSTRING(uuid,1,8)+SUBSTRING(uuid,10,4)+SUBSTRING(uuid,15,4)+SUBSTRING(uuid,20,4)+SUBSTRING(uuid,25,12) from (select cast(NEWID() as varchar(36)) as uuid ) s) where id='"+detailid+"'";
        		rs1.executeSql(sql1);
        		
        		String proposer=Util.null2String(rs.getString("proposer"));   //申请人
        		String department=Util.null2String(rs.getString("department"));   //申请人部门
        		String flowTitle=Util.null2String(rs.getString("flowTitle"));   //流程标题
        		String dealStatus=Util.null2String(rs.getString("dealStatus"));   //审核状态
        		String accountName=Util.null2String(rs.getString("accountName"));   //开户名称
        		String bankAccount=Util.null2String(rs.getString("bankAccount"));   //银行账户
        		String bankBench=Util.null2String(rs.getString("bankBench"));   //开户银行
        		String money=Util.null2String(rs.getString("money"));   //金额
        		String localProvence=Util.null2String(rs.getString("localProvence"));   //所属省/市
        		String belongProvince=Util.null2String(rs.getString("belongProvince"));   //所属省
        		String belongCity=Util.null2String(rs.getString("belongCity"));   //所属市
        		String showOrHide=Util.null2String(rs.getString("showOrHide"));   //显示/移出
        		String usePay=Util.null2String(rs.getString("usePay"));   //付款用途
        		String payStatus=Util.null2String(rs.getString("payStatus"));   //支付状态
        		String accountBankBrow=Util.null2String(rs.getString("accountBankBrow"));   //银行账户选择
        		String sql2="";
        		rs2.executeSql(sql2);
        		
        		
        		
        	}
		} catch (Exception e) {
		    writeLog("资金退回财务出纳时生成uuid和台账日期异常:"+e);
		    return "0";
		}
		return "1";
	}
}
