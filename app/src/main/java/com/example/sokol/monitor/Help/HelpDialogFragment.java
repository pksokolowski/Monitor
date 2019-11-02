package com.example.sokol.monitor.Help;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
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

    public void setInitialTopic(int topic){
        startupTabIndex = topic;
    }

    private void setupTabs(){
        ViewPager viewPager = mView.findViewById(R.id.view_pager);
        PagerAdapter pagerAdapter = new FixedTabsPagerAdapter(getChildFragmentManager(), getActivity());
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = mView.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        if(startupTabIndex != -1) {
            viewPager.setCurrentItem(startupTabIndex);
        }
    }
}
