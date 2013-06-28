/*
 * <one line to give the program's name and a brief idea of what it does.>
 *
 * Copyright (C) 2008 Girish Managoli
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.utils.pdpm;

import com.utils.pdpm.PDPM_GLOBALS;
import com.utils.pdpm.PDPM_ERROR;
import com.utils.pdpm.PDPM_DEBUG;

class PDPM_Date
{
    public static final char DATE_FORMAT_SEPARATOR = ':';

    public static final int NUM_SECONDS_IN_MINUTE = 60;
    public static final int NUM_MINUTES_IN_HOUR   = 60;

    private static int DATE_FORMAT_HOUR_START_INDEX   = 0;
    private static int DATE_FORMAT_HOUR_END_INDEX     = 2;
    private static int DATE_FORMAT_MINUTE_START_INDEX = 3;
    private static int DATE_FORMAT_MINUTE_END_INDEX   = 5;
    private static int DATE_FORMAT_SECOND_START_INDEX = 6;
    private static int DATE_FORMAT_SECOND_END_INDEX   = 8;

    private boolean isAbsolute;

    // Number of seconds since start of day (since 00:00:00)
    private int hours;
    private int minutes;
    private int seconds;

    public PDPM_Date(String formatted_string) throws Exception
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG(
            "[PDPM_Date ctor()] Input formatted_string: " + formatted_string);

        hours = 0;
        minutes = 0;
        seconds = 0;

        try
        {
            hours = 
               new Integer(formatted_string.substring(
                 DATE_FORMAT_HOUR_START_INDEX, DATE_FORMAT_HOUR_END_INDEX));
            minutes = 
               new Integer(formatted_string.substring(
                 DATE_FORMAT_MINUTE_START_INDEX, DATE_FORMAT_MINUTE_END_INDEX));
            seconds = 
               new Integer(formatted_string.substring(
                 DATE_FORMAT_SECOND_START_INDEX, DATE_FORMAT_SECOND_END_INDEX));

            PDPM_DEBUG.PDPM_REPORTER_LOG("[PDPM_Date ctor()] hours: " + hours +
                    " minutes: " + minutes + " seconds: " + seconds);
        }
        catch(Exception e)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR(
               "Date formatting error. Unrecoverable error" + formatted_string);

            PDPM_DEBUG.PDPM_REPORTER_ERR(e);
            throw e;
        }
    }

    public PDPM_Date(int secs)
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG(
            "-> [PDPM_Date ctor()] Input seconds: " + seconds);

        /*
        int remaining_seconds = secs;

        int lhours = remaining_seconds/NUM_SECONDS_IN_HOUR;
        remaining_seconds = remaining_seconds % NUM_SECONDS_IN_HOUR;
        PDPM_DEBUG.PDPM_REPORTER_LOG(
            "lhours: " + lhours + " remaining_seconds: " + remaining_seconds);

        int lmins = remaining_seconds/NUM_SECONDS_IN_MINUTE;
        remaining_seconds = remaining_seconds % NUM_SECONDS_IN_MINUTE;
        PDPM_DEBUG.PDPM_REPORTER_LOG(
            "lmins: " + lmins + " remaining_seconds: " + remaining_seconds);

        hours = lhours;
        minutes = lmins;
        seconds = remaining_seconds;
        */
        hours = 0;
        minutes = 0;
        seconds = secs;

        this.rationalize();

        PDPM_DEBUG.PDPM_REPORTER_LOG("<- [PDPM_Date ctor()]");
    }

    public void add(String formatted_string) throws Exception
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG(
            "PDPM_Date.add() formatted_string: " + formatted_string);

        int l_hours = 0;
        int l_minutes = 0;
        int l_seconds = 0;

        try
        {
            l_hours = 
               new Integer(formatted_string.substring(
                 DATE_FORMAT_HOUR_START_INDEX, DATE_FORMAT_HOUR_END_INDEX));

            l_minutes = 
               new Integer(formatted_string.substring(
                 DATE_FORMAT_MINUTE_START_INDEX, DATE_FORMAT_MINUTE_END_INDEX));

            l_seconds = 
               new Integer(formatted_string.substring(
                 DATE_FORMAT_SECOND_START_INDEX, DATE_FORMAT_SECOND_END_INDEX));

            PDPM_DEBUG.PDPM_REPORTER_LOG("[PDPM_Date.add] hours: " + hours +
                    " minutes: " + minutes + " seconds: " + seconds);
        }
        catch(Exception e)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR(
               "Date formatting error. Unrecoverable error" + formatted_string);

            PDPM_DEBUG.PDPM_REPORTER_ERR(e);
            throw e;
        }

        hours = hours + l_hours;
        minutes = minutes + l_minutes;
        seconds = seconds + l_seconds;
        this.rationalize();
    }

    public void add(int secs)
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG(
            "-> [PDPM_Date add()] Input seconds: " + secs);

        seconds = seconds + secs;

        this.rationalize();

        PDPM_DEBUG.PDPM_REPORTER_LOG("<- [PDPM_Date add()]");
    }

    public void set(String formatted_string) throws Exception
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG(
            "[PDPM_Date set()] Input formatted_string: " + formatted_string);

        hours = 0;
        minutes = 0;
        seconds = 0;

        try
        {
            hours = 
               new Integer(formatted_string.substring(
                 DATE_FORMAT_HOUR_START_INDEX, DATE_FORMAT_HOUR_END_INDEX));
            minutes = 
               new Integer(formatted_string.substring(
                 DATE_FORMAT_MINUTE_START_INDEX, DATE_FORMAT_MINUTE_END_INDEX));
            seconds = 
               new Integer(formatted_string.substring(
                 DATE_FORMAT_SECOND_START_INDEX, DATE_FORMAT_SECOND_END_INDEX));

            PDPM_DEBUG.PDPM_REPORTER_LOG("[PDPM_Date ctor()] hours: " + hours +
                    " minutes: " + minutes + " seconds: " + seconds);
        }
        catch(Exception e)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR(
               "Date formatting error. Unrecoverable error" + formatted_string);

            PDPM_DEBUG.PDPM_REPORTER_ERR(e);
            throw e;
        }
    }

    private void rationalize()
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG("-> PDPM_Date.rationalize()" +
            "hours: " + hours + " minutes: " + minutes + "seconds: " + seconds);

        if(seconds >= NUM_SECONDS_IN_MINUTE)
        {
            int lmins = seconds/NUM_SECONDS_IN_MINUTE;
            int remaining_seconds = seconds % NUM_SECONDS_IN_MINUTE;
            PDPM_DEBUG.PDPM_REPORTER_LOG(
              "lmins: " + lmins + " remaining_seconds: " + remaining_seconds);

            minutes += lmins;
            seconds = remaining_seconds;
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG(
            "hours: " + hours + " minutes: " + minutes + "seconds: " + seconds);

        if(minutes >= NUM_MINUTES_IN_HOUR)
        {
            int remaining_minutes = minutes;

            int lhours = remaining_minutes/NUM_MINUTES_IN_HOUR;
            remaining_minutes = remaining_minutes % NUM_MINUTES_IN_HOUR;
            PDPM_DEBUG.PDPM_REPORTER_LOG(
              "lhours: " + lhours + " remaining_minutes: " + remaining_minutes);

            hours   += lhours;
            minutes = remaining_minutes;
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG("<- PDPM_Date.rationalize()" +
            "hours: " + hours + " minutes: " + minutes + "seconds: " + seconds);

        return;
    }

    public int hours_part()
    {
        return this.hours;
    }

    public int minutes_part()
    {
        return this.minutes;
    }

    public int seconds_part()
    {
        return this.seconds;
    }

    public int seconds()
    {
        int total_seconds = 
               ((hours * NUM_SECONDS_IN_MINUTE * NUM_MINUTES_IN_HOUR) +
                (minutes * NUM_SECONDS_IN_MINUTE) +
                (seconds));

        PDPM_DEBUG.PDPM_REPORTER_LOG("[PDPM_Date.seconds()] total_seconds: " +
                total_seconds);

        return total_seconds;
    }

    public String format()
    {
        String format = "";

        format = append_number(format, hours);
        format = format + DATE_FORMAT_SEPARATOR;

        format = append_number(format, minutes);
        format = format + DATE_FORMAT_SEPARATOR;

        format = append_number(format, seconds);

        return format;
    }

    // Utility function to make the number two-digit
    //  by prefixing zero if required
    private static String append_number(String str, int number)
    {
        if(number < 10)
        {
            str = str + "0" + String.valueOf(number);
        }
        else
        {
            str = str + String.valueOf(number);
        }
        return str;
    }

    static PDPM_Date earlier(PDPM_Date date1, PDPM_Date date2)
    {
        if(date1.seconds() <= date2.seconds())
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "[PDPM_Date.earlier()] Returning date1");
            return date1;
        }
        else
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "[PDPM_Date.earlier()] Returning date2");
            return date2;
        }
    }

    static PDPM_Date later(PDPM_Date date1, PDPM_Date date2)
    {
        if(date1.seconds() >= date2.seconds())
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "[PDPM_Date.later()] Returning date1");
            return date1;
        }
        else
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "[PDPM_Date.later()] Returning date2");
            return date2;
        }
    }
}
