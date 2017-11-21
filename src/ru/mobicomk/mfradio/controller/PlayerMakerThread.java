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

package ru.mobicomk.mfradio.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.iface.PlayerQueue;
import ru.mobicomk.mfradio.util.BooleanFlag;
import ru.mobicomk.mfradio.util.Dloader;
import ru.mobicomk.mfradio.util.Locale;
import ru.mobicomk.mfradio.util.PlayerFactory;

/**
 * Player maker thread class.
 * <p>Download next chunk of the audio stream data and makes new instance of a 
 * Player object. Then append it to the Player's queue.</p>
 *
 * @author  Roman Bondarenko
 * @see Dloader
 * @see UIController
 * @see PlayerQueue
 */
class PlayerMakerThread extends Thread {
    
    /**
     * Controller must setting up this flag to <b>true</b> for stop thread
     * execution.
     */
    BooleanFlag StopFlag;
    
    //private static final int BUFFER_SIZE = 12288; // 2sec/48kbps
    //private static final int BUFFER_SIZE = 24576; // 4sec/48kbps
    private static final int BUFFER_SIZE = 49152; // 8sec/48kbps
    
    private static final int OFFSET_SIZE = 0/*512*/; // 1/6sec@24kbps
        // set OFFSET_SIZE > 0 if you want to make Players with overlapped
    
    private byte[] buffer_ ;
    
    private Hashtable headers_;
    private String streamType_;
    private UIController controller_;
    private Dloader dloader_;
    private PlayerListener listener_;
    private PlayerQueue queue_;
    
    
    /**
     * Creates a new instance of PlayerMakerThread
     * @param controller Application controller object.
     */
    public PlayerMakerThread(UIController controller){
        controller_ = controller;
        headers_ = null;
    }
    
    /**
     * Stop thread helper.
     */
    public void stop() {
        StopFlag.set();
        if (dloader_ != null) {
            dloader_.StopFlag.set();
        }
    }
    
    /**
     * Thread entry point.
     * @see java.lang.Thread
     * @see java.lang.Thread#run
     */
    public void run() {
        //controller_.log("maker >> start work!");
        
        int readed = 0;
        int startOffset = 0;
        InputStream is = null;
        Player p = null;
        int noDataStep = 0;
        int noConnectionStep = 0;
        boolean threadIsBreaked = false;
        final Locale locale = controller_.getLocale();
        
        try {
            while (!StopFlag.value()) {
                
                // read data
                controller_.progressMessage(locale.getString(Constants.STR_Prefetching));
                //controller_.log("maker >> try to read data...");
                
                if (startOffset > 0) {
                    is = new ByteArrayInputStream(buffer_, readed - startOffset, startOffset);
                    is.read(buffer_, 0, startOffset);
                }
                
                readed = dloader_.read(buffer_, startOffset);  // exception
                
                if (StopFlag.value()) {
                    break;
                }
                
                if (readed < 1){
                    controller_.progressMessage(locale.getString(Constants.STR_Try_again));
                    //controller_.log("maker >> no data readed! go next step.");
                    Thread.yield();
                    //Thread.sleep(500);
                    noDataStep++;
                    startOffset = 0;
                    if (noDataStep > 5) {
                        //controller_.log("maker >> " + noDataStep + " step(s) without data... goodbye!");
                        break; 
                    }
                    if (noDataStep == 3) {
                        try {
                            //controller_.log("maker >> " + noDataStep + " step(s) without data... try to reconnect.");
                            controller_.progressMessage(locale.getString(Constants.STR_Reconnecting));
                            if (!dloader_.reconnect()) {
                                controller_.progressMessage(locale.getString(Constants.STR_Cant_connect));
                                break; // stop this thread
                            }
                        } catch (InterruptedException ex) {
                            //controller_.log("maker (reconnect) >> InterruptedException >> " + ex.getMessage());
                            break; // stop this thread
                        }
                    }
                    continue; // try again
                }
                
                try {
                    //controller_.log("maker >> data readed (size: "+readed+")");
                    noDataStep = 0;
                    is = new ByteArrayInputStream(buffer_, 0, readed + startOffset);
                    //controller_.log("maker >> try to make player...");
                    p = PlayerFactory.createPlayer(is, streamType_, listener_);
                    
                    //controller_.log("maker >> player is created!");
                    queue_.pushTail(p);
                    startOffset = OFFSET_SIZE; //3072; // 24kbps - 1 sec
                    //controller_.log("maker >> OK! player in queue");
                    
                } catch (Exception ex) {
                    //controller_.log("maker >> " + ex.toString());
                    closeInputStream(is);
                    if (p != null) {
                        p.close();
                        p = null;
                    }
                }
            }
        } catch (InterruptedException ex) {
            //controller_.log("maker >> " + ex.toString());
            StopFlag.set();
        } catch (Exception ex) {
            //controller_.log("maker >> " + ex.toString());
        }
        
        closeInputStream(is);
        
        dloader_.disconnect();
        dloader_ = null;
        buffer_ = null;
        
        System.gc();
        
        if (!StopFlag.value()) {
            StopFlag.set();
            controller_.makerIsInterrupted(); // thread is breaked by exception!
        } else {
            controller_.makerIsStopped();
        }
        //controller_.log("maker >> finish work!");
    }
    
