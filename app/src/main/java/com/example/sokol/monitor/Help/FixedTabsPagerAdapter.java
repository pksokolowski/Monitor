package com.example.sokol.monitor.Help;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.example.sokol.monitor.R;

class FixedTabsPagerAdapter extends FragmentPagerAdapter {
    public FixedTabsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }
    Context mContext;

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case HelpProvider.TOPIC_GETTING_STARTED:
                return UniversalHelpPageFragment.getFragWithContent(mContext.getString(R.string.help_universal_getting_started));
            case HelpProvider.TOPIC_MAIN_ACTIVITY:
                return UniversalHelpPageFragment.getFragWithContent(mContext.getString(R.string.help_universal_general));
            case HelpProvider.TOPIC_CATS:
                return UniversalHelpPageFragment.getFragWithContent(mContext.getString(R.string.help_universal_cats));
            case HelpProvider.TOPIC_LOGS:
                return UniversalHelpPageFragment.getFragWithContent(mContext.getString(R.string.help_universal_logs));
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case HelpProvider.TOPIC_GETTING_STARTED:
                return "Getting Started";
            case HelpProvider.TOPIC_MAIN_ACTIVITY:
                return "General";
            case HelpProvider.TOPIC_CATS:
                return "CATS";
            case HelpProvider.TOPIC_LOGS:
                return "LOGS";
        }
        return super.getPageTitle(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        ViewOwner potentialOwner = (ViewOwner) object;
        return potentialOwner.owns(view);
    }
}
