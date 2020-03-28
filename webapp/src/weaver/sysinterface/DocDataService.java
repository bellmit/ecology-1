package weaver.sysinterface;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.encoding.Base64;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import weaver.conn.ConnStatement;
import weaver.conn.RecordSet;
import weaver.crm.Maint.CustomerInfoComInfo;
import weaver.docs.DocDetailLog;
import weaver.docs.category.DocTreeDocFieldComInfo;
import weaver.docs.category.MainCategoryComInfo;
import weaver.docs.category.SecCategoryComInfo;
import weaver.docs.category.SubCategoryComInfo;
import weaver.docs.docs.DocComInfo;
import weaver.docs.docs.DocImageManager;
import weaver.docs.docs.DocManager;
import weaver.docs.docs.DocReadTagUtil;
import weaver.docs.webservices.DocAttachment;
import weaver.docs.webservices.DocInfo;
import weaver.file.ImageFileManager;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.share.ShareManager;
import weaver.splitepage.operate.SpopForDoc;
import weaver.systeminfo.SystemEnv;
import weaver.systeminfo.language.LanguageComInfo;

public class DocDataService extends BaseService {
	private HttpServletRequest request;
	private  HttpServletResponse response;
	
	public DocDataService(HttpServletRequest request, HttpServletResponse response){
		this.request = request;
		this.response = response;
	}
	
	public void getDoc(User user) throws Exception{
		int docid = Util.getIntValue(request.getParameter("docid"));
		JSONObject  doc = new JSONObject();
		doc.put("id", docid);
		if(!getRight(docid, user, 1)){
			this.returnValue(doc, response);
			return;
		}
		JSONArray jsonArray =  getDocJsonList(user, 1, 1,docid,"","","");
		if(jsonArray.size()>0){
			doc = (JSONObject)jsonArray.get(0);
		}
		this.returnValue(doc, response);
	}
	
	public void getDocList(User user) throws Exception{
		int pageNo = Util.getIntValue(request.getParameter("pageno"),1);
		int pageSize = Util.getIntValue(request.getParameter("pagesize"),10);
		String maincategory = Util.null2String(request.getParameter("maincategory"));
		String subcategory = Util.null2String(request.getParameter("subcategory"));
		String seccategory = Util.null2String(request.getParameter("seccategory"));
		JSONArray jsonArray = getDocJsonList(user, pageNo, pageSize, 0,maincategory,subcategory,seccategory);
		this.returnValue(jsonArray, response);
	}
	