    private void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
                is = null;
            } catch (IOException ioex) {
                //controller_.log("closeInputStream >> IOException >> " + ioex.getMessage());
            }
        }
        headers_ = null;
    }
    
    /**
     * Second level initialization.
     * @param url URL of the audio stream.
     * @param queue Player queue object.
     * @param listener Player listener object.
     * @throws java.lang.Exception if any error (IO, HTTP responce code not
     * equals 200, unsupported stream format).
     */
    public void init(String url, PlayerQueue queue, PlayerListener listener)
    throws Exception {
        
        try {
            controller_.updateProgress();
            final Locale locale = controller_.getLocale();
            
            StopFlag = new BooleanFlag(false);
            listener_ = listener;
            queue_ = queue;
            
            controller_.updateProgress();
            dloader_ = new Dloader(controller_);
            
            controller_.progressMessage(locale.getString(Constants.STR_Connecting));
            controller_.updateProgress();
            
            // connect
            if (!dloader_.connect(url)) {
                throw new IOException(locale.getString(Constants.STR_Cant_connect));
            }
            
            // read responce code
            String[] resp = dloader_.readResponseCode();
            if (!"200".equals(resp[1])) {
                throw new IOException(locale.getString(Constants.STR_HTTP_response_code) + resp[1] + " " + resp[2]);
            }
            
            // read headers for streamType
            headers_ = dloader_.readHeaders();
            
            if (headers_.containsKey("content-type")) {
                streamType_ = (String) headers_.get("content-type");
            } else {
                if (url.endsWith(".mp3")) {
                    streamType_ = "audio/mp3";
                } else if (url.endsWith(".wav")) {
                    streamType_ = "audio/x-wav";
                } else {
                    streamType_ = "audio/mpeg"; // default type
                    //throw new MediaException("Unknown audio type!");
                }
            }
            
            // check for supported type
            if (!isSupported("http", streamType_)) {
                if (streamType_.equals("audio/mpeg") || streamType_.equals("audio/mp3")) {
                    if (isSupported("http", "audio/mpeg3")) {
                        streamType_ = "audio/mpeg3";
                    } else {
                        throw new MediaException(locale.getString(Constants.STR_Unsupported_content_type_BEGIN
                            + streamType_ + locale.getString(Constants.STR_Unsupported_content_type_END)));
                    }
                } else if (streamType_.equals("audio/mpeg3")) {
                    if (isSupported("http", "audio/mpeg")) {
                        streamType_ = "audio/mpeg";
                    } else if (isSupported("http", "audio/mp3")) {
                        streamType_ = "audio/mp3";
                    } else {
                        throw new MediaException(locale.getString(Constants.STR_Unsupported_content_type_BEGIN
                            + streamType_ + locale.getString(Constants.STR_Unsupported_content_type_END)));
                    }
                } else {
                    throw new MediaException(locale.getString(Constants.STR_Unsupported_content_type_BEGIN
                        + streamType_ + locale.getString(Constants.STR_Unsupported_content_type_END)));
                }
            }
            
            //if (url.startsWith("http://test.local")) {
            //    buffer_ = new byte[90000];
            //} else {
            buffer_ = new byte[BUFFER_SIZE];
            //}
        } catch (Exception ex) {
            //controller_.progressMessage("interrupt - 2.1");
            dloader_.disconnect();
            dloader_ = null;
            throw ex;
        }
    }
    
    /**
     * HTTP responce headers accessor.
     * @return HTTP responce headers hash table.
     */
    public Hashtable getHeaders() {
        return headers_;
    }
    
    /*
     *
     */
    private static boolean isSupported(String proto, String streamType) {
        boolean isSupported = false;
        String[] protocols = javax.microedition.media.Manager.getSupportedProtocols(streamType);
        for (int i=0; i < protocols.length; i++) {
            if (protocols[i].toLowerCase().equals(proto)) {
                isSupported = true;
                break;
            }
        }
        return isSupported;
    }
}
