package de.Main.database;



public enum SQLDataType {
    CHAR(255),
    BOOLEAN,
    TEXT(255),
    INT(255),
    DOUBLE(255),
    WORLD(255),
    LONG;


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
