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

public class FilterActivity extends AppCompatActivity {

    ArrayList<String> lines = new ArrayList<>();
    ArrayList<String> filteredLines = new ArrayList<>();
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Intent intent = getIntent();
        lines = intent.getStringArrayListExtra("allLines");

        SharedPreferences sharedpreferences = getSharedPreferences("fi.tuni.tiko.busradartampere", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        //SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        LinearLayout linearL = (LinearLayout) findViewById(R.id.filterlinearlayout);

        //Collections.sort(lines);

        for(int i = 0; i < 200; i++) {
            for(String j : lines) {
                String withoutLetters = j.replaceAll("[^\\d]", "" );
                //Log.d("BRT", "without letters : " +withoutLetters);
                if (i == Integer.parseInt(withoutLetters)) {
                    filteredLines.add(j);
                    //lines.remove(j);
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
                        //Log.d("BRT", "enabled" +buttonView.getText());
                        //editor.putString(""+buttonView.getText(), "show");
                        editor.remove("" +buttonView.getText());
                        editor.apply();
                    } else {
                        //Log.d("BRT", "disabled" +buttonView.getText());
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
            Log.d("BRT", entry.getKey() + ": " + entry.getValue().toString());

            if (!lines.contains(entry.getKey())) {
                //Log.d("BRT", "should create button for line " +entry.getKey());

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
                            //Log.d("BRT", "enabled" +buttonView.getText());
                            //editor.putString(""+buttonView.getText(), "show");
                            //editor.apply();
                            editor.remove("" +buttonView.getText());
                            editor.apply();
                        } else {
                            //Log.d("BRT", "disabled" +buttonView.getText());
                            editor.putString(""+buttonView.getText(), "hide");
                            editor.apply();
                        }
                    }
                });

                linearL.addView(switc);

            }
        }

        //Log.d("BRT","filter activity loaded");
    }

    public void createSwitchButton() {}

}
