package com.example.mailit.activities;

import static com.example.mailit.prefrences.PreferencesManager.PREF_KEY_EMAIL;
import static com.example.mailit.prefrences.PreferencesManager.PREF_KEY_FIRST_NAME;
import static com.example.mailit.prefrences.PreferencesManager.PREF_KEY_IS_LOGIN;
import static com.example.mailit.prefrences.PreferencesManager.PREF_KEY_LAST_NAME;
import static com.example.mailit.prefrences.PreferencesManager.PREF_KEY_PROFILE;
import static com.example.mailit.prefrences.PreferencesManager.PREF_KEY_SESSION_TOKEN;
import static com.example.mailit.prefrences.PreferencesManager.PREF_KEY_USER_ID;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mailit.R;
import com.example.mailit.beans.User;
import com.example.mailit.data.AppData;
import com.example.mailit.data.async.UserAsyncTask;
import com.example.mailit.databinding.ActivityLoginBinding;
import com.example.mailit.network.NetworkHelper;
import com.example.mailit.prefrences.PreferencesManager;
import com.example.mailit.utils.Action;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private PreferencesManager preferences;
    private NetworkHelper networkHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        hideActionbar();
        initialize();
        setListeners();
    }

    private void hideActionbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        getSupportActionBar().hide();
    }

    private void initialize() {
        networkHelper = NetworkHelper.getInstance(getApplicationContext());
        preferences = PreferencesManager.getInstance(this);
    }

    private void setListeners() {
        binding.signInBtn.setOnClickListener(this::onLoginClicked);
        binding.signUpTv.setOnClickListener(this::onSignUpClicked);
    }

    private void onSignUpClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void onLoginClicked(View view) {
        String email = binding.emailEdt.getText().toString().trim();
        String password = binding.passwordEdt.getText().toString().trim();

        signInProcess(email, password);
    }

    private void signInProcess(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, R.string.invalid_input_error, Toast.LENGTH_SHORT).show();
            return;
        }

        final User inputUser = new User(email, password);
        networkHelper.signinUser(inputUser, new ResultListener<User>() {
            @Override
            public void onResult(Result<User> result) {
                Log.d(TAG, "Result of signing user in server: " + result);
                Error error = (result != null) ? result.getError() : null;
                User resultUser = (result != null) ? result.getItem() : null;
                if ((result == null) || (error != null) || (resultUser == null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.user_signin_error);
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }

                inputUser.setId(resultUser.getId());
                inputUser.setFirstName(resultUser.getFirstName());
                inputUser.setLastName(resultUser.getLastName());
                inputUser.setProfile(resultUser.getProfile());
                inputUser.setSessionToken(resultUser.getSessionToken());
                insertToDatBase(inputUser);
            }
        });
    }

    private void insertToDatBase(User inputUser) {
        UserAsyncTask userInsertTask = new UserAsyncTask(getApplicationContext(), Action.INSERT, new ResultListener<User>() {
            @Override
            public void onResult(Result<User> result) {
                Error error = (result != null) ? result.getError() : null;
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.user_signin_error);
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }

                AppData appData = (AppData) getApplication();
                appData.setCurrentUser(inputUser);

                binding.emailEdt.setText("");
                binding.passwordEdt.setText("");

                login(inputUser);
            }
        });
        userInsertTask.execute(inputUser);
    }

    private void login(User user) {
        saveInPreferences(user);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveInPreferences(@NonNull User user) {
        preferences.put(PREF_KEY_USER_ID, user.getId());
        preferences.put(PREF_KEY_FIRST_NAME, user.getFirstName());
        preferences.put(PREF_KEY_LAST_NAME, user.getLastName());
        preferences.put(PREF_KEY_EMAIL, user.getEmail());
        preferences.put(PREF_KEY_PROFILE, user.getProfile());
        preferences.put(PREF_KEY_SESSION_TOKEN, user.getSessionToken());
        preferences.put(PREF_KEY_IS_LOGIN, true);
    }
}
