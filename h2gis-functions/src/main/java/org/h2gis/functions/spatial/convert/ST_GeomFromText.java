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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.sql.SQLException;

/**
 * Convert a Well Known Text String into a Geometry instance.
 * TODO Read SRID in table constraint
 * @author Nicolas Fortin
 */
public class ST_GeomFromText extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_GeomFromText() {
        addProperty(PROP_REMARKS, "Convert a Well Known Text geometry string into a geometry instance.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "toGeometry";
    }

    /**
     * Convert well known text parameter into a Geometry
     * @param wkt Well known text
     * @return Geometry instance or null if parameter is null
     */
    public static Geometry toGeometry(String wkt) throws SQLException {
        if(wkt == null) {
            return null;
        }
        WKTReader wktReader = new WKTReader();
        wktReader.setIsOldJtsCoordinateSyntaxAllowed(false);
        try {
            return wktReader.read(wkt);
        } catch (ParseException ex) {
            throw new SQLException("Cannot parse the WKT.",ex);
        }
    }

    /**
     * Convert well known text parameter into a Geometry
     * @param wkt Well known text
     * @param srid Geometry SRID
     * @return Geometry instance
     * @throws SQLException If wkt is invalid
     */
    public static Geometry toGeometry(String wkt, int srid) throws SQLException {
        if(wkt == null) {
            return null;
        }
        try {
            WKTReader wktReaderSRID = new WKTReader(new GeometryFactory(new PrecisionModel(),srid));
            wktReaderSRID.setIsOldJtsCoordinateSyntaxAllowed(false);
            return wktReaderSRID.read(wkt);
        } catch (ParseException ex) {
            throw new SQLException(ex);
        }
    }
}
