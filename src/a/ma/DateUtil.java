package a.ma;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * this can be put in util package
 * @author ama
 *
 */
public class DateUtil {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static String formatDate(Date dt){
		return sdf.format(dt);
	}
	public static SimpleDateFormat getDateFromatter(){
		return sdf;
	}
}
