package com.jvm4csharp.generator;

public final class TemplateHelper {
    private static String _newLine;

    public static final String SPACE;
    public static final String TAB;
    public static final String NEWLINE;
    public static final String BLOCK_OPEN;
    public static final String BLOCK_CLOSE;

    static {
        SPACE = " ";
        TAB = "\t";
        BLOCK_OPEN = "{";
        BLOCK_CLOSE = "}";
        NEWLINE = System.getProperty("line.separator");
    }
}
