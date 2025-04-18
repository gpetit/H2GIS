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


package org.h2gis.functions.spatial.convert;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 * This class is used to generate an OSM map link URL from a geometry.
 * 
 * @author Erwan Bocher
 */
public class ST_OSMMapLink extends DeterministicScalarFunction {

    public ST_OSMMapLink() {
        addProperty(PROP_REMARKS, "Generate an OSM map link URL based on the bounding box of the input geometry.\n"
                + "An optional argument could be used to place a marker on the center of the bounding box.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "generateLink";
    }

    /**
     * Create the OSM map link based on the bounding box of the geometry.
     *
     * @param geom the input geometry.
     * @return OSM link
     */
    public static String generateLink(Geometry geom) {
        return generateLink(geom, false);
    }

    /**
     * Create the OSM map link based on the bounding box of the geometry.
     *
     * @param geom the input geometry.
     * @param withMarker true to place a marker on the center of the BBox.
     * @return osm link
     */
    public static String generateLink(Geometry geom, boolean withMarker) {
        if (geom == null) {
            return null;
        }
        Envelope env = geom.getEnvelopeInternal();
        StringBuilder sb = new StringBuilder("http://www.openstreetmap.org/?");
        sb.append("minlon=").append(env.getMinX());
        sb.append("&minlat=").append(env.getMinY());
        sb.append("&maxlon=").append(env.getMaxX());
        sb.append("&maxlat=").append(env.getMaxY());
        if (withMarker) {
            Coordinate centre = env.centre();
            sb.append("&mlat=").append(centre.y);
            sb.append("&mlon=").append(centre.x);
        }
        return sb.toString();
    }
}
