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

public class PDPM_Application
{
    private String application_name;

    private String application_title;

    private String application_friendly_name;

    private String application_category;

    private int application_count;

    public PDPM_Application(String name, String title)
    {
        application_name = name;

        application_title = title;

        application_friendly_name = null;

        application_category = null;

        application_count = 0;
    }
    
    public String get_application_name()
    {
        return application_name;
    }
    
    public String get_application_title()
    {
        return application_title;
    }
    
    public String get_application_friendly_name()
    {
        return application_friendly_name;
    }
    
    public String get_application_category()
    {
        return application_category;
    }
    
    public int get_application_count()
    {
        return application_count;
    }
    
    public void set_friendly_name(String friendly_name)
    {
        application_friendly_name = friendly_name;
    }

    public void set_category(String category)
    {
        application_category = category;
    }

    public void update_count(int count)
    {
        application_count = application_count + count;
    }
}

