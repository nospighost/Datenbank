package de.Main.database;



public enum SQLDataType {
    CHAR(255),
    BOOLEAN,
    TEXT(255),
    INT(255),
    Double(255),
    World(255);

    private final long size;

    SQLDataType() {
        this.size = -1;
    }

    SQLDataType(int size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public String toSQL() {
        if (size > 0)
            return this.name().toUpperCase() + "(" + this.size + ")";
        return this.name().toUpperCase();


    }
}
