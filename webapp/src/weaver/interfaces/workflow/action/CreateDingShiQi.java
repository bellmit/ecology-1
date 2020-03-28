package weaver.interfaces.workflow.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.DetailTableInfo;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;

/**
 * 创建第一个定时器
 * 
 * @author cj
 * @date 2019/3/28
 * @version 1.0
 */
public class CreateDingShiQi extends BaseCronJob {
	BaseBean lg = new BaseBean();
	SelectSYFJ sc = new SelectSYFJ();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
			+ Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-"
			+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);

	// 通过继承BaseCronJob类可以实现定时同步
	public void execute() {
		try {
			create();
		} catch (Exception e) {
		}
	}

	/**
	 * 触发流程
	 */
	public void create() {
		lg.writeLog("开始触发流程！！！");
		RecordSetDataSource rsd = new RecordSetDataSource("exchangeDB");
		String sql = "select * from dingshiqi";
		rsd.executeSql(sql);
		lg.writeLog("sql---" + sql);
		while (rsd.next()) {
			String dsid = Util.null2String(rsd.getString("dsid"));
			String dsname = Util.null2String(rsd.getString("dsname"));
			String dstype = Util.null2String(rsd.getString("dstype"));
			String useperson = Util.null2String(rsd.getString("useperson"));
			String usebumen = Util.null2String(rsd.getString("usebumen"));
			String usetime = Util.null2String(rsd.getString("usetime"));
			String statu = Util.null2String(rsd.getString("statu"));
			lg.writeLog("statu---" + statu);

			if (statu.equals("1")) {

				lg.writeLog("该流程已经触发了");

			} else {

				try {
					int requestId = createLC(dsid, dsname, dstype, useperson,
							usebumen, usetime);
					lg.writeLog("requestId---" + requestId);
					if (requestId > 0) {
						String sqles = "update dingshiqi set statu='1' where dsid="
								+ dsid;
						rsd.executeSql(sqles);
						lg.writeLog("更新语句" + sqles);
						lg.writeLog("流程触发成功！！！");
					}

				} catch (Exception e) {

					lg.writeLog("流程触发异常：" + e);

				}

			}

		}

	}

	/**
	 * 创建流程
	 */
	public int createLC(String dsid, String dsname, String dstype,
			String useperson, String usebumen, String usetime) {
		lg.writeLog("开始创建流程！！！");
		String newRequestId = "";

		try {
			RequestService requestService = new RequestService();
			RequestInfo info = new RequestInfo();
			info.setWorkflowid("471"); // 流程类型id
			info.setCreatorid("73");// 创建人 id
			info.setDescription("触发定时器");// 设置流程标题
			info.setRequestlevel("0");// 0 正常，1重要，2紧急
			info.setIsNextFlow("0");// 流转到下一节点
			
			MainTableInfo mainTableInfo = new MainTableInfo();
			
			List<Property> fields = new ArrayList<Property>();
			Property field = null;
			if (dsid != "") {
				field = new Property();
				field.setName("dsid");
				field.setValue(dsid);
				fields.add(field);
			}
			if (dsname != "") {
				field = new Property();
				field.setName("dsname");
				field.setValue(dsname);
				fields.add(field);
			}
			if (dstype != "") {
				field = new Property();
				field.setName("dstype");
				field.setValue(dstype);
				fields.add(field);
			}
			if (useperson != "") {
				field = new Property();
				field.setName("useperson");
				field.setValue(useperson);
				fields.add(field);
			}
			if (usebumen != "") {
				field = new Property();
				field.setName("usebumen");
				field.setValue(usebumen);
				fields.add(field);
			}
			if (usetime != "") {
				field = new Property();
				field.setName("usetime");
				field.setValue(usetime);
				fields.add(field);
			}

			Property[] fieldarray = (Property[]) fields
					.toArray(new Property[fields.size()]);
			mainTableInfo.setProperty(fieldarray);
			info.setMainTableInfo(mainTableInfo);
			newRequestId = requestService.createRequest(info);

			lg.writeLog("创建流程成功！！！");

		} catch (Exception e) {

			lg.writeLog("创建流程失败。。。。异常是：" + e);

		}

		return Util.getIntValue(newRequestId, 0);
	}

}
