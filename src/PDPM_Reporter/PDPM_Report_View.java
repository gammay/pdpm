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

import com.utils.pdpm.PDPM_UI_Plugin;
import com.utils.pdpm.PDPM_Date;
import com.utils.pdpm.PDPM_View_Formatter;
import com.utils.pdpm.PDPM_View_Formatter_Category_Details;
import com.utils.pdpm.PDPM_View_Formatter_Application_Details;
import com.utils.pdpm.PDPM_View_Formatter_Application_Title_Details;

import java.io.*;
import java.util.*;
import java.text.*;

public class PDPM_Report_View
{
    PDPM_UI_Plugin ui = null;

    String rep_file_name;
    String rep_file_full_path;
    String date;

    String patterns[] =
    {
        PDPM_GLOBALS.PDPM_PATTERN_USER,
        PDPM_GLOBALS.PDPM_PATTERN_COMPUTER,
        PDPM_GLOBALS.PDPM_PATTERN_LOGSTART,
        PDPM_GLOBALS.PDPM_PATTERN_LOGEND,
        PDPM_GLOBALS.PDPM_PATTERN_DAYLOGIN,
        PDPM_GLOBALS.PDPM_PATTERN_DAYLOGOUT,
    };

    String pattern_friendly_names[] =
    {
        "User",
        "On Computer",
        null,
        null,
        PDPM_GLOBALS.PDPM_PATTERN_DAYLOGIN_FRIENDLY,
        PDPM_GLOBALS.PDPM_PATTERN_DAYLOGOUT_FRIENDLY,
    };

    String pattern_values[] = null;

    int view_format_types[] =
    {
        PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_TYPE_CATEGORY,
        PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_TYPE_APPLICATIONS,
        PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_TYPE_APPLICATION_DETAILED,
    };
    
    String view_format_names[] =
    {
        PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_NAME_CATEGORY,
        PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_NAME_APPLICATIONS,
        PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_NAME_APPLICATION_DETAILED,
    };

    PDPM_View_Formatter view_formatters[] =
    {
        new PDPM_View_Formatter_Category_Details(),
        new PDPM_View_Formatter_Application_Details(),
        new PDPM_View_Formatter_Application_Title_Details(),
    };

    BufferedReader input = null; 

    int error = 0;
    String error_string = "None";

    public PDPM_Report_View(PDPM_UI_Plugin ui, String arg) throws Exception
    {
        this.ui = ui;

        // arg is report file name or Date
        if(arg.startsWith("rep-"))
        {
            // File name
            rep_file_name = arg;
        }
        else
        {
            throw new Exception("Report File name should start with \"rep-\"");
        }

        // Check if file exists
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

        rep_file_full_path = rep_file_config_path + rep_file_name;

        // Check if file exists
        try 
        { 
            PDPM_DEBUG.PDPM_REPORTER_LOG("File: " + rep_file_full_path);
            FileReader file = new FileReader(rep_file_full_path);
            input = new BufferedReader(file);
            input.close();
        }   
        catch(FileNotFoundException x)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR("File not found [" 
                    + rep_file_full_path + "]");
            PDPM_DEBUG.PDPM_REPORTER_ERR(x);
            error = PDPM_ERROR.PDPM_REPORTER_FILE_NOT_FOUND;
            ui.Error("File" + rep_file_full_path + "not found");
            ui.End(get_build_info());
            return;
        } 

        date = rep_file_name.substring(4, 12);

        // Some validity checks
        if(view_format_types.length != view_formatters.length &&
           view_format_types.length != view_format_names.length)
        {
            throw new Exception("Number of View formats supported should match number of report names and number of formatter implementations");
        }
        
        if(patterns.length != pattern_friendly_names.length)
        {
            throw new Exception("Number of patterns and pattern friendly names should match");
        }
        
