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

import javax.microedition.lcdui.Canvas;
import java.io.IOException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.iface.ProgressObserver;


/**
 * Progress bar form class.
 *
 * <p>Progress bar form contents progress bar, status message and (optionally) 
 * <i>Stop</i> command menu item. This form shows when application performs 
 * long-time operations.</p>
 *
 * <p>If <i>Stop</i> menu item enabled, then user can break current operation 
 * by selecting this command.</p>
 *
 * <p>Programmer can enable or disable <i>Stop</i> menu item through 
 * <i>Stoppable</i> flag. To enable <i>Stop</i> command programmer must set 
 * <i>Stoppable</i> flag to <b>true</b>. Correspondingly <b>false</b> turns 
 * it to disabled state.</p>
 *
 * @author  Roman Bondarenko
 */
public class ProgressBarUI 
    extends Canvas
    implements ProgressObserver, CommandListener {

    private static final int PROGRESS_MAX = 10;
    
    private int progress_ ;
    private Image buffer_ = null;
    private String message_ = null;
    private boolean stoppable_ = false;
    private boolean stopped_ = false;
    private Command stopCommand_;

    // GUI componets
    private static final int ICON_DOT                   = 0;
    private static final int ICON_BLUE_GREEN_TOP_LEFT   = 1;
    private static final int ICON_WHITE_BLUE_TOP_RIGHT  = 2;
    private static final int ICON_WHITE_GREEN_TOP_RIGHT = 3;
    private static final int ICON_BLUE_GREEN_TOP_RIGHT  = 4;
    private static final int ICON_WHITE_BLUE_RIGHT      = 5;
    private static final int ICON_BLACK_WHITE_TOP_LEFT  = 6;
    private static final int ICON_DOT_INACTIVE          = 7;
    
    private static final String[] iconPaths_ = { 
        "/i/dot.png"      // 0
        , "/i/bg-tl.png"    // 1
        , "/i/b-tr.png"     // 2
        , "/i/g-tr.png"     // 3
        , "/i/bg-tr.png"    // 4
        , "/i/b-tr-2.png"   // 5
        , "/i/black-tl.png" // 6
        , "/i/dot-inactive.png"      // 7
    };
    private Image[] icons_ = new Image[iconPaths_.length];
    
    private Font bigFont_       = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private Font smallFont_     = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);

    private UIController controller;
    
    /** 
     * Creates a new instance of Progress bar and initialize it. 
     */
    public ProgressBarUI(UIController controller) {
	this.controller = controller;
        initCommand();
        initUI();
    }

    /**
     * Second-level initialization. Called before show this form.
     * @param message Progress bar status message.
     * @param stoppable New stoppable flag value. If it is <b>true</b>, then 
     * <i>Stop</i> button will be show on <i>Progress bar</i> form and user can 
     * break current long-time operation. Othrwise, if stoppable flag is 
     * <b>false</b>, then user can't break opration.
     */
    public void init(String message, boolean stoppable) {
        setMessage(message);
        setStoppable(stoppable);
        progress_ = 0;
        stopped_ = false;
    }
    
    // ProgressObserver implementation /////////////////////////////////////////
    
    /**
     * This method called for update long-time operation progress.
     * @see ru.mobicomk.mfradio.iface.ProgressObserver
     * @see ru.mobicomk.mfradio.iface.ProgressObserver#updateProgress
     */
    public void updateProgress() {
        progress_++;
        if (progress_ > PROGRESS_MAX) {
            progress_ = 0;
        }
        repaint();
    }

    /**
     * Set new progress bar status message.
     * @param message New status message value.
     * @see ru.mobicomk.mfradio.iface.ProgressObserver
     * @see ru.mobicomk.mfradio.iface.ProgressObserver#setMessage
     */
    public void setMessage(String message) {
        message_ = message;
        repaint();
    } 

    /**
     * Get <i>stoppable</i> flag value.
     * @return <b>true</b> if <i>stoppable</i> flag is set, <b>false</b> othrwise.
     * @see ru.mobicomk.mfradio.ui.ProgressBarUI
     * @see ru.mobicomk.mfradio.iface.ProgressObserver
     * @see ru.mobicomk.mfradio.iface.ProgressObserver#isStoppable
     */
    
    public boolean isStoppable() {
        return stoppable_;
    } 

    /**
     * Set <i>stoppable</i> flag to new state.
     * @param stoppable New value for <i>stoppable</i> flag.
     * @see ru.mobicomk.mfradio.ui.ProgressBarUI
     * @see ru.mobicomk.mfradio.iface.ProgressObserver
     * @see ru.mobicomk.mfradio.iface.ProgressObserver#setStoppable
     */
    public void setStoppable(boolean stoppable) {
        stoppable_ = stoppable;

        if (stoppable_) {
            addCommand(stopCommand_);
        } else {
            removeCommand(stopCommand_);
        } 
    } 
    
    /**
     * Check if user select <i>Stop</i> command.
     * @return <b>true</b> if user selects a <i>Stop</i> command, 
     * <b>false</b> in other case.
     * @see ru.mobicomk.mfradio.iface.ProgressObserver
     * @see ru.mobicomk.mfradio.iface.ProgressObserver#isStopped
     */
    public boolean isStopped() {
        return stopped_;
    } 
    
    // CommandListener implementation //////////////////////////////////////////
    
    /** 
     * CommandListener interface implementation.
     * @see javax.microedition.lcdui.CommandListener
     * @see javax.microedition.lcdui.CommandListener#commandAction
     */
    public void commandAction(Command c, Displayable d) {
        if (c == stopCommand_) {
            stopped_ = true;
        } 
    } 

    // Canvas implementation ///////////////////////////////////////////////////
    
    /*
     * {@link Canvas} interface implementaion.
     * @see javax.microedition.lcdui.Canvas
     * @see javax.microedition.lcdui.Canvas#paint
     */
    protected void paint(Graphics graphics) {
        if (buffer_ == null) {
            buffer_ = Image.createImage(graphics.getClipWidth(), graphics.getClipHeight());
        }
        
        int width = buffer_.getWidth();
        int height = buffer_.getHeight();
        Graphics g = buffer_.getGraphics();
        
        paintBackground(g, 0, 0, width, height);
        paintProgress(g, 0, 0, width, height);
        
        if (message_ != null) {
            paintMessage(g, 0, 0, width, height);
        }
        
        graphics.drawImage(buffer_, 0, 0, Graphics.TOP | Graphics.LEFT);        
    }

    // Privates ////////////////////////////////////////////////////////////////
    
    /*
     * Initialize all UI components and add it to the form.
     */
    private void initUI() {
        for (int i = 0; i < iconPaths_.length; i++) {
            try {
                icons_[i] = Image.createImage(iconPaths_[i]);
            } catch (IOException ioe) {
            }
        }
    }
    
    private void initCommand() {
        stopCommand_ = new Command(controller.getLocale().getString(Constants.STR_Stop), Command.STOP, 10);
        setCommandListener(this);        
    }
    
    private void paintBackground(Graphics g, int x, int y, int width, int height) {
        Image img = icons_[ICON_BLACK_WHITE_TOP_LEFT];

        int span2 = Constants.UI_ITEM_SPAN * 2;        
        int left = x + Constants.UI_ITEM_SPAN;
        int right = x + width - Constants.UI_ITEM_SPAN - img.getWidth();
        int top = y + Constants.UI_ITEM_SPAN;
        int bottom = y + height - Constants.UI_ITEM_SPAN - img.getHeight();
        
        g.setColor(Constants.COLOR_BLACK);
        g.fillRect(x, y, width, height);
        g.setColor(Constants.COLOR_BACKGROUND);
        g.fillRect(left, top, width - span2, height - span2);
        
        drawImageTL(g, img, Sprite.TRANS_NONE, left, top);
        drawImageTL(g, img, Sprite.TRANS_ROT90, right, top);
        drawImageTL(g, img, Sprite.TRANS_ROT180, right, bottom);
        drawImageTL(g, img, Sprite.TRANS_ROT270, left, bottom);
    }

    private void drawImageTL(Graphics g, Image image, int transform, int x, int y) {
        g.drawRegion(image, 0, 0, image.getWidth(), image.getHeight(), transform, x, y, Graphics.TOP | Graphics.LEFT);
    }

    private void paintProgress(Graphics g, int x, int y, int width, int height) {
        int margin2 = Constants.UI_ITEM_MARGIN * 2;
        int x0 = x + Constants.UI_ITEM_MARGIN;
        int y0 = y + Constants.UI_ITEM_MARGIN;
        int clientX = x0 + Constants.UI_ITEM_SPAN;
        int clientY = y0 + Constants.UI_ITEM_SPAN;
        int clientWidth = width - ((Constants.UI_ITEM_MARGIN + Constants.UI_ITEM_SPAN) * 2);
        
        Image imgDot = icons_[ICON_DOT];
        //Image imgDotInactive = icons_[ICON_DOT_INACTIVE];
        Image imgBlueGreenTL = icons_[ICON_BLUE_GREEN_TOP_LEFT];
        Image imgBlueTR = icons_[ICON_WHITE_BLUE_TOP_RIGHT];
        Image imgGreenTR = icons_[ICON_WHITE_GREEN_TOP_RIGHT];
        Image imgBlueGreenTR = icons_[ICON_BLUE_GREEN_TOP_RIGHT];
        Image imgBlueTR1 = icons_[ICON_WHITE_BLUE_RIGHT];
        
        Font font;
        int color;
        int songX = 0;
        int songY = 0;
        
        // draw progress background
        g.setColor(Constants.COLOR_LIST_BACKGROUND);
        g.fillRect(x0, y0, width - margin2, height - margin2);
        
        // draw corners
        drawImageTL(g, imgBlueTR, Sprite.TRANS_ROT270
            , x0 - 1 
            , y0 - 1);
        drawImageTL(g, imgBlueTR, Sprite.TRANS_NONE
            , x + width - Constants.UI_ITEM_MARGIN - imgBlueTR.getWidth() + 1
            , y0 - 1);        
        drawImageTL(g, imgBlueTR, Sprite.TRANS_ROT90
            , x + width - Constants.UI_ITEM_MARGIN - imgBlueTR.getHeight() + 1
            , y + height - Constants.UI_ITEM_MARGIN - imgBlueTR.getWidth() + 1);
        drawImageTL(g, imgBlueTR, Sprite.TRANS_ROT180
            , x0 - 1
            , y + height - Constants.UI_ITEM_MARGIN - imgBlueTR.getWidth() + 1);
        
        font = bigFont_;
        color = Constants.COLOR_LIST_TEXT;
        songX = clientX;
        songY = (height - font.getHeight())/2;
            
        g.setColor(Constants.COLOR_BACKGROUND);
        g.fillRect(clientX + Constants.UI_ITEM_SPAN, songY - 1, clientWidth , font.getHeight() + 2);
        
        int x1 = clientX + Constants.UI_ITEM_SPAN + clientWidth + 1 - imgBlueTR.getHeight();
        if (songY - 1 - imgBlueTR.getHeight() > y0) {
            drawImageTL(g, imgBlueTR, Sprite.TRANS_ROT90
                , x1
                , songY - imgBlueTR.getHeight());
        } else {
            drawImageTL(g, imgBlueTR1, Sprite.TRANS_NONE
                , x1-1
                , songY - 1 - imgBlueTR1.getWidth());
        }
        
        if ((songY + font.getHeight() + imgBlueTR.getHeight()) < (y0 + height - margin2)) {
            drawImageTL(g, imgBlueTR, Sprite.TRANS_NONE
                , x1
                , songY + font.getHeight());
        } else {
            drawImageTL(g, imgBlueTR1, Sprite.TRANS_NONE
                , x1-1
                , songY + font.getHeight() + 1);
        }
        
        // draw cursor
        g.setColor(Constants.COLOR_SELECTED);
        g.fillRect(clientX + Constants.UI_ITEM_SPAN + 1, songY, clientWidth - 1, font.getHeight());
        
        x1 = clientX + Constants.UI_ITEM_SPAN + 1 + clientWidth - imgGreenTR.getWidth();
        
        drawImageTL(g, imgGreenTR, Sprite.TRANS_NONE
            , x1
            , songY - 1);
        drawImageTL(g, imgGreenTR, Sprite.TRANS_ROT90
            , x1
            , songY + font.getHeight() + 1 - imgGreenTR.getWidth());
        
        drawImageTL(g, imgBlueGreenTL, Sprite.TRANS_NONE
            , clientX + Constants.UI_ITEM_SPAN
            , songY - 1);        
        drawImageTL(g, imgBlueGreenTL, Sprite.TRANS_ROT270
            , clientX + Constants.UI_ITEM_SPAN
            , songY + font.getHeight() + 1 - imgBlueGreenTL.getWidth());
        
        // dots
        int dotShift = (clientWidth - (Constants.UI_ITEM_SPAN * 2)) / PROGRESS_MAX;
        int dotX = clientX + Constants.UI_ITEM_SPAN + dotShift/2;
        int dotY = songY + (font.getHeight() / 2) - imgDot.getHeight()/2;
        
        //int dotShift = imgDot.getWidth() + Constants.UI_ITEM_SPAN;
        /*
        for (int i = 0; i< progress_; i++) {
            g.drawImage(imgDot, dotX + i * dotShift, dotY, Graphics.TOP | Graphics.LEFT);
        }*/
        
        for (int i = 0; i< PROGRESS_MAX; i++) {
            if (i >= (progress_ - 2) && i <= progress_)
                g.drawImage(imgDot, dotX + i * dotShift, dotY, Graphics.TOP | Graphics.LEFT);
        }
    }

    private void paintMessage(Graphics g, int x, int y, int width, int height) {
        int margin2 = Constants.UI_ITEM_MARGIN * 2;
        int x0 = x + Constants.UI_ITEM_MARGIN;
        int y0 = y + height - Constants.UI_ITEM_MARGIN;
        int left = x0 + Constants.UI_ITEM_SPAN + Constants.UI_ITEM_MARGIN;
        int bottom = y0;
        int maxStringWidth = width - ((Constants.UI_ITEM_MARGIN + Constants.UI_ITEM_SPAN) * 2);
        
        char[] chars = message_.toCharArray();
        int length = message_.length();

        while (smallFont_.charsWidth(chars, 0, length) > maxStringWidth) {
           length--;
        }

        g.setColor(Constants.COLOR_WHITE);
        g.setFont(smallFont_);
        g.drawSubstring(message_, 0, length, left, bottom, Graphics.LEFT | Graphics.BOTTOM);
    }
    
}
