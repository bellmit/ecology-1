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
 * level1 流程归档时,发送邮件
 * 
 * @author lsq
 * @date 2019/7/8
 * @version 1.0
 */
public class Capacity_Email_openpermiss extends BaseBean implements Action {
	private static BaseBean baseBean = new BaseBean();

	@Override
	public String execute(RequestInfo request) {
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		RecordSet rs2 = new RecordSet();
		RecordSet rs3 = new RecordSet();
		RecordSet rs4 = new RecordSet();
		RecordSet rs5 = new RecordSet();
		String requsetid = Util.null2String(request.getRequestid());
		String sql = "select *  from uf_crm_openpermiss where requestid='"
				+ requsetid + "'";
		rs.executeSql(sql);
		// String sfxs = ""; // 是否显示 共计69个字段

		String ktlx = "";// 开通类型
		String flownum = ""; // 编号
		String customer = "";// 客户名称
		String business = "";// 所属业务
		String createdate = "";// 日期
		String content1 = ""; // 介入机房
		String content2 = "";// 专线线路
		String content3 = "";// 行情用途
		String startdate = "";// 权限起始时间
		String enddate = "";// 权限终止时间
		String reminddate = "";// 权限到期提醒时间
		String openfile = "";// 开通的相关文件
		String remark1 = "";// 备注
		String opendate = "";// 开通时间
		String remark2 = "";// 操作说明
		String content4 = "";
		String content5 = "";
		String content6 = "";
		if (rs.next()) {
			customer = Util.null2String(rs.getString("customer"));
			business = Util.null2String(rs.getString("business"));
			ktlx = Util.null2String(rs.getString("ktlx"));
			if (ktlx.equals("0")) {
				ktlx = "新开(新客户)";
			} else if (ktlx.equals("1")) {
				ktlx = "增加(老客户)";
			}
			flownum = Util.null2String(rs.getString("flownum"));
			createdate = Util.null2String(rs.getString("createdate"));
			content1 = Util.null2String(rs.getString("content1"));
			content2 = Util.null2String(rs.getString("content2"));
			content3 = Util.null2String(rs.getString("content3"));
			String[] conarr1 = content1.split(",");
			for (int i = 0; i < conarr1.length; i++) {
				String sql3 = "select content from uf_crm_permissinfo where id='"
						+ conarr1[i] + "'";
				rs3.executeSql(sql3);
				if (rs3.next()) {

					content4 += Util.null2String(rs3.getString("content"))
							+ "  ";
				}
			}
			String[] conarr2 = content2.split(",");
			for (int i = 0; i < conarr2.length; i++) {
				String sql4 = "select content from uf_crm_permissinfo where id='"
						+ conarr2[i] + "'";
				rs4.executeSql(sql4);
				if (rs4.next()) {
					content5 += Util.null2String(rs4.getString("content"))
							+ "  ";
				}
			}
			String[] conarr3 = content3.split(",");
			for (int i = 0; i < conarr3.length; i++) {
				String sql5 = "select content from uf_crm_permissinfo where id='"
						+ conarr3[i] + "'";
				rs5.executeSql(sql5);
				if (rs5.next()) {
					content6 += Util.null2String(rs5.getString("content"))
							+ " ";
				}
			}
			startdate = Util.null2String(rs.getString("startdate"));
			enddate = Util.null2String(rs.getString("enddate"));
			reminddate = Util.null2String(rs.getString("reminddate"));
			openfile = Util.null2String(rs.getString("openfile"));
			remark1 = Util.null2String(rs.getString("remark1"));
			opendate = Util.null2String(rs.getString("opendate"));
			remark2 = Util.null2String(rs.getString("remark2"));
		}
		if (business.equals("1") || business.equals("2")
				|| business.equals("3") || business.equals("4")
				|| business.equals("7") || business.equals("5")
				|| business.equals("6")) { // 业务是这几个 才发送邮件
			// 客户
			String sql1 = "select name from  uf_crm_customerinfo where id='"
					+ customer + "'";
			rs1.executeSql(sql1);
			if (rs1.next()) {
				customer = Util.null2String(rs1.getString("name"));
			}
			// 业务
			String sql2 = "select name from  uf_crm_businessinfo  where id='"
					+ business + "'";
			rs2.executeSql(sql2);
			String businesscontent = "";
			if (rs2.next()) {
				businesscontent = Util.null2String(rs2.getString("name"));
			}
			// 收件人
			String sendTo = "";
			if (business.equals("5") || business.equals("6")) {
				sendTo = Util.null2String(baseBean.getPropValue("sendto",
						"emailAddress2"));
			} else if (business.equals("1") || business.equals("2")
					|| business.equals("3") || business.equals("4")
					|| business.equals("7")) {
				sendTo = Util.null2String(baseBean.getPropValue("sendto",
						"emailAddress1"));
			}
			// 内容
			String text = "标题:权限开通\r\n编号:" + flownum + "\r\n日期:" + createdate
					+ "\r\n客户:" + customer + "\r\n所属业务:" + businesscontent
					+ "\r\n开通类型:" + ktlx;
			if (business.equals("1")) {
				text += "\r\n数据传输类型:" + content4 + "\r\n接入系统:" + content5
						+ "\r\n开通内容:" + content6;
			} else {
				text += "\r\n申请行情:" + content4 + "\r\n接入机房:" + content5
						+ "\r\n专线线路:" + content6;
			}
			text += "\r\n权限起始时间:" + startdate + "\r\n权限终止时间:" + enddate
					+ "\r\n到期提醒时间:" + reminddate + "\r\n开通时间:" + opendate
					+ "\r\n操作说明:" + remark2 + "\r\n备注:" + remark1;
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
					baseBean.getPropValue("emailaccount", "Needauthsend"))
					.equals("1");
			// SSL协议
			String needSSL = Util.null2String(baseBean.getPropValue(
					"emailaccount", "NeedSSL"));
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
			// 抄送人
			String sendCc = "";
			// 密送人
			String sendBcc = "";
			// 主题
			String subject = "权限开通(客户:" + customer + ")";

			// 优先级
			String priority = "";
			// 附件id
			// String fujian = "";

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
			if (openfile.length() > 0) {
				// 一个附件中包含了多个文件
				String[] strArr = openfile.split(",");
				writeLog("strArr.length:" + strArr.length);
				for (int i = 0; i < strArr.length; i++) {
					writeLog("strArr[i]:" + strArr[i]);
					sql = "SELECT imagefileid FROM docimagefile WHERE docid='"
							+ strArr[i] + "'";
					rs.executeSql(sql);
					writeLog("sql------" + sql);
					if (rs.next()) {
						imageFileId = Util
								.null2String(rs.getInt("imagefileid"));
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
				isfujian = true;
			}
			// 标记
			boolean respect = false;
			if (isfujian) {
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
							new ByteArrayDataSource(
									(InputStream) localObject10, (String) iszip));
					localHashMap.put(((StringBuffer) isaesencrypt).toString(),
							localDataHandler);
				}
				writeLog("含附件");
				// 调用发送邮件方法(带附件)
				respect = localSendMail.sendMiltipartText(sendAccountAddress,
						sendTo, sendCc, sendBcc, subject, text, filenames,
						filecontents, priority, localHashMap);

			} else {
				writeLog("不含附件");
				// 调用发送邮件方法
				respect = localSendMail.send(sendAccountAddress, sendTo,
						sendCc, sendBcc, subject, text, priority);

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
		return "1";
	}

}
