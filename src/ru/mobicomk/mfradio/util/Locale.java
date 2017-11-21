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

import java.io.EOFException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * <h2>Native language text support for UI</h2>
 * <p>
 * The midlet uses very basic localization system which includes folowing UTF-8
 * plain text files:
 * 
 * <pre>
 *   lang.lst
 *   [locale name in &quot;lang.lst&quot;].lng
 *   [locale name in &quot;lang.lst&quot;].lng
 *   .... 
 * </pre>
 * 
 * File named <code>lang.lst</code> contains list of available loclaizations.
 * Each line of the file uses format
 * 
 * <pre>
 *  [locale name]=[language name in native spelling]
 * </pre>
 * 
 * For example: <code>en=English</code>. Other files contains localized
 * messages according to the file names. Thus file <code>en.lng</code> is used
 * for messages in English. Message file format is
 * 
 * <pre>
 *   [message key]=[message text]
 * </pre>
 * 
 * Simple formatting language is available for message text files. It uses some
 * character sequences and links to previosuly declared message keys in the
 * file. Tags "\p", "\h1" and "\h2" have the same meaning as in HTML. Each new
 * line of message lays on separate line in source file.
 * <p>
 * <h3>Multiline text example with formatting</h3>
 * 
 * <p>
 * <code><pre>
 *   PlayerUI_help_text=\h1 Controls
 *   \p   - select station in the list.
 *   \h2 Right(4)/Left(6)
 *   \p   - volume control.
 *   \h2 OK(5)
 *   \p   - start/stop station playback.
 *   \h1 Menu
 *   \h2 &lt;Add&gt;
 *   \p   - add station.
 *   \h2 &lt;Play&gt;/&lt;Stop&gt;
 *   \p   - play/stop on selected station.
 *   \h2 &lt;Edit&gt;
 *   \p   - change station settings.
 *   \h2 &lt;Delete&gt;
 *   \p   - remove station from playlist.
 *   \h2 &lt;Exit&gt;
 *   \p   - exit the player.
 * </pre></code>
 * 
 * <p>
 * It is possible to inline the text of other previously declared message in the
 * file.
 * <h3>Inlined text example</h3>
 * <p>
 * Source text that uses inlined messages: <code><pre>
 *   ...
 *   Playlist=Список станций
 *   ...
 *   ...
 *   WAPUI_help_text=...
 *   ...
 *   \p   - таким значком обозначены ссылки, которые не могут быть добавлены в Ваш '
 * </code>
 *  &amp;lt
 * <code>
 *  Playlist&gt;'.
 *   ...
 * </pre></code> Message with name <code>WAPUI_help_text</code> will be displayed
 * as: <code><pre>
 *  \p   - таким значком обозначены ссылки, которые не могут быть добавлены в Ваш 'Список станций'.
 * </pre></code> <br>
 * 
 * @author Alexey Rybalko
 * @see ru.mobicomk.mfradio.controller.UIController
 */
public final class Locale {

    private String locale;

    private Hashtable stringHash;

    private static Hashtable localeHash;

    // private static String[] availableLocaleNames;

    private Locale(String name) {
        this.locale = name;
    }

    public synchronized String getString(final String key) {
        if (stringHash == null) {
            stringHash = new Hashtable();
            loadProperties(stringHash, "/locale/" + locale + ".lng", true);
        }
        final String value = (String) stringHash.get(key);
        return (value != null) ? value : key;
    }

    public String getName() {
        return locale;
    }

