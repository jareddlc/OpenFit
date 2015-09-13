package com.solderbyte.openfit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class DialogHelp extends DialogFragment {

    public DialogHelp() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.menu_help);
        builder.setMessage(R.string.open_help);

        return builder.create();
    }
}
