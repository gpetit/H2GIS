/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.osm;

import org.xml.sax.SAXException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * A class to manage all common element properties.
 *
 * @author Erwan Bocher
 */
public class OSMElement {

    private final SimpleDateFormat dataFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final SimpleDateFormat dataFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private final HashMap<String, String> tags;
    private long id, uid;
    private String user;
    private int version, changeset;
    private boolean visible;
    private Timestamp timestamp;
    private String name = "";

    public OSMElement() {
        tags = new HashMap<String, String>();
    }

    /**
     * The id of the element
     *
     * @return id value
     */
    public long getID() {
        return id;
    }

    /**
     * Set an id to the element
     *
     * @param id set id value
     */
    public void setId(String id) {
        this.id = Long.valueOf(id);
    }

    /**
     * The user
     *
     * @return user value
     */
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getUID() {
        return uid;
    }

    public void setUid(String uid) {
        if (uid != null) {
            this.uid = Long.valueOf(uid);
        }
    }

    /**
     * @return The way name (extracted from tag)
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Way name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return true if visible
     */
    public boolean getVisible() {
        return visible;
    }

    public void setVisible(String visible) {
        if(visible!=null){
            this.visible = Boolean.valueOf(visible);
        }
    }

    /**
     *
     * @return GPX version
     */
    public int getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version != null ? Integer.valueOf(version) : 0;
    }

    /**
     *
     * @return change set value
     */
    public int getChangeSet() {
        return changeset;
    }

    public void setChangeset(String changeset) {
        if(changeset!=null){
            this.changeset = Integer.valueOf(changeset);
        }
    }

    /**
     *
     * @return time stamp
     */
    public Timestamp getTimeStamp() {
        return timestamp;

    }

    public void setTimestamp(String OSMtime) throws SAXException {
        if(OSMtime!=null){
        try {
            timestamp = new Timestamp(dataFormat1.parse(OSMtime).getTime());
        } catch (ParseException ex) {
            try {
                timestamp = new Timestamp(dataFormat2.parse(OSMtime).getTime());
            } catch (ParseException ex1) {
                throw new SAXException("Cannot parse the timestamp for the node  :  " + getID(), ex);
            }
        }}
    }

    /**
     *
     * @param key key value
     * @param value value
     * @return True if the tag should be inserted in the tag table.
     */
    public boolean addTag(String key, String value) {
        if(key.equalsIgnoreCase("name")) {
            name = value;
            return false;
        }
        tags.put(key, value);
        return true;
    }

    public HashMap<String, String> getTags() {
        return tags;
    }

}
