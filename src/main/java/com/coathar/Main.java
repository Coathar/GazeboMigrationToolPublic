package com.coathar;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public class Main
{
    public static void main(String[] args) throws SQLException
    {
        MigrationManager migrationManager = new MigrationManager();
        LoadedTableManager loaderManager = new LoadedTableManager();

        try
        {
            migrationManager.processMigrations();
            loaderManager.processLoaders();
        }
        catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex)
        {
            ex.printStackTrace();
        }
    }
}
