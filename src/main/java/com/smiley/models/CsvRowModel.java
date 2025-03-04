package com.smiley.models;

import java.util.List;

public class CsvRowModel{
    public int RowIndex;
    public List<CsvColumnModel> Columns;

    public CsvRowModel(int index, List<CsvColumnModel> columns){
        this.RowIndex = index;
        this.Columns = columns;
    }
}