    public String getNativeName() {
        loadLocaleHash();
        return (String) localeHash.get(locale);
    }

    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj.getClass().equals(getClass())) {
            Locale otherLocale = (Locale) obj;
            isEqual = otherLocale.getName().equals(locale);

        } else {
            isEqual = super.equals(obj);
        }
        return isEqual;
    }

    public String toString() {
        return locale;
    }

    /**
     * Get available locales for application
     * 
     * @return array of Locale objects
     */
    public static Locale[] getAvailable() {
        loadLocaleHash();
        Enumeration keysEnum = localeHash.keys();
        Locale[] locales = new Locale[localeHash.size()];
        int i = 0;
        while (keysEnum.hasMoreElements()) {
            String name = (String) keysEnum.nextElement();
            locales[i++] = new Locale(name);
        }
        return locales;
    }

    private static void loadLocaleHash() {
        if (localeHash == null) {
            localeHash = new Hashtable();
            loadProperties(localeHash, "/locale/lang.lst", false);
        }
    }

    /**
     * Get locale object for its name
     * 
     * @param localeName
     *            shortage for locale. E.g. "ru" for Russian
     * @return Locale object requested
     */
    public static Locale getForName(String localeName) {
        loadLocaleHash();
        return new Locale(localeName);
    }

    private static void loadProperties(Hashtable propertiesTable, String resourceName, boolean inlineTags) {
        try {
            propertiesTable.clear();
            InputStream inStream = propertiesTable.getClass().getResourceAsStream(resourceName);
            byte[] buf = new byte[1024];
            byte b;
            int bytesInLine = 0;

            String key = null;
            String lastKey = null;
            StringBuffer valueBuf = null;

            boolean EOF = false;
            while (!EOF) {
                b = (byte) inStream.read();
                switch (b) {

                    case -1:
                        EOF = true;
                    case '\n':
                        String str = decodeUTF8(buf, 0, bytesInLine);
                        int delimiterPos = str.indexOf('=');

                        if (delimiterPos != -1) {
                            if (lastKey != key && (valueBuf != null)) {
                                putToHash(propertiesTable, inlineTags, key, valueBuf);
                            }
                            lastKey = key;
                            key = str.substring(0, delimiterPos);
                            valueBuf = new StringBuffer(str.substring(delimiterPos + 1));
                        } else {
                            valueBuf.append('\n' + str);
                        }

                        if (EOF) {
                            putToHash(propertiesTable, inlineTags, key, valueBuf);
                        }

                        bytesInLine = 0;
                        break;

                    default:
                        if (b != '\r') {
                            buf[bytesInLine++] = b;
                        }
                }
            }
            inStream.close();
        } catch (EOFException ex) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.gc();
    }

    private static void putToHash(Hashtable propertiesTable, boolean inlineTags, String key, StringBuffer valueBuf) {
        String value = valueBuf.toString();
        if (inlineTags) {
            value = inlineTags(propertiesTable, value);
        }
        propertiesTable.put(key, value);
    }

    private static String decodeUTF8(byte data[], int offset, int length) {
        StringBuffer buff = new StringBuffer();
        int max = offset + length;
        for (int i = offset; i < max; i++) {
            char c = 0;
            if ((data[i] & 0x80) == 0) {
                c = (char) data[i];
            } else if ((data[i] & 0xe0) == 0xc0) {
                c |= ((data[i++] & 0x1f) << 6);
                c |= ((data[i] & 0x3f) << 0);
            } else if ((data[i] & 0xf0) == 0xe0) {
                c |= ((data[i++] & 0x0f) << 12);
                c |= ((data[i++] & 0x3f) << 6);
                c |= ((data[i] & 0x3f) << 0);
            } else if ((data[i] & 0xf8) == 0xf0) {
                c |= ((data[i++] & 0x07) << 18);
                c |= ((data[i++] & 0x3f) << 12);
                c |= ((data[i++] & 0x3f) << 6);
                c |= ((data[i] & 0x3f) << 0);
            } else {
                c = '?';
            }
            buff.append(c);
        }
        return buff.toString();
    }

    private static String inlineTags(Hashtable propertiesTable, final String value) {
        String inlinedStr = value;
        int tagStart;
        StringBuffer strBuf = new StringBuffer(inlinedStr);
        while ((tagStart = inlinedStr.indexOf('<')) != -1) {
            int tagEnd = inlinedStr.indexOf('>');
            String key = inlinedStr.substring(tagStart + 1, tagEnd);
            strBuf.delete(tagStart, tagEnd + 1);
            String msg = (String) propertiesTable.get(key);
            strBuf.insert(tagStart, (msg == null) ? key : msg);
            inlinedStr = strBuf.toString();
        }
        return inlinedStr;
    }
}
