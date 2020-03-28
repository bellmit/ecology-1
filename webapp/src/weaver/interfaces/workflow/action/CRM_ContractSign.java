package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;

public class CRM_ContractSign extends BaseBean implements Action {
	public String execute(RequestInfo request) {
		try {
			writeLog("合同签订--开始");
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			RecordSet rs2 = new RecordSet();
			RecordSet rs3 = new RecordSet();
			RecordSet rs4 = new RecordSet();
			RecordSet rs5 = new RecordSet();
			RecordSet rs6 = new RecordSet();
			RecordSet rs7 = new RecordSet();
			RecordSet rs8 = new RecordSet();
			RecordSet rs9 = new RecordSet();
			int modeId = 138; // 建模id
			String billid = request.getRequestid();
			String sql = "";
			sql = "select a.applyType,a.creator,a.dept,a.business,b.customer,b.contractrelation,b.relevacontract,b.contractmoney,b.uuid "
					+ "from uf_crm_contractagre a,uf_crm_contractagre_dt1 b where a.id=b.mainid and a.requestid="
					+ billid;
			rs.executeSql(sql);
			while (rs.next()) {
				String applyType = Util.null2String(rs.getString("applyType"));//申请类型(0:自用;1:机房内转发;2:接口转发) 业务类型为level-2非展示时使用;
				String creator = Util.null2String(rs.getString("creator"));// 申请人
				String dept = Util.null2String(rs.getString("dept"));// 申请部门
				String business = Util.null2String(rs.getString("business"));// 业务
				String customer = Util.null2String(rs.getString("customer"));// 客户
				String contractrelation = Util.null2String(rs.getString("contractrelation"));// 合同关系
				String relevacontract = Util.null2String(rs.getString("relevacontract"));// 关联合同
				String contractmoney = Util.null2String(rs.getString("contractmoney"));// 合同金额
				String uuid = Util.null2String(rs.getString("uuid"));// 流程uuid
				String contracttype = "";// 合同类型
				if (contractrelation.equals("0") && relevacontract.equals("")) {// 新签
					contracttype = "0";// 合同
				} else if (contractrelation.equals("0")&& !relevacontract.equals("")) {// 新签
					contracttype = "1";// 补充协议
				} else if (contractrelation.equals("1")) {// 续签
					contracttype = "0";// 合同
				} else if (contractrelation.equals("2")) {// 重签
					contracttype = "0";// 合同
				} else if (contractrelation.equals("3")) {// 终止
					contracttype = "2";// 终止协议
				} else if (contractrelation.equals("4")) {
			        contracttype = "1";
		        }
				writeLog("customer----" + customer);
				writeLog("business----" + business);
				// 更新原合同状态为终止
				writeLog("relevacontract----" + relevacontract);
				if (!relevacontract.equals("")) {
					sql = "update uf_crm_contract set status=1 where id='"+ relevacontract + "'";
					rs1.executeSql(sql);
				}
				String htid = "";// 新生成的合同id
				sql = "select id from uf_crm_contract where flowid='" + billid+ "' and uuid='" + uuid + "'";
				writeLog("查询合同sql=" + sql);
				rs2.executeSql(sql);
				if (rs2.next()) {
					htid = Util.null2String(rs2.getString("id"));
				}
				writeLog("合同htid=" + htid);
				// 更新新生成合同的合同类型
				if (!htid.equals("")) {
					sql = "update uf_crm_contract set contracttype='"+ contracttype + "' where id='" + htid + "'";
					rs3.executeSql(sql);
					if (contractrelation.equals("0")|| contractrelation.equals("1")|| contractrelation.equals("2")
							|| (contractrelation.equals("3") && !contractmoney.equals("0.00")) || (contractrelation.equals("4"))) {
						try {
							int requestids = createFormContract(creator, dept,customer, business, htid);
							if (requestids > 0) {
								writeLog("创建收款计划流程触发工作流成功，流程requestid:"+ requestids);
							}
						} catch (Exception e) {
							writeLog("创建收款计划流程触发出错：" + e);
						}
					}
				}

				// 生成公示信息-Level-1行情许可、期权行情展示许可、固定收益行情许可、指数授权、Level-2行情展示许可、Level-2行情非展示许可、期权行情非展示许可(新增)
				if (business.equals("1") || business.equals("2")|| business.equals("3") || business.equals("4")
						|| business.equals("5") || business.equals("6")|| business.equals("7")) {
					String gsid = "";
					String pubstatus = "1";// 不显示
					String xuhao = "999";// 序号
					String specialset = "";
					String uuids = "";
					RecordSetDataSource exchangeDB = new RecordSetDataSource("exchangeDB");
					sql = "select id,status,uuid,dsporder,specialset from uf_crm_permitpub where customer='"
							+ customer + "'and business='" + business + "'";
					rs4.executeSql(sql);
					if (rs4.next()) {
						gsid = Util.null2String(rs4.getString("id"));// 许可公示id
						pubstatus = Util.null2String(rs4.getString("status"));// 公示状态
						uuids = Util.null2String(rs4.getString("uuid"));// 许可库的uuid
						xuhao = Util.null2String(rs4.getString("dsporder"));   //序号
						specialset = Util.null2String(rs4.getString("specialset"));
					}

					if (gsid.equals("")) {// 不存在，则新增
						// 插入许可记录
						RecordSet rscompany = new RecordSet();
						rscompany.executeSql("select name,creditcode from uf_CRM_CustomerInfo where id='"+ customer + "'");
						String organizationCode = "";
						String companyName = "";
						if (rscompany.next()) {
							// 公司名称
							companyName = Util.null2String(rscompany.getString("name"));
							// 组织机构代码
							organizationCode = Util.null2String(rscompany.getString("creditcode"));
						}
						// 计算显示状态
						sql = "select count(1) as flag from uf_crm_contract where startdate <=convert(varchar(10),GETDATE(),120) "
								+ "and enddate>=convert(varchar(10),GETDATE(),120) and status='0' "
								+ "and customer='"+ customer+ "' and business='" + business + "'";
						String pubflag = "0";
						rs8.executeSql(sql);
						if (rs8.next()) {
							pubflag = Util.null2String(rs8.getString("flag"));
						}
						if (pubflag.equals("0")) {// 无有效合同
							pubstatus = "1";// 不显示
						} else {
							pubstatus = "0";// 显示
						}
						// 计算该业务目前最大序号
						sql = "select isnull(ROUND(max(dsporder)+1,0,1),1) as dsporder from uf_crm_permitpub where business='"+ business + "'";
						rs7.executeSql(sql);
						if (rs7.next()) {
							xuhao = Util.null2String(rs7.getString("dsporder"));// 最大序号+1
						}
						// 调用获取首字母方法 ,,,返回首字母大写
						String initialUp = getInitialUp(customer);
						String type=""; //期权类型: 0:展示  1:非展示
						if(business.equals("2")) {
							type="0";
						}else if(business.equals("7")) {
							type="1";
						}
						sql = "insert into uf_crm_permitpub"
								+ "(formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime,customer,business,status,dsporder,uuid,initialUp,type,applyType) "
								+ "values "
								+ "("+ modeId+ ",1,0,convert(varchar(10),getdate(),120),convert(varchar(8),getdate(),108),"+ "'"+ customer+ "','"+ business+ "','"+ pubstatus+ "','"+ xuhao+ "','"+ uuid+ "','" + initialUp + "','"+type+"','"+applyType+"')";
						rs5.executeSql(sql);
						sql = "select id from uf_crm_permitpub where uuid='"+ uuid + "'";
						rs6.executeSql(sql);
						if (rs6.next()) {
							String id = Util.null2String(rs6.getString("id"));// 查询出许可公示记录
							int logid = Integer.parseInt(id);
							ModeRightInfo ModeRightInfo = new ModeRightInfo();
							ModeRightInfo.editModeDataShare(5, modeId, logid);// 新建的时候添加共享-所有人
							ModeRightInfo.editModeDataShare(4, modeId, logid);// 新建的时候添加共享-所有人
						}
						writeLog("business-------"+business);
						// 往中间库推送数据
						if (business.equals("1")) { // Level-1行情许可
							sql = "insert into sync_website_lv1_permit_list(id,organization_code,company_name,status,showorder)  values('"
									+ uuid+ "','"+ organizationCode+ "','"+ companyName+ "','"+ pubstatus+ "','"+ xuhao + "')";
						} else if (business.equals("2")) {// 期权行情展示许可
							sql = " insert into sync_website_opt_permit_list "
									+ " (ID,ORGANIZATION_CODE,COMPANY_NAME,STATUS,SHOWORDER,TYPE) "
									+ " VALUES " + " ('" + uuid + "','"+ organizationCode + "','" + companyName+ "','" + pubstatus + "','" + xuhao + "','"+type+"') ";
						} else if (business.equals("3")) {// 固定收益行情许可

							sql = "insert into SYNC_FIXEDINCOME_PERMIT_LIST  (id,ORGANIZATION_CODE,COMPANY_NAME,STATUS,SHOWORDER)  VALUES  ('"
									+ uuid+ "' , '"+ organizationCode+ "' , '"+ companyName+ "' , '"+ pubstatus+ "' , '"+ xuhao+ "') ";

						} else if (business.equals("4")) {// 指数授权
							sql = "insert into sync_website_sse_permit_list (ID,ORGANIZATION_CODE,COMPANY_NAME,STATUS,SHOWORDER) values('"
									+ uuid+ "','"+ organizationCode+ "','"+ companyName+ "','"+ pubstatus+ "','"+ xuhao + "')";
						} else if (business.equals("5")) {// Level-2行情展示许可
                            
							sql = "insert into sync_website_lv2_permit_list(id,organization_code,company_name,status,showorder)  values('"
									+ uuid+ "','"+ organizationCode+ "','"+ companyName+ "','"+ pubstatus+ "','"+ xuhao + "')";
							
						} else if (business.equals("6")) {// 或Level-2行情非展示许可
							
							sql = "insert into SYNC_LV2DATAFEED_PERMIT_LIST "
									+ " (id,ORGANIZATION_CODE,COMPANY_NAME ,STATUS,SHOWORDER,APPLY_TYPE) VALUES('" 
									+ uuid + "' , '"+ organizationCode + "' , '" + companyName+ "' ,'" + pubstatus + "' , '" + xuhao+ "','"+applyType+"') ";
							
						} else if(business.equals("7")) {//期权行情非展示许可
							sql = "insert into sync_website_opt_permit_list "
									+ " (ID,ORGANIZATION_CODE,COMPANY_NAME,STATUS,SHOWORDER,TYPE) VALUES ('" 
									+ uuid + "','"+ organizationCode + "','" + companyName+ "','" + pubstatus + "','" + xuhao + "','"+type+"') ";							
						}
						try {
							writeLog("中间库插入sql----" + sql);
							exchangeDB.executeSql(sql);
						} catch (Exception e) {
							writeLog("中间库插入出错e----" + e);
						}

					} else {// 更新

						sql = "update uf_crm_permitpub set status='"
								+ pubstatus + "',dsporder='" + xuhao
								+ "' where id='" + gsid + "'";
						rs9.executeSql(sql);

						// 往中间库更新数据
						if (business.equals("1")) {
							sql = "update sync_website_lv1_permit_list set status='"
									+ pubstatus+ "',showorder='"+ xuhao+ "' where id='" + uuids + "'";
						} else if (business.equals("2")) {
							sql = "update sync_website_opt_permit_list set status='"
									+ pubstatus+ "',showorder='"+ xuhao+ "' where id='" + uuids + "'";
						} else if (business.equals("3")) {
							sql = " update SYNC_FIXEDINCOME_PERMIT_LIST set STATUS = '"
									+ pubstatus+ "',SHOWORDER ='"+ xuhao+ "' where id ='"+uuids+ "'";
						} else if (business.equals("4")) {
							sql = "update sync_website_sse_permit_list set status='"
									+ pubstatus+ "',showorder='"+ xuhao+ "' where id='" + uuids + "'";
						} else if (business.equals("5")) {
							sql = "update sync_website_lv2_permit_list set status='"
									+ pubstatus+ "',showorder='"+ xuhao+ "' where id='" + uuids + "'";
						} else if (business.equals("6")) {
							sql = "update SYNC_LV2DATAFEED_PERMIT_LIST set status='"
									+ pubstatus+ "',showorder='"+ xuhao+ "' where id='" + uuids + "'";
						} else if (business.equals("7")) {
							sql = "update sync_website_opt_permit_list set status='"
									+ pubstatus+ "',showorder='"+ xuhao+ "' where id='" + uuids + "'";							
						}
						// 更新中间表
						try {
							exchangeDB.executeSql(sql);
							writeLog("更新成功");
						} catch (Exception e) {
							writeLog("中间库更新异常e" + e);
						}

					}
				}
				writeLog("合同签订--结束");
			}
		} catch (Exception e) {
			writeLog("合同签订--出错" + e);
			return "0";
		}
		return "1";
	}

