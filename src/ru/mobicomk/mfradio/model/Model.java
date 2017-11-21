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

package ru.mobicomk.mfradio.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.iface.ModelListener;
import ru.mobicomk.mfradio.util.StationInfo;

/**
 * <h2>Application data model class </h2>
 * Model containts station playlist, volume level information 
 * and locale for the Player
 *
 * @author Roman Bondarenko, Alexey Rybalko
 * @see ru.mobicomk.mfradio.controller.UIController
 */
public class Model {
    private static final int SETTING_LOCALE = 1;

    /** Maximal player volume level. */
    public static final int MAX_VOL = 100;

    /** Minimal player volume level. */
    public static final int MIN_VOL = 0;

    private Vector titles_;

    private Vector urls_;

    private int selectedIdx_;

    private int volume_; // 0..100

    private ModelListener listener_;

    private MIDlet midlet_;

    //TODO use hash map for common handling of all other settings: load/save, set/get.
    private String localeName;

    /**
     * Creates a new instance of the Model.
     * @param midlet MIDlet object.
     */
    public Model(MIDlet midlet) {
        midlet_ = midlet;
        volume_ = 50;
        selectedIdx_ = -1;
        listener_ = null;

        titles_ = new Vector();
        urls_ = new Vector();
    }

    /**
     * Set listener for this model object. Only one listener may be joined 
     * for model events. 
     * @param listener New listener object.
     */
    public void setModelListener(ModelListener listener) {
        listener_ = listener;
    }

    /**
     * Load application data from storage (RMS) or application properties 
     * into this model object.
     */
    public void load() {
        String[] storesNames = RecordStore.listRecordStores();

        if (!titles_.isEmpty()) {
            titles_.removeAllElements();
            urls_.removeAllElements();
        }

        if (storesNames == null) {
            loadFromProps();
        } else {
            loadSettings();
            loadPlayList();
        }

        if (titles_.size() > 0) {
            selectedIdx_ = 0;
        }
        firePlaylistChanges();
    }

    /**
     * Save application data from this model object into storage (RMS).
     */
    public void save() {
        storeSettings();
        storePlayList();
    }

    /**
     * Update station information in playlist.
     * @param idx Index of the station in playlist.
     * @param si New station information.
     */
    public void setStationInfo(int idx, StationInfo si) {
        urls_.setElementAt(si.Url, idx);
        titles_.setElementAt(si.Title, idx);
        firePlaylistChanges();
    }

    /**
     * Get station information from playlist by position.
     * @param idx Index of the station in playelist.
     * @return Station information object.
     */
    public StationInfo getStationInfo(int idx) {
        if (idx > -1 && idx < urls_.size()) {
            return new StationInfo((String) urls_.elementAt(idx), (String) titles_.elementAt(idx));
        }
        return null;
    }

    /**
     * Get station information from playlist by URL.
     * @param url URL of a station.
     * @return Station information object.
     */
    public StationInfo getStationInfo(String url) {
        return getStationInfo(urls_.indexOf(url));
    }

    /**
     * Add station at tail of the play list. 
     * This method fire <i>Playlist Changes</i> notification of the 
     * {@link ModelListener} interface.
     * @param si New station information.
     */
    public void addStationInfo(StationInfo si) {
        urls_.addElement(si.Url);
        titles_.addElement(si.Title);
        firePlaylistChanges();
    }

    /**
     * Remove station information from playlist. Find station by position. 
     * This method fire <i>Playlist Changes</i> notification of the 
     * {@link ModelListener} interface.
     * @param idx Index of the station in playlist.
     */
    public void deleteStationInfo(int idx) {
        urls_.removeElementAt(idx);
        titles_.removeElementAt(idx);
        firePlaylistChanges();
    }

    /**
     * Volume level accessor.
     * @return Volume level value.
     */
    public int getVolume() {
        return volume_;
    }

    /**
     * Set new volume level value.
     * @param newVolume New volume level value.
     */
    public void setVolume(int newVolume) {
        volume_ = newVolume;
    }

    /**
     * Selected (current) station position accessor.
     * @return Selected station position.
     */
    public int getSelectedStationIdx() {
        return selectedIdx_;
    }

    /**
     * Select new station in playlist.
     * @param idx New selected station index.
     * @throws java.lang.IndexOutOfBoundsException if given index out of range.
     */
    public void selectStation(int idx) throws IndexOutOfBoundsException {
        if (idx > titles_.size() || idx < -1) {
            throw new IndexOutOfBoundsException();
        }

        selectedIdx_ = idx;
    }

    /**
     * Playlist stations titles accessor.
     * @return Playlist stations titles.
     */
    public Enumeration getStationTitles() {
        return titles_.elements();
    }

    /**
     * Size of the playlist accessor.
     * @return Size of the playlist.
     */
    public int getStationsCount() {
        return titles_.size();
    }

    // Privates ////////////////////////////////////////////////////////////////

    private void firePlaylistChanges() {
        if (listener_ != null) {
            listener_.playlistChanges(titles_.elements(), titles_.size());
        }
    }

    private void loadFromProps() {
        for (int n = 1; n < 100; n++) {
            String url = midlet_.getAppProperty(Constants.APP_RADIO_URL_KEY_PREFIX + n);
            if (url == null || url.length() == 0) {
                break;
            }
            String title = midlet_.getAppProperty(Constants.APP_RADIO_TITLE_KEY_PREFIX + n);
            if (title == null || title.length() == 0) {
                title = url;
            }
            titles_.addElement(title);
            urls_.addElement(url);
        }
        String localeName = midlet_.getAppProperty(Constants.APP_LOCALE_KEY);
        this.localeName = (localeName != null) ? localeName : Constants.APP_DEFAULT_LOCALE;
    }

