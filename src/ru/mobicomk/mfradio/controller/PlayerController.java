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

import java.util.Hashtable;
import javax.microedition.media.PlayerListener;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.iface.PlayerQueue;
import ru.mobicomk.mfradio.util.AppException;
import ru.mobicomk.mfradio.util.PlayerQueueImpl;
import ru.mobicomk.mfradio.util.StationInfo;

/**
 * Player maker and player runner threads controller.
 * Владеет PlayerQueue, владеет и управляет PlayerMaker-ом и PlayerRunner-ом.
 * Создаётся для каждой новой интернет радиостанции.
 *
 * @author  Roman Bondarenko
 */
public class PlayerController {
    
    // Members /////////////////////////////////////////////////////////////////
    
    private StationInfo stationInfo_;
    private PlayerMakerThread maker_;
    private PlayerRunnerThread runner_;
    private PlayerQueue playerQueue_;
    private PlayerListener playerListener_;
    
    private UIController uiController_;
    
    private boolean isPlaying_;
    
    // Public //////////////////////////////////////////////////////////////////
    
    /**
     * Creates a new instance of PlayerController.
     * @param uiController Application controller object.
     */
    public PlayerController(UIController uiController) {
        uiController_ = uiController;
        playerListener_ = null;
        isPlaying_ = false;
    }
    
    /**
     * Second level initialize method. Setting up station information for
     * player.
     * <p>Note: Must be called before each {@link #startPlay} method invocation.</p>
     * <p>Note: Player must be stopped before this method call.</p>
     * @param si New radio station information.
     * @throws java.lang.Exception if called during plaing.
     */
    public synchronized void init(StationInfo si) throws Exception {
        if (isPlaying_) {
            throw new AppException(uiController_.getLocale().getString(Constants.STR_Must_be_stopped));
        }
        uiController_.progressMessage(uiController_.getLocale().getString(Constants.STR_Initialize));
        initPlayerImpl(si); // exception
    }
    
    /**
     * Run stream playing loop.
     * <p>Note: Must be called ater {@link #init} method.</p>
     * <p>Note: Player must be stopped before this method call.</p>
     * @throws java.lang.Exception if called during plaing or if called
     * without initialization ({@link #init}).
     */
    public synchronized void startPlay() throws Exception {
        //uiController_.log("startPlay >> begin");
        
        if (isPlaying_) {
            throw new Exception(uiController_.getLocale().getString(Constants.STR_Must_be_stopped));
        }
        if (maker_ == null || runner_ == null) {
            throw new Exception(uiController_.getLocale().getString(Constants.STR_Must_be_inited));
        }
        
        maker_.start();
        runner_.start();
        
        isPlaying_ = true;
        //uiController_.log("startPlay >> end");
    }
/*
    public void stopPlay() {
        //uiController_.log("stopPlay >> begin");
 
        if (!isPlaying_) {
            //uiController_.log("stopPlay >> end (not playing)");
            return;
        }
 
        maker_.StopFlag.set();
        runner_.StopFlag.set();
 
        runner_.stop();
 
 
        //uiController_.log("stopPlay >> Stop flags is set...");
 
        Thread.yield();
        /*
        try {
            maker_.join();
            runner_.join();
        } catch (InterruptedException ex) {
            //uiController_.log(ex);
        }* /
        //maker_.interrupt();
        //runner_.interrupt();
 
        maker_ = null;
        runner_ = null;
 
        playerQueue_.clear();
        playerQueue_ = null;
 
        isPlaying_ = false;
        //uiController_.log("stopPlay: leave");
    }*/
    