	/**
	 * 创建收款计划申请流程
	 */
	public int createFormContract(String creator, String dept, String customer,
			String business, String contract) {
		String newrequestid = "";
		try {
			String workflowid = "444"; //
			String lcbt = "创建收款计划";// 流程标题
			RequestService requestService = new RequestService();
			RequestInfo requestInfo = new RequestInfo();
			writeLog("创建收款计划申请触发start：");
			requestInfo.setWorkflowid(workflowid);// 流程类型id
			requestInfo.setCreatorid(creator);// 创建人
			requestInfo.setDescription(lcbt);// 设置流程标题
			requestInfo.setRequestlevel("0");// 0 正常，1重要，2紧急
			requestInfo.setIsNextFlow("0");// 保存在发起节点
			SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd");
			String createDate = dateFormat_now.format(new Date());
			RecordSet rs = new RecordSet();

			MainTableInfo mainTableInfo = new MainTableInfo();
			List<Property> fields = new ArrayList<Property>();
			Property field = null;

			field = new Property();
			field.setName("createdate");
			field.setValue(createDate);
			fields.add(field);

			field = new Property();
			field.setName("creator");
			field.setValue(creator);
			fields.add(field);

			field = new Property();
			field.setName("dept");
			field.setValue(dept);
			fields.add(field);

			field = new Property();
			field.setName("customer");
			field.setValue(customer);
			fields.add(field);

			field = new Property();
			field.setName("business");
			field.setValue(business);
			fields.add(field);

			field = new Property();
			field.setName("contract");
			field.setValue(contract);
			fields.add(field);

			fields.add(field);
			Property[] fieldarray = (Property[]) fields
					.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			requestInfo.setMainTableInfo(mainTableInfo);
			newrequestid = requestService.createRequest(requestInfo);

			writeLog("创建收款计划申请触发end：");
		} catch (Exception e) {
			writeLog("创建收款计划申请触发出错：" + e);
		}
		return Util.getIntValue(newrequestid, 0);
	}

	/**
	 * 获取客户名称首字母大写
	 * 
	 * @param id
	 * @param customerid
	 * @return String 首字母
	 */
	public String getInitialUp(String customerid) {
		writeLog("获取客户名称首字母开始...");
		String initailss = ""; // 首字母
		try {
			RecordSet rs = new RecordSet();
			String sql = " select dbo.fnpbGetPYFirstLetter((select name from uf_crm_customerinfo where id='"
					+ customerid + "')) as chinaname";
			rs.executeSql(sql);
			writeLog("获取客户名称首字母sql:" + sql);
			if (rs.next()) {
				String initail = Util.null2String(rs.getString("chinaname"));
				String initails=initail.replace(" ","");  //去点所有空格
				initailss=initails.replaceAll("[\\pP\\p{Punct}]","");
			}
		} catch (Exception e) {
			writeLog("获取客户名称首字母异常:" + e);
		}
		writeLog("获取客户名称首字母结束...");
		return initailss;
	}
}