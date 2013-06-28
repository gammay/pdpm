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

import java.io.*;

import com.utils.pdpm.PDPM_ERROR;
import com.utils.pdpm.PDPM_GLOBALS;
import com.utils.pdpm.PDPM_DEBUG;

class PDPM_Config_Reader
{
    static String config_params[] = 
    {
        PDPM_GLOBALS.PDPM_CONFIG_PARAM_PDPM_FILES_PATH,
        PDPM_GLOBALS.PDPM_CONFIG_PARAM_PDPM_REPORTS_PATH,
        PDPM_GLOBALS.PDPM_CONFIG_PARAM_PDPM_DELETE_OLD_LOGS,
    };

    static Object config_param_values[] = null;

    public static boolean load_config_params()
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG("-> load_config_params()");

        // Allocate memory
        if(config_param_values == null)
        {
            config_param_values = new String[config_params.length];
        }

        for(int num_params = 0; num_params < config_params.length; num_params++)
        {
            Object value = load_config_param(config_params[num_params]);

            if(value != null)
            {
                config_param_values[num_params] = (Object) value;
            }
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG("<- load_config_params()");

        return true;
    }

    private static Object load_config_param(String param)
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG("-> load_config_param() for: " +
                param);

        // Open file
        FileReader file = null;
        BufferedReader input = null; 

        try 
        { 
            file = new FileReader(PDPM_GLOBALS.PDPM_MASTER_CONFIG_FILE);
            input = new BufferedReader(file);
        }   
        catch(FileNotFoundException x)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR("File not found [" 
                    + PDPM_GLOBALS.PDPM_MASTER_CONFIG_FILE + "]");
            PDPM_DEBUG.PDPM_REPORTER_ERR(x.getMessage());
            return null;
        }  

        String line = null;
        boolean found = false;

        while(true)
        {
            try
            {
                line = input.readLine();

                if(line == null)
                {
                    break;
                }

                line = line.trim();

                if(line.length() == 0)
                {
                    continue;
                }

                if(line.charAt(0) == '#')
                {
                    continue;
                }

                if(line.endsWith("\n"))
                {
                    line = line.substring(0, line.length() - 2);
                }

                if(!line.equals(param))
                {
                    continue;
                }

                found = true;
                break;
            }
            catch(Exception e)
            {
                PDPM_DEBUG.PDPM_REPORTER_ERR("Exception while processing. Trying next line");
                PDPM_DEBUG.PDPM_REPORTER_ERR(e);
            }
        }

        // Get type first
        String type = 
            get_pattern(input, PDPM_GLOBALS.PDPM_CONFIG_PATTERN_TYPE);

        PDPM_DEBUG.PDPM_REPORTER_ERR("Type: " + type);

        if(type == null)
        {
            PDPM_DEBUG.PDPM_REPORTER_ERR("Type not set. Assuming String");
            type = PDPM_GLOBALS.PDPM_CONFIG_TYPE_STRING;
        }

        // Get Value
        Object value = get_value(input, type);

        PDPM_DEBUG.PDPM_REPORTER_LOG("Found Value: " + value);

        try
        {
        file.close();
        input.close();
        }
        catch(IOException e)
        {
            // Ignore
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG("<- load_config_param()");

        return value;
    }

    private static String get_pattern(BufferedReader input, String pattern)
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG("->get_pattern() pattern: " +
                pattern);

        String line = null;
        String value = null;

        while(true)
        {
            try
            {
                line = input.readLine();

                PDPM_DEBUG.PDPM_REPORTER_LOG("Read line: " + line);

                if(line == null)
                {
                    break;
                }

                line = line.trim();

                if(line.length() == 0)
                {
                    continue;
                }

                if(line.charAt(0) == '#')
                {
                    continue;
                }

                if(!line.startsWith(pattern))
                {
                    continue;
                }

                PDPM_DEBUG.PDPM_REPORTER_LOG("Found pattern");

                int index = line.indexOf('=');

                value = line.substring(index + 1);

                value = value.trim();

                // Strip newline
                if(value.endsWith("\n"))
                {
                    value = value.substring(0, value.length() - 2);
                }

                // Strip double qoutes
                if(value.startsWith("\""))
                {
                    value = value.substring(1, value.length() - 1);
                }
                if(value.endsWith("\""))
                {
                    value = value.substring(0, value.length() - 2);
                }

                break;
            }
            catch(Exception e)
            {
                PDPM_DEBUG.PDPM_REPORTER_ERR(
                        "Exception while processing. Trying next line");
                PDPM_DEBUG.PDPM_REPORTER_ERR(e);
            }
        }
        
        PDPM_DEBUG.PDPM_REPORTER_LOG("Value: " + value);

        PDPM_DEBUG.PDPM_REPORTER_LOG("<- get_pattern()");

        return value;
    }

    private static Object get_value(BufferedReader input, String type)
    {
        String value_str = null;
        Object value = null;
        
        value_str = get_pattern(input, PDPM_GLOBALS.PDPM_CONFIG_PATTERN_VALUE);

        if(value_str == null)
        {
            return null;
        }

        if(type.equals(PDPM_GLOBALS.PDPM_CONFIG_TYPE_STRING))
        {
            value = new String(value_str);
        }
        else if(type.equals(PDPM_GLOBALS.PDPM_CONFIG_TYPE_FOLDER_SELECTION))
        {
            value = new String(value_str);
        }
        else if(type.equals(PDPM_GLOBALS.PDPM_CONFIG_TYPE_YES_NO))
        {
            // TODO. Trying to store Boolean throws ArrayStoreException
            //  To be checked and fixed later
            if(value_str.equalsIgnoreCase("YES"))
            {
                // value = new Boolean(true);
                value = new String("YES");
            }
            else if(value_str.equalsIgnoreCase("NO"))
            {
                // value = new Boolean(false);
                value = new String("NO");
            }
        }

        return value;
    }

    public static Object get_config_param(String param)
    {
        PDPM_DEBUG.PDPM_REPORTER_LOG("-> get_config_param()");

        if(config_param_values == null)
        {
            load_config_params();
        }

        for(int num_params = 0; num_params < config_params.length; num_params++)
        {
            if(config_params[num_params].equals(param))
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG("Found param. Value: " +
                        config_param_values[num_params]);
                PDPM_DEBUG.PDPM_REPORTER_LOG("<- get_config_param()");
                return config_param_values[num_params];
            }
        }

        PDPM_DEBUG.PDPM_REPORTER_LOG("<- get_config_param()");

        return null;
    }

    public static void main(String[] args)
    {
        PDPM_DEBUG.PDPM_DEBUG_INIT();

        load_config_params();
    }
}
