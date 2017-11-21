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

package ru.mobicomk.mfradio.controller;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.media.PlayerListener;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.MFRadioMidlet;
import ru.mobicomk.mfradio.iface.ContentHandler;
import ru.mobicomk.mfradio.iface.Logger;
import ru.mobicomk.mfradio.iface.ModelListener;
import ru.mobicomk.mfradio.iface.ProgressObserver;
import ru.mobicomk.mfradio.model.Model;
import ru.mobicomk.mfradio.ui.EditStationUI;
import ru.mobicomk.mfradio.ui.HelpUI;
import ru.mobicomk.mfradio.ui.LocaleChoiceUI;
import ru.mobicomk.mfradio.ui.PlayerUI;
import ru.mobicomk.mfradio.ui.ProgressBarUI;
import ru.mobicomk.mfradio.ui.SimpleWapBrowserUI;
import ru.mobicomk.mfradio.ui.SplashUI;
import ru.mobicomk.mfradio.util.AppException;
import ru.mobicomk.mfradio.util.Locale;
import ru.mobicomk.mfradio.util.M3UContentHandler;
import ru.mobicomk.mfradio.util.MP3ContentHandler;
import ru.mobicomk.mfradio.util.StationInfo;

//import megafon.sap.one.util.MemoryLogger;
//import megafon.sap.one.util.FileLogger;

/**
 * <h2>Main application controller</h2>
 *
 * <p>Application structure (only basic classes):</p>
 *
 * <code><pre>
 *                                           +----------+
 *                                     +---->| SplashUI |
 *                                     |     +----------+
 *                                     |             
 *                       +-------------+-+              +----------+
 *                +----->| UIController  |----------+-->| PlayerUI |
 *                |      +--+------------+          |   +----------+
 *  +-------------+-+       |                       |   
 *  | MFRadioMidlet |       |   +---------------+   |   +----------+
 *  +---+-----------+       |   | ProgressBarUI |<--+-->| EditUI   |
 *      |                   |   +---------------+   |   +----------+
 *      V                   |                       |   
 *  +-------+               |        +--------+     |   +--------------------+    
 *  | Model |<--------------+        | HelpUI |<----+-->| SimpleWapBrowserUI |   
 *  +-------+               |        +--------+         +------+---------+---+      
 *                          |                                  | Utils   |
 *                          |                                  +---------+ 
 *                          |
 *                 +--------+                                     
 *                 V                                                              
 *     +------------------+                                     
 *     | PlayerController +-------------+                  
 *     +---+-----------+--+             V                              
 *         |                     +--------------------+        
 *         V                  +--+ PlayerRunnerThread |
 *  +-------------------+     |  +--------------------+  
 *  | PlayerMakerThread +-+   V      
 *  +-----+-------------+ |  +-----------------+ 
 *        |               +->| PlayerQueueImpl | 
 *        +---------+        +-----------------+
 *        V         V
 *  +---------+   +---------------+
 *  | Dloader |   | PlayerFactory |
 *  +---------+   +---------------+  
 *
 * </pre></code>
 *
 * <p>{@link MFRadioMidlet} makes {@link Model} and {@link UIController}.</p>
 * <p>{@link UIController} makes all UI forms and {@link PlayerController}.</p>
 * <p>{@link PlayerController} runs {@link ru.mobicomk.mfradio.controller.PlayerMakerThread} and 
 * {@link ru.mobicomk.mfradio.controller.PlayerRunnerThread} when user selects station.</p>
 * <p>All manipulation with playlist through {@link UIController}.</p>
 *
 * @author  Roman Bondarenko
 */
public class UIController {
    
    // Members /////////////////////////////////////////////////////////////////
    
    private MFRadioMidlet midlet_;    // TODO: MFRadioMidlet -> MIDlet
    private Model model_;
    private PlayerController player_;
    
    // references to all UI classes
    private ProgressObserver    progressUI_;
    private ProgressBarUI       progressBarUI_;
    private PlayerUI            playerUI_;
    private EditStationUI       editStationUI_;
    private SimpleWapBrowserUI  wapBrowserUI_;
    private HelpUI              helpUI_;
    private LocaleChoiceUI 	localeChoiceUI_;
    
    // Navigable inteface
    private Display     display_;
    private Displayable currDisplayable_;
    
