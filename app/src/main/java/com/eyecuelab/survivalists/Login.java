package com.eyecuelab.survivalists;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Login extends AppCompatActivity {

    @Bind(R.id.emailEditText) EditText mEmailEditText;
    @Bind(R.id.passwordEditText) EditText mPasswordEditText;
    @Bind(R.id.loginButton) Button mLoginButton;
    @Bind(R.id.registerButton) Button mRegisterButton;

    private Firebase mFirebaseRef;
    private ProgressDialog mAuthProgressDialog;
    private Firebase.AuthResultHandler mAuthResultHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

    }
}
