package com.example.sokol.monitor.EasyCatsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.ErrorMessageConcatenator;
import com.example.sokol.monitor.R;

import java.util.List;

public class EasyCatsEditor extends DialogFragment implements Dialog.OnClickListener, View.OnClickListener {
    private View mView;

    private CatData mCat;
    private int mCatIndex;
    private String[] mDeletedCatsTitles;
    private List<String> mNonDeletedCatsTitles;

    private AutoCompleteTextView mTitleEdit;
    private EditText mInitialEdit;
    private CheckBox mActiveEdit;

    public void setDeletedCats(String[] deletedCats){
        mDeletedCatsTitles = deletedCats;
    }

    public void setCat(CatData cat, int i) {
        mCat = cat;
        mCatIndex = i;
    }

    public void setOnInteractionEndedListener(OnInteractionEnded listener) {
        mListener = listener;
    }
    private OnInteractionEnded mListener;

    public interface OnInteractionEnded {
        void onCatCreated(CatData cat);
        void onCatDeleted(CatData cat, int i);
        void onCatChanged(int i, CatData replacementCat);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.easy_ui_cats_editor, null);

        setupUI();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setNeutralButton(R.string.easy_ui_button_delete, this)
                .setPositiveButton(R.string.easy_ui_button_save, null)
                .setNegativeButton(R.string.easy_ui_button_cancel, null);

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // close keyboard:
    }

    private void setupUI() {
        mTitleEdit = mView.findViewById(R.id.title);
        mInitialEdit = mView.findViewById(R.id.initial);
        mActiveEdit = mView.findViewById(R.id.active);

        if (mCat != null) {
            mTitleEdit.setText(mCat.getTitle());
            // since cat title is unique for each cat, it can't be changed
            // on an existing cat
            mTitleEdit.setEnabled(false);
        }
    }

    private boolean isInputCorrect() {
        ErrorMessageConcatenator errors = new ErrorMessageConcatenator();

        String title = mTitleEdit.getText().toString();
        String initial = mInitialEdit.getText().toString();

        if(title.length() == 0) {
            errors.add(getString(R.string.cats_dialog_error_no_title));
            mTitleEdit.setError(getString(R.string.cats_dialog_error_no_title));
        } else {
            // TODO: 29.06.2018 check if the cat already exists and is nonDeleted
            // best ask Fragment...
        }

        if(initial.length() == 0){
            errors.add(getString(R.string.cats_dialog_error_no_initial));
            mInitialEdit.setError(getString(R.string.cats_dialog_error_no_initial));
        }

        if (errors.size() == 0) return true;

        return false;
    }

    private CatData makeCat(){
        long ID = -1;
        String title = mTitleEdit.getText().toString();
        String initial = mInitialEdit.getText().toString();
        int status = mActiveEdit.isChecked() ? CatData.CATEGORY_STATUS_ACTIVE : CatData.CATEGORY_STATUS_INACTIVE;
        if(mCat != null){
            ID = mCat.getID();
            title = mCat.getTitle();
        }
        return new CatData(ID, title, initial, status);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        // deleting cat
        if(mCat == null) return;
        mListener.onCatDeleted(mCat, mCatIndex);
    }

    @Override
    public void onClick(View view) {
        // check data validity
        if(!isInputCorrect()) return;

        // save
        if(mCat == null){
            // creating a new cat
            mListener.onCatCreated(makeCat());
        }else{
            // changing an existing cat
            mListener.onCatChanged(mCatIndex, makeCat());
        }
        dismiss();
    }
}