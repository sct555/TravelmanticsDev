package com.example.travelmantics_dev;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    private static FirebaseUtil firebaseUtil;
    public static FirebaseUser mFirebaseUser;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageRef;
    public static FirebaseAuth.AuthStateListener mAuthListener;
    public static ArrayList<TravelDeal> mDeals;
    private static final int RC_SIGN_IN = 123;
    private static ListActivity caller;
    private FirebaseUtil(){};
    public static boolean isAdmin;
    public static String userEmail;
    public static String userDisplayName;
    public static String userUid;

    public static void openFbReference(String ref, final ListActivity callerActivity) {
        Log.d("CustomMessage","openFbReference()");

        if(firebaseUtil == null) {
            firebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;

            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null){
                        FirebaseUtil.signIn();
                        ListActivity.snackbarMessage = "";
                        Log.d("CustomMessage","ListActivity.snackbarMessage set to empty if()");
                        com.example.travelmantics_dev.DealActivity.snackbarMessage = "";
                        Log.d("CustomMessage","DealActivity.snackbarMessage set to empty if()");
                    }
                    else {
                        String userId = firebaseAuth.getUid();
                        checkAdmin(userId);
                        ListActivity.snackbarMessage = "";
                        Log.d("CustomMessage","ListActivity.snackbarMessage set to empty else()");
                        com.example.travelmantics_dev.DealActivity.snackbarMessage = "";
                        Log.d("CustomMessage","DealActivity.snackbarMessage set to empty else()");
                    }
//****
                    mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (mFirebaseUser != null) {
                        userEmail = mFirebaseUser.getEmail();
                        userDisplayName = mFirebaseUser.getDisplayName();
                        userUid = mFirebaseUser.getUid();
                    }

                    Log.d("CustomMessage","FirebaseUtil.openFbReference - userDisplayName: " + userDisplayName);
//****


                }
            };
            connectStorage();
        }
        mDeals = new ArrayList<TravelDeal>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);

    }

    private static void signIn() {

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
//                        .setLogo(R.mipmap.travelmantics_icon)                       //*************** didn't exist
                        .setLogo(R.drawable.foreground_original)
                        .setIsSmartLockEnabled(true)                                               //*************** didn't exist
                        .build(),
                RC_SIGN_IN);

    }

    private static void checkAdmin(String uid) {
        FirebaseUtil.isAdmin = false;
        DatabaseReference ref = mFirebaseDatabase.getReference().child("administrators").child(uid); //*************** administrator
        Log.d("CustomMessage", "ref: " + ref);

        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                Log.d("CustomMessage", "checkAdmin onChildAdded - FirebaseUtil.isAdmin is: " +FirebaseUtil.isAdmin);
                caller.showMenu();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        ref.addChildEventListener(listener);

    }


    public static void attachListener() {
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    public static void detachListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }

    public static void connectStorage() {
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference().child("deals_pictures");
    }
}
