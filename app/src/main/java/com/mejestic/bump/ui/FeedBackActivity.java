package com.mejestic.bump.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.mejestic.R;

public class FeedBackActivity extends AppCompatActivity {

  private EditText editttext;
  private Button sendButton;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feedback);
    editttext = (EditText) findViewById(R.id.edit_feedback);
    sendButton = (Button) findViewById(R.id.send);
    sendButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        String string = editttext.getText().toString();
        Toast.makeText(getApplicationContext(), "Thanks for the feedback!", Toast.LENGTH_SHORT)
            .show();
        finish();
      }
    });
  }
}
