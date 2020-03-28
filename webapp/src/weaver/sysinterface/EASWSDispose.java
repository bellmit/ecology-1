package weaver.sysinterface;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.util.blob.WritableBlob;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.json.JSONObject;
import org.json.XML;

import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.Util;

/**
 * 银企直连--付款单接口
 * 
 * @author lsq
 * @date 2019/6/4
 * @version 1.0
 */
public class EASWSDispose extends BaseBean {
	/**
	 * EAS前置登陆
	 * 
	 * @return String
	 */
	public String EASLogin() {
		try {
			// 使用RPC方式调用WebService
			RPCServiceClient serviceClient = new RPCServiceClient();

			Options options = serviceClient.getOptions();
			// 指定调用WebService的URL
			EndpointReference targetEPR = new EndpointReference(
					Util.null2String(Prop.getPropValue("EASLoginInfo",
							"urlLogin")));
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

			//指定命名空间  和  访问的方法名
			QName opAddEntry = new QName(Util.null2String(Prop.getPropValue(
					"EASLoginInfo", "urlLogin")), "login");
			//指定 方法的参数
			Object[] opAddEntryArgs = new Object[] {
					Util.null2String(Prop.getPropValue("EASLoginInfo",
							"username")),
					Util.null2String(Prop.getPropValue("EASLoginInfo",
							"password")),
					Util.null2String(Prop.getPropValue("EASLoginInfo",
							"slnname")),
					Util.null2String(Prop
							.getPropValue("EASLoginInfo", "dcname")),
					Util.null2String(Prop.getPropValue("EASLoginInfo",
							"language")),
					Util.getIntValue(Prop
							.getPropValue("EASLoginInfo", "dbtype")) };
			//访问webservice接口  并得到  返回值
			OMElement element = serviceClient.invokeBlocking(opAddEntry,
					opAddEntryArgs);
			//获取返回值的第一个参数   getText()获取的是具体的参数内容   toString()获取的是整个参数值(包含了标签)
			element.getFirstElement().getText();
			//element.getFirstElement().toString();
			//相当于释放资源(如果连接次数过多\频繁 写这行代码 可解决连接超时的异常)
			serviceClient.cleanupTransport();
			
		} catch (Exception e) {
			writeLog("登录异常:"+e);
			return "0";
		}

		return "1";
	}

	/**
	 * 新增付款单
	 * 
	 * @return String
	 */
	public String AddFKD(String xml) {

		String login = EASLogin();
		String result = "";
		if (login.equals("0")) {
			writeLog("登录失败result:"+login);
		} else if (login.equals("1")) {
			writeLog("登录成功result:"+login);
			try {
				// 使用RPC方式调用WebService
				RPCServiceClient serviceClient = new RPCServiceClient();

				Options options = serviceClient.getOptions();
				// 指定调用WebService的URL urlAdd
				EndpointReference targetEPR = new EndpointReference(
						Util.null2String(Prop.getPropValue("EASLoginInfo",
								"urlAdd")));
				// 确定目标服务地址
				options.setTo(targetEPR);
				// 解决高并发链接超时问题
				options.setManageSession(true);
				options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
				// 设置响应超时，默认5s
				options.setProperty(HTTPConstants.SO_TIMEOUT, 5000);
				// 设置连接超时，默认5s
				options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 5000);

				QName opAddEntry = new QName(Util.null2String(Prop
						.getPropValue("EASLoginInfo", "urlAdd")), "AddFKD");
				// 设置参数
				Object[] opAddEntryArgs = new Object[] { xml };
				OMElement element = serviceClient.invokeBlocking(opAddEntry,
						opAddEntryArgs);
				result = element.getFirstElement().getText();
				serviceClient.cleanupTransport();
			} catch (Exception e) {
				writeLog("付款单执行异常:" + e);
				return result;
			}
		}
		return result;
	}

	/**
	 * 查询付款单
	 * 
	 * @param myBillGUID
	 *            异构系统单据ID
	 * @return String
	 */
	public String Query(String xml) {
		String result = "";
		writeLog("登录成功");
		try {
			// 使用RPC方式调用WebService
			RPCServiceClient serviceClient = new RPCServiceClient();

			Options options = serviceClient.getOptions();
			// 指定调用WebService的URL
			EndpointReference targetEPR = new EndpointReference(
					Util.null2String(Prop.getPropValue("EASLoginInfo",
							"urlQuery")));
			// 确定目标服务地址
			options.setTo(targetEPR);
			// 确定调用方法
			options.setAction("urn:Query");

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
			result = element.getFirstElement().getText();
			serviceClient.cleanupTransport();
		} catch (Exception e) {
			writeLog("查询付款单异常:" + e);
			return result;
		}

		return result;
	}
}
