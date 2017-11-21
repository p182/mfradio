/*
 *    MFRadio - stream radio client for Java 2 Micro Edition
 *    Copyright (C) 2001 - 2007 Mobicom-Kavkaz, Inc
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

package ru.mobicomk.mfradio.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.iface.ContentHandler;
import ru.mobicomk.mfradio.util.Locale;
import ru.mobicomk.mfradio.util.Utils;


/**
 * Simple WAP/HTML link extractor and browser.
 *
 * <p>This browser gets WAP/HTML page from given location and make list of 
 * links extracted from this page, then display it.</p>
 *
 * <p>This browser recognize links to <i>MP3</i> or <i>M3U</i> files and 
 * links to other WAP/HTML pages. Other links recognized as unknown.</p>
 *
 * <p>User can selects other pages (by pressing to WAP/HTML pages links). Link 
 * to <i>MP3</i> or <i>M3U</i> file will be added to application's playlist if
 * user selects it.</p>
 *
 * @author  Roman Bondarenko
 */
public class SimpleWapBrowserUI extends List implements CommandListener, ContentHandler {
    
    private UIController uiController_;
    private List menuList_;

    private Vector names_   = new Vector();
    private Vector urls_    = new Vector();
    private Stack history_  = new Stack();
    private Stack historyIndex_ = new Stack();
    private String currURL_ = null;
    
    private String queryDefault_;
    private String queryInputURL_;

    // Commands
    private Command exitCommand_;
    private Command backCommand_;
    private Command openCommand_;
    private Command selectCommand_;
    private Command saveCommand_;
    private Command menuCommand_;
    private Command refreshCommand_;
    private Command helpCommand_;
    
    private Command allCommands_[] = {
        exitCommand_
            , backCommand_
            , openCommand_
            , selectCommand_
            , saveCommand_
            , menuCommand_
            , refreshCommand_
            , helpCommand_
    };
    
    // UI components
    private static final int ICON_LINK                  = 0;
    private static final int ICON_NOT_LINK              = 1;
    private static final int ICON_UNKNOWN               = 2;

    private static final String[] iconPaths_ = { 
          "/i/doc.png"          // 0
        , "/i/doc3.png"    // 1
        , "/i/unknown.png"      // 2
    };
    private Image[] icons_ = new Image[iconPaths_.length];

    /**
     * Create and initialize new browser object.
     * @param title Browser title.
     * @param uiController Application controller object.
     */
    public SimpleWapBrowserUI(String title, UIController uiController) {
        super(title, Choice.IMPLICIT);
        uiController_ = uiController;
        
        initUI();
        initCommands();
    }

    /**
     * Displays links list or page from given location.
     * @param url URL of the WAP/HTML page.
     * @param selectedIndex Index of selected link.
     * @see ru.mobicomk.mfradio.ui.SimpleWapBrowserUI
     */
    public void displayHTML(final String url, final int selectedIndex) {
        uiController_.beginWaitScreenRequested(uiController_.getLocale().getString(Constants.STR_Loading), true);
        
        new Thread(new Runnable() {
            public void run() {
                displayHTML_(url, selectedIndex);
            }
        }).start();
    }
    
    // interface ContentHandler ////////////////////////////////////////////////
    
    /**
     * {@link ContentHandler} interface implementation. Free all alloicated 
     * resources.
     * @see ru.mobicomk.mfradio.iface.ContentHandler
     * @see ru.mobicomk.mfradio.iface.ContentHandler#close
     */
    public void close() {
        clearLists();
        history_.setSize(0);
        historyIndex_.setSize(0);
    }
    
    /**
     * {@link ContentHandler} interface implementation. 
     * @param url The link to resource.
     * @return <b>true</b> if given URL points to HTML or WAP page, 
     * <b>false</b> othrwise.
     * @see ru.mobicomk.mfradio.iface.ContentHandler
     * @see ru.mobicomk.mfradio.iface.ContentHandler#canHandle
     */
    public boolean canHandle(String url) {
        return isHTMLorWML(url);
    }
    
    /**
     * {@link ContentHandler} interface implementation. Get and display page 
     * referenced by given URL.
     * @param name Link title. (NOTE: Ignored in this case).
     * @param url The link to resource.
     * @see ru.mobicomk.mfradio.iface.ContentHandler
     * @see ru.mobicomk.mfradio.iface.ContentHandler#handle
     */
    public void handle(String name, String url) {
        displayHTML(url, 0);
    }
    
    // CommandListener implementation //////////////////////////////////////////
    
