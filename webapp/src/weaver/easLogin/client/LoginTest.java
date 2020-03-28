package weaver.easLogin.client;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.namespace.QName;

import kingdie.ormrpc.services.EASLogin.EASLoginProxyProxy;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;

import org.json.JSONObject;
import org.json.XML;

import weaver.file.Prop;

import weaver.general.Util;
import weaver.interfaces.workflow.action.SelectSYFJ;

import client.WSContext;

public class LoginTest {

	/*
	 * public static void execute() { try { // 使用RPC方式调用WebService //
	 * RPCServiceClient serviceClient = new RPCServiceClient(); RPCServiceClient
	 * serviceClient = new RPCServiceClient(); Options options =
	 * serviceClient.getOptions(); // 指定调用WebService的URL EndpointReference
	 * targetEPR = new EndpointReference(
	 * "http://10.10.20.121:6888/ormrpc/services/EASLogin"); // 确定目标服务地址
	 * options.setTo(targetEPR); // 确定调用方法 options.setAction("urn:login"); //
	 * 把chunk关掉后，会自动加上Content-Length options.setProperty(HTTPConstants.CHUNKED,
	 * "false"); // 解决高并发链接超时问题 options.setManageSession(true);
	 * options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
	 * 
	 * // 设置响应超时，默认5s options.setProperty(HTTPConstants.SO_TIMEOUT, 5000); //
	 * 设置连接超时，默认5s options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 5000);
	 * 
	 * QName opAddEntry = new
	 * QName("http://10.10.20.121:6888/ormrpc/services/EASLogin", "login");
	 * 
	 * Object[] opAddEntryArgs = new Object[] {
	 * Util.null2String(Prop.getPropValue("EASLoginInfo", "username")),
	 * Util.null2String(Prop.getPropValue("EASLoginInfo", "password")),
	 * Util.null2String(Prop.getPropValue("EASLoginInfo", "slnname")),
	 * Util.null2String(Prop.getPropValue("EASLoginInfo", "dcname")),
	 * Util.null2String(Prop.getPropValue("EASLoginInfo", "language")),
	 * Util.getIntValue(Prop.getPropValue("EASLoginInfo", "dbtype")) };
	 * 
	 * OMElement element = serviceClient.invokeBlocking(opAddEntry,
	 * opAddEntryArgs); } catch (Exception e) { System.out.println("异常:"+e);
	 * 
	 * }
	 * 
	 * //String result = element.getFirstElement().toString();
	 * //System.out.println(result);
	 * 
	 * }
	 */

	public String EASLogin() {
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date=format.parse("2019-07-31");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.set(Calendar.DATE, (calendar.get(Calendar.DATE) + 1));
		
			System.out.println(calendar.get(Calendar.DAY_OF_MONTH));
			if (calendar.get(Calendar.DAY_OF_YEAR) == 1) {
			    
			}else{
			    
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		System.out.println(Util.null2String(Prop.getPropValue("EASLoginInfo",
				"username")));
		try {
			// 使用RPC方式调用WebService
			RPCServiceClient serviceClient = new RPCServiceClient();

			Options options = serviceClient.getOptions();
			// 指定调用WebService的URL
			EndpointReference targetEPR = new EndpointReference(
					"http://10.10.20.121:6888/ormrpc/services/EASLogin");
			System.out.println(Util.null2String(Prop.getPropValue(
					"EASLoginInfo", "urlLogin")));
			// 确定目标服务地址
			options.setTo(targetEPR);
			// 确定调用方法
			options.setAction("urn:login");
			// 解决高并发链接超时问题
			options.setManageSession(true);
			options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);

			// 设置响应超时，默认5s
			options.setProperty(HTTPConstants.SO_TIMEOUT, 5000);
			// 设置连接超时，默认5s
			options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 5000);

			QName opAddEntry = new QName(Util.null2String(Prop.getPropValue(
					"EASLoginInfo", "urlLogin")), "login");

			/*
			 * Object[] opAddEntryArgs = new Object[] {
			 * Util.null2String(Prop.getPropValue("EASLoginInfo", "username")),
			 * Util.null2String(Prop.getPropValue("EASLoginInfo", "password")),
			 * Util.null2String(Prop.getPropValue("EASLoginInfo", "slnname")),
			 * Util.null2String(Prop .getPropValue("EASLoginInfo", "dcname")),
			 * Util.null2String(Prop.getPropValue("EASLoginInfo", "language")),
			 * Util.getIntValue(Prop .getPropValue("EASLoginInfo", "dbtype")) };
			 */

			Object[] opAddEntryArgs = new Object[] { "sseinfo", "sseinfo",
					"eas", "SJS", "l2", 0 };

			OMElement element = serviceClient.invokeBlocking(opAddEntry,
					opAddEntryArgs);
			element.getFirstElement().getText();
			serviceClient.cleanupTransport();
		} catch (Exception e) {
			System.out.println("登录异常:" + e);
			return "0";
		}

