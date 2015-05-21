package a.ma;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
/**
 * This class hold a pair of dates. assuming start datetime is earlier or equal to end date time
 * It does not enforce start time has to be earlier or equal to end date yet
 * @author ama
 *
 */
public class DatePeriod {
	private Date startDateTime, endDateTime;
	
	public DatePeriod(Date startDateTime, Date endDateTime) {
		Date rightNow = Calendar.getInstance().getTime();
		this.startDateTime = (startDateTime == null ? rightNow : startDateTime);
		this.endDateTime = (endDateTime == null ? rightNow : endDateTime);
	}

	public static DatePeriod parseDatePeriod(String datesWithSepcialFormat) {
		if (datesWithSepcialFormat == null)
			return new DatePeriod(null, null);
		String[] dates = datesWithSepcialFormat.split("->");
		DatePeriod dp = null;
		if (dates != null && dates.length == 2) {
			try {
				dp = new DatePeriod(DateUtil.getDateFromatter().parse(dates[0]), DateUtil.getDateFromatter().parse(dates[1]));
			} catch (ParseException e) {
				return new DatePeriod(null, null);
			}
		}
		return dp;
	}

	public boolean isBetween(Date dt) {
		if (dt.getTime() >= startDateTime.getTime()
				&& dt.getTime() <= endDateTime.getTime())
			return true;
		return false;
	}
	public String toString(){
		return DateUtil.getDateFromatter().format(startDateTime)+"<->"+DateUtil.getDateFromatter().format(endDateTime);
	}
}
