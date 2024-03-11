package com.coathar.framework;

import com.coathar.loaders.AchievementLoader;
import com.opencsv.*;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class Loader<T>
{
    private String fileName;
    private List<String> fileGuids = new ArrayList<>();

    private Class<T> cachedType = (Class<T>)
            ((ParameterizedType)getClass()
                    .getGenericSuperclass())
                    .getActualTypeArguments()[0];

    public void start(Connection connection) throws IOException
    {
        try (InputStream in = this.getClass().getResourceAsStream("/" + getFileName() + ".csv"))
        {
            CSVReaderBuilder builder = new CSVReaderBuilder(new InputStreamReader(in));
            CSVReader reader = builder
                    .withCSVParser(new RFC4180Parser())
                    .build();

            List<T> beans = new CsvToBeanBuilder<T>(reader).withType(cachedType).withQuoteChar('"').withSeparator(',').withIgnoreEmptyLine(true).build().parse();

            for (T bean : beans)
            {
                parseLine(connection, bean);
            }

            deleteExtraRecords(connection);
        }
    }

    protected void parseLine(Connection connection, T bean)
    {
        String gazeboID = null;
        try
        {
            Field gazeboIDField = bean.getClass().getDeclaredField("GazeboID");
            gazeboIDField.setAccessible(true);
            gazeboID = (String) gazeboIDField.get(bean);
            fileGuids.add(gazeboID);
            gazeboIDField.setAccessible(false);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new RuntimeException("A GazeboID field is required for loaded tables.");
        }


        T cached = getSavedRecord(connection, gazeboID);

        if (cached == null)
        {
            runInsert(connection, bean);
            return;
        }

        if (checkHash(bean, cached))
        {
            runUpdate(connection, bean);
        }
    }

    protected abstract boolean checkHash(T file, T cached);

    protected abstract String getSchema();

    protected T getSavedRecord(Connection connection, String gazeboID)
    {
        try
        {
            T toReturn = null;

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM [" + getSchema() + "].[" + cachedType.getSimpleName() + "] WHERE [GazeboID] = ?");
            statement.setString(1, gazeboID);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next())
            {
                toReturn = cachedType.newInstance();
                for (Field field : cachedType.getDeclaredFields())
                {
                    field.setAccessible(true);
                    try
                    {
                        field.set(toReturn, resultSet.getObject(field.getName()));
                    }
                    catch (SQLException ignored)
                    {
                        // Unable to get data from database, assume no value in db?
                        resultSet.close();
                        statement.close();
                        return null;
                    }

                    field.setAccessible(false);
                }
            }

            resultSet.close();
            statement.close();

            return toReturn;
        }
        catch (SQLException | InstantiationException | IllegalAccessException e)
        {
            return null;
        }
    }

    protected void runInsert(Connection connection, T bean)
    {
        try
        {
            StringBuilder sql = new StringBuilder("INSERT INTO [");
            sql.append(getSchema());
            sql.append("].[");
            sql.append(bean.getClass().getSimpleName());
            sql.append("] (");

            StringBuilder questionMark = new StringBuilder();
            for (Field field : bean.getClass().getDeclaredFields())
            {
                sql.append("[");
                sql.append(field.getName());
                sql.append("],");

                questionMark.append("?,");
            }
            sql.deleteCharAt(sql.length() - 1);
            questionMark.deleteCharAt(questionMark.length() - 1);

            sql.append(") VALUES (");
            sql.append(questionMark);
            sql.append(")");

            PreparedStatement statement = connection.prepareStatement(sql.toString());

            int i = 1;
            for (Field field : bean.getClass().getDeclaredFields())
            {
                field.setAccessible(true);
                statement.setObject(i++, field.get(bean));
                field.setAccessible(false);

            }
            statement.execute();
        }
        catch (SQLException | IllegalAccessException e)
        {
            throw new RuntimeException(bean.toString(), e);
        }
    }

    protected void runUpdate(Connection connection, T bean)
    {
        try
        {
            StringBuilder sql = new StringBuilder("UPDATE [");
            sql.append(getSchema());
            sql.append("].[");
            sql.append(bean.getClass().getSimpleName());
            sql.append("] SET ");
            for (Field field : bean.getClass().getDeclaredFields())
            {
                if (!field.getName().equalsIgnoreCase("gazeboid"))
                {
                    sql.append("[");
                    sql.append(field.getName());
                    sql.append("] = ?,");
                }

            }

            sql.deleteCharAt(sql.length() - 1);
            sql.append(" WHERE [GazeboID] = ?");

            PreparedStatement update = connection.prepareStatement(sql.toString());

            int i = 1;
            for (Field field : bean.getClass().getDeclaredFields())
            {
                if (!field.getName().equalsIgnoreCase("gazeboid"))
                {
                    field.setAccessible(true);
                    update.setObject(i++, field.get(bean));
                    field.setAccessible(false);
                }
            }

            Field gazeboIDField = bean.getClass().getDeclaredField("GazeboID");
            gazeboIDField.setAccessible(true);
            update.setObject(i, gazeboIDField.get(bean));
            gazeboIDField.setAccessible(false);
            update.execute();
        }
        catch (SQLException | IllegalAccessException | NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void deleteExtraRecords(Connection connection)
    {
        StringBuilder sql = new StringBuilder("DELETE FROM [");

        try
        {
            sql.append(getSchema());
            sql.append("].[");
            sql.append(cachedType.getSimpleName());
            sql.append("]");
            sql.append(" WHERE GazeboID NOT IN (");

            for (String guid : fileGuids)
            {
                sql.append("'");
                sql.append(guid);
                sql.append("',");
            }

            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
            connection.prepareStatement(sql.toString()).execute();
        }
        catch (SQLException e)
        {
            System.out.println("Error executing the following: " + sql);
            throw new RuntimeException(e);
        }
    }

    public String getFileName()
    {
        if (fileName != null)
        {
            return fileName;
        }

        // Remove the "Loader" off the end of the file names.
        fileName = this.getClass().getSimpleName().replaceAll("Loader", "");

        return fileName;
    }
}
