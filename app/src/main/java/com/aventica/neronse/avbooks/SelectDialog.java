package com.aventica.neronse.avbooks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class SelectDialog extends DialogFragment {
    public interface SelectDialogListener{
        void onSelectType(String type);
    }

    SelectDialogListener selectDialogListener;

    public static SelectDialog newInstance() {

        Bundle args = new Bundle();

        SelectDialog fragment = new SelectDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        selectDialogListener = (SelectDialogListener) getActivity();

        adb.setTitle(R.string.select_search_type)
                .setItems(R.array.search_type, (dialog, which) -> {
                    switch (which){
                        case 0:
                            selectDialogListener.onSelectType("inauthor:");
                            break;
                        case 1:
                            selectDialogListener.onSelectType("intitle:");
                            break;
                        case 2:
                            selectDialogListener.onSelectType("inpublisher:");
                            break;
                    }
                    dialog.cancel();
                });
        return adb.create();
    }
}
