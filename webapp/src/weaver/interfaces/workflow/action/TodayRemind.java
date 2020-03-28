package weaver.interfaces.workflow.action;

import java.util.Calendar;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

/**
 * 今日提醒
 * @author lsq
 * @date 2019/4/28
 * @version 1.0
 */
public class TodayRemind  extends BaseCronJob{

	BaseBean lg = new BaseBean();
	SelectSYFJ sc = new SelectSYFJ();
	RecordSet rs = new RecordSet();
	Calendar todaycal = Calendar.getInstance();
	String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
			+ Util.add0(todaycal.get(Calendar.MONTH) + 1, 2) + "-"
			+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);

	//0 0 0 30 12 ? *  每年12月30日 0点 执行一次
	//0 0 0 1/1 * ? *  每天执行一次
	// 通过继承BaseCronJob类可以实现定时同步
	public void execute() {
		try {
			create();
		} catch (Exception e) {
			lg.writeLog("创建流程异常e:" + e);
		}
	}

	private void create() {

		
		
		
	}
	
	
	
	
	
	
}
