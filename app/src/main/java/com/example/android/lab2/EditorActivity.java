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
package com.example.android.lab2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.lab2.data.Lab2Contract.PassEntry;

/**
 * Allows user to create a new pass or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the data loader */
    private static final int EXISTING_PASS_LOADER = 0;

    private Uri mCurrentPassUri;
    private EditText mResEditText;
    private EditText mLoginEditText;
    private EditText mPassEditText;
    private EditText mNoteEditText;

    private boolean mPassHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPassHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPassHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentPassUri = intent.getData();

        if (mCurrentPassUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_pass));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_pass));

            // Initialize a loader to read the data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PASS_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mResEditText = (EditText) findViewById(R.id.edit_res);
        mLoginEditText = (EditText) findViewById(R.id.edit_login);
        mPassEditText = (EditText) findViewById(R.id.edit_pass);
        mNoteEditText = (EditText) findViewById(R.id.edit_notes);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mResEditText.setOnTouchListener(mTouchListener);
        mLoginEditText.setOnTouchListener(mTouchListener);
        mPassEditText.setOnTouchListener(mTouchListener);
        mNoteEditText.setOnTouchListener(mTouchListener);
    }


    private boolean saveInfo() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String resString = mResEditText.getText().toString().trim();
        String loginString = mLoginEditText.getText().toString().trim();
        String passString = mPassEditText.getText().toString().trim();
        String notesString = mNoteEditText.getText().toString().trim();

        if (TextUtils.isEmpty(loginString))
            loginString = "";

        if (TextUtils.isEmpty(resString)){
            Toast.makeText(this, getString(R.string.res_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
            }
        if (TextUtils.isEmpty(passString)){
            Toast.makeText(this, getString(R.string.pass_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
            }

        if (TextUtils.isEmpty(notesString))
            notesString = "";

        ContentValues values = new ContentValues();
        values.put(PassEntry.COLUMN_RES, resString);
        values.put(PassEntry.COLUMN_LOGIN, loginString);
        values.put(PassEntry.COLUMN_PASSWORD, passString);
        values.put(PassEntry.COLUMN_NOTES, notesString);

        if (mCurrentPassUri == null) {
            Uri newUri = getContentResolver().insert(PassEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_pass_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_pass_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentPassUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_pass_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_pass_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Delete item
        if (mCurrentPassUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // "Save"  option
            case R.id.action_save:
                if (saveInfo() == false)
                    return false;
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // "Up" button in the app bar
            case android.R.id.home:
                if (!mPassHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        if (!mPassHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all attributes, define a projection that contains
        // all columns from the table
        String[] projection = {
                PassEntry._ID,
                PassEntry.COLUMN_RES,
                PassEntry.COLUMN_LOGIN,
                PassEntry.COLUMN_PASSWORD,
                PassEntry.COLUMN_NOTES };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentPassUri,         // Query the content URI for the current
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int resColumnIndex = cursor.getColumnIndex(PassEntry.COLUMN_RES);
            int loginColumnIndex = cursor.getColumnIndex(PassEntry.COLUMN_LOGIN);
            int passColumnIndex = cursor.getColumnIndex(PassEntry.COLUMN_PASSWORD);
            int notesColumnIndex = cursor.getColumnIndex(PassEntry.COLUMN_NOTES);

            // Extract out the value from the Cursor for the given column index
            String res = cursor.getString(resColumnIndex);
            String login = cursor.getString(loginColumnIndex);
            String pass = cursor.getString(passColumnIndex);
            String notes = cursor.getString(notesColumnIndex);

            // Update the views on the screen with the values from the database
            mResEditText.setText(res);
            mLoginEditText.setText(login);
            mPassEditText.setText(pass);
            mNoteEditText.setText(notes);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mResEditText.setText("");
        mLoginEditText.setText("");
        mNoteEditText.setText("");
        mPassEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePass();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePass() {
        if (mCurrentPassUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentPassUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_pass_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_pass_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}