    /**
     * {@link CommandListener} interface implementation. Respond to commands.
     * @see javax.microedition.lcdui.CommandListener
     * @see javax.microedition.lcdui.CommandListener#commandAction
     */
    public void commandAction(Command c, Displayable s) {
        try {
            // respond to menu items
            if ((s == menuList_) && (menuList_ != null) && menuList_.isShown()) {
                int selIndex = menuList_.getSelectedIndex();
                uiController_.replaceCurrent(this);
                if (c == backCommand_) {
                    return;
                }
                if (c == List.SELECT_COMMAND || c == selectCommand_) {
                    c = menuItem2Command(selIndex);
                    // fall through - the commands will then be handled as if
                    // they came from the main example.list
                }
            }
            
            if ((c == List.SELECT_COMMAND && isShown()) || (c == openCommand_)) {
                gotoURL(getSelectedIndex());
            } else if (c == saveCommand_) {
                saveToPlaylist(getSelectedIndex());
            } else if (c == backCommand_) {
                goBack();
            } else if (c == refreshCommand_) {
                refresh();
            } else if (c == menuCommand_) {
                showMenu(getSelectedIndex());
            } else if (c == exitCommand_) {
                exit();
            } else if (c == helpCommand_) {
                help();
            }
        } catch (Throwable t) {
            internalError(t, "in commandAction");
        }
    }

    // Privates ////////////////////////////////////////////////////////////////
    
    private void displayHTML_(final String url, final int selectedIndex) {
	final Locale locale = uiController_.getLocale();
        try {
            
            uiController_.progressMessage(locale.getString(Constants.STR_Clearing));
            uiController_.updateProgress(); // exception
            boolean error = false;
            clearLists();
            currURL_ = url;
            
            uiController_.progressMessage(locale.getString(Constants.STR_Connecting));
            uiController_.updateProgress(); // exception
            
            try {
                readHTML(url);
            } catch (Exception e) {
                append("["+Utils.friendlyException(e)+"]", null);
                //uiController_.log("displayHTML >> " + e.toString());
                error = true;
            }
            
            uiController_.updateProgress(); // exception
            
            addCommand(backCommand_);
            setCommandListener(this);
            
            String url0;
            String name0;
            int iconIdx;
            
            if (!error) {
                uiController_.progressMessage(locale.getString(Constants.STR_Rendering));
                for (int i=0; i<names_.size(); i++) {
                    url0 = (String)urls_.elementAt(i);
                    
                    iconIdx = ICON_UNKNOWN;
                    if (isHTMLorWML(url0)) {
                        iconIdx = ICON_LINK;
                    } else if (uiController_.canHandleLink(url0)) {
                        iconIdx = ICON_NOT_LINK;
                    }
                    
                    append((String) names_.elementAt(i), icons_[iconIdx]);
                    uiController_.updateProgress(); // exception
                }
                setListIndex(selectedIndex);
                addCommand(menuCommand_);
            } else {
                addCommand(refreshCommand_);
            }
            uiController_.updateProgress();
        } catch (InterruptedException ex) {
            int sel = append("["+ex.getMessage()+"]", null);
            setListIndex(sel);

            removeCommand(menuCommand_);
            addCommand(refreshCommand_);
            addCommand(backCommand_);
            setCommandListener(this);
        } finally {
            uiController_.endWaitScreenRequested(this);
        }
    }
    
    private void initCommands() {
	final Locale locale  = uiController_.getLocale();
	exitCommand_    = new Command(locale.getString(Constants.STR_Exit_Browser), Command.EXIT, 1);
	backCommand_    = new Command(locale.getString(Constants.STR_Back), Command.BACK, 1);
	openCommand_    = new Command(locale.getString(Constants.STR_Open), Command.ITEM, 1);
	selectCommand_  = new Command(locale.getString(Constants.STR_Select), Command.ITEM, 1);
	saveCommand_    = new Command(locale.getString(Constants.STR_Save_in_playlist), Command.ITEM, 1);
	menuCommand_    = new Command(locale.getString(Constants.STR_Menu), Command.ITEM, 1);
        refreshCommand_ = new Command(locale.getString(Constants.STR_Refresh), Command.ITEM, 1);
    }
    
    private void initUI() {
        for (int i = 0; i < iconPaths_.length; i++) {
            try {
                icons_[i] = Image.createImage(iconPaths_[i]);
            } catch (IOException ioe) { 
            
            }
        }
    }
    
    private void exit() {
        currURL_ = null;
        clearLists();
        uiController_.exitWapBrowserRequested();
    }