    // handlers
    private ContentHandler[] urlHandlers_ = {
        new M3UContentHandler(this)
        , new MP3ContentHandler(this)
        //, new ASFContentHandler(this)
        //, new RAMContentHandler(this)
    };
    
    private int playedStationIdx_;
    private String repositoryURL_;
    private Object stopPlaySync_;
    private boolean inStopPlayStation_;
    
    // Log
    private Logger logger_;
    
    private Locale locale;
    
    
    // Methods /////////////////////////////////////////////////////////////////
    
    /**
     * Initialize new UIController object.
     * @param midlet MIDlet object
     * @param model Application model object
     */
    public UIController(MFRadioMidlet midlet, Model model) {
        midlet_ = midlet;
        model_ = model;
        playedStationIdx_ = -1;
        
        stopPlaySync_ = new Object();
        inStopPlayStation_ = false;
        
        display_ = Display.getDisplay(midlet);
        
        player_ = new PlayerController(this);
        progressUI_ = null;
        helpUI_ = null;
        
        logger_ = null;
        //logger_ = new MemoryLogger(this);
        //logger_ = new FileLogger(this);
    }
    
    // Actions /////////////////////////////////////////////////////////////////
    
    /**
     * Start play selected station.
     */
    public void playStation() {
        runWithProgress(
            new EventDispatcherThread(EventIds.EVENT_ID_PLAY_STATION, getPlayerUI())
            , locale.getString(Constants.STR_Connecting)
            , true);
    }
    
    /**
     * Stop play selected station and (optionally) switch to other
     * screen form.
     * @param nextScreen next displayable object. May be <b>null</b>.
     */
    public void stopPlayStation(Displayable nextScreen) {
        //log("stopPlayStation >> begin");
        
        synchronized (stopPlaySync_) {
            if (inStopPlayStation_) {
                //log("stopPlayStation >> end (inStopPlayStation is TRUE)");
                return;
            }
            inStopPlayStation_ = true;
        }

        player_.stopPlay();
        
        if (nextScreen != null) {
            replaceCurrent(nextScreen);
        }
        
        if (getCurrDisplayable() == getPlayerUI()) {
            getPlayerUI().updateUI();
        }
        
        //log("stopPlayStation >> end");
        
        synchronized (stopPlaySync_) {
            inStopPlayStation_ = false;
        }
    }
    
    /**
     * Start UI - start initialize application thread.
     */
    public void startUI() {
//        PlayerUI playerUI = getPlayerUI();
        runWithSplash(new EventDispatcherThread(EventIds.EVENT_ID_START_UI, null)
        , "");
    }
    
    /**
     * Stop UI - save and close all.
     */
    public void stopUI() {
        stopPlayStation(null);
        Displayable d = currDisplayable_;
        
        if (logger_ != null) {
            logger_.flush();
        }
        showLog();
        model_.save();
        
        try {
            Thread.sleep((logger_ == null) ? 1000 : 4000); // 1sec
        } catch (InterruptedException ex) { /*...*/ }
    }
    
    /**
     * Pause UI - not implemented yet.
     */
    public void pauseUI() {
        // TODO: implement it!
    }
    
    /**
     * Stop execution MIDlet.
     */
    public void exitMIDlet() {
        progressUI_ = null;
        replaceCurrent(null);
        midlet_.exitMIDlet();
    }
    
    /**
     * Initialize and show <i>Edit station</i> form.
     */
    public void editStationRequested() {
        getEditStationUI().init(model_.getStationInfo(model_.getSelectedStationIdx()));
        progressUI_ = null;
        replaceCurrent(getEditStationUI());
    }
    
    /**
     * Close <i>Edit station</i> form and switch to <i>Player</i> form.
     */
    public void endEditStationRequested() {
        playerUIRequested();
    }
    
    /**
     * Initialize and show <i>Add new station</i> form.
     */
    public void addStationRequested() {
        getEditStationUI().init(null);
        progressUI_ = null;
        replaceCurrent(getEditStationUI());
    }
    
    /**
     * Close <i>Add new station</i> form and switch to <i>Player</i> form.
     */
    public void endAddStationRequested() {
        playerUIRequested();
    }
    
    /**
     * Show <i>Player</i> form (main appilcation form).
     */
    public void playerUIRequested() {
        getPlayerUI().updateUI();
        
        progressUI_ = null;
        replaceCurrent(getPlayerUI());
    }
    
