
package com.example.android.lab2;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.lab2.data.Lab2Contract.PassEntry;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PassCursorAdapter extends CursorAdapter {


    public PassCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);

        int resColumnIndex = cursor.getColumnIndex(PassEntry.COLUMN_RES);
        int loginColumnIndex = cursor.getColumnIndex(PassEntry.COLUMN_LOGIN);

        byte[] res = cursor.getBlob(resColumnIndex);
        byte[] login = cursor.getBlob(loginColumnIndex);

        String resStr=null;
        try {
            resStr = decrypt(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String loginStr = null;
        try {
            loginStr = decrypt(login);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (TextUtils.isEmpty(loginStr)) {
            loginStr = "";
        }

        nameTextView.setText(resStr);
        summaryTextView.setText(loginStr);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String decrypt(byte[] encrypted) throws Exception {
        //byte[] encrypted = strEncrypted.getBytes();
        String password = CatalogActivity.passString;
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(passwordBytes);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/OFB/NoPadding");
        byte[] iv_byte ={-10,127,13,4,-8,-34,67,99,105,-97,33,56,-23,87,-67,7};
        //String iv = iv_byte.toString();
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv_byte));
        byte[] decryptedBytes = cipher.doFinal(encrypted);
        return new String(decryptedBytes);
    }
}
