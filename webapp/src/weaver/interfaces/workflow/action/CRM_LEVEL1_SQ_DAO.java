package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import weaver.conn.*;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.soa.workflow.request.MainTableInfo;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.RequestService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//信息商上游触发上证level1申请流程
public class CRM_LEVEL1_SQ_DAO extends BaseCronJob {
    BaseBean lg = new BaseBean();
    SelectSYFJ sc = new SelectSYFJ();
    Calendar today = Calendar.getInstance();
    String syndate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);

    // 通过继承BaseCronJob类可以实现定时同步
    public void execute() {
        try {
            create();
        } catch (Exception ignored) {
        }
    }

    /**
     * level1申请流程触发
     */
    public void create() {
        RecordSetDataSource rds = new RecordSetDataSource("exchangeDB");
        String sql = "select * from SYNC_WEBSITE_LEVEL1 where STATUS=0";
        rds.executeSql(sql);
        while (rds.next()) {
            String syid = Util.null2String(rds.getString("ID"));
            String gsid = Util.null2String(rds.getString("COMPANY_ID"));
            //公司基本信息：根据gsid查询出公司的全部信息
            String gsmc = "";
            String zcdz = "";
            String zzjgdm = "";
            String frdb = "";
            String gswz = "";
			/*String glfzr = "";
			String fzrdh = "";
			String ywlxr = "";
			String lxrdh = "";
			String gscz = "";
			String Email = "";
			String jsfzr = "";
			String fzrdh1 = "";
			String sjkfwq = "";
			String gsbj = "";
			String mgs = "";
			*/
            String bgdz = "";

            String wxzz = "";
            String xzgly = "";

            RecordSetDataSource rds1 = new RecordSetDataSource("exchangeDB");
            String sqlcom = "select * from SYNC_COMPANY where ID='" + gsid + "'";
            rds1.executeSql(sqlcom);
            if (rds1.next()) {
                gsmc = Util.null2String(rds1.getString("COMPANY_NAME"));
                zcdz = Util.null2String(rds1.getString("REGISTERED_ADDRESS"));
                zzjgdm = Util.null2String(rds1.getString("ORGANIZATION_CODE"));
                frdb = Util.null2String(rds1.getString("LEGAL_PERSON"));
                gswz = Util.null2String(rds1.getString("WEBSITE"));
                bgdz = Util.null2String(rds1.getString("ADDRESS"));
                wxzz = Util.null2String(rds1.getString("SATELLITE_ADDRESS"));
                xzgly = Util.null2String(rds1.getString("SATELLITE_MANAGER"));
				/*glfzr =Util.null2String(rds1.getString("MANAGER_PRINCIPAL"));
				fzrdh =Util.null2String(rds1.getString("MANAGER_TEL"));
				ywlxr =Util.null2String(rds1.getString("BUSINESS_LINKMAN"));
				lxrdh =Util.null2String(rds1.getString("BUSINESS_TEL"));
				gscz =Util.null2String(rds1.getString("FAX"));
				Email =Util.null2String(rds1.getString("EMAIL"));
				
				jsfzr =Util.null2String(rds1.getString("TECH_PRINCIPAL"));
				fzrdh1 =Util.null2String(rds1.getString("TECH_TEL"));
				sjkfwq =Util.null2String(rds1.getString("SYSTEM_ADDRESS"));

				gsbj =Util.null2String(rds1.getString("MAIN_BUSINESS"));
				mgs =Util.null2String(rds1.getString("SHAREHOLDING_RATIO"));*/
            }
            //申请许可要素
            //申请许可内容
			/*String sqxknr = Util.null2String(rds.getString("APPLY_CONTENT"));
			if(sqxknr !=""){
				Boolean a=sqxknr.contains("实时行情");
				Boolean b=sqxknr.contains("延时行情");
				if(a){
					sqxknr="0";
				}
				if(b){
					sqxknr="1";
				}
			
		    }*/
            //申请许可用途
            String sqxkyt = Util.null2String(rds.getString("APPLY_PURPOSE"));
            String dnrjzs = "";
            String hlwwzzs = "";
            String sjzs = "";
            String gbdszs = "";
            String qt1 = "";
            if (!sqxkyt.equals("")) {
                boolean a = sqxkyt.contains("电脑软件");
                boolean b = sqxkyt.contains("互联网网站");
                boolean c = sqxkyt.contains("手机");
                boolean d = sqxkyt.contains("广播电视");
                boolean e = sqxkyt.contains("其他");
                if (a) {
                    dnrjzs = "1";
                }
                if (b) {
                    hlwwzzs = "1";
                }
                if (c) {
                    sjzs = "1";
                }
                if (d) {
                    gbdszs = "1";
                }
                if (e) {
                    qt1 = "1";
                }

            }
            //申请许可范围
            String sqxkfw = Util.null2String(rds.getString("APPLY_RANGE"));
            String xjsdjs = "";
            String shcs = "";
            String zgnd = "";
            String jhqs = "";
            String qq = "";
            if (!sqxkfw.equals("")) {
                boolean a = sqxkfw.contains("县级市/地级市");
                boolean b = sqxkfw.contains("省会城市");
                boolean c = sqxkfw.contains("中国内地");
                boolean d = sqxkfw.contains("京沪/全省");
                boolean e = sqxkfw.contains("全球");
                if (a) {
                    xjsdjs = "1";
                }
                if (b) {
                    shcs = "1";
                }
                if (c) {
                    zgnd = "1";
                }
                if (d) {
                    jhqs = "1";
                }
                if (e) {
                    qq = "1";
                }
            }
            //申请许可年限
            //String sqxknxbegin = Util.null2String(rds.getString("APPLY_START_TIME"));
            //String sqxknxend = Util.null2String(rds.getString("APPLY_END_TIME"));


            String sqsj = Util.null2String(rds.getString("APPLY_TIME"));
            //行情接受方式
            String hqjsfs = Util.null2String(rds.getString("RECEIVE_MODE"));
            //String shztkd="";
            String hlw1 = "";
            String zx1 = "";
            if (!hqjsfs.equals("")) {
                //Boolean a=hqjsfs.contains("上海证通宽带");
                boolean b = hqjsfs.contains("互联网");
                boolean c = hqjsfs.contains("专线");
                if (b) {
                    hlw1 = "1";
                }
                if (c) {
                    zx1 = "1";
                }
            }
            //产品或服务描述
            String cphfwmc = Util.null2String(rds.getString("PRODUCT"));
            String mbkh = Util.null2String(rds.getString("CUSTOMER"));

            //String dj = Util.null2String(rds.getString("PRICE"));
            String tcsj = Util.null2String(rds.getString("LAUNCH_TIME"));
            //最终用户接收类别
			/*String zzyhjslb = Util.null2String(rds.getString("RECEIVE_DEVICE"));
			String pc="";
			String wife="";
			String dsjjdh="";
			String other="";
			if(zzyhjslb !=""){
				Boolean a=zzyhjslb.contains("电脑");
				Boolean b=zzyhjslb.contains("无线");
				Boolean c=zzyhjslb.contains("电视机/机顶盒");
				Boolean d=zzyhjslb.contains("其他");
				if(a){
					pc="1";
				}
				if(b){
					wife="1";
				}
				if(c){
					dsjjdh="1";
				}
				if(d){
					other="1";
				}
			}*/
            //传输方式
			/*String csfs = Util.null2String(rds.getString("TRANSMISSION_MODE"));
			String zx="";
			String wx="";
			String jyw="";
			String hlw="";
			String qt="";
			if(csfs !=""){
				Boolean a=csfs.contains("专线");
				Boolean b=csfs.contains("无线网络");
				Boolean c=csfs.contains("局域网");
				Boolean d=csfs.contains("互联网");
				Boolean e=csfs.contains("其他");
				if(a){
					zx="1";
				}
				if(b){
					wx="1";
				}
				if(c){
					jyw="1";
				}
				if(d){
					hlw="1";
				}
				if(e){
					qt="1";
				}
			}*/
            String qtgnhxx = Util.null2String(rds.getString("MEMO"));
            String othrebz = Util.null2String(rds.getString("RECEIVE_DEVICE_OTHER"));
            String qtsrk = Util.null2String(rds.getString("TRANSMISSION_MODE_OTHER"));

            //新增字段
            //申请联系人
            String applyLinkman = Util.null2String(rds.getString("APPLY_LINKMAN"));
            //申请联系人电话
            String applyTel = Util.null2String(rds.getString("APPLY_TEL"));
            //申请联系人邮箱
            String applyEmail = Util.null2String(rds.getString("APPLY_EMAIL"));
            //技术对口人1
            String engineer1 = Util.null2String(rds.getString("ENGINEER1"));
            //技术对口人1电话
            String engineerTel1 = Util.null2String(rds.getString("ENGINEER1_TEL"));
            //技术对口人1邮箱
            String engineerEmail1 = Util.null2String(rds.getString("ENGINEER1_EMAIL"));
            //技术对口人2
            String engineer2 = Util.null2String(rds.getString("ENGINEER2"));
            //技术对口人2电话
            String engineerTel2 = Util.null2String(rds.getString("ENGINEER2_TEL"));
            //技术对口人2邮箱
            String engineerEamil2 = Util.null2String(rds.getString("ENGINEER2_EMAIL"));

            //验证客户是否存在
            RecordSet rs = new RecordSet();
            RecordSet rs1 = new RecordSet();
            String customerid = "";//客户id
            String cusbusinessid = "";//客户业务id
            String businessid = "1";//level1行情許可业务
            String sqlkh = "select * from uf_crm_customerinfo where name='" + gsmc + "'";
            rs.executeSql(sqlkh);
            if (rs.next()) {
                customerid = Util.null2String(rs.getString("id"));
                //验证客户业务是否存在
                String sqlyw = "select * from uf_crm_custbusiness where customer='" + customerid + "' and business='" + businessid + "'";
                rs1.executeSql(sqlyw);
                if (rs1.next()) {
                    cusbusinessid = Util.null2String(rs1.getString("id"));
                }
            }

            //客户业务不存在创建客户业务流程;存在创建工作流;
            if (cusbusinessid.equals("")) {
                customerid = "";//业务不存在时，客户变为空
                try {
                    int requestids = createCustomerBusiness(gsmc, businessid);
                    if (requestids > 0) {
                        lg.writeLog("创建客户业务卡片流程触发工作流成功，流程requestid:" + requestids);
                    }
                } catch (Exception e) {
                    lg.writeLog("创建客户业务卡片流程触发出错：" + e);
                }
            }
            //创建工作流开始
            try {
                int requestid = createWF(syid, gsid, gsmc, zcdz, zzjgdm, frdb,
                        gswz, bgdz, dnrjzs, hlwwzzs, sjzs, gbdszs, qt1, xjsdjs, shcs, zgnd, jhqs, qq,
                        sqsj, hlw1, zx1, cphfwmc, mbkh, tcsj, qtgnhxx, wxzz, xzgly, othrebz, qtsrk, customerid,
                        applyLinkman, applyTel, applyEmail, engineer1, engineerTel1, engineerEmail1, engineer2, engineerTel2, engineerEamil2);
                if (requestid > 0) {
                    SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String createDate = dateFormat_now.format(new Date());
                    String sqlupdate = "update SYNC_WEBSITE_LEVEL1 set STATUS=1,STEP2_TIME=to_date('" + createDate + "','yyyy-mm-dd hh24:mi:ss')  where ID='" + syid + "'";
                    rds.executeSql(sqlupdate);
                    lg.writeLog("level1申请流程触发工作流成功，流程requestid:" + requestid);
                }
            } catch (Exception e) {
                lg.writeLog("level1申请流程触发出错：" + e);
            }
        }
        lg.writeLog("level1申请流程触发结束");
    }

    /**
     * 创建level1申请流程
     */

    public int createWF(String syid, String gsid, String gsmc, String zcdz, String zzjgdm, String frdb,
                        String gswz, String bgdz, String dnrjzs, String hlwwzzs, String sjzs, String gbdszs, String qt1,
                        String xjsdjs, String shcs, String zgnd, String jhqs, String qq,
                        String sqsj, String hlw1, String zx1, String cphfwmc, String mbkh, String tcsj,
                        String qtgnhxx,
                        String wxzz, String xzgly, String othrebz, String qtsrk, String customerid,
                        String applyLinkman, String applyTel, String applyEmail, String engineer1, String engineerTel1,
                        String engineerEmail1, String engineer2, String engineerTel2, String engineerEamil2
    ) {
        String newrequestid = "";
        try {
            String workflowid = "472";
            String lcbt = "LEVEL-1行情许可申请";// 流程标题
            RequestService requestService = new RequestService();
            RequestInfo requestInfo = new RequestInfo();

            requestInfo.setWorkflowid(workflowid);//流程类型id
            requestInfo.setCreatorid("124");//创建人
            requestInfo.setDescription(lcbt);//设置流程标题
            requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
            requestInfo.setIsNextFlow("0");//流转到下一节点

            MainTableInfo mainTableInfo = new MainTableInfo();
            List<Property> fields = new ArrayList<Property>();
            Property field;


            // 主表字段开始,1,syid,gsid,gsmc,zcdz,zzjgdm,frdb,
            if (!syid.equals("")) {
                field = new Property();
                field.setName("syid");
                field.setValue(syid);
                fields.add(field);
            }
            if (!gsid.equals("")) {
                field = new Property();
                field.setName("gsid");
                field.setValue(gsid);
                fields.add(field);
            }
            if (!gsmc.equals("")) {
                field = new Property();
                field.setName("gsmc");
                field.setValue(gsmc);
                fields.add(field);
            }
            if (!zcdz.equals("")) {
                field = new Property();
                field.setName("zcdz");
                field.setValue(zcdz);
                fields.add(field);
            }
            if (!zzjgdm.equals("")) {
                field = new Property();
                field.setName("zzjgdm");
                field.setValue(zzjgdm);
                fields.add(field);
            }
            if (!frdb.equals("")) {
                field = new Property();
                field.setName("frdb");
                field.setValue(frdb);
                fields.add(field);
            }

            //7,gswz,glfzr,fzrdh,ywlxr,lxrdh,gscz,
            if (!gswz.equals("")) {
                field = new Property();
                field.setName("gswz");
                field.setValue(gswz);
                fields.add(field);
            }


            //13,Email,bgdz,jsfzr,fzrdh1,sjkfwq,gsbj
            if (!bgdz.equals("")) {
                field = new Property();
                field.setName("bgdz");
                field.setValue(bgdz);
                fields.add(field);
            }

            //19,mgs,dnzd,ydzd,sqxkfw,sqxknx,sqksksrq,sqsxjsrq
            if (!dnrjzs.equals("")) {
                field = new Property();
                field.setName("dnrjzs");
                field.setValue(dnrjzs);
                fields.add(field);
            }
            if (!hlwwzzs.equals("")) {
                field = new Property();
                field.setName("hlwwzzs");
                field.setValue(hlwwzzs);
                fields.add(field);
            }
            if (!sjzs.equals("")) {
                field = new Property();
                field.setName("sjzs");
                field.setValue(sjzs);
                fields.add(field);
            }
            if (!gbdszs.equals("")) {
                field = new Property();
                field.setName("gbdszs");
                field.setValue(gbdszs);
                fields.add(field);
            }
            if (!qt1.equals("")) {
                field = new Property();
                field.setName("qt1");
                field.setValue(qt1);
                fields.add(field);
            }
            if (!xjsdjs.equals("")) {
                field = new Property();
                field.setName("xjsdjs");
                field.setValue(xjsdjs);
                fields.add(field);
            }
            if (!shcs.equals("")) {
                field = new Property();
                field.setName("shcs");
                field.setValue(shcs);
                fields.add(field);
            }
            if (!zgnd.equals("")) {
                field = new Property();
                field.setName("zgnd");
                field.setValue(zgnd);
                fields.add(field);
            }
            if (!jhqs.equals("")) {
                field = new Property();
                field.setName("jhqs");
                field.setValue(jhqs);
                fields.add(field);
            }
            if (!qq.equals("")) {
                field = new Property();
                field.setName("qq");
                field.setValue(qq);
                fields.add(field);
            }

            //25,sqsj,hqjsfs,cphfwmc,cpljdz,mbkh,dj,
            if (!sqsj.equals("")) {
                field = new Property();
                field.setName("sqsj");
                field.setValue(sqsj);
                fields.add(field);
            }
            if (!hlw1.equals("")) {
                field = new Property();
                field.setName("hlw1");
                field.setValue(hlw1);
                fields.add(field);
            }
            if (!zx1.equals("")) {
                field = new Property();
                field.setName("zx1");
                field.setValue(zx1);
                fields.add(field);
            }
            if (!cphfwmc.equals("")) {
                field = new Property();
                field.setName("cphfwmc");
                field.setValue(cphfwmc);
                fields.add(field);
            }

            if (!mbkh.equals("")) {
                field = new Property();
                field.setName("mbkh");
                field.setValue(mbkh);
                fields.add(field);
            }
            //31  tcsj,pc,mobile,qt2,zx,,wx,jyw,hlw,qt,qtgnhxx,ywbdynr,ywbdrnr
            if (!tcsj.equals("")) {
                field = new Property();
                field.setName("tcsj");
                field.setValue(tcsj);
                fields.add(field);
            }
            if (!qtgnhxx.equals("")) {
                field = new Property();
                field.setName("qtgnhxx");
                field.setValue(qtgnhxx);
                fields.add(field);
            }
            //wxzz, xzgly
            if (!wxzz.equals("")) {
                field = new Property();
                field.setName("wxzz");
                field.setValue(wxzz);
                fields.add(field);
            }
            if (!xzgly.equals("")) {
                field = new Property();
                field.setName("xzgly");
                field.setValue(xzgly);
                fields.add(field);
            }

            if (!othrebz.equals("")) {
                field = new Property();
                field.setName("othrebz");
                field.setValue(othrebz);
                fields.add(field);
            }
            if (!qtsrk.equals("")) {
                field = new Property();
                field.setName("qtsrk");
                field.setValue(qtsrk);
                fields.add(field);
            }

            if (!customerid.equals("")) {
                field = new Property();
                field.setName("customer");
                field.setValue(customerid);
                fields.add(field);
            }

            //新增字段
            if (!applyLinkman.equals("")) {
                field = new Property();
                field.setName("applyLinkman");
                field.setValue(applyLinkman);
                fields.add(field);
            }

            if (!applyTel.equals("")) {
                field = new Property();
                field.setName("applyTel");
                field.setValue(applyTel);
                fields.add(field);
            }

            if (!applyEmail.equals("")) {
                field = new Property();
                field.setName("applyEmail");
                field.setValue(applyEmail);
                fields.add(field);
            }

            if (!engineer1.equals("")) {
                field = new Property();
                field.setName("engineer1");
                field.setValue(engineer1);
                fields.add(field);
            }

            if (!engineerTel1.equals("")) {
                field = new Property();
                field.setName("engineerTel1");
                field.setValue(engineerTel1);
                fields.add(field);
            }

            if (!engineerEmail1.equals("")) {
                field = new Property();
                field.setName("engineerEmail1");
                field.setValue(engineerEmail1);
                fields.add(field);
            }

            if (!engineer2.equals("")) {
                field = new Property();
                field.setName("engineer2");
                field.setValue(engineer2);
                fields.add(field);
            }

            if (!engineerTel2.equals("")) {
                field = new Property();
                field.setName("engineerTel2");
                field.setValue(engineerTel2);
                fields.add(field);
            }

            if (!engineerEamil2.equals("")) {
                field = new Property();
                field.setName("engineerEamil2");
                field.setValue(engineerEamil2);
                fields.add(field);
            }
            //上游附件,根据gsid来查询

            RecordSetDataSource fjss = new RecordSetDataSource("exchangeDB");
            String sqlfj = "select * from SYNC_ATTACH where info_id ='" + syid + "'";
            lg.writeLog("sqlfj:" + sqlfj);
            boolean result = fjss.executeSql(sqlfj);
            lg.writeLog("result:" + result);
            String docids = "";
            while (fjss.next()) {
                String id = Util.null2String(fjss.getString("ID"));//附件id
                String NRBT = Util.null2String(fjss.getString("ATTACH_NAME"));//附件名称
                lg.writeLog("id:" + id);
                lg.writeLog("ATTACH_NAME:" + NRBT);
                int aa = sc.getContent(id, "124", NRBT);
                if (aa != 0) {
                    docids = docids + "," + aa;
                }


            }
            lg.writeLog("docids:" + docids);
            if (docids.length() > 0) {
                docids = docids.substring(1);
            }

            field = new Property();
            field.setName("fj");
            field.setValue(docids);
            fields.add(field);

            Property[] fielder = (Property[]) fields.toArray(new Property[fields.size()]);
            mainTableInfo.setProperty(fielder);
            requestInfo.setMainTableInfo(mainTableInfo);
            newrequestid = requestService.createRequest(requestInfo);
        } catch (Exception e) {
            lg.writeLog("level1申请流程触发出错：" + e);
        }
        return Util.getIntValue(newrequestid, 0);
    }

    /**
     * 创建客户卡片流程
     */
    public int createCustomerBusiness(String name, String business) {
        String nearest = "";
        try {
            String workflowid = "430";    //
            String lcbt = "创建客户业务卡片:" + name;// 流程标题
            RequestService requestService = new RequestService();
            RequestInfo requestInfo = new RequestInfo();
            lg.writeLog("创建客户业务卡片程触发start：");
            requestInfo.setWorkflowid(workflowid);//流程类型id
            requestInfo.setCreatorid("124");//创建人
            requestInfo.setDescription(lcbt);//设置流程标题
            requestInfo.setRequestlevel("0");//0 正常，1重要，2紧急
            requestInfo.setIsNextFlow("0");//保存在发起节点

            SimpleDateFormat dateFormat_now = new SimpleDateFormat("yyyy-MM-dd");
            String createDate = dateFormat_now.format(new Date());

            MainTableInfo mainTableInfo = new MainTableInfo();
            List<Property> fields = new ArrayList<Property>();
            Property field = null;

            field = new Property();
            field.setName("name");
            field.setValue(name);
            fields.add(field);

            field = new Property();
            field.setName("business");
            field.setValue(business);
            fields.add(field);

            field = new Property();
            field.setName("createdate");
            field.setValue(createDate);
            fields.add(field);

            field = new Property();
            field.setName("creator");
            field.setValue("124");
            fields.add(field);

            field = new Property();
            field.setName("dept");
            field.setValue("23");
            fields.add(field);

            fields.add(field);
            Property[] fielder = (Property[]) fields.toArray(new Property[fields.size()]);
            mainTableInfo.setProperty(fielder);
            requestInfo.setMainTableInfo(mainTableInfo);
            nearest = requestService.createRequest(requestInfo);
            lg.writeLog("创建客户业务卡片流程触发end：");
        } catch (Exception e) {
            lg.writeLog("创建客户业务卡片流程触发出错：" + e);
        }
        return Util.getIntValue(nearest, 0);
    }
}
