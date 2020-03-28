package weaver.interfaces.workflow.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import oracle.sql.BLOB;
import weaver.docs.docs.DocManagerNoRequest;
import weaver.docs.docs.ImageFileIdUpdate;
import weaver.file.FileManage;
import weaver.conn.RecordSet;
import weaver.file.multipart.DefaultFileRenamePolicy;
import weaver.file.FileUpload;
import weaver.general.BaseBean;
import weaver.general.StaticObj;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.system.SystemComInfo;
import weaver.interfaces.datasource.DataSource;
//数据迁移--附件--输出流
public class FileManager extends BaseBean {
	private static ImageFileIdUpdate imageFileIdUpdate = new ImageFileIdUpdate();
	private StaticObj staticobj;

	public FileManager() {
		staticobj = StaticObj.getInstance();
	}
	
	public String changeDoc(int userid,int maincategory,int subcategory,int seccategory,String tablename) {
		//userid 文档创建人
		//maincategory 主目录
		//subcategory 分目录
		//seccategory 子目录
		//tablename 中间表的表名
		//利用表单建模搭的中间表，表结构必须至少要有 id(唯一标识) files(blob格式的文档) filename(文件名) docid(文档ID-生成文档后回写用)
		int retDocid = 0;
		String docids = "";
		try {
			DataSource ds = (DataSource) StaticObj.getServiceByFullname(
					("datasource.local"), DataSource.class);
			Connection conn = ds.getConnection();
			Statement stmtdetail = conn.createStatement();
			
			String fileid = "";
			ResultSet rsdetail = stmtdetail
					.executeQuery("select * from "+tablename+" where (docid is null or docid = '') and dbms_lob.getlength(files) > 0");
			while (rsdetail.next()) {
				fileid = Util.null2String(rsdetail.getString("id"));
				BLOB blob = (oracle.sql.BLOB) rsdetail.getBlob("files");
				String fileName = Util.null2String(rsdetail
						.getString("filename"));
				ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
				BufferedInputStream in = new BufferedInputStream(
						blob.getBinaryStream());
				int c;
				while ((c = in.read()) != -1) {
					bytestream.write(c);
				}
				byte data[] = bytestream.toByteArray();
				bytestream.close();
				retDocid = buildFile(userid, fileName, data, fileid,maincategory,subcategory,seccategory,tablename);// 生成文件
				docids = docids + ","+retDocid;
			}
			docids = docids.substring(1);
			rsdetail.close();
			stmtdetail.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			writeLog(e);
		}
		return docids;
	}
	public int buildFilelqy(int userid, String ContextFilename, byte[] data,int maincategory,int subcategory,int seccategory) {
				int retDocid = 0;
				RecordSet rs = new RecordSet();
				OutputStream os = null;
				ZipOutputStream filezipOut = null;
				try {
					SystemComInfo syscominfo = new SystemComInfo();
					String createdir = FileUpload.getCreateDir(syscominfo
							.getFilesystem());
					FileManage.createDir(createdir);
					DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy();
					String refilename = weaver.general.Util.getRandom();
					String filepath = createdir + refilename + ".zip";
					writeLog("filepath：" + filepath);
					java.io.File outfile = new java.io.File(filepath);
					outfile = defpolicy.rename(outfile);
					filezipOut = new ZipOutputStream(new BufferedOutputStream(
							new FileOutputStream(outfile)));
					filezipOut.setMethod(ZipOutputStream.DEFLATED); // 设置压缩方法
					filezipOut.putNextEntry(new ZipEntry(refilename));
					os = filezipOut;
					
					
					
					int fsize = data.length;
					os.write(data, 0, fsize);
					os.flush();
					if (os != null)
						os.close();
					if (filezipOut != null)
						filezipOut.close();
					int imageid = 0;
					String imagefileused = "1";
					String iszip = "1";
					String isencrypt = "1";
					String originalfilename = ContextFilename;// 原始文件名
					long filesize = fsize;
					String contenttype = "application/octet-stream";
					//String contenttype = "pdf";
					char separator = Util.getSeparator();
					imageid = imageFileIdUpdate.getImageFileNewId();
					String para = "" + imageid + separator + originalfilename
							+ separator + contenttype + separator + imagefileused
							+ separator + filepath + separator + iszip + separator
							+ isencrypt + separator + filesize;
					rs.executeProc("ImageFile_Insert", para);
					DocManagerNoRequest docManagerNoRequest = new DocManagerNoRequest();
					// 填补文档信息
					String extname = "";// 扩展名
					String imageFileName = Util.null2String(ContextFilename);
					int tempPos = imageFileName.lastIndexOf(".");
					if (tempPos != -1) {
						extname = imageFileName.substring(tempPos + 1);
					}
					Map dataMap = new HashMap();
					String docsubject = "";
					if (tempPos == -1)
						docsubject = originalfilename;
					
					else
						docsubject = originalfilename.substring(0, tempPos);
					
					dataMap.put("docsubject", docsubject);
					dataMap.put("doccreaterid", "" + userid);
					dataMap.put("docCreaterType", "1");
					dataMap.put("maincategory", "" + maincategory);
					dataMap.put("subcategory", "" + subcategory);
					dataMap.put("seccategory", "" + seccategory);
					dataMap.put("fileids", "" + imageid);
					docManagerNoRequest.UploadDocNoRequest(dataMap);
					retDocid = docManagerNoRequest.getId();// 文档ID
					
					
				} catch (Exception e) {
					writeLog(e);
				} finally {
					try {
						if (os != null)
							os.close();
						if (filezipOut != null)
							filezipOut.close();
					} catch (Exception e) {
					}
					return retDocid;
				}
			}
	public int buildFile(int userid, String ContextFilename, byte[] data,
	String fileid,int maincategory,int subcategory,int seccategory,String tablename) {
		int retDocid = 0;
		RecordSet rs = new RecordSet();
		OutputStream os = null;
		ZipOutputStream filezipOut = null;
		try {
			SystemComInfo syscominfo = new SystemComInfo();
			String createdir = FileUpload.getCreateDir(syscominfo
					.getFilesystem());
			FileManage.createDir(createdir);
			DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy();
			String refilename = weaver.general.Util.getRandom();
			String filepath = createdir + refilename + ".zip";
			writeLog("filepath：" + filepath);
			java.io.File outfile = new java.io.File(filepath);
			outfile = defpolicy.rename(outfile);
			filezipOut = new ZipOutputStream(new BufferedOutputStream(
					new FileOutputStream(outfile)));
			filezipOut.setMethod(ZipOutputStream.DEFLATED); // 设置压缩方法
			filezipOut.putNextEntry(new ZipEntry(refilename));
			os = filezipOut;
			int fsize = data.length;
			os.write(data, 0, fsize);
			os.flush();
			if (os != null)
				os.close();
			if (filezipOut != null)
				filezipOut.close();
			int imageid = 0;
			String imagefileused = "1";
			String iszip = "1";
			String isencrypt = "1";
			String originalfilename = ContextFilename;// 原始文件名
			long filesize = fsize;
			String contenttype = "application/octet-stream";
			char separator = Util.getSeparator();
			imageid = imageFileIdUpdate.getImageFileNewId();
			String para = "" + imageid + separator + originalfilename
					+ separator + contenttype + separator + imagefileused
					+ separator + filepath + separator + iszip + separator
					+ isencrypt + separator + filesize;
			rs.executeProc("ImageFile_Insert", para);
			DocManagerNoRequest docManagerNoRequest = new DocManagerNoRequest();
			// 填补文档信息
			String extname = "";// 扩展名
			String imageFileName = Util.null2String(ContextFilename);
			int tempPos = imageFileName.lastIndexOf(".");
			if (tempPos != -1) {
				extname = imageFileName.substring(tempPos + 1);
			}
			Map dataMap = new HashMap();
			String docsubject = "";
			if (tempPos == -1)
				docsubject = originalfilename;
			else
				docsubject = originalfilename.substring(0, tempPos);
			dataMap.put("docsubject", docsubject);
			dataMap.put("doccreaterid", "" + userid);
			dataMap.put("docCreaterType", "1");
			dataMap.put("maincategory", "" + maincategory);
			dataMap.put("subcategory", "" + subcategory);
			dataMap.put("seccategory", "" + seccategory);
			dataMap.put("fileids", "" + imageid);
			docManagerNoRequest.UploadDocNoRequest(dataMap);
			retDocid = docManagerNoRequest.getId();// 文档ID
			
			
			
			String doccreatedatetime = TimeUtil.getCurrentTimeString();
			String doccreatedate = doccreatedatetime.substring(0, 10);
			String doccreatetime = doccreatedatetime.substring(11);
			rs.executeSql("UPDATE DocDetail SET maincategory=" + maincategory
					+ ",subcategory=" + subcategory + ",seccategory="
					+ seccategory + ",usertype='1',doccreaterid='" + userid
					+ "',ownerid='" + userid
					+ "',doctype='1',docextendname='html',doccreatedate='"
					+ doccreatedate + "',doccreatetime='" + doccreatetime
					+ "',doclastmoddate='" + doccreatedate
					+ "',doclastmodtime='" + doccreatetime + "' WHERE id="
					+ docManagerNoRequest.getId());
			rs.executeSql("INSERT INTO Shareinnerdoc(sourceid,type,content,seclevel,sharelevel,srcfrom,opuser,sharesource) values('"
					+ docManagerNoRequest.getId()
					+ "','1','"
					+ userid
					+ "','10','3','80','" + userid + "','0')");
			rs.executeSql("update "+tablename+" set docid = "
					+ docManagerNoRequest.getId() + " where id = " + fileid);
		} catch (Exception e) {
			writeLog(e);
		} finally {
			try {
				if (os != null)
					os.close();
				if (filezipOut != null)
					filezipOut.close();
			} catch (Exception e) {
			}
			return retDocid;
		}
	}
}
