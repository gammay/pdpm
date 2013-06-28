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

import java.io.*;
import java.util.*;

import com.utils.pdpm.PDPM_Application;

class PDPM_Application_Config_List
{
    private class PDPM_Application_Config
    {
        private String application_name;
        private String application_friendly_name;
        private String application_category;

        private PDPM_Application_Config(String name, 
                               String friendly_name, String category)
        {
            application_name = name;
            application_friendly_name = friendly_name;
            application_category = category;
        }
    }

    private PDPM_Application_Config pdpm_applications_config[] = null;

    int num_pdpm_applications = 0;

    protected PDPM_Application_Config_List()
    {
        pdpm_applications_config = 
            new PDPM_Application_Config[PDPM_GLOBALS.PDPM_MAX_APPLICATIONS];

        // Read from file and fill
        BufferedReader input = null; 
        try 
        { 
            FileReader file = 
                new FileReader(PDPM_GLOBALS.PDPM_APPLICATIONS_CONFIG_FILE);

            input = new BufferedReader(file);
        }   
        catch(FileNotFoundException x)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR("File not found [" 
                    + PDPM_GLOBALS.PDPM_APPLICATIONS_CONFIG_FILE + "]");
            PDPM_DEBUG.PDPM_REPORTER_ERR(x);
            return;
        }

        String line;

        while(true)
        {
            try
            {
                line = input.readLine();
                PDPM_DEBUG.PDPM_REPORTER_LOG(
                        "[PDPM_Application_Config_List] Line: " + line);

                if(line == null)
                {
                    break;
                }

                line = line.trim();

                if(line.length() == 0)
                {
                    continue;
                }

                // Ignore comment lines
                if(line.charAt(0) == '#')
                {
                    continue;
                }

                // Get Application Name
                int appname_index = 0;
                // Get Application Friendly Name
                int appfname_index = line.indexOf(',', appname_index + 1);
                appfname_index += 1;
                // Get Application Category
                int appcategory_index = line.indexOf(',', appfname_index + 1);
                appcategory_index += 1;

                String app_name = 
                    line.substring(0, appfname_index - 1);
                String app_f_name = 
                    line.substring(appfname_index, appcategory_index - 1);
                String app_category = 
                    line.substring(appcategory_index);

                if(app_name != null && app_name.length() != 0 &&
                   app_f_name != null && app_f_name.length() != 0 &&
                   app_category != null && app_category.length() != 0)
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG(
                            "Adding Application EXE: " + app_name + 
                            " Friendly Name: " + app_f_name +
                            " Category: " + app_category);

                    pdpm_applications_config[num_pdpm_applications++]
                        = new PDPM_Application_Config(
                                app_name, app_f_name, app_category);
                }
            }
            catch(Exception e)
            {
                PDPM_DEBUG.PDPM_REPORTER_ERR(
                        "Exception while reading file. Trying next line");
                PDPM_DEBUG.PDPM_REPORTER_ERR(e);
                return;
            }
        }
    }

    protected void process(PDPM_Application_List pdpm_applications)
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG(
                " --> PDPM_Application_Config_List.process");

        PDPM_Application application_ptr = null;

        for(int n = 0; n < pdpm_applications.pdpm_application_list.length; n++)
        {
            boolean found = false;

            application_ptr = pdpm_applications.pdpm_application_list[n];

            if(application_ptr == null)
            {
                break;
            }

            for(int i = 0; i < pdpm_applications_config.length; i++)
            {
                if(pdpm_applications_config[i] == null)
                {
                    break;
                }

                if(application_ptr.get_application_name().equalsIgnoreCase(
                            pdpm_applications_config[i].application_name))
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG("Found Category " +
                            pdpm_applications_config[i].application_category +
                            " for application " +
                            application_ptr.get_application_name());

                    application_ptr.set_friendly_name( 
                        pdpm_applications_config[i].application_friendly_name);

                    application_ptr.set_category( 
                        pdpm_applications_config[i].application_category);

                    found = true;
                }
            }

            if(!found)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG(
                        "Category not found for application " +
                        application_ptr.get_application_name());

                application_ptr.set_friendly_name("Unknown");
                application_ptr.set_category("Unknown");
            }
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG(
                " <-- PDPM_Application_Config_List.process");
    }
}

