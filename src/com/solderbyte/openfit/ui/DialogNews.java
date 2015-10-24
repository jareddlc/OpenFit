package com.solderbyte.openfit.ui;

import com.solderbyte.openfit.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DialogNews extends DialogFragment {

    public DialogNews() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_news);
        builder.setMessage(R.string.dialog_message_news);
        builder.setPositiveButton(R.string.dialog_close_news,  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {}
        });

        return builder.create();
    }
}