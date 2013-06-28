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
 * If error in report generation, window does not close automatically.
 * Increased sleep duration in other cases.
 *
 * 29-Dec-2008: Girish:
 * - If "-yesterday" flag is specified, reported is generated only for
 *   yesterday and not for "SCAN_LAST_DAYS_DEFAULT starting yesterday"
 * - This is to handle the case where PC is hibernated and user wants to
 *   see report for yesterday, the next day morning.
 */
package com.utils.pdpm;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.*;

import com.utils.pdpm.PDPM_ERROR;
import com.utils.pdpm.PDPM_GLOBALS;

import com.utils.pdpm.PDPM_Reporter;
import com.utils.pdpm.PDPM_UI_Plugin;
import com.utils.pdpm.PDPM_HTML_REPORT;

public class PDPM_Generate_Pending_Reports
{
    private static final int SCAN_LAST_DAYS_DEFAULT = 3;
    private static final String YESTERDAY_FLAG = "-yesterday";
    
    /*
     * Parameters:
     * -yesterday - Start scanning from yesterday (Used for calling from
     *              PDPM_Collector.exe). If not specified, scanning starts
     *              from today's date.
     * Some number - Number of days to scan.
     *               If not specified, SCAN_LAST_DAYS_DEFAULT is used
     *
     * All parameters are optional.
     * Examples:
     * > PDPM_Collector.exe calls using the syntax -
     *      "PDPM_Generate_Pending_Reports -yesterday"
     * > When double-clicked from Explorer by user -
     *      "PDPM_Generate_Pending_Reports"
     */
    public static void main(String[] args) throws Exception
    {
        PDPM_DEBUG.PDPM_DEBUG_INIT();

        int scan_days = SCAN_LAST_DAYS_DEFAULT;
        boolean yesterday_flag = false;
        String html_report_name = null;
        PrintWriter output_file = null;

        if(args.length != 0)
        {
            for(int i = 0; i < args.length; i++)
            {
                if(args[i] != null)
                {
                    // Check if it is yesterday flag
                    if(args[i].compareToIgnoreCase(YESTERDAY_FLAG) == 0)
                    {
                        yesterday_flag = true;
                    }
                    else
                    {
                        yesterday_flag = false;
                        // Check if it is a number
                        try
                        {
                            scan_days = Integer.parseInt(args[0]);
                        }
                        catch(Exception e)
                        {
                        }
                    }
                }
            }
        }
        
        if(yesterday_flag)
        {
            scan_days = 1;

            // Clear output file
            try
            {
                // Print a line of text
                output_file = 
                  new PrintWriter(
                    new FileOutputStream(
                        PDPM_GLOBALS.WRITE_REPORT_NAME_FILE_TO));
            }
            // Catches any error conditions
            catch (IOException e)
            {
                PDPM_DEBUG.PDPM_REPORTER_ERR("Could not create output file");
                PDPM_DEBUG.PDPM_REPORTER_ERR(e.getMessage());
                System.out.println("Could not create output file" + 
                        PDPM_GLOBALS.WRITE_REPORT_NAME_FILE_TO);
                return;
            }
        }
        else
        {
            System.out.print("Scanning for last " + scan_days + " days:");
        }

        // Start with yesterday's date
        Calendar cal = new GregorianCalendar();
        cal.setLenient(false);
        cal.add(Calendar.DATE, -1);
        /*
        cal.clear();
        cal.set(Calendar.YEAR, 2004);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DATE, 1);
        */

        int n = 1;

        PDPM_Generate_Report.DELAY = 0;
        

        while(true)
        {
            /*
            System.out.println(cal.get(Calendar.MONTH) + "-" +
                    cal.get(Calendar.DATE) + "-" +
                    cal.get(Calendar.YEAR));
            */

            /*
            String date_str = cal.get(Calendar.MONTH) + "_" +
                    cal.get(Calendar.DATE) + "_" +
                    cal.get(Calendar.YEAR);
            */

            DateFormat df = new SimpleDateFormat("MM_dd_yy");
            String date_str = df.format(cal.getTime());

            System.out.print("Checking for date (MM_DD_YY): " + date_str 
                    + "...");

            // Form dat file name
            String dat_file_name = "dat-" + date_str + ".pdp";

            try
            {
                /*
                FileReader file = new FileReader(dat_file_name);
                BufferedReader input = new BufferedReader(file);
                System.out.println("    - Found. Processing...");
                */
                PDPM_Generate_Report generator = new PDPM_Generate_Report();
                String gen_args[] = new String[1];
                gen_args[0] = dat_file_name;
                // generator.main(gen_args);
                html_report_name = generator.generate(gen_args);
            }
            catch(FileNotFoundException e)
            {
                System.out.println("     - PDPM Data for this date is not available.");
            }
            catch(Exception e)
            {
                System.out.println("     - Error scanning");
            }

            // Enough reports generated?
            if(n >= scan_days)
            {
                break;
            }

            // Go back one previous day
            cal.add(Calendar.DATE, -1);
            n++;
        }

        if(yesterday_flag)
        {
            // Write report file name to Open-Current-Report.bat so that
            // the report can be opened by the script Yesterday's-Report.bat
            if(html_report_name != null)
            {
                // Replace forward slash with backward slash
                html_report_name = html_report_name.replace('/', '\\');
                if(output_file != null)
                {
                    output_file.println(html_report_name);
                    output_file.close();
                }
            }
            else
            {
                // Thread.sleep(5000);
                System.in.read();
            }
        }

        if(!yesterday_flag)
        {
            Thread.sleep(5000);
        }
    }
}
