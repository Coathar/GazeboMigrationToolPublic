package com.coathar.framework;

import java.util.Locale;

public class Column
{
    private String columnName;
    private DataType dataType;
    private String dataSize;
    private int dataScale;
    private boolean requireValue = true;
    private String defaultValue;
    private boolean isIdentityColumn;

    public Column(String columnName, DataType dataType)
    {
        this.columnName = "[" + columnName + "]";
        this.dataType = dataType;
    }

    public Column(String columnName)
    {
        this.columnName = "[" + columnName + "]";
    }

    /**
     * Sets the data size of the column. Used for string size and the first argument for decimals.
     * @param dataSize Data size to use.
     * @return The modified column
     */
    public Column setDataSize(int dataSize)
    {
        this.dataSize = Integer.toString(dataSize);
        return this;
    }

    /**
     * Sets the data size of the column. Used for string size and the first argument for decimals.
     * @param dataSize Data size to use.
     * @return The modified column
     */
    public Column setDataSize(String dataSize)
    {
        this.dataSize = dataSize;
        return this;
    }

    /**
     * Sets the data scale of the column. Used for the second argument in decimals.
     * @param dataScale Data size to use.
     * @return The modified column
     */
    public Column setDataScale(int dataScale)
    {
        this.dataScale = dataScale;
        return this;
    }

    /**
     * Sets whether the column's value is required. If you are modifying this on a table with existing values,
     * you MUST set a default as well.
     * @param requireValue Whether the column can be null.
     * @return The modified column
     */
    public Column setRequireValue(boolean requireValue)
    {
        this.requireValue = requireValue;
        return this;
    }

    /**
     * Sets the default value of the column.
     * @param defaultValue The default value of the column.
     * @return The modified column
     */
    public Column setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Sets the default value of the column.
     * @param defaultValue The default value of the column.
     * @return The modified column
     */
    public Column setDefaultValue(int defaultValue)
    {
        this.defaultValue = Integer.toString(defaultValue);
        return this;
    }

    /**
     * Sets the default value of the column.
     * @param defaultValue The default value of the column.
     * @return The modified column
     */
    public Column setDefaultValue(boolean defaultValue)
    {
        this.defaultValue = defaultValue ? "1" : "0";
        return this;
    }

    /**
     * Sets whether this column will be an identity column.
     * This just defaults to inserting IDENTITY(1, 1) for now.
     * @param isIdentityColumn Whether this column will be an identity column.
     * @return The modified column
     */
    public Column setIdentityColumn(boolean isIdentityColumn)
    {
        this.isIdentityColumn = isIdentityColumn;
        return this;
    }

    public void toSQL(StringBuilder query)
    {
        query.append(columnName);
        query.append(" ");

        if (dataType != null)
        {
            query.append(dataType.toString().toLowerCase(Locale.ROOT));

            if (dataType.allowsSize() && dataSize != null)
            {
                query.append("(");
                query.append(dataSize);

                if (dataType.allowsScale())
                {
                    query.append(",");
                    query.append(dataScale);
                }

                query.append(")");
            }
        }

        if (isIdentityColumn)
        {
            query.append(" IDENTITY (1, 1)");
        }

        if (requireValue)
        {
            query.append(" NOT NULL ");
        }

        if (defaultValue != null)
        {
            boolean isSQLFunction = defaultValue.equalsIgnoreCase("getdate()") || defaultValue.equalsIgnoreCase("current_timestamp");
            query.append("DEFAULT ");

            if (!isSQLFunction)
            {
                query.append("'");
            }

            query.append(defaultValue);

            if (!isSQLFunction)
            {
                query.append("'");
            }
            
            query.append(" ");
        }
    }
}
