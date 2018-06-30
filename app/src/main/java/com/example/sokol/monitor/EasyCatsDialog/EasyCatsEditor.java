package com.example.sokol.monitor.EasyCatsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import com.example.sokol.monitor.CatData;
import com.example.sokol.monitor.ErrorMessageConcatenator;
import com.example.sokol.monitor.R;

import java.util.List;

public class EasyCatsEditor extends DialogFragment implements Dialog.OnClickListener, View.OnClickListener, TextWatcher {
    private View mView;

    private CatData mCat;
    private int mCatIndex;
    private String[] mDeletedCatsTitles;
    private List<String> mNonDeletedCatsTitles;

    private AutoCompleteTextView mTitleEdit;
    private EditText mInitialEdit;
    private CheckBox mActiveEdit;

    public void setData(CatData catToEdit, int indexOfThecat, List<String> titlesOfDeletedCats, List<String> titlesOfNonDeletedCats){
        mCat = catToEdit;
        mCatIndex = indexOfThecat;
        mDeletedCatsTitles = ToCatArrayHelper.getArray(titlesOfDeletedCats);
        mNonDeletedCatsTitles = titlesOfNonDeletedCats;
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
            mInitialEdit.setText(mCat.getInitial());
            mActiveEdit.setChecked(mCat.getStatus() == CatData.CATEGORY_STATUS_ACTIVE);
            // since cat title is unique for each cat, it can't be changed
            // on an existing cat
            mTitleEdit.setEnabled(false);
        } else{
            // setup adapter with autocomplete suggestions for the title editText:
            final ArrayAdapter<String> suggestionsAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_dropdown_item_1line, mDeletedCatsTitles);
            mTitleEdit.setAdapter(suggestionsAdapter);
        }

        // remind user that initial should be short.
        mInitialEdit.addTextChangedListener(this);
    }

    private boolean isInputCorrect() {
        int errorCounter = 0;

        CatData provisionalCat = makeCat();

        // order matters, first the initial, because the last one to call requestFocus will get it.
        // and initial is less important than potential title errors.
        if(provisionalCat.getInitial().length() == 0){
            errorCounter+=1;
            mInitialEdit.setError(getString(R.string.cats_dialog_error_no_initial));
            mInitialEdit.requestFocus();
        }
        if(provisionalCat.getTitle().length() == 0) {
            errorCounter +=1;
            mTitleEdit.setError(getString(R.string.cats_dialog_error_no_title));
            mTitleEdit.requestFocus();
        } else if(mCat == null && mNonDeletedCatsTitles.contains(provisionalCat.getTitle())){
            errorCounter +=1;
            mTitleEdit.setError(getString(R.string.cats_dialog_error_title_is_taken));
            mTitleEdit.requestFocus();
        }

        if (errorCounter == 0) return true;

        return false;
    }

    private CatData makeCat(){
        long ID = -1;
        String title = mTitleEdit.getText().toString().trim();
        String initial = mInitialEdit.getText().toString().trim();
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

    // TextWatcher for the mInitialEdit EditText. Guarding against too long initials.
    // but not forbidding them. Just letting the user know.
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // get length in characters as discernible by the user, counting emojis as
        // a single character.
        int codePoints = (int) charSequence.toString().codePoints().count();

        if(codePoints > 1){
            mInitialEdit.setError(getString(R.string.cats_dialog_error_long_initial));
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}