        // Initialisations
        pattern_values = new String[patterns.length];
        for(int i = 0; i < pattern_values.length; i++)
        {
            pattern_values[i] = null;
        }
    }

    public boolean start(int view_type)
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG("->PDPM_Report_View.start");

        // Heading
        try
        {
            DateFormat input_df = new SimpleDateFormat("MM_dd_yy");
            input_df.setLenient(false);
            Date d = input_df.parse(date, new ParsePosition(0));
            DateFormat output_df =
                new SimpleDateFormat("EEEEE, MMMMM d yyyy");
            ui.Start(date);
            ui.Heading("Day Report for " + output_df.format(d));
        }
        catch(Exception e)
        {
            ui.Error("Date " + date + "is invalid");
            ui.End(get_build_info());
            return false;
        }

        // Summary
        ui.SummaryStart();

        int day_total_time_seconds = 0;

        // boolean partial_day = false;

        PDPM_Date date_log_start = null;
        PDPM_Date date_log_end = null;

        try
        {
        boolean no_login = false;
        boolean no_logout = false;

        PDPM_Date date_day_login = null;
        PDPM_Date date_day_logout = null;

        PDPM_Date date_standard_day_login = 
            new PDPM_Date(PDPM_GLOBALS.STANDARD_DAY_START);
        PDPM_Date date_standard_day_logout =
            new PDPM_Date(PDPM_GLOBALS.STANDARD_DAY_END);

        this.summary_report();

        for(int pattern_index = 0;
            pattern_index < patterns.length;
            pattern_index++)
        {
          // ui.SummaryRow(patterns[pattern_index], pattern_values[pattern_index]);
          PDPM_DEBUG.PDPM_REPORTER_LOG("loop: " + pattern_index);

          if(patterns[pattern_index].equals(PDPM_GLOBALS.PDPM_PATTERN_LOGSTART))
          {
              PDPM_DEBUG.PDPM_REPORTER_LOG("Log start found");
              PDPM_DEBUG.PDPM_REPORTER_LOG("Value: " + 
                      pattern_values[pattern_index]);
              if(pattern_values[pattern_index] != null)
              {
                  date_log_start = 
                      new PDPM_Date(pattern_values[pattern_index]);
              }
              PDPM_Date standard_log_start = 
                  new PDPM_Date(PDPM_GLOBALS.STANDARD_DAY_LOGSTART);

              /*
              // Check if the logs started at expected time. If not set
              //  the partial day flag
              // Check only the hours and minute part, since the logging may
              //  start a few seconds late
              if((date_log_start.hours_part() != 
                          standard_log_start.hours_part()) &&
                 (date_log_start.minutes_part() != 
                          standard_log_start.minutes_part()))
              {
                  PDPM_DEBUG.PDPM_REPORTER_LOG("Partial Day Flag Set");
                  partial_day = true;
              }
              */
          }

          if(patterns[pattern_index].equals(PDPM_GLOBALS.PDPM_PATTERN_LOGEND))
          {
              PDPM_DEBUG.PDPM_REPORTER_LOG("Log end found");
              PDPM_DEBUG.PDPM_REPORTER_LOG("Value: " + 
                      pattern_values[pattern_index]);

              if(pattern_values[pattern_index] != null)
              {
                  date_log_end = 
                      new PDPM_Date(pattern_values[pattern_index]);
              }
              PDPM_Date standard_log_end =
                  new PDPM_Date(PDPM_GLOBALS.STANDARD_DAY_LOGEND);

              /*
              // Check if the logs end at expected time. If not, set
              //  partial day flag
              // Check only hour and minute part, since the logging may end
              //  a few seconds early
              if((date_log_end.hours_part() != 
                          standard_log_end.hours_part()) &&
                 (date_log_end.minutes_part() != 
                          standard_log_end.minutes_part()))
              {
                  PDPM_DEBUG.PDPM_REPORTER_LOG("Partial Day Flag Set");
                  partial_day = true;
              }
              */
          }

          if(patterns[pattern_index].equals(PDPM_GLOBALS.PDPM_PATTERN_DAYLOGIN))
          {
              PDPM_DEBUG.PDPM_REPORTER_LOG("Day login found");
              PDPM_DEBUG.PDPM_REPORTER_LOG("Value: " + 
                      pattern_values[pattern_index]);
              if(pattern_values[pattern_index] == null)
              {
                  // Not logged in at all
                  PDPM_DEBUG.PDPM_REPORTER_LOG("Day Login is null");
                  no_login = true;
              }
              else
              {
                  date_day_login = 
                      new PDPM_Date(pattern_values[pattern_index]);
              }
          }
          
          if(patterns[pattern_index].equals(
                      PDPM_GLOBALS.PDPM_PATTERN_DAYLOGOUT))
          {
              PDPM_DEBUG.PDPM_REPORTER_LOG("Day logout found");
              if(pattern_values[pattern_index] == null)
              {
                  // Not logged off at all
                  PDPM_DEBUG.PDPM_REPORTER_LOG("Day logout is null");
                  // No login and no logout too, implies full day idle
                  no_logout = true;
              }
              else
              {
                  date_day_logout =
                      new PDPM_Date(pattern_values[pattern_index]);
              }
          }

          if(pattern_friendly_names[pattern_index] != null)
          {
              ui.SummaryRow(pattern_friendly_names[pattern_index],
                      pattern_values[pattern_index]);
          }
        }

        if((no_login == true) && (no_logout == true))
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG("Day Idle");

            // Total working time
            // - is zero
            day_total_time_seconds = 0;

            ui.SummaryRow(PDPM_GLOBALS.PDPM_PATTERN_DAY_TOTAL_FRIENDLY,
                    "00:00:00");

            ui.SummaryEnd();
            ui.ProcessingNoteStart();
            ui.ProcessingNoteRow("No Activity during the day");
            ui.ProcessingNoteEnd();
            ui.End(get_build_info());
            return true;
        }
        else if((no_login == false) && (no_logout == true))
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG("No logout, still working");

            // Total working time
            // - Login: Earlier of actual and standard
            // - Logout: The last log of the day
            /*
            day_total_time_seconds =
              (PDPM_Date.earlier(date_day_login, 
                                 date_standard_day_login)).seconds() -
              (PDPM_Date.later(date_day_logout,
                               date_standard_day_logout)).seconds();
            */
            day_total_time_seconds =
                date_log_end.seconds() - date_day_login.seconds();

            /*
            ui.ProcessingNoteStart();
            ui.ProcessingNoteRow(
                    "No logout. Late night? Dont work so much buddy");
            ui.ProcessingNoteEnd();
            */
        }
        else if((no_login == true) && (no_logout == false))
        {
            // Cant happen. ERROR
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "No login, but logout is present. Should not happen");
        }
        else if((no_login == false) && (no_logout == false))
        {
            // Normal case
            PDPM_DEBUG.PDPM_REPORTER_LOG("Normal day.");

            // Total working time
            // - Login: Earlier of actual and standard
            // - Logout: Later of actual and standard
            day_total_time_seconds = date_day_logout.seconds() -
                                     date_day_login.seconds();
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG("Day total time seconds: " +
                day_total_time_seconds);

        } // try
        catch(Exception e)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR(e);
            ui.Error("Exception while processing: " + e.toString());
            ui.End(get_build_info());
            return false;
        }
        
        ui.SummaryRow(PDPM_GLOBALS.PDPM_PATTERN_DAY_TOTAL_FRIENDLY,
                new PDPM_Date(day_total_time_seconds).format());

        ui.SummaryEnd();

        // All the views that will be reported
        ui.DetailsAll(view_format_names);

        // Details
        for(int num_views = 0;
            num_views < view_format_types.length;
            num_views++)
        {
            /*
            if((view_format_types[num_views] != 
                    PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_TYPE_ALL) &&
               (view_format_types[num_views] != view_type))
            {
                continue;
            }
            */
            
            PDPM_DEBUG.PDPM_REPORTER_LOG("Processing View Type: " +
                    view_format_types[num_views]);
            try
            {
                // Close and open the file to reset the read pointer
                if(input != null)
                {
                    input.close();
                    input = null;
                }

                FileReader file = new FileReader(rep_file_full_path);
                input = new BufferedReader(file);

                if(view_formatters[num_views] == null)
                {
                    continue;
                }

                ui.DetailsStart(view_format_names[num_views]);

                view_formatters[num_views].set_ui_callback(ui);
                view_formatters[num_views].process(input,
                                                   day_total_time_seconds);
            }
            catch(Exception e)
            {
                // Exception could be because of view formatter is not set
                PDPM_DEBUG.PDPM_REPORTER_ERR(
                        "Exception while preparing view format report");
                PDPM_DEBUG.PDPM_REPORTER_ERR(e);
                ui.Error("Exception while preparing view format report");
                ui.End(get_build_info());
                return false;
            }

            // ui.DetailsHeading(view_formatters[num_views].heading);

            /*
            for(int j = 0; j < view_formatters[num_views].num_rows; j++)
            {
                ui.DetailsRow(view_formatters[num_views].columns[j]);
            }
            */

            // ui.DetailsEnd(view_formatters[num_views].processing_note);
        } // END: for

        {
            if(date_log_start != null && date_log_end != null)
            {
                ui.ProcessingNoteStart();

                DateFormat output_df =
                    new SimpleDateFormat("d MMM yy HH:mm:ss");
                ui.ProcessingNoteRow("Report Generated - " +
                        output_df.format(new GregorianCalendar().getTime()));

                ui.ProcessingNoteRow(
                        "Report based on logs available between " + 
                        date_log_start.format() + " and " + 
                        date_log_end.format());

                ui.ProcessingNoteRow("Percentages calculated on the base of \"Day Total Time\"");

                ui.ProcessingNoteEnd();
            }
        }
        
        // ui.EndNote("Percentages calculated on the base of \"Day Total Time\"");

        ui.End(get_build_info());

        return true;
    } // END: start

    private String get_build_info()
    {
        String build_info = "Generated by PDPM Application ";

        try
        {
            FileReader file = 
                new FileReader(PDPM_GLOBALS.PDPM_BUILD_INFO_FILE);
            input = new BufferedReader(file);

            String line = null;

            while(true)
            {
                line = input.readLine();

                if(line == null)
                {
                    break;
                }

                line = line.trim();

                // Ignore blank lines
                if(line.length() == 0)
                {
                    continue;
                }

                // Ignore comment lines
                if(line.charAt(0) == '#')
                {
                    continue;
                }

                // First non-blank, non-comment line should contain build info
                build_info = build_info + line;
                break;
            }
        }
        catch(Exception e)
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG("Build info file: " +
                PDPM_GLOBALS.PDPM_BUILD_INFO_FILE + " not found");
            PDPM_DEBUG.PDPM_REPORTER_ERR(e);
        }

        return build_info;
    }

    private void summary_report()
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG("-> PDPM_Report_View.summary_report");
        
        String line;
        int num_pattern = 0;

        FileReader file = null;
        BufferedReader input = null;

        while(true)
        {
            // Open file
            try
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG("File: " + rep_file_full_path);
                file = null;
                file = new FileReader(rep_file_full_path);
                input = null;
                input = new BufferedReader(file);
            }
            catch(Exception e)
            {
                // No handling. Should succeed as the file exists has already
                //  been done
            }

            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Searching for pattern: " + patterns[num_pattern]);

            while(true)
            {
                try
                {
                    line = input.readLine();

                    if(line == null)
                    {
                        break;
                    }

                    PDPM_DEBUG.PDPM_REPORTER_LOG("Line: " + line);

                    line = line.trim();
                    if(line.length() == 0)
                    {
                        continue;
                    }

                    if(line.charAt(0) == '#')
                    {
                        continue;
                    }

                    int pattern_index = line.indexOf('=');
                    if(pattern_index == -1)
                    {
                        continue;
                    }

                    String pattern = line.substring(0, pattern_index);
                    PDPM_DEBUG.PDPM_REPORTER_LOG(
                            "pattern string: " + pattern);
                    if(pattern.equals(patterns[num_pattern]))
                    {
                        PDPM_DEBUG.PDPM_REPORTER_LOG("Found pattern");

                        // Found pattern
                        // Store value
                        String value = line.substring(pattern_index + 1);

                        // if(value != null)
                        if(value.length() == 0)
                        {
                            PDPM_DEBUG.PDPM_REPORTER_LOG(
                                    "Pattern Value not found");
                        }
                        else
                        {
                            pattern_values[num_pattern] = new String(value);
                        }

                        PDPM_DEBUG.PDPM_REPORTER_LOG("Pattern Value: " +
                                pattern_values[num_pattern]);

                        break;
                    }

                    // PDPM_DEBUG.PDPM_REPORTER_LOG("num_pattern: " + num_pattern);
                    // PDPM_DEBUG.PDPM_REPORTER_LOG("patterns.length: " + patterns.length);
                } // try
                catch(Exception e)
                {
                    PDPM_DEBUG.PDPM_REPORTER_ERR(
                            "Exception during processing. Trying next line");
                    PDPM_DEBUG.PDPM_REPORTER_ERR(e);
                    continue;                
                }
            } // END: while

            num_pattern++;                
            // If all patterns have been found, break
            if(num_pattern >= patterns.length)
            {
                break;
            }
        } // END: while
        
        PDPM_DEBUG.PDPM_REPORTER_LOG("<- PDPM_Report_View.summary_report");
        
        return;
    } // END: summary_report
}
