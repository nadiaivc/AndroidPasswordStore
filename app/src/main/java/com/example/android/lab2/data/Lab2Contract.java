
package com.example.android.lab2.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

public final class Lab2Contract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private Lab2Contract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.lab2";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PASS = "passwords";

    public static final class PassEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PASS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PASS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PASS;


        public final static String TABLE_NAME = "passwords";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_RES = "res";

        public final static String COLUMN_LOGIN ="login";

        public final static String COLUMN_PASSWORD = "password";

        public final static String COLUMN_NOTES = "notes";

        public static final String LOGIN_UNKNOWN = null;
        public static final String NOTES_UNKNOWN = null;


        public static boolean isValidGender(int gender) {

                return true;

        }
    }

}

