package com.example.travelmantics_dev;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    public static final int PICTURE_RESULT = 42;
    public static String snackbarMessage = "";

    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    EditText textCurrency;
    TravelDeal deal;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        txtTitle = (EditText) findViewById(R.id.editText_Title);
        txtDescription = (EditText) findViewById(R.id.editText3_Description);
        txtPrice = (EditText) findViewById(R.id.editText2_Price);
        imageView = (ImageView) findViewById(R.id.image);
        textCurrency = (EditText) findViewById(R.id.textView_Currency);

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;

        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());

        showImage(deal.getImageUrl());

        Button btnImage = (findViewById(R.id.btnImage));
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction((Intent.ACTION_GET_CONTENT));
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.setType("image/jpeg");
                startActivityForResult(Intent.createChooser(intent,"Insert Picture"), PICTURE_RESULT);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setContentView(R.layout.activity_list);

        View parentLayout = findViewById(android.R.id.content); //snackBar attempt

        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                clean();
//                backToList(snackbarMessage); //snackBar
                backToList();
                return true;

            case R.id.delete_menu:
                deleteDeal();
//                backToList(snackbarMessage); //snackBar attempt
                backToList();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditTexts(true);
        }
        else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditTexts(false);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            View parentLayout = findViewById(android.R.id.content);
            final Snackbar mySnackbar = ThemedSnackbar.make(parentLayout, "Uploading image...", Snackbar.LENGTH_INDEFINITE);
            mySnackbar.show();

            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());

            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            String pictureName = taskSnapshot.getStorage().getPath();
                            deal.setImageUrl(url);
                            deal.setImageName(pictureName);
                            Log.d("CustomMessage", "Image Url: " + url);
                            Log.d("CustomMessage", "Image Name: " + pictureName);
                            showImage(url);

                            mySnackbar.dismiss();

                            View parentLayout = findViewById(android.R.id.content);
                            Snackbar mySnackbar = ThemedSnackbar.make(parentLayout, "Upload complete", Snackbar.LENGTH_LONG).setDuration(4000);
                            mySnackbar.show();
                        }

                    });
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if(FirebaseUtil.isAdmin) {
            DealActivity.snackbarMessage = "Changes discarded";
            clean();
//            backToList(snackbarMessage);
            backToList();
            super.finish();
        }
        else {
            DealActivity.snackbarMessage = "";
//            backToList(snackbarMessage);
            backToList();
            super.finish();
        }
    }

    private void saveDeal() {
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());
        if (deal.getId() == null) {
            mDatabaseReference.push().setValue(deal);
            DealActivity.snackbarMessage = "Deal saved";
        } else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
            DealActivity.snackbarMessage = "";
        }
    }


    //Original deleteDeal()
    private void deleteDeal() {
        if (deal.getId() == null) {
            snackbarMessage = "Please save a deal before deleting it";
            return;
        }

        mDatabaseReference.child(deal.getId()).removeValue();

        if(deal.getImageName() !=null && deal.getImageName().isEmpty() == false) {
            StorageReference picref = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void avoid) {
                    Log.d("CustomMessage", "DealActivity deleteDeal() onSuccess - Image successfully deleted");
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("CustomMessage", "DealActivity deleteDeal() onFailure - Image deletion unsuccessful: Error: " + e.getMessage());
                }
            });
            snackbarMessage = "Deal deleted";
        }
    }


    //snackBar backToList
    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
//        intent.putExtra("snackbarMessage",DealActivity.snackbarMessage);
        startActivity(intent);
    }

    private void clean() {
        txtTitle.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtTitle.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled) {
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
        textCurrency.setEnabled(false);

        Button btnImage = (findViewById(R.id.btnImage));
        btnImage.setEnabled(isEnabled);
        if(isEnabled) {
            btnImage.setVisibility(View.VISIBLE);
        }
        else {
            btnImage.setVisibility(View.GONE);
        }
    }

    private void showImage(String url) {
        if (url != null && url.isEmpty() == false) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_STORE)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(imageView);
        }
    }
}
