package com.example.sokol.monitor;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.sokol.monitor.DataBase.DbHelper;
import com.example.sokol.monitor.Help.HelpProvider;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

/**
 * Created by Sokol on 23.03.2018.
 */

public class CatsDialogFragment extends DialogFragment {

    OnNeedUserInterfaceUpdate mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnNeedUserInterfaceUpdate) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnNeedUserInterfaceUpdate");
        }
    }

    private View mView;
    private AutoCompleteTextView titleEditText;
    private EditText initialEditText;
    private RecyclerView recyclerView;
    private ImageButton addButton;

    private long lastEditorActionRegistered = 0;

    private void setupUIReferences(){
        titleEditText = mView.findViewById(R.id.cat_name);
        initialEditText = mView.findViewById(R.id.cat_initial);
        recyclerView = mView.findViewById(R.id.plans_recycler_view);
        addButton = mView.findViewById(R.id.add_imagebutton);
    }

    List<CatData> cats;
    List<CatData> Allcats;
    List<String> deletedCats;

    private void loadCatsData(){
        cats = new ArrayList<>();
        Allcats = null;
        deletedCats = new ArrayList<>();
        // load categories from the database
        DbHelper db = DbHelper.getInstance(getActivity());
        Allcats = db.getCategories(CatData.CATEGORY_STATUS_DELETED);
        if (Allcats == null) Allcats = new ArrayList<>();

        // extract only Inactive+
        for (int i = 0; i < Allcats.size(); i++) {
            CatData cat = Allcats.get(i);
            if (cat.getStatus() >= CatData.CATEGORY_STATUS_INACTIVE) {
                cats.add(cat);
            } else {
                deletedCats.add(cat.getTitle());
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mView = inflater.inflate(R.layout.cats_dialog, null);
        setupUIReferences();

        loadCatsData();

        final ArrayAdapter<String> suggestionsAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, deletedCats);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final CatsAdapter adapter = new CatsAdapter(cats);
        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder//.setMessage("Edit event")
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // unless nothing is changed:
                        if (!adapter.isDataChanged()) return;

                        // save cats to db
                        DbHelper db = DbHelper.getInstance(getActivity());
                        db.pushCategories(adapter.getAllCats());

                        // update the notification:
                        NotificationProvider.showNotificationIfEnabled(getActivity(), true);

                        // let mainactivity know cats have changed
                        mCallback.onNeedUserInterfaceUpdate();
                    }
                })
                .setNegativeButton("cancel", null)
                .setNeutralButton("help", null)
                .setView(mView);

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(getSimpleCallback(adapter, suggestionsAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        setupUIListeners(deletedCats, suggestionsAdapter, adapter);

        titleEditText.setAdapter(suggestionsAdapter);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        AlertDialog alertDialog = (AlertDialog) getDialog();
        Button okButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                HelpProvider.requestHelp(getActivity(), HelpProvider.TOPIC_CATS);
            }
        });
    }

    private void setupUIListeners(final List<String> deletedCats, final ArrayAdapter<String> suggestionsAdapter, final CatsAdapter adapter) {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewCat(deletedCats, suggestionsAdapter, adapter);
            }
        });

        initialEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                long now = TimeHelper.now();
                // "spam" protection in case of double editor actions.
                if(lastEditorActionRegistered < now-2000){
                    lastEditorActionRegistered = now;
                    saveNewCat(deletedCats, suggestionsAdapter, adapter);
                }
                return true;
            }
        });
    }

    private void saveNewCat(List<String> deletedCats, ArrayAdapter<String> suggestionsAdapter, CatsAdapter adapter) {
        String title = String.valueOf(titleEditText.getText());
        if (title.length() == 0) return;
        String initial = String.valueOf(initialEditText.getText());
        if (initial.length() == 0) return;

        // jeżeli dodano cat z sugerowanych, to więcej go nie sugeruje
        int indexWithinDeletedOnes = deletedCats.indexOf(title);
        if (indexWithinDeletedOnes != -1) {
            suggestionsAdapter.remove(title);
        }

        adapter.addACat(new CatData(title, initial, CatData.CATEGORY_STATUS_ACTIVE));
        titleEditText.setText("");
        initialEditText.setText("");

        titleEditText.requestFocus();
    }

    private ItemTouchHelper.SimpleCallback getSimpleCallback(final CatsAdapter adapter, final ArrayAdapter<String> suggestionsAdapter)
    {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(UP | DOWN, LEFT | RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                // move item in `fromPos` to `toPos` in adapter.

                adapter.move(fromPos, toPos);

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                CatsAdapter adapter = (CatsAdapter) recyclerView.getAdapter();
                int pos = viewHolder.getAdapterPosition();

                if (direction == LEFT || direction == RIGHT) {
                    // add to suggested cats:
                    suggestionsAdapter.add(adapter.getCatAt(pos).getTitle());

                    // remove
                    adapter.remove(pos);
                }
            }
        };

        return simpleCallback;
    }
}
