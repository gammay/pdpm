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

import com.utils.pdpm.PDPM_Report_Types;

abstract class PDPM_UI_Plugin
{
    final public void PrepareReport(String arg)
    {
        // Called by UI
        // Initialise PDPM Report View
        // Get errors
        // Summary
        // Details
    }

    abstract public void Start(String date_short);

    abstract public void End(String info);
    
    abstract public void Error(String error_str);

    abstract public void Heading(String heading);

    abstract public void SummaryStart();

    abstract public void SummaryHeader(String header1, String header2);

    abstract public void SummaryRow(String col1, String col2);

    abstract public void SummaryEnd();

    abstract public void ProcessingNoteStart();

    abstract public void ProcessingNoteRow(String note);

    abstract public void ProcessingNoteEnd();

    abstract public void DetailsAll(String[] details_description);

    // Type 1 details (the default type)
    //  With chart
    abstract public void DetailsStart(String description);

    abstract public void DetailsHeading(PDPM_Report_Types.PDPM_Details_List list);

    abstract public void DetailsRow(PDPM_Report_Types.PDPM_Details_List list);

    abstract public void DetailsEnd(String note);

    // Type 2 details
    //  Without chart
    abstract public void DetailsType2Start(String description);

    abstract public void DetailsType2Heading(PDPM_Report_Types.PDPM_Details_List list);

    abstract public void DetailsType2Row(PDPM_Report_Types.PDPM_Details_List list);

    abstract public void DetailsType2End(String note);

    abstract public void EndNote(String note);
}
