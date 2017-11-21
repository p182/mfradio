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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.controller.UIController;



/**
 * Socket-based downloader class. Used for access to audio stream data 
 * through socket connection.
 *
 * <p>For connection information (site name, port number, etc.) this class uses
 * URL of a resource. After establish connection ({@link #connect}) class uses
 * HTTP 1.1 protocol commands.</p>
 *
 * <p>Example of use:
 * 
 * <code><pre>
 * // ...
 * // create new instance 
 * Dloader dloader = new Dloader(controller); 
 *     // controller is an application controller object.
 * 
 * // connect
 * if (!dloader.connect(url)) {
 *     throw new IOException("Can't connect!");
 * }
 *
 * // read responce code
 * String[] resp = dloader.readResponceCode();
 * if (!"200".equals(resp[1])) {
 *    throw new IOException("HTTP response code " + resp[1] + " " + resp[2]);
 * }
 * 
 * // read headers for streamType
 * String streamType;
 * Hashtable headers = dloader.readHeaders();
 * if (headers.containsKey("content-type")) { 
 *     streamType = (String) headers.get("content-type");
 * } else {
 *     streamType = "unknown";
 * }
 * 
 * // read data
 * byte[] buffer = new byte[BUFFER_SIZE];
 * int startOffset = 0;
 * int bytesReaded = dloader.read(buffer, startOffset);
 * dloader.disconnect();
 * dloader = null;
 *
 * if (bytesReaded > 0) {
 *     // ...use data from buffer...
 * }
 * // ...
 * </pre></code>
 * </p>
 *
 * @author  Roman Bondarenko
 */
