package xyz.smartsniff;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class with methods used across the app.
 *
 * Autor: Daniel Castro García
 * Email: dandev237@gmail.com
 * Fecha: 30/06/2016
 */
public class Utils {

    private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

    /**
     * Given a date, this function returns the date with the format "dd/MM/yyyy HH:mm:ss"
     * @param date  The date to format.
     * @return      The date, formatted.
     */
    public static String formatDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        return formatter.format(date);
    }
}
