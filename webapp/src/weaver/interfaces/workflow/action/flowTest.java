package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 测试流程字段
 * 
 * @author lsq
 * @date 2019/8/7
 * @version 1.0
 */
public class flowTest extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		try {
            String s1="Programming";
            String s2=new String("Programming");
			String s3="Programming"+"ming";
			String s4=s1;
			System.out.println("测试1:"+s1==s2);
			System.out.println("测试2:"+s1==s3);
			System.out.println("测试3:"+s1.equals(s2));
			System.out.println("测试4:"+s1.equals(s2));
			System.out.println("测试5:"+s1==s4);
			System.out.println("测试6:"+s1==s1.intern());
		} catch (Exception e) {
			writeLog("流程测试异常:" + e);
			return "0";
		}

		return "1";
	}

}