	public JSONArray getDocJsonList(User user,int page,int size,int docid,String maincategory,String subcategory,String seccategory) throws Exception{
		String title = Util.null2String(request.getParameter("title"));
		int userid = user.getUID();
		JSONArray docs = new JSONArray();
		if (user != null) {
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			ShareManager shareManager = new ShareManager();
			DocComInfo dci = new DocComInfo();
			MainCategoryComInfo mcci = new MainCategoryComInfo();
			SubCategoryComInfo scci = new SubCategoryComInfo();
			SecCategoryComInfo seci = new SecCategoryComInfo();
			DepartmentComInfo dmci = new DepartmentComInfo();
			LanguageComInfo lci = new LanguageComInfo();
			CustomerInfoComInfo cici = new CustomerInfoComInfo();
			ResourceComInfo rci = new ResourceComInfo();

			String sql = "";
			if (rs.getDBType().equals("oracle"))
				sql = " t1.*,t2.sharelevel,t3.doccontent from DocDetail t1,"+shareManager.getShareDetailTableByUser("doc", user)+" t2,DocDetailContent t3 where t1.id = t2.sourceid and t1.id = t3.docid ";
			else
				sql = " t1.*,t2.sharelevel from DocDetail t1,"+shareManager.getShareDetailTableByUser("doc", user)+" t2 where t1.id = t2.sourceid ";
			sql += " and ((docstatus = 7 and (sharelevel>1 or (t1.doccreaterid="+user.getUID()+")) ) or t1.docstatus in ('1','2','5')) ";
			sql += "  and seccategory!=0 and (ishistory is null or ishistory = 0) ";
			if(docid!=0){
				sql += " and t1.id = "+docid;
			}
			if(maincategory.length()>0){
				sql+=" and maincategory in ("+maincategory+")";
			}
			if(subcategory.length()>0){
				sql+=" and subcategory in ("+subcategory+")";	
			}
			if(seccategory.length()>0){
				sql+=" and seccategory in ("+seccategory+")";
			}
			if(title.length()>0){
				sql+=" and docsubject like '%"+title+"%'";
			}
			if(page==1){
					sql += " order by doclastmoddate desc,doclastmodtime desc,id desc";
			}
			int firstResult = 0;
			int endResult = 0;
			int pageNo=page;
			int pageSize=size;
			
			if(page>0&&size>0) {
				if (rs.getDBType().equals("oracle")) {
					firstResult = pageNo * pageSize + 1;
					endResult = (pageNo - 1) * pageSize;
					sql = " select " + sql;
					sql = "select * from ( select row_.*, rownum rownum_ from ( " + sql + " ) row_ where rownum < " + firstResult + ") where rownum_ > " +endResult; 					
				} else {
					if(page>1) {
						//sql = " select top " + size + " * from ( select top  " + size + " * from ( select top " + (page * size) + sql + " ) tbltemp1  order by doclastmoddate asc,doclastmodtime asc,id asc ) tbltemp2  order by doclastmoddate desc,doclastmodtime desc,id desc ";
//						firstResult = (pageSize+1) * (pageNo-1);
//						endResult = pageSize*pageNo;
//						int recordCount=this.getDocCountByUser(user);
//						if (firstResult > recordCount) {
//							firstResult = recordCount;
//							endResult = recordCount - (pageSize * (pageNo - 1));
//						}
						String orderby1="order by doclastmoddate asc,doclastmodtime asc,id asc";
						String orderby2="order by doclastmoddate desc,doclastmodtime desc,id desc";
//						sql = " select top " + endResult + " * from ( select top " + endResult + " * from (  select  top " + firstResult + " " + sql + " " + ") tbltemp1 " + orderby1 + " ) tbltemp2 " + orderby2;
//						sql = "select * from ( "+
//							"　　select ROW_NUMBER() OVER(Order by doclastmoddate desc,doclastmodtime desc,id desc ) AS RowId, "+sql+"  "+
//							" ) as b " +
//							    "  where RowId between "+firstResult+" and "+endResult;
						
						StringBuilder buff = new StringBuilder("SELECT ");
						buff.append("*");
						buff.append(" FROM (SELECT ROW_NUMBER() OVER (ORDER BY ");
						buff.append(" doclastmoddate desc,doclastmodtime desc,id desc ");
						buff.append(") AS RN,PAGE1.* FROM(");
						buff.append("select "+sql);
						buff.append(") PAGE1 ) PAGE2 WHERE RN <= ");
						buff.append(pageNo * pageSize);
						buff.append(" AND RN > ");
						buff.append((pageNo - 1) * pageSize);
						sql = buff.toString();

					} else {
						sql = " select top " + size + sql;
					}
				}
			} else {
				sql = " select " + sql;
			}
			rs.executeSql(sql);
			while (rs.next()) {
				JSONObject doc = new JSONObject();
				
				
				doc.put("id", Util.getIntValue(Util.null2String(rs.getString("id"))));
				doc.put("doctype", Util.getIntValue(Util.null2String(rs.getString("doctype")), 1));

				String docsubject_tmp = Util.null2String(rs.getString("docsubject"));
				docsubject_tmp = docsubject_tmp.replaceAll("\n", "");// TD11607
				doc.put("docsubject", docsubject_tmp);
				doc.put("doccode", Util.null2String(rs.getString("docCode")));

				int docpublishtype = Util.getIntValue(Util.null2String(rs.getString("docpublishtype")), 1);
				doc.put("docpublishtype", docpublishtype);
				String publishable = "";
				if (docpublishtype == 2)
					publishable = SystemEnv.getHtmlLabelName(227, user.getLanguage());
				else if (docpublishtype == 3)
					publishable = SystemEnv.getHtmlLabelName(229, user.getLanguage());
				else
					publishable = SystemEnv.getHtmlLabelName(58, user.getLanguage());
				doc.put("publishable", publishable);

				doc.put("docstatus", Util.getIntValue(Util.null2String(rs.getString("docstatus"))));
				doc.put("docstatusstr", dci.getStatusView(Util.getIntValue(doc.get("id")+""), user));

				doc.put("maincategory", rs.getInt("maincategory"));
				doc.put("maincategorystr", mcci.getMainCategoryname(rs.getInt("maincategory") + ""));

				doc.put("subcategory", rs.getInt("subcategory"));
				doc.put("subcategorystr", scci.getSubCategoryname(rs.getInt("subcategory") + ""));

				doc.put("seccategory", rs.getInt("seccategory"));
				doc.put("seccategoryStr", seci.getSecCategoryname(rs.getInt("seccategory") + ""));

				doc.put("docdepartmentid", rs.getInt("docdepartmentid"));
				doc.put("docdepartmentstr", dmci.getDepartmentname(rs.getInt("docdepartmentid") + ""));

				doc.put("doclangurage", rs.getInt("doclangurage"));
				doc.put("doclanguragestr", lci.getLanguagename(rs.getInt("doclangurage") + ""));


				doc.put("doccreaterid", rs.getInt("doccreaterid"));
				doc.put("doccreatertype", Util.getIntValue(Util.null2String(rs.getString("docCreaterType"))));
				doc.put("doccreatername", Util.getIntValue(Util.null2String(rs.getString("docCreaterType"))) == 1 ? rci.getResourcename(rs.getInt("doccreaterid")+ "") : cici.getCustomerInfoname(rs.getInt("doccreaterid") + ""));
				doc.put("doccreatedate", Util.null2String(rs.getString("doccreatedate")));
				doc.put("doccreatetime", Util.null2String(rs.getString("doccreatetime")));
				

				doc.put("maindoc", rs.getInt("mainDoc"));
				doc.put("maindocname", (rs.getInt("mainDoc") == Util.getIntValue(doc.get("id")+"")) ? SystemEnv.getHtmlLabelName(524, user.getLanguage()) + SystemEnv.getHtmlLabelName(58, user.getLanguage()) : dci.getDocname(rs.getInt("mainDoc") + ""));
				
				doc.put("ownerid", rs.getInt("ownerid"));
				doc.put("ownertype", Util.getIntValue(Util.null2String(rs.getString("ownerType"))));
				doc.put("ownername", Util.getIntValue(Util.null2String(rs.getString("ownerType")))==1?rci.getResourcename(rs.getInt("ownerid") + "") : cici.getCustomerInfoname(rs.getInt("ownerid") + ""));


				doc.put("hrmresid", Util.null2String(rs.getString("hrmresid")));
				doc.put("assetid", Util.null2String(rs.getString("assetid")));
				doc.put("crmid", Util.null2String(rs.getString("hrmresid")));
				doc.put("itemid", Util.null2String(rs.getString("itemid")));
				doc.put("projectid", Util.null2String(rs.getString("projectid")));
				doc.put("financeid", Util.null2String(rs.getString("financeid")));

				
				doc.put("accessorycount", Util.null2String(rs.getInt("accessorycount")));
				boolean isread = false;
				int readcount = 0;
				rs1.executeSql("select sum(readCount) from docreadtag where userid="+userid+" and userType="+user.getLogintype()+" and docid="+Util.getIntValue(Util.null2String(doc.get("id")),0));
				if(rs1.next()){
					readcount  = rs1.getInt(1);
					if(readcount>0){
						isread = true;
					}
				}
				doc.put("isread", isread);
				String excludeMethods = "";
				if(docid>0){
					excludeMethods = ",getDoccontent,getExtDocInfo,getAttachment,getPictures,";
				}else{
					excludeMethods = ",getDoccontent,getPictures,";
				}
				String doccontent = "";
				if(excludeMethods.indexOf(",getDoccontent,")>-1){
					doccontent = Util.toBaseEncoding(rs.getString("doccontent"), user.getLanguage(), "1");
					if (docpublishtype == 2) {
						int tmppos = doccontent.indexOf("!@#$%^&*");
						if (tmppos != -1)
							doccontent = doccontent.substring(tmppos + 8, doccontent.length());
					}
				}
				doc.put("doccontent", doccontent);
				
				if(excludeMethods.indexOf(",getExtDocInfo,")>-1){
					if(Util.getIntValue(doc.get("doctype")+"")==2){
						Map extDocInfo = getExtDocInfo(Util.null2String(doc.get("docid")));
						doc.put("versionid", Util.null2String(rs.getString("versionId")));
						doc.put("imagefileid", Util.null2String(rs.getString("imageFileId")));
					}					
				}
				
				if(excludeMethods.indexOf(",getAttachment,")>-1){
					JSONArray attachments = getAttachment(Util.getIntValue(doc.get("id")+"", 0));
					doc.put("attachments", attachments);
				}
				if(excludeMethods.indexOf(",getPictures,")>-1){
					int picturesSize = 0;
					if(docid>0){
						picturesSize = 1;
					}
					JSONArray pictures = getPictures(Util.getIntValue(doc.get("id")+"", 0),picturesSize);
					doc.put("pictures", pictures);
				}
				docs.add(doc);
			}
		}
		return docs;
	}
	
