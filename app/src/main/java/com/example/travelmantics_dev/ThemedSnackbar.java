package com.example.travelmantics_dev;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;


public class ThemedSnackbar extends AppCompatActivity {

    //public static boolean snackbarTriggered = false;

    public static Snackbar make(View view, CharSequence text, int duration) {

        Snackbar snackbar = Snackbar.make(view, text, duration);
//        snackbar.getView().setBackgroundColor(getAttribute(view.getContext(), R.attr.colorAccent));
        snackbar.getView().setBackgroundColor(getAttribute(view.getContext(), R.attr.colorPrimary));
        return snackbar;
    }


    public static Snackbar make(View view, int resId, int duration) {
        return make(view, view.getResources().getText(resId), duration);
    }


    private static int getAttribute(Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

}