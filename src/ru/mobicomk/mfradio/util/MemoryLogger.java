/*
 *    Copyright (C) 2001 - 2007 Mobicom-Kavkaz, Inc
 *    MFRadio - stream radio client for Java 2 Micro Edition
 *    
 *    Visit the project page at: http://mfradio.sourceforge.net
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    Java (TM) and all Java (TM)-based marks are a trademark or 
 *    registered trademark of Sun Microsystems, Inc, in the United States 
 *    and other countries.
 */

package ru.mobicomk.mfradio.util;

import java.util.Date;

import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.iface.Logger;

/**
 * @author  Roman Bondarenko
 */
public class MemoryLogger 
    implements Logger {
    
    // Members /////////////////////////////////////////////////////////////////
    //

    protected static final int MAX_LOG_SIZE = 1024 * 3;
    
    protected StringBuffer logBuffer_;
    protected UIController controller_;
    
    
    // Public iface ////////////////////////////////////////////////////////////
    //
    
    public MemoryLogger(UIController controller) {
        controller_ = controller;
        logBuffer_ = new StringBuffer();
        addTime();
    }
    
    public String toString() {
        return logBuffer_.toString();
    }
    
    // Logger iface ////////////////////////////////////////////////////////////
    //

    public void log(String msg) {
        String thread = Thread.currentThread().toString();
        String logItem = "[" + thread.substring(14, thread.length() - 1) + "] " + msg;
        
        System.out.println(logItem);
     
        synchronized (logBuffer_) {
            logBuffer_.append(logItem + "\n");
        }
        
        checkSave();
    }

    public void log(Throwable t) {
        String thread = Thread.currentThread().toString();
        String logItem = "[" + thread.substring(14, thread.length() - 1) + "] " + t.getMessage();
        
        System.out.println(logItem);
        t.printStackTrace();
        
        synchronized (logBuffer_) {
            logBuffer_.append(logItem + "\n");
        }
        
        checkSave();              
    }
    
    public void flush () {
        logBuffer_.delete(0, logBuffer_.length());
    }
    
    
    // Privates ////////////////////////////////////////////////////////////////
    //

    protected void addTime() {
        Date now = new Date();
        logBuffer_.append("\n\n==== " + now.toString()+ "\n\n");
    }
    
    protected void checkSave() {
        if (logBuffer_.length() > MAX_LOG_SIZE) {
            synchronized (logBuffer_) {
                String log2Save = logBuffer_.toString();
                logBuffer_.delete(0, logBuffer_.length());
                addTime();
                logBuffer_.append(log2Save.substring((int)(MAX_LOG_SIZE / 3)));
                log2Save = null;
            }
        }
    }
}
