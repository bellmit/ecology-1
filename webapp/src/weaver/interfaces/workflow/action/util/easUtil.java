package weaver.interfaces.workflow.action.util;

import java.rmi.RemoteException;
import kingdie.ormrpc.services.EASLogin.EASLoginProxyProxy;


import _121._20._10._10._6888.ormrpc.services.wsvisfacade.WSVISFacadeSoapBindingStub;
import _121._20._10._10._6888.ormrpc.services.wsvisfacade.WSVISFacadeSrvProxyService;
import _121._20._10._10._6888.ormrpc.services.wsvisfacade.WSVISFacadeSrvProxyServiceLocator;

import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.Util;

import client.WSContext;

public class easUtil extends BaseBean{
	
	public String EASLogin_excute(String cardxml){
		try {
			
			EASLoginProxyProxy proxy = new EASLoginProxyProxy();
			WSContext ctx;
			ctx = proxy.login(Util.null2String(Prop.getPropValue("sse_eas", "userName")), 
					Util.null2String(Prop.getPropValue("sse_eas", "password")), 
					Util.null2String(Prop.getPropValue("sse_eas", "slnName")), 
					Util.null2String(Prop.getPropValue("sse_eas", "dcName")), 
					Util.null2String(Prop.getPropValue("sse_eas", "language")), 
					Util.getIntValue(Prop.getPropValue("sse_eas", "dbType")));
			if(ctx.getSessionId() != null){
				writeLog("EAS认证通过");
				String result = EASWSGLWebServiceFacade_excute(cardxml);
				return result;
			}else{
				writeLog("EAS认证不通过");
				return "EAS认证不通过";
			}
		} catch (RemoteException e) {
			writeLog("向EAS登录认证时出现异常："+e);
			return e.toString();
		}
	}
	
	public String EASWSGLWebServiceFacade_excute(String cardxml){
		try {
			WSVISFacadeSrvProxyService service=new WSVISFacadeSrvProxyServiceLocator();
			WSVISFacadeSoapBindingStub wfbs=(WSVISFacadeSoapBindingStub)service.getWSVISFacade();
			String backValue=wfbs.createFACard(cardxml);
			return backValue;
		} catch (Exception e) {
			writeLog("向金蝶新增资产接口推送出错："+e);
			return e.toString();
		}
	}
}
