package com.coathar.migrations;

import com.coathar.framework.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class Migration_0001 extends Migration
{

    @Override
    public String getDescription()
    {
        return "Creates the Season table";
    }

    @Override
    public FailureCase getFailureCase()
    {
        return FailureCase.RESUME_NON_CRITICAL;
    }

    @Override
    public void run(Connection connection) throws SQLException, IOException
    {
        createTable(connection, "GazeboCore", "Season", Arrays.asList(
                        new Column("SeasonID", DataType.BIGINT).setIdentityColumn(true),
                        new Column("SeasonNumber", DataType.TINYINT),
                        new Column("GazeboID", DataType.UNIQUEIDENTIFIER),
                        new Column("StartDate", DataType.DATETIME2),
                        new Column("EndDate", DataType.DATETIME2)),

                Arrays.asList(new Constraint(ConstraintType.PRIMARY, "SeasonNumber"),
                        new Constraint(ConstraintType.UNIQUE, "GazeboID"))
        );
    }
}
