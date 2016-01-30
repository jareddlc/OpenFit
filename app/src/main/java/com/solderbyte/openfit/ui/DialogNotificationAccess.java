package com.solderbyte.openfit.ui;

import com.solderbyte.openfit.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class DialogNotificationAccess  extends DialogFragment {

    public DialogNotificationAccess() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_notification_access);
        builder.setMessage(R.string.dialog_message_notification_access);
        builder.setNegativeButton(R.string.dialog_close_notification_access, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {}
        });
        builder.setPositiveButton(R.string.dialog_open_notification_access,  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }
        });

        return builder.create();
    }
}
