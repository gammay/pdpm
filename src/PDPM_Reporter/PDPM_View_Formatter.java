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

import com.utils.pdpm.PDPM_Date;
import com.utils.pdpm.PDPM_UI_Plugin;

import java.io.*;
import java.util.*;
import java.text.*;

abstract class PDPM_View_Formatter
{
    public PDPM_Report_Types.PDPM_Details_List heading;

    public PDPM_Report_Types.PDPM_Details_List columns[] = null;

    public int num_rows = 0;

    public int day_total_time_seconds = 0;

    public String processing_note = null;

    protected PDPM_UI_Plugin ui = null;

    final void set_ui_callback(PDPM_UI_Plugin ui)
    {
        this.ui = ui;
    }
    
    public abstract boolean process(BufferedReader input_file,
                                    int day_total_time_seconds);

    final protected String get_field(String line, int num_field)
    {
        // Get the index to required field
        int field_index = -1;

        if(num_field == 1)
        {
            // Get index to the next field as well to extract the field value
            int next_field_index = line.indexOf(',', 0);
            // In case if we reached end of the string
            if(next_field_index == -1)
            {
                next_field_index = line.length();
            }

            return line.substring(0, next_field_index);
        }
        else
        {
            for(int field = 0; field < num_field - 1; field++)
            {
                field_index = line.indexOf(',', field_index + 1);
            }

            if(field_index == -1)
            {
                // Error, field is not found.
                return null;
            }

            // Get index to the next field as well to extract the field value
            int next_field_index = line.indexOf(',', field_index + 1);
            // In case if we reached end of the string
            if(next_field_index == -1)
            {
                next_field_index = line.length();
            }

            return line.substring(field_index + 1, next_field_index);
        }
    }
}

