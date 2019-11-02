package com.example.sokol.monitor.Help;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.View;

import com.example.sokol.monitor.R;

class FixedTabsPagerAdapter extends FragmentPagerAdapter {
    FixedTabsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }
    private Context mContext;

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
                return mContext.getString(R.string.help_topic_getting_started);
            case HelpProvider.TOPIC_MAIN_ACTIVITY:
                return mContext.getString(R.string.help_topic_general);
            case HelpProvider.TOPIC_CATS:
                return mContext.getString(R.string.help_topic_cats);
            case HelpProvider.TOPIC_LOGS:
                return mContext.getString(R.string.help_topic_logs);
        }
        return super.getPageTitle(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        ViewOwner potentialOwner = (ViewOwner) object;
        return potentialOwner.owns(view);
    }
}