	public void getDocCount(User user) throws Exception {	
		int maincategory = Util.getIntValue(request.getParameter("maincategory"),0);
		int subcategory = Util.getIntValue(request.getParameter("subcategory"),0);
		int seccategory = Util.getIntValue(request.getParameter("seccategory"),0);
		String title = Util.null2String(request.getParameter("title"));
		JSONObject json = new JSONObject();
		int doccount=0;
		if (user != null) {
			RecordSet rs = new RecordSet();
			ShareManager shareManager = new ShareManager();
			String sql = "";
			if (rs.getDBType().equals("oracle"))
				sql = " from DocDetail t1,"+shareManager.getShareDetailTableByUser("doc", user)+" t2,DocDetailContent t3 where t1.id = t2.sourceid and t1.id = t3.docid ";
			else
				sql = " from DocDetail t1,"+shareManager.getShareDetailTableByUser("doc", user)+" t2 where t1.id = t2.sourceid ";
			sql += " and ((docstatus = 7 and (sharelevel>1 or (t1.doccreaterid="+user.getUID()+")) ) or t1.docstatus in ('1','2','5')) ";
			sql += "  and seccategory!=0 and (ishistory is null or ishistory = 0) ";
			if(maincategory>0){
				sql+=" and maincategory="+maincategory;
			}
			if(subcategory>0){
				sql+=" and subcategory="+subcategory;	
			}
			if(seccategory>0){
				sql+=" and seccategory="+seccategory;
			}
			if(title.length()>0){
				sql+=" and docsubject like '%"+title+"%'";
			}
			sql = " select count(*) as c " + sql;
			
			this.writeLog("getDocCount: sql = " +sql);
			rs.executeSql(sql);
			if(rs.next())
				doccount = rs.getInt("c");
		}
		json.put("doccount", doccount);
		this.returnValue(json, this.response);
	}
	
