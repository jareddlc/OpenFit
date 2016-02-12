package com.solderbyte.openfit.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.solderbyte.openfit.R;

public class DialogPurchase extends DialogFragment {
    public DialogPurchase() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_purchase);
        builder.setMessage(R.string.dialog_message_purchase);
        builder.setPositiveButton(R.string.dialog_close_purchase,  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {}
        });

        return builder.create();
    }
}