class PDPM_View_Formatter_Category_Details extends PDPM_View_Formatter
{
    public boolean process(BufferedReader input_file,
                           int day_total_time_seconds)
    {
        final int NUM_COLUMNS = 3;

        final int COL_CATEGORY   = 0;
        final int COL_TIME       = 1;
        final int COL_PERCENTAGE = 2;

        this.day_total_time_seconds = day_total_time_seconds;

        PDPM_DEBUG.PDPM_REPORTER_LOG(
                "-> PDPM_View_Formatter_Category_Details.process()");
        
        num_rows = 0;

        heading = new PDPM_Report_Types.PDPM_Details_List(NUM_COLUMNS);
        heading.set(COL_CATEGORY, "Category");
        heading.set(COL_TIME, "Time (HH:MM:SS)");
        heading.set(COL_PERCENTAGE, "%");

        columns =
            new PDPM_Report_Types.PDPM_Details_List[
                                       PDPM_GLOBALS.PDPM_MAX_APPLICATIONS];

        String line;

        while(true)
        {
            try
            {
                
            line = input_file.readLine();

            if(line == null)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG("End of file");
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

            String line_category =
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_CATEGORY);
            String line_count = 
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_TIME);
            String application_name = 
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_FRIENDLY_NAME);
            String application_exe = 
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_APPL_NAME);

            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Category on this line: " + line_category);
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Count on this line: " + line_count);

            if(line_category == null || line_count == null)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Category or Count not found on this line. Skipping to next line");
                continue;
            }
            
            boolean found = false;
            int n;
            // Add to List
            // If it is already in the list, update the count
            for(n = 0; n < columns.length; n++)
            {
                if(columns[n] == null)
                {
                    break;
                }
                
                if((columns[n].get(COL_CATEGORY)).equals(line_category))
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG("Found Category in List");

                    found = true;
                    
                    // Found, update count
                    try
                    {
                        Integer old_count = new Integer(columns[n].get(COL_TIME));
                        Integer new_count = new Integer(line_count);
                        new_count = new_count + old_count;
                        PDPM_DEBUG.PDPM_REPORTER_LOG(
                                "New count: " + new_count.toString());
                        columns[n].set(COL_TIME, new_count.toString());
                    }
                    catch(NumberFormatException nfe)
                    {
                        PDPM_DEBUG.PDPM_REPORTER_ERR(
                                "Time Count is not a valid number"); 
                        PDPM_DEBUG.PDPM_REPORTER_ERR(nfe);
                        continue;
                    }
                    /*
                    // If the category is Unknown,
                    //  append application or EXE name
                    if((columns[n].get(COL_CATEGORY)).equals(
                                PDPM_GLOBALS.PDPM_LOCKED_CATEGORY_UNKNOWN))
                    {
                        String append_name = null;
                        if(application_name != null)
                        {
                            append_name = application_name;
                        }
                        else
                        {
                            append_name = application_exe;
                        }

                        if(append_name != null)
                        {
                            // First check if this name is already appended
                            if(!columns[n].get(COL_CATEGORY).contains(
                                        append_name))
                            {
                                // Append name
                                String new_category_name =
                                    columns[n].get(COL_CATEGORY) +
                                    " (" + append_name + ")";

                                columns[n].set(
                                        COL_CATEGORY, new_category_name);
                            }
                        }
                    }
                    */
                }
            }
            // Else create a new entry in the list
            if(!found)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG("Adding new category to the list");
                columns[n] = 
                    new PDPM_Report_Types.PDPM_Details_List(NUM_COLUMNS);
                columns[n].set(COL_CATEGORY, line_category);
                columns[n].set(COL_TIME, line_count);
                num_rows++;

                /*
                // If the category is Unknown,
                //  append application or EXE name
                if((columns[n].get(COL_CATEGORY)).equals(
                            PDPM_GLOBALS.PDPM_LOCKED_CATEGORY_UNKNOWN))
                {
                    String append_name = null;
                    if(application_name != null)
                    {
                        append_name = application_name;
                    }
                    else
                    {
                        append_name = application_exe;
                    }

                    if(append_name != null)
                    {
                        // First check if this name is already appended
                        if(!columns[n].get(COL_CATEGORY).contains(
                                    append_name))
                        {
                            // Append name
                            String new_category_name =
                                columns[n].get(COL_CATEGORY) +
                                " (" + append_name + ")";

                            columns[n].set(
                                    COL_CATEGORY, new_category_name);
                        }
                    }
                }
                */
            }

            } // try
            catch(Exception e)
            {
                PDPM_DEBUG.PDPM_REPORTER_ERR(
                        "Exception while processing. Skipping to next line.");
                
                PDPM_DEBUG.PDPM_REPORTER_ERR(e);
                
                continue;
            }
        } // END: while
        
        // Calculate Percentages
        int total_time = 0;
        for(int n = 0; n < num_rows; n++)
        {
            total_time = total_time + new Integer(columns[n].get(COL_TIME));
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG("Counted Total Time: " +
                total_time);

        PDPM_DEBUG.PDPM_REPORTER_LOG("Total Time: " + day_total_time_seconds);

        int difference = (total_time > day_total_time_seconds) ?
                         (total_time - day_total_time_seconds) :
                         (day_total_time_seconds - total_time);

        if(difference != 0)
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG("Adding difference " + difference);
            columns[num_rows] = 
                new PDPM_Report_Types.PDPM_Details_List(NUM_COLUMNS);
            columns[num_rows].set(COL_CATEGORY, "Unaccounted");
            columns[num_rows].set(COL_TIME, new String("" + difference));
            num_rows++;
        }

        // int index_locked_or_idle = -1;
        float percentage_totals = 0;
        
        for(int n = 0; n < num_rows; n++)
        {
            // Dont calculate percentage for Locked or Idle now, do it at the
            //  end by filling up remaing percentage value;
            /*
            if(index_locked_or_idle == -1)
            {
                if(columns[n].get(COL_CATEGORY).equals(
                            PDPM_GLOBALS.PDPM_LOCKED_APPLICATION_NAME))
                {
                    index_locked_or_idle = n;
                    continue;
                }
            }
            */

            float percentage =
                (100 * (new Float(columns[n].get(COL_TIME))))/
                                                   day_total_time_seconds;

            PDPM_DEBUG.PDPM_REPORTER_LOG("Percentage for: " +
                    columns[n].get(COL_CATEGORY) + " Time " +
                    columns[n].get(COL_TIME) + " is... ");

            if(percentage < 1)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG("      < 1");
                columns[n].set(COL_PERCENTAGE, "(Small)");
            }
            else
            {
                // String str_percentage = "" + percentage;
                Formatter formatter = new Formatter();
                formatter.format("%2.2f", new Float(percentage));
                String str_percentage = formatter.toString();

                PDPM_DEBUG.PDPM_REPORTER_LOG("      " + str_percentage);

                columns[n].set(
                        // COL_PERCENTAGE, new Float(percentage).toString());
                        COL_PERCENTAGE, str_percentage);
            }

            percentage_totals = percentage_totals + percentage;
        }

        /*
        if(index_locked_or_idle != -1)
        {
            float percentage = 100 - percentage_totals;

            columns[index_locked_or_idle].set(
                    // COL_PERCENTAGE, new Integer(percentage).toString());
                    COL_PERCENTAGE, new Float(percentage).toString());
        }
        */

        PDPM_DEBUG.PDPM_REPORTER_LOG("percentage_totals: " + percentage_totals);

        if(percentage_totals <= 99.99)
        {
            Formatter formatter = new Formatter();
            formatter.format("%2.2f", 
                    new Float((float) (100 - percentage_totals)));
            String str_percentage = formatter.toString();

            processing_note = 
                new String("Round-off error of about " + 
                        str_percentage + "%");

            PDPM_DEBUG.PDPM_REPORTER_LOG(processing_note);
        }

        // Sort
        PDPM_Report_Types.PDPM_Details_List.sort(columns, 
                COL_PERCENTAGE,
                PDPM_Report_Types.PDPM_Details_List.TYPE_NUMERIC_FLOAT);

        // Convert Time in seconds Time column to HH:MM:SS format
        for(int ci = 0; ci < num_rows; ci++)
        {
            if(columns[ci] == null)
            {
                break;
            }

            String stime = columns[ci].get(COL_TIME);
            PDPM_DEBUG.PDPM_REPORTER_LOG("stime: " + stime);
            int time = Integer.parseInt(stime);
            PDPM_DEBUG.PDPM_REPORTER_LOG("time: " + stime);

            PDPM_Date d = new PDPM_Date(time);

            columns[ci].set(COL_TIME, d.format());
            d = null;
        }

        ui.DetailsType2Heading(heading);

        for(int j = 0; j < num_rows; j++)
        {
            ui.DetailsType2Row(columns[j]);
        }

        ui.DetailsType2End(processing_note);

        PDPM_DEBUG.PDPM_REPORTER_LOG(
                "<- PDPM_View_Formatter_Category_Details.process()");

        return true;
    } // END: process
}

