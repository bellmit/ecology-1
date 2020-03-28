package weaver.interfaces.workflow.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

import javax.activation.DataHandler;
import javax.activation.FileTypeMap;
import javax.servlet.http.HttpServletRequest;

import sun.misc.BASE64Encoder;
import weaver.conn.ConnStatement;
import weaver.conn.RecordSet;
import weaver.email.MailErrorMessageInfo;
import weaver.file.AESCoder;
import weaver.file.FileUpload;
import weaver.general.*;
import weaver.soa.workflow.request.RequestInfo;
import weaver.systeminfo.SystemEnv;

/**
 * 
 * 流程Email
 * 
 * @author lsq
 * @date 2019/4/16
 * @version 1.0
 */
public class WorkFlow_Email extends BaseBean implements Action {

	private static BaseBean baseBean = new BaseBean();

	@Override
	public String execute(RequestInfo request) {
		writeLog("进入重写方法。。。。");
		RecordSet rs = new RecordSet();
		SendMail localSendMail = new SendMail();
        //获取配置文件中的数据
		//发送邮件的邮箱用户名
		String userName = Util.null2String(baseBean.getPropValue(
				"emailaccount", "Username"));
		//注意：不是登陆密码，是授权码
		String password = Util.null2String(baseBean.getPropValue(
				"emailaccount", "Password"));
		//邮箱服务器
		String mailServer = Util.null2String(baseBean.getPropValue(
				"emailaccount", "MailServer"));
		//认证
		Boolean needAuth = Util.null2String(
				baseBean.getPropValue("emailaccount", "Needauthsend")).equals(
				"1");
		//SSL协议
		String needSSL = Util.null2String(baseBean.getPropValue("emailaccount",
				"NeedSSL"));
		//服务器端口号
		String serverPort = Util.null2String(baseBean.getPropValue(
				"emailaccount", "SmtpServerPort"));
		//发送邮件的邮箱账户
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
		String sendAccountAddress = "";
		// 收件人
		String sendTo = "";
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
		String fujian1 = "";
		String fujian2 = "";
		String fujian3 = "";
		String fujian4 = "";

		String requestid = Util.null2String(request.getRequestid());
		String sql = "select '13873449643@163.com' as sendto,'' as sendcc,'' as sendBcc,dhwb1 as subject,dhwb2 as content,'' as priority,fj as fujian,fj2 as fujian2,fj3 as fujian3,fj4 as fujian4 from formtable_main_176 where requestid='"
				+ requestid + "'";
		writeLog("sql语句----" + sql);
		rs.executeSql(sql);
		if (rs.next()) {
			sendTo = Util.null2String(rs.getString("sendto"));
			sendCc = Util.null2String(rs.getString("sendcc"));
			sendBcc = Util.null2String(rs.getString("sendbcc"));
			subject = Util.null2String(rs.getString("subject"));
			text = Util.null2String(rs.getString("content"));
			priority = Util.null2String(rs.getString("priority"));
			fujian1 = Util.null2String(rs.getString("fujian"));
			fujian2 = Util.null2String(rs.getString("fujian2"));
			fujian3 = Util.null2String(rs.getString("fujian3"));
			fujian4 = Util.null2String(rs.getString("fujian4"));
			writeLog("fujian2---" + fujian2);
		}

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
		boolean respect = false;

		// 标记
		boolean isfujian = false;

		// 判断附件1有无
		if ( fujian1.length()>0||fujian1 != null) {
			// 一个附件中包含了多个文件
			String[] strArr = fujian1.split(",");
			writeLog("strArr----" + strArr);
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
					writeLog("开始1");
					filenames.add(imagefilename);
					filecontents.add(localObject16);

					writeLog("开始2");
				} catch (Exception e) {
					writeLog("异常为-------" + e);
				}
			}
			isfujian = true;
		}
		// 判断附件2有无
		if (fujian2.length()>0||fujian2 != null) {
			// 一个附件中包含了多个文件
			String[] strArr = fujian2.split(",");
			writeLog("strArr----" + strArr);
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
					writeLog("开始1");
					filenames.add(imagefilename);
					filecontents.add(localObject16);

					writeLog("开始2");
				} catch (Exception e) {
					writeLog("异常为-------" + e);
				}
			}
			isfujian = true;
		}
		// 判断附件3有无
		if (fujian3.length()>0||fujian3 != null) {
			// 一个附件中包含了多个文件
			String[] strArr = fujian3.split(",");
			writeLog("strArr----" + strArr);
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
					writeLog("开始1");
					filenames.add(imagefilename);
					filecontents.add(localObject16);

					writeLog("开始2");
				} catch (Exception e) {
					writeLog("异常为-------" + e);
				}
			}
			isfujian = true;
		}
		// 判断附件4有无
		if (fujian4.length()>0||fujian4 != null) {
			// 一个附件中包含了多个文件
			String[] strArr = fujian4.split(",");
			writeLog("strArr----" + strArr);
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
					writeLog("开始1");
					filenames.add(imagefilename);
					filecontents.add(localObject16);
					writeLog("开始2");
				} catch (Exception e) {
					writeLog("异常为-------" + e);
				}
			}
			isfujian = true;
		}
		

		if (isfujian) {
	        //给hashmap赋值
			int i6 = filenames.size();
			for (int i7 = 0; i7 < i6; i7++) {
				writeLog("开始9");
				localObject10 = filecontents.get(i7);
				writeLog("开始9.1");
				filerealpath = (String) filenames.get(i7);
				writeLog("开始9.2");
				iszip = FileTypeMap.getDefaultFileTypeMap().getContentType(
						(String) filerealpath);
				writeLog("开始9.3");
				isencrypt = new BASE64Encoder();
				writeLog("开始9.4");
				filerealpath = "=?GBK?B?"
						+ ((BASE64Encoder) isencrypt)
								.encode(((String) filerealpath).getBytes()) + "?=";
				writeLog("开始9.5");
				filerealpath = ((String) filerealpath).replace("\n", "");
				writeLog("开始9.6");
				isaesencrypt = new StringBuffer(((String) filerealpath).length());
				writeLog("开始10");
				for (int i10 = 0; i10 < ((String) filerealpath).length(); i10++) {
					if (filerealpath.getBytes()[i10] != 13) {
						((StringBuffer) isaesencrypt)
								.append(((String) filerealpath).charAt(i10));
					}
				}
				writeLog("开始11");
				DataHandler localDataHandler = new DataHandler(
						new ByteArrayDataSource((InputStream) localObject10,
								(String) iszip));
				localHashMap.put(((StringBuffer) isaesencrypt).toString(),
						localDataHandler);
				writeLog("开始12");
			}
			//调用发送邮件方法(带附件)
			respect = localSendMail.sendMiltipartText(sendAccountAddress,
					sendTo, sendCc, sendBcc, subject, text, filenames,
					filecontents, priority, localHashMap);
		} else {
			//调用发送邮件方法
			respect = localSendMail.send(sendAccountAddress, sendTo, sendCc,
					sendBcc, subject, text, priority);
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
