package weaver.workflow.sse.action;

import java.rmi.RemoteException;
import kingdie.ormrpc.services.EASLogin.EASLoginProxyProxy;
import kingdie.ormrpc.services.WSGLWebServiceFacade.WSGLWebServiceFacadeSrvProxyProxy;
import org.json.JSONObject;
import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.Util;
import wsvoucher.client.WSWSVoucher;
import client.WSContext;

public class EASUtil extends BaseBean{
	public String EASLogin_excute(WSWSVoucher[] vochers){
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
				String result = EASWSGLWebServiceFacade_excute(vochers);
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
	public String EASWSGLWebServiceFacade_excute(WSWSVoucher[] vochers){
		try {
			WSGLWebServiceFacadeSrvProxyProxy vProxy = new WSGLWebServiceFacadeSrvProxyProxy();
			//构建数组 ，一个凭证一个数组，比如这个凭证有三条分录
			writeLog("-----金蝶传递值：length="+vochers.length+"--------");
			for (int i = 0; i < vochers.length; i++) {
				JSONObject jsonObject = new JSONObject(vochers[i]);
				writeLog("分录科目："+vochers[i].getAccountNumber()+"--传递给金蝶的分录明细"+i+":"+jsonObject.toString());
			}
			for(int i=0;i<vochers.length; i++){
				writeLog("科目编号"+i+":"+vochers[i].getAccountNumber());
			}
			
			String[] returnValues = vProxy.importVoucherOfReturnID(vochers,0, 0,0);
			writeLog("-----金蝶返回值："+returnValues[0]+"--------");
			String[] result = returnValues[0].split("\\|\\|");
			if (result[4].equals("成功保存")) {
				writeLog("-----【成功增加凭证！金蝶凭证编号："+result[5]+"】--------");
				writeLog("增加凭证成功后返回信息："+result[0]+"-"+result[1]+"-"+result[2]+"-"+result[3]+"-"+result[4]+"-"+result[5]+"-"+result[6]);
				return result[4]+"-"+result[5];
			}else{
				writeLog("增加凭证失败后返回信息："+returnValues[0]);
				return "增加凭证失败后返回信息："+returnValues[0];
			}
		} catch (Exception e) {
			writeLog("向EAS抛凭证时出现异常："+e);
			return e.toString();
		}
	}
}
