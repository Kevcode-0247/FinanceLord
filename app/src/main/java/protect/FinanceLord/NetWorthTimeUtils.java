package protect.FinanceLord;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class NetWorthTimeUtils {

    public Date setTime(int year, int month, int day){
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, day);

        // 同样的，这里我们需要吧时分秒清零，因为我们需要定位到某一天的起始日期
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();
        return date;
    }

    public static String getStringFromDate(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String dateStr = dateFormat.format(date);
        return dateStr;
    }

    public static Date parseDateString(String dateStr, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = dateFormat.parse(dateStr);
        return date;
    }
}
