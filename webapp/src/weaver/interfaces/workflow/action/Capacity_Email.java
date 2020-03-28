package weaver.interfaces.workflow.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

import javax.activation.DataHandler;
import javax.activation.FileTypeMap;

import sun.misc.BASE64Encoder;

import weaver.conn.RecordSet;
import weaver.file.AESCoder;
import weaver.general.BaseBean;
import weaver.general.ByteArrayDataSource;
import weaver.general.SendMail;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 流程归档发送邮件
 * 
 * @author lsq
 * @date 2019/4/22
 * @version 1.0
 */
public class Capacity_Email extends BaseBean implements Action {

	private static BaseBean baseBean = new BaseBean();

	@Override
	public String execute(RequestInfo request) {
		RecordSet rs = new RecordSet();
		SendMail localSendMail = new SendMail();
		// 获取配置文件中的数据
		// 发送邮件的邮箱用户名
		String userName = Util.null2String(baseBean.getPropValue(
				"emailaccount", "Username"));
		// 注意：不是登陆密码，是授权码
		String password = Util.null2String(baseBean.getPropValue(
				"emailaccount", "Password"));
		// 发送邮件的邮箱服务器
		String mailServer = Util.null2String(baseBean.getPropValue(
				"emailaccount", "MailServer"));
		// 认证
		Boolean needAuth = Util.null2String(
				baseBean.getPropValue("emailaccount", "Needauthsend")).equals(
				"1");
		// SSL协议
		String needSSL = Util.null2String(baseBean.getPropValue("emailaccount",
				"NeedSSL"));
		// 服务器端口号
		String serverPort = Util.null2String(baseBean.getPropValue(
				"emailaccount", "SmtpServerPort"));
		// 发送邮件的邮箱账户
		String accountName = Util.null2String(baseBean.getPropValue(
				"emailaccount", "AccountName"));
		localSendMail.setUsername(userName);
		localSendMail.setPassword(password);
		localSendMail.setMailServer(mailServer);
		localSendMail.setNeedauthsend(needAuth);
		localSendMail.setNeedSSL(needSSL);
		localSendMail.setSmtpServerPort(serverPort);
		localSendMail.setAccountName(accountName);

		// 发件人账户地址
		String sendAccountAddress = accountName;
		// 收件人
		String sendTo = Util.null2String(baseBean.getPropValue("inBox",
				"emailAddress"));
		// 抄送人
		String sendCc = "";
		// 密送人
		String sendBcc = "";
		// 主题
		String subject = "";
		// 内容
		String text = "";
		// 优先级
		String priority = "";
		// 附件id
		String fujian = "";

		String num = "";
		String applyDate = "";
		String applyPerson = "";
		String Source = "";
		String sloveDept = "";
		String remark = "";

		// 获取流程id
		String requestid = Util.null2String(request.getRequestid());
		String sql = "SELECT * FROM formtable_main_255 WHERE requestid='"
				+ requestid + "'";
		rs.executeSql(sql);
		if (rs.next()) {
			num = Util.null2String(rs.getString("Num"));
			applyDate = Util.null2String(rs.getString("ApplyDate"));
			subject = Util.null2String(rs.getString("Title"));
			applyPerson = Util.null2String(rs.getString("ApplyPerson"));
			Source = Util.null2String(rs.getString("Source"));
			sloveDept = Util.null2String(rs.getString("SolveDept"));
			priority = Util.null2String(rs.getString("Priority"));
			remark = Util.null2String(rs.getString("Remark"));
			fujian = Util.null2String(rs.getString("RelevantAccessory"));
			writeLog("附件-----" + fujian);
		}
		// 部门名
		String deptName="";
		// 人员名
		String lastName = "";
		// 来源
		String selectName = "";
		// 优先级
		String priorityName = "";
		// 签字人
		String operatorName = "";
		// 签字日期
		String opratorDate = "";
		// 签字时间
		String operatorTime = "";
		// 签字内容
		String opratorRemark = "";
		// 签字意见
		String pinjie = "";
		
		String[] deptId=sloveDept.split(",");
		for(int i=0;i<deptId.length;i++){
			sql = "SELECT * FROM HrmDepartment WHERE id='" + deptId[i] + "'";
			rs.executeSql(sql);
			if (rs.next()) {
				String departmentName  = Util.null2String(rs.getString("departmentname"));
				deptName+=departmentName+"  ";
			}
		}
		
		sql = "SELECT * FROM hrmresource WHERE id='" + applyPerson + "'";
		rs.executeSql(sql);
		if (rs.next()) {
			lastName = Util.null2String(rs.getString("lastname"));
		}

		sql = "SELECT * FROM workflow_SelectItem WHERE fieldid='11863' and selectvalue='"
				+ priority + "'";
		rs.executeSql(sql);
		if (rs.next()) {
			priorityName = Util.null2String(rs.getString("selectname"));
		}

		sql = "SELECT * FROM workflow_SelectItem WHERE fieldid='11864' and selectvalue='"
				+ Source + "'";
		rs.executeSql(sql);
		if (rs.next()) {
			selectName = Util.null2String(rs.getString("selectname"));
		}

		sql = "SELECT (SELECT lastname FROM HrmResource WHERE id =operator) as operatorname,* FROM workflow_requestLog WHERE requestid='"
				+ requestid + "'";
		rs.executeSql(sql);
		while (rs.next()) {
			operatorName = Util.null2String(rs.getString("operatorname"));
			opratorDate = Util.null2String(rs.getString("operatedate"));
			operatorTime = Util.null2String(rs.getString("operatetime"));
			opratorRemark = Util.null2String(rs.getString("remark"));

			String txt = opratorRemark.replaceAll("</?[^>]+>", ""); // 剔出<html>的标签
			// txt = opratorRemark.replaceAll("<a>\\s*|\t|\r|\n</a>",
			// "");//去除字符串中的空格,回车,换行符,制表符

			pinjie += operatorName + "    " + opratorDate + "  " + operatorTime
					+ "  " + txt + "\r\n";
		}

		text = "编号:" + num + "\r\n标题:" + subject + "\r\n申请日期:" + applyDate
				+ "\r\n申请人:" + lastName + "\r\n来源:" + selectName + "\r\n优先级:"
				+ priorityName + "\r\n解决部门:" + deptName + "\r\n详情描述:"
				+ remark + "\r\n签字意见:\r\n" + pinjie;
		// 附件名
		ArrayList<String> filenames = new ArrayList<String>();
		// 附件内容
		ArrayList<Object> filecontents = new ArrayList<Object>();
		HashMap<String, DataHandler> localHashMap = new HashMap<String, DataHandler>();

		// 文件id
		String imageFileId = "";
		// 文件名
		String imagefilename = "";
		// 文件类型
		String imagefiletype = "";
		// 文件存放目录
		String filerealpath = "";
		// 是否压缩
		String iszip = "";
		// 是否加密
		Object isencrypt = null;
		// 是否使用AES附件加密
		Object isaesencrypt = null;
		// AES加密密码
		String aescode = "";
		// 文件大小
		String fileSize = "";
		// 文件
		String imagefile = "";
		Object localObject16 = null;
		Object localObject10 = null;
		sendAccountAddress = accountName;
		// 标记
		boolean isfujian = false;

		// 判断附件有无
		if (fujian.length() > 0 || fujian != null) {
			// 一个附件中包含了多个文件
			String[] strArr = fujian.split(",");
			for (int i = 0; i < strArr.length; i++) {
				sql = "SELECT imagefileid FROM docimagefile WHERE docid='"
						+ strArr[i] + "'";
				rs.executeSql(sql);
				writeLog("sql------" + sql);
				if (rs.next()) {
					imageFileId = Util.null2String(rs.getInt("imagefileid"));
				}
				writeLog("imagefileid----" + imageFileId);
				sql = "SELECT * FROM imagefile WHERE imagefileid='"
						+ imageFileId + "'";
				rs.executeSql(sql);
				if (rs.next()) {
					imagefilename = Util.null2String(rs
							.getString("imagefilename"));
					imagefiletype = Util.null2String(rs
							.getString("imagefiletype"));
					filerealpath = Util.null2String(rs
							.getString("filerealpath"));
					iszip = Util.null2String(rs.getString("iszip"));
					isencrypt = Util.null2String(rs.getString("isencrypt"));

					aescode = Util.null2String(rs.getString("aescode"));
					fileSize = Util.null2String(rs.getString("fileSize"));
					imagefile = Util.null2String(rs.getString("imagefile"));
					isaesencrypt = Util.null2String(rs
							.getString("isaesencrypt"));
				}
				writeLog("开始");
				try {

					File localFile = new File((String) filerealpath);
					if (((String) iszip).equals("1")) {
						ZipInputStream localZipInputStream = new ZipInputStream(
								new FileInputStream(localFile));
						if (localZipInputStream.getNextEntry() != null) {
							localObject16 = new BufferedInputStream(
									localZipInputStream);
						}
					} else {
						localObject16 = new BufferedInputStream(
								new FileInputStream(localFile));
					}

					if (((String) isaesencrypt).equals("1")) {
						localObject16 = AESCoder.decrypt(
								(InputStream) localObject16, aescode);
					}
					filenames.add(imagefilename);
					filecontents.add(localObject16);

				} catch (Exception e) {
					writeLog("异常为-------" + e);
				}
			}
			// 给hashmap赋值
			int i6 = filenames.size();
			for (int i7 = 0; i7 < i6; i7++) {
				localObject10 = filecontents.get(i7);
				filerealpath = (String) filenames.get(i7);
				iszip = FileTypeMap.getDefaultFileTypeMap().getContentType(
						(String) filerealpath);
				isencrypt = new BASE64Encoder();
				filerealpath = "=?GBK?B?"
						+ ((BASE64Encoder) isencrypt)
								.encode(((String) filerealpath).getBytes())
						+ "?=";
				filerealpath = ((String) filerealpath).replace("\n", "");
				isaesencrypt = new StringBuffer(
						((String) filerealpath).length());
				for (int i10 = 0; i10 < ((String) filerealpath).length(); i10++) {
					if (filerealpath.getBytes()[i10] != 13) {
						((StringBuffer) isaesencrypt)
								.append(((String) filerealpath).charAt(i10));
					}
				}
				DataHandler localDataHandler = new DataHandler(
						new ByteArrayDataSource((InputStream) localObject10,
								(String) iszip));
				localHashMap.put(((StringBuffer) isaesencrypt).toString(),
						localDataHandler);
			}
			isfujian = true;
		}

		boolean respect = false;
		if (isfujian) {
			// 调用发送邮件方法(带附件)
			respect = localSendMail.sendMiltipartText(sendAccountAddress,
					sendTo, sendCc, sendBcc, subject, text, filenames,
					filecontents, priority, localHashMap);
			writeLog("含附件");
		} else {
			// 调用发送邮件方法
			respect = localSendMail.send(sendAccountAddress, sendTo, sendCc,
					sendBcc, subject, text, priority);
			writeLog("不含附件");
		}
		String str = "";
		if (respect == true) {
			str = "发送成功！";
			writeLog("str-----" + str);
		} else {
			str = "发送失败！";
			writeLog("str-----" + str);
			return "0";
		}

		return "1";
	}

}
