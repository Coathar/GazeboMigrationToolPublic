package com.coathar.framework;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class Migration
{
    public abstract String getDescription();

    public abstract FailureCase getFailureCase();

    protected String lastStatement;

    private int migrationNumber = -1;

    public int getMigrationNumber()
    {
        if (migrationNumber != -1)
        {
            return migrationNumber;
        }

        String[] splitName = this.getClass().getSimpleName().split("_");

        try
        {
            migrationNumber = Integer.parseInt(splitName[splitName.length - 1]);
        }
        catch (IllegalArgumentException ignored)
        {
            System.out.println("Error getting migration number from " + this.getClass().getSimpleName());
        }

        return migrationNumber;
    }

    /**
     * Gets whether this migration has run or not.
     * @param connection The connection to the SQL server
     * @return Whether the migration has run.
     * @throws SQLException
     */
    public boolean hasRun(Connection connection) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("SELECT [MigrationID] FROM [GazeboCore].[MigrationHistory] WHERE [MigrationNumber] = ?");
        statement.setInt(1, getMigrationNumber());
        statement.execute();

        return statement.getResultSet().next();
    }

    /**
     * Marks the migration as run.
     * @param connection Connection to the SQL server
     * @throws SQLException
     */
    public void markRan(Connection connection) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO [GazeboCore].[MigrationHistory] ([MigrationNumber], [MigrationSummary]) VALUES (?, ?)");
        statement.setInt(1, getMigrationNumber());
        statement.setString(2, getDescription());

        statement.execute();
    }

    /**
     * Gets the last statement ran for debugging purposes.
     * @return Last statement ran for debugging purposes.
     */
    public String getLastStatement()
    {
        return lastStatement;
    }

    /**
     * Code to run for the migration.
     * @param connection
     * @throws SQLException
     */
    public abstract void run(Connection connection) throws SQLException, IOException;

    /**
     * Creates a schema on the database if it does not exist.
     * @param connection Connection to the SQL server.
     * @param schema Schema to create.
     * @throws SQLException
     */
    public void createSchema(Connection connection, String schema) throws SQLException
    {
        lastStatement = "IF NOT EXISTS ( SELECT  *\n" +
                "                FROM    sys.schemas\n" +
                "                WHERE   name = N'" + schema + "' )\n" +
                "    EXEC('CREATE SCHEMA " + schema + "');";
        PreparedStatement preparedStatement = connection.prepareStatement(lastStatement);
        preparedStatement.execute();
    }

    /**
     * Creates a new table in the database.
     * @param connection Connection to the SQL server.
     * @param schema Schema to create the table under.
     * @param tableName Name of the table.
     * @param columns A list of columns to add.
     * @param constraints A list of constraints to add.
     * @throws SQLException
     */
    public void createTable(Connection connection, String schema, String tableName, List<Column> columns, List<Constraint> constraints) throws SQLException
    {
        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE [");
        sb.append(schema);
        sb.append("].[");
        sb.append(tableName);
        sb.append("] (");

        for (Column column : columns)
        {
            column.toSQL(sb);

            if (columns.indexOf(column) != columns.size() - 1 || constraints.size() > 0)
            {
                sb.append(", ");
            }
        }

        for (Constraint index : constraints)
        {
            index.toSQL(sb, tableName);

            if (constraints.indexOf(index) != constraints.size() - 1)
            {
                sb.append(",");
            }
        }

        sb.append(" );");

        lastStatement = sb.toString();
        connection.prepareStatement(lastStatement).execute();
    }

    /**
     * Adds a list of constraints to the given table.
     * @param connection Connection to the SQL server.
     * @param schemaName Schema of the table to modify.
     * @param tableName Name of the table to modify.
     * @param constraint List of constraints to add to the table.
     * @throws SQLException
     */
    public void addConstraints(Connection connection, String schemaName, String tableName, Constraint constraint) throws SQLException
    {
        StringBuilder query = new StringBuilder("ALTER TABLE [");
        query.append(schemaName);
        query.append("].[");
        query.append(tableName);
        query.append("] ");

        query.append("ADD ");
        constraint.toSQL(query, tableName);
        query.append(";");

        lastStatement = query.toString();
        connection.prepareStatement(lastStatement).execute();
    }

    /**
     * Adds a column to the given table. Please note that if you are adding a column that requires a value, you MUST
     * specify a default if there are already fields in it.
     * @param connection Connection to the SQL server.
     * @param schemaName Schema name of the table to modify.
     * @param tableName Table name of the table to modify.
     * @param column Column to add to the table.
     * @throws SQLException
     */
    public void addColumn(Connection connection, String schemaName, String tableName, Column column) throws SQLException
    {
        StringBuilder query = new StringBuilder("ALTER TABLE [");
        query.append(schemaName);
        query.append("].[");
        query.append(tableName);
        query.append("] ");
        query.append("ADD ");
        column.toSQL(query);

        lastStatement = query.toString();
        connection.prepareStatement(lastStatement).execute();
    }

    /**
     * Updates the given column in the table.
     * @param connection The connection to the SQL server.
     * @param schemaName Schema name of the table to modify.
     * @param tableName Table name of the table to modify.
     * @param column Column to modify on the table. The only thin you can modify is the data type and other information.
     *               To rename a column see
     * @throws SQLException
     */
    public void modifyColumn(Connection connection, String schemaName, String tableName, Column column) throws SQLException
    {
        StringBuilder query = new StringBuilder("ALTER TABLE [");
        query.append(schemaName);
        query.append("].[");
        query.append(tableName);
        query.append("] ");
        query.append("ALTER COLUMN ");
        column.toSQL(query);

        lastStatement = query.toString();
        connection.prepareStatement(lastStatement).execute();
    }

    public void dropConstraint(Connection connection, String schemaName, String tableName, String constraintName) throws SQLException
    {
        StringBuilder query = new StringBuilder("ALTER TABLE [");
        query.append(schemaName);
        query.append("].[");
        query.append(tableName);
        query.append("] ");
        query.append("DROP CONSTRAINT ");
        query.append(constraintName);

        lastStatement = query.toString();
        connection.prepareStatement(lastStatement).execute();
    }

    public void dropColumn(Connection connection, String schemaName, String tableName, String columnName) throws SQLException
    {
        StringBuilder query = new StringBuilder("ALTER TABLE [");
        query.append(schemaName);
        query.append("].[");
        query.append(tableName);
        query.append("] ");
        query.append("DROP Column ");
        query.append(columnName);

        lastStatement = query.toString();
        connection.prepareStatement(lastStatement).execute();
    }
}
