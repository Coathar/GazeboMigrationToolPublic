package com.coathar.framework;

import java.util.Arrays;
import java.util.List;

public class Constraint
{
    private ConstraintType constraintType;
    private List<String> columnNames;
    private boolean isClustered;

    public Constraint(ConstraintType constraintType, String... columnNames)
    {
        this.constraintType = constraintType;

        if (constraintType == ConstraintType.PRIMARY)
        {
            isClustered = true;
        }

        this.columnNames = Arrays.asList(columnNames);
    }

    public void setClustered(boolean clustered)
    {
        isClustered = clustered;
    }

    public void toSQL(StringBuilder query, String tableName)
    {
        query.append("CONSTRAINT ");
        switch (constraintType)
        {
            case UNIQUE:
                query.append("AK_");
                query.append(tableName);
                for (String columnName : columnNames)
                {
                    query.append(columnName);

                    if (columnNames.indexOf(columnName) != columnNames.size() - 1)
                    {
                        query.append("_");
                    }
                }

                query.append(" UNIQUE");
                break;
            case PRIMARY:
                query.append("PK_");
                query.append(tableName);
                for (String columnName : columnNames)
                {
                    query.append(columnName);

                    if (columnNames.indexOf(columnName) != columnNames.size() - 1)
                    {
                        query.append("_");
                    }
                }

                query.append(" PRIMARY KEY");
                break;
        }

        if (isClustered)
        {
            query.append(" CLUSTERED");
        }

        query.append("(");

        for (String columnName : columnNames)
        {
            query.append(columnName);

            if (columnNames.indexOf(columnName) != columnNames.size() - 1)
            {
                query.append(", ");
            }
        }

        query.append(")");
    }
}
