package com.example.android.lab2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.lab2.data.Lab2Contract.PassEntry;

import java.util.Base64;
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PASS_LOADER = 0;
    PassCursorAdapter mCursorAdapter;
    public static String passString;

    //private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    //private static final String ALIAS = "test-key";
    //private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";

    //Cipher cipher = null;
    //SecretKey secretKey = null;
    //KeyStore keyStore;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        passString = intent.getStringExtra("pass");
        String checkString = "Hello! It is my string!";

        /* try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        }
        catch(Exception e) {}

        //////// KEY GENERATION
        KeyGenParameterSpec aesSpec = new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setKeySize(128)
                .build();

        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            keyGenerator.init(aesSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        keyGenerator.generateKey();
*/
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        String getPass = pref.getString("pass",null);
        if (getPass == null) {
            //the first login to the app
            //byte[] encryptPass = encryptString(passString);
            //String encryptPassStr = new String(encryptPass);
            byte[] encryptPass = null;
            try {
                encryptPass = EncryptOrDecrypt.encrypt(checkString, passString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String encryptPassStr = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                encryptPassStr = Base64.getEncoder().encodeToString(encryptPass);
            }
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("pass", encryptPassStr);
            editor.commit();
        }
        //equals
        else {
            //byte[] getPassByte = getPass.getBytes();
            //String getPassDecrypt = decryptString(getPassByte);

            byte[] getPassByte = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                getPassByte = Base64.getDecoder().decode(getPass);
            }

            String getPassDecrypt = null;
            try {
                getPassDecrypt = EncryptOrDecrypt.decrypt(getPassByte,passString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!checkString.equals(getPassDecrypt))
            {
                Toast.makeText(this, "Wrong password",
                        Toast.LENGTH_SHORT).show();
                super.onBackPressed();
                return;
            }
        }



        setContentView(R.layout.activity_catalog);
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

    //I tried to store password using KeyStore, but without results....
   /* public byte[] encryptString(String pass) {

        try {
            secretKey = ((KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null)).getSecretKey();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }

        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] source = pass.getBytes();
        byte[] encryptedBytes = new byte[0];
        try {
            encryptedBytes = cipher.doFinal(source);/////////////////////////////
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return encryptedBytes;
    }



    public String decryptString(byte[] encryptedBytes) {
        try {
            secretKey = ((KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null)).getSecretKey();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }

        IvParameterSpec ivParameterSpec = new IvParameterSpec(cipher.getIV());

        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] decryptedBytes = new byte[0];

        try {
            decryptedBytes = cipher.doFinal(encryptedBytes);/////////////////////
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String decryptedPass = new String(decryptedBytes);
        return decryptedPass;
    }*/
}
/*
        try {
            secretKey = ((KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null)).getSecretKey();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }

        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] source = passString.getBytes();
        byte[] encryptedBytes = new byte[0];
        try {
            encryptedBytes = cipher.doFinal(source);/////////////////////////////
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

*/