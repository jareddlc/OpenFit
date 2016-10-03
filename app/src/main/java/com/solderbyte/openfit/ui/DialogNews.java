package com.solderbyte.openfit.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.solderbyte.openfit.R;

public class DialogNews extends DialogFragment {
    public DialogNews() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater adbInflater = LayoutInflater.from(getActivity());
        View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);
        final CheckBox dontShowAgain = (CheckBox)eulaLayout.findViewById(R.id.skip);
        builder.setView(eulaLayout);

        builder.setTitle(R.string.dialog_title_news);
        builder.setMessage(R.string.dialog_message_news);

        builder.setPositiveButton(R.string.dialog_close_news,  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                // Store the answer in SharedPreferences
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean( OpenFitActivity.OpenFitFragment.PREFERENCE_SKIP_CHANGELOG_KEY,
                                   dontShowAgain.isChecked());
                editor.apply();
            }
        });

        return builder.create();
    }
}