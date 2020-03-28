package weaver.interfaces.workflow.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.RequestInfo;

public class DD_ZDYZ extends BaseBean
  implements Action
{
  public String execute(RequestInfo request)
  {
    try
    {
      RecordSet llqqyy = new RecordSet();
      RecordSet lqys = new RecordSet();
      RecordSet ddss = new RecordSet();
      llqqyy.executeSql("select * from uf_ddcx_dt1 where status !=1 and skje !=0 ");

      while (llqqyy.next())
      {
        String iiiddd = Util.null2String(llqqyy.getString("id"));
        String skje = Util.null2String(llqqyy.getString("skje"));
        String DEADLINE = Util.null2String(llqqyy.getString("skqx"));
        Double dddd = Double.valueOf(skje);
        String cccc = String.format("%.2f", new Object[] { dddd });

        UUID uuuuid = UUID.randomUUID();
        String wwwyid = uuuuid.toString();

        String mainid = Util.null2String(llqqyy.getString("mainid"));
        lqys.executeSql("select * from uf_ddcx where id = '" + mainid + "'");
        if (lqys.next()) {
          String CUSTOMER = Util.null2String(lqys.getString("khmc"));
          String name = "";
          ddss.executeSql("select * from CRM_CustomerInfo where id =" + CUSTOMER);
          if (ddss.next()) {
            name = Util.null2String(ddss.getString("name"));
          }
          String CUSTOMER_CODE = Util.null2String(lqys.getString("gsdm"));
          String ORDER_DATE = Util.null2String(lqys.getString("dgsj"));
          String PRODUCT = Util.null2String(lqys.getString("yelx"));
          String gmcp = Util.null2String(lqys.getString("gmcp"));
          if (PRODUCT.equals("0"))
          {
            PRODUCT = "L1授权费用";
          } else if (PRODUCT.equals("1"))
          {
            PRODUCT = "L1授权费用";
          } else if (PRODUCT.equals("2"))
          {
            PRODUCT = "L2授权费用";
          } else if (PRODUCT.equals("3"))
          {
            PRODUCT = "L2授权费用";
          } else if (PRODUCT.equals("4"))
          {
            if (gmcp.contains("实收信息费")) {
              PRODUCT = "用户使用费";
              name = name + ORDER_DATE.substring(0, 7) + " Level2数据报送";
            }
            if (gmcp.contains("DATAFEED金额")) {
              PRODUCT = "DATAFEED金额";
              name = name + ORDER_DATE.substring(0, 7) + " Level2数据报送";
            }
            if (gmcp.contains("VDE金额")) {
              PRODUCT = "VDE金额";
              name = name + ORDER_DATE.substring(0, 7) + " Level2数据报送";
            }
          }
          else if (PRODUCT.equals("5"))
          {
            PRODUCT = "CA服务费";
          } else if (PRODUCT.equals("6"))
          {
            PRODUCT = "股东大会网络投票";
          } else if (PRODUCT.equals("7"))
          {
            PRODUCT = "融资融券业务";
          } else if (PRODUCT.equals("8"))
          {
            PRODUCT = "其他";
          } else if (PRODUCT.equals("9"))
          {
            PRODUCT = "专线业务";
          } else if (PRODUCT.equals("10"))
          {
            PRODUCT = "其他";
          } else if (PRODUCT.equals("11"))
          {
            PRODUCT = "其他";
          } else if (PRODUCT.equals("12"))
          {
            PRODUCT = "互联网行情托管";
          } else if (PRODUCT.equals("13"))
          {
            PRODUCT = "其他";
          } else if (PRODUCT.equals("14"))
          {
            PRODUCT = "其他";
          } else if (PRODUCT.equals("15"))
          {
            PRODUCT = "会员信息服务平台";
          }

          String VOUCHER_TYPE = "0";

          String htbh = Util.null2String(lqys.getString("htbhwb"));

          String ddbh = Util.null2String(lqys.getString("ddbh"));
          String gsid = Util.null2String(lqys.getString("gsid"));
          RecordSetDataSource ss = new RecordSetDataSource("exchangeDB");
          RecordSetDataSource sdd = new RecordSetDataSource("financeTest");

          String sql = "select * from SYNC_COMPANY where ID='" + gsid + "'";
          String OPERATOR = "";
          String OPERATOR_TEL = "";
          String LINKMAN = "";
          String LINKMAN_TEL = "";
          String LINKMAN_MOBILE = "";
          String LINKMAN_EMAIL = "";
          ss.executeSql(sql);
          while (ss.next()) {
            OPERATOR = Util.null2String(ss.getString("OPERATOR"));
            OPERATOR_TEL = Util.null2String(ss.getString("OPERATOR_TEL"));
            LINKMAN = Util.null2String(ss.getString("LINKMAN"));
            LINKMAN_TEL = Util.null2String(ss.getString("LINKMAN_TEL"));
            LINKMAN_MOBILE = Util.null2String(ss.getString("LINKMAN_MOBILE"));
            LINKMAN_EMAIL = Util.null2String(ss.getString("LINKMAN_EMAIL"));
          }

          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          String createDate = sdf.format(new Date());
          String STATUS = "0";

          String sqlaa = "insert into SYNC_FINANCE_VOUCHER (";
          String sqlbb = ") values (";
          if (wwwyid != "") {
            sqlaa = sqlaa + "ID,";
            sqlbb = sqlbb + "'" + wwwyid + "',";
          }
          if (name != "") {
            sqlaa = sqlaa + "TITLE,";
            sqlbb = sqlbb + "'" + name + "',";
          }
          if (CUSTOMER != "") {
            sqlaa = sqlaa + "CUSTOMER,";
            sqlbb = sqlbb + "'" + name + "',";
          }
          if (CUSTOMER_CODE != "") {
            sqlaa = sqlaa + "CUSTOMER_CODE,";
            sqlbb = sqlbb + "'" + CUSTOMER_CODE + "',";
          }
          if (PRODUCT != "") {
            sqlaa = sqlaa + "PRODUCT,";
            sqlbb = sqlbb + "'" + PRODUCT + "',";
          }
          if (cccc != "") {
            sqlaa = sqlaa + "BILL_AMOUNT,";
            sqlbb = sqlbb + "'" + cccc + "',";
          }
          if (VOUCHER_TYPE != "") {
            sqlaa = sqlaa + "VOUCHER_TYPE,";
            sqlbb = sqlbb + "'" + VOUCHER_TYPE + "',";
            if (VOUCHER_TYPE.equals("1")) {
              sqlaa = sqlaa + "CONTRACT_CODE,";
              sqlbb = sqlbb + "'" + htbh + "',";
            }
          }

          if (ddbh != "") {
            sqlaa = sqlaa + "SID,";
            sqlbb = sqlbb + "'" + ddbh + "',";
          }
          if (OPERATOR != "") {
            sqlaa = sqlaa + "OPERATOR,";
            sqlbb = sqlbb + "'" + OPERATOR + "',";
          }
          if (OPERATOR_TEL != "") {
            sqlaa = sqlaa + "OPERATOR_TEL,";
            sqlbb = sqlbb + "'" + OPERATOR_TEL + "',";
          }
          if (LINKMAN != "") {
            sqlaa = sqlaa + "LINKMAN,";
            sqlbb = sqlbb + "'" + LINKMAN + "',";
          }
          if (LINKMAN_TEL != "") {
            sqlaa = sqlaa + "LINKMAN_TEL,";
            sqlbb = sqlbb + "'" + LINKMAN_TEL + "',";
          }
          if (LINKMAN_MOBILE != "") {
            sqlaa = sqlaa + "LINKMAN_MOBILE,";
            sqlbb = sqlbb + "'" + LINKMAN_MOBILE + "',";
          }
          if (LINKMAN_EMAIL != "") {
            sqlaa = sqlaa + "LINKMAN_EMAIL,";
            sqlbb = sqlbb + "'" + LINKMAN_EMAIL + "',";
          }
          sqlaa = sqlaa + "HX_INVOICE,";
          sqlbb = sqlbb + "'0',";
          sqlaa = sqlaa + "HX_RECORD,";
          sqlbb = sqlbb + "'0',";
          if (ORDER_DATE != "") {
            sqlaa = sqlaa + "ORDER_DATE,";
            sqlbb = sqlbb + "'" + ORDER_DATE + "',";
          }
          if (createDate != "") {
            sqlaa = sqlaa + "STEP1_TIME,";
            sqlbb = sqlbb + "'" + createDate + "',";
          }
          if (DEADLINE != "") {
            sqlaa = sqlaa + "DEADLINE,";
            sqlbb = sqlbb + "'" + DEADLINE + "',";
          }
          sqlaa = sqlaa + "STATUS1,STATUS_PRINT,STATUS,JD_STATUS";
          sqlbb = sqlbb + "'" + STATUS + "','" + STATUS + "','" + STATUS + "','" + STATUS + "')";

          sdd.executeSql(sqlaa + sqlbb);
          writeLog("订单已经成功推送到财务，订单流水号为：" + ddbh + "，订单id为：" + wwwyid);
          writeLog(sqlaa + sqlbb);

          lqys.executeSql("update uf_ddcx_dt1 set status = '1',wyid ='" + wwwyid + "' where id =" + iiiddd);
          writeLog("update uf_ddcx_dt1 set status = '1',wyid ='" + wwwyid + "' where id =" + iiiddd);

          lqys.executeSql("update uf_ddcx set status = '2',sh_status = '1' where id =" + mainid);
          writeLog("update uf_ddcx set status = '2',sh_status = '1' where id =" + mainid);
        }
      }
    }
    catch (Exception e)
    {
      writeLog("订单推送到mysql中的订单表sync_finance_voucher出错" + e);
      return "0";
    }
    return "1";
  }
}