public class PDPM_Application_List
{
    protected PDPM_Application pdpm_application_list[] = null;

    private PDPM_Application_Config_List pdpm_application_config_list = null;

    public PDPM_Application_List()
    {
        pdpm_application_list = 
             new PDPM_Application[PDPM_GLOBALS.PDPM_MAX_APPLICATIONS];

        pdpm_application_config_list =
            new PDPM_Application_Config_List();
    }

    public boolean insert(String application_name, 
                       String application_title, 
                       int count)
    {
        try
        {
            PDPM_Application application_ptr = null;
            int n = 0;

            for(n = 0; n < pdpm_application_list.length; n++)
            {
                application_ptr =  pdpm_application_list[n];

                if(application_ptr == null)
                {
                    break;
                }

                if((application_name.equals(
                        application_ptr.get_application_name())) &&
                   (application_title.equals(
                        application_ptr.get_application_title())))
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG("Found");
                    break;
                }
            }

            if(application_ptr != null)
            {
                // Found, update count
                application_ptr.update_count(count);
                PDPM_DEBUG.PDPM_REPORTER_LOG("Updated count: " + 
                        application_ptr.get_application_count());
            }
            else
            {
                // Not found, create new application entry
                application_ptr = 
                    new PDPM_Application(application_name, application_title);
                application_ptr.update_count(count);

                // Before inserting into the application list array, check if
                //  max size is reached
                if(n >= PDPM_GLOBALS.PDPM_MAX_APPLICATIONS)
                {
                    PDPM_DEBUG.PDPM_REPORTER_LOG("Max applications reached");
                    return false;
                }

                PDPM_DEBUG.PDPM_REPORTER_LOG("New application inserted");
                pdpm_application_list[n] = application_ptr;
            }

        }
        catch(Exception e)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR("Exception during processing");
            PDPM_DEBUG.PDPM_REPORTER_ERR(e);            
            return false;
        }
        return true;
    }

    public void process()
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG(" --> PDPM_Application_List.process");

        try
        {
            pdpm_application_config_list.process(this);
        }
        catch(Exception e)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR("Exception during processing");
            PDPM_DEBUG.PDPM_REPORTER_ERR(e);            
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG(" <-- PDPM_Application_List.process");
    }

    public void write_to_file(PrintWriter out)
    {
        PDPM_Application application_ptr = null;

        for(int n = 0; n < pdpm_application_list.length; n++)
        {
            application_ptr =  pdpm_application_list[n];

            if(application_ptr == null)
            {
                break;
            }

            out.print(application_ptr.get_application_name());
            out.print(",");
            out.print(application_ptr.get_application_friendly_name());
            out.print(",");
            out.print(application_ptr.get_application_title());
            out.print(",");
            out.print(application_ptr.get_application_category());
            out.print(",");
            out.print((new Integer(
                          application_ptr.get_application_count())).toString());
            out.println("");
        }

        return;
    }

    public void debug_print()
    {
        PDPM_Application application_ptr = null;

        PDPM_DEBUG.PDPM_REPORTER_LOG(" ---- ---- ---- ----");
        PDPM_DEBUG.PDPM_REPORTER_LOG("Dumping PDPM Application List");

        for(int n = 0; n < pdpm_application_list.length; n++)
        {
            application_ptr =  pdpm_application_list[n];

            if(application_ptr == null)
            {
                break;
            }

            PDPM_DEBUG.PDPM_REPORTER_LOG("[" + n + "]");
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    application_ptr.get_application_name());
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    application_ptr.get_application_friendly_name());
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    application_ptr.get_application_title());
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    application_ptr.get_application_category());
            PDPM_DEBUG.PDPM_REPORTER_LOG(
                    (new Integer(application_ptr.get_application_count())).toString());
            PDPM_DEBUG.PDPM_REPORTER_LOG(" ---- ---- ---- ----");
        }

        return;
     }
}