    private void help() {
        uiController_.showHelp(HelpUI.HELP_WAPBROWSERUI_TOPIC);
    }
    
    private static void internalError(Throwable t, String desc) {
        // TODO: add log entry
    }
    
    private void clearLists() {
        names_.setSize(0);
        urls_.setSize(0);
        
        deleteAll();
        
        removeCommand(refreshCommand_);
        removeCommand(menuCommand_);
        
        System.gc();
    }
    
    private Command menuItem2Command(int index) {
        // go through all commands and test if their label
        // matches this menuList_ item's string
        if ((index < 0) || (index >= menuList_.size())) {
            return null;
        }
        String menuStr = menuList_.getString(index);
        for (int i = 0; i < allCommands_.length; i++) {
            if (allCommands_[i].getLabel().equals(menuStr)) {
                return allCommands_[i];
            }
        }
        return null;
    }
    
    private void showMenu(int index) {
        String url=(String) urls_.elementAt(index);
        boolean html = isHTMLorWML(url);
        
        menuList_ = new List(uiController_.getLocale().getString(Constants.STR_Menu), Choice.IMPLICIT);
        menuList_.setCommandListener(this);
        
        if (html) {
            menuList_.append(openCommand_.getLabel(), null);
        } else {
            menuList_.append(saveCommand_.getLabel(), null);
        }
        menuList_.append(refreshCommand_.getLabel(), null);
        menuList_.append(helpCommand_.getLabel(), null);
        menuList_.append(exitCommand_.getLabel(), null);
        
        menuList_.addCommand(backCommand_);
        menuList_.addCommand(selectCommand_);
        
        uiController_.replaceCurrent(menuList_);
    }
    
    private void gotoURL(int index) {
        String url = (String) urls_.elementAt(index);
        gotoURL(url, index);
    }
    
    private void gotoURL(String url, int index) {
        try {
            if (index >= names_.size() || isHTMLorWML(url)) {
                if (currURL_ != null) {
                    history_.push(currURL_);
                    historyIndex_.push(new Integer(index));
                }
                displayHTML(url, 0);
            } else {
                saveToPlaylist(index);
            }
        } catch (Exception e) {
            uiController_.showError(Utils.friendlyException(e), this);
        }
        
        /*
        if (Utils.DEBUG) {
            Utils.debugOut("SimpleHttpBrowser: after gotoURL. History contains "+history_.size()+" entries.");
            for (int i = history_.size()-1; i>=0; i--) {
                Utils.debugOut("     "+i+": "+((String) history_.elementAt(i)));
            }
        }
        */
    }
    
    private void goBack() {
        /*
        if (Utils.DEBUG) {
            Utils.debugOut("SimpleHttpBrowser: before goBack. History contains "+history_.size()+" entries.");
            for (int i = history_.size()-1; i>=0; i--) {
                Utils.debugOut("     "+i+": "+((String) history_.elementAt(i))+"  #"+((Integer) historyIndex_.elementAt(i)));
            }
        }
        */
        
        if (!history_.empty()) {
            String url = (String) history_.pop();
            int index = ((Integer) historyIndex_.pop()).intValue();
            displayHTML(url, index);
        } else {
            exit();
        }
    }
    
    private void refresh() {
        int selIndex = getSelectedIndex();
        // Utils.debugOut("SimpleHttpBrowser.Refresh: index "+selIndex);
        displayHTML(currURL_, selIndex);
    }
    
    // somehow this doesn't work if there was a screen switch before !
    private void setListIndex(int index) {
        if (index>=0 && index<size()) {
            setSelectedIndex(index, true);
        }
    }
    
