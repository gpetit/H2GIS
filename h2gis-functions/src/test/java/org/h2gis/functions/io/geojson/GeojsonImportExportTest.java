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

package org.h2gis.functions.io.geojson;

import org.h2.jdbc.JdbcSQLDataException;
import org.h2.jdbc.JdbcSQLException;
import org.h2.util.StringUtils;
import org.h2.util.geometry.JTSUtils;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.postgis_jts.PostGISDBFactory;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.sql.*;
import java.util.Properties;

import org.h2gis.unitTest.GeometryAsserts;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

import static org.h2gis.unitTest.GeometryAsserts.assertGeometryEquals;
/**
 *
 * @author Erwan Bocher
 * @author Hai Trung Pham
 */
public class GeojsonImportExportTest {

    private static Connection connection;
    private static final String DB_NAME = "GeojsonExportTest";
    private static final WKTReader WKTREADER = new WKTReader();
    private static final Logger log = LoggerFactory.getLogger(GeojsonImportExportTest.class);
    private static final PostGISDBFactory dataSourceFactory = new PostGISDBFactory();

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        H2GISFunctions.registerFunction(connection.createStatement(), new ST_AsGeoJSON(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new GeoJsonWrite(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new GeoJsonRead(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new ST_GeomFromGeoJSON(), "");

    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testGeojsonPoint() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
            stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINT))");
            stat.execute("insert into TABLE_POINT values(1, 'POINT(1 2)')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_POINT;");
            res.next();
            assertEquals("{\"type\":\"Point\",\"coordinates\":[1.0,2.0]}", res.getString(1));
            res.close();
        }
    }

    @Test
    public void testGeojsonPointRound() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
            stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINT))");
            stat.execute("insert into TABLE_POINT values(1, 'POINT(1.12345678911 2.12345678911 )')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_POINT;");
            res.next();
            assertEquals("{\"type\":\"Point\",\"coordinates\":[1.123456789,2.123456789]}", res.getString(1));
            res.close();
        }
    }

    @Test
    public void testGeojsonPointRound2() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
            stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINT))");
            stat.execute("insert into TABLE_POINT values(1, 'POINT(1.12345678911 2.12345678911 )')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom, 2) from TABLE_POINT;");
            res.next();
            assertEquals("{\"type\":\"Point\",\"coordinates\":[1.12,2.12]}", res.getString(1));
            res.close();
        }
    }

    @Test
    public void testGeojsonPointZ() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
            stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINTZ))");
            stat.execute("insert into TABLE_POINT values(1, 'POINTZ(1 2 3)')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_POINT;");
            res.next();
            assertEquals("{\"type\":\"Point\",\"coordinates\":[1.0,2.0,3.0]}", res.getString(1));
            res.close();
        }
    }

    @Test
    public void testGeojsonLineString() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_LINE");
            stat.execute("create table TABLE_LINE(idarea int primary key, the_geom GEOMETRY(LINESTRING))");
            stat.execute("insert into TABLE_LINE values(1, 'LINESTRING(1 2, 2 3)')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_LINE;");
            res.next();
            assertEquals("{\"type\":\"LineString\",\"coordinates\":[[1.0,2.0],[2.0,3.0]]}", res.getString(1));
            res.close();
        }
    }

    @Test
    public void testGeojsonPolygon() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON");
            stat.execute("create table TABLE_POLYGON(idarea int primary key, the_geom GEOMETRY(POLYGON))");
            stat.execute("insert into TABLE_POLYGON values(1, 'POLYGON((0 0, 2 0, 2 2, 0 2, 0 0))')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_POLYGON;");
            res.next();
            assertEquals(res.getString(1), "{\"type\":\"Polygon\",\"coordinates\":"
                    + "[[[0.0,0.0],[2.0,0.0],[2.0,2.0],[0.0,2.0],[0.0,0.0]]]}");
            res.close();
        }
    }

    @Test
    public void testGeojsonPolygonWithHole() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON");
            stat.execute("create table TABLE_POLYGON(idarea int primary key, the_geom GEOMETRY(POLYGON))");
            stat.execute("insert into TABLE_POLYGON values(1, 'POLYGON ((101 345, 300 345, 300 100, 101 100, 101 345), \n"
                    + "  (130 300, 190 300, 190 220, 130 220, 130 300), \n"
                    + "  (220 200, 255 200, 255 138, 220 138, 220 200))')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_POLYGON;");
            res.next();
            assertEquals(res.getString(1), "{\"type\":\"Polygon\",\"coordinates\":["
                    + "[[101.0,345.0],[300.0,345.0],[300.0,100.0],[101.0,100.0],[101.0,345.0]],"
                    + "[[130.0,300.0],[190.0,300.0],[190.0,220.0],[130.0,220.0],[130.0,300.0]],"
                    + "[[220.0,200.0],[255.0,200.0],[255.0,138.0],[220.0,138.0],[220.0,200.0]]]}");
            res.close();
        }
    }

    @Test
    public void testGeojsonMultiPoint() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINT");
            stat.execute("create table TABLE_MULTIPOINT(idarea int primary key, the_geom GEOMETRY(MULTIPOINT))");
            stat.execute("insert into TABLE_MULTIPOINT values(1, 'MULTIPOINT ((190 320), (180 160), (394 276))')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_MULTIPOINT;");
            res.next();
            assertEquals(res.getString(1), "{\"type\":\"MultiPoint\",\"coordinates\":["
                    + "[190.0,320.0],"
                    + "[180.0,160.0],"
                    + "[394.0,276.0]]}");
            res.close();
        }
    }

    @Test
    public void testGeojsonMultiLineString() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRING");
            stat.execute("create table TABLE_MULTILINESTRING(idarea int primary key, the_geom GEOMETRY(MULTILINESTRING))");
            stat.execute("insert into TABLE_MULTILINESTRING values(1, 'MULTILINESTRING ((80 240, 174 356, 280 250), \n"
                    + "  (110 140, 170 240, 280 360))')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_MULTILINESTRING;");
            res.next();
            assertEquals(res.getString(1), "{\"type\":\"MultiLineString\",\"coordinates\":["
                    + "[[80.0,240.0],[174.0,356.0],[280.0,250.0]],"
                    + "[[110.0,140.0],[170.0,240.0],[280.0,360.0]]]}");
            res.close();
        }
    }

    @Test
    public void testGeojsonMultiPolygon() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOLYGON");
            stat.execute("create table TABLE_MULTIPOLYGON(idarea int primary key, the_geom GEOMETRY(MULTIPOLYGON))");
            stat.execute("insert into TABLE_MULTIPOLYGON values(1, 'MULTIPOLYGON (((120 370, 180 370, 180 290, 120 290, 120 370)), \n"
                    + "  ((162 245, 234 245, 234 175, 162 175, 162 245)), \n"
                    + "  ((210 390, 235 390, 235 308, 210 308, 210 390)))')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_MULTIPOLYGON;");
            res.next();
            assertEquals(res.getString(1), "{\"type\":\"MultiPolygon\",\"coordinates\":["
                    + "[[[120.0,370.0],[180.0,370.0],[180.0,290.0],[120.0,290.0],[120.0,370.0]]],"
                    + "[[[162.0,245.0],[234.0,245.0],[234.0,175.0],[162.0,175.0],[162.0,245.0]]],"
                    + "[[[210.0,390.0],[235.0,390.0],[235.0,308.0],[210.0,308.0],[210.0,390.0]]]]}");
            res.close();
        }
    }

    @Test
    public void testGeojsonGeometryCollection() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_GEOMETRYCOLLECTION");
            stat.execute("create table TABLE_GEOMETRYCOLLECTION(idarea int primary key, the_geom GEOMETRY)");
            stat.execute("insert into TABLE_GEOMETRYCOLLECTION values(1, 'GEOMETRYCOLLECTION ("
                    + "POLYGON ((100 360, 140 360, 140 320, 100 320, 100 360)), \n"
                    + "  POINT (130 290), \n"
                    + "  LINESTRING (190 360, 190 280))')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_GEOMETRYCOLLECTION;");
            res.next();
            assertEquals(res.getString(1), "{\"type\":\"GeometryCollection\",\"geometries\":["
                    + "{\"type\":\"Polygon\",\"coordinates\":["
                    + "[[100.0,360.0],[140.0,360.0],[140.0,320.0],[100.0,320.0],[100.0,360.0]]"
                    + "]},"
                    + "{\"type\":\"Point\",\"coordinates\":[130.0,290.0]},"
                    + "{\"type\":\"LineString\",\"coordinates\":[[190.0,360.0],[190.0,280.0]]}]}");
            res.close();
        }
    }

    @Test
    public void testWriteReadGeojsonPoint() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
            stat.execute("create table TABLE_POINTS(the_geom GEOMETRY(POINT))");
            stat.execute("insert into TABLE_POINTS values( 'POINT(1 2)')");
            stat.execute("insert into TABLE_POINTS values( 'POINT(10 200)')");
            stat.execute("CALL GeoJsonWrite('target/points.geojson', 'TABLE_POINTS', true);");
            stat.execute("CALL GeoJsonRead('target/points.geojson', 'TABLE_POINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(1 2)")));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(10 200)")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonPointZ() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
            stat.execute("create table TABLE_POINTS(the_geom GEOMETRY(POINTZ))");
            stat.execute("insert into TABLE_POINTS values( 'POINTZ(1 2 3)')");
            stat.execute("insert into TABLE_POINTS values( 'POINTZ(10 200 2000)')");
            stat.execute("CALL GeoJsonWrite('target/points.geojson', 'TABLE_POINTS', true);");
            stat.execute("CALL GeoJsonRead('target/points.geojson', 'TABLE_POINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
            res.next();
            assertEquals(3,((Geometry) res.getObject(1)).getCoordinate().getZ());
            res.next();
            assertEquals(2000,((Geometry) res.getObject(1)).getCoordinate().getZ());
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonPointProperties() throws Exception {
        Statement stat = connection.createStatement();
        try {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(the_geom GEOMETRY(POINT), id INT, climat VARCHAR, tempo TIMESTAMP)");
            stat.execute("insert into TABLE_POINTS values( 'POINT(1 2)', 1, 'bad', NOW())");
            stat.execute("insert into TABLE_POINTS values( 'POINT(10 200)', 2, 'good', NOW())");
            stat.execute("CALL GeoJsonWrite('target/points_properties.geojson', 'TABLE_POINTS', true);");
            stat.execute("CALL GeoJsonRead('target/points_properties.geojson', 'TABLE_POINTS_READ');");
            try (ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;")) {
                res.next();
                assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(1 2)")));
                assertTrue((res.getInt(2) == 1));
                assertEquals("bad", res.getString(3));
                res.next();
                assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(10 200)")));
                assertTrue((res.getInt(2) == 2));
                assertEquals("good", res.getString(3));
            }
        } finally {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
            stat.close();
        }
    }

    @Test
    public void testWriteReadGeojsonLinestring() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS");
            stat.execute("create table TABLE_LINESTRINGS(the_geom GEOMETRY(LINESTRING))");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 2, 5 3, 10 19)')");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 10, 20 15)')");
            stat.execute("CALL GeoJsonWrite('target/lines.geojson', 'TABLE_LINESTRINGS', true);");
            stat.execute("CALL GeoJsonRead('target/lines.geojson', 'TABLE_LINESTRINGS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING(1 2, 5 3, 10 19)")));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING(1 10, 20 15)")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonMultiLinestring() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRINGS");
            stat.execute("create table TABLE_MULTILINESTRINGS(the_geom GEOMETRY(MULTILINESTRING))");
            stat.execute("insert into TABLE_MULTILINESTRINGS values( 'MULTILINESTRING ((90 220, 260 320, 280 200), \n"
                    + "  (150 140, 210 190, 210 220))')");
            stat.execute("insert into TABLE_MULTILINESTRINGS values( 'MULTILINESTRING ((126 324, 280 300), \n"
                    + "  (140 190, 320 220))')");
            stat.execute("CALL GeoJsonWrite('target/mutilines.geojson', 'TABLE_MULTILINESTRINGS', true);");
            stat.execute("CALL GeoJsonRead('target/mutilines.geojson', 'TABLE_MULTILINESTRINGS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTILINESTRINGS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTILINESTRING ((90 220, 260 320, 280 200), \n"
                    + "  (150 140, 210 190, 210 220))")));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTILINESTRING ((126 324, 280 300), \n"
                    + "  (140 190, 320 220))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRINGS_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonMultiPoint() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINTS");
            stat.execute("create table TABLE_MULTIPOINTS(the_geom GEOMETRY(MULTIPOINT))");
            stat.execute("insert into TABLE_MULTIPOINTS values( 'MULTIPOINT ((140 260), (246 284))')");
            stat.execute("insert into TABLE_MULTIPOINTS values( 'MULTIPOINT ((150 290), (180 170), (266 275))')");
            stat.execute("CALL GeoJsonWrite('target/multipoints.geojson', 'TABLE_MULTIPOINTS', true);");
            stat.execute("CALL GeoJsonRead('target/multipoints.geojson', 'TABLE_MULTIPOINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTIPOINTS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((140 260), (246 284))")));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((150 290), (180 170), (266 275))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINTS_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonPolygon() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON");
            stat.execute("create table TABLE_POLYGON(the_geom GEOMETRY(POLYGON))");
            stat.execute("insert into TABLE_POLYGON values( 'POLYGON ((110 320, 220 320, 220 200, 110 200, 110 320))')");
            stat.execute("CALL GeoJsonWrite('target/polygon.geojson', 'TABLE_POLYGON', true);");
            stat.execute("CALL GeoJsonRead('target/polygon.geojson', 'TABLE_POLYGON_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POLYGON_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POLYGON ((110 320, 220 320, 220 200, 110 200, 110 320))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonPolygonWithHoles() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON");
            stat.execute("create table TABLE_POLYGON(the_geom GEOMETRY(POLYGON))");
            stat.execute("insert into TABLE_POLYGON values( 'POLYGON ((100 300, 300 300, 300 100, 100 100, 100 300), \n"
                    + "  (120 280, 170 280, 170 220, 120 220, 120 280), \n"
                    + "  (191 195, 250 195, 250 140, 191 140, 191 195))')");
            stat.execute("CALL GeoJsonWrite('target/polygonholes.geojson', 'TABLE_POLYGON', true);");
            stat.execute("CALL GeoJsonRead('target/polygonholes.geojson', 'TABLE_POLYGON_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POLYGON_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POLYGON ((100 300, 300 300, 300 100, 100 100, 100 300), \n"
                    + "  (120 280, 170 280, 170 220, 120 220, 120 280), \n"
                    + "  (191 195, 250 195, 250 140, 191 140, 191 195))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POLYGON_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonMultiPolygon() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOLYGON");
            stat.execute("create table TABLE_MULTIPOLYGON(the_geom GEOMETRY(MULTIPOLYGON))");
            stat.execute("insert into TABLE_MULTIPOLYGON values( 'MULTIPOLYGON (((95 352, 160 352, 160 290, 95 290, 95 352)), \n"
                    + "  ((151 235, 236 235, 236 176, 151 176, 151 235)), \n"
                    + "  ((200 350, 245 350, 245 278, 200 278, 200 350)))')");
            stat.execute("CALL GeoJsonWrite('target/mutilipolygon.geojson', 'TABLE_MULTIPOLYGON', true);");
            stat.execute("CALL GeoJsonRead('target/mutilipolygon.geojson', 'TABLE_MULTIPOLYGON_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTIPOLYGON_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOLYGON (((95 352, 160 352, 160 290, 95 290, 95 352)), \n"
                    + "  ((151 235, 236 235, 236 176, 151 176, 151 235)), \n"
                    + "  ((200 350, 245 350, 245 278, 200 278, 200 350)))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOLYGON_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonGeometryCollection() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_GEOMETRYCOLLECTION");
            stat.execute("create table TABLE_GEOMETRYCOLLECTION(the_geom GEOMETRY)");
            stat.execute("insert into TABLE_GEOMETRYCOLLECTION values( 'GEOMETRYCOLLECTION (POLYGON ((80 320, 110 320, 110 280, 80 280, 80 320)), \n"
                    + "  LINESTRING (70 190, 77 200, 150 240), \n"
                    + "  POINT (160 300))')");
            stat.execute("CALL GeoJsonWrite('target/geometrycollection.geojson', 'TABLE_GEOMETRYCOLLECTION', true);");
            stat.execute("CALL GeoJsonRead('target/geometrycollection.geojson', 'TABLE_GEOMETRYCOLLECTION_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_GEOMETRYCOLLECTION_READ;");
            res.next();
            Geometry geom = (Geometry) res.getObject(1);
            assertEquals(3, geom.getNumGeometries());
            assertTrue(geom.getGeometryN(0).equals(WKTREADER.read("POLYGON ((80 320, 110 320, 110 280, 80 280, 80 320))")));
            assertTrue(geom.getGeometryN(1).equals(WKTREADER.read("LINESTRING (70 190, 77 200, 150 240)")));
            assertTrue(geom.getGeometryN(2).equals(WKTREADER.read("POINT (160 300)")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_GEOMETRYCOLLECTION_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonSingleMultiPolygon() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOLYGON");
            stat.execute("create table TABLE_MULTIPOLYGON(the_geom GEOMETRY(MULTIPOLYGON))");
            stat.execute("insert into TABLE_MULTIPOLYGON values( 'MULTIPOLYGON (((95 352, 160 352, 160 290, 95 290, 95 352)))')");
            stat.execute("CALL GeoJsonWrite('target/mutilipolygon.geojson', 'TABLE_MULTIPOLYGON', true);");
            stat.execute("CALL GeoJsonRead('target/mutilipolygon.geojson', 'TABLE_MULTIPOLYGON_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTIPOLYGON_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOLYGON (((95 352, 160 352, 160 290, 95 290, 95 352)))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOLYGON_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonSingleMultiPoint() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINTS");
            stat.execute("create table TABLE_MULTIPOINTS(the_geom GEOMETRY(MULTIPOINT))");
            stat.execute("insert into TABLE_MULTIPOINTS values( 'MULTIPOINT ((140 260))')");
            stat.execute("CALL GeoJsonWrite('target/multipoints.geojson', 'TABLE_MULTIPOINTS', true);");
            stat.execute("CALL GeoJsonRead('target/multipoints.geojson', 'TABLE_MULTIPOINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTIPOINTS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((140 260))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTIPOINTS_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonSingleMultiLinestring() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRINGS");
            stat.execute("create table TABLE_MULTILINESTRINGS(the_geom GEOMETRY(MULTILINESTRING))");
            stat.execute("insert into TABLE_MULTILINESTRINGS values( 'MULTILINESTRING ((90 220, 260 320, 280 200))')");
            stat.execute("CALL GeoJsonWrite('target/mutilines.geojson', 'TABLE_MULTILINESTRINGS', true);");
            stat.execute("CALL GeoJsonRead('target/mutilines.geojson', 'TABLE_MULTILINESTRINGS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTILINESTRINGS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTILINESTRING ((90 220, 260 320, 280 200))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRINGS_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonCRS() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_CRS");
            stat.execute("create table TABLE_POINTS_CRS(the_geom GEOMETRY(POINT,4326), id INT, climat VARCHAR)");
            stat.execute("insert into TABLE_POINTS_CRS values( ST_GEOMFROMTEXT('POINT(1 2)', 4326), 1, 'bad')");
            stat.execute("insert into TABLE_POINTS_CRS values( ST_GEOMFROMTEXT('POINT(10 200)',4326), 2, 'good')");
            stat.execute("CALL GeoJsonWrite('target/points_crs_properties.geojson', 'TABLE_POINTS_CRS', true);");
            stat.execute("CALL GeoJsonRead('target/points_crs_properties.geojson', 'TABLE_POINTS_CRS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_CRS_READ;");
            res.next();
            Geometry geom = (Geometry) res.getObject(1);
            assertTrue(geom.equals(WKTREADER.read("POINT(1 2)")));
            assertTrue((geom.getSRID() == 4326));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_CRS_READ");
        }
    }

    @Test
    public void testWriteReadBadSRID() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(the_geom GEOMETRY(POINT,9999), id INT, climat VARCHAR)");
            stat.execute("insert into TABLE_POINTS values( ST_GEOMFROMTEXT('POINT(1 2)', 9999), 1, 'bad')");
            stat.execute("insert into TABLE_POINTS values( ST_GEOMFROMTEXT('POINT(10 200)',9999), 2, 'good')");
            stat.execute("CALL GeoJsonWrite('target/points_properties.geojson', 'TABLE_POINTS', true);");
            stat.execute("CALL GeoJsonRead('target/points_properties.geojson', 'TABLE_POINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
            res.next();
            Geometry geom = (Geometry) res.getObject(1);
            assertTrue(geom.equals(WKTREADER.read("POINT(1 2)")));
            assertTrue((geom.getSRID() == 0));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        }
    }

    @Test
    public void testWriteReadGeojsonMixedGeometries() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MIXED");
            stat.execute("create table TABLE_MIXED(the_geom GEOMETRY)");
            stat.execute("insert into TABLE_MIXED values( 'MULTIPOINT ((140 260), (246 284))')");
            stat.execute("insert into TABLE_MIXED values( 'LINESTRING (150 290, 180 170, 266 275)')");
            stat.execute("CALL GeoJsonWrite('target/mixedgeom.geojson', 'TABLE_MIXED', true);");
            stat.execute("CALL GeoJsonRead('target/mixedgeom.geojson', 'TABLE_MIXED_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MIXED_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((140 260), (246 284))")));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING (150 290, 180 170, 266 275)")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MIXED_READ");
        }
    }

    @Test
    public void testReadGeoJSON1() throws Exception {
        try (Statement stat = connection.createStatement()) {
            ResultSet res = stat.executeQuery("SELECT ST_GeomFromGeoJSON('{\"type\":\"Point\",\"coordinates\":[10,1]}')");
            res.next();
            assertEquals("POINT (10 1)", res.getString(1));
        }
    }

    @Test
    public void testReadGeoJSON2() throws Exception {
        try (Statement stat = connection.createStatement()) {
            ResultSet res = stat.executeQuery("SELECT ST_GeomFromGeoJSON('{\"type\":\"LineString\",\"coordinates\":[[1,1],[10,10]]}')");
            res.next();
            assertEquals("LINESTRING (1 1, 10 10)", res.getString(1));
        }
    }

    @Test
    public void testReadGeoJSON3() throws Exception {
        try (Statement stat = connection.createStatement()) {
            ResultSet res = stat.executeQuery("SELECT ST_GeomFromGeoJSON('{ \"type\": \"MultiPoint\", \"coordinates\": [ [100, 0], [101, 1] ]}')");
            res.next();
            assertEquals("MULTIPOINT ((100 0), (101 1))", res.getString(1));
        }
    }

    @Test
    public void testReadGeoJSON4() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS geojson_data; CREATE TABLE geojson_data " +
                    "as SELECT CONCAT('{ \"type\": \"Point\", \"coordinates\": [', X, ',0] }') as json FROM GENERATE_SERIES(0,3) ORDER BY X");
            ResultSet res = stat.executeQuery("SELECT ST_GeomFromGeoJSON(json) from geojson_data");
            res.next();
            assertEquals("POINT (0 0)", res.getString(1));
            res.next();
            assertEquals("POINT (1 0)", res.getString(1));
            res.next();
            assertEquals("POINT (2 0)", res.getString(1));
        }
    }

    @Test
    public void testReadGeoJSON5() throws Exception {
        try (Statement stat = connection.createStatement()) {
            String collection="{\n" +
                    "  \"type\": \"GeometryCollection\",\n" +
                    "  \"geometries\": [\n" +
                    "    {\n" +
                    "      \"type\": \"Point\",\n" +
                    "      \"coordinates\": [4.404732087, 51.22893535]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"type\": \"LineString\",\n" +
                    "      \"coordinates\": [\n" +
                    "        [4.404732087, 51.22893535],\n" +
                    "        [4.403982789, 51.229262879],\n" +
                    "        [4.403812348, 51.229155607]\n" +
                    "      ]\n" +
                    "    }\n"+
                    "  ]\n" +
                    "}";
            ResultSet res = stat.executeQuery("SELECT ST_GeomFromGeoJSON('"+collection+"')");
            res.next();
            assertEquals("GEOMETRYCOLLECTION (POINT (4.404732087 51.22893535), LINESTRING (4.404732087 51.22893535, 4.403982789 51.229262879, 4.403812348 51.229155607))", res.getString(1));
        }
    }

    @Test
    public void testReadGeoJSON6() throws Exception {
        try (Statement stat = connection.createStatement()) {
            String collection="{\n" +
                    "  \"type\": \"GeometryCollection\",\n" +
                    "  \"geometries\": [\n" +
                    "   {\n" +
                    "      \"type\": \"LineString\",\n" +
                    "      \"coordinates\": [\n" +
                    "        [4.404732087, 51.22893535],\n" +
                    "        [4.403982789, 51.229262879],\n" +
                    "        [4.403812348, 51.229155607]\n" +
                    "      ]\n" +
                    "    },\n"+
                    "    {\n" +
                    "      \"type\": \"Point\",\n" +
                    "      \"coordinates\": [4.404732087, 51.22893535]\n" +
                    "    }"+
                    "  ]\n" +
                    "}";
            ResultSet res = stat.executeQuery("SELECT ST_GeomFromGeoJSON('"+collection+"')");
            res.next();
            assertEquals("GEOMETRYCOLLECTION (LINESTRING (4.404732087 51.22893535, 4.403982789 51.229262879, 4.403812348 51.229155607), POINT (4.404732087 51.22893535))", res.getString(1));
        }
    }

    @Test
    public void testReadGeoJSON7() throws Exception {
        try (Statement stat = connection.createStatement()) {
            String collection="{\n" +
                    "  \"type\": \"GeometryCollection\",\n" +
                    "  \"geometries\": [\n" +
                    "    {\n" +
                    "        \"type\": \"LineString\",\n" +
                    "        \"coordinates\": [\n" +
                    "          [-0.0147294, 53.7405502],\n" +
                    "          [-0.0146484, 53.7406478],\n" +
                    "          [-0.0141911, 53.7411993]\n"+
                    "        ]\n" +
                    "      },"+
                    "    {\n" +
                    "      \"type\": \"LineString\",\n" +
                    "      \"coordinates\": [\n" +
                    "        [4.404732087, 51.22893535],\n" +
                    "        [4.403982789, 51.229262879],\n" +
                    "        [4.403812348, 51.229155607]\n" +
                    "      ]\n" +
                    "    }\n"+
                    "  ]\n" +
                    "}";
            ResultSet res = stat.executeQuery("SELECT ST_GeomFromGeoJSON('"+collection+"')");
            res.next();
            assertEquals("GEOMETRYCOLLECTION (LINESTRING (-0.0147294 53.7405502, -0.0146484 53.7406478, -0.0141911 53.7411993), LINESTRING (4.404732087 51.22893535, 4.403982789 51.229262879, 4.403812348 51.229155607))", res.getString(1));
        }
    }


    @Test
    public void testWriteReadNullGeojsonPoint() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(the_geom GEOMETRY(POINT), id int)");
            stat.execute("insert into TABLE_POINTS values( null, 1)");
            stat.execute("insert into TABLE_POINTS values( 'POINT(10 200)', 2)");
            stat.execute("CALL GeoJsonWrite('target/null_point.geojson', 'TABLE_POINTS', true);");
            stat.execute("CALL GeoJsonRead('target/null_point.geojson', 'TABLE_POINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
            res.next();
            assertNull(res.getObject(1));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(10 200)")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        }
    }


    @Test
    public void testWriteReadlGeojsonComplex() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_COMPLEX, TABLE_COMPLEX_READ");
            stat.execute("create table TABLE_COMPLEX(the_geom geometry, gid long)");
            stat.execute("insert into TABLE_COMPLEX values( null, 1463655908000)");
            stat.execute("insert into TABLE_COMPLEX values( 'POINT(10 200)', 1)");
            stat.execute("insert into TABLE_COMPLEX values( 'LINESTRING(15 20, 0 0)',  NULL)");
            stat.execute("CALL GeoJsonWrite('target/complex.geojson', 'TABLE_COMPLEX', true);");
            stat.execute("CALL GeoJsonRead('target/complex.geojson', 'TABLE_COMPLEX_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_COMPLEX_READ;");
            res.next();
            assertNull(res.getObject(1));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(10 200)")));
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING(15 20, 0 0)")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        }
    }

    @Test
    public void testReadEmptyLineString() throws Exception {
        JTSUtils.ewkb2geometry(JTSUtils.geometry2ewkb(new GeometryFactory().createLineString()));
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS ROAD_EMPTYLINESTRING");
            stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("road_emptylinestring.geojson").getPath()) + ", 'ROAD_EMPTYLINESTRING');");
        }
    }

    @Test
    public void testReadComplexFile() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_COMPLEX_READ");
            stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("complex.geojson").getPath()) + ", 'TABLE_COMPLEX_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_COMPLEX_READ;");
            ResultSetMetaData metadata = res.getMetaData();
            assertEquals(16, metadata.getColumnCount());
            assertEquals("GEOMETRY(POINT Z, 0)", metadata.getColumnTypeName(1));
            assertEquals("DOUBLE PRECISION", metadata.getColumnTypeName(2));
            assertEquals("DOUBLE PRECISION", metadata.getColumnTypeName(3));
            assertEquals("BIGINT", metadata.getColumnTypeName(4));
            assertEquals("BIGINT", metadata.getColumnTypeName(5));
            assertEquals("BIGINT", metadata.getColumnTypeName(6));
            assertEquals("CHARACTER VARYING", metadata.getColumnTypeName(7));
            assertEquals("DOUBLE PRECISION", metadata.getColumnTypeName(8));
            assertEquals("DOUBLE PRECISION", metadata.getColumnTypeName(9));
            assertEquals("CHARACTER VARYING", metadata.getColumnTypeName(10));
            assertEquals("BOOLEAN", metadata.getColumnTypeName(11));
            assertEquals("CHARACTER VARYING", metadata.getColumnTypeName(12));
            assertEquals("DOUBLE PRECISION", metadata.getColumnTypeName(13));
            assertEquals("DOUBLE PRECISION", metadata.getColumnTypeName(14));
            assertEquals("DOUBLE PRECISION", metadata.getColumnTypeName(15));
            assertEquals("DOUBLE PRECISION", metadata.getColumnTypeName(16));
            res.next();
            assertNull(res.getObject(1));
            assertEquals(79.04200463708992, res.getDouble(2));
            assertEquals("#C5E805", res.getString(7));
            assertEquals(0, res.getDouble(13));
            assertNull(res.getObject(14));
            assertNull(res.getObject(15));
            res.next();
            assertNull(res.getObject(1));
            assertNull(res.getObject(2));
            assertEquals("#C5E805", res.getString(7));
            assertEquals("12.0", res.getString(10));
            assertNull(res.getObject(11));
            assertTrue(res.getBoolean(12));
            assertEquals(0.5, res.getDouble(13));
            res.next();
            assertEquals(10.2d, ((Geometry) res.getObject(1)).getCoordinate().getZ(), 0);
            assertNull(res.getObject(14));
            assertEquals(0.87657195d, res.getDouble(15), 0);
            assertEquals(234.16d, res.getDouble(16), 0);
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        }
    }

    @Test
    public void testReadProperties() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_PROPERTIES_READ;");
            stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("data.geojson").getPath()) + ", 'TABLE_PROPERTIES_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_PROPERTIES_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POLYGON ((7.49587624983838 48.5342070572556, 7.49575955525988 48.5342516702309, 7.49564286068138 48.5342070572556, 7.49564286068138 48.534117831187, 7.49575955525988 48.5340732180938, 7.49587624983838 48.534117831187, 7.49587624983838 48.5342070572556))")));
            assertEquals(-105576, res.getDouble(2), 0);
            assertEquals(275386, res.getDouble(3), 0);
            assertEquals(56.848998452816424, res.getDouble(4), 0);
            assertEquals(55.87291487481895, res.getDouble(5), 0);
            assertEquals(0.0, res.getDouble(6), 0);
            assertNull(res.getString(7));
            assertEquals(2, res.getDouble(8), 0);
            assertEquals("2017-01-19T18:29:26+01:00", res.getString(9));
            assertEquals("1484846966000", res.getBigDecimal(10).toString());
            assertEquals("2017-01-19T18:29:27+01:00", res.getString(11));
            assertEquals("1484846967000", res.getBigDecimal(12).toString());
            assertEquals("{\"member1\":1,\"member2\":{\"member21\":21,\"member22\":22}}", res.getString(13));
            assertEquals("[49,40.0,{\"member1\":1,\"member2\":{\"member21\":21,\"member22\":22}},\"string\",[13,\"string\",{\"member3\":3,\"member4\":4}]]", res.getString(14));
            assertEquals("[58,47,58,57,58,49,58,51,58,58,49,57,58,58,49,58,57,56,57,58,59,58,57,58,49,47,48,57,48,58,57,57,51,56,52,57,51,57,49,58,55,58,50,48,48,52,56,57,48,58,52,48,53,50,57,54,57,47,58,57,54,54,53,56,57,55,58,58,57,58,57,57]", res.getString(15));
            res.next();
            res.close();
        }
    }

    @Test
    public void testWriteReadProperties() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_PROPERTIES;");
            stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("data.geojson").getPath()) + ", 'TABLE_PROPERTIES');");
            stat.execute("CALL GeoJsonWrite('target/properties_read.geojson','TABLE_PROPERTIES', true)");
            stat.execute("DROP TABLE IF EXISTS TABLE_PROPERTIES_READ;");
            stat.execute("CALL GeoJsonRead('target/properties_read.geojson', 'TABLE_PROPERTIES_READ')");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_PROPERTIES_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POLYGON ((7.49587624983838 48.5342070572556, 7.49575955525988 48.5342516702309, 7.49564286068138 48.5342070572556, 7.49564286068138 48.534117831187, 7.49575955525988 48.5340732180938, 7.49587624983838 48.534117831187, 7.49587624983838 48.5342070572556))")));
            assertEquals(-105576, res.getDouble(2), 0);
            assertEquals(275386, res.getDouble(3), 0);
            assertEquals(56.848998452816424, res.getDouble(4), 0);
            assertEquals(55.87291487481895, res.getDouble(5), 0);
            assertEquals(0.0, res.getDouble(6), 0);
            assertNull(res.getString(7));
            assertEquals(2, res.getDouble(8), 0);
            assertEquals("2017-01-19T18:29:26+01:00", res.getString(9));
            assertEquals("1484846966000", res.getBigDecimal(10).toString());
            assertEquals("2017-01-19T18:29:27+01:00", res.getString(11));
            assertEquals("1484846967000", res.getBigDecimal(12).toString());
            assertEquals("{\"member1\":1,\"member2\":{\"member21\":21,\"member22\":22}}", res.getString(13));
            assertEquals("[49,40.0,{\"member1\":1,\"member2\":{\"member21\":21,\"member22\":22}},\"string\",[13,\"string\",{\"member3\":3,\"member4\":4}]]", res.getString(14));
            assertEquals("[58,47,58,57,58,49,58,51,58,58,49,57,58,58,49,58,57,56,57,58,59,58,57,58,49,47,48,57,48,58,57,57,51,56,52,57,51,57,49,58,55,58,50,48,48,52,56,57,48,58,52,48,53,50,57,54,57,47,58,57,54,54,53,56,57,55,58,58,57,58,57,57]", res.getString(15));
            res.next();
            res.close();
        }
    }

    @Test
    public void testWriteReadNullField() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_NULL;");
            stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("null.geojson").getPath()) + ", 'TABLE_NULL');");
            stat.execute("CALL GeoJsonWrite('target/null_read.geojson','TABLE_NULL', true)");
            stat.execute("DROP TABLE IF EXISTS TABLE_NULL_READ");
            stat.execute("CALL GeoJsonRead('target/null_read.geojson', 'TABLE_NULL_READ')");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_NULL_READ;");
            res.next();
            assertNull((res.getObject(1)));
            assertEquals("string", res.getString(2));
            assertEquals("{\"member1\":1,\"member2\":{\"member21\":null,\"member22\":22},\"member3\":null}", res.getString(3));
            assertEquals("[49,40.0,{\"member1\":null,\"member2\":{\"member21\":null,\"member22\":22}},\"string\",[13,\"string\",{\"member3\":null,\"member4\":4},null]]", res.getString(4));
            res.next();
            assertNull((res.getObject(1)));
            assertTrue(res.getBoolean(2));
            assertEquals("{\"member\":1}", res.getString(3));
            assertEquals("[1,2]", res.getString(4));
            res.next();
            assertNull((res.getObject(1)));
            assertEquals(2, res.getInt(2), 0);
            assertEquals("{\"member\":1}", res.getString(3));
            assertEquals("[1,2]", res.getString(4));
            res.next();
            assertNull((res.getObject(1)));
            assertEquals("{\"member3\":4}", res.getString(2));
            assertEquals("{\"member\":1}", res.getString(3));
            assertEquals("[1,2]", res.getString(4));
            res.next();
            assertNull((res.getObject(1)));
            assertEquals("[5,6,6]", res.getString(2));
            assertEquals("{\"member\":1}", res.getString(3));
            assertEquals("[1,2]", res.getString(4));
            res.next();
            assertNull((res.getObject(1)));
            assertNull(res.getString(2));
            assertEquals("{\"member\":1}", res.getString(3));
            assertEquals("[1,2]", res.getString(4));
            res.close();
        }
    }

    @Test
    public void testReadAdditionalProps() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_ADDITIONALPROPS_READ;");
            stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("additionalProps.geojson").getPath()) + ", 'TABLE_ADDITIONALPROPS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_ADDITIONALPROPS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT (100.0 0.0)")));
            assertEquals(105576, res.getDouble(2), 0);
            res.next();
            res.close();
        }
    }

    @Test
    public void testWriteReadEmptyTable() throws SQLException {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
            stat.execute("create table TABLE_POINTS(the_geom GEOMETRY(POINT))");
            stat.execute("CALL GeoJsonWrite('target/points.geojson', 'TABLE_POINTS', true);");
            stat.execute("CALL GeoJsonRead('target/points.geojson', 'TABLE_POINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
            ResultSetMetaData rsmd = res.getMetaData();
            assertEquals(0, rsmd.getColumnCount());
            assertFalse(res.next());
        }
    }

    @Test
    public void testWriteReadGeojsonProperties() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_MIXED_PROPS");
            stat.execute("create table TABLE_MIXED_PROPS(the_geom GEOMETRY, decimal_field DECIMAL(4, 2), numeric_field NUMERIC(4,2), real_field REAL)");
            stat.execute("insert into TABLE_MIXED_PROPS values( 'MULTIPOINT ((140 260), (246 284))', 12.12, 14.23, 23)");
            stat.execute("CALL GeoJsonWrite('target/mixedgeomprops.geojson', 'TABLE_MIXED_PROPS', true);");
            stat.execute("CALL GeoJsonRead('target/mixedgeomprops.geojson', 'TABLE_MIXED_PROPS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MIXED_PROPS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTIPOINT ((140 260), (246 284))")));
            assertEquals(12.12, res.getDouble(2), 0);
            assertEquals(14.23, res.getDouble(3), 0);
            assertEquals(23, res.getDouble(4), 0);
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MIXED_PROPS_READ");
        }
    }

    @Test
    public void exportImportFile() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File fileOut = new File("target/lineal_export.geojson");
        stat.execute("DROP TABLE IF EXISTS LINEAL");
        stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING Z))");
        stat.execute("insert into lineal values(1, 'LINESTRINGZ(-10 109 5, 12 2 6)')");
        // Create a geojson file using table area
        stat.execute("CALL GeoJSONWrite('target/lineal_export.geojson', 'LINEAL', true)");
        // Read this geojson file to check values
        assertTrue(fileOut.exists());
        stat.execute("DROP TABLE IF EXISTS IMPORT_LINEAL;");
        stat.execute("CALL GeoJSONRead('target/lineal_export.geojson', 'IMPORT_LINEAL', true)");
        try (ResultSet res = stat.executeQuery("SELECT IDAREA FROM IMPORT_LINEAL;")) {
            res.next();
            assertEquals(1, res.getInt(1));
        }
    }


    @Test
    public void exportImportFileWithSpace() throws SQLException, IOException {
        assertThrows(JdbcSQLDataException.class, () -> {
            Statement stat = connection.createStatement();
            File fileOut = new File("target/lineal export.geojson");
            stat.execute("DROP TABLE IF EXISTS LINEAL");
            stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING))");
            stat.execute("insert into lineal values(1, 'LINESTRING(-10 109 5, 12  6)')");
            // Create a shape file using table area
            stat.execute("CALL GeoJSONWrite('target/lineal export.geojson', 'LINEAL')");
            // Read this shape file to check values
            assertTrue(fileOut.exists());
            stat.execute("DROP TABLE IF EXISTS IMPORT_LINEAL;");
            stat.execute("CALL GeoJSONRead('target/lineal export.geojson')");
        });
    }

    @Test
    public void exportImportFileWithDot() throws SQLException, IOException {
        assertThrows(JdbcSQLDataException.class, () -> {
            Statement stat = connection.createStatement();
            File fileOut = new File("target/lineal.export.geojson");
            stat.execute("DROP TABLE IF EXISTS LINEAL");
            stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING))");
            stat.execute("insert into lineal values(1, 'LINESTRING(-10 109 5, 12  6)')");
            // Create a shape file using table area
            stat.execute("CALL GeoJSONWrite('target/lineal.export.geojson', 'LINEAL')");
            // Read this shape file to check values
            assertTrue(fileOut.exists());
            stat.execute("DROP TABLE IF EXISTS IMPORT_LINEAL;");
            stat.execute("CALL GeoJSONRead('target/lineal.export.geojson')");
        });
    }

    @Test
    public void exportImportResultSet() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File fileOut = new File("target/lineal_export.geojson");
        stat.execute("DROP TABLE IF EXISTS LINEAL");
        stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING Z))");
        stat.execute("insert into lineal values(1, 'LINESTRINGZ(-10 109 5, 12 2 6)')");
        ResultSet resultSet = stat.executeQuery("SELECT * FROM lineal");
        GeoJsonWriteDriver gjw = new GeoJsonWriteDriver(connection);
        gjw.write(new EmptyProgressVisitor(), resultSet, new File("target/lineal_export.geojson"), null, true);
        // Read this geojson file to check values
        assertTrue(fileOut.exists());
        stat.execute("DROP TABLE IF EXISTS IMPORT_LINEAL,LINEAL_EXPORT;");
        stat.execute("CALL GeoJSONRead('target/lineal_export.geojson')");
        try (ResultSet res = stat.executeQuery("SELECT IDAREA FROM LINEAL_EXPORT;")) {
            res.next();
            assertEquals(1, res.getInt(1));
        }
    }

    @Test
    public void exportQueryImportFile() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File fileOut = new File("target/lineal_export.geojson");
        stat.execute("DROP TABLE IF EXISTS LINEAL");
        stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING Z))");
        stat.execute("insert into lineal values(1, 'LINESTRINGZ(-10 109 5, 12 2 6)'),(2, 'LINESTRINGZ(-15 109 5, 120 2 6)')");
        // Create a geojson file using a query
        stat.execute("CALL GeoJSONWrite('target/lineal_export.geojson', '(SELECT THE_GEOM FROM LINEAL LIMIT 1 )', true)");
        // Read this shape file to check values
        assertTrue(fileOut.exists());
        stat.execute("DROP TABLE IF EXISTS IMPORT_LINEAL,LINEAL_EXPORT;");
        stat.execute("CALL GeoJSONRead('target/lineal_export.geojson')");

        try (ResultSet res = stat.executeQuery("SELECT COUNT(*) FROM LINEAL_EXPORT;")) {
            res.next();
            assertEquals(1, res.getInt(1));
        }
    }

    @Test
    public void exportQueryImportFileOption() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File fileOut = new File("target/lineal_export.geojson");
        stat.execute("DROP TABLE IF EXISTS LINEAL");
        stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING Z))");
        stat.execute("insert into lineal values(1, 'LINESTRINGZ(-10 109 5, 12 2 6)'),(2, 'LINESTRINGZ(-15 109 5, 120 2 6)')");
        // Create a geojson file using a query
        stat.execute("CALL GeoJSONWrite('target/lineal_export.geojson', '(SELECT THE_GEOM FROM LINEAL LIMIT 1 )', true)");
        // Read this shape file to check values
        assertTrue(fileOut.exists());
        stat.execute("DROP TABLE IF EXISTS IMPORT_LINEAL;");
        stat.execute("CALL GeoJSONRead('target/lineal_export.geojson', 'IMPORT_LINEAL', true)");

        try (ResultSet res = stat.executeQuery("SELECT COUNT(*) FROM IMPORT_LINEAL;")) {
            res.next();
            assertEquals(1, res.getInt(1));
        }
        stat.execute("CALL GeoJSONRead('target/lineal_export.geojson', true)");

        try (ResultSet res = stat.executeQuery("SELECT COUNT(*) FROM IMPORT_LINEAL;")) {
            res.next();
            assertEquals(1, res.getInt(1));
        }
    }

    @Test
    public void exportQueryImportFileGZOption() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File fileOut = new File("target/lineal_export.gz");
        stat.execute("DROP TABLE IF EXISTS LINEAL");
        stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING Z))");
        stat.execute("insert into lineal values(1, 'LINESTRINGZ(-10 109 5, 12 2 6)'),(2, 'LINESTRINGZ(-15 109 5, 120 2 6)')");
        // Create a geojson file using a query
        stat.execute("CALL GeoJSONWrite('target/lineal_export.gz', '(SELECT THE_GEOM FROM LINEAL LIMIT 1 )', true)");
        // Read this shape file to check values
        assertTrue(fileOut.exists());
        stat.execute("DROP TABLE IF EXISTS IMPORT_LINEAL;");
        stat.execute("CALL GeoJSONRead('target/lineal_export.gz')");

        try (ResultSet res = stat.executeQuery("SELECT COUNT(*) FROM LINEAL_EXPORT;")) {
            res.next();
            assertEquals(1, res.getInt(1));
        }
    }

    @Test
    public void exportQueryImportFileGZOption2() throws SQLException, IOException {
        Statement stat = connection.createStatement();
        File fileOut = new File("target/lineal_export.gz");
        stat.execute("DROP TABLE IF EXISTS LINEAL");
        stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING Z))");
        stat.execute("insert into lineal values(1, 'LINESTRINGZ(-10 109 5, 12 2 6)'),(2, 'LINESTRINGZ(-15 109 5, 120 2 6)')");
        // Create a geojson file using a query
        stat.execute("CALL GeoJSONWrite('target/lineal_export.geojson.gz', '(SELECT THE_GEOM FROM LINEAL LIMIT 1 )', true)");
        // Read this shape file to check values
        assertTrue(fileOut.exists());
        stat.execute("DROP TABLE IF EXISTS LINEAL_EXPORT_GEOJSON;");
        stat.execute("CALL GeoJSONRead('target/lineal_export.geojson.gz')");

        try (ResultSet res = stat.executeQuery("SELECT COUNT(*) FROM LINEAL_EXPORT_GEOJSON;")) {
            res.next();
            assertEquals(1, res.getInt(1));
        }
        stat.execute("CALL GeoJSONRead('target/lineal_export.geojson.gz', true)");

        try (ResultSet res = stat.executeQuery("SELECT COUNT(*) FROM LINEAL_EXPORT_GEOJSON;")) {
            res.next();
            assertEquals(1, res.getInt(1));
        }
    }

    @Test
    public void exportResultSetBadEncoding() throws SQLException, IOException {
        assertThrows(JdbcSQLDataException.class, () -> {
            Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS LINEAL");
            stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING))");
            stat.execute("insert into lineal values(1, 'LINESTRING(-10 109 5, 12  6)')");
            ResultSet resultSet = stat.executeQuery("SELECT * FROM lineal");
            GeoJsonDriverFunction geoJsonDriver = new GeoJsonDriverFunction();
            geoJsonDriver.exportTable(connection, "(SELECT * FROM lineal)", new File("target/lineal_export.geojson"), "CP52", true, new EmptyProgressVisitor());
        });
    }

    @Test
    public void exportBadEncoding() throws SQLException, IOException {
        assertThrows(JdbcSQLDataException.class, () -> {
            Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS LINEAL");
            stat.execute("create table lineal(idarea int primary key, the_geom GEOMETRY(LINESTRING))");
            stat.execute("insert into lineal values(1, 'LINESTRING(-10 109 5, 12  6)')");
            GeoJsonDriverFunction geoJsonDriver = new GeoJsonDriverFunction();
            geoJsonDriver.exportTable(connection, "lineal", new File("target/lineal_export.geojson"), "CP52", true, new EmptyProgressVisitor());
        });
    }


    @Test
    public void testSelectWriteReadGeojsonLinestring() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS, TABLE_LINESTRINGS_READ");
            stat.execute("create table TABLE_LINESTRINGS(the_geom GEOMETRY(LINESTRING), id int)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 2, 5 3, 10 19)', 1)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 10, 20 15)', 2)");
            stat.execute("CALL GeoJsonWrite('target/lines.geojson', '(SELECT * FROM TABLE_LINESTRINGS WHERE ID=2)', true);");
            stat.execute("CALL GeoJsonRead('target/lines.geojson', 'TABLE_LINESTRINGS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING(1 10, 20 15)")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
        }
    }

    @Test
    public void testSelectWriteRead() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL GeoJsonWrite('target/lines.geojson', '(SELECT ST_GEOMFROMTEXT(''LINESTRING(1 10, 20 15)'', 4326) as the_geom)', true);");
            stat.execute("CALL GeoJsonRead('target/lines.geojson', 'TABLE_LINESTRINGS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING(1 10, 20 15)")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
        }
    }

    @Test
    public void testReadCRS84() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("urn_crs84.geojson").getPath()) + ")");
        ResultSet res = stat.executeQuery("SELECT * FROM URN_CRS84;");
        res.next();
        assertGeometryEquals("SRID=4326;POINT(7.49587624983838 48.5342070572556)", (Geometry) res.getObject(1));
        res.close();
        stat.execute("DROP TABLE IF EXISTS URN_CRS84");
    }

    @Test
    public void testWriteReadGeojsonTableCase() throws Exception {
        Statement stat = connection.createStatement();
        try {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(the_geom GEOMETRY(POINT), id INT, climat VARCHAR)");
            stat.execute("insert into TABLE_POINTS values( 'POINT(1 2)', 1, 'bad')");
            stat.execute("CALL GeoJsonWrite('target/points_properties.geojson', 'table_points', true);");
            stat.execute("CALL GeoJsonRead('target/points_properties.geojson', 'table_points_import');");
            try (ResultSet res = stat.executeQuery("SELECT * FROM table_points_import;")) {
                res.next();
                assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POINT(1 2)")));
                assertTrue((res.getInt(2) == 1));
                assertEquals("bad", res.getString(3));
            }
        } finally {
            stat.execute("DROP TABLE IF EXISTS table_points_import");
            stat.close();
        }
    }

    @Test
    public void testReadWithNullValues() throws Exception {
        Statement stat = connection.createStatement();
        stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("startNull.geojson").getPath()) + ")");
        ResultSet res = stat.executeQuery("SELECT * FROM startNull;");
        ResultSetMetaData metaData = res.getMetaData();
        assertEquals("org.locationtech.jts.geom.Geometry", metaData.getColumnClassName(1));
        assertEquals("java.lang.Long", metaData.getColumnClassName(2));
        assertEquals("java.lang.String", metaData.getColumnClassName(3));
        assertEquals("java.lang.Long", metaData.getColumnClassName(4));
        assertEquals("java.lang.Long", metaData.getColumnClassName(5));
        assertEquals("java.lang.Long", metaData.getColumnClassName(6));
        assertEquals("java.lang.String", metaData.getColumnClassName(7));
        assertEquals("java.lang.String", metaData.getColumnClassName(8));
        assertEquals("java.lang.Long", metaData.getColumnClassName(9));
        assertEquals("java.lang.String", metaData.getColumnClassName(10));
        assertEquals("java.lang.Long", metaData.getColumnClassName(11));
        assertEquals("java.lang.Double", metaData.getColumnClassName(12));
    }


    @Test
    public void exportImportPointPostGIS(TestInfo testInfo) throws IOException, SQLException {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        Connection con= null;
        try {
            DataSource ds  = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test "+ testInfo.getDisplayName());
        }
        if(con!=null){
            Statement stat = con.createStatement();
            File ouputFile = new File("target/punctual_export_postgis.geojson");
            Files.deleteIfExists(ouputFile.toPath());
            stat.execute("DROP TABLE IF EXISTS PUNCTUAL");
            stat.execute("create table punctual(idarea int primary key, the_geom GEOMETRY(POINTZ, 4326))");
            stat.execute("insert into punctual values(1, ST_GEOMFROMTEXT('POINT(-10 109 5)',4326))");
            // Create a shape file using table area
            GeoJsonDriverFunction driver = new GeoJsonDriverFunction();
            driver.exportTable(con,"punctual", ouputFile,new EmptyProgressVisitor());
            // Read this shape file to check values
            assertTrue(ouputFile.exists());
            stat.execute("DROP TABLE IF EXISTS IMPORT_PUNCTUAL;");
            driver.importFile(con,  "IMPORT_PUNCTUAL", ouputFile, true, new EmptyProgressVisitor());
            ResultSet res = stat.executeQuery("SELECT THE_GEOM FROM IMPORT_PUNCTUAL;");
            res.next();
            Geometry geom = (Geometry) res.getObject(1);
            assertEquals(4326, geom.getSRID());
            Coordinate coord = geom.getCoordinate();
            assertEquals(coord.getZ(), 5, 10E-1);
            stat.execute("DROP TABLE IF EXISTS IMPORT_PUNCTUAL;");
            res.close();
        }
    }

    @Test
    public void testSelectWrite() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL GeoJsonWrite('target/lines.geojson', '(SELECT ST_GEOMFROMTEXT(''LINESTRING(1 10, 20 15)'', 4326) as the_geom)', true);");
            stat.execute("CALL GeoJsonRead('target/lines.geojson', 'TABLE_LINESTRINGS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
            res.next();
            Geometry geom = (Geometry) res.getObject("THE_GEOM");
            assertEquals(4326, geom.getSRID());
            GeometryAsserts.assertGeometryEquals("SRID=4326;LINESTRING(1 10, 20 15)", geom);
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
        }
    }

    @Test
    public void testPostGISImport() throws Exception {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        try (Connection con = dataSourceFactory.createDataSource(props).getConnection()) {
            GeoJsonDriverFunction geoJsonDriverFunction = new GeoJsonDriverFunction();
            geoJsonDriverFunction.importFile(con, "geojsontest", new File(GeojsonImportExportTest.class.getResource("data.geojson").getFile()), true, new EmptyProgressVisitor());
            try(ResultSet res = con.createStatement().executeQuery("SELECT * FROM geojsontest;")) {
                assertTrue(res.next());
                assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POLYGON ((7.49587624983838 48.5342070572556, 7.49575955525988 48.5342516702309, 7.49564286068138 48.5342070572556, 7.49564286068138 48.534117831187, 7.49575955525988 48.5340732180938, 7.49587624983838 48.534117831187, 7.49587624983838 48.5342070572556))")));
                assertEquals(-105576, res.getDouble(2), 0);
                assertEquals(275386, res.getDouble(3), 0);
                assertEquals(56.848998452816424, res.getDouble(4), 0);
                assertEquals(55.87291487481895, res.getDouble(5), 0);
                assertEquals(0.0, res.getDouble(6), 0);
                assertNull(res.getString(7));
                assertEquals(2, res.getDouble(8), 0);
                assertEquals("2017-01-19T18:29:26+01:00", res.getString(9));
                assertEquals("1484846966000", res.getBigDecimal(10).toString());
                assertEquals("2017-01-19T18:29:27+01:00", res.getString(11));
                assertEquals("1484846967000", res.getBigDecimal(12).toString());
                assertEquals("{\"member1\":1,\"member2\":{\"member21\":21,\"member22\":22}}", res.getString(13));
                assertEquals("[49,40.0,{\"member1\":1,\"member2\":{\"member21\":21,\"member22\":22}},\"string\",[13,\"string\",{\"member3\":3,\"member4\":4}]]", res.getString(14));
                assertEquals("[58,47,58,57,58,49,58,51,58,58,49,57,58,58,49,58,57,56,57,58,59,58,57,58,49,47,48,57,48,58,57,57,51,56,52,57,51,57,49,58,55,58,50,48,48,52,56,57,48,58,52,48,53,50,57,54,57,47,58,57,54,54,53,56,57,55,58,58,57,58,57,57]", res.getString(15));
                res.next();
            }
        } catch (PSQLException ex) {
            if (ex.getCause() == null || ex.getCause() instanceof ConnectException) {
                // Connection issue ignore
                log.warn("Connection error to local PostGIS, ignored", ex);
            } else {
                throw ex;
            }
        } catch (SQLException ex) {
            log.error(ex.getLocalizedMessage(), ex.getNextException());
            throw ex;
        }
    }

    @Test
    public void testSelectWritePostGIS(TestInfo testInfo) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/orbisgis_db";
        Properties props = new Properties();
        props.setProperty("user", "orbisgis");
        props.setProperty("password", "orbisgis");
        props.setProperty("url", url);
        Connection con = null;
        try {
            DataSource ds = dataSourceFactory.createDataSource(props);
            con = ds.getConnection();

        } catch (SQLException e) {
            log.warn("Cannot connect to the database to execute the test " + testInfo.getDisplayName());
        }
        if (con != null) {
            // Create a shape file using table area
            GeoJsonDriverFunction driver = new GeoJsonDriverFunction();
            driver.exportTable(con, "(SELECT ST_GEOMFROMTEXT('LINESTRING(1 10, 20 15)', 4326) as the_geom)",  new File("target/lines.geojson"), true,new EmptyProgressVisitor());
            driver.importFile(con, "TABLE_LINESTRINGS_READ", new File("target/lines.geojson"), new EmptyProgressVisitor());
            try (Statement stat = con.createStatement()) {
                ResultSet res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
                res.next();
                Geometry geom = (Geometry) res.getObject("THE_GEOM");
                assertEquals(4326, geom.getSRID());
                GeometryAsserts.assertGeometryEquals("SRID=4326;LINESTRING(1 10, 20 15)", geom);
                res.close();
                stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
            }
        }
    }


    @Test
    public void testSelectWriteReadGeojsonParameters() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS;");
            stat.execute("create table TABLE_LINESTRINGS(the_geom GEOMETRY(LINESTRING), id int)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 2, 5 3, 10 19)', 1)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 10, 20 15)', 2)");
            stat.execute("CALL GeoJsonWrite('target/lines.geojson', '(SELECT * FROM TABLE_LINESTRINGS WHERE ID=2)', TRUE);");
            stat.execute("CALL GeoJsonRead('target/lines.geojson', 'TABLE_LINESTRINGS_READ', true);");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING(1 10, 20 15)")));
            res.close();
            stat.execute("CALL GeoJsonRead('target/lines.geojson', true);");
            res = stat.executeQuery("SELECT * FROM TABLE_LINESTRINGS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("LINESTRING(1 10, 20 15)")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
        }
    }

    @Test
    public void testSelectWriteManyTimes() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS;");
            stat.execute("create table TABLE_LINESTRINGS(the_geom GEOMETRY(LINESTRING), id int)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 2, 5 3, 10 19)', 1)");
            stat.execute("insert into TABLE_LINESTRINGS values( 'LINESTRING(1 10, 20 15)', 2)");
            stat.execute("CALL GeoJsonWrite('target/lines.geojson', '(SELECT * FROM TABLE_LINESTRINGS WHERE ID=2)', true);");
            assertThrows(JdbcSQLException.class, () -> {
                stat.execute("CALL GeoJsonWrite('target/lines.geojson', 'TABLE_LINESTRINGS_READ');");
            });
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRINGS_READ");
        }
    }

    @Test
    public void testWriteReadNoSensitive() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
            stat.execute("create table TABLE_POINTS(the_geom GEOMETRY(POINTZ))");
            stat.execute("insert into TABLE_POINTS values( 'POINTZ(1 2 3)')");
            stat.execute("insert into TABLE_POINTS values( 'POINTZ(10 200 2000)')");
            stat.execute("CALL GeoJsonWrite('target/points.geojson', 'table_POINTS', true);");
            stat.execute("CALL GeoJsonRead('target/points.geojson', 'TABLE_POINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
            res.next();
            assertEquals(3,((Geometry) res.getObject(1)).getCoordinate().getZ());
            res.next();
            assertEquals(2000,((Geometry) res.getObject(1)).getCoordinate().getZ());
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        }
    }

    @Test
    public void testReadlGeojsonFeatures() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("features.geojson").getPath()) + ", 'TABLE_FEATURES', true);");
            ResultSet res = stat.executeQuery("SELECT count(*) FROM TABLE_FEATURES;");
            res.next();
            assertEquals(3, res.getInt(1));
        }
    }

    @Test
    public void testReadlGeojsonFeature() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("feature.geojson").getPath()) + ", 'TABLE_FEATURE', true);");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_FEATURE;");
            res.next();
            assertGeometryEquals("POINT Z (-1.637021666666667 47.15928666666667 10.2)", res.getObject(1));
            assertEquals(79.04200463708992, res.getDouble(2));
        }
    }

    @Test
    public void testReadlGeojsonGeometry() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL GeoJsonRead(" + StringUtils.quoteStringSQL(GeojsonImportExportTest.class.getResource("geometry.geojson").getPath()) + ", 'TABLE_GEOMETRY', true);");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_GEOMETRY;");
            res.next();
            assertGeometryEquals("POINT Z (-1.637021666666667 47.15928666666667 10.2)", res.getObject(1));
        }
    }

    @Test
    public void testWriteNullData() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS DATA;");
            stat.execute("create table DATA(the_geom GEOMETRY(GEOMETRY), id int, t time)");
            stat.execute("insert into DATA values( 'LINESTRING(1 2, 5 3, 10 19)', 1, '04:23:57')");
            stat.execute("insert into DATA values( 'LINESTRING(1 10, 20 15)', 2, null)");
            stat.execute("CALL GeoJsonWrite('target/lines.geojson', 'DATA', true);");
        }
    }

    @Test
    public void testWriteReadJsonExtension() throws Exception {
        try (Statement stat = connection.createStatement()) {
            File folderOut = new File("target/");
            String geojson = folderOut.getAbsolutePath() + File.separator + "mutilines.json";
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRINGS");
            stat.execute("create table TABLE_MULTILINESTRINGS(the_geom GEOMETRY(MULTILINESTRING))");
            stat.execute("insert into TABLE_MULTILINESTRINGS values( 'MULTILINESTRING ((90 220, 260 320, 280 200))')");
            stat.execute("CALL GeoJsonWrite('" +geojson+ "', 'TABLE_MULTILINESTRINGS', true);");
            stat.execute("CALL GeoJsonRead('" +geojson+ "', 'TABLE_MULTILINESTRINGS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_MULTILINESTRINGS_READ;");
            res.next();
            assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("MULTILINESTRING ((90 220, 260 320, 280 200))")));
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_MULTILINESTRINGS_READ");
        }
    }


    @Test
    public void testGeojsonPointXYZM() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINT");
            stat.execute("create table TABLE_POINT(idarea int primary key, the_geom GEOMETRY(POINTZM))");
            stat.execute("insert into TABLE_POINT values(1, 'POINTZM(1 2 5 1564184363)')");
            ResultSet res = stat.executeQuery("SELECT ST_AsGeoJSON(the_geom) from TABLE_POINT;");
            res.next();
            assertEquals("{\"type\":\"Point\",\"coordinates\":[1.0,2.0,5.0,1.564184363E9]}", res.getString(1));
            res.close();
        }
    }


    @Test
    public void testWriteReadXYZM() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
            stat.execute("create table TABLE_POINTS(the_geom GEOMETRY(POINTZM))");
            stat.execute("insert into TABLE_POINTS values( 'POINTZM(1 2 3 200)')");
            stat.execute("insert into TABLE_POINTS values( 'POINTZM(10 200 2000 250)')");
            stat.execute("CALL GeoJsonWrite('target/points.geojson', 'TABLE_POINTS', true);");
            stat.execute("CALL GeoJsonRead('target/points.geojson', 'TABLE_POINTS_READ');");
            ResultSet res = stat.executeQuery("SELECT * FROM TABLE_POINTS_READ;");
            res.next();
            assertEquals(3,((Geometry) res.getObject(1)).getCoordinate().getZ());
            assertEquals(200,((Geometry) res.getObject(1)).getCoordinate().getM());
            res.next();
            assertEquals(2000,((Geometry) res.getObject(1)).getCoordinate().getZ());
            assertEquals(250,((Geometry) res.getObject(1)).getCoordinate().getM());
            res.close();
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS_READ");
        }
    }
}
