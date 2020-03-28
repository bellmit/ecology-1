package weaver.workflow.sse.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weaver.soa.workflow.request.Cell;
import weaver.soa.workflow.request.DetailTable;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.Row;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.*;
import weaver.hrm.resource.ResourceComInfo;
import weaver.hrm.resource.ResourceUtil;
import weaver.interfaces.workflow.action.Action;
import wsvoucher.client.WSWSVoucher;

public class SSEWorkflow_ImportEAS_283_action extends BaseBean implements Action {
    public String execute(RequestInfo request){
    	String result = "1";
        try{
			RecordSet rs = new RecordSet();
			String requestid = Util.null2String(request.getRequestid());
			String nodetype = Util.null2String(request.getRequestManager().getNodetype());
			//获得流程主表数据
			Map<String,String> mainTableDataMap = new HashMap<String,String>(); 
			Property[] props = request.getMainTableInfo().getProperty();
			for(int i = 0; i < props.length; i++){
				String fieldname = props[i].getName().toLowerCase();
				String fieldval = Util.null2String(props[i].getValue());
				mainTableDataMap.put(fieldname, fieldval);
			}
			String companyNumber = "FHG002";//公司编码
			SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd");
			String bookedDate = dateFormat_now.format(new Date());//记账日期
			String bizDate = dateFormat_now.format(new Date());//业务日期
			SimpleDateFormat dateFormat_now1 = new SimpleDateFormat("yyyy");
			int periodYear = Util.getIntValue(dateFormat_now1.format(new Date()),0);//会计期间-年 
			SimpleDateFormat dateFormat_now2 = new SimpleDateFormat("MM");
			int periodNumber = Util.getIntValue(dateFormat_now2.format(new Date()),0);//会计期间-月
			//ResourceUtil rsutil = new ResourceUtil();
			//String lastname = rsutil.getHrmShowName(Util.null2String(mainTableDataMap.get("bxr")));//申请人姓名
			String currencyNumber = "BB01";//币种
			double localRate = 1.00;//汇率
			String creator = Util.null2String(Prop.getPropValue("sse_eas", "creator"));//制单人 
			int attaches = Util.getIntValue(Util.null2String(mainTableDataMap.get("fjzs")), 0);//附件数量
			//String voucherNumber = "";//凭证号
			String voucherType = convervoucherType(Util.null2String(mainTableDataMap.get("fkfs")));//凭证字
			String sfgdzcfk = Util.null2String(mainTableDataMap.get("sfgdzcfk"));//是否固定资产付款
			String xmmc = Util.null2String(mainTableDataMap.get("xmmc"));//项目
			String lcbh = Util.null2String(mainTableDataMap.get("bh"));//流程编号
			String asstActType2 = "项目";
			String asstActType21 = getprjcode(xmmc);//核算项目2-编码
			String asstActType22 = getprjname(xmmc);//核算项目2-名称
			String bm = Util.null2String(mainTableDataMap.get("bm"));//部门
			String asstActType1 = "部门";
			String asstActType11 = getdepartmentcode(bm);//核算项目1-编码
			String asstActType12 = getdepartmentname(bm);//核算项目1-名称
			//获得流程明细表数据-根据明细表数据做批量操作
			DetailTable dtltable =  request.getDetailTableInfo().getDetailTable(0);
			Row[] rows = dtltable.getRow();
			//构建数组 ，一个凭证一个数组
			int length = 0;
			for(int i = 0; i< rows.length; i++){
				Row row = rows[i];
				Cell[] cells = row.getCell();
				String fplx = "";//发票类型
				for(int j=0; j<cells.length; j++){
					Cell cell = cells[j];
					if(cell.getName().equals("fplx")){
						fplx = Util.null2String(cell.getValue());
					}
				}
				if(fplx.equals("0") && !sfgdzcfk.equals("0")){
					length += 2;
				}else {
					length += 1;
				}
			}
			WSWSVoucher[] vochers = new WSWSVoucher[length+1];
			int entryDC = 1;//分录行方向
			int entrySeq = 0;//分录行号
			double creditAmount = 0.00;//贷方金额
			if(!sfgdzcfk.equals("0")){//如果有税额&发票类型为增票&不是固定资产，则一行明细两个分录
				String accountNumber = "";//科目编码
				for(int i = 0; i< rows.length; i++){
					Row row = rows[i];
					Cell[] cells = row.getCell();
					double se = 0.00;//税金额
					double bhsje = 0.00;//不含税金额
					for(int j=0; j<cells.length; j++){
						Cell cell = cells[j];
						if(cell.getName().equals("se")){
							se = Util.getDoubleValue(cell.getValue(),0);
							creditAmount += se;
						}else if(cell.getName().equals("fykm")){
							accountNumber = getaccountNumber(Util.null2String(cell.getValue()));
						}else if(cell.getName().equals("bhsje")){
							bhsje = Util.getDoubleValue(cell.getValue(),0);
							creditAmount += bhsje;
						}
					}
					double originalAmount = se;//原币金额
					double debitAmount = se;//借方金额
					String accountNumber1 = "2221.08.01";
					vochers[entrySeq] = new WSWSVoucher();
					vochers[entrySeq].setCompanyNumber(companyNumber);
					vochers[entrySeq].setBookedDate(bookedDate);
					vochers[entrySeq].setBizDate(bizDate);
					vochers[entrySeq].setPeriodYear(periodYear);
					vochers[entrySeq].setPeriodNumber(periodNumber);
					vochers[entrySeq].setVoucherType(voucherType);
					vochers[entrySeq].setAttaches(attaches);
					vochers[entrySeq].setVoucherNumber("");
					vochers[entrySeq].setEntrySeq(entrySeq+1);
					vochers[entrySeq].setVoucherAbstract("");
					vochers[entrySeq].setAccountNumber(accountNumber1);
					vochers[entrySeq].setCurrencyNumber(currencyNumber);
					vochers[entrySeq].setLocalRate(localRate);
					vochers[entrySeq].setEntryDC(entryDC);
					vochers[entrySeq].setOriginalAmount(originalAmount);
					vochers[entrySeq].setDebitAmount(debitAmount);
					vochers[entrySeq].setCreator(creator);
					vochers[entrySeq].setAsstActType1(asstActType1);
					vochers[entrySeq].setAsstActName1(asstActType12);
					vochers[entrySeq].setAsstActNumber1(asstActType11);
					vochers[entrySeq].setAsstActType2(asstActType2);
					vochers[entrySeq].setAsstActName2(asstActType22);
					vochers[entrySeq].setAsstActNumber2(asstActType21);
					vochers[entrySeq].setDescription(lcbh);
					entrySeq++;
					//不含税分录
					vochers[entrySeq] = new WSWSVoucher();
					vochers[entrySeq].setCompanyNumber(companyNumber);
					vochers[entrySeq].setBookedDate(bookedDate);
					vochers[entrySeq].setBizDate(bizDate);
					vochers[entrySeq].setPeriodYear(periodYear);
					vochers[entrySeq].setPeriodNumber(periodNumber);
					vochers[entrySeq].setVoucherType(voucherType);
					vochers[entrySeq].setAttaches(attaches);
					vochers[entrySeq].setVoucherNumber("");
					vochers[entrySeq].setEntrySeq(entrySeq+1);
					vochers[entrySeq].setVoucherAbstract("");
					vochers[entrySeq].setAccountNumber(accountNumber);
					vochers[entrySeq].setCurrencyNumber(currencyNumber);
					vochers[entrySeq].setLocalRate(localRate);
					vochers[entrySeq].setEntryDC(entryDC);
					vochers[entrySeq].setOriginalAmount(bhsje);
					vochers[entrySeq].setDebitAmount(bhsje);
					vochers[entrySeq].setCreator(creator);
					vochers[entrySeq].setAsstActType1(asstActType1);
					vochers[entrySeq].setAsstActName1(asstActType12);
					vochers[entrySeq].setAsstActNumber1(asstActType11);
					vochers[entrySeq].setAsstActType2(asstActType2);
					vochers[entrySeq].setAsstActName2(asstActType22);
					vochers[entrySeq].setAsstActNumber2(asstActType21);
					vochers[entrySeq].setDescription(lcbh);
					entrySeq++;
				}
			}else{
				for(int i = 0; i< rows.length; i++){
					Row row = rows[i];
					Cell[] cells = row.getCell();
					String accountNumber = "";//科目编码
					double fkje = 0.00;//总金额
					for(int j=0; j<cells.length; j++){
						Cell cell = cells[j];
						if(cell.getName().equals("fkje")){
							fkje = Util.getDoubleValue(cell.getValue(),0);
							creditAmount += fkje;
						}else if(cell.getName().equals("fykm")){
							accountNumber = getaccountNumber(Util.null2String(cell.getValue()));
						}
					}
					double originalAmount = fkje;//原币金额
					double debitAmount = fkje;//借方金额
					vochers[entrySeq] = new WSWSVoucher();
					vochers[entrySeq].setCompanyNumber(companyNumber);
					vochers[entrySeq].setBookedDate(bookedDate);
					vochers[entrySeq].setBizDate(bizDate);
					vochers[entrySeq].setPeriodYear(periodYear);
					vochers[entrySeq].setPeriodNumber(periodNumber);
					vochers[entrySeq].setVoucherType(voucherType);
					vochers[entrySeq].setAttaches(attaches);
					vochers[entrySeq].setVoucherNumber("");
					vochers[entrySeq].setEntrySeq(entrySeq+1);
					vochers[entrySeq].setVoucherAbstract("");
					vochers[entrySeq].setAccountNumber(accountNumber);
					vochers[entrySeq].setCurrencyNumber(currencyNumber);
					vochers[entrySeq].setLocalRate(localRate);
					vochers[entrySeq].setEntryDC(entryDC);
					vochers[entrySeq].setOriginalAmount(originalAmount);
					vochers[entrySeq].setDebitAmount(debitAmount);
					vochers[entrySeq].setCreator(creator);
					vochers[entrySeq].setAsstActType1(asstActType1);
					vochers[entrySeq].setAsstActName1(asstActType12);
					vochers[entrySeq].setAsstActNumber1(asstActType11);
					vochers[entrySeq].setAsstActType2(asstActType2);
					vochers[entrySeq].setAsstActName2(asstActType22);
					vochers[entrySeq].setAsstActNumber2(asstActType21);
					vochers[entrySeq].setDescription(lcbh);
					entrySeq++;
				}	
			}
			//贷方
			vochers[entrySeq] = new WSWSVoucher();
			vochers[entrySeq].setCompanyNumber(companyNumber);
			vochers[entrySeq].setBookedDate(bookedDate);
			vochers[entrySeq].setBizDate(bizDate);
			vochers[entrySeq].setPeriodYear(periodYear);
			vochers[entrySeq].setPeriodNumber(periodNumber);
			vochers[entrySeq].setVoucherType(voucherType);
			vochers[entrySeq].setAttaches(attaches);
			vochers[entrySeq].setVoucherNumber("");
			vochers[entrySeq].setEntrySeq(entrySeq+1);
			vochers[entrySeq].setVoucherAbstract("");
			vochers[entrySeq].setAccountNumber(convervoucherType1(Util.null2String(mainTableDataMap.get("fkfs"))));
			vochers[entrySeq].setCurrencyNumber(currencyNumber);
			vochers[entrySeq].setLocalRate(localRate);
			vochers[entrySeq].setEntryDC(0);
			vochers[entrySeq].setOriginalAmount(creditAmount);
			vochers[entrySeq].setCreditAmount(creditAmount);
			vochers[entrySeq].setCreator(creator);
			vochers[entrySeq].setAsstActType1("银行账户");
			vochers[entrySeq].setAsstActName1(getbankname(Util.null2String(mainTableDataMap.get("fkyh"))));
			vochers[entrySeq].setAsstActNumber1(getbankcode(Util.null2String(mainTableDataMap.get("fkyh"))));
			vochers[entrySeq].setDescription(lcbh);
			EASUtil easutil = new EASUtil();
			String result1 = easutil.EASLogin_excute(vochers);
			if(result1.lastIndexOf("成功保存") >= 0){
				//向流程表单回写金蝶凭证编号
				String kingdieNO = result1.split("-")[1];
			}else{
	            writeLog("006管理类项目付款 流程向EAS抛凭证出错："+result1);
	            request.getRequestManager().setMessage("-1");
				request.getRequestManager().setMessagecontent("006管理类项目付款 流程向EAS抛凭证出错："+result1);
				result = "0";
			}
			return result;
        }catch(Exception e){
            writeLog("006管理类项目付款 流程向EAS抛凭证出错："+e);
            request.getRequestManager().setMessage("-1");
			request.getRequestManager().setMessagecontent("006管理类项目付款 流程向EAS抛凭证出错："+e);
            return "0";
        }
    }
    //根据付款方式所选下拉框将值转换
    public String convervoucherType(String selectvalue){
    	String voucherType = "";
    	if(selectvalue.equals("0")){
    		voucherType = "银";
    	}else if(selectvalue.equals("1")){
    		voucherType = "银";
    	}else if(selectvalue.equals("2")){
    		voucherType = "现";
    	}
    	return voucherType;
    }
  //根据付款方式所选下拉框值转换为贷方科目
    public String convervoucherType1(String selectvalue){
    	String voucherType = "";
    	if(selectvalue.equals("0")){
    		voucherType = "1002.01";
    	}else if(selectvalue.equals("1")){
    		voucherType = "1002.02";
    	}else if(selectvalue.equals("2")){
    		voucherType = "1001";
    	}
    	return voucherType;
    }
    //根据科目ID获取科目编码
    public String getaccountNumber(String accountid){
    	String accountNumber = "";
    	RecordSet rs = new RecordSet();
    	if(!accountid.equals("")){
        	rs.executeSql("select codeName from fnabudgetfeetype where codeName is not null and id = "+accountid);
        	if(rs.next()){
        		accountNumber = Util.null2String(rs.getString("codeName"));
        	}
    	}
    	return accountNumber;
    }
  //根据付款银行ID获取银行名称
    public String getbankname(String id){
    	String bank = "";
    	RecordSet rs = new RecordSet();
    	if(!id.equals("")){
        	rs.executeSql("select zhmc,zhbm from uf_yinhangzh where id = "+id);
        	if(rs.next()){
        		bank = Util.null2String(rs.getString("zhmc"));
        	}
    	}
    	return bank;
    }
  //根据付款银行ID获取银行编码
    public String getbankcode(String id){
    	String bank = "";
    	RecordSet rs = new RecordSet();
    	if(!id.equals("")){
        	rs.executeSql("select zhmc,zhbm from uf_yinhangzh where id = "+id);
        	if(rs.next()){
        		bank = Util.null2String(rs.getString("zhbm"));
        	}
    	}
    	return bank;
    }
  //根据成本中心ID获取部门编码
    public String getdepartmentcode(String id){
    	String asstActType1 = "";
    	RecordSet rs = new RecordSet();
    	if(!id.equals("")){
        	rs.executeSql("select departmentcode,departmentname from hrmdepartment where id = "+id);
        	if(rs.next()){
        		asstActType1 = Util.null2String(rs.getString("departmentcode"));
        	}
    	}
    	return asstActType1;
    }
  //根据成本中心ID获取部门名称
    public String getdepartmentname(String id){
    	String asstActType1 = "";
    	RecordSet rs = new RecordSet();
    	if(!id.equals("")){
        	rs.executeSql("select departmentcode,departmentname from hrmdepartment where id = "+id);
        	if(rs.next()){
        		asstActType1 = Util.null2String(rs.getString("departmentname"));
        	}
    	}
    	return asstActType1;
    }
  //根据项目卡片ID获取项目名称
    public String getprjname(String id){
    	String prj = "";
    	RecordSet rs = new RecordSet();
    	if(!id.equals("")){
        	rs.executeSql("select xmmc,xmbh from uf_xmkp where id = "+id);
        	if(rs.next()){
        		prj = Util.null2String(rs.getString("xmmc"));
        	}
    	}
    	return prj;
    }
  //根据项目卡片ID获取项目编码
    public String getprjcode(String id){
    	String prj = "";
    	RecordSet rs = new RecordSet();
    	if(!id.equals("")){
    		rs.executeSql("select xmmc,xmbh from uf_xmkp where id = "+id);
        	if(rs.next()){
        		prj = Util.null2String(rs.getString("xmbh"));
        	}
    	}
    	return prj;
    }
}