    /*
     * main parsing function
     */
    private void readHTML(String source) throws Exception {
	final Locale locale  = uiController_.getLocale();
        uiController_.updateProgress();    
        //uiController_.log("readHTML >> try to open connection");
        
        InputStream is0=Connector.openInputStream(source);
        InputStreamReader in = null;
        
        uiController_.progressMessage(locale.getString(Constants.STR_Reading));        
        uiController_.updateProgress();    
    
        try {
            in = new InputStreamReader(is0, "UTF-8");            
        } catch (UnsupportedEncodingException ex) {
            //uiController_.log("readString >> UTF-8: " + ex.toString());
            try {
                in = new InputStreamReader(is0, "UTF8");
            } catch (UnsupportedEncodingException ex1) {
                //uiController_.log("readString >> UTF8: " + ex1.toString());
                in = new InputStreamReader(is0);
            }
        }
        
        //uiController_.log("readHTML >> connection opened!");
        uiController_.updateProgress();
        try {
            String url;
            String name;
            String[] base=Utils.splitURL(source);
            
            //uiController_.log("readHTML >> source="+Utils.mergeURL(base));
            
            while ((url = readHref(in)) != null) {
                //uiController_.log("readHTML >> url="+url);
                
                uiController_.updateProgress();
                //uiController_.log("readHTML >> join with base");
                String[] splitU = joinURLs(base, url);
                
                //uiController_.log("readHTML >> merge");
                url = Utils.mergeURL(splitU);
                
                //uiController_.log("readHTML >> url="+url);
                
                // do not include those sort links in file listings.
                if (splitU[4].indexOf('?')!=0) {
                    name = readHrefName(in);
                    
                    //uiController_.log("readHTML >> Read name=\""+name+"\" with url=\""+url+"\"");
                    
                    names_.addElement(name);
                    urls_.addElement(url);
                }
            }
            
            if (names_.size()==0) {
                throw new Exception(locale.getString(Constants.STR_No_links_found + source));
            }
        } catch (Exception ex) {
            //uiController_.log("readHTML >> " + ex.toString());
            in.close();
            in = null;
            throw ex;
        } finally {
            //uiController_.log("readHTML >> close IS");
            if (in != null) {
                in.close();
                in = null;
            }
        }
        
    }
    
    // URL methods /////////////////////////////////////////////////////////////
    
    private static boolean isHTMLorWML(String url) {
        try {
            String[] sURL = Utils.splitURL(url);
            return sURL[0].equals("http")
            && (sURL[4]=="" // no filename part
                || sURL[4].indexOf(".") == -1
                || sURL[4].indexOf(".wmlc") == sURL[4].length()-5
                || sURL[4].indexOf(".wml") == sURL[4].length()-4
                || sURL[4].indexOf(".html") == sURL[4].length()-5
                || sURL[4].indexOf(".htm") == sURL[4].length()-4);
        } catch (Exception e) {
            internalError(e, "isHTMLorWML()");
            return false;
        }
    }
    
    private String[] joinURLs(String[] url, String relPath) throws Exception {
        String[] rel=Utils.splitURL(relPath);
        String[] result=new String[6];
        
        result[0]=(rel[0]=="")?url[0]:rel[0];
        result[1]=(rel[1]=="")?url[1]:rel[1];
        result[2]=(rel[2]=="")?url[2]:rel[2];
        
        if (rel[3].length()>0) {
            if (rel[3].charAt(0)=='/') {
                // absolute path given
                result[3]=rel[3];
            } else {
                result[3]=url[3]+'/'+rel[3];
            }
        } else {
            result[3]=url[3];
        }
        
        result[4]=(rel[4]=="")?url[4]:rel[4];
        result[5]=(rel[5]=="")?url[5]:rel[5];
        
        return result;
    }
    
    
    // beware: highly optimized HTML parsing code ahead !
    
    private boolean charEquals(char char1, char char2, boolean caseSensitive) {
        boolean equal=(char1==char2);
        if (!equal && !caseSensitive
            && ((char1>=0x41 && char1<=0x5A) || (char1>=0x41 && char1<=0x5A))) {
            equal=((char1^0x20)==char2);
        }
        return equal;
    }
    
    
    private boolean skip(InputStreamReader is, String until, boolean onlyWhiteSpace, boolean caseSensitive) throws Exception {
	final Locale locale  = uiController_.getLocale();
        //if (TRACE) System.out.println("skip(is, \""+until+"\", onlyWhiteSpace="+onlyWhiteSpace+", caseSensitive="+caseSensitive+")");
        int len=until.length();
        int found=0;
        int v=is.read();
        while (v>0) {
            if (v==0) {
                // binary data
                throw new Exception(locale.getString(Constants.STR_Incorrect_data_format));
            }
            boolean equal=charEquals((char) v, until.charAt(found), caseSensitive);
            //uiController_.log("Read '"+((char) v)+ "' ("+v+")" + " found="+found+"  equal="+equal);
            if (!equal) {
                if (onlyWhiteSpace && v>32) {
                    throw new Exception(locale.getString(Constants.STR_Incorrect_data_format));
                }
                if (found>0) {
                    found=0;
                    continue;
                }
            } else {
                found++;
            }
            if (found==len) {
                return true;
            }
            v=is.read();
        }
        return false;
    }
    
