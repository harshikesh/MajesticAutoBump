package com.mejestic.bump.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import com.mejestic.R;
import com.mejestic.bump.util.Constants;

public class SettingsActivity extends AppCompatActivity {

  private ToggleButton toggle;
  private ToggleButton toll_toggle;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    toggle = (ToggleButton) findViewById(R.id.noti_toggle);
    toll_toggle = (ToggleButton) findViewById(R.id.toll_toggle);
    toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences pref =
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pref.edit().putBoolean(Constants.IS_NOTICATION, isChecked).apply();
      }
    });
    toll_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences pref =
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pref.edit().putBoolean(Constants.AVOID_TOLL, isChecked).apply();
      }
    });
  }
}
