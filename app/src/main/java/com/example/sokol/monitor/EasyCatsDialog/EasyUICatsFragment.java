package com.example.sokol.monitor.EasyCatsDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sokol.monitor.model.CatData;
import com.example.sokol.monitor.DataBase.DbHelper;
import com.example.sokol.monitor.Help.HelpProvider;
import com.example.sokol.monitor.notifications.NotificationProvider;
import com.example.sokol.monitor.OnNeedUserInterfaceUpdate;
import com.example.sokol.monitor.R;

import java.util.ArrayList;
import java.util.List;

public class EasyUICatsFragment extends DialogFragment implements EasyCatsAdapter.OnItemSelectedListener, View.OnClickListener, OnReorderDragListener, OnCatCheckedChange, EasyCatsEditor.OnInteractionEnded {
    private View mView;

    private List<CatData> mCats;
    List<String> mDeletedCats;
    private EasyCatsAdapter mCatsAdapter;
    private RecyclerView mRecycler;
    private FloatingActionButton mFab;
    private TextView mTitle;

    private ItemTouchHelper mItemTouchHelper;

    OnNeedUserInterfaceUpdate mUserInterfaceUpdater;

    private void loadCatsData(){
        mCats = new ArrayList<>();
        List<CatData> Allcats = null;
        mDeletedCats = new ArrayList<>();
        // load categories from the database
        DbHelper db = DbHelper.getInstance(getActivity());
        Allcats = db.getCategories(CatData.CATEGORY_STATUS_DELETED);
        if (Allcats == null) Allcats = new ArrayList<>();

        // extract only Inactive+
        for (int i = 0; i < Allcats.size(); i++) {
            CatData cat = Allcats.get(i);
            if (cat.getStatus() >= CatData.CATEGORY_STATUS_INACTIVE) {
                mCats.add(cat);
            } else {
                mDeletedCats.add(cat.getTitle());
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mUserInterfaceUpdater = (OnNeedUserInterfaceUpdate) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnNeedUserInterfaceUpdate");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.easy_ui_dialog, null);

        loadCatsData();
        mCatsAdapter = new EasyCatsAdapter(mCats, this, this);

        setupUIElements();

        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setAdapter(mCatsAdapter);

        mCatsAdapter.setOnItemSelectedListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mView);

        return builder.create();
    }

    private void setupUIElements(){
        mRecycler = mView.findViewById(R.id.easy_ui_recycler);
        mFab = mView.findViewById(R.id.easy_ui_fab);
        mTitle = mView.findViewById(R.id.easy_ui_title);

        mTitle.setText(R.string.easy_ui_cats_title);
        mFab.setOnClickListener(this);

        mItemTouchHelper = SimpleCallbackHelper.attachSimpleItemTouchHelper(mRecycler, mCatsAdapter);

        ImageView helpImage = mView.findViewById(R.id.easy_ui_help);
        helpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpProvider.requestHelp((AppCompatActivity)getActivity(), HelpProvider.TOPIC_CATS);
            }
        });
    }

    @Override
    public void onItemSelected(int i, CatData cat) {
        displayEditor(cat, i);
    }

    /**
     * fab (floating action button) click
     */
    @Override
    public void onClick(View view) {
        displayEditor(null, 0);
    }

    private void displayEditor(CatData cat, int i){
        EasyCatsEditor editor = new EasyCatsEditor();
        editor.setData(cat, i, mDeletedCats, ToCatArrayHelper.getTitlesOfNonDeletedCats(mCatsAdapter.getAllCats()));
        editor.setOnInteractionEndedListener(this);
        editor.show(getActivity().getSupportFragmentManager(), "CATS editor");
    }

    @Override
    public void onStartReorderDrag(RecyclerView.ViewHolder viewHolder) {
        // dragging the cat in the recycler
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onEndReorderDrag(long catA_ID, long catB_ID) {
        DbHelper db = DbHelper.getInstance(getActivity());
        db.swapCategories(catA_ID, catB_ID);

        letUserKnow();
    }

    @Override
    public void onCatChackerChange(CatData cat, int i) {
        DbHelper db = DbHelper.getInstance(getActivity());
        db.changeCategory(cat);
        letUserKnow();
    }

    // callbacks from EasyCatsEditor
    @Override
    public void onCatCreated(CatData cat) {
        DbHelper db = DbHelper.getInstance(getActivity());
        DbHelper.AdditionResult result = db.addCatIfAbsentUpdateOtherwise(cat);
        mCatsAdapter.addACat(result.getCatWithAssignedID(), result.getmOrderNumAmongNonDeletedCats());
        // no longer suggest title of this cat in editor, as it is already created again
        if(mDeletedCats.contains(cat.getTitle())) mDeletedCats.remove(cat.getTitle());
        letUserKnow();
    }

    @Override
    public void onCatDeleted(CatData cat, int i) {
        mCatsAdapter.remove(i);
        DbHelper db = DbHelper.getInstance(getActivity());
        db.deleteCategory(cat);
        // add to deleted cat titles, for suggestions.
        mDeletedCats.add(cat.getTitle());
        letUserKnow();
    }

    @Override
    public void onCatChanged(int i, CatData replacementCat) {
        mCatsAdapter.replace(i, replacementCat);
        DbHelper db = DbHelper.getInstance(getActivity());
        db.changeCategory(replacementCat);
        letUserKnow();
    }

    /**
     * This method saves (to the database) the differences between the data in mCatsAdapter
     * and in the database.
     * Notification is always shown to let the user know, their changes took effect.
     */

    private void letUserKnow() {
        NotificationProvider.showNotificationIfEnabled(getActivity(), true);
        // let mainactivity know cats have changed
        mUserInterfaceUpdater.onNeedUserInterfaceUpdate();
    }

}