package com.example.contactapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.contactapp.R;

public class ContactDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        TextView nameTextView = findViewById(R.id.text_contact_name);
        TextView phoneTextView = findViewById(R.id.text_contact_phone);
        Button callButton = findViewById(R.id.button_call);

        Intent intent = getIntent();
        String contactName = intent.getStringExtra("contactName");
        String contactPhone = intent.getStringExtra("contactPhone");

        nameTextView.setText(contactName);
        phoneTextView.setText(contactPhone);

        callButton.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + contactPhone));
            startActivity(callIntent);
        });
    }
}
