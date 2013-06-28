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
 * Added descriptive error.
 */

package com.utils.pdpm;

import java.lang.*;
import java.io.*;
import java.util.*;

import com.utils.pdpm.PDPM_ERROR;
import com.utils.pdpm.PDPM_GLOBALS;

import com.utils.pdpm.PDPM_Reporter;
import com.utils.pdpm.PDPM_UI_Plugin;
import com.utils.pdpm.PDPM_HTML_REPORT;

public class PDPM_Generate_Report
{
    public static int DELAY = 2000;

    public static void main(String[] args) throws Exception
    {
        generate(args);
    }

    public static String generate(String[] args) throws Exception
    {
        PDPM_DEBUG.PDPM_DEBUG_INIT();

        if(args.length == 0)
        {
            System.out.println("Atleast one dat file name needed");
            PDPM_DEBUG.PDPM_REPORTER_LOG("Atleast one dat file name needed");

            System.out.println("");
            if(DELAY != 0)
            {
                Thread.sleep(DELAY);
            }
            return null;
        }

        // The last generated report file name

        String html_report_name = null;

        // for each date in argument
        for(int d = 0; d < args.length; d++)
        {
            String dat_file_name = null;
            dat_file_name = args[d];

            System.out.println("Processing file " + dat_file_name + " ...");
            PDPM_DEBUG.PDPM_REPORTER_LOG("Processing file " + dat_file_name);

            // Stage 1: Call Reporter to generate rep file
            System.out.print("    Stage 1 ...");
            PDPM_DEBUG.PDPM_REPORTER_LOG("    Stage 1 ...");

            PDPM_Reporter reporter = new PDPM_Reporter();
            String rep_file_name = reporter.Generate(dat_file_name);

            if(rep_file_name == null)
            {
                String error = reporter.get_last_error();
                int error_code = reporter.get_last_error_code();

                if(error != null)
                {
                    System.out.println("Error: " + error);
                    if(error_code == PDPM_ERROR.PDPM_REPORTER_FILE_NOT_FOUND)
                    {
                        System.out.println("\n(This happens if PDPM was not running on this day, because the machine was shutdown or put on hibernate, for example)");
                    }

                    PDPM_DEBUG.PDPM_REPORTER_LOG("Error: " + error);
                }

                /*
                   System.out.println(
                   "    Error in generating report. Please see debug file: " + 
                   PDPM_DEBUG.LOG_FILE_NAME + " for details");
                   */

                PDPM_DEBUG.PDPM_REPORTER_LOG("Error");

                System.out.println(" ");
                if(DELAY != 0)
                {
                    Thread.sleep(DELAY);
                }
                return null;
            }
            System.out.println(" Pass");
            PDPM_DEBUG.PDPM_REPORTER_LOG(" Pass");

            // Call HTML Report Generator to generate HTML report
            System.out.print("    Stage 2 ...");
            PDPM_DEBUG.PDPM_REPORTER_LOG("    Stage 2 ...");

            PDPM_HTML_REPORT html_report = new PDPM_HTML_REPORT();
            PDPM_Report_View view = 
                new PDPM_Report_View(html_report, rep_file_name);

            boolean ret = view.start(
                    PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_TYPE_ALL);

            if(ret == false)
            {
                /*
                   System.out.println(
                   "    Error in generating report. Please see debug file: " + 
                   PDPM_DEBUG.LOG_FILE_NAME + " for details");
                   */
                PDPM_DEBUG.PDPM_REPORTER_LOG("Error");
                System.out.println("Error (See log file for details)");

                System.out.println(" ");
                if(DELAY != 0)
                {
                    Thread.sleep(DELAY);
                }
                return null;                
            }

            System.out.println(" Pass");

            html_report_name = html_report.get_report_file_name();
            System.out.println("    Report Generated: " 
                    + html_report_name);
            PDPM_DEBUG.PDPM_REPORTER_LOG("    Report Generated: " 
                    + html_report_name);

            // If log files are to be deleted, delete
            // TODO
        }

        System.out.println(" ");
        if(DELAY != 0)
        {
            Thread.sleep(DELAY);
        }

        return html_report_name;
    }
}