public class Dloader
    implements Runnable {

    /** 
     * Controller must setting up this flag to <b>true</b> for stop thread 
     * execution. 
     * 
     * <p>Example: <code>
     * <pre>
     * Dloader dloader = new Dloader(..);
     * // ...
     * dloader.StopFlag.set();
     * // ...
     * </pre></code></p>
     */
    public BooleanFlag StopFlag;
    
    private UIController controller_;
    private String site_; // "site:port"
    private String file_; // "file/name"
    
    private int buffIdx_;
    
    private StreamConnection conn_;
    private InputStream is_;
    private OutputStream os_;
    private DataInputStream dis_;    
    private DataOutputStream dos_;    
    private Object streamSync_;
    
    private static final int WAIT_SLEEPS_COUNT = 120;
    private static final int WAIT_SLEEP = 500; // msec
        // NOTE: wait timeout = (WAIT_SLEEPS_COUNT * WAIT_SLEEP) msec
    
    /**
     * Create and initialize new instance of the downloader class.
     * @param controller Application controller object. 
     */
    public Dloader(UIController controller) {
        controller_ = controller;
        streamSync_ = new Object();
        StopFlag = new BooleanFlag(false);
        buffIdx_ = 1;
    }
    
    /**
     * Get downloader connection state.
     * @return <b>true</b> if the downloader currenly connected to resource, 
     * <b>false</b> otherwise.
     */
    public boolean isConnected() {
        return (null != dis_);
    }
    
    /**
     * Re-connect to current resource.
     * @throws java.lang.InterruptedException if user breaks download operation. 
     * @throws ru.mobicomk.mfradio.util.AppException if site name or file name not 
     * specified or is empty.
     * @return <b>true</b> if connected, <b>false</b> otherwise.
     */
    public boolean reconnect() throws InterruptedException, AppException {
        disconnect();
        return connect();
    }
    
    /**
     * Connect to a given resource.
     * @param url URL of a resource to connection.
     * @throws java.lang.InterruptedException if user breaks download operation. 
     * @throws ru.mobicomk.mfradio.util.AppException if site name or file name not 
     * specified or is empty.
     * @throws ru.mobicomk.mfradio.util.UrlFormatException if error during URL parsing.
     * @return <b>true</b> if connected, <b>false</b> otherwise.
     */
    public boolean connect(String url) throws InterruptedException, AppException, UrlFormatException {
        String[] urlParts = Utils.splitURL(url);
        site_ = urlParts[1] + ":" + urlParts[2]; // host:port
        urlParts[0] = "";
        urlParts[1] = "";
        urlParts[2] = "";
        file_ = Utils.mergeURL(urlParts);
        return connect();
    }
    
    /**
     * Thread-safe breaking of the current connection. 
     */
    public void disconnect() {
        synchronized (streamSync_) {
            // free up I/O streams and close the socket connection
            try { if (dis_ != null) dis_.close();  dis_ = null; } catch (Exception ignored) {}
            try { if (dos_ != null) dos_.close();  dos_ = null; } catch (Exception ignored) {}
            try { if (os_ != null) os_.close();     os_ = null; } catch (Exception ignored) {}
            try { if (is_ != null) is_.close();     is_ = null; } catch (Exception ignored) {}
            try { if (conn_ != null) conn_.close(); conn_ = null; } catch (Exception ignored) {}
        }
    }

    /**
     * Read resource data to the buffer.
     *
     * <p><b>NOTE:</b> buffer.length must be divisible by chunkSize.</p>
     *
     * @param buffer buffer into which the data is read.
     * @param startOffset The start offset of the data.
     * @throws java.io.IOException if I/O error occurs.
     * @throws java.lang.InterruptedException if user breaks download process.
     * @return Readed bytes count, or -1 if there is no more data because the 
     * end of the stream has been reached.
     * @see java.io.DataInputStream
     * @see java.io.DataInputStream#read
     */
    public int read(byte[] buffer, int startOffset) throws IOException, InterruptedException {
        synchronized (streamSync_) {
            int progress = startOffset;
            int chunkSize = 512;
            int length = 0;
            int stepsFor10Percents = ((buffer.length - startOffset) / chunkSize) / 10;
            int step = 0;
            long beginTime = System.currentTimeMillis();
            long kbps = 0;
            long percents = 0;
            long deltaTime = 0;

            final Locale locale = controller_.getLocale();
	    try {
                if (dis_ != null && buffer.length > 0 && !StopFlag.value()) {
                    //controller_.log("read >> start dload " + buffer.length + " byte(s)");
                    do {
                        if ((progress + chunkSize) > buffer.length) {
                            chunkSize = buffer.length - progress;
                        }
                        ////controller_.log("read >> progress: " + progress + "; chunkSize: " + chunkSize);
                        length = dis_.read(buffer, progress, chunkSize);
                        ////controller_.log("read >> readed: " + length);
                        if (length == -1) {
                            break;
                        }
                        
                        progress += length;
                        step++;
                        //controller_.log("read >> progress: " + progress);
                        if (step == stepsFor10Percents) {
                            if (buffer.length > 0) {
                                kbps = 0;
                                percents = 0;
                                deltaTime = System.currentTimeMillis() - beginTime;
                                if (deltaTime > 0) {
                                    //kbps = ((progress - startOffset) / 128) / (deltaTime / 1000);
                                    kbps = ((progress - startOffset) * 125) / (16 * deltaTime);
                                    percents = (100 * progress) / buffer.length;
                                    
                                    /*
                                    controller_.progressMessage(controller_.getLocale.getString(Constants.STR_Buffer)
                                        + " " + buffIdx_ + ": " + percents + "% "
                                        +controller_.getLocale()e.getString(Constants.STR_Speed + " " + kbps + "kbps");
                                    */
                                    
                                    controller_.progressMessage(locale.getString(Constants.STR_Buffer)
                                        + " " + buffIdx_ + ": " + percents + "% / "
                                        + kbps + "kbps");
                                }
                            } else {
                                controller_.progressMessage(locale.getString(Constants.STR_Buffer)
                                        + " " + buffIdx_ + ": 0%");
                            }
                            controller_.updateProgress();
                            step = 0;
                        }
                    } while(/*(length != -1) &&*/ (progress < buffer.length) && !StopFlag.value());
                    buffIdx_ = (buffIdx_ == 1) ? 2 : 1;
                }
            } catch (IOException ex) {
                //controller_.log("read >> " + ex.toString() + " >> bufLen: "+buffer.length+"; length: " + length + "; progress: " + progress + "; chunk: " + chunkSize);
            }

            //controller_.log("read >> finish dload. dload "+progress+" byte(s)");
            controller_.progressMessage(locale.getString(Constants.STR_Prefetched));
            return (progress - startOffset);
        }
    }

    /**
     * Get HTTP-response code for previous HTTP-request.
     *
     * <p>Using:
     * <code><pre>
     * // ...
     * // read responce code
     * String[] resp = dloader.readResponceCode();
     * if (!"200".equals(resp[1])) {
     *    throw new IOException("HTTP response code " + resp[1] + " " + resp[2]);
     * }
     * </pre></code>
     * </p>
     *
     * @throws java.io.IOException if I/O error occurs.
     * @return String array with response code and text representation of the 
     * response. 
     * <p>Structure of the result (example is <code>"HTTP/1.0 200 OK"</code>) :
     * <table border="1"><tr><td>Index</td><td>Description</td><td>Example</td></tr><tr><td>
     * 0  </td><td>  Protocol                         </td><td><code>  HTTP/1.1 </code></td></tr></tr><tr><td>
     * 1  </td><td>  Response code                    </td><td><code>  200      </code></td></tr></tr><tr><td>
     * 2  </td><td>  Text description of the response </td><td><code>  OK       </code></td></tr></tr>
     * </table>
     * </p>
     */
    public String[] readResponseCode() throws IOException {
        String[] ret = new String[3];
        String line = readLine();
        
        int first = 0;
        int last = line.indexOf(' ');
        int idx = 0;
        
        while (last != -1 && idx < 3) {
            ret[idx] = line.substring(first, last);
            first = last + 1;
            last = line.indexOf(' ', first);
            idx++;
        }
        
        return ret;
    }

    /**
     * Get all HTTP-response headers.
     *
     * <p>Using:
     * <code><pre>
     * // ...
     * // read headers for streamType
     * String streamType;
     * Hashtable headers = dloader.readHeaders();
     * if (headers.containsKey("content-type")) {
     *     streamType = (String) headers.get("content-type");
     * } else {
     *     streamType = "unknown";
     * }
     * // ...
     * </pre></code>
     * </p>
     *
     * @throws java.io.IOException  if I/O error occurs.
     * @return Hashtable with all headers: keys is a header names, 
     * values is a headers values.
     * <p>Structure of the result :
     * <table border="1"><tr><td>Key</td><td>Value</td></tr><tr><td>
     * content-type  </td><td>  audio/mpeg  </td></tr></tr><tr><td>
     * icy-name  </td><td>  Sample station  </td></tr></tr><tr><td>
     * icy-genre </td><td>  all  </td></tr></tr><tr><td>
     * ...  </td><td>  ... </td></tr></tr>
     * </table>
     * </p>
     *
     * @see java.util.Hashtable
     */
    public Hashtable readHeaders() throws IOException {
        String key;
        String val;
        int idx = -1;
        Hashtable hash = new Hashtable(10);
        
        String line = readLine();
        while (!"".equals(line)) {
            idx = line.indexOf(':');
            key = line.substring(0, idx).trim().toLowerCase();
            val = line.substring(idx + 1).trim();
            hash.put(key, val);
            line = readLine();
        }
        
        return hash;
    }
    
    // Runnable iplementation //////////////////////////////////////////////////
    
    /** 
     * {@link Runnable} interface implementation. Entry point of the dowloader 
     * thread.
     * @see java.lang.Runnable
     * @see java.lang.Runnable#run
     */
    public void run() {
        _connect();
    }
    
    // Privates ////////////////////////////////////////////////////////////////
    
    /*
     * Connect to specified resource.
     * @throws java.lang.InterruptedException if user breaks download operation. 
     * @throws megafon.sap.one.util.AppException if site name or file name not 
     * specified or is empty.
     * @return 
     */
    private boolean connect() throws InterruptedException, AppException {
        final Locale locale = controller_.getLocale();
	if (site_ == null 
            || site_.length() == 0 
            || file_ == null 
            || file_.length() == 0) {
            throw new AppException(locale.getString(Constants.STR_Invalid_agument));
        }
        if (is_ != null) {
            disconnect();
        }
        
        //controller_.log("connect >> start connection thread...");
        Thread t = new Thread(this);
        //t.setPriority(Thread.MAX_PRIORITY);
        t.start(); // connect

        //controller_.log("connect >> wait for connection...");
        try {
            for (int step = 0; step < WAIT_SLEEPS_COUNT; step++) {
                if (isConnected() || !t.isAlive()) {
                    break;
                }
                controller_.updateProgress(); // exception
                Thread.sleep(WAIT_SLEEP);
            }
        } catch (InterruptedException ex) {
            //controller_.progressMessage("interrupt - 1");
            //t.interrupt(); // WARNING: CLDC 1.1
            
            // for CLDC 1.0
            int step = 0;
            controller_.progressMessage(locale.getString(Constants.STR_Stop));            
            while (t.isAlive() && (step < 3)) {
                controller_.updateProgress(); // exception
                Thread.sleep(1000); // sleep 1 second
                step++;
            }
            
            t = null;
            disconnect();
        }
        
        return isConnected();
    }
    
    private void _connect() {
        synchronized (streamSync_) {
            try {
                //controller_.progressMessage("sc://" + site_ + file_);

                // establish a socket connection with remote server
                conn_ = (StreamConnection)Connector.open("socket://" + site_);

                // create DataOuputStream on top of the socket connection
                os_ = conn_.openOutputStream();
                dos_ = new DataOutputStream(os_);
                
                // send the HTTP request
                String req = "GET "+file_+" HTTP/1.1\r\nUser-Agent: Profile/MIDP-1.0 Configuration/CLDC-1.0\r\n\r\n";
                dos_.write(req.getBytes());
                // dos_.writeChars("GET /" + file_ + "\n");
                dos_.flush();
                
                // create DataInputStream on top of the socket connection
                is_ = conn_.openInputStream();
                dis_ = new DataInputStream(is_);

                //controller_.progressMessage("connected!");
                
            } catch (Exception ex) {
                //controller_.progressMessage("sc:ex:"+ex.getMessage());
                disconnect();
            }
        }
    }
    
    private String readLine() throws IOException {
        StringBuffer sb = new StringBuffer();
        int ch;
        char waitFor = '\r';
        
        while ((ch = dis_.read()) != -1) {
            if ('\r' == (char)ch) {
                waitFor = '\n';
                continue; // skip char
            } else if (waitFor == (char)ch) {
                break;
            }
            sb.append((char)ch);
        }
        return sb.toString();
    }

}
