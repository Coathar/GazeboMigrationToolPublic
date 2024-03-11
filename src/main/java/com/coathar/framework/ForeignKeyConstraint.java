package com.coathar.framework;

public class ForeignKeyConstraint extends Constraint
{
    private String parentSchema;
    private String parentTable;
    private String parentColumn;
    private String column;

    private DeleteUpdatePropagationType onDelete;
    private DeleteUpdatePropagationType onUpdate;

    public ForeignKeyConstraint(String parentSchema, String parentTable, String parentColumn, String column)
    {
        super(ConstraintType.FOREIGN, (String) null);

        this.parentSchema = parentSchema;
        this.parentTable = parentTable;
        this.parentColumn = parentColumn;
        this.column = column;

        onDelete = DeleteUpdatePropagationType.NO_ACTION;
        onUpdate = DeleteUpdatePropagationType.NO_ACTION;
    }

    public ForeignKeyConstraint setOnDelete(DeleteUpdatePropagationType propagationType)
    {
        onDelete = propagationType;
        return this;
    }

    public ForeignKeyConstraint setOnUpdate(DeleteUpdatePropagationType propagationType)
    {
        onUpdate = propagationType;
        return this;
    }

    @Override
    public void toSQL(StringBuilder query, String tableName)
    {
        query.append("CONSTRAINT FK_");
        query.append(tableName);
        query.append("_");
        query.append(column);
        query.append("_");
        query.append(parentTable);
        query.append(" FOREIGN KEY (");
        query.append(column);
        query.append(") REFERENCES [");
        query.append(parentSchema);
        query.append("].[");
        query.append(parentTable);
        query.append("] (");
        query.append(parentColumn);
        query.append(") ");

        if (onDelete != DeleteUpdatePropagationType.NO_ACTION)
        {
            query.append("ON DELETE ");
            query.append(onDelete.toString().replace("_", " "));
        }

        if (onUpdate != DeleteUpdatePropagationType.NO_ACTION)
        {
            query.append("ON UPDATE ");
            query.append(onUpdate.toString().replace("_", " "));
        }
    }

    public enum DeleteUpdatePropagationType
    {
        NO_ACTION,
        CASCADE,
        SET_NULL,
        SET_DEFAULT;
    }
}