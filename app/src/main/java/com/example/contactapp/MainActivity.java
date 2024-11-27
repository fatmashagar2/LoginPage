package com.example.contactapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private ArrayList<String> contactList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addContactButton = findViewById(R.id.button_add_contact);
        ListView contactsListView = findViewById(R.id.list_contacts);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        contactsListView.setAdapter(adapter);

        addContactButton.setOnClickListener(v -> showAddContactDialog());

        contactsListView.setOnItemClickListener((parent, view, position, id) -> {
            String contact = contactList.get(position);
            String phoneNumber = contact.split(": ")[1];
            showCallDialog(contact, phoneNumber);
        });

        checkPermissions();
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_contact, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.edit_contact_name);
        EditText phoneEditText = dialogView.findViewById(R.id.edit_contact_phone);
        Button saveButton = dialogView.findViewById(R.id.button_save_contact);

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            if (!name.isEmpty() && !phone.isEmpty()) {
                addContact(name, phone);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter both name and phone", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void addContact(String name, String phone) {
        ContentValues values = new ContentValues();

        // Add raw contact
        values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, (String) null);
        values.put(ContactsContract.RawContacts.ACCOUNT_NAME, (String) null);
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = Long.parseLong(rawContactUri.getLastPathSegment());

        // Add contact name
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        // Add contact phone
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        contactList.add(name + ": " + phone);
        adapter.notifyDataSetChanged();
    }

    private void showCallDialog(String contactName, String phoneNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Call Contact");
        builder.setMessage("Do you want to call " + contactName + "?");

        builder.setPositiveButton("Call", (dialog, which) -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                makeCall(phoneNumber);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSIONS_REQUEST_CODE);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void makeCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.CALL_PHONE
            }, PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean allPermissionsGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
                if (allPermissionsGranted) {
                    Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
