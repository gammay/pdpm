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

public class PDPM_DEBUG
{
    public static final boolean DEBUG = true;

    public static String LOG_FILE_NAME = "dbg-pdpm-java.log";

    public static PrintStream out = null;

    public static void PDPM_DEBUG_INIT()
    {
        if(DEBUG)
        {
            if(out == null)
            {
                try
                {
                    out = new PrintStream(LOG_FILE_NAME);
                }
                // Catches any error conditions
                catch (IOException e)
                {
                    PDPM_DEBUG.PDPM_REPORTER_ERR("Could not create log file");
                    PDPM_DEBUG.PDPM_REPORTER_ERR(e);
                }
            }
        }
    }

    public static void PDPM_REPORTER_LOG(String log)
    {
        if(DEBUG)
        {
            out.println(log);
        }
    }

    public static void PDPM_REPORTER_ERR(String log)
    {
        if(DEBUG)
        {
            out.println(log);
        }
    }

    public static void PDPM_REPORTER_ERR(Exception e)
    {
        // e.printStackTrace(System.err);
        e.printStackTrace(out);
    }
}

