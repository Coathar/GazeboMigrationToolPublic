package com.coathar.framework;

public class DefaultConstraint extends Constraint
{
    public String defaultValue;
    public String columnName;

    public DefaultConstraint(String defaultValue, String columnName)
    {
        super(ConstraintType.DEFAULT, (String) null);

        this.columnName = columnName;
        this.defaultValue = defaultValue;
    }

    public DefaultConstraint(int defaultValue, String columnName)
    {
        super(ConstraintType.DEFAULT, (String) null);

        this.columnName = columnName;
        this.defaultValue = Integer.toString(defaultValue);
    }

    public DefaultConstraint(boolean defaultValue, String columnName)
    {
        super(ConstraintType.DEFAULT, (String) null);

        this.columnName = columnName;
        this.defaultValue = defaultValue ? "1" : "2";
    }

    @Override
    public void toSQL(StringBuilder query, String tableName)
    {
        query.append("CONSTRAINT DF_");
        query.append(tableName);
        query.append("_");
        query.append(columnName);
        query.append(" DEFAULT");
        query.append(" '");
        query.append(defaultValue);
        query.append("' FOR ");
        query.append(columnName);
    }
}
