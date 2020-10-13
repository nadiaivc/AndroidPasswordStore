
package com.example.android.lab2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.lab2.data.Lab2Contract.PassEntry;

public class Lab2DbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = Lab2DbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "pass.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link Lab2DbHelper}.
     *
     * @param context of the app
     */
    public Lab2DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the  table
        String SQL_CREATE_PASS_TABLE =  "CREATE TABLE " + PassEntry.TABLE_NAME + " ("
                + PassEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PassEntry.COLUMN_RES + " TEXT NOT NULL, "
                + PassEntry.COLUMN_LOGIN + " TEXT, "
                + PassEntry.COLUMN_PASSWORD+ " TEXT NOT NULL, "
                + PassEntry.COLUMN_NOTES + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PASS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}