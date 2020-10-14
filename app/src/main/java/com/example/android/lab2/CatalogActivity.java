package com.example.android.lab2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.lab2.data.Lab2Contract.PassEntry;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the data loader */
    private static final int PASS_LOADER = 0;

    /** Adapter for the ListView */
    PassCursorAdapter mCursorAdapter;
    public static String passString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        Intent intent = getIntent();
        passString = intent.getStringExtra("pass");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView passListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        passListView.setEmptyView(emptyView);

        mCursorAdapter = new PassCursorAdapter(this, null);
        passListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        passListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentPassUri = ContentUris.withAppendedId(PassEntry.CONTENT_URI, id);
                intent.setData(currentPassUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PASS_LOADER, null, this);
    }


    private void insertPass() {

    }

    private void deleteAllPass() {
        int rowsDeleted = getContentResolver().delete(PassEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pass database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // res/menu/menu_catalog.xml file
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertPass();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllPass();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                PassEntry._ID,
                PassEntry.COLUMN_RES,
                PassEntry.COLUMN_LOGIN };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                PassEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
