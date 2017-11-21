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

import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import ru.mobicomk.mfradio.controller.UIController;

/**
 * @author  Roman Bondarenko
 */
public class FileLogger 
    extends MemoryLogger
    implements Runnable {

    // Members /////////////////////////////////////////////////////////////////
    //
    
    //private static final String LOG_FILE_DIR = "file:///C:/other/";   
    private static final String LOG_DIR = "E:";
    private static final String EMUL_LOG_DIR = "root1";
    
    private String logFile_;    
    protected String log2Save_;        
    
    // Public iface ////////////////////////////////////////////////////////////
    //
    
    public FileLogger(UIController controller) {
        super(controller);
        logFile_ = "";
    }
    
   public void flush () {
       synchronized (logBuffer_) {
           log2Save_ = logBuffer_.toString();
           logBuffer_.delete(0, logBuffer_.length());
           addTime();
       }

       String logDir = LOG_DIR;
       if ("wtk-emulator".equals(System.getProperty("device.model"))) {
           logDir = EMUL_LOG_DIR;
       }

       logFile_ = "file:///" + logDir + "/mfr8-" + System.currentTimeMillis() + ".txt";
       Thread t = new Thread(this);
       t.start();
    }    
   
    public void run() {
        FileConnection fConn = null;
        OutputStream os = null;
        String err = null;
        try {
            
            fConn = (FileConnection) Connector.open(logFile_);
            if (!fConn.exists()) {
                fConn.create();
            }
            if (fConn.canWrite()) {
                //fConn.truncate(0);
                os = fConn.openOutputStream();
                os.write(log2Save_.getBytes());
            } else {
                err = "Can't write log " + logFile_;
            }
        } catch (SecurityException sex) {
            err = "SecurityException: " + sex.getMessage();
        } catch (IOException ioex) {
            err = "IOException: " + ioex.getMessage();
        } finally {
            if (os != null) {
                try { os.close();} catch (IOException ex) { ex.printStackTrace(); }
                os = null;
            }
            if (fConn != null) {
                try { fConn.close(); } catch (IOException ex) { ex.printStackTrace(); }
                fConn = null;
            }
        }
        if (err != null) {
            controller_.showError(err, controller_.getCurrDisplayable());
        }
        log2Save_ = "";
    }

    
    // Privates ////////////////////////////////////////////////////////////////
    //
    
    protected void checkSave() {
        if (logBuffer_.length() > MAX_LOG_SIZE) {
            flush();
        }
    }
    
}
