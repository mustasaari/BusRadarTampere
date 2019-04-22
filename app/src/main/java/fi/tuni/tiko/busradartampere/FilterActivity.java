package fi.tuni.tiko.busradartampere;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Activity class for filtering bus routes.
 * This class only gets information about routes that were found during application running time.
 * When user disables lines, they will be stored on preferences in case that line is not found
 * next time user start application so that user can see disabled lines that are not present that time.
 * When line is enabled again the preference is removed.
 *
 * @author Mikko Mustasaari
 * @version 2019.0422
 * @since 1.0
 */

public class FilterActivity extends AppCompatActivity {

    /*
     * Lists for route informations
     */

    ArrayList<String> lines = new ArrayList<>();
    ArrayList<String> filteredLines = new ArrayList<>();
    SharedPreferences.Editor editor;

    /*
     * Try to do some sorting for lines and draw switched for lines
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Intent intent = getIntent();
        lines = intent.getStringArrayListExtra("allLines");

        SharedPreferences sharedpreferences = getSharedPreferences("fi.tuni.tiko.busradartampere", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        LinearLayout linearL = (LinearLayout) findViewById(R.id.filterlinearlayout);

        for(int i = 0; i < 200; i++) {
            for(String j : lines) {
                String withoutLetters = j.replaceAll("[^\\d]", "" );
                if (i == Integer.parseInt(withoutLetters)) {
                    filteredLines.add(j);
                }
            }
        }

        //draw buttons for present lines
        for (String x : filteredLines) {
            Switch switc = new Switch(this);
            switc.setText(x);
            switc.setTextSize(24);

            String state = sharedpreferences.getString(""+x, "true");
            if (state.equals("hide")) {
                switc.setChecked(false);
            }
            else {
                switc.setChecked(true);
            }

            switc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        editor.remove("" +buttonView.getText());
                        editor.apply();
                    } else {
                        editor.putString(""+buttonView.getText(), "hide");
                        editor.apply();
                    }
                }
            });

            linearL.addView(switc);
        }

        //Create buttons for lines that have saved preferences but are not currently operating
        Map<String, ?> allEntries = sharedpreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {

            if (!lines.contains(entry.getKey())) {

                Switch switc = new Switch(this);
                switc.setText(entry.getKey());
                switc.setTextSize(24);

                String state = sharedpreferences.getString(""+entry.getKey(), "true");
                if (state.equals("hide")) {
                    switc.setChecked(false);
                }
                else {
                    switc.setChecked(true);
                }

                switc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            editor.remove("" +buttonView.getText());
                            editor.apply();
                        } else {
                            editor.putString(""+buttonView.getText(), "hide");
                            editor.apply();
                        }
                    }
                });

                linearL.addView(switc);

            }
        }

    }

    public void createSwitchButton() {}

}