class PDPM_View_Formatter_Application_Details extends PDPM_View_Formatter
{
    public boolean process(BufferedReader input_file,
                           int day_total_time_seconds)
    {
        /*
        final int NUM_COLUMNS = 5;

        final int COL_APPLICATION     = 0;
        final int COL_APPLICATION_EXE = 1;
        final int COL_CATEGORY        = 2;
        final int COL_TIME            = 3;
        final int COL_PERCENTAGE      = 4;
        */
        final int NUM_COLUMNS = 4;

        final int COL_APPLICATION     = 0;
        final int COL_CATEGORY        = 1;
        final int COL_TIME            = 2;
        final int COL_PERCENTAGE      = 3;

        this.day_total_time_seconds = day_total_time_seconds;

        PDPM_DEBUG.PDPM_REPORTER_LOG(
                "-> PDPM_View_Formatter_Application_Details.process()");
        
        num_rows = 0;

        heading = new PDPM_Report_Types.PDPM_Details_List(NUM_COLUMNS);
        heading.set(COL_APPLICATION, "Application");
        // heading.set(COL_APPLICATION_EXE, "Application EXE");
        heading.set(COL_CATEGORY, "Category");
        heading.set(COL_TIME, "Time (HH:MM:SS)");
        heading.set(COL_PERCENTAGE, "%");

        columns =
            new PDPM_Report_Types.PDPM_Details_List[
                                       PDPM_GLOBALS.PDPM_MAX_APPLICATIONS];

        String line;
        String unknown_application_exe_names = null;

        while(true)
        {
            try
            {
                
            line = input_file.readLine();

            if(line == null)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG("End of file");
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

            String line_application =
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_FRIENDLY_NAME);
            String line_application_exe =
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_APPL_NAME);
            String line_application_category =
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_CATEGORY);
            String line_count = 
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_TIME);

            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Application on this line: " + line_application);
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Application EXE on this line: " + line_application_exe);
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Category on this line: " + line_application_category);
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Count on this line: " + line_count);

            if(line_application == null || line_count == null)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Application or Count not found on this line. Skipping to next line");
                continue;
            }
            
            boolean found = false;
            int n;

            // Add to List
            // If it is already in the list, update the count
            for(n = 0; n < columns.length; n++)
            {
                if(columns[n] == null)
                {
                    break;
                }
                
                if((columns[n].get(COL_APPLICATION)).equals(line_application))
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG("Found Application in List");

                    found = true;
                    
                    // Found, update count
                    try
                    {
                        Integer old_count = 
                            new Integer(columns[n].get(COL_TIME));
                        Integer new_count = new Integer(line_count);
                        new_count = new_count + old_count;
                        PDPM_DEBUG.PDPM_REPORTER_LOG(
                                "New count: " + new_count.toString());
                        columns[n].set(COL_TIME, new_count.toString());
                    }
                    catch(NumberFormatException nfe)
                    {
                        PDPM_DEBUG.PDPM_REPORTER_ERR(
                                "Time Count is not a valid number"); 
                        PDPM_DEBUG.PDPM_REPORTER_ERR(nfe);
                        continue;
                    }

                    // If the application is Unknown, append the EXE name
                    if((columns[n].get(COL_APPLICATION)).equals(
                                PDPM_GLOBALS.PDPM_LOCKED_APPLICATION_UNKNOWN))
                    {
                        /*
                        // First check if this EXE name is already appended
                        if(!columns[n].get(COL_APPLICATION_EXE).contains(
                                    line_application_exe))
                        {
                            // Append EXE name
                            String new_application_name = 
                                columns[n].get(COL_APPLICATION_EXE) +
                                ", " +
                                line_application_exe;

                            columns[n].set(
                                    COL_APPLICATION_EXE, new_application_name);
                        }
                        */
                        // First check if this EXE name is already appended
                        if(unknown_application_exe_names == null)
                        {
                            unknown_application_exe_names = new String(line_application_exe);
                        }
                        else if(!unknown_application_exe_names.contains(
                                    line_application_exe))
                        {
                            unknown_application_exe_names =
                                unknown_application_exe_names + ", " + line_application_exe;
                        }

                    }
                }
            }
            // Else create a new entry in the list
            if(!found)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG("Adding new application to the list");
                columns[n] = 
                    new PDPM_Report_Types.PDPM_Details_List(NUM_COLUMNS);

                columns[n].set(COL_APPLICATION, line_application);
                // columns[n].set(COL_APPLICATION_EXE, line_application_exe);
                columns[n].set(COL_CATEGORY, line_application_category);
                columns[n].set(COL_TIME, line_count);
                num_rows++;
            }

            }
            catch(Exception e)
            {
                PDPM_DEBUG.PDPM_REPORTER_ERR(
                        "Exception while processing. Skipping to next line.");
                
                PDPM_DEBUG.PDPM_REPORTER_ERR(e);
                
                continue;
            }
        } // END: while
        
        // Append EXE names for Unknown applications
        /*
        for(int m = 0; m < columns.length; m++)
        {
            if(columns[m] == null)
            {
                break;
            }

            if((columns[m].get(COL_APPLICATION)).equals(
                        PDPM_GLOBALS.PDPM_LOCKED_APPLICATION_UNKNOWN))
            {
                // Append EXE name
                String new_application_name = 
                    columns[m].get(COL_APPLICATION) +
                    " (" +
                    unknown_application_exe_names +
                    ")";

                columns[m].set(
                        COL_APPLICATION, new_application_name);

                break;
            }
        }
        */

        // Calculate Percentages
        int total_time = 0;
        for(int n = 0; n < num_rows; n++)
        {
            total_time = total_time + new Integer(columns[n].get(COL_TIME));
        }
        
        /*
        PDPM_DEBUG.PDPM_REPORTER_LOG("Total Time: " + total_time);
        
        for(int n = 0; n < num_rows; n++)
        {
            int percentage =
                (100 * (new Integer(columns[n].get(COL_TIME))))/total_time;
            columns[n].set(COL_PERCENTAGE, new Integer(percentage).toString());
        }
        */

        PDPM_DEBUG.PDPM_REPORTER_LOG("Counted Total Time: " +
                total_time);

        PDPM_DEBUG.PDPM_REPORTER_LOG("Total Time: " + day_total_time_seconds);

        int difference = (total_time > day_total_time_seconds) ?
                         (total_time - day_total_time_seconds) :
                         (day_total_time_seconds - total_time);

        if(difference != 0)
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG("Adding difference " + difference);
            columns[num_rows] = 
                new PDPM_Report_Types.PDPM_Details_List(NUM_COLUMNS);
            columns[num_rows].set(COL_APPLICATION, "Unaccounted");
            // columns[num_rows].set(COL_APPLICATION_EXE, "Unknown");
            columns[num_rows].set(COL_CATEGORY, "Unaccounted");
            columns[num_rows].set(COL_TIME, new String("" + difference));
            num_rows++;
        }

        // int index_locked_or_idle = -1;
        float percentage_totals = 0;
        
        for(int n = 0; n < num_rows; n++)
        {
            // Dont calculate percentage for Locked or Idle now, do it at the
            //  end by filling up remaing percentage value;
            /*
            if(index_locked_or_idle == -1)
            {
                if(columns[n].get(COL_CATEGORY).equals(
                            PDPM_GLOBALS.PDPM_LOCKED_APPLICATION_NAME))
                {
                    index_locked_or_idle = n;
                    continue;
                }
            }
            */

            float percentage =
                (100 * (new Float(columns[n].get(COL_TIME))))/
                                                   day_total_time_seconds;

            if(percentage < 1)
            {
                columns[n].set(COL_PERCENTAGE, "(Small)");
            }
            else
            {
                Formatter formatter = new Formatter();
                formatter.format("%2.2f", new Float(percentage));
                String str_percentage = formatter.toString();

                columns[n].set(
                        COL_PERCENTAGE, str_percentage);
            }

            percentage_totals = percentage_totals + percentage;
        }

        /*
        if(index_locked_or_idle != -1)
        {
            int percentage = 100 - percentage_totals;

            columns[index_locked_or_idle].set(
                    COL_PERCENTAGE, new Integer(percentage).toString());
        }
        */

        if(percentage_totals <= 99.99)
        {
            Formatter formatter = new Formatter();
            formatter.format("%2.2f", 
                    new Float((float) (100 - percentage_totals)));
            String str_percentage = formatter.toString();

            processing_note = 
                new String("Round-off error of about " + 
                        str_percentage + "%");

            PDPM_DEBUG.PDPM_REPORTER_LOG(processing_note);
        }

        // Sort
        PDPM_Report_Types.PDPM_Details_List.sort(columns, 
                COL_TIME,
                PDPM_Report_Types.PDPM_Details_List.TYPE_NUMERIC_FLOAT);

        // Convert Time in seconds Time column to HH:MM:SS format
        for(int ci = 0; ci < num_rows; ci++)
        {
            if(columns[ci] == null)
            {
                break;
            }

            String stime = columns[ci].get(COL_TIME);
            PDPM_DEBUG.PDPM_REPORTER_LOG("stime: " + stime);
            int time = Integer.parseInt(stime);
            PDPM_DEBUG.PDPM_REPORTER_LOG("time: " + stime);

            PDPM_Date d = new PDPM_Date(time);

            columns[ci].set(COL_TIME, d.format());
            d = null;
        }

        ui.DetailsType2Heading(heading);

        for(int j = 0; j < num_rows; j++)
        {
            ui.DetailsType2Row(columns[j]);
        }

        String unknown_note = null;

        if(unknown_application_exe_names != null)
        {
            unknown_note = "Unknown Applications: (" + 
                unknown_application_exe_names + ")";

            if(processing_note != null)
            {
                processing_note = processing_note + "<BR>" + unknown_note;
            }
            else
            {
                processing_note = unknown_note;
            }
        }

        ui.DetailsType2End(processing_note);

        PDPM_DEBUG.PDPM_REPORTER_LOG(
                "<- PDPM_View_Formatter_Application_Details.process()");

        return true;
    } // END: process
}

