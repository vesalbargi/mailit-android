package com.example.mailit.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mailit.R;
import com.example.mailit.beans.User;
import com.example.mailit.databinding.ActivitySignUpBinding;
import com.example.mailit.network.NetworkHelper;
import com.example.mailit.prefrences.PreferencesManager;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private ActivitySignUpBinding binding;
    private PreferencesManager preferences;
    private User user;
    private NetworkHelper networkHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setUpActionBar();
        initialize();
        setListeners();
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.show();
    }

    private void initialize() {
        networkHelper = NetworkHelper.getInstance(getApplicationContext());
        preferences = PreferencesManager.getInstance(this);
    }

    private void setListeners() {
        binding.signUpBtn.setOnClickListener(this::onSignUpClicked);
    }

    private void onSignUpClicked(View view) {
        readAndValidate();
    }

    private void readAndValidate() {
        String firstName = binding.firstNameEdt.getText().toString().trim();
        String lastName = binding.lastNameEdt.getText().toString().trim();
        String email = binding.emailEdt.getText().toString().trim();
        String password = binding.passwordEdt.getText().toString().trim();
        String confirmPass = binding.confirmPasswordEdt.getText().toString().trim();
        String profile = binding.profileEdt.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            binding.firstNameEdt.setError(getText(R.string.invalid_input_error));
        } else if (TextUtils.isEmpty(lastName)) {
            binding.lastNameEdt.setError(getText(R.string.invalid_input_error));
        } else if (TextUtils.isEmpty(email)) {
            binding.emailEdt.setError(getText(R.string.invalid_input_error));
        } else if (TextUtils.isEmpty(password)) {
            binding.passwordEdt.setError(getText(R.string.invalid_input_error));
        } else if (TextUtils.isEmpty(confirmPass)) {
            binding.confirmPasswordEdt.setError(getText(R.string.invalid_input_error));
        } else if (!isValidEmailAddress(email)) {
            binding.emailEdt.setError(getText(R.string.invalid_email));
        } else if (!password.equals(confirmPass)) {
            binding.passwordEdt.setError(getText(R.string.passwords_not_match));
            binding.confirmPasswordEdt.setError(getText(R.string.passwords_not_match));
        } else {
            if (!isValidURL(profile)) {
                int randomColor = ((int) (Math.random() * 16777215)) | (0xFF << 24);
                String color = Integer.toString(randomColor);
                user = new User(firstName, lastName, email, password, color);
            } else {
                user = new User(firstName, lastName, email, password, profile);
            }
            signUpProcess();
            finish();
        }
    }

    private void signUpProcess() {
        networkHelper.signupUser(user, new ResultListener<User>() {
            @Override
            public void onResult(Result<User> result) {
                Log.d(TAG, "Result of signing user up in server: " + result);
                Error error = (result != null) ? result.getError() : null;
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.user_signup_error);
                    Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
    }
}
