package com.example.android.lab2;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.lab2.data.Lab2Contract.PassEntry;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;

import javax.xml.parsers.ParserConfigurationException;


public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private final int MY_PERMISSIONS_EXTERNAL_STORAGE = 1;

    private static final int PASS_LOADER = 0;
    PassCursorAdapter mCursorAdapter;
    public static String passString;
    public static String previousPass;
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

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_EXTERNAL_STORAGE);
        }


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
        String getPass = pref.getString("pass", null);
        previousPass = null;

        if (getPass == null) {
            //the first login to the app
            //byte[] encryptPass = encryptString(passString);
            //String encryptPassStr = new String(encryptPass);
            savePassword();
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
                getPassDecrypt = EncryptOrDecrypt.decrypt(getPassByte, passString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!checkString.equals(getPassDecrypt)) {
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



    @RequiresApi(api = Build.VERSION_CODES.O)
    //import from Download
    public void importDatabase() throws IOException {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "App has no permission",
                    Toast.LENGTH_SHORT).show();
        }

        else {
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.dialog_export_data, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(promptsView);
            final EditText userInput = (EditText) promptsView.findViewById(R.id.file_name);
            builder.setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        public void onClick(DialogInterface dialog, int id) {
                            String fileName = userInput.getText().toString();
                            if (TextUtils.isEmpty(fileName)) {
                                return;
                            }
                            fileName = fileName + ".xml";
                            insertPass(fileName);
                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void insertPass(String fileName) {
        try {
            XmlPullParser xpp = prepareXpp(fileName);
            if (xpp == null)
                return;
            String helper = null;
            String resStr = null;
            String loginStr = null;
            String passStr = null;
            String notesStr = null;
            byte[] getPassByte = new byte[0];
            byte[] getNotesByte = new byte[0];
            byte[] getLoginByte = new byte[0];
            byte[] getResByte = new byte[0];
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    // начало документа
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // начало тэга
                    case XmlPullParser.START_TAG:
                        helper = xpp.getName();
                        int i;
                        for (i = 0; i < xpp.getAttributeCount(); i+=4) {
                            for (int j = 0; j < 4 ; j++) {
                                helper = xpp.getAttributeName(i+j);
                                switch (helper) {
                                    case "res":
                                        resStr = xpp.getAttributeValue(i+j);

                                        getResByte = Base64.getDecoder().decode(resStr);

                                        break;
                                    case "login":
                                        loginStr = xpp.getAttributeValue(i+j);
                                        if (loginStr.isEmpty()){
                                            Toast.makeText(this, "login is empty",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        getLoginByte = Base64.getDecoder().decode(loginStr);
                                        break;
                                    case "pass":
                                        passStr = xpp.getAttributeValue(i+j);
                                        if (passStr.isEmpty()){
                                            Toast.makeText(this, "password is empty",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        getPassByte = Base64.getDecoder().decode(passStr);
                                        break;
                                    case "notes":
                                        notesStr = xpp.getAttributeValue(i+j);
                                        getNotesByte = Base64.getDecoder().decode(notesStr);
                                        break;
                                    default:
                                        break;
                                }
                            }

                                ContentValues values = new ContentValues();
                                values.put(PassEntry.COLUMN_RES, getResByte);
                                values.put(PassEntry.COLUMN_LOGIN, getLoginByte);
                                values.put(PassEntry.COLUMN_PASSWORD, getPassByte);
                                values.put(PassEntry.COLUMN_NOTES, getNotesByte);

                                    Uri newUri = getContentResolver().insert(PassEntry.CONTENT_URI, values);

                                    if (newUri == null) {
                                        Toast.makeText(this, getString(R.string.editor_insert_pass_failed),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, getString(R.string.editor_insert_pass_successful),
                                                Toast.LENGTH_SHORT).show();
                                    }

                        }

                    case XmlPullParser.END_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    default:
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException | SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }



    XmlPullParser prepareXpp(String fileName) throws IOException, ParserConfigurationException, SAXException, XmlPullParserException {
        File download_folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File f = new File(download_folder, fileName);
        String line = null;
        if (!f.exists()) {
            Toast.makeText(this, "File not found for reading",
                Toast.LENGTH_SHORT).show();
            return null;
        }
            try {
                FileReader fis = new FileReader(f);
                BufferedReader bufRead = new BufferedReader(fis, 100);

                try {
                    line = bufRead.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.v(this.toString(), "IOException found in reading line from file.");
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();

            }

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput( new StringReader( line ) );

        return xpp;
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




    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                try {
                    importDatabase();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_delete_all_entries:
                deleteAllPass();
                return true;
            case R.id.action_change_password:
                changePass();
                return true;
            case R.id.action_export:
                try {
                    exportDatabase();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }





    @RequiresApi(api = Build.VERSION_CODES.O)
    private void exportData(String fileName) {
        try {
            File download_folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            //File data = Environment.getDataDirectory();
            XmlSerializer serial = Xml.newSerializer();

            if (download_folder.canWrite()) {
                File backupDB = new File(download_folder, fileName);
                //File currentDB = getDatabasePath("pass.db");
                FileOutputStream fileos = new FileOutputStream(backupDB);

                try {
                    serial.setOutput(fileos, null);
                    serial.startDocument(null, null);
                    serial.startTag(null, "resources");


                    String[] projection = {
                            PassEntry._ID,
                            PassEntry.COLUMN_RES,
                            PassEntry.COLUMN_LOGIN,
                            PassEntry.COLUMN_PASSWORD,
                            PassEntry.COLUMN_NOTES};
                    String resStr = null;
                    String loginStr = null;
                    Cursor loopCursor = getContentResolver().query(PassEntry.CONTENT_URI, projection, null, null);


                    if (loopCursor.moveToFirst()) {
                        int idLoop = 0;
                        do {
                            int idColumnIndex = loopCursor.getColumnIndex(PassEntry._ID);
                            idLoop = loopCursor.getInt(idColumnIndex);


                            int resColumnIndex = loopCursor.getColumnIndex(PassEntry.COLUMN_RES);
                            int loginColumnIndex = loopCursor.getColumnIndex(PassEntry.COLUMN_LOGIN);
                            int passColumnIndex = loopCursor.getColumnIndex(PassEntry.COLUMN_PASSWORD);
                            int notesColumnIndex = loopCursor.getColumnIndex(PassEntry.COLUMN_NOTES);

                            byte[] resByte = loopCursor.getBlob(resColumnIndex);
                            byte[] loginByte = loopCursor.getBlob(loginColumnIndex);
                            byte[] passByte = loopCursor.getBlob(passColumnIndex);
                            byte[] notesByte = loopCursor.getBlob(notesColumnIndex);

                            String toStr = null;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                toStr = Base64.getEncoder().encodeToString(resByte);
                                serial.attribute(null, "res", toStr);
                                toStr = Base64.getEncoder().encodeToString(loginByte);
                                serial.attribute(null, "login", toStr);
                                toStr = Base64.getEncoder().encodeToString(passByte);
                                serial.attribute(null, "pass", toStr);
                                toStr = Base64.getEncoder().encodeToString(notesByte);
                                serial.attribute(null, "notes", toStr);

                            }
                        } while (loopCursor.moveToNext());

                        serial.endTag(null, "resources");
                        serial.endDocument();
                        serial.flush();
                        fileos.close();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    //export to Download
    public void exportDatabase() throws IOException {


        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "App has no permission",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.dialog_export_data, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(promptsView);
            final EditText userInput = (EditText) promptsView.findViewById(R.id.file_name);
            builder.setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        public void onClick(DialogInterface dialog, int id) {
                            String fileName = userInput.getText().toString();
                            if (TextUtils.isEmpty(fileName)) {
                                return;
                            }
                            fileName = fileName + ".xml";
                            exportData(fileName);
                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }


    private void savePassword() {
        byte[] encryptPass = null;
        String checkString = "Hello! It is my string!";
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void changePassDB() {
        //decrypt all fields with the previous password, encrypt with a new one
        String[] projection = {
                PassEntry._ID,
                PassEntry.COLUMN_RES,
                PassEntry.COLUMN_LOGIN,
                PassEntry.COLUMN_PASSWORD,
                PassEntry.COLUMN_NOTES};
        String resStr = null;
        String loginStr = null;
        Cursor loopCursor = getContentResolver().query(PassEntry.CONTENT_URI, projection, null, null);


        if (loopCursor.moveToFirst()) {
            int idLoop = 0;
            do {
                int idColumnIndex = loopCursor.getColumnIndex(PassEntry._ID);
                idLoop = loopCursor.getInt(idColumnIndex);


                int resColumnIndex = loopCursor.getColumnIndex(PassEntry.COLUMN_RES);
                int loginColumnIndex = loopCursor.getColumnIndex(PassEntry.COLUMN_LOGIN);
                int passColumnIndex = loopCursor.getColumnIndex(PassEntry.COLUMN_PASSWORD);
                int notesColumnIndex = loopCursor.getColumnIndex(PassEntry.COLUMN_NOTES);

                byte[] resByte = loopCursor.getBlob(resColumnIndex);
                byte[] loginByte = loopCursor.getBlob(loginColumnIndex);
                byte[] passByte = loopCursor.getBlob(passColumnIndex);
                byte[] notesByte = loopCursor.getBlob(notesColumnIndex);

                try {
                    resStr = EncryptOrDecrypt.decrypt(resByte, previousPass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    loginStr = EncryptOrDecrypt.decrypt(loginByte, previousPass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String passStr = null;
                try {
                    passStr = EncryptOrDecrypt.decrypt(passByte, previousPass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String notesStr = null;
                try {
                    notesStr = EncryptOrDecrypt.decrypt(notesByte, previousPass);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                byte[] resStringbyte = null;
                try {
                    resStringbyte = EncryptOrDecrypt.encrypt(resStr, passString);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                byte[] loginStringbyte = null;
                try {
                    loginStringbyte = EncryptOrDecrypt.encrypt(loginStr, passString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                byte[] passStringbyte = null;
                try {
                    passStringbyte = EncryptOrDecrypt.encrypt(passStr, passString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                byte[] notesStringbyte = null;
                try {
                    notesStringbyte = EncryptOrDecrypt.encrypt(notesStr, passString);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ContentValues values = new ContentValues();
                values.put(PassEntry.COLUMN_RES, resStringbyte);
                values.put(PassEntry.COLUMN_LOGIN, loginStringbyte);
                values.put(PassEntry.COLUMN_PASSWORD, passStringbyte);
                values.put(PassEntry.COLUMN_NOTES, notesStringbyte);

                Uri currentPassUri = ContentUris.withAppendedId(PassEntry.CONTENT_URI, idLoop);
                int rowsAffected = getContentResolver().update(currentPassUri, values, null, null);
            } while (loopCursor.moveToNext());
            previousPass = null;
        }
    }

//похоже на говно - на самом деле так и есть, i'm sorry
    private void changePass() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_change_password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.new_password);
        builder.setCancelable(false)
                .setPositiveButton(R.string.action_change_password, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(DialogInterface dialog, int id) {
                        String checkIsEmpty = userInput.getText().toString();
                        if (TextUtils.isEmpty(checkIsEmpty)){
                            return;
                        }
                        previousPass = passString;
                        passString = checkIsEmpty;
                        savePassword();
                        changePassDB();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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