package com.coathar;

import com.coathar.framework.Loader;
import com.coathar.framework.Migration;
import com.coathar.gazebodblib.DatabaseConnection;
import com.coathar.gazebodblib.models.gazebocore.PlayerQuestModel;
import com.coathar.gazebodblib.models.gazebocore.QuestModel;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

public class LoadedTableManager
{
    private Connection connection;
    private DatabaseConnection dbConnection;

    public LoadedTableManager() throws SQLException
    {
        dbConnection = DatabaseConnection.getInstance();

        DatabaseConnection.ConnectionDetails connectionDetails = new DatabaseConnection.ConnectionDetails();
        // Fill in connection details

        dbConnection.setConnectionDetails(connectionDetails);
        connection = dbConnection.getConnection();
        connection.setAutoCommit(false);
    }

    public void runSpecificLoader(Loader loader) throws SQLException
    {
        try
        {
            loader.start(connection);
        }
        catch (IOException ex)
        {
            connection.rollback();
            ex.printStackTrace();
        }
    }

    public void processLoaders() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, SQLException
    {
        ClassPath classPath = ClassPath.from(getClass().getClassLoader());

        for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive("com.coathar.loaders"))
        {
            Class<?> clazz = Class.forName(classInfo.getName());

            Object tempObject = (clazz.getConstructor().newInstance());

            if (tempObject instanceof Loader)
            {
                Loader loader = (Loader)tempObject;

                System.out.println("Running " + loader.getFileName() + " loader");
                try
                {
                    loader.start(connection);
                }
                catch (Exception ex)
                {
                    connection.rollback();
                    ex.printStackTrace();
                    continue;
                }

                connection.commit();
                System.out.println("Finished running " + loader.getFileName() + " loader");
            }
        }
    }
}