	public int getDocCountByUser(User user) throws Exception {
		int maincategory = Util.getIntValue(request.getParameter("maincategory"),0);
		int subcategory = Util.getIntValue(request.getParameter("subcategory"),0);
		int seccategory = Util.getIntValue(request.getParameter("seccategory"),0);
		if (user != null) {
			RecordSet rs = new RecordSet();
			ShareManager shareManager = new ShareManager();

			String sql = "";
			if (rs.getDBType().equals("oracle"))
				sql = " from DocDetail t1,"+shareManager.getShareDetailTableByUser("doc", user)+" t2,DocDetailContent t3 where t1.id = t2.sourceid and t1.id = t3.docid ";
			else
				sql = " from DocDetail t1,"+shareManager.getShareDetailTableByUser("doc", user)+" t2 where t1.id = t2.sourceid ";
			sql += " and ((docstatus = 7 and (sharelevel>1 or (t1.doccreaterid="+user.getUID()+")) ) or t1.docstatus in ('1','2','5')) ";
			sql += "  and seccategory!=0 and (ishistory is null or ishistory = 0) ";
			if(maincategory>0){
				sql+=" and maincategory="+maincategory;
			}
			if(subcategory>0){
				sql+=" and subcategory="+subcategory;	
			}
			if(seccategory>0){
				sql+=" and seccategory="+seccategory;
			}
			sql = " select count(*) as c " + sql;
			rs.executeSql(sql);
			if(rs.next())
			return rs.getInt("c");
		}
		return 0;
	}
	
