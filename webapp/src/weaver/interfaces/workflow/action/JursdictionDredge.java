package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.SendMail;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 
 * @author lsq
 * @date 2019/4/17
 * @version 1.0
 */
public class JursdictionDredge extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		writeLog("进入重写方法。。。。");
		RecordSet rs = new RecordSet();
		SendMail localSendMail = new SendMail();

		// 邮箱密码
		String str1 = "";
		// 登录名
		String str2 = "";
		// SMTP服务器
		String str3 = "";
		// 是否需要发件认证
		String str4 = "";
		// SMTP服务器端口
		String str5 = "";
		// 帐户名称
		String str6 = "";
		boolean bool1 = false;

		rs.executeSql("SELECT * FROM MailAccount WHERE id=(select max(id) from MailAccount)");
		if (rs.next()) {

			str1 = rs.getString("accountPassword");
			str2 = rs.getString("accountId");
			str3 = rs.getString("smtpServer");
			bool1 = Util.null2String(rs.getString("needCheck")).equals("1");
			str4 = rs.getString("sendneedSSL");
			str5 = rs.getString("smtpServerPort");
			str6 = rs.getString("accountName");

			writeLog("str1---" + str1);
			writeLog("str2---" + str2);
			writeLog("str3---" + str3);
			writeLog("bool1---" + bool1);
			writeLog("str4---" + str4);
			writeLog("str5---" + str5);
			writeLog("str6---" + str6);
		}

		localSendMail.setUsername(str2);
		localSendMail.setPassword(str1);
		localSendMail.setMailServer(str3);
		localSendMail.setNeedauthsend(bool1);
		localSendMail.setNeedSSL(str4);
		localSendMail.setSmtpServerPort(str5);
		localSendMail.setAccountName(str6);
		

		String str = "";
		// 账户地址
		String sendfrom = "";
		// 收件人
		String sendTo = "";
		// 抄送人
		String sendCc = "";
		// 密送人
		String sendBcc = "";
		// 主题
		String subject = "权限开通";
		// 内容
		String text = "";
		// 优先级
		String priority = "";

		String requestid = Util.null2String(request.getRequestid());
		String sql = "select * from uf_crm_openpermiss where requestid='"
				+ requestid + "'";
		writeLog("sql语句----" + sql);
		rs.executeSql(sql);
		if (rs.next()) {

			text = Util.null2String(rs.getString("content"));
			writeLog("text---" + text);

		}

		boolean respect = localSendMail.send(sendfrom, sendTo,
				sendCc, sendBcc, subject, text, priority);
		writeLog("respect-----" + respect);
		if (respect == true) {
			str = "发送成功！";
		} else {
			str = "发送失败！";
		}
		writeLog("str-----" + str);
		return str;

	}

}
