package org.woheller69.weather.activities;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.webkit.WebView;
import org.woheller69.weather.R;

import androidx.appcompat.app.ActionBar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HelpActivity extends NavigationActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        WebView view = findViewById(R.id.help);

        String language = getResources().getConfiguration().getLocales().get(0).getLanguage();

        String filename = "help-"+language+".html";

        AssetManager am = getAssets();
        try {
            List<String> mapList = Arrays.asList(am.list("help"));

            if (!mapList.contains(filename)) {
                filename = "help-en.html";
            }
        } catch ( IOException ex){
            ex.printStackTrace();
        }

        view.loadUrl("file:///android_asset/help/"+ filename);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_help;
    }
}
