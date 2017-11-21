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

package ru.mobicomk.mfradio.ui;

import java.io.IOException;
import java.util.Enumeration;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.iface.ModelListener;
import ru.mobicomk.mfradio.model.Model;
import ru.mobicomk.mfradio.util.Locale;

/**
 * <i>Player</i> form class. This is a main form of the application. It displays
 * afetr application start and contents playlist and volume control.
 *
 * @author  Roman Bondarenko
 * @see UIController
 */
public class PlayerUI
    extends Canvas
    implements CommandListener, PlayerListener, ModelListener {
    
    // Application controller
    private UIController controller_;
    
    private String[] stationTitle_ = new String[0];
    private int selectedStationIdx_ = 0;
    private int playedStationIdx_ = -1;
    private int volume_ = 0;
    
    private String status_ = "";
    private int pressedButton_ = 0;
    
    // Commands
    private Command exit_;
    private Command play_;
    private Command stop_;
    private Command edit_;
    private Command delete_;
    private Command add_;
    private Command help_;
    private Command lang_;
    
    private Command showLog_;
    
    // UI components
    private static final int ICON_UP                    = 0;
    private static final int ICON_UP_PRESSED            = 1;
    private static final int ICON_SMALL_LOGO            = 2;
    private static final int ICON_DOT                   = 3;
    private static final int ICON_BLUE_GREEN_TOP_LEFT   = 4;
    private static final int ICON_WHITE_BLUE_TOP_RIGHT  = 5;
    private static final int ICON_WHITE_GREEN_TOP_RIGHT = 6;
    private static final int ICON_BLUE_GREEN_TOP_RIGHT  = 7;
    private static final int ICON_WHITE_BLUE_RIGHT      = 8;
    private static final int ICON_BLACK_WHITE_TOP_LEFT  = 9;
    
    private static final String[] iconPaths_ = {
        "/i/up.png" // 0
            , "/i/up_pressed.png"   // 1
            , "/i/mflogo-small.png" // 2
            , "/i/dot.png"      // 3
            , "/i/bg-tl.png"    // 4
            , "/i/b-tr.png"     // 5
            , "/i/g-tr.png"     // 6
            , "/i/bg-tr.png"    // 7
            , "/i/b-tr-2.png"   // 8
            , "/i/black-tl.png"   // 9
    };
    private Image[] icons_ = new Image[iconPaths_.length];
    private Image cachedScreen_;
    
    private Font bigFont_       = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private Font bigBoldFont_   = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
    private Font bigBoldUnderlinedFont_ = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_UNDERLINED, Font.SIZE_LARGE);
    private Font smallBoldFont_ = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
    private Font smallFont_     = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private Font smallUnderlinedFont_ = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_UNDERLINED, Font.SIZE_SMALL);
    
    private boolean isColor_;
    private Image buffer_ = null;
    private Drawer[] drawers_;

    
    /**
     * Creates a new instance of Player form and initialize it.
     * @param controller application controller object.
     */
    public PlayerUI(UIController controller) {
        super() ;
        controller_ = controller;
        
        initUI();
        initCommands();
        
        controller_.setPlayerListener(this);
        controller_.setModelListener(this);
        
        isColor_ = controller_.isColor();
    }
    
    /**
     * Update all form elements.
     */
    public void updateUI() {
        updateState();
        updateMenu();
        repaint();
    }
    
    /**
     * Set new form status line.
     * @param status new status message.
     */
    public void setStatus(String status) {
        status_ = status;
        repaint();
    }
     
    /**
     * Overrides method <i>keyPressed</i> of Canvas class.
     * @param keyCode the key code of the key that was pressed.
     * @see javax.microedition.lcdui.Canvas
     */
    public void keyPressed(int keyCode) {
        pressedButton_ = getGameAction(keyCode);
        repaint();
    }
    
    /**
     * Overrides method <i>keyReleased</i> of Canvas class.
     * @param keyCode the key code of the key that was released.
     * @see javax.microedition.lcdui.Canvas
     */
    public void keyReleased(int keyCode) {
        pressedButton_ = 0;
        //repaint();
        
        handle(getGameAction(keyCode));
        //update();
    }
    
    /** 
     * CommandListener interface implementation.
     * @see javax.microedition.lcdui.CommandListener
     */
    public void commandAction(Command c, Displayable d) {
        if (c == exit_) {
            controller_.exitMIDlet();
        } else if (c == play_) {
            controller_.playStation();
        } else if (c == stop_) {
            controller_.stopPlayStation(this);
        } else if (c == edit_) {
            controller_.editStationRequested();
        } else if (c == delete_) {
            controller_.deleteStation();
        } else if (c == add_) {
            controller_.addStationRequested();
        } else if (c == showLog_) {
            controller_.showLog();
        } else if (c == help_) {
            controller_.showHelp(HelpUI.HELP_PLAYERUI_TOPIC); // TODO: remove HelpUI import
        } else if (c == lang_) {
            controller_.localeChoiceRequested();
        }
    }   
    
    /**
     * Canvas interface method implementation.
     * @param graphics the Graphics object to be used for rendering the Canvas.
     * @see javax.microedition.lcdui.Canvas
     */
    public void paint(Graphics graphics) {
        
        /* TODO: add cache support
        if (cachedScreen_ == null) {
            //buildCachedScreen(graphics); // prepare not mutable parts
            cachedScreen_ = Image.createImage(graphics.getClipWidth(), graphics.getClipHeight());
         
            int width = cachedScreen_.getWidth();
            int height = cachedScreen_.getHeight();
            Graphics g = cachedScreen_.getGraphics();
         
            drawers_[0].cachedDraw(g, 0, 0, width, height);
         
            int topOffset = 0;
            int itemHeight = 0;
            for (int idx = 1; idx < drawers_.length - 1; idx++) {
                itemHeight = drawers_[idx].getHeight();
                drawers_[idx].cachedDraw(g, 0, topOffset, width, itemHeight);
                topOffset += itemHeight;
            }
         
            drawers_[drawers_.length - 1].cachedDraw(g, 0, 0, width, height);
        }
         
        // draw not mutable parts
        graphics.drawImage(cachedScreen_, 0, 0, Graphics.TOP | Graphics.LEFT);
        */
        
        if (buffer_ == null) {
            initPaint(graphics);
        }
        
        int width = buffer_.getWidth();
        int height = buffer_.getHeight();
        
        Graphics g = buffer_.getGraphics();
        
        drawers_[0].draw(g, 0, 0, width, height);
        
        int topOffset = 0;
        int itemHeight = 0;
        for (int idx = 1; idx < drawers_.length - 1; idx++) {
            itemHeight = drawers_[idx].getHeight();
            drawers_[idx].draw(g, 0, topOffset, width, itemHeight);
            topOffset += itemHeight;
        }
        
        drawers_[drawers_.length - 1].draw(g, 0, 0, width, height);
        
        graphics.drawImage(buffer_, 0, 0, Graphics.TOP | Graphics.LEFT);
    }
    
    // listeners ///////////////////////////////////////////////////////////////
    
    /**
     * {@link ModelListener} interface implementation.
     * @param titles new list of radio station titles from playlist.
     * @param count count of titles.
     * @see ru.mobicomk.mfradio.iface.ModelListener
     */
    public synchronized void playlistChanges(Enumeration titles, int count) {
	
        if (stationTitle_.length != count) {
            stationTitle_ = null;
            stationTitle_ = new String[count];
        }
        
        int idx=0;
        while (titles.hasMoreElements()) {
            stationTitle_[idx] = (String)titles.nextElement();
            idx++;
        }
    }
    
    /**
     * {@link PlayerListener} interface implementation.
     * @param player The player which generated the event.
     * @param string The event generated as defined by the enumerated types.
     * @param object The associated event data.
     * @see javax.microedition.media.PlayerListener
     */
    public synchronized void playerUpdate(Player player, String string, Object object) {
        setStatus(string);
    }
    
    // Privates ////////////////////////////////////////////////////////////////
    
    private void initCommands() {
        final Locale locale = controller_.getLocale();
        play_ = new Command(locale.getString(Constants.STR_Play), Command.SCREEN, 1);
        edit_ = new Command(locale.getString(Constants.STR_Edit), Command.SCREEN, 2);
        add_ = new Command(locale.getString(Constants.STR_Add), Command.SCREEN, 3);
        delete_ = new Command(locale.getString(Constants.STR_Delete), Command.SCREEN, 4);
        help_ = new Command(locale.getString(Constants.STR_Help), Command.SCREEN, 5);
        lang_ = new Command(locale.getString(Constants.STR_Lang), Command.SCREEN, 6);
        showLog_ = new Command("Log", Command.SCREEN, 6);
        stop_ = new Command(locale.getString(Constants.STR_Stop), Command.STOP, 1);
        exit_ = new Command(locale.getString(Constants.STR_Exit), Command.EXIT, 1);
        
        if (selectedStationIdx_ != -1) {
            addCommand(play_);
            addCommand(edit_);
            addCommand(delete_);
        }
        
        addCommand(add_);
        addCommand(help_);
        addCommand(showLog_);
        addCommand(exit_);
        addCommand(lang_);
        
        setCommandListener(this);
    }
    
    private void initUI() {
        for (int i = 0; i < iconPaths_.length; i++) {
            try {
                icons_[i] = Image.createImage(iconPaths_[i]);
            } catch (IOException ioe) {
                
            }
        }
    }
    
    private void initPaint(Graphics graphics) {
        buffer_ = Image.createImage(graphics.getClipWidth(), graphics.getClipHeight());
        
        int clientHeight = buffer_.getHeight();
        
        NavigationDrawer navDrawer = new NavigationDrawer();
        StatusDrawer statusDrawer = new StatusDrawer();
        Drawer playlistDrawer = null;
        
        int navigationHeight = navDrawer.getHeight();
        int statusHeight = statusDrawer.getHeight();
        int playlistHeight = clientHeight - navigationHeight - statusHeight;
        
        // correct playlist height & playlist display mode
        int plClientHeight = playlistHeight - Constants.UI_ITEM_MARGIN * 2;
        int plItemHeight = Constants.UI_ITEM_SPAN + smallFont_.getHeight();
        int plBigItemHeight = Constants.UI_ITEM_SPAN + bigFont_.getHeight();
        int plItemsCount = plClientHeight / plItemHeight;
        
        if (plItemsCount < 3) {
            //check for 1big + 1small string
            int pl2ItemsHeight = Constants.UI_ITEM_SPAN * 3 + bigFont_.getHeight() + smallFont_.getHeight();
            if (plClientHeight < pl2ItemsHeight) {
                // check for 1big string
                int pl1ItemHeight = Constants.UI_ITEM_SPAN * 2 + bigFont_.getHeight();
                if (plClientHeight < pl1ItemHeight) {
                    playlistDrawer = new Playlist1ItemDrawer(smallFont_, smallBoldFont_, playlistHeight);
                } else {
                    playlistDrawer = new Playlist1ItemDrawer(bigFont_, bigBoldFont_, playlistHeight);
                }
            } else {
                playlistDrawer = new PlaylistItemsDrawer(2, playlistHeight);
            }
        } else {
            int plItemsHeight = (plItemsCount - 1) * plItemHeight + plBigItemHeight + Constants.UI_ITEM_SPAN;
            if (plClientHeight < plItemsHeight) {
                playlistDrawer = new PlaylistItemsDrawer(plItemsCount - 1, playlistHeight);
            } else {
                playlistDrawer = new PlaylistItemsDrawer(plItemsCount, playlistHeight);
            }
        }
        
        drawers_ = new Drawer[5];
        drawers_[0] = new BackgroundDrawer();
        drawers_[1] = playlistDrawer;
        drawers_[2] = navDrawer;
        drawers_[3] = statusDrawer;
        drawers_[4] = new LogoDrawer();
    }
    
    private void handle(int action) {
        switch (action) {
            case Canvas.UP:    { controller_.selectPrevStation(); break; }
            case Canvas.DOWN:  { controller_.selectNextStation();break; }
            case Canvas.LEFT:  { controller_.decVolume(); break; }
            case Canvas.RIGHT: { controller_.incVolume(); break; }
            case Canvas.FIRE:  {
                if (selectedStationIdx_ > -1) {
                    togglePlayStation();
                } else {
                    controller_.addStationRequested();
                }
                break;
            }
        }
    }
    
    private void togglePlayStation() {
        if (playedStationIdx_ != -1) {
            if (playedStationIdx_ == selectedStationIdx_) {
                controller_.stopPlayStation(this);
            } else {
                controller_.playStation();
            }
        } else if (selectedStationIdx_ > -1) {
            controller_.playStation();
        }
    }
    
    private void setStopCommand() {
        removeCommand(play_);
        addCommand(stop_);
    }
    
    private void setPlayCommand() {
        removeCommand(stop_);
        addCommand(play_);
    }
    
     private void updateState() {
        selectedStationIdx_ = controller_.getSelectedStationIdx();
        playedStationIdx_ = controller_.getPlayedStationIdx();
        volume_ = controller_.getVolume();
    }
    
    private void updateMenu() {
        if (selectedStationIdx_ == -1) {
            removeCommand(edit_);
            removeCommand(play_);
            removeCommand(stop_);
            removeCommand(delete_);
        } else {
            addCommand(edit_);
            addCommand(delete_);
            if (selectedStationIdx_ == playedStationIdx_) {
                setStopCommand();
            } else {
                setPlayCommand();
            }
        }
    }
   

    // Helper classes //////////////////////////////////////////////////////////
    
    /*
     * Drawer interface
     */
    private interface Drawer {
        public void draw(Graphics g, int x, int y, int width, int height);
        public void cachedDraw(Graphics g, int x, int y, int width, int height);
        public int getHeight();
    }
    
    /*
     *
     */
    private class Playlist1ItemDrawer
        implements Drawer {
        
        private Font normalFont_;
        private Font selectedFont_;
        private int height_ = 0;
        private int listHeight_ = 0;
        private int margin2_ = Constants.UI_ITEM_MARGIN * 2;
        
        public Playlist1ItemDrawer(Font normalFont, Font selectedFont, int requestedHeight) {
            normalFont_ = normalFont;
            selectedFont_ = selectedFont;
            height_ = requestedHeight;
            listHeight_ = margin2_ + (Constants.UI_ITEM_SPAN * 2) + normalFont.getHeight();
        }
        
        public int getHeight() {
            return height_;
        }
        
        public void draw(Graphics g, int x, int y, int width, int height){
            int x0 = x + Constants.UI_ITEM_MARGIN;
            int y0 = y + Constants.UI_ITEM_MARGIN;
            int clientX = x0 + Constants.UI_ITEM_SPAN;
            int clientY = y0 + Constants.UI_ITEM_SPAN;
            int clientWidth = width - ((Constants.UI_ITEM_MARGIN + Constants.UI_ITEM_SPAN) * 2);
            
            // draw playlist background
            g.setColor(Constants.COLOR_LIST_BACKGROUND);
            g.fillRoundRect(x0, y0, width - margin2_, listHeight_ - margin2_, Constants.UI_ITEM_CORNER, Constants.UI_ITEM_CORNER);
            
            // draw song title
            Font font;
            int color;
            
            if (selectedStationIdx_ == playedStationIdx_) {
                font = selectedFont_;
                color = Constants.COLOR_SELECTED_TEXT;
            } else {
                font = normalFont_;
                color = Constants.COLOR_LIST_TEXT;
            }
            
            String song = stationTitle_[selectedStationIdx_];
            int songWidth = font.stringWidth(song);
            int songX = clientX + ((clientWidth - songWidth) / 2);
            
            g.setColor(color);
            g.setFont(font);
            g.drawString(song, songX, clientY, Graphics.TOP | Graphics.LEFT);
        }
        
        public void cachedDraw(Graphics g, int x, int y, int width, int height) {
        }
    }
    
    /**
     *
     */
    private class PlaylistItemsDrawer
        implements Drawer {
        
        private int itemsCount_ = 0;
        private int height_ = 0;
        private int listHeight_ = 0;
        private int margin2_ = Constants.UI_ITEM_MARGIN * 2;
        private int listItemsSpan = 4;
        
        public PlaylistItemsDrawer(int itemsCount, int requestedHeight) {
            itemsCount_ = itemsCount;
            height_ = requestedHeight;
            listHeight_ = margin2_ + (listItemsSpan * 2)
            + ((Constants.UI_ITEM_SPAN + smallFont_.getHeight()) * (itemsCount_ - 1))
            + (Constants.UI_ITEM_SPAN + bigFont_.getHeight() /* x1 */)
            + Constants.UI_ITEM_SPAN;
        }
        
        public int getHeight() {
            return height_;
        }
        
        public void draw(Graphics g, int x, int y, int width, int height){
            int x0 = x + Constants.UI_ITEM_MARGIN;
            int y0 = y + Constants.UI_ITEM_MARGIN;
            int clientX = x0 + Constants.UI_ITEM_SPAN;
            int clientY = y0 + Constants.UI_ITEM_SPAN;
            int clientWidth = width - ((Constants.UI_ITEM_MARGIN + Constants.UI_ITEM_SPAN) * 2);
            
            Image imgDot = icons_[ICON_DOT];
            Image imgBlueGreenTL = icons_[ICON_BLUE_GREEN_TOP_LEFT];
            Image imgBlueTR = icons_[ICON_WHITE_BLUE_TOP_RIGHT];
            Image imgGreenTR = icons_[ICON_WHITE_GREEN_TOP_RIGHT];
            Image imgBlueGreenTR = icons_[ICON_BLUE_GREEN_TOP_RIGHT];
            Image imgBlueTR1 = icons_[ICON_WHITE_BLUE_RIGHT];
            
            int left = x + Constants.UI_ITEM_MARGIN - 1;
            int top = y + Constants.UI_ITEM_MARGIN - 1;
            int right = x + width - Constants.UI_ITEM_MARGIN - imgBlueTR.getWidth() + 1;
            int bottom = y + listHeight_ - Constants.UI_ITEM_MARGIN - imgBlueTR.getWidth() + 1;
            
            int fromIdx = 0;
            int toIdx = -1;
            int maxIdx = stationTitle_.length - 1;
            
            if (stationTitle_.length > 0) {
                if (selectedStationIdx_ < maxIdx) {
                    toIdx = selectedStationIdx_ + 1;
                    fromIdx = toIdx - (itemsCount_ - 1);
                } else if (selectedStationIdx_ == 0) {
                    fromIdx = 0;
                    toIdx = (itemsCount_ - 1);
                } else if (selectedStationIdx_ == maxIdx) {
                    toIdx = selectedStationIdx_;
                    fromIdx = toIdx - (itemsCount_ - 1);
                }
                
                if (fromIdx < 0) {
                    toIdx += (0 - fromIdx);
                    fromIdx = 0;
                }
                
                if (toIdx > maxIdx) {
                    toIdx = maxIdx;
                }
            }
            
            Font font;
            int color;
            int songX = 0;
            int songY = 0;
            int songWidth = 0;
            int topOffset = listItemsSpan;
            
            //g.setColor(YELLOW);
            //g.drawRect(x0, y0, width - margin2_, height_ - margin2_);
            
            // draw playlist background
            g.setColor(Constants.COLOR_LIST_BACKGROUND);
            g.fillRect(x0, y0, width - margin2_, listHeight_ - margin2_);
            
            // draw corners
            drawImage(g, imgBlueTR, Sprite.TRANS_ROT270, left, top);
            drawImage(g, imgBlueTR, Sprite.TRANS_NONE,  right, top);
            drawImage(g, imgBlueTR, Sprite.TRANS_ROT90, right, bottom);
            drawImage(g, imgBlueTR, Sprite.TRANS_ROT180, left, bottom);
            
            for (int i = fromIdx; i <= toIdx; i++) {
                // draw item
                if (i == selectedStationIdx_) {
                    if (i == playedStationIdx_) {
                        // current played
                        font = isColor_ ? bigBoldFont_ : bigBoldUnderlinedFont_;
                        color = Constants.COLOR_SELECTED_TEXT;
                    } else {
                        // current
                        font = bigFont_;
                        color = Constants.COLOR_LIST_TEXT;
                    }
                } else {
                    if (i == playedStationIdx_) {
                        // normal played
                        font = /*isColor_ ? smallFont_ : */smallUnderlinedFont_;
                        color = Constants.COLOR_SELECTED_TEXT;
                    } else {
                        // normal
                        font = smallFont_;
                        color = Constants.COLOR_LIST_TEXT;
                    }
                }
                
                songWidth = font.stringWidth(stationTitle_[i]);
                songX = clientX + ((clientWidth - songWidth) / 2);
                songY = clientY + topOffset;
                
                int dotX = clientX + 3*Constants.UI_ITEM_SPAN + imgDot.getWidth();
                
                if (songX < (dotX + Constants.UI_ITEM_SPAN)) {
                    songX = dotX + Constants.UI_ITEM_SPAN;
                }
                
                if (i == selectedStationIdx_) {
                    g.setColor(Constants.COLOR_BACKGROUND);
                    g.fillRect(clientX + Constants.UI_ITEM_SPAN, songY - 1, clientWidth , font.getHeight() + 2);
                    
                    //int x1 = clientX + clientWidth + Constants.UI_ITEM_SPAN + 1;
                    int right1 = x + width - Constants.UI_ITEM_MARGIN - imgBlueTR1.getWidth();
                    
                    if (songY - 1 - imgBlueTR.getHeight() > y0) {
                        drawImage(g, imgBlueTR, Sprite.TRANS_ROT90
                            , right
                            , songY - imgBlueTR.getWidth());
                    } else {
                        drawImage(g, imgBlueTR1, Sprite.TRANS_NONE
                            , right1
                            , songY - imgBlueTR1.getHeight() - 1);
                    }
                    
                    if ((songY + font.getHeight() + imgBlueTR.getHeight()) < (y0 + listHeight_ - margin2_)) {
                        drawImage(g, imgBlueTR, Sprite.TRANS_NONE
                            , right
                            , songY + font.getHeight());
                    } else {
                        drawImage(g, imgBlueTR1, Sprite.TRANS_NONE
                            , right1
                            , songY + font.getHeight() + 1);
                    }
                    
                    // draw cursor
                    g.setColor(Constants.COLOR_SELECTED);
                    g.fillRect(clientX + Constants.UI_ITEM_SPAN + 1, songY, clientWidth - 1, font.getHeight());
                    
                    //int[] rgbData = new int[1];
                    //imgGreenTR.getRGB(rgbData, 0, 6, 0, 0, 1, 1);
                    //System.out.println("LIST_BACK: " + LIST_BACKGROUND + "; FROM IMG: " + rgbData[0]);
                    
                    //x1 = clientX + Constants.UI_ITEM_SPAN + 1 + clientWidth;
                    int x1 = x + width - Constants.UI_ITEM_MARGIN - imgGreenTR.getHeight() + 1;
                    
                    drawImage(g, imgGreenTR, Sprite.TRANS_NONE
                        , x1
                        , songY - 1);
                    
                    drawImage(g, imgGreenTR, Sprite.TRANS_ROT90
                        , x1
                        , songY + font.getHeight() - imgGreenTR.getWidth() + 1);
                    
                    drawImage(g, imgBlueGreenTL, Sprite.TRANS_NONE
                        , clientX + Constants.UI_ITEM_SPAN
                        , songY - 1);
                    
                    drawImage(g, imgBlueGreenTL, Sprite.TRANS_ROT270
                        , clientX + Constants.UI_ITEM_SPAN
                        , songY + font.getHeight() - imgBlueGreenTL.getWidth() + 1);
                    
                    // dot
                    g.drawImage(imgDot, clientX + 3*Constants.UI_ITEM_SPAN, songY + (font.getHeight() / 2) - imgDot.getHeight()/2, Graphics.TOP | Graphics.LEFT);
                }
                g.setFont(font);
                g.setColor(color);
                
                
                int maxMessageWidth = clientX + clientWidth - songX;
                char[] dots = {'.', '.', '.'};
                int dotsWidth = font.charsWidth(dots, 0, dots.length);
                char[] chars = stationTitle_[i].toCharArray();
                int length = stationTitle_[i].length();
                boolean needToTruncate = false;
                
                while (font.charsWidth(chars, 0, length) > maxMessageWidth) {
                    length--;
                    needToTruncate = true;
                }
                if (needToTruncate) {
                    length -= 3;
                    g.drawSubstring(stationTitle_[i], 0, length, songX, songY, Graphics.LEFT | Graphics.TOP);
                    songX += font.charsWidth(chars, 0, length);
                    g.drawString("...", songX, songY, Graphics.LEFT | Graphics.TOP);
                } else {
                    g.drawString(stationTitle_[i], songX, songY, Graphics.TOP | Graphics.LEFT);
                }
                
                topOffset += Constants.UI_ITEM_SPAN + font.getHeight();
            }
        }
        
        private void drawImage(Graphics g, Image image, int transform, int x, int y) {
            g.drawRegion(image, 0, 0, image.getWidth(), image.getHeight(), transform, x, y, Graphics.TOP | Graphics.LEFT);
        }
        
        public void cachedDraw(Graphics g, int x, int y, int width, int height) {
        }
    }
    
    /**
     *
     */
    private class BackgroundDrawer
        implements Drawer {
        
        private int span2_ = 0;
        
        public BackgroundDrawer() {
            span2_ = Constants.UI_ITEM_SPAN * 2;
        }
        
        public int getHeight() {
            return Constants.UI_ITEM_MARGIN;
        }
        
        public void draw(Graphics g, int x, int y, int width, int height) {
            Image img = icons_[ICON_BLACK_WHITE_TOP_LEFT];
            
            int left = x + Constants.UI_ITEM_SPAN;
            int right = x + width - Constants.UI_ITEM_SPAN - img.getWidth();
            int top = y + Constants.UI_ITEM_SPAN;
            int bottom = y + height - Constants.UI_ITEM_SPAN - img.getHeight();
            
            g.setColor(Constants.COLOR_BLACK);
            g.fillRect(x, y, width, height);
            g.setColor(Constants.COLOR_BACKGROUND);
            g.fillRect(left, top, width - span2_, height - span2_);
            
            drawImage(g, img, Sprite.TRANS_NONE,    left, top);
            drawImage(g, img, Sprite.TRANS_ROT90,  right, top);
            drawImage(g, img, Sprite.TRANS_ROT180, right, bottom);
            drawImage(g, img, Sprite.TRANS_ROT270,  left, bottom);
        }
        
        private void drawImage(Graphics g, Image image, int transform, int x, int y) {
            g.drawRegion(image, 0, 0, image.getWidth(), image.getHeight(), transform, x, y, Graphics.TOP | Graphics.LEFT);
        }
        
        public void cachedDraw(Graphics g, int x, int y, int width, int height) {
        }
    }
    
    /**
     *
     */
    private class LogoDrawer implements Drawer {
        
        private int span2_ = 0;
        
        public LogoDrawer() {
            span2_ = Constants.UI_ITEM_SPAN * 2;
        }
        
        public int getHeight() {
            return icons_[ICON_SMALL_LOGO].getHeight();
        }
        
        public void draw(Graphics g, int x, int y, int width, int height) {
            Image imgLogo = icons_[ICON_SMALL_LOGO];
            int x0 = x + width - Constants.UI_ITEM_MARGIN - imgLogo.getWidth();
            int y0 = y + height - Constants.UI_ITEM_MARGIN - imgLogo.getHeight();
            
            g.drawImage(imgLogo, x0 + 2, y0 + 2, Graphics.TOP | Graphics.LEFT);
        }
        
        public void cachedDraw(Graphics g, int x, int y, int width, int height) {
        }
    }
    
    /**
     *
     */
    private class StatusDrawer implements Drawer {
        
        private int height_ = 0;
        private int margin2_ = 0;
        
        public StatusDrawer(){
            margin2_ = 2 * Constants.UI_ITEM_MARGIN;
            height_ = margin2_ + (Constants.UI_ITEM_SPAN * 2) + smallFont_.getHeight();
        }
        
        public int getHeight() {
            return height_;
        }
        
        public void draw(Graphics g, int x, int y, int width, int height) {
            Image img = icons_[ICON_WHITE_BLUE_TOP_RIGHT];
            
            int left = x + Constants.UI_ITEM_MARGIN - 1;
            int right = x + width - Constants.UI_ITEM_MARGIN - img.getWidth() + 1;
            int top = y + Constants.UI_ITEM_MARGIN - 1;
            int bottom = y + height - Constants.UI_ITEM_MARGIN - img.getHeight() + 1;
            
            int clientX = x + Constants.UI_ITEM_MARGIN + Constants.UI_ITEM_SPAN;
            int clientY = y + Constants.UI_ITEM_MARGIN + Constants.UI_ITEM_SPAN;
            
            // draw background
            g.setColor(Constants.COLOR_LIST_BACKGROUND);
            g.fillRect(left + 1, top + 1, width - margin2_, height_ - margin2_);
            
            drawImage(g, img, Sprite.TRANS_ROT270, left, top);
            drawImage(g, img, Sprite.TRANS_NONE,  right, top);
            drawImage(g, img, Sprite.TRANS_ROT90, right, bottom);
            drawImage(g, img, Sprite.TRANS_ROT180, left, bottom);
            
            // draw text
            g.setColor(Constants.COLOR_LIST_TEXT);
            g.setFont(smallFont_);
            g.drawString(status_, clientX, clientY, Graphics.TOP | Graphics.LEFT);
        }
        
        private void drawImage(Graphics g, Image image, int transform, int x, int y) {
            g.drawRegion(image, 0, 0, image.getWidth(), image.getHeight(), transform, x, y, Graphics.TOP | Graphics.LEFT);
        }
        
        public void cachedDraw(Graphics g, int x, int y, int width, int height) {
        }
    }
    
    /**
     *
     */
    private class NavigationDrawer implements Drawer {
        
        private int steps_ = Model.MAX_VOL/10;
        private int height_ = 0;
        private int margin2_ = 0;
        private int volumeWidth_ = 0;
        private int stepWidth_ = 0;
        
        public NavigationDrawer(){
            margin2_ = 2 * Constants.UI_ITEM_MARGIN;
            height_ = /*margin2_ +*/ icons_[ICON_UP].getHeight() * 2 + icons_[ICON_UP].getHeight() / 2; // height * 2.5
        }
        
        public int getHeight() {
            return height_;
        }
        
        public void draw(Graphics g, int x, int y, int width, int height) {
            int x0 = x + Constants.UI_ITEM_MARGIN;
            int y0 = y + 2; // TODO
            int clientWidth = width - margin2_;
            int iconHeight = icons_[ICON_UP].getHeight();
            int iconWidth = icons_[ICON_UP].getWidth();
            
            if (volumeWidth_ == 0) {
                volumeWidth_ = clientWidth - ((iconWidth + Constants.UI_ITEM_SPAN) * 2);
                while ((volumeWidth_ / steps_) * steps_ != volumeWidth_) {
                    volumeWidth_--;
                }
                stepWidth_ = volumeWidth_ / steps_;
            }
            
            int volX = x0 + ((clientWidth - volumeWidth_) / 2);
            int volY = y0 + iconHeight;
            
            // left
            drawImage(g, icons_[pressedButton_ == LEFT ? ICON_UP_PRESSED : ICON_UP]
                , x0
                , volY - iconHeight/4
                , Sprite.TRANS_ROT270);
            
            // right
            drawImage(g, icons_[pressedButton_ == RIGHT ? ICON_UP_PRESSED : ICON_UP]
                , x0 + clientWidth - iconWidth
                , volY - iconHeight/4
                , Sprite.TRANS_ROT90);
            
            // top
            drawImage(g, icons_[pressedButton_ == UP ? ICON_UP_PRESSED : ICON_UP]
                , x0 + ((clientWidth - iconWidth) / 2)
                , y0
                , Sprite.TRANS_NONE);
            
            // bottom
            drawImage(g, icons_[pressedButton_ == DOWN ? ICON_UP_PRESSED : ICON_UP]
                , x0 + ((clientWidth - iconWidth) / 2)
                , y0 + iconHeight + (iconHeight / 2)
                , Sprite.TRANS_ROT180);
            
            for (int step = 0; step < steps_; step++) {
                g.setColor(step < (volume_/10) ? Constants.COLOR_LIST_BACKGROUND : Constants.COLOR_GRAY);
                g.fillRect(volX + step * stepWidth_, volY, stepWidth_, iconHeight/2);
                g.setColor(Constants.COLOR_BACKGROUND);
                g.drawRect(volX + step * stepWidth_, volY, stepWidth_, iconHeight/2);
            }
        }
        
        private void drawImage(Graphics g, Image img, int x, int y, int trans) {
            g.drawRegion(img, 0, 0, img.getWidth(), img.getHeight(), trans, x, y, Graphics.TOP | Graphics.LEFT);
        }
        
        public void cachedDraw(Graphics g, int x, int y, int width, int height) {
        }
    }
}
