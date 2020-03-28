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
 * level2非展示行情许可 流程归档时  发送邮件
 * @author lsq
 * @date 2019/7/9
 * @version 1.0
 */
public class Capacity_Email_Level_2_dataFeed_permis extends BaseBean implements Action{
	private static BaseBean baseBean = new BaseBean();
	@Override
	public String execute(RequestInfo request) {
		RecordSet rs=new RecordSet();
		RecordSet rs1=new RecordSet();
		RecordSet rs2=new RecordSet();
		RecordSet rs3=new RecordSet();
		RecordSet rs4=new RecordSet();
		String requestid=Util.null2String(request.getRequestid());
		String sql=" select * from 	formtable_main_99  where requestid='"+requestid+"'";
		rs.executeSql(sql);
		String 	customer="";  //客户
		String 	business="";  //所属业务
		String 	sumMoney="";  //金额
		String 	ywfzr="";  //业务负责人
		String 	gmcp="";  //购买产品
		String 	gsdm="";  //公司代码
		String 	xkfw="";  //许可范围
		String 	companyName="";  //公司名称
		String 	companyId="";  //公司id
		String 	businessLinkMan="";  //联系人
		String 	businessTel="";  //电话
		String 	email="";  //电子邮箱
		String 	fax="";  //手机
		String 	address="";  //联系地址
		String 	zipCode="";  //邮编
		String 	dataUser_other="";  //数据用途其他
		String 	dataUse_lhpt="";  //量化平台
		String 	dataUse_Owrite="";  //数据展示其他填写
		String 	dataUse_qxzs="";  //期权做市
		String 	startTime="";  //起始时间
		String 	systemName="";  //系统名称
		String 	manufacturer="";  //开发厂商
		String 	interfaceLinkMan="";  //接口联系人
		String 	interfaceTel="";  //电话（接口）
		String 	interfaceEmail="";  //邮箱（接口）
		String 	operationLinkman="";  //运维联系人
		String 	operationTel="";  //电话（运维）
		String 	operationEmail="";  //邮箱（运维）
		String 	emergencyLinkMan="";  //应急联系人
		String 	emergencyTel="";  //电话（应急）
		String 	emergencyEmail="";  //邮箱（应急）
		String 	scale="";  //转发数据规模
		String 	files="";  //外部申请附件
		String 	status="";  //订单状态
		String 	dgrq="";  //订购日期
		String 	fkqx="";  //付款期限
		String 	applyType="";  //申请类型
		String 	internAttach="";  //内部审核附件
		String 	transmit="";  //转发用途
		String  machineroom="";  //机房
		if(rs.next()){
			customer=Util.null2String(rs.getString("customer"));
			business=Util.null2String(rs.getString("business"));
			sumMoney=Util.null2String(rs.getString("sumMoney"));
			ywfzr=Util.null2String(rs.getString("ywfzr"));
			gmcp=Util.null2String(rs.getString("gmcp"));
			gsdm=Util.null2String(rs.getString("gsdm"));
			xkfw=Util.null2String(rs.getString("xkfw"));
			companyName=Util.null2String(rs.getString("companyName"));
			companyId=Util.null2String(rs.getString("companyId"));
			businessLinkMan=Util.null2String(rs.getString("businessLinkMan"));
			businessTel=Util.null2String(rs.getString("businessTel"));
			email=Util.null2String(rs.getString("email"));
			fax=Util.null2String(rs.getString("fax"));
			address=Util.null2String(rs.getString("address"));
			zipCode=Util.null2String(rs.getString("zipCode"));
			dataUser_other=Util.null2String(rs.getString("dataUser_other"));
			if(dataUser_other.equals("1")){
				dataUser_other="其他";
			}
			dataUse_lhpt=Util.null2String(rs.getString("dataUse_lhpt"));
			if(dataUse_lhpt.equals("1")){
				dataUse_lhpt="量化平台";
			}
			dataUse_Owrite=Util.null2String(rs.getString("dataUse_Owrite"));
			dataUse_qxzs=Util.null2String(rs.getString("dataUse_qxzs"));
			if(dataUse_qxzs.equals("1")){
				dataUse_qxzs="期权做市";
			}
			startTime=Util.null2String(rs.getString("startTime"));
			systemName=Util.null2String(rs.getString("systemName"));
			manufacturer=Util.null2String(rs.getString("manufacturer"));
			interfaceLinkMan=Util.null2String(rs.getString("interfaceLinkMan"));
			interfaceTel=Util.null2String(rs.getString("interfaceTel"));
			interfaceEmail=Util.null2String(rs.getString("interfaceEmail"));
			operationLinkman=Util.null2String(rs.getString("operationLinkman"));
			operationTel=Util.null2String(rs.getString("operationTel"));
			operationEmail=Util.null2String(rs.getString("operationEmail"));
			emergencyLinkMan=Util.null2String(rs.getString("emergencyLinkMan"));
			emergencyTel=Util.null2String(rs.getString("emergencyTel"));
			emergencyEmail=Util.null2String(rs.getString("emergencyEmail"));
			scale=Util.null2String(rs.getString("scale"));
			files=Util.null2String(rs.getString("files"));
			status=Util.null2String(rs.getString("status"));
			dgrq=Util.null2String(rs.getString("dgrq"));
			fkqx=Util.null2String(rs.getString("fkqx"));
			applyType=Util.null2String(rs.getString("applyType"));
			internAttach=Util.null2String(rs.getString("internAttach"));
			transmit=Util.null2String(rs.getString("transmit"));
			if(transmit.equals("0")){
				transmit="机房内转发";
			}else if(transmit.equals("1")){
				transmit="接口转发";
			}
			String id=Util.null2String(rs.getString("id"));
			String sql1="select engineName,xtbswz from formtable_main_99_dt1 where mainid='"+id+"'";
			rs1.executeSql(sql1);
			while(rs1.next()){
				String engineName=Util.null2String(rs1.getString("engineName"));
				String xtbswz=Util.null2String(rs1.getString("xtbswz"));
				machineroom+="机房地址:"+engineName+"  系统部署位置:"+xtbswz+"\r\n";
			}
		}
		String sql2 = "select name from  uf_crm_customerinfo where id='"
				+ customer + "'";
		rs2.executeSql(sql2);
		if (rs2.next()) {
			customer = Util.null2String(rs2.getString("name"));
		}
		String sql3 = "select name,businessleader from  uf_crm_businessinfo  where id='"
				+ business + "'";
		rs3.executeSql(sql3);
		if (rs3.next()) {
			business = Util.null2String(rs3.getString("name"));
		}
		String sql4 = "select lastname from  hrmresource where id='" + ywfzr
				+ "'";
		rs4.executeSql(sql4);
		if (rs4.next()) {
			ywfzr = Util.null2String(rs4.getString("lastname"));
		}
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
		String sendTo =  Util.null2String(baseBean.getPropValue(
				"inBox", "emailAddress"));
		// 抄送人
		String sendCc = "";
		// 密送人
		String sendBcc = "";
		// 主题
		String subject = "";
		// 内容
		String text ="基本信息\r\n公司名称:"+companyName+"\r\n客户:"+customer+"\r\n所属业务:"+business+"\r\n联系人:"+businessLinkMan+"\r\n电话:"+businessTel+"\r\n电子邮件:"+email+"\r\n手机:"+fax+"\r\n联系地址:"+address+"\r\n邮编:"+zipCode;
		if(applyType.equals("0")){   //自用
			subject="LEVEL-2非展示数据自用申请(客户:"+customer+")";
			text+="\r\n\r\n使用描述\r\n数据用途:"+dataUse_lhpt+" "+dataUse_qxzs+" "+dataUse_Owrite+"\r\n起始时间:"+startTime+"\r\n指定机房:\r\n"+machineroom;
		}else if(applyType.equals("1")){  //转发
			subject="LEVEL-2非展示数据转发申请(客户:"+customer+")";
			text+="\r\n\r\n转发用途\r\n转发用途:"+transmit
		            +"\r\n转发用户规模:"+scale+"个"
					+"\r\n转发机房信息:\r\n"+machineroom;
		}
		text+="\r\n系统说明\r\n系统名称:"+systemName+"\r\n开发商:"+manufacturer
				+"\r\n技术联系\r\n接口联系人:"+interfaceLinkMan+"  电话:"+interfaceTel+"  邮箱:"+interfaceEmail+"\r\n运维联系人:"+operationLinkman+"  电话:"+operationTel+"  邮编:"+operationEmail+"\r\n紧急联系人:"+emergencyLinkMan+"  电话:"+emergencyTel+"  邮编:"+emergencyEmail;
		// 优先级
		String priority = "";
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
		if (files.length() > 0) {
			// 一个附件中包含了多个文件
			String[] strArr = files.split(",");
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
		boolean isoperationPlan = false;
		// 判断经营计划有无
		if (internAttach.length() > 0) {
			// 一个附件中包含了多个文件
			String[] strArr = internAttach.split(",");
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

			isoperationPlan = true;
		}
		boolean respect = false;
		if (isfujian||isoperationPlan) {
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
			writeLog("含附件");
			// 调用发送邮件方法(带附件)
			respect = localSendMail.sendMiltipartText(sendAccountAddress,
					sendTo, sendCc, sendBcc, subject, text, filenames,
					filecontents, priority, localHashMap);
			
		} else {
			writeLog("不含附件");
			// 调用发送邮件方法
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