	private Map getExtDocInfo(String docid) throws Exception {
		Map result = new HashMap();
		RecordSet rs = new RecordSet();
		String sql1 = "";
		sql1 = "select * from DocImageFile where docid=" + docid + " and (isextfile <> '1' or isextfile is null) order by versionId desc";
		rs.executeSql(sql1);
		rs.next();
		int versionId = Util.getIntValue(rs.getString("versionId"), 0);
		int imageFileId = Util.getIntValue(rs.getString("imagefileid"));
		if (versionId == 0) {
			rs.executeSql("select * from DocImageFile where docid=" + docid + " order by versionId desc");
			if (rs.next()) {
				versionId = Util.getIntValue(rs.getString("versionId"), 0);
				imageFileId = Util.getIntValue(rs.getString("imagefileid"));
			}
		}
		result.put("versionId", versionId + "");
		result.put("imageFileId", imageFileId + "");
		return result;
	}
	
	private boolean getRight(int docid, User user, int type) throws Exception {
		RecordSet rs = new RecordSet();
		boolean hasRight = false;
		DocManager docManager = new DocManager();
		String docStatus = "";
		int isHistory = 0;
		int secCategory = 0;
		String docPublishType = "";// 文档发布类型 1:正常(不发布) 2:新闻 3:标题新闻

		docManager.resetParameter();
		docManager.setId(docid);
		docManager.getDocInfoById();

		docStatus = docManager.getDocstatus();
		isHistory = docManager.getIsHistory();
		secCategory = docManager.getSeccategory();
		docPublishType = docManager.getDocpublishtype();

		if (docPublishType != null && (docPublishType.equals("2") || docPublishType.equals("3"))) {
			String newsClause = "";
			String sqlDocExist = " select 1 from DocDetail where id=" + docid + " ";
			String sqlNewsClauseOr = "";
			boolean hasOuterNews = false;

			rs.executeSql("select newsClause from DocFrontPage where publishType='0'");
			while (rs.next()) {
				hasOuterNews = true;
				newsClause = Util.null2String(rs.getString("newsClause"));
				if (newsClause.equals("")) {
					newsClause = " 1=1 ";
				}
				if (!newsClause.trim().equals("")) {
					// sqlDocExist+=" and "+newsClause;
					sqlNewsClauseOr += " or (" + newsClause + ")";
				}
			}

			if (!sqlNewsClauseOr.equals("")) {
				sqlNewsClauseOr = sqlNewsClauseOr.substring(sqlNewsClauseOr.indexOf("("));
				sqlDocExist += " and (" + sqlNewsClauseOr + ") ";
			}
			// System.out.print(sqlDocExist);
			if (hasOuterNews) {
				rs.executeSql(sqlDocExist);
				if (rs.next()) {
					hasRight = true;
				}
			}

		}
		if (user == null) {
			return false;
		}
		String userId = "" + user.getUID();
		String loginType = user.getLogintype();
		String userSeclevel = user.getSeclevel();
		String userType = "" + user.getType();
		String userDepartment = "" + user.getUserDepartment();
		String userSubComany = "" + user.getUserSubCompany1();

		String userInfo = loginType + "_" + userId + "_" + userSeclevel + "_" + userType + "_" + userDepartment + "_" + userSubComany;

		ArrayList PdocList = null;

		SpopForDoc spopForDoc = new SpopForDoc();
		PdocList = spopForDoc.getDocOpratePopedom("" + docid, userInfo);

		SecCategoryComInfo secCategoryComInfo = new SecCategoryComInfo();

		// 0:查看
		boolean canReader = false;
		// 1:编辑
		boolean canEdit = false;
		// 3:编辑
		boolean canDel = false;
		
		if (((String) PdocList.get(0)).equals("true")) {
			canReader = true;
		}
		if (((String) PdocList.get(1)).equals("true")) {
			canEdit = true;
		}
		if (((String) PdocList.get(2)).equals("true")) {
			canDel = true;
		}
		
		if (canReader
				&& ((canEdit && !docStatus.equals("8")) || (docStatus.equals("7") && isHistory == 1 && secCategoryComInfo.isReaderCanViewHistoryEdition(secCategory)) || docStatus.equals("1") || docStatus.equals("2")
						|| docStatus.equals("3") || docStatus.equals("9") || docStatus.equals("0") || docStatus.equals("5"))) {
			canReader = true;
		} else {
			canReader = false;
		}

		if (isHistory == 1) {
			if (secCategoryComInfo.isReaderCanViewHistoryEdition(secCategory)) {
				if (canReader && !canEdit)
					canReader = true;
			} else {
				if (canReader && !canEdit)
					canReader = false;
			}
		}

		if (canEdit && ((docStatus.equals("3") || docStatus.equals("5") || docStatus.equals("6")) || isHistory == 1)) {
			canEdit = false;
			canReader = true;
		}

		if (canEdit && (docStatus.equals("0") || docStatus.equals("1") || docStatus.equals("2") || docStatus.equals("7")) && (isHistory != 1))
			canEdit = true;
		else
			canEdit = false;

		if (type == 1)
			return canReader;
		if (type == 2)
			return hasRight;
		if (type == 3)
			return canDel;
		return canEdit;
	}
	
