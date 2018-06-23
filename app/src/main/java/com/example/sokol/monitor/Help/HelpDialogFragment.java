package com.example.sokol.monitor.Help;

import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sokol.monitor.R;

public class HelpDialogFragment extends DialogFragment {
    private View mView;
    // tab to be used as default on startup
    private int startupTabIndex = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.help_dialog, null);

        setupTabs();

        return mView;
    }

    private void setupTabs(){
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        ViewPager viewPager = mView.findViewById(R.id.view_pager);
        PagerAdapter pagerAdapter = new FixedTabsPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = mView.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }
}