class PDPM_View_Formatter_Application_Title_Details extends PDPM_View_Formatter
{
    public boolean process(BufferedReader input_file,
                           int day_total_time_seconds)
    {
        final int NUM_COLUMNS = 5;

        final int COL_APPLICATION_TITLE = 0;
        final int COL_APPLICATION       = 1;
        final int COL_CATEGORY          = 2;
        final int COL_TIME              = 3;
        final int COL_PERCENTAGE        = 4;

        this.day_total_time_seconds = day_total_time_seconds;

        PDPM_DEBUG.PDPM_REPORTER_LOG(
                "-> PDPM_View_Formatter_Application_Title_Details.process()");
        
        num_rows = 0;

        heading = new PDPM_Report_Types.PDPM_Details_List(NUM_COLUMNS);
        heading.set(COL_APPLICATION_TITLE, "Application Detail");
        heading.set(COL_APPLICATION, "Application");
        heading.set(COL_CATEGORY, "Category");
        heading.set(COL_TIME, "Time (HH:MM:SS)");
        heading.set(COL_PERCENTAGE, "%");

        columns =
            new PDPM_Report_Types.PDPM_Details_List[
                                       PDPM_GLOBALS.PDPM_MAX_APPLICATIONS];

        String line;

        while(true)
        {
            try
            {
                
            line = input_file.readLine();

            if(line == null)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG("End of file");
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

            String line_application_title =
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_APPL_TITLE);
            String line_application =
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_FRIENDLY_NAME);
            String line_application_exe =
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_APPL_NAME);
            String line_application_category =
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_CATEGORY);
            String line_count = 
                get_field(line, PDPM_GLOBALS.REPORT_FILE_FIELD_TIME);

            PDPM_DEBUG.PDPM_REPORTER_LOG(
                   "Application Title on this line: " + line_application_title);
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Application on this line: " + line_application);
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Category on this line: " + line_application_category);
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Count on this line: " + line_count);

            if(line_application_title == null || line_count == null)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG(
                    "Application or Count not found on this line. Skipping to next line");
                continue;
            }
            
            if(line_application_title.length() >
                    PDPM_GLOBALS.REPORT_FORMAT_APPL_TITLE_MAX_LEN)
            {
                line_application_title =
                    line_application_title.substring(0,
                            PDPM_GLOBALS.REPORT_FORMAT_APPL_TITLE_MAX_LEN - 3);

                line_application_title = line_application_title + "...";
            }

            boolean found = false;
            int n;
            // Add to List
            // If it is already in the list, update the count
            for(n = 0; n < columns.length; n++)
            {
                if(columns[n] == null)
                {
                    break;
                }
                
                if((columns[n].get(COL_APPLICATION_TITLE)).equals(
                            line_application_title))
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG(
                            "Found Application Title in List");

                    found = true;
                    
                    // Found, update count
                    try
                    {
                        Integer old_count = new Integer(columns[n].get(COL_TIME));
                        Integer new_count = new Integer(line_count);
                        new_count = new_count + old_count;
                        PDPM_DEBUG.PDPM_REPORTER_LOG(
                                "New count: " + new_count.toString());
                        columns[n].set(COL_TIME, new_count.toString());
                    }
                    catch(NumberFormatException nfe)
                    {
                        PDPM_DEBUG.PDPM_REPORTER_ERR(
                                "Time Count is not a valid number"); 
                        PDPM_DEBUG.PDPM_REPORTER_ERR(nfe);
                        continue;
                    }

                    // If the application title is Unknown,
                    // append the aplication name
                    if((columns[n].get(COL_APPLICATION_TITLE)).equals(
                                PDPM_GLOBALS.PDPM_APPLICATION_TITLE_UNKNOWN))
                    {
                        // First check if this app name is already appended
                        if(!columns[n].get(COL_APPLICATION).contains(
                                    line_application))
                        {
                            // Append app name
                            String new_application_name = 
                                columns[n].get(COL_APPLICATION) +
                                ", " +
                                line_application;

                            columns[n].set(
                                    COL_APPLICATION, new_application_name);
                        }

                        columns[n].set(COL_CATEGORY, 
                                PDPM_GLOBALS.REPORT_FORMAT_CATEGORY_VARIOUS);
                    }
                }
            }
            // Else create a new entry in the list
            if(!found)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG(
                        "Adding new application to the list");

                columns[n] = 
                    new PDPM_Report_Types.PDPM_Details_List(NUM_COLUMNS);

                columns[n].set(COL_APPLICATION_TITLE, line_application_title);
                columns[n].set(COL_APPLICATION, line_application);
                columns[n].set(COL_CATEGORY, line_application_category);
                columns[n].set(COL_TIME, line_count);
                num_rows++;
            }

            }
            catch(Exception e)
            {
                PDPM_DEBUG.PDPM_REPORTER_ERR(
                        "Exception while processing. Skipping to next line.");
                
                PDPM_DEBUG.PDPM_REPORTER_ERR(e);
                
                continue;
            }
        } // END: while
        
        // Calculate Percentages
        int total_time = 0;
        for(int n = 0; n < num_rows; n++)
        {
            total_time = total_time + new Integer(columns[n].get(COL_TIME));
        }
        
        PDPM_DEBUG.PDPM_REPORTER_LOG("Counted Total Time: " +
                total_time);

        PDPM_DEBUG.PDPM_REPORTER_LOG("Total Time: " + day_total_time_seconds);

        int difference = (total_time > day_total_time_seconds) ?
                         (total_time - day_total_time_seconds) :
                         (day_total_time_seconds - total_time);

        if(difference != 0)
        {
            PDPM_DEBUG.PDPM_REPORTER_LOG("Adding difference " + difference);
            columns[num_rows] = 
                new PDPM_Report_Types.PDPM_Details_List(NUM_COLUMNS);
            columns[num_rows].set(COL_APPLICATION_TITLE, "Unaccounted");
            columns[num_rows].set(COL_APPLICATION, "Unknown");
            columns[num_rows].set(COL_CATEGORY, "Unaccounted");
            columns[num_rows].set(COL_TIME, new String("" + difference));
            num_rows++;
        }

        // int index_locked_or_idle = -1;
        float percentage_totals = 0;
        
        for(int n = 0; n < num_rows; n++)
        {
            float percentage =
                (100 * (new Float(columns[n].get(COL_TIME))))/
                                                   day_total_time_seconds;

            if(percentage < 1)
            {
                columns[n].set(COL_PERCENTAGE, "(Small)");
            }
            else
            {
                Formatter formatter = new Formatter();
                formatter.format("%2.2f", new Float(percentage));
                String str_percentage = formatter.toString();

                columns[n].set(
                        COL_PERCENTAGE, str_percentage);
            }

            percentage_totals = percentage_totals + percentage;
        }

        if(percentage_totals <= 99.99)
        {
            Formatter formatter = new Formatter();
            formatter.format("%2.2f", 
                    new Float((float) (100 - percentage_totals)));
            String str_percentage = formatter.toString();

            processing_note = 
                new String("Round-off error of about " + 
                        str_percentage + "%");

            PDPM_DEBUG.PDPM_REPORTER_LOG(processing_note);
        }

        // Sort
        PDPM_Report_Types.PDPM_Details_List.sort(columns, 
                COL_TIME,
                PDPM_Report_Types.PDPM_Details_List.TYPE_NUMERIC_FLOAT);

        // Convert Time in seconds Time column to HH:MM:SS format
        for(int ci = 0; ci < num_rows; ci++)
        {
            if(columns[ci] == null)
            {
                break;
            }

            String stime = columns[ci].get(COL_TIME);
            // PDPM_DEBUG.PDPM_REPORTER_LOG("stime: " + stime);
            int time = Integer.parseInt(stime);
            // PDPM_DEBUG.PDPM_REPORTER_LOG("time: " + stime);

            PDPM_Date d = new PDPM_Date(time);

            columns[ci].set(COL_TIME, d.format());
            d = null;
        }

        ui.DetailsHeading(heading);

        for(int j = 0; j < num_rows; j++)
        {
            ui.DetailsRow(columns[j]);
        }

        ui.DetailsEnd(processing_note);

        PDPM_DEBUG.PDPM_REPORTER_LOG(
                "<- PDPM_View_Formatter_Application_Title_Details.process()");

        return true;
    } // END: process
}


