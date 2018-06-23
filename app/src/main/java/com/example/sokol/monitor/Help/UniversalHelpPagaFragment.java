package com.example.sokol.monitor.Help;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.sokol.monitor.R;

public class UniversalHelpPagaFragment extends Fragment {

    private View myView;
    private String myMarkup;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.help_universal_page_fragment, container, false);
        populateLayoutWithParagraphs();
        return myView;
    }

    /**
     * use this method to get the fragment with markup set in it.
     * @param markup markup in the following format:
     *               title[p]paragraph text[/p]Another title[p]another paragraph[/p]
     * @return
     */
    public static UniversalHelpPagaFragment getFragWithContent(String markup){
        UniversalHelpPagaFragment frag = new UniversalHelpPagaFragment();
        frag.myMarkup = markup;
        return frag;
    }

    private void populateLayoutWithParagraphs(){
     LinearLayout paragraphsLayout = myView.findViewById(R.id.paragraphs_layout);

    }
}