    /**
     * Stop stream playing loop.
     */
    public synchronized void stopPlay() {
        //uiController_.log("stopPlay >> begin");
        
        if (!isPlaying_) {
            //uiController_.log("stopPlay >> end (not playing)");
            return;
        }
        
        // check for maker is a current thread
        boolean needToStopMaker = (maker_ != null)
        && !maker_.StopFlag.value()
        && (maker_ != Thread.currentThread());
        
        if (needToStopMaker && maker_.isAlive()) {
            maker_.stop();
        }
        
        if (playerQueue_ != null) {
            playerQueue_.clear();
        }
        
        boolean needToStopRunner = (runner_ != null)
        && !runner_.StopFlag.value()
        && (runner_ != Thread.currentThread());
        
        if (needToStopRunner && runner_.isAlive()) {
            runner_.stop();
        }
        
        Thread.yield();
        
        //uiController_.log("stopPlay >> Stop flags is set...");
        
        int counter = 0;
        try {
            while (((needToStopMaker && maker_.isAlive())
            || (needToStopRunner && runner_.isAlive()))
            && (counter < 6)) {
                Thread.sleep(500);
                uiController_.updateProgress();
                counter++;
            }
        } catch (InterruptedException ex) { }
        
        /*
        if (maker_.isAlive()) {
            maker_.interrupt(); // WARNING: CLDC 1.1!
            uiController_.progressMessage("maker killed!");
            //uiController_.log("stopPlay >> maker killed!");
        }
         
        if (runner_.isAlive()) {
            runner_.interrupt(); // WARNING: CLDC 1.1!
            uiController_.progressMessage("runner killed!");
            //uiController_.log("stopPlay >> runner killed!");
        }*/
        
        maker_ = null;
        runner_ = null;
        playerQueue_ = null;
        
        isPlaying_ = false;
        
        //uiController_.log("stopPlay >> end");
    }
    
    /**
     * Get information about current player state.
     * @return <b>true</b> if stream playing loop is running, <b>false</b>
     * othrwise.
     */
    public boolean isPlaying() {
        return isPlaying_;
    }
    
    /**
     * Set the volume using linear point scale with values between 0 and 100.
     * 0 is silence; 100 is the loudest usefull level that current
     * VolumeControl supports. If the given level is less than 0 or greater
     * than 100, the level well be set to 0 or 100 respectively.
     * @param level The new volume specified in the level scale.
     */
    public void setVolume(int level) {
        if (runner_ != null) {
            runner_.setVolume(level);
        }
    }
    
    /**
     * HTTP headers of the audio stream request accessor.
     * @return HTTP header of the audio stream request.
     */
    public Hashtable getHeaders() {
        return maker_.getHeaders();
    }
    
    // Privates ////////////////////////////////////////////////////////////////
    
    private void initPlayerImpl(StationInfo si) throws Exception  {
        stationInfo_ = si;
        boolean inited = false;
        
        try {
            uiController_.updateProgress(); // exception
            
            //uiController_.log("initPlayer >> start");
            
            playerQueue_ = new PlayerQueueImpl(uiController_);
            maker_ = new PlayerMakerThread(uiController_);
            runner_ = new PlayerRunnerThread(playerQueue_, uiController_);
            runner_.setPlayerListener(playerListener_);
            
            maker_.init(stationInfo_.Url, playerQueue_, runner_); // exception
            uiController_.updateProgress(); // exception
            
            //uiController_.log("initPlayer >> finish");
            
            inited = true;
        } catch (Exception ex) {
            //uiController_.progressMessage("interrupt - 3");
            //uiController_.log("initPlayer >> interrupt - 3");
            runner_ = null;
            maker_ = null;
            playerQueue_ = null;
            System.gc();
            throw ex;
        } /*
        catch (MediaException ex) {
            //uiController_.log("PlayerMakerThread.ctor >> MediaException >> " + ex.getMessage());
            uiController_.firePlayerStatus("err: " + ex.getMessage());
        } catch (IOException ex) {
            uiController_.firePlayerStatus("err: " + ex.getMessage());
            //uiController_.log("PlayerMakerThread.ctor >> IOException >> " + ex.getMessage());
        } catch (Exception ex) {
            uiController_.firePlayerStatus("err: " + ex.getMessage());
            //uiController_.log("PlayerMakerThread.ctor >> Exception >> " + ex.getMessage());
        }*/
    }
    
    void setPlayerListener(PlayerListener listener) {
        playerListener_ = listener;
    }
    
}