	private JSONArray getAttachment(int docid) throws Exception {
		JSONArray jsonArray = new JSONArray();
		ImageFileManager imageFileManager=new ImageFileManager();
		DocImageManager dim = new DocImageManager();
		dim.resetParameter();
		dim.setDocid(docid);
		dim.selectDocImageInfo2();
		while (dim.next()) {
			int imagefileid = Util.getIntValue(dim.getImagefileid());
			if (imagefileid < 1) {
				continue;
			}
			JSONObject json = new JSONObject();
			json.put("imagefilename",dim.getImagefilename());
			json.put("Imagefiledesc",dim.getImagefiledesc());
			json.put("docimagefileId",dim.getImagefileid());
			
//			int byteread;
//			byte data[] = new byte[1024];
//			InputStream imagefile = null;
//
//			imageFileManager.getImageFileInfoById(imagefileid);
//			imagefile=imageFileManager.getInputStream();		
//			if (imagefile!=null) {
//				String filecontent = "";
//				ByteArrayOutputStream out = null;
//				try {
//					out = new ByteArrayOutputStream();
//					while ((byteread = imagefile.read(data)) != -1) {
//						out.write(data, 0, byteread);
//						out.flush();
//					}
//					filecontent = Base64.encode(out.toByteArray());
//				} catch (Exception e) {
//				} finally {
//					if (imagefile != null)
//						imagefile.close();
//					if (out != null)
//						out.flush();
//					if (out != null)
//						out.close();
//				}
//				json.put("filecontent", filecontent);
//			}
			jsonArray.add(json);
		}
		return jsonArray;
	}

