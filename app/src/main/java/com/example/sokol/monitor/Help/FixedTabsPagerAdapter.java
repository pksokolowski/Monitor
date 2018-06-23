package com.example.sokol.monitor.Help;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

class FixedTabsPagerAdapter extends FragmentPagerAdapter {
    public FixedTabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case HelpProvider.TOPIC_GETTING_STARTED:
                return UniversalHelpPagaFragment.getFragWithContent("title[p]some small text[/p]");
            case HelpProvider.TOPIC_MAIN_ACTIVITY:
                return UniversalHelpPagaFragment.getFragWithContent("title[p]some small text[/p]title2[p]some small text2 llllllllll lllllllllllllllll llllllllllllllllllp ppppppppppppppppppppppppppppppppp[/p]");
            case HelpProvider.TOPIC_CATS:
                return UniversalHelpPagaFragment.getFragWithContent("");
            case HelpProvider.TOPIC_LOGS:
                return UniversalHelpPagaFragment.getFragWithContent("");
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
        return ((UniversalHelpPagaFragment)object).isThisYourView(view);
    }
}