		return "1";
	}

	public void addPay() throws Exception {

		String xml = "<KingdeeWsDataSet><KingdeeWsData>"
				+ "<CompanyName>FHG002</CompanyName>"
				+ "<bizDate>2019-01-17</bizDate>"
				+ "<payerAccountBank>216580801810001</payerAccountBank>"
				+ "<payeeType>999</payeeType>" + "<payeeName>周舶</payeeName>"
				+ "<payeeAccountBankName>招商银行</payeeAccountBankName>"
				+ "<payeeAccountBank>4682030211174259</payeeAccountBank>"
				+ "<skfgjNum>中国</skfgjNum>" + "<skfsNum>上海市</skfsNum>"
				+ "<skfsxNum>市辖区</skfsxNum>" + "<skfxianNum></skfxianNum>"
				+ "<payRemark>博物馆藏品到付费。</payRemark>"
				+ "<sourceBillType>999</sourceBillType>"
				+ "<SourceSys>hrHR</SourceSys>"
				+ "<MyBillGUID>42424111dsd</MyBillGUID>"
				+ "<actPayAmt>105.00</actPayAmt>" + "<Entry>"
				+ "<expenseType>99</expenseType>" + "<amount>105.00</amount>"
				+ "<OppAccountNum>1002.01</OppAccountNum>"
				+ "<payRemark></payRemark></Entry>"
				+ "</KingdeeWsData></KingdeeWsDataSet>";
		// 使用RPC方式调用WebService
		RPCServiceClient serviceClient = new RPCServiceClient();

		Options options = serviceClient.getOptions();
		// 指定调用WebService的URL
		EndpointReference targetEPR = new EndpointReference("http://10.10.20.101:6887/ormrpc/services/WSPayBillFacade");
		// 确定目标服务地址
		options.setTo(targetEPR);
		// 确定调用方法
		options.setAction("urn:AddFKD");
		// 把chunk关掉后，会自动加上Content-Length
		options.setProperty(HTTPConstants.CHUNKED, "false");
		// 解决高并发链接超时问题
		options.setManageSession(true);
		options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);

		// 设置响应超时，默认5s
		options.setProperty(HTTPConstants.SO_TIMEOUT, 5000);
		// 设置连接超时，默认5s
		options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 5000);

		QName opAddEntry = new QName("http://10.10.20.101:6887/ormrpc/services/WSPayBillFacade", "AddF");

		Object[] opAddEntryArgs = new Object[] { xml };
		OMElement element = serviceClient.invokeBlocking(opAddEntry,
				opAddEntryArgs);
		String result = element.getFirstElement().getText();
		serviceClient.cleanupTransport();
		System.out.println("result:"+result);

	}

	public void query() throws Exception {

		String xml = "<KingdeeWsDataSet><KingdeeWsData><SourceSys>hrHR</SourceSys><MyBillGUID>1546484085419000_15466</MyBillGUID></KingdeeWsData></KingdeeWsDataSet>";
		// 使用RPC方式调用WebService
		RPCServiceClient serviceClient = new RPCServiceClient();

		Options options = serviceClient.getOptions(); //
		// 指定调用WebService的URL
		EndpointReference targetEPR = new EndpointReference(
				Util.null2String(Prop.getPropValue("EASLoginInfo", "urlQuery")));
		// 确定目标服务地址
		options.setTo(targetEPR);
		// 确定调用方法
		options.setAction("urn:Query");
		// 把chunk关掉后，会自动加上Content-Length
		options.setProperty(HTTPConstants.CHUNKED, "false");
		// 解决高并发链接超时问题
		options.setManageSession(true);
		options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);

		// 设置响应超时，默认5s
		options.setProperty(HTTPConstants.SO_TIMEOUT, 5000);
		// 设置连接超时，默认5s
		options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 5000);

		QName opAddEntry = new QName(Util.null2String(Prop.getPropValue(
				"EASLoginInfo", "urlQuery")), "Query");

		Object[] opAddEntryArgs = new Object[] { xml };
		OMElement element = serviceClient.invokeBlocking(opAddEntry,
				opAddEntryArgs);
		String result = element.getFirstElement().getText();
		JSONObject xmlJSONObj = XML.toJSONObject(result);
		serviceClient.cleanupTransport();
		System.out.println(result);

	}

	public  void getThisMonthLastday() {
		Calendar cale = Calendar.getInstance();
		cale.add(Calendar.MONTH, 1);
		cale.set(Calendar.DAY_OF_MONTH, 0);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar todaycal = Calendar.getInstance();
		System.out.println(Util.add0(todaycal.get(Calendar.MONTH) + 2, 1));
	}

	public void logineas2() {
		EASLoginProxyProxy proxy = new EASLoginProxyProxy();
		WSContext ctx;
		try {
			ctx = proxy.login(
					Util.null2String(Prop.getPropValue("sse_eas", "userName")),
					Util.null2String(Prop.getPropValue("sse_eas", "password")),
					Util.null2String(Prop.getPropValue("sse_eas", "slnName")),
					Util.null2String(Prop.getPropValue("sse_eas", "dcName")),
					Util.null2String(Prop.getPropValue("sse_eas", "language")),
					Util.getIntValue(Prop.getPropValue("sse_eas", "dbType")));
			System.out.println(ctx.getSessionId());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * public void loginEas(){
	 * 
	 * EASLoginProxyService easLoginProxyService=new EASLoginProxyService();
	 * EASLoginProxy proxy= easLoginProxyService.getEASLogin(); WSContext
	 * ws=proxy.login("sseinfo", "sseinfo", "eas", "FHG002", "l2", 2);
	 * System.out.println(ws.getSessionId()); }
	 */
	public void test(){
		Calendar todaycal = Calendar.getInstance();
		String syndate = Util.add0(todaycal.get(Calendar.YEAR), 4) + "-"
				+ Util.add0(todaycal.get(Calendar.MONTH), 2) + "-"
				+ Util.add0(todaycal.get(Calendar.DAY_OF_MONTH), 2);
		System.out.println("月份:"+Util.add0(todaycal.get(Calendar.MONTH)+8 , 2));
		int num=0;
		System.out.println(num+=1);
	}
	
	public static void main(String[] args) {
		// getThisMonthLastday();
		LoginTest loginTest = new LoginTest();
		//loginTest.test();
		loginTest.getThisMonthLastday();
		// loginTest.logineas2();
		// loginTest.loginEas();
		try {
			//String login = loginTest.EASLogin();
			//System.out.println(login);
			// execute();
			//loginTest.query();
			//loginTest.addPay();
			//
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