	private JSONArray getPictures(int docid,int size) throws Exception {
		JSONArray jsonArray = new JSONArray();
		DocImageManager dim = new DocImageManager();
		dim.resetParameter();
		dim.setDocid(docid);
		dim.selectDocPictures();
		int count=0;
		while (dim.next()) {
			if(size==count&&size!=0){
				break;
			}
			count++;
			int imagefileid = Util.getIntValue(dim.getImagefileid());
			if (imagefileid < 1) {
				continue;
			}
			
			JSONObject json = new JSONObject();
			json.put("imagefilename",dim.getImagefilename());
			json.put("Imagefiledesc",dim.getImagefiledesc());
			json.put("docimagefileId",dim.getImagefileid());

//			int byteread;
//			byte data[] = new byte[1024];
//			InputStream imagefile = null;
//			ConnStatement statement = new ConnStatement();
//			
//			try{
//				String sql = "select imagefilename,filerealpath,iszip,isencrypt,imagefiletype,imagefile,isaesencrypt,aescode from ImageFile where imagefileid = " + imagefileid;
//				boolean isoracle = (statement.getDBType()).equals("oracle");
//
//				statement.setStatementSql(sql);
//				statement.executeQuery();
//				if (statement.next()) {
//					
//			        String filerealpath = Util.null2String(statement.getString("filerealpath"));
//					ZipInputStream zin = null;
//					if (filerealpath.equals("")) { // 旧的文件放在数据库中的方式
//						if (isoracle)
//							imagefile = new BufferedInputStream(statement.getBlobBinary("imagefile"));
//						else
//							imagefile = new BufferedInputStream(statement.getBinaryStream("imagefile"));
//					} else {
//						File thefile = new File(filerealpath);
//						imagefile = new BufferedInputStream(new FileInputStream(thefile));
//					}
//
//					String filecontent = "";
//					ByteArrayOutputStream out = null;
//					try {
//						out = new ByteArrayOutputStream();
//						while ((byteread = imagefile.read(data)) != -1) {
//							out.write(data, 0, byteread);
//							out.flush();
//						}
//						filecontent = Base64.encode(out.toByteArray());
//					} catch (Exception e) {
//					} finally {
//						if (imagefile != null)
//							imagefile.close();
//						if (zin != null)
//							zin.close();
//						if (out != null)
//							out.flush();
//						if (out != null)
//							out.close();
//					}
//
//					json.put("filecontent", filecontent);
//				}
				jsonArray.add(json);
//			}catch(Exception e){
//				
//			}finally{
//				try{
//					statement.close();
//				}catch(Exception e){
//				}
//			}
		}
		return jsonArray;
	}
	
	public void addDocReadTag(User user){
		String docId = Util.getIntValue(request.getParameter("docid"))+"";
		String userId = user.getUID()+"";
		String userLoginType = user.getLogintype()+"";
		String clientIP = Util.getIpAddr(request);
		//安全性检查
    	if(docId==null||docId.equals("")
    	 ||userId==null||userId.equals("")
    	 ||userLoginType==null||userLoginType.equals("")){
    		return ;
    	}
    	//返回值 ""
        try {
            String docCreaterId="";
            String docUserType="";
            String docSubject = "";
            RecordSet rs =new RecordSet();
            rs.executeSql("select docCreaterId,userType,docsubject from docdetail where id="+docId);
            if(rs.next()){
            	docCreaterId=rs.getString("docCreaterId");
            	docUserType=rs.getString("userType");
            	
            	docSubject = rs.getString("docsubject");
            }
            
            if( !userId.equals(docCreaterId) || !userLoginType.equals(docUserType) ) {
            	char flag=Util.getSeparator() ;            	
                rs.executeProc("docReadTag_AddByUser",""+docId+flag+userId+flag+userLoginType); 
                //TD.6349 下载流程文档附件，增加文档日志
                DocDetailLog ddl = new DocDetailLog();
                ddl.resetParameter();
                ddl.setDocId(Util.getIntValue(docId));
                ddl.setDocSubject(docSubject);
                ddl.setOperateType("0");
                ddl.setOperateUserid(Util.getIntValue(userId));
                ddl.setUsertype(userLoginType);
                ddl.setClientAddress(clientIP);
                ddl.setDocCreater(Util.getIntValue(docCreaterId));
                ddl.setDocLogInfo();
            }
            return  ;
        } catch (Exception e) {
            return ;
        }
		
	}
	
}
