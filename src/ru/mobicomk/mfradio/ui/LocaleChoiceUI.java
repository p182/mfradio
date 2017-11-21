/*
 *    MFRadio - stream radio client for Java 2 Micro Edition
 *    Copyright (C) 2001 - 2006 Mobicom-Kavkaz, Inc
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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.util.Locale;

/**
 * Language choice form
 * 
 * @author Alexey Rybalko
 * 
 */
public class LocaleChoiceUI extends Form implements CommandListener {

    private UIController controller;

    private ChoiceGroup localeChoice;

    private Command okCommand;

    private Command cancelCommand;

    private Locale[] locales;

    public LocaleChoiceUI(UIController controller) {
        super(controller.getLocale().getString(Constants.STR_Lang));

        this.controller = controller;

        initCommands();
        initUI();
    }

    private void initUI() {
        localeChoice = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);
        append(localeChoice);
    }

    private void initCommands() {
        okCommand = new Command(controller.getLocale().getString(Constants.STR_OK), Command.OK, 1);
        cancelCommand = new Command(controller.getLocale().getString(Constants.STR_Cancel), Command.CANCEL, 1);
        addCommand(okCommand);
        addCommand(cancelCommand);
        setCommandListener(this);
    }

    public void init(Locale[] locales) {
        this.locales = locales;
        for (int i = 0; i < locales.length; i++) {
            localeChoice.append(locales[i].getNativeName(), null);
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        Locale newLocale = null;
        if (command == okCommand) {
            newLocale = locales[localeChoice.getSelectedIndex()];

        }
        controller.endLocaleChoiceRequest(newLocale);
    }

    public void setSelectedLocale(Locale selectedLocale) {
        if (locales != null) {
            for (int i = 0; i < locales.length; i++) {
                if (locales[i].equals(selectedLocale)) {
                    localeChoice.setSelectedIndex(i, true);
                    break;
                }
            }
        }
    }

}
