package weaver.interfaces.workflow.action;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import oracle.sql.BLOB;

public class SelectContent {
	private Connection conn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	FileManager fm=new FileManager();

	
	public int getContent(String ID,String USERID,String NRBT){
		int docids = 0;
		
		try{
			conn = DBHelper.getConn();
			String sql="select * FROM MMDATA  WHERE ID="+ID;
			
			pstmt = conn.prepareStatement(sql);
			
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				BLOB blob = (oracle.sql.BLOB)(rs.getBlob("CONTENT"));//附件内容
				ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
				BufferedInputStream in = new BufferedInputStream(
						blob.getBinaryStream());
				int c;
				while ((c = in.read()) != -1) {
					bytestream.write(c);
				}
				byte data[] = bytestream.toByteArray();
				
				bytestream.close();
				docids = fm.buildFilelqy(Integer.parseInt(USERID),NRBT,data,87,88,134);// 生成文件
				
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			DBHelper.closeRs(rs);
			DBHelper.closePstmt(pstmt);
			DBHelper.closeConn(conn);
		}
		
		return docids;
	}


}
