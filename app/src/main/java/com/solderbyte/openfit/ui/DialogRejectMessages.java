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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

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
        builder.setTitle(R.string.dialog_edit_rejectcall_messages);
        builder.setView(textEntryView);
        final ArrayList<EditText> editList = new ArrayList<EditText>();
        ViewGroup parentView = (ViewGroup) textEntryView;
        for(int i = 0; i < parentView.getChildCount(); ++i) {
            if(parentView.getChildAt(i) instanceof ScrollView) {
                ViewGroup parentViewScroll = (ViewGroup) parentView.getChildAt(i);
                for(int j = 0; j < parentViewScroll.getChildCount(); ++j) {
                    ViewGroup parentViewLayout = (ViewGroup) parentViewScroll.getChildAt(j);
                    for(int k = 0; k < parentViewLayout.getChildCount(); ++k) {
                        if(parentViewLayout.getChildAt(k) instanceof EditText) {
                            editList.add((EditText) parentViewLayout.getChildAt(k));
                        }
                    }
                }
            }
        }
        for(int i = 0; i < this.savedMessages.size(); ++i) {
            editList.get(i).setText(this.savedMessages.get(i));
        }
        builder.setPositiveButton(R.string.dialog_save_rejectcall_messages, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                ArrayList<String> messages = new ArrayList<String>();
                for(int i = 0; i < editList.size(); ++i) {
                    String strField = editList.get(i).getText().toString().trim();
                    if(strField.equals("")) {
                        continue;
                    }
                    messages.add(strField);
                }
                Intent msg = new Intent(INTENT_UI_REJECTMESSAGES);
                msg.putExtra("rejectMessages", messages);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(msg);
            }
        });
        builder.setNegativeButton(R.string.dialog_close_rejectcall_messages, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
            }
        });

        return builder.create();
    }
}
