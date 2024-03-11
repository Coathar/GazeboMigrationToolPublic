package com.coathar.framework;

public enum DataType
{
    /**
     * Boolean
     */
    BIT,

    /**
     * Integer
     */
    TINYINT,
    SMALLINT,
    MEDIUMINT,
    INT,

    /**
     * Long
     */
    BIGINT,

    /**
     * Float
     */
    FLOAT(true, true),

    /**
     * Double
     */
    DOUBLE(true, true),

    /**
     * Big Decimal
     */
    DECIMAL(true, true),

    /**
     * String
     */
    NVARCHAR(true),
    VARCHAR(true),

    /**
     * Timestamp
     */
    DATETIME2,

    /**
     * UUID
     */
    UNIQUEIDENTIFIER;


    private boolean allowsSize = false;
    private boolean allowsScale = false;

    DataType()
    {

    }

    DataType(boolean allowsSize)
    {
        this.allowsSize = allowsSize;
    }

    DataType(boolean allowsSize, boolean allowsScale)
    {
        this.allowsSize = allowsSize;
        this.allowsScale = allowsScale;
    }

    public boolean allowsSize()
    {
        return this.allowsSize;
    }

    public boolean allowsScale()
    {
        return this.allowsScale;
    }
}
