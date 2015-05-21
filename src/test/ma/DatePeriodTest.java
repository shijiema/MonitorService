package test.ma;

import static org.junit.Assert.*;

import org.junit.Test;

import a.ma.DatePeriod;

public class DatePeriodTest {

	@Test
	public void testToString() {
		DatePeriod dp = DatePeriod.parseDatePeriod("2015-05-10 12:01:01<->2015-05-14 13:01:01");
		assertTrue("Failed to parse date range from patterned string", "2015-05-10 12:01:01<->2015-05-14 13:01:01".equals(dp.toString()));
	}

}
