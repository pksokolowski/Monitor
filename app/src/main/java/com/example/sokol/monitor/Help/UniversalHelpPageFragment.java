package com.example.sokol.monitor.Help;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sokol.monitor.R;

import java.util.regex.Pattern;

public class UniversalHelpPageFragment extends Fragment implements ViewOwner {

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
    public static UniversalHelpPageFragment getFragWithContent(String markup){
        UniversalHelpPageFragment frag = new UniversalHelpPageFragment();
        frag.myMarkup = markup;
        return frag;
    }

    private void populateLayoutWithParagraphs() {
        LinearLayout paragraphsLayout = myView.findViewById(R.id.paragraphs_layout);
        String[] ps = myMarkup.split(Pattern.quote("[/p]"));
        for (String s : ps) {
            int pIndex = s.indexOf("[p]");
            if (pIndex == -1) continue;

            String title = s.substring(0, pIndex).trim();
            String text = s.substring(pIndex + 3).trim();

            Paragraph p = new Paragraph(getContext());
            p.setContent(title, text);
            paragraphsLayout.addView(p);
        }
    }

    public boolean owns(View view){
        return view == myView;
    }

    private static class Paragraph extends LinearLayout{

        public Paragraph(Context context) {
            super(context);
            setup(context);
        }

        public Paragraph(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            setup(context);
        }

        private void setup(Context context) {
            inflate(context, R.layout.help_universal_page_fragment_paragraph, this);
        }

        public void setContent(String title, String text){
            TextView titleTextView = findViewById(R.id.title);
            TextView textTextView = findViewById(R.id.text);

            titleTextView.setText(title);
            textTextView.setText(text);
        }
    }
}
