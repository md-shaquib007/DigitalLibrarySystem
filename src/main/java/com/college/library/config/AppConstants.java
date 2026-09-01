package com.college.library.config;

public final class AppConstants {

    public static final int BORROW_DAYS = 14;
    public static final double FINE_PER_DAY = 5.0;
    public static final int PAGE_SIZE = 10;
    public static final String UPLOAD_DIR = "uploads/books";
    public static final String SESSION_USER = "loggedInUser";
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB

    private AppConstants() {}
}
