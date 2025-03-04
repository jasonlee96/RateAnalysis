package com.smiley.models;

public class CsvColumnModel {
    public int ColumnIndex;
    public String Value;

    public CsvColumnModel(int index, String value){
        this.ColumnIndex = index;
        this.Value = value;
    }
}
