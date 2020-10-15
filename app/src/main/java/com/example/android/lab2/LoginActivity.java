package com.example.android.lab2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText mPassEditText;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button entryBtn = (Button) findViewById(R.id.entry_btn);
        mPassEditText = (EditText) findViewById(R.id.pass_login);

        entryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, CatalogActivity.class);
                String passString = mPassEditText.getText().toString();
                if (TextUtils.isEmpty(passString)){
                    return;
                }
                intent.putExtra("pass", passString);
                startActivity(intent);
            }
        });
    }


}
