package com.ocpsoft.pretty.config;

/*
 * PrettyFaces is an OpenSource JSF library to create bookmarkable URLs.
 * 
 * Copyright (C) 2009 - Lincoln Baxter, III <lincoln@ocpsoft.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see the file COPYING.LESSER or visit the GNU
 * website at <http://www.gnu.org/licenses/>.
 */
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

/**
 * Parses Pretty Faces configuraion from an input stream.
 * 
 * @author Aleksei Valikov
 */
public interface PrettyConfigParser
{

    /**
     * Parses the input stream and feeds configuration elements to the builder.
     * 
     * @param builder
     *            target builder, must not be <code>null</code>.
     * @param resource
     *            input stream to be parsed, must not be <code>null</code>
     * @throws IOException
     *             If input stream could not be read.
     * @throws SAXException
     *             If configuration could not be parsed.
     */
    public void parse(PrettyConfigBuilder builder, InputStream resource) throws IOException, SAXException;
}