    /**
     * Start simple WAP/HTML browser for radio station links repository on
     * the Internet.
     */
    public void repositoryRequested() {
        if (!isRepositorySupported()) {
            return;
        }
        
        repositoryURL_ = midlet_.getAppProperty(Constants.APP_REPOSITORY_URL_KEY_PREFIX + "1");
        
        //if ("wtk-emulator".equals(System.getProperty("device.model"))) {
        //    repositoryURL_ = "http://wap.local/index.wml";
        //}
        
        new EventDispatcherThread(EventIds.EVENT_ID_GOTO_REPO, getWapBrowserUI()).start();
    }
    
    /**
     * Close <i>WAP browser</i> form and switch to <i>Player</i> form.
     */
    public void exitWapBrowserRequested() {
        progressUI_ = null;
        replaceCurrent(getPlayerUI());
    }
    
    /**
     * Show selected help screen.
     * @param helpTopic Requested help topic. For possible values
     * see {@link HelpUI}.
     */
    public void showHelp(int helpTopic) {
        if (helpUI_ == null) {
            helpUI_ = new HelpUI(this);
        }
        helpUI_.init(helpTopic, getCurrDisplayable());
        progressUI_ = null;
        replaceCurrent(helpUI_);
    }
    
    /**
     * Close <i>Help</i> form and switch to other displayable object.
     * @param d Next displayable object. May be <b>null</b>.
     */
    public void endShowHelp(Displayable d) {
        replaceCurrent(d);
    }
    
    // Player's queue methods //////////////////////////////////////////////////
    
    /**
     * Player's queue listener method. Fired if queue is not empty. Close
     * <i>Wait</i> form and switch to <i>Player</i> form.
     */
    public void queueIsNotEmpty() {
        new Thread(new Runnable() {
            public void run() {
                getPlayerUI().updateUI();
                endWaitScreenRequested(getPlayerUI());
            }
        }).start();
    }
    
    /**
     * Player's queue listener method. Fired if queue is empty. Start
     * <i>Wait</i> form.
     */
    public void queueIsEmpty() {
        beginWaitScreenRequested(locale.getString(Constants.STR_Prefetching), true);
    }
    
    /**
     * Player's maker thread listener. Fired if thread was interrupted
     * by exception.
     */
    public void makerIsInterrupted()  {
        //log("makerIsInterrupted >> begin");
        Displayable d = getPlayerUI();
        stopPlayStation(getCurrDisplayable() != d ? d : null);
        //log("makerIsInterrupted >> end");
    }
    
    /**
     * Player's maker thread listener. Fired if thread was stopped
     * by user.
     */
    public void makerIsStopped() {
        //log("makerIsStopped >> begin");
        Displayable d = getPlayerUI();
        stopPlayStation(getCurrDisplayable() != d ? d : null);
        //log("makerIsStopped >> end");
    }
    
    /**
     * Player's runner thread listener. Fired if thread was interrupted
     * by exception.
     */
    public void runnerIsInterrupted() {
        //log("runnerIsInterrupted >> begin");
        Displayable d = getPlayerUI();
        stopPlayStation(getCurrDisplayable() != d ? d : null);
        //log("runnerIsInterrupted >> end");
    }
    
    /**
     * Player's runner thread listener. Fired if thread was stopped
     * by user.
     */
    public void runnerIsStopped() {
        //log("runnerIsStopped >> begin");
        playedStationIdx_ = -1;
        
        if (!inStopPlayStation_) {
            if (getCurrDisplayable() != getPlayerUI()) {
                playerUIRequested();
            } else {
                updatePlayerUI();
            }
        } else if (getCurrDisplayable() == getPlayerUI()) {
            updatePlayerUI();
            //log("runnerIsStopped >> player UI is updated!");
        }
        
        //log("runnerIsStopped >> end");
    }
    
    // Station list methods ////////////////////////////////////////////////////
    
    /**
     * Selected station in playlist accessor.
     * @return Selected (current) station info from playlist.
     */
    public StationInfo getSelectedStation() {
        return model_.getStationInfo(model_.getSelectedStationIdx());
    }
    
    /**
     * Index of selected station in playlist.
     * @return Index of selected (current) station in playlist.
     */
    public int getSelectedStationIdx() {
        return model_.getSelectedStationIdx();
    }
    
    /**
     * Index of currently played station in playlist.
     * @return Index of currently played station in playlist.
     */
    public int getPlayedStationIdx() {
        return playedStationIdx_;
    }
    
