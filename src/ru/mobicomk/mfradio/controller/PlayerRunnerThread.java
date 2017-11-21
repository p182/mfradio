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

import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;

import ru.mobicomk.mfradio.iface.PlayerQueue;
import ru.mobicomk.mfradio.util.BooleanFlag;
import ru.mobicomk.mfradio.util.PlayerQueueException;

/**
 * Player runner thread.
 * <p>
 * Gets next Player object from Player's queue and starts it.
 * </p>
 * 
 * @author Roman Bondarenko
 * @see UIController
 * @see PlayerQueue
 */
class PlayerRunnerThread extends Thread implements PlayerListener {

    /**
         * Controller must setting up this flag to <b>true</b> for stop thread
         * execution.
         */
    BooleanFlag StopFlag;

    private UIController controller_;

    private PlayerQueue queue_;

    private Object playerSync_;

    private Object player2Sync_;

    private Player player0_ = null;

    private Player player1_ = null;

    private PlayerListener listener_ = null;

    /**
         * Creates a new instance of PlayerRunnerThread.
         * 
         * @param queue
         *                Player queue object.
         * @param controller
         *                Application controller object.
         */
    PlayerRunnerThread(PlayerQueue queue, UIController controller) {
	controller_ = controller;
	queue_ = queue;
	StopFlag = new BooleanFlag(false);
	playerSync_ = new Object();
	player2Sync_ = new Object();
    }

    /**
         * Set the volume using linear point scale with values between 0 and
         * 100. 0 is silence; 100 is the loudest usefull level that current
         * VolumeControl supports. If the given level is less than 0 or greater
         * than 100, the level well be set to 0 or 100 respectively.
         * 
         * @param volume
         *                The new volume specified in the level scale.
         */
    void setVolume(int volume) {
	if (player1_ != null) {
	    if (player1_.getState() == Player.STARTED) {
		VolumeControl vc = (VolumeControl) player1_
			.getControl("VolumeControl");
		if (vc != null) {
		    vc.setLevel(volume);
		}
	    }
	}
    }

    /**
         * Stop execution thread helper.
         */
    void stop() {
	StopFlag.set();
	synchronized (playerSync_) {
	    playerSync_.notify();
	}
    }

    /**
         * Connect player listener object to this player runner thread. Supports
         * only once listener at a time.
         * <p>
         * <b>Note:</b> Old listener disconnects without any notifications if
         * new object was connected.
         * </p>
         * 
         * @param listener
         *                Player listener object.
         */
    void setPlayerListener(PlayerListener listener) {
	listener_ = listener;
    }

    // Runnable interface
        // //////////////////////////////////////////////////////

    /**
         * Thread entry point.
         * 
         * @see java.lang.Thread
         * @see java.lang.Thread#run
         */
    public void run() {
	try {

	    /*
                 * long duration = player1_.getDuration();
                 * 
                 * if (duration != Player.TIME_UNKNOWN) { durationSwitchLoop(); }
                 * else { eosSwitchLoop(); }
                 */

	    switchLoop();

	} catch (InterruptedException ex) {
	    ex.printStackTrace();
	    // StopFlag.set();
	} catch (PlayerQueueException ex) {
	    ex.printStackTrace();
	    StopFlag.set();
	} catch (MediaException ex) {
	    ex.printStackTrace();
	} finally {
	    closePlayer(player1_);
	    closePlayer(player0_);
	}

	if (!StopFlag.value()) {
	    StopFlag.set();
	    controller_.runnerIsInterrupted();
	} else {
	    controller_.runnerIsStopped();
	}

	// controller_.log("runner >> finish thread");
    }

    /**
         * {@link PlayerListener} interface implementation method.
         * 
         * @param player
         *                The player which generated this event.
         * @param event
         *                The event generated as defined by the enumerated
         *                types.
         * @param object
         *                The associated event data.
         * @see javax.microedition.media.PlayerListener interface.
         */
    public void playerUpdate(Player player, String event, Object object) {
	if (event == PlayerListener.END_OF_MEDIA) {
	    boolean err = false;

	    if (player0_ != null) {

		synchronized (player2Sync_) {
		    VolumeControl volume = (VolumeControl) player0_
			    .getControl("VolumeControl");
		    if (volume != null) {
			volume.setLevel(controller_.getVolume());// !!
		    }
		    try {
			player0_.start();
			// controller_.log("upd >>
                        // "+System.currentTimeMillis()+" >> start player");
		    } catch (MediaException ex) {
			err = true;
		    }

		    player1_.close();
		    player1_ = err ? null : player0_;
		    if (err) {
			player0_.close();
			// controller_.log("upd >>
                        // "+System.currentTimeMillis()+" >> close player");
		    }
		    player0_ = null;
		}

		synchronized (playerSync_) {
		    playerSync_.notify();
		}

	    } else {

		player1_.close();
		player1_ = null;
		// controller_.log("upd >> "+System.currentTimeMillis()+" >>
                // close player; no more players.");
	    }
	}
    }

    // Privates
        // ////////////////////////////////////////////////////////////////

