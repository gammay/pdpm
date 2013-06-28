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

/*
 * Change Log:
 * 19-Jan-2008: Girish:
 * Added error code.
 */

package com.utils.pdpm;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.*;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.utils.pdpm.PDPM_Application_List;
import com.utils.pdpm.PDPM_ERROR;
import com.utils.pdpm.PDPM_GLOBALS;

class PDPM_Reporter
{
    public static PDPM_Application_List pdpm_applications = null;

    public String error = null;

    public int error_code = 0;

    public static void main(String args[])
    {
        PDPM_Reporter reporter = new PDPM_Reporter();

        PDPM_DEBUG.PDPM_DEBUG_INIT();

        // reporter.Generate("dat-10_19_06.pdp");
        reporter.Generate(args[0]);
    }

    public String get_last_error()
    {
        return error;
    }

    public int get_last_error_code()
    {
        return error_code;
    }

    private String getReportFileName(String dat_file_name)
    {
        String rep_file_name;

        // Check first if input file name contains string "dat-"
        if(dat_file_name.startsWith("dat-"))
        {
            // Valid file name
        }
        else
        {
            // Invalid file name, but do nothing
        }

        // Then get only the date part of the file name
        String date_part = null;
        try
        {
            date_part = dat_file_name.substring(4, 12);
        }
        catch(Exception e)
        {
            // Invalid file name format
        }

        if(date_part == null)
        {
            rep_file_name = "rep-no_date.pdp";
        }
        else
        {
            // Create output file name
            rep_file_name = "rep-";
            rep_file_name += date_part;
            rep_file_name += ".pdp";
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG("Report file name: " + rep_file_name);

        return rep_file_name;
    }

    public String Generate(String data_file_name)
    {
        /* Initialisation */
        pdpm_applications = new PDPM_Application_List();


        /* Open Input File */
        FileReader file = null;
        BufferedReader input = null; 

        String data_file_config_path = 
            (String) PDPM_Config_Reader.get_config_param(
                    PDPM_GLOBALS.PDPM_CONFIG_PARAM_PDPM_FILES_PATH);

        if(data_file_config_path == null)
        {
            data_file_config_path = "";
        }
        else
        {
            if(data_file_config_path.endsWith("/") ||
               data_file_config_path.endsWith("\\"))
            {
            }
            else
            {
                data_file_config_path = data_file_config_path + "/";
            }
        }

        String data_file_full_path = 
            data_file_config_path + data_file_name;

        try 
        { 
            PDPM_DEBUG.PDPM_REPORTER_LOG("Trying to open file: " +
                    data_file_full_path);

            file = new FileReader(data_file_full_path);
            input = new BufferedReader(file);
        }   
        catch(FileNotFoundException x)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR("File not found [" 
                    + data_file_full_path + "]");
            PDPM_DEBUG.PDPM_REPORTER_ERR(x.getMessage());
            error = "File not found [" + data_file_full_path + "]";
            error_code = PDPM_ERROR.PDPM_REPORTER_FILE_NOT_FOUND;
            return null;
        }  

        /* Check if we are generating report for today */
        boolean today_flag = false;
        Calendar cal = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("MM_dd_yy");
        String date_str = df.format(cal.getTime());
        // Form expected dat file name, if its for today
        String today_dat_file_name = "dat-" + date_str + ".pdp";
        if(data_file_name.equals(today_dat_file_name))
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG("Today's report is being generated");
            today_flag = true;
        }

        // Create output file
        PrintWriter out;
        String rep_file_name = getReportFileName(data_file_name);

        String rep_file_config_path = 
            (String) PDPM_Config_Reader.get_config_param(
                    PDPM_GLOBALS.PDPM_CONFIG_PARAM_PDPM_FILES_PATH);

        if(rep_file_config_path == null)
        {
            rep_file_config_path = "";
        }
        else
        {
            if(rep_file_config_path.endsWith("/") ||
               rep_file_config_path.endsWith("\\"))
            {
            }
            else
            {
                rep_file_config_path = rep_file_config_path + "/";
            }
        }