    /**
     * Update station info in playlist.
     * @param idx Index of station.
     * @param si New station information.
     */
    public void setStation(int idx, StationInfo si) {
        if (si != null) {
            model_.setStationInfo(model_.getSelectedStationIdx(), si);
        }
    }
    
    /**
     * Insert new station information into playlist.
     * @param si New station information.
     */
    public void addStation(StationInfo si) {
        if (si != null) {
            if (!model_.hasURL(si.Url)) {
                model_.addStationInfo(si);
            } else {
                StationInfo si2 = model_.getStationInfo(si.Url);
                showWarning(locale.getString(Constants.STR_Station_in_playlist)
                    + si2.Title
                    , currDisplayable_);
            }
        }
    }
    
    /**
     * Delete current (selected) station information from playlist and select
     * next or previous station.
     */
    public void deleteStation() {
        int selectedIdx = model_.getSelectedStationIdx();
        int newSelectedIdx = selectedIdx;
        
        if (selectedIdx > 0) {
            newSelectedIdx--;
        } else if (model_.getStationsCount() > 1) {
            newSelectedIdx++;
        } else {
            newSelectedIdx = -1;
        }
        
        model_.selectStation(newSelectedIdx); // ??
        model_.deleteStationInfo(selectedIdx);
        
        updatePlayerUI();
    }
    
    /**
     * Select previous station info in playlist.
     */
    public void selectPrevStation() {
        if (model_.getStationsCount() > 0) {
            int oldSelIdx = model_.getSelectedStationIdx();
            if (oldSelIdx > 0) {
                model_.selectStation(oldSelIdx - 1);
            } else {
                model_.selectStation(model_.getStationsCount() - 1);
            }
        }
        updatePlayerUI();
    }
    
    /**
     * Select next station info in playlist.
     */
    public void selectNextStation() {
        if (model_.getStationsCount() > 0) {
            int oldSelIdx = model_.getSelectedStationIdx();
            if (oldSelIdx < model_.getStationsCount() - 1) {
                model_.selectStation(oldSelIdx + 1);
            } else {
                model_.selectStation(0);
            }
        }
        updatePlayerUI();
    }
    
    // Handle links methods ////////////////////////////////////////////////////
    
