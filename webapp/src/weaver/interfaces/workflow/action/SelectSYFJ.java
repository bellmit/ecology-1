package weaver.interfaces.workflow.action;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import oracle.sql.BLOB;
import weaver.general.BaseBean;
//查询上游附件
public class SelectSYFJ {
	private Connection conn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	FileManager fm=new FileManager();

	
	public int getContent(String gsid,String USERID,String NRBT){
		int docids = 0;
		try{
			BaseBean lg = new BaseBean();
			conn = DBHelper_SYFJ.getConn();
			String sqlfj="select * from SYNC_ATTACH where info_id ='"+gsid+"'";
			lg.writeLog("------------docids111-------------");			
			pstmt = conn.prepareStatement(sqlfj);
			lg.writeLog("------------docids222-------------");	
			
			rs = pstmt.executeQuery();
			lg.writeLog("------------docids333-------------");
			while(rs.next()){
				lg.writeLog("------------docids444-------------");	
				BLOB blob = (oracle.sql.BLOB)(rs.getBlob("CONTENT"));//附件内容
				lg.writeLog("------------docids555-------------");	
				ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
				BufferedInputStream in = new BufferedInputStream(
						blob.getBinaryStream());
				lg.writeLog("------------docids666-------------");
				int c;
				lg.writeLog("------------docids777-------------");
				while ((c = in.read()) != -1) {
					lg.writeLog("------------docids888-------------");
					bytestream.write(c);
				}
				lg.writeLog("------------docids999-------------");
				byte data[] = bytestream.toByteArray();
				lg.writeLog("------------docids10-------------");
				bytestream.close();
				lg.writeLog("------------docids11-------------");
				docids = fm.buildFilelqy(Integer.parseInt(USERID),NRBT,data,87,88,135);// 生成文件
				lg.writeLog("------------docids12-------------");
			}
		} catch(Exception e){
			e.printStackTrace();

		} finally{
			DBHelper_SYFJ.closeRs(rs);
			DBHelper_SYFJ.closePstmt(pstmt);
			DBHelper_SYFJ.closeConn(conn);
		}	
		return docids;
	}


	private void writeLog(String string) {
		// TODO Auto-generated method stub
		
	}


}