package com.example.parstagram;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parstagram.R;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //if they are already signed in, advance to main activity
        if (ParseUser.getCurrentUser() != null){
            goMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick login button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the ParseUser
                ParseUser user = new ParseUser();
                // Set core properties
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();
                user.setUsername(username);
                user.setPassword(password);
                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            loginUser(username, password);
                        } else {
                            // Sign up didn't succeed. Look at the ParseException
                            // to figure out what went wrong
                            Log.e(TAG, "Issue with login", e);
                            Toast.makeText(LoginActivity.this, "Error creating account!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
            }
        });
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "attempting to login user "+ username);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                // if request doesn't succeed e is not null
                if (e != null){
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(LoginActivity.this, "Error signing in!", Toast.LENGTH_SHORT).show();
                    return;
                }
                goMainActivity();
                Toast.makeText(LoginActivity.this, "success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        // finish login acitivty once you've done the navigation
        finish();
    }
}
