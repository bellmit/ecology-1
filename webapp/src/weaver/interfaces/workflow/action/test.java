package weaver.interfaces.workflow.action;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class test {

	public static void main(String[] args) throws ParseException {
     	String s="2016-2-20";
		//String s="";
		DateFormat aa= new SimpleDateFormat("yyyy-MM-dd");
		Date date = aa.parse(s);
		Calendar cl = Calendar.getInstance();
		cl.setTime(date);
		cl.add(Calendar.YEAR, 1);
		String nian=String.valueOf(cl.get(Calendar.YEAR));
		String yue=String.valueOf(cl.get(Calendar.MONTH)+1);
		String ri=String.valueOf(cl.get(Calendar.DAY_OF_MONTH));
		String d13 = nian + "-" + yue + "-" + ri;
        System.out.println("流程测试:"+d13);
	}

}
