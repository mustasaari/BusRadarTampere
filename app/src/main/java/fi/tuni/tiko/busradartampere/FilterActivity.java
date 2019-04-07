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

public class FilterActivity extends AppCompatActivity {

    ArrayList<String> lines = new ArrayList<>();
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

        for (String x : lines) {
            Switch switc = new Switch(this);
            switc.setText(x);

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
                        Log.d("BRT", "enabled" +buttonView.getText());
                        editor.putString(""+buttonView.getText(), "show");
                        editor.apply();
                    } else {
                        Log.d("BRT", "disabled" +buttonView.getText());
                        editor.putString(""+buttonView.getText(), "hide");
                        editor.apply();
                    }
                }
            });

            linearL.addView(switc);
        }

        Log.d("BRT","filter activity loaded");
    }

}
