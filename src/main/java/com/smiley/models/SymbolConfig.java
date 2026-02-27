package com.smiley.models;

import lombok.Data;

@Data
public class SymbolConfig {
    private String name;
    private String type;
    private String url;
    private String rateRegex;
    private WatcherConfig watcher;
}
