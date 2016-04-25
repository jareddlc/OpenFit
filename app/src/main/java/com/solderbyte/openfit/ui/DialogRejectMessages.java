package com.solderbyte.openfit.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.solderbyte.openfit.R;

import java.util.ArrayList;

public class DialogRejectMessages extends DialogFragment {
    private static final String LOG_TAG = "OpenFit:DialogRejectMessages";
    private static final String INTENT_UI_REJECTMESSAGES = "com.solderbyte.openfit.ui.rejectmessages";

    private ArrayList<String> savedMessages;

    public DialogRejectMessages() {}

    public DialogRejectMessages(ArrayList<String> sm) {
        savedMessages = sm;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.reject_messages, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_edit_reject_messages);
        builder.setView(textEntryView);
        builder.setPositiveButton(R.string.dialog_save_reject_messages, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
            }
        });
        builder.setNegativeButton(R.string.dialog_close_reject_messages, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
            }
        });

        return builder.create();
    }
}