    // if a character other than white space is found, it is returned
    private int findDelimiter(InputStreamReader is) throws Exception {
        //if (TRACE) System.out.println("findDelimiter(is)");
        while (true) {
            int v=is.read();
            if (v==-1) {
                return -1;
            }
            if (v==0) {
                // binary data
                throw new Exception(uiController_.getLocale().getString(Constants.STR_Incorrect_data_format));
            }
            if (v>32) {
                return v;
            }
        }
    }
    
    private String readString(InputStreamReader is
        , char firstChar
        , String delim
        , boolean delimCaseSensitive) throws Exception {
        
        //uiController_.log("readString >> firstChar='"+firstChar+"', delim='"+delim+"', delimCaseSensitive="+delimCaseSensitive);
        
        StringBuffer sb = new StringBuffer();
        boolean lastWhiteSpace=false;
        
        if (firstChar!=0) {
            sb.append(firstChar);
            lastWhiteSpace=(firstChar<=32);
        }
        
        int v;
        boolean inTag=false;
        int found=0;
        int len=delim.length();
        int appendedInDelim=0;
        
        while (true) {
            v=is.read();
            if (v==-1) {
                throw new Exception(uiController_.getLocale().getString(Constants.STR_Unterminated_string));
            }
            if (v<=32) {
                // whitespace
                if (lastWhiteSpace) {
                    continue;
                }
                v=32;
                lastWhiteSpace=true;
            } else {
                lastWhiteSpace=false;
                if (v=='<') {
                    inTag=true;
                }
            }
            boolean equal=charEquals((char) v, delim.charAt(found), delimCaseSensitive);
            //if (TRACE) System.out.println("ReadString '"+((char) v)+"' found="+found+"  equal="+equal);
            if (!equal) {
                if (found>0) {
                    found=0;
                    appendedInDelim=0;
                    equal=charEquals((char) v, delim.charAt(found), delimCaseSensitive);
                }
            }
            if (equal) {
                found++;
                if (found==len) {
                    if (appendedInDelim>0) {
                        sb.setLength(sb.length()-appendedInDelim);
                    }
                    break;
                }
            }
            if (!inTag) {
                sb.append((char) v);
                // when we are inside the delimiter, we want to get rid of the delimiter later
                // so track it
                if (found>0) {
                    appendedInDelim++;
                }
            } else if (v=='>') {
                inTag=false;
            }
        }
        //uiController_.log("readString >> ret: '"+sb.toString()+"'");
        return sb.toString();
    }
    
    
    /*
     * Simplified parser to find xyz of a <a href="xyz">blablabla</a> statement
     */
    private String readHref(InputStreamReader is) throws Exception {
        //uiController_.log("readHref >> enter");
        uiController_.updateProgress();
        
        // first skip everything until "<a"
        if (!skip(is, "<a", false, false)) {
            //uiController_.log("readHref >> [first skip everything until '<a'] ret: null! (1)");
            return null;
        }
        uiController_.updateProgress();
        // read "href"
        if (!skip(is, "href", false, false)) {
            //uiController_.log("readHref >> [read 'href'] ret: null! (2)");
            return null;
        }
        uiController_.updateProgress();
        // read until "="
        if (!skip(is, "=", true, true)) {
            //uiController_.log("readHref >> [read until '='] ret: null! (3)");
            return null;
        }
        uiController_.updateProgress();
        // wait for " or ' or nothing
        int delim=findDelimiter(is);
        char endDelim=(char) delim;
        char firstChar=0;
        if (delim!='"' && delim!='\'') {
            // url not enclosed in quotes
            endDelim='>';
            firstChar=(char) delim;
        }
        uiController_.updateProgress();
        String ret = readString(is, firstChar, ""+endDelim, true);
        
        //uiController_.log("readHref >> URL="+ret);
        
        uiController_.updateProgress();
        if (firstChar==0) {
            if (!skip(is, ">", true, true)) {
                //uiController_.log("readHref >> [read until '>'] ret: null! (4)");
                return null;
            }
        }

        uiController_.updateProgress();
        //uiController_.log("readHref >> ret: " + ret);
        return ret;
    }
    
    /**
     * Simplified parser to find blabla of a <a href="xyz">blablabla</a> statement
     */
    private String readHrefName(InputStreamReader is) throws Exception {
        // the stream is at first char after >. We just read the string until we find "</a>"
        String ret = readString(is, (char) 0, "</a>", false);
        return ret;
    }

    private void saveToPlaylist(int idx) {
        uiController_.handleLink((String) names_.elementAt(idx)
            , (String) urls_.elementAt(idx));
    }
}