    private void closePlayer(Player player) {
	if (player == null || player.getState() == Player.CLOSED) {
	    return;
	}

	try {
	    player.stop();
	} catch (MediaException e) {
	}

	player.close();
	player = null;
    }

    private void switchLoop() throws PlayerQueueException,
	    InterruptedException, MediaException {
	player1_ = null;

	while (!StopFlag.value()) {

	    // controller_.log("eos >> "+System.currentTimeMillis()+" >>
                // wait for next player");
	    player0_ = queue_.popHead(); // exception
	    // controller_.log("eos >> "+System.currentTimeMillis()+" >>
                // next player! duration: " + player0_.getDuration()/1000);

	    if (player1_ == null && player0_ != null) {

		synchronized (player2Sync_) {
		    VolumeControl volume = (VolumeControl) player0_
			    .getControl("VolumeControl");
		    if (volume != null) {
			volume.setLevel(controller_.getVolume());// !!
		    }
		    player0_.start();
		    player1_ = player0_;
		    player0_ = null;
		    // controller_.log("eos >>
                        // "+System.currentTimeMillis()+" >> start player");
		}

	    } else {

		synchronized (playerSync_) {
		    // controller_.log("eos >>
                        // "+System.currentTimeMillis()+" >> wait for EOS");
		    playerSync_.wait(/* player0_.getDuration()/1000 + 1000 */);
		}

	    }
	}
    }

    // //////////////////////////////////////////////////////////////////////////
    // 
    // Other method
    //
    // //////////////////////////////////////////////////////////////////////////

    /*
         * private void eosSwitchLoop() throws PlayerQueueException,
         * InterruptedException, MediaException { doNotWait_ = true; while
         * (!StopFlag.value()) { player0_ = queue_.popHead();
         * //controller_.log("eos >> "+System.currentTimeMillis()+" >> next
         * player");
         * 
         * if (doNotWait_) { VolumeControl volume =
         * (VolumeControl)player0_.getControl("VolumeControl"); if (volume !=
         * null) { volume.setLevel(controller_.getVolume());// !! }
         * player0_.start(); //controller_.log("eos >>
         * "+System.currentTimeMillis()+" >> start player"); doNotWait_ = false; }
         * else { synchronized (playerSync_) { //controller_.log("eos >>
         * "+System.currentTimeMillis()+" >> wait for EOS"); playerSync_.wait(); } } } }
         *  // PlayerListener interface
         * ////////////////////////////////////////////////
         * 
         * public void playerUpdate(Player player, String event, Object object) {
         * 
         * if(event == PlayerListener.STOPPED){ player1_ = null; player.close();
         * //controller_.log("upd >> "+System.currentTimeMillis()+" >> STOPPED
         * for " + player.toString()); return; }
         * 
         * if (event == PlayerListener.STARTED) { //controller_.log("upd >>
         * "+System.currentTimeMillis()+" >> STARTED for " + player.toString());
         * //volume_ = null; player0_ = null; player1_ = player; synchronized
         * (playerSync_) { playerSync_.notify(); } return; }
         * 
         * if(event == PlayerListener.END_OF_MEDIA){ //controller_.log("upd >>
         * "+System.currentTimeMillis()+" >> EOS for " + player.toString());
         * 
         * if (StopFlag.value()) { //controller_.log("upd >>
         * "+System.currentTimeMillis()+" >> Stop flag!"); return; //?? }
         * 
         * VolumeControl volume =
         * (VolumeControl)player0_.getControl("VolumeControl"); if (volume !=
         * null) { volume.setLevel(controller_.getVolume());// !! }
         * 
         * if (player0_ != null) { try { player0_.start();
         * //controller_.log("upd >> "+System.currentTimeMillis()+" >> start
         * player " + player0_.toString()); } catch (MediaException ex) {
         * //controller_.log("upd >> "+System.currentTimeMillis()+" >>
         * "+ex.toString()+"; set doNotWait flag."); doNotWait_ = true; } } else {
         * //controller_.log("upd >> "+System.currentTimeMillis()+" >> player0
         * is null! set doNotWait flag."); doNotWait_ = true; }
         * 
         * //try { //player.stop(); //} catch (MediaException ex) { }
         * player.close(); //controller_.log("upd >>
         * "+System.currentTimeMillis()+" >> close player " +
         * player.toString()); //player1_ = null; } }
         * 
         * private void durationSwitchLoop() { while (!StopFlag.value()) { try {
         * player0_ = queue_.popHead(); sleep(player1_.getDuration() / 1000 -
         * 50);
         * 
         * if (StopFlag.value()) { break; }
         * 
         * VolumeControl volume =
         * (VolumeControl)player0_.getControl("VolumeControl"); if (volume !=
         * null) { volume.setLevel(controller_.getVolume());// !! }
         * player0_.start(); player1_ = player0_;
         *  } catch (PlayerQueueException ex) { break; // stop execution thread } } }
         * 
         * public void playerUpdate(Player player, String event, Object object) {
         * if(event == PlayerListener.STOPPED){ player.close(); return; }
         * 
         * if(event == PlayerListener.END_OF_MEDIA){ if (StopFlag.value()) {
         * return; } try { player.stop(); } catch (MediaException ex) { } } }
         */
}
