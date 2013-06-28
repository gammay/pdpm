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

import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.*;

import com.utils.pdpm.PDPM_ERROR;
import com.utils.pdpm.PDPM_GLOBALS;

import com.utils.pdpm.PDPM_Reporter;
import com.utils.pdpm.PDPM_UI_Plugin;
import com.utils.pdpm.PDPM_HTML_REPORT;

public class PDPM_Generate_Todays_Report
{
    public static void main(String[] args) throws Exception
    {
        PDPM_DEBUG.PDPM_DEBUG_INIT();

        // Clear output file
        PrintWriter out;
        try
        {
            // Print a line of text
            out = 
              new PrintWriter(
                new FileOutputStream(PDPM_GLOBALS.WRITE_REPORT_NAME_FILE_TO));
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

        // today's date
        Calendar cal = new GregorianCalendar();
        cal.setLenient(false);

        PDPM_Generate_Report.DELAY = 0;
        
        DateFormat df = new SimpleDateFormat("MM_dd_yy");
        String date_str = df.format(cal.getTime());

        System.out.println("Generating today's report (" + date_str + ")...");

        // Form dat file name
        String dat_file_name = "dat-" + date_str + ".pdp";

        String html_report_name;

        try
        {
            PDPM_Generate_Report generator = new PDPM_Generate_Report();
            String gen_args[] = new String[1];
            gen_args[0] = dat_file_name;
            html_report_name = generator.generate(gen_args);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("... Data file is not available. (PDPM should be running to get data file)");
            return;
        }
        catch(Exception e)
        {
            System.out.println("... Error scanning");
            e.printStackTrace();
            return;
        }

        if(html_report_name != null)
        {
            // Replace forward slash with backward slash
            html_report_name = html_report_name.replace('/', '\\');
            out.println(html_report_name);
        }
        else
        {
            Thread.sleep(5000);
        }

        out.close();

        // Thread.sleep(5000);

        return;
    }
}
