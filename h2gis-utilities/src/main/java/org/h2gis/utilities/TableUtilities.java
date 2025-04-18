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

package org.h2gis.utilities;

import org.h2.tools.SimpleResultSet;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility in order to simplify table function usage
 *
 * @author Nicolas Fortin
 * @author Adam Gouge
 */
public class TableUtilities {

   private TableUtilities() {
       // This is a utility class.
   }

    /**
     * Copy fields from table into a {@link org.h2.tools.SimpleResultSet}
     *
     * @param rs Result set that will receive columns
     * @param metadata Import columns from this table
     *
     * @throws SQLException Error
     */
    public static void copyFields(SimpleResultSet rs, ResultSetMetaData metadata) throws SQLException {
        int columnCount = metadata.getColumnCount();
        for (int columnId = 1; columnId <= columnCount; columnId++) {
            String type = metadata.getColumnTypeName(columnId);
            String columnName =metadata.getColumnName(columnId);
            String label = metadata.getColumnLabel(columnId);
            if(label!=null){
                columnName = label;
            }
            //TODO : workarround due to the geometry type signature returned by H2  eg. GEOMETRY(POLYGON)
            if (type.toLowerCase().startsWith("geometry")) {
                rs.addColumn(columnName, metadata.getColumnType(columnId),
                        "GEOMETRY", metadata.getPrecision(columnId), metadata.getScale(columnId));
            }
            else{
                rs.addColumn(columnName, metadata.getColumnType(columnId),
                        type, metadata.getPrecision(columnId), metadata.getScale(columnId));
            }
        }
    }

    /**
     * Copy fields from table into a {@link org.h2.tools.SimpleResultSet}
     *
     * @param connection Active connection
     * @param rs Result set that will receive columns
     * @param tableLocation Import columns from this table
     *
     * @throws SQLException Error
     */
    public static void copyFields(Connection connection, SimpleResultSet rs, TableLocation tableLocation) throws SQLException {
        final Statement statement = connection.createStatement();
        try {
            final ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM " + tableLocation + " LIMIT 0;");
            try {
                copyFields(rs, resultSet.getMetaData());
            } finally {
                resultSet.close();
            }
        } finally {
            statement.close();
        }
    }

    /**
     * Return true if this connection only wants the list of columns.
     * This is a hack. See: https://groups.google.com/forum/#!topic/h2-database/NHH0rDeU258
     *
     * @param connection Connection
     *
     * @return True if this connection only wants the list of columns
     *
     */
    public static boolean isColumnListConnection(Connection connection) throws SQLException {
        return connection.getMetaData().getURL().equals("jdbc:columnlist:connection");
    }
    
    /**
     * Convert an input table String to a TableLocation
     *
     * @param connection Connection
     * @param inputTable Input table
     *
     * @return corresponding TableLocation
     *
     */
    public static TableLocation parseInputTable(Connection connection,
                                                String inputTable) throws SQLException {
       return TableLocation.parse(inputTable, DBUtils.getDBType(connection));
    }
    
    
    /**
     * Suffix a TableLocation
     *
     * @param inputTable Input table
     * @param suffix     Suffix
     *
     * @return suffixed TableLocation
     */
    public static TableLocation suffixTableLocation(TableLocation inputTable,
                                                    String suffix) {
        return new TableLocation(inputTable.getCatalog(), inputTable.getSchema(),
                inputTable.getTable() + suffix);
    }
    
    /**
     * Return the table identifier in the best fit depending on database type
     *
     * @param requestedTable Catalog and schema used
     * @param tableName Table without quotes
     * @param dbType Database type.
     *
     * @return Find table identifier
     */
    public static String caseIdentifier(TableLocation requestedTable, String tableName, DBTypes dbType) {
        return new TableLocation(requestedTable.getCatalog(), requestedTable.getSchema(),
                TableLocation.parse(tableName, dbType).getTable()).toString();
    }
}
