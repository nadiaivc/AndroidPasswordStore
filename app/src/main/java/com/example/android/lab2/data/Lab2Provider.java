/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.lab2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.lab2.data.Lab2Contract.PassEntry;

public class Lab2Provider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = Lab2Provider.class.getSimpleName();
    private static final int PASS = 100;
    private static final int PASS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(Lab2Contract.CONTENT_AUTHORITY, Lab2Contract.PATH_PASS, PASS);
        sUriMatcher.addURI(Lab2Contract.CONTENT_AUTHORITY, Lab2Contract.PATH_PASS + "/#", PASS_ID);
    }

    /** Database helper object */
    private Lab2DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new Lab2DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PASS:
                cursor = database.query(PassEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PASS_ID:
                selection = PassEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(PassEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PASS:
                return insertPass(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pass into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPass(Uri uri, ContentValues values) {
        String res = values.getAsString(PassEntry.COLUMN_RES);
        if (res == null) {
            throw new IllegalArgumentException("res");
        }

        String pass = values.getAsString(PassEntry.COLUMN_PASSWORD);
        if (pass == null) {
            throw new IllegalArgumentException("password");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(PassEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PASS:
                return updatePass(uri, contentValues, selection, selectionArgs);
            case PASS_ID:
                selection = PassEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePass(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePass(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(PassEntry.COLUMN_RES)) {
            String name = values.getAsString(PassEntry.COLUMN_RES);
            if (name == null) {
                throw new IllegalArgumentException("res");
            }
        }

        if (values.containsKey(PassEntry.COLUMN_PASSWORD)) {
            String pass = values.getAsString(PassEntry.COLUMN_PASSWORD);
            if (pass == null) {
                throw new IllegalArgumentException("res");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(PassEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PASS:
                rowsDeleted = database.delete(PassEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PASS_ID:
                selection = PassEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(PassEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PASS:
                return PassEntry.CONTENT_LIST_TYPE;
            case PASS_ID:
                return PassEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