        String rep_file_full_path = 
            rep_file_config_path + rep_file_name;

        try
        {
            // Print a line of text
            out = new PrintWriter(new FileOutputStream(rep_file_full_path));

        }
        // Catches any error conditions
        catch (IOException e)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR("Could not create output file");
            PDPM_DEBUG.PDPM_REPORTER_ERR(e.getMessage());
            error = "Could not create output file" + rep_file_full_path;
            return null;
        }

        // Write Header to output file
        try
        {
            out.println("# PDPM Report File");
            out.println("# This is a generated file, DO NOT edit manually");
            out.println("");
        }
        catch(Exception e)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR("Error writing to output file");
            PDPM_DEBUG.PDPM_REPORTER_ERR(e);
        }

        boolean start_processing = false;
        boolean day_start_processing = false;
        String line = null;

        String log_start_time = null;
        String log_end_time = null;

        String day_login_time = null;
        String day_logout_time = null;

        PDPM_Date now_time = null;

        String prev_app_name = null;

        // Two pass approach
 
        // First pass to find day login and logout times
        PDPM_DEBUG.PDPM_REPORTER_LOG("First pass");

        boolean first_pass_read_next_line = true;

        int missed_seconds = 0;

        while(true)
        {
            try
            {
                if(first_pass_read_next_line)
                {
                    line = input.readLine();
                }
                else
                {
                    first_pass_read_next_line = true;
                }

                if(line == null)
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG("End of input file");
                    break;
                }

                PDPM_DEBUG.PDPM_REPORTER_LOG("Line read: \\" + line + "/");

                line = line.trim();

                // Ignore blank line
                if(line.length() == 0)
                {
                    continue;
                }

                // Ignore comment lines
                if(line.charAt(0) == '#')
                {
                    continue;
                }

                // Parse special comment lines
                if(line.charAt(0) == '!')
                {
                    if(line.contains(PDPM_GLOBALS.PDPM_PATTERN_START))
                    {
                        if(!start_processing)
                        {
                            PDPM_DEBUG.PDPM_REPORTER_LOG("Found START mark");
                            start_processing = true;
                            continue;
                        }
                    }
                    else if(line.contains(PDPM_GLOBALS.PDPM_PATTERN_USER))
                    {
                        PDPM_DEBUG.PDPM_REPORTER_LOG("Found USER mark" + line);
                        // Copy line to output
                        out.println(line);
                    }
                    else if(line.contains(PDPM_GLOBALS.PDPM_PATTERN_COMPUTER))
                    {
                        PDPM_DEBUG.PDPM_REPORTER_LOG("Found COMPUTER mark" + 
                                line);
                        // Copy line to output
                        out.println(line);
                    }
                }

                if(!start_processing)
                {
                    continue;
                }
                /*
                if(!start_processing)
                {
                    // Look for start mark
                    if(line.compareToIgnoreCase("!START") == 0)
                    {
                        PDPM_DEBUG.PDPM_REPORTER_LOG("Found !START mark");
                        start_processing = true;
                        continue;
                    }
                    else
                    {
                        continue;
                    }
                }
                */

                // Get Time
                int log_time_index = 0;
                // Get Application Name
                int appname_index = line.indexOf(',');
                appname_index += 1;
                int apptitle_index = line.indexOf(',', appname_index);
                apptitle_index += 1;
                // Get Application Count
                int appcount_index = line.indexOf(',', apptitle_index);
                appcount_index += 1;

                String log_time = null;
                String app_name =  null;
                String app_count = null;
                try
                {
                    log_time =
                        line.substring(log_time_index, appname_index - 1);
                    app_name = 
                        line.substring(appname_index, apptitle_index - 1);
                    app_count = 
                        line.substring(appcount_index);
                }
                catch(StringIndexOutOfBoundsException oobe)
                {
                    // Exception possible, when the first application the day
                    //  is not started and the line contains only a number
                    //  Just ignore this line and continue with next
                    PDPM_DEBUG.PDPM_REPORTER_LOG(
                            "No application name, skipping this line");
                    continue;
                }

                if(app_name == null)
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG(
                            "No application name, skipping this line");
                    continue;
                }

                PDPM_DEBUG.PDPM_REPORTER_LOG("Application Name: " + app_name);

                // When the logging started 
                // - the first valid log in the log file
                if(log_start_time == null)
                {
                    log_start_time = new String(log_time);
                    now_time = new PDPM_Date(log_start_time);
                    PDPM_DEBUG.PDPM_REPORTER_LOG("now_time created. Seconds: " +
                            now_time.seconds());
                }

                if(now_time != null)
                {
                    PDPM_Date actual_time = new PDPM_Date(log_time);

                    int l_missed_seconds = 
                        actual_time.seconds() - (now_time.seconds() + 1);

                    if(l_missed_seconds == -1)
                    {
                        l_missed_seconds = 0;
                    }

                    if(l_missed_seconds != 0)
                    {
                        PDPM_DEBUG.PDPM_REPORTER_LOG("Expected time: " +
                                now_time.format() + " + 1 second" +
                                " Actual: " + log_time);

                        PDPM_DEBUG.PDPM_REPORTER_LOG("Counter missed: " + 
                                    l_missed_seconds + " seconds");

                        missed_seconds += l_missed_seconds;

                        now_time.set(log_time);
                    }
                }

                // Process Application Count
                Integer app_master_count = null;
                try
                {
                    app_master_count = Integer.parseInt(app_count);
                    // PDPM_DEBUG.PDPM_REPORTER_LOG("Count: " + count);
                }
                catch(NumberFormatException e)
                {
                    // Ignore line
                    PDPM_DEBUG.PDPM_REPORTER_LOG(app_count + " is not a count");
                    continue;
                }

                while(true)
                {
                    // Read next line and check if it contains count or next
                    //  application name
                    if((line = input.readLine()) == null)
                    {
                        break;
                    }

                    PDPM_DEBUG.PDPM_REPORTER_LOG("Line read: " + line);

                    // Is it a count?
                    Integer count = null;
                    try
                    {
                        count = Integer.parseInt(line);
                        app_master_count += count;
                    }
                    catch(NumberFormatException e)
                    {
                        PDPM_DEBUG.PDPM_REPORTER_LOG("Not a count");
                        first_pass_read_next_line = false;
                        break;
                    }
                }

                PDPM_DEBUG.PDPM_REPORTER_LOG("Count: " + app_master_count);

                now_time.add(app_master_count);

                // When did user login for the day
                // - the first valid log which is not "Locked or Idle"
                if(!day_start_processing)
                {
                    // Look for first log of day that is not "Locked or Idle"
                    if(app_name.compareToIgnoreCase(
                           PDPM_GLOBALS.PDPM_LOCKED_APPLICATION_NAME) == 0)
                    {
                        // Still locked, skip
                        continue;
                    }
                    else
                    {
                        PDPM_DEBUG.PDPM_REPORTER_LOG(
                                "First login of the day found");
                        // Not locked, user has logged in now
                        day_start_processing = true;
                        day_login_time = new String(log_time);
                    }
                }

                // When did user logout for the day
                // - the last time "Locked or Idle" series of logs starts
                if((app_name.compareToIgnoreCase(
                       PDPM_GLOBALS.PDPM_LOCKED_APPLICATION_NAME) == 0))
                {
                    if((prev_app_name != null) &&
                       (prev_app_name.compareToIgnoreCase(
                           PDPM_GLOBALS.PDPM_LOCKED_APPLICATION_NAME) != 0))
                    {
                        day_logout_time = log_time;

                        PDPM_DEBUG.PDPM_REPORTER_LOG("Got day_logout_time as:" +
                                day_logout_time + " prev_app_name was: " +
                                prev_app_name);
                    }
                }
                // But if the log starts again with a valid application name
                //  then reset the day_logout_time
                if((app_name.compareToIgnoreCase(
                        PDPM_GLOBALS.PDPM_LOCKED_APPLICATION_NAME) != 0))
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG("Log started with valid app: " 
                            + app_name + " Resetting day_logout_time to null");

                    day_logout_time = null;
                }

                prev_app_name = app_name;
            }
            catch(Exception x)
            { 
                PDPM_DEBUG.PDPM_REPORTER_ERR(
                        "Exception during processing. Trying next line");

                PDPM_DEBUG.PDPM_REPORTER_ERR(x);
                continue;
            } // catch
        } // while

        // When logging ends
        // - the last valid log in the log file
        log_end_time = now_time.format();

        // If the day logout is found to be null and the report is for a
        //  previous day, log end time is the logout time. This happens
        //  if the machine is shutdown at the end of the day.
        if(day_logout_time == null && today_flag == false)
        {
            // Only if login time is also not null, as for an idle day
            //  both login and logout times should show up as null
            if(day_login_time != null)
            {
                day_logout_time = log_end_time;
            }
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG("End of first pass: day_login_time: " +
                day_login_time + " day_logout_time: " + day_logout_time);

        // Close and open file again
        try
        {
            file.close();
            file = null;
            input.close();
            input = null;

            file = new FileReader(data_file_full_path);
            input = new BufferedReader(file);
        }
        catch(Exception e)
        {
            // No handling
        }

        // Second pass for applications count
        boolean read_next_line = true;
        int day_login_seconds = 0;
        int day_logout_seconds = 0;

        try
        {
            if(day_login_time != null)
            {
                day_login_seconds = new PDPM_Date(day_login_time).seconds();
            }

            if(day_logout_time != null)
            {
                day_logout_seconds = new PDPM_Date(day_logout_time).seconds();
            }
        }
        catch(Exception e)
        {
            // Exception when login and logout times are not set
            PDPM_DEBUG.PDPM_REPORTER_ERR(
                    "Exception during time conversion. Resetting times to 0");

            PDPM_DEBUG.PDPM_REPORTER_ERR(e);

            day_login_seconds = 0;
            day_logout_seconds = 0;
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG("Second pass");
        while(true)
        {
            try
            {
                if(read_next_line)
                {
                    line = input.readLine();
                }
                else
                {
                    read_next_line = true;
                }

                if(line == null)
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG("End of input file");
                    break;
                }

                PDPM_DEBUG.PDPM_REPORTER_LOG("Line read: \\" + line + "/");

                line = line.trim();

                // Ignore blank line
                if(line.length() == 0)
                {
                    continue;
                }

                // Ignore comment lines
                if(line.charAt(0) == '#')
                {
                    continue;
                }

                if(!start_processing)
                {
                    // Look for start mark
                    if(line.compareToIgnoreCase("!START") == 0)
                    {
                        PDPM_DEBUG.PDPM_REPORTER_LOG("Found !START mark");
                        start_processing = true;
                        continue;
                    }
                    else
                    {
                        continue;
                    }
                }

                // Get Time
                int log_time_index = 0;
                // Get Application Name
                int appname_index = line.indexOf(',');
                appname_index += 1;
                // Get Application Title
                int apptitle_index = line.indexOf(',', appname_index);
                apptitle_index += 1;
                // Get Application Count
                int appcount_index = line.indexOf(',', apptitle_index);
                appcount_index += 1;

                String log_time = null;
                String app_name =  null;
                String app_title =  null;
                String app_count =  null;
                try
                {
                    log_time =
                        line.substring(log_time_index, appname_index - 1);
                    app_name = 
                        line.substring(appname_index, apptitle_index - 1);
                    app_title = 
                        line.substring(apptitle_index, appcount_index - 1);
                    app_count = 
                        line.substring(appcount_index);
                }
                catch(StringIndexOutOfBoundsException oobe)
                {
                    // Exception possible, when the first application the day
                    //  is not started and the line contains only a number
                    //  Just ignore this line and continue with next
                    PDPM_DEBUG.PDPM_REPORTER_LOG(
                            "No application name, skipping this line");
                    continue;
                }

                int log_time_seconds = new PDPM_Date(log_time).seconds();
                PDPM_DEBUG.PDPM_REPORTER_LOG("Log Time: " + log_time + 
                        " in seconds: " + log_time_seconds);

                // Start counting only after login time is reached
                if(day_login_seconds != 0 &&
                   log_time_seconds < day_login_seconds)
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG(
                            "Day login time not reached. Skipping");
                    continue;
                }

                // If day logout time is reached, end processing
                if(day_logout_seconds != 0 &&
                   log_time_seconds >= day_logout_seconds)
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG(
                            "Day logout time reached. End processing");
                    break;
                }

                if(app_name == null)
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG(
                            "No application name, skipping this line");
                    continue;
                }

                // Process Application Count
                Integer app_master_count = null;
                try
                {
                    app_master_count = Integer.parseInt(app_count);
                    // PDPM_DEBUG.PDPM_REPORTER_LOG("Count: " + count);
                }
                catch(NumberFormatException e)
                {
                    // Ignore line
                    PDPM_DEBUG.PDPM_REPORTER_LOG("Not a count");
                    continue;
                }

                while(true)
                {
                    // Read next line and check if it contains count or next
                    //  application name
                    if((line = input.readLine()) == null)
                    {
                        break;
                    }

                    PDPM_DEBUG.PDPM_REPORTER_LOG("Line read: " + line);

                    // Is it a count?
                    Integer count = null;
                    try
                    {
                        count = Integer.parseInt(line);
                        app_master_count += count;
                    }
                    catch(NumberFormatException e)
                    {
                        PDPM_DEBUG.PDPM_REPORTER_LOG("Not a count");
                        read_next_line = false;
                        break;
                    }
                }

                PDPM_DEBUG.PDPM_REPORTER_LOG("Count: " + app_master_count);

                now_time.add(app_master_count);

                pdpm_applications.insert(app_name, app_title, app_master_count);
            } // try
            catch(Exception x)
            { 
                PDPM_DEBUG.PDPM_REPORTER_ERR(
                        "Exception during processing. Trying next line");

                PDPM_DEBUG.PDPM_REPORTER_ERR(x);
                continue;
            } // catch
        } // while(true)

        pdpm_applications.process();
        // pdpm_applications.debug_print();

        // Write to output file
        try
        {
            // Log start time
            out.print(PDPM_GLOBALS.PDPM_PATTERN_LOGSTART);
            if(log_start_time != null)
            {
                out.println("=" + log_start_time);
            }
            else
            {
                out.println("=");
            }
            // Log end time
            out.print(PDPM_GLOBALS.PDPM_PATTERN_LOGEND);
            if(log_end_time != null)
            {
                out.println("=" + log_end_time);
            }
            else
            {
                out.println("=");
            }
            // Day login time
            out.print(PDPM_GLOBALS.PDPM_PATTERN_DAYLOGIN);
            if(day_login_time != null)
            {
                out.println("=" + day_login_time);
            }
            else
            {
                out.println("=");
            }
            // Day logout time
            out.print(PDPM_GLOBALS.PDPM_PATTERN_DAYLOGOUT);
            if(day_logout_time != null)
            {
                out.println("=" + day_logout_time);
            }
            else
            {
                out.println("=");
            }

            // Application Details
            out.println("");
            out.println("# Format:");
            out.println("# EXE Name,Friendly Name,Application Title,Application Category,Time in seconds");
            pdpm_applications.write_to_file(out);
        }
        catch(Exception e)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR("Error writing to output file");
            PDPM_DEBUG.PDPM_REPORTER_ERR(e);
        }

        /* Close Files */
        try
        {
            input.close();
            out.close();
        }
        catch(Exception e)
        {
            // No handling
        }

        return rep_file_name;
    }
}
