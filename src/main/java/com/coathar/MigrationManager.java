package com.coathar;

import com.coathar.framework.FailureCase;
import com.coathar.framework.Migration;
import com.coathar.gazebodblib.DatabaseConnection;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

public class MigrationManager
{
    private Connection connection;
    private DatabaseConnection dbConnection;

    public MigrationManager() throws SQLException
    {
        dbConnection = DatabaseConnection.getInstance();

        DatabaseConnection.ConnectionDetails connectionDetails = new DatabaseConnection.ConnectionDetails();
        // Fill in connection details



        dbConnection.setConnectionDetails(connectionDetails);
        connection = dbConnection.getConnection();
        connection.setAutoCommit(false);
    }

    public void processMigrations() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, SQLException
    {
        ClassPath classPath = ClassPath.from(getClass().getClassLoader());

        for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive("com.coathar.migrations"))
        {
            Class<?> clazz = Class.forName(classInfo.getName());

            Object tempObject = (clazz.getConstructor().newInstance());

            if (tempObject instanceof Migration)
            {
                Migration toRun = (Migration)tempObject;

                try
                {
                    if (toRun.hasRun(connection))
                    {
                        continue;
                    }
                }
                catch (SQLException ex)
                {
                    System.out.println("Error checking if migration has run migration: " + toRun.getClass().getSimpleName());
                    ex.printStackTrace();

                    if (toRun.getFailureCase() == FailureCase.STAY_OFFLINE)
                    {
                        break;
                    }

                    continue;
                }

                try
                {
                    System.out.println("Running Migration: " + toRun.getMigrationNumber());
                    toRun.run(connection);
                }
                catch (Exception ex)
                {
                    System.out.println("Error running migration: " + toRun.getClass().getSimpleName());
                    System.out.println("Last statement: " + toRun.getLastStatement());
                    connection.rollback();
                    ex.printStackTrace();

                    if (toRun.getFailureCase() == FailureCase.STAY_OFFLINE)
                    {
                        break;
                    }

                    continue;
                }

                try
                {
                    toRun.markRan(connection);
                }
                catch (SQLException ex)
                {
                    System.out.println("Error cleaning up migration: " + toRun.getClass().getSimpleName());
                    connection.rollback();
                    ex.printStackTrace();

                    if (toRun.getFailureCase() == FailureCase.STAY_OFFLINE)
                    {
                        break;
                    }

                    continue;
                }

                connection.commit();

                System.out.println("Finished Migration: " + toRun.getMigrationNumber());

            }
        }
    }
}
