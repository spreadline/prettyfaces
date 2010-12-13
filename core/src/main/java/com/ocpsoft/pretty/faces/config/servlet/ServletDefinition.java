/*
 * PrettyFaces is an OpenSource JSF library to create bookmarkable URLs.
 * Copyright (C) 2010 - Lincoln Baxter, III <lincoln@ocpsoft.com> This program
 * is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with this program. If not, see the file COPYING.LESSER
 * or visit the GNU website at <http://www.gnu.org/licenses/>.
 */

package com.ocpsoft.pretty.faces.config.servlet;

/**
 * @author Lincoln Baxter, III <lincoln@ocpsoft.com>
 */
public class ServletDefinition
{
    private String servletName;
    private String servletClass;

    public String getServletName()
    {
        return servletName;
    }

    public void setServletName(final String servletName)
    {
        this.servletName = servletName;
        if (this.servletName != null)
        {
            this.servletName = this.servletName.trim();
        }
    }

    public String getServletClass()
    {
        return servletClass;
    }

    public void setServletClass(final String servletClass)
    {
        this.servletClass = servletClass;
        if (this.servletClass != null)
        {
            this.servletClass = this.servletClass.trim();
        }
    }

    @Override
    public String toString()
    {
        return "ServletDefinition [servletClass=" + servletClass + ", servletName=" + servletName + "]";
    }
}