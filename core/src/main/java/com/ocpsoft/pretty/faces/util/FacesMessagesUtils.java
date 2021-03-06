/*
 * Copyright 2010 Lincoln Baxter, III
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ocpsoft.pretty.faces.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * @author Lincoln Baxter, III <lincoln@ocpsoft.com>
 */
public class FacesMessagesUtils
{
    private static final String token = "com.ocpsoft.pretty.SAVED_FACES_MESSAGES";

    @SuppressWarnings("unchecked")
    public int saveMessages(final FacesContext facesContext, final Map<String, Object> destination)
    {
        int restoredCount = 0;
        if (FacesContext.getCurrentInstance() != null)
        {
            List<FacesMessage> messages = new ArrayList<FacesMessage>();
            for (Iterator<FacesMessage> iter = facesContext.getMessages(null); iter.hasNext();)
            {
                messages.add(iter.next());
                iter.remove();
            }

            if (messages.size() > 0)
            {
                List<FacesMessage> existingMessages = (List<FacesMessage>) destination.get(token);
                if (existingMessages != null)
                {
                    existingMessages.addAll(messages);
                }
                else
                {
                    destination.put(token, messages);
                }
                restoredCount = messages.size();
            }
        }
        return restoredCount;
    }

    @SuppressWarnings("unchecked")
    public int restoreMessages(final FacesContext facesContext, final Map<String, Object> source)
    {
        int restoredCount = 0;
        if (FacesContext.getCurrentInstance() != null)
        {
            List<FacesMessage> messages = (List<FacesMessage>) source.remove(token);

            if (messages == null)
            {
                return 0;
            }

            restoredCount = messages.size();
            for (Object element : messages)
            {
                facesContext.addMessage(null, (FacesMessage) element);
            }
        }
        return restoredCount;
    }
}