    /**
     * Check if application has handler for link (URL).
     * @param url Link (URL) for check.
     * @return Result of checking (<b>true</b> or <b>false</b>).
     */
    public boolean canHandleLink(String url) {
        for (int idx = 0; idx < urlHandlers_.length; idx++) {
            if (urlHandlers_[idx].canHandle(url)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Handle link (URL).
     * @param title Link title.
     * @param url Link URL.
     */
    public void handleLink(String title, String url) {
        for (int idx = 0; idx < urlHandlers_.length; idx++) {
            if (urlHandlers_[idx].canHandle(url)) {
                urlHandlers_[idx].handle(title, url);
                //urlHandlers_[idx].close();
                break;
            }
        }
    }
    
    // Volume methods //////////////////////////////////////////////////////////
    
    /**
     * Player volume accessor.
     * @return Current player volume level.
     */
    public int getVolume() {
        return model_.getVolume();
    }
    
    /**
     * Increment player volume level.
     */
    public void incVolume() {
        int vol = model_.getVolume();
        if (vol < Model.MAX_VOL) {
            model_.setVolume(vol + 10);
            player_.setVolume(vol + 10);
        }
        updatePlayerUI();
    }
    
    /**
     * Decrement player volume level.
     */
    public void decVolume() {
        int vol = model_.getVolume();
        if (vol > Model.MIN_VOL) {
            model_.setVolume(vol - 10);
            player_.setVolume(vol - 10);
        }
        updatePlayerUI();
    }
    
    // Progress methods ////////////////////////////////////////////////////////
    
    /**
     * Update progress of a current long operation.
     * @throws java.lang.InterruptedException if user press <i>Stop</i>.
     */
    public void updateProgress() throws InterruptedException {
        if ((progressUI_ != null)/* && (getCurrDisplayable() == progressUI_)*/ ) {
            if (!progressUI_.isStopped()) {
                progressUI_.updateProgress();
                return;
            }
            //log("updateProgress >> " + locale.getString(Constants.STR_Operation_interrupted);
            throw new InterruptedException(locale.getString(Constants.STR_Operation_interrupted));
        }
    }
    
    /**
     * Set progress bar stoppable flag. If it's <b>true</b>, then user can
     * interrupt long operation by pressing <i>Stop</i>. Operation is
     * uninterrupted in other case.
     * @param stoppable New <i>stoppable</i> flag value.
     * @return Previous <i>stoppable</i> flag value.
     */
    public boolean progressStoppable(boolean stoppable) {
        boolean prevStoppable = false;
        if ((progressUI_ != null) && (getCurrDisplayable() == progressUI_)) {
            prevStoppable = progressUI_.isStoppable();
            progressUI_.setStoppable(stoppable);
        }
        return prevStoppable;
    }
    
    /**
     * Set progress bar message.
     * @param message New progress bar message.
     */
    public void progressMessage(String message) {
        if (progressUI_ != null) {
            progressUI_.setMessage(message);
        }
    }
    
    // Switch UI methods ///////////////////////////////////////////////////////
    
    /**
     * Start thread with long operation and show <i>Wait</i> form.
     * @param thread Thread to start.
     * @param title Initial title of a progress bar.
     * @param stoppable Initial <i>stoppable</i> flag value of a progress bar.
     */
    public void runWithProgress(Thread thread, String title, boolean stoppable) {
        beginWaitScreenRequested(title, stoppable);
        
        thread.start();
    }
    
    /**
     * Start thread with long operation and show <i>Splach</i> form.
     * @param thread Thread to start.
     * @param title Title of a <i>Splach</i> form.
     */
    public void runWithSplash(Thread thread, String title) {
        SplashUI splash = new SplashUI();
        splash.init(title);
        progressUI_ = splash;
        replaceCurrent(splash);
        
        thread.start();
    }
    
    /**
     * Switch to <i>Wait</i> from.
     * @param title Initial title of a progress bar.
     * @param stoppable Initial <i>stoppable</i> flag value of a progress bar.
     */
    public void beginWaitScreenRequested(String title, boolean stoppable) {
        //log("beginWaitScreen >> begin; curr=" + getCurrDisplayable() + "; pBar=" + progressBarUI_);
	ProgressBarUI progressBar = getProgressBarUI();
        if (getCurrDisplayable() != progressBar) {
            progressBar.init(title, stoppable);
            progressUI_ = progressBar;
            replaceCurrent(progressBar);
        }
        //log("beginWaitScreen >> end");
    }
    
    /**
     * Hide <i>Wait</i> form and switch to other displayable object. Do nothing
     * if next displayable object equals current displayable.
     * @param d Next displayable object.
     */
    public void endWaitScreenRequested(Displayable d) {
        //log("endWaitScreen >> begin; curr=" + getCurrDisplayable() + "; pBar=" + progressBarUI_);
        if (getCurrDisplayable() != d && d != getProgressBarUI()) {
            progressUI_ = null;
            replaceCurrent(d);
        }
        //log("endWaitScreen >> end");
    }
    
    // ShowXXX methods /////////////////////////////////////////////////////////
    
    /**
     * Show error helper.
     * @param msg Error message.
     * @param d Next displayable object.
     */
    public void showError(String msg, Displayable d) {
        showAlert(locale.getString(Constants.STR_Error), msg, AlertType.ERROR, d);
    }
    
    /**
     * Show warning helper.
     * @param msg Warning message.
     * @param d Next displayable object.
     */
    public void showWarning(String msg, Displayable d) {
        showAlert(locale.getString(Constants.STR_Warning), msg, AlertType.WARNING, d);
    }
    
    /**
     * Show information helper.
     * @param msg Information message.
     * @param d Next displayable object.
     */
    public void showInfo(String msg, Displayable d) {
        showAlert(locale.getString(Constants.STR_Info), msg, AlertType.INFO, d);
    }
    
    private void showAlert(String title, String msg, AlertType type, Displayable d) {
        Alert a = new Alert(title, msg, null, type);
        a.setTimeout(Alert.FOREVER);
        
        if (d == null) {
            d = currDisplayable_;
        }
        
        replaceCurrent(a, d);
    }
    
    /**
     * Show form helper. Shows form with custom title and text body. Make delay
     * after switch to it.
     * @param title Form title.
     * @param text Form text body.
     * @param sec Seconds count for delay.
     */
    public void showForm(String title, String text, int sec) {
        Form f = new Form(title);
        StringItem si = new StringItem(null, text);
        f.append(si);
        final Command exit = new Command(locale.getString(Constants.STR_Exit), Command.EXIT, 1);
        f.addCommand(exit);
        f.setCommandListener(new CommandListener() {
            public void commandAction(Command command, Displayable displayable) {
                if (command == exit) {
                    exitMIDlet();
                }
            }
        });
        progressUI_ = null;
        replaceCurrent(f);
        
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
    }
    
    // logger methods //////////////////////////////////////////////////////////
    
    /**
     * Append message to log.
     * @param msg Message to append to log.
     */
    public void log(String msg) {
        if (logger_ != null) {
            logger_.log(msg);
        }
    }
    
    /**
     * Append exception information to log.
     * @param t Exception object.
     */
    public void log(Throwable t) {
        if (logger_ != null) {
            logger_.log(t);
        }
    }
    
   /**
    * Show log messages.
    */
    public void showLog() {
        if (logger_ != null) {
            showInfo(logger_.toString(), currDisplayable_);
        }
    }
    
    // Other methods ///////////////////////////////////////////////////////////
    
    /**
     * Set player events listener helper.
     * @param listener New player listener.
     */
    public void setPlayerListener(PlayerListener listener) {
        if (player_ != null) {
            player_.setPlayerListener(listener);
        }
    }
    
    /**
     * Set model events listener helper.
     * @param listener New model listener.
     */
    public void setModelListener(ModelListener listener) {
        model_.setModelListener(listener);
    }
    
    /**
     * MIDlet's display accessor.
     * @return MIDlet's display.
     */
    public Display getDisplay() {
        return display_;
    }
    
    /**
     * Current displayable object accessor.
     * @return Current displayable object.
     */
    public Displayable getCurrDisplayable() {
        return currDisplayable_;
    }
    
    /**
     * Get information about color support of the device.
     * @return <b>true</b> if the display supports color, <b>false</b> othrwise.
     */
    public boolean isColor() {
        return display_.isColor();
    }
    
    /**
     * Update <i>Player</i> form helper.
     */
    public void updatePlayerUI() {
        getPlayerUI().updateUI();
    }
    
    /**
     * Get information about on-line radio station links repository support.
     * @return <b>true</b> if repository URL not empty in the MIDlet properties,
     * <b>false</b> in othrwise. Property key name is <i>RepositoryURL-1</i>.
     */
    public boolean isRepositorySupported() {
        String url = midlet_.getAppProperty(Constants.APP_REPOSITORY_URL_KEY_PREFIX + "1");
        return ((url != null) && !"".equals(url));
    }
    
    // Navigable methods ///////////////////////////////////////////////////////
    
    /**
     * Replace current displayable object with new.
     * @param d Next displayable object.
     * @return Previous displayable object.
     */
    public Displayable replaceCurrent(Displayable d) {
        display_.setCurrent(d);
        //log("go >> switch to " + d);
        if (! (d instanceof Alert)) {
            // Alerts come back automatically
            currDisplayable_ = d;
        }
        return d;
    }
    
    /**
     * Show alert and switch to other displayable object after it.
     * @param a Alert object to show.
     * @param d Next displayable object.
     * @return Previous displayable object.
     */
    public Displayable replaceCurrent(Alert a, Displayable d){
        display_.setCurrent(a, d);
        //log("go >> switch to " + d);
        if (! (d instanceof Alert)) {
            // Alerts come back automatically
            currDisplayable_ = d;
        }
        return d;
    }
    
    // Privates ////////////////////////////////////////////////////////////////
    
    private EditStationUI getEditStationUI() {
        if (editStationUI_ == null) {
            editStationUI_ = new EditStationUI(this);
        }
        return editStationUI_;
    }
    
    private PlayerUI getPlayerUI() {
        if (playerUI_ == null) {
            playerUI_ = new PlayerUI(this);
        }
        return playerUI_;
    }
    
    private ProgressBarUI getProgressBarUI() {
        if (progressBarUI_ == null) {
            progressBarUI_ = new ProgressBarUI(this);
        }
        return progressBarUI_;
    }
    
    private Displayable getWapBrowserUI() {
        if (wapBrowserUI_ == null) {
            wapBrowserUI_ = new SimpleWapBrowserUI(locale.getString(Constants.STR_Repository), this);
        }
        return wapBrowserUI_;
    }
    
    /*
     *
     */
    class EventIds {
        public static final int BASE_IDX    = 0;
        
        public static final int EVENT_ID_UNDEFINED      = BASE_IDX + 0;
        public static final int EVENT_ID_START_UI       = BASE_IDX + 1;
        public static final int EVENT_ID_STOP_UI        = BASE_IDX + 2;
        public static final int EVENT_ID_PLAY_STATION   = BASE_IDX + 3;
        public static final int EVENT_ID_SAVE_LOG       = BASE_IDX + 4;
        public static final int EVENT_ID_GOTO_REPO      = BASE_IDX + 5;
    }
    
    /*
     *
     */
    class EventDispatcherThread
        extends Thread {
        
        private int taskId_;
        private Displayable fallbackUI_;
        
        EventDispatcherThread(int taskId, Displayable fallbackUI) {
            taskId_ = taskId;
            fallbackUI_ = fallbackUI;
            return;
        }
        
        public void run() {
            try {
                switch (taskId_) {
                    
                    case EventIds.EVENT_ID_PLAY_STATION: {
                        try {
                            //log("edt:play >> start");
                            synchronized (player_) {
                                stopPlayStation(null);
                                updateProgress(); // exception
                                player_.init(getSelectedStation()); // exception
                                player_.startPlay(); // exception
                                playedStationIdx_ = model_.getSelectedStationIdx();
                                updateProgress();
                            }
                            //log("edt:play >> finish");
                        } catch (InterruptedException ex) {
                            //log("edt:play >> " + ex.toString());
                            //progressMessage("interrupt - 4");
                            replaceCurrent(fallbackUI_);
                        } catch (AppException ex) {
                            //log("edt:play >> " + ex.toString());
                            // do nothing, because this exception may be fired
                            // only if user press "play"
                        } catch (Throwable t) {
                            //log("edt:play >> " + t.toString());
                            showError(locale.getString(Constants.STR_Error_start_player_for)
                                + "\"" + getSelectedStation().Title
                                + "\": " + t.getMessage()
                                , fallbackUI_);
                        }
                        break;
                    }
                    
                    case EventIds.EVENT_ID_START_UI: {
//                        updateProgress();
                        model_.load();
//                        updateProgress();
                        try {Thread.sleep(2000);} catch (InterruptedException ex) { }
                        
                        //FIXME initial load of playlist
                        getPlayerUI().playlistChanges(model_.getStationTitles(), model_.getStationsCount());
                        getPlayerUI().updateUI();
                        endWaitScreenRequested(getPlayerUI());
                        break;
                    }
                    
                    case EventIds.EVENT_ID_GOTO_REPO: {
                        wapBrowserUI_.handle(null, repositoryURL_);
                    }
                    
                    // Other cases ...
                }
            } catch (Exception ex) {
                showError("Exception: " + ex.getMessage(), fallbackUI_);
            }
        } // end of run() method
        
    } // end of EventDispatcher class
    
    public void setLocale(Locale newlocale) {
	if (locale != null) {
	    if (!locale.equals(newlocale)) {
		locale = newlocale;
		progressUI_ = null;
		progressBarUI_ = null;
		playerUI_ = null;
		editStationUI_ = null;
		wapBrowserUI_ = null;
		helpUI_ = null;
		localeChoiceUI_ = null;
	    }
	}
    }
    
    public Locale getLocale() {	
	if (locale == null) {
	    locale = Locale.getForName(model_.getLocaleName());
	}
	return locale;
    }

    public void endLocaleChoiceRequest(Locale newLocale) {
	if ((newLocale != null) && (!locale.equals(newLocale))) {
	    setLocale(newLocale);
	    // create new PlayerUI object
	    getPlayerUI();
	    model_.setLocaleName(locale.getName());
	    // when locale name is changed the model will notify PlayerUI to update playlist
	}
	replaceCurrent(getPlayerUI());
    }

    public void localeChoiceRequested() {
	progressUI_ = null;
	getLocaleChoiceUI().setSelectedLocale(locale);
	replaceCurrent(getLocaleChoiceUI());
    }

    private LocaleChoiceUI getLocaleChoiceUI() {
	if (localeChoiceUI_ == null) {
	    localeChoiceUI_ = new LocaleChoiceUI(this);
	    localeChoiceUI_.init(Locale.getAvailable());
	}
	return localeChoiceUI_;
    }
    
}