    private void loadPlayList() {
        DataInputStream dis;
        RecordStore rs;
        RecordEnumeration e;
        try {
            rs = RecordStore.openRecordStore(Constants.APP_RMS_PLAYLIST, false);
            e = rs.enumerateRecords(null, null, false);
            while (e.hasNextElement()) {
                dis = new DataInputStream(new ByteArrayInputStream(e.nextRecord()));
                try {
                    urls_.addElement(dis.readUTF());
                    titles_.addElement(dis.readUTF());
                } catch (IOException ex) {
                    //
                }
            }
            e.destroy();
            rs.closeRecordStore();
        } catch (RecordStoreFullException fullStore) {
            //handle a full record store problem
        } catch (RecordStoreNotFoundException notFoundException) {
            //handle store not found which should not happen with the
            //createIfNecessary tag set to true
        } catch (RecordStoreException recordStoreException) {
            //handling record store problems
        }
    }

    private void loadSettings() {
        DataInputStream dis;
        RecordStore rs;
        try {
            rs = RecordStore.openRecordStore(Constants.APP_RMS_SETTINGS, false);
            dis = new DataInputStream(new ByteArrayInputStream(rs.getRecord(SETTING_LOCALE)));
            try {
                localeName = dis.readUTF();
            } catch (IOException ex) {

            }
            //proceed other settings' records if needed

            rs.closeRecordStore();
        } catch (RecordStoreFullException fullStore) {
            //handle a full record store problem
        } catch (RecordStoreNotFoundException notFoundException) {
            //handle store not found which should not happen with the
            //createIfNecessary tag set to true
        } catch (RecordStoreException recordStoreException) {
            //handling record store problems
        }
    }

    private void storePlayList() {
        byte[] rec;
        ByteArrayOutputStream os;
        DataOutputStream dos;
        RecordStore rs;
        RecordEnumeration e;
        Enumeration urlsEnum = urls_.elements();
        Enumeration titlesEnum = titles_.elements();
        try {
            rs = RecordStore.openRecordStore(Constants.APP_RMS_PLAYLIST, true);
            e = rs.enumerateRecords(null, null, false);
            while (e.hasNextElement() && urlsEnum.hasMoreElements()) {
                os = new ByteArrayOutputStream();
                dos = new DataOutputStream(os);
                try {
                    dos.writeUTF((String) urlsEnum.nextElement());
                    dos.writeUTF((String) titlesEnum.nextElement());
                    dos.close();
                    rec = os.toByteArray();
                    rs.setRecord(e.nextRecordId(), rec, 0, rec.length);
                } catch (IOException ex) {
                    //
                } finally {
                    dos = null;
                    os = null;
                }
            }

            if (urlsEnum.hasMoreElements()) {
                // add new records
                while (urlsEnum.hasMoreElements()) {
                    os = new ByteArrayOutputStream();
                    dos = new DataOutputStream(os);

                    try {
                        dos.writeUTF((String) urlsEnum.nextElement());
                        dos.writeUTF((String) titlesEnum.nextElement());
                        dos.close();
                        rec = os.toByteArray();

                        rs.addRecord(rec, 0, rec.length);
                    } catch (IOException ex) {
                        // 
                    } finally {
                        dos = null;
                        os = null;
                    }
                }
            } else if (e.hasNextElement()) {
                // delete next records
                while (e.hasNextElement()) {
                    rs.deleteRecord(e.nextRecordId());
                }
            }
            e.destroy();
            rs.closeRecordStore();
        } catch (RecordStoreFullException fullStore) {
            //handle a full record store problem
        } catch (RecordStoreNotFoundException notFoundException) {
            //handle store not found which should not happen with the
            //createIfNecessary tag set to true
        } catch (RecordStoreException recordStoreException) {
            //handling record store problems
        } catch (java.lang.NullPointerException npe) {
            //
        }
    }

    private void storeSettings() {
        byte[] rec;
        ByteArrayOutputStream os;
        DataOutputStream dos;
        RecordStore rs;
        try {
            rs = RecordStore.openRecordStore(Constants.APP_RMS_SETTINGS, true);
            try {
                os = new ByteArrayOutputStream();
                dos = new DataOutputStream(os);
                dos.writeUTF(localeName);
                rec = os.toByteArray();
                if (rs.getNumRecords() == 0) {
                    rs.addRecord(rec, 0, rec.length);
                    //add other settings data serialy
                } else {
                    rs.setRecord(SETTING_LOCALE, rec, 0, rec.length);
                    //set other settings data serialy
                }
                os.close();
            } catch (IOException ex) {

            }
            rs.closeRecordStore();
        } catch (RecordStoreFullException fullStore) {
            //handle a full record store problem
        } catch (RecordStoreNotFoundException notFoundException) {
            //handle store not found which should not happen with the
            //createIfNecessary tag set to true
        } catch (RecordStoreException recordStoreException) {
            //handling record store problems
        } catch (java.lang.NullPointerException npe) {
            //
        }
    }

    /**
     * Tests if playlist containts station with given URL.
     * @param url URL for check.
     * @return <b>true</b> if playlist containts station with given URL, 
     * <b>false</b> othrwise.
     */
    public boolean hasURL(String url) {
        return urls_.contains(url);
    }

    public String getLocaleName() {
        return localeName;
    }

    public void setLocaleName(String localeName) {
        this.localeName = localeName;
        firePlaylistChanges();
    }

}
