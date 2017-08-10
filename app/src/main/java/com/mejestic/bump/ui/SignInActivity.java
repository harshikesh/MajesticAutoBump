package com.mejestic.bump.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.mejestic.R;

public class SignInActivity extends AppCompatActivity
    implements OnClickListener, GoogleApiClient.OnConnectionFailedListener {

  private static final String TAG = SignInActivity.class.getSimpleName();
  private static final int RC_SIGN_IN = 101;
  private Button buttonSignin;
  private GoogleApiClient mGoogleApiClient;
  private Uri mPhotoUrl;
  private String mName;
  private String mEmail;
  private SharedPreferences mSharedPref;
  private CheckBox mCheck;
  private FirebaseAuth mFirebaseAuth;
  private AuthCredential credential;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signin);
    mFirebaseAuth = FirebaseAuth.getInstance();

    GoogleSignInOptions gso =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(
            getString(R.string.default_web_client_id)).requestEmail().build();
    mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();

    buttonSignin = (Button) findViewById(R.id.btn_signin);
    buttonSignin.setOnClickListener(this);
    buttonSignin.setEnabled(false);
    findViewById(R.id.agreement).setOnClickListener(this);
    mCheck = (CheckBox) findViewById(R.id.check);
    mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          buttonSignin.setEnabled(true);
        } else {
          buttonSignin.setEnabled(false);
        }
      }
    });
    mSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    if (mSharedPref.getBoolean("issignin", false)) {
      callActivity();
    }
  }

  private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
    // Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
    if (acct == null) {
      return;
    }
    credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

    mFirebaseAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
          @Override public void onComplete(@NonNull Task<AuthResult> task) {
            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
          }
        });
  }

  //showing custom dialog for legar agreement
  protected Dialog showCustomDialog(int id) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(" LEGAL DISCLAIMER: \n"
        + "When you use PotHole services, we may collect and process information about your actual location.\n"
        + "We use various technologies to determine location, including IP address, GPS, and other sensors .")
        .setCancelable(false)
        .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putBoolean("accepted", true);
            editor.commit();
            mCheck.setChecked(true);
          }
        })
        .setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
          }
        });
    AlertDialog alert = builder.create();
    return alert;
  }

  @Override public void onClick(View v) {

    switch (v.getId()) {
      case R.id.btn_signin:
        signIn();
        break;
      case R.id.agreement:
        Dialog d = showCustomDialog(1);
        d.show();
        break;
    }
  }

  //Call Google sign in api
  private void signIn() {
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  //Get Google signin result
  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      firebaseAuthWithGoogle(result.getSignInAccount());
      handleSignInResult(result);
    }
  }

  //Update ui according to signin result, and call next activity
  private void handleSignInResult(GoogleSignInResult result) {
    Log.d(TAG, "handleSignInResult:" + result.isSuccess());
    if (result.isSuccess()) {
      GoogleSignInAccount acct = result.getSignInAccount();
      mPhotoUrl = acct.getPhotoUrl();
      mName = acct.getDisplayName();
      mEmail = acct.getEmail();
      nextActivity(true);
    } else {
      nextActivity(false);
    }
  }

  //Call Next activiy , send data like name, email, url to next screen
  private void nextActivity(boolean b) {
    if (b) {
      mSharedPref.edit().putBoolean("issignin", true).apply();
      mSharedPref.edit().putString("name", mName).apply();
      mSharedPref.edit().putString("email", mEmail).apply();
      mSharedPref.edit().putString("url", String.valueOf(mPhotoUrl)).apply();
    } else {
      mSharedPref.edit().putBoolean("issignin", false).apply();
    }
    callActivity();
  }

  // call Mainactivity class
  private void callActivity() {
    Intent in = new Intent(this, MainActivity.class);
    startActivity(in);
    finish();
  }

  // Api call back on connection callback of googleapiclient
  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.d(TAG, "onConnectionFailed:" + connectionResult);
  }
}
