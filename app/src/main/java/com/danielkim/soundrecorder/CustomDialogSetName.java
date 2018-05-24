package com.danielkim.soundrecorder;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class CustomDialogSetName extends DialogFragment {

    private RecordingItem item;
    private DBHelper mDatabase;

    public void setItem(RecordingItem item) {
        this.item = item;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.dialog_save);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        final EditText edtName = (EditText) dialog.findViewById(R.id.edt_name);
        mDatabase = new DBHelper(getActivity());
        edtName.setText(item.getName().replace(".mp4", ""));
        dialog.findViewById(R.id.rl_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove();
                dismiss();
            }
        });

        dialog.findViewById(R.id.rl_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtName.getText().toString().equals("")) {
                    return;
                }
                if (rename(edtName.getText().toString()))
                    dismiss();
            }
        });
        return dialog;
    }

    public boolean rename(String name) {
        //rename a file

        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + name + ".mp4";
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory() && !item.getName().replace(".mp4","").equals(name)) {
            //file name is not unique, cannot rename file.
            Toast.makeText(getActivity(),
                    String.format(getActivity().getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            //file name is unique, rename file
            File oldFilePath = new File(item.getFilePath());
            oldFilePath.renameTo(f);
            mDatabase.renameItem(item, name, mFilePath);
            return true;
        }
    }

    public void remove() {
        //remove item from database, recyclerview and storage
        //delete file from storage
        File file = new File(item.getFilePath());
        file.delete();
        mDatabase.removeItemWithId(item.getId());
    }


}
