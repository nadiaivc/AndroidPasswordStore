
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
            resStr = EncryptOrDecrypt.decrypt(res, CatalogActivity.passString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String loginStr = null;
        try {
            loginStr = EncryptOrDecrypt.decrypt(login, CatalogActivity.passString);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (TextUtils.isEmpty(loginStr)) {
            loginStr = "";
        }

        nameTextView.setText(resStr);
        summaryTextView.setText(loginStr);
    }

}
