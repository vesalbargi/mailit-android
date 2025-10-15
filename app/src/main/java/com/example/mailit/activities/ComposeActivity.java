package com.example.mailit.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mailit.R;
import com.example.mailit.beans.Email;
import com.example.mailit.beans.User;
import com.example.mailit.data.async.EmailAsyncTask;
import com.example.mailit.databinding.ActivityComposeBinding;
import com.example.mailit.network.NetworkHelper;
import com.example.mailit.prefrences.PreferencesManager;
import com.example.mailit.utils.Action;
import com.example.mailit.utils.Constants;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;
import com.example.mailit.utils.Type;

import java.util.Date;

public class ComposeActivity extends AppCompatActivity {

    private static final String TAG = "ComposeActivity";
    private ActivityComposeBinding binding;
    private NetworkHelper networkHelper;
    private PreferencesManager preferences;
    private User currentUser;
    private Email email;
    private int position;
    private boolean draftOpened = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComposeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initialize();
        setUpActionBar();
        getData();
        setListeners();
    }

    private void initialize() {
        networkHelper = NetworkHelper.getInstance(getApplicationContext());
        preferences = PreferencesManager.getInstance(this);
        currentUser = saveFromPreferences();
    }

    private User saveFromPreferences() {
        String id = preferences.get(PreferencesManager.PREF_KEY_USER_ID, "");
        String firstName = preferences.get(PreferencesManager.PREF_KEY_FIRST_NAME, "");
        String lastName = preferences.get(PreferencesManager.PREF_KEY_LAST_NAME, "");
        String email = preferences.get(PreferencesManager.PREF_KEY_EMAIL, "");
        String password = preferences.get(PreferencesManager.PREF_KEY_PASSWORD, "");
        String profile = preferences.get(PreferencesManager.PREF_KEY_PROFILE, "");
        User user = new User(id, firstName, lastName, email, password, profile);
        return user;
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.EMAIL)) {
            email = (Email) intent.getSerializableExtra(Constants.EMAIL);
            position = intent.getIntExtra(Constants.POSITION, -1);
            draftOpened = true;
            setData();
        }
    }

    private void setData() {
        String receiver = email.getReceiver();
        String subject = email.getSubject();
        String content = email.getContent();
        binding.emailToEdt.setText(receiver);
        binding.subjectEdt.setText(subject);
        binding.contentEdt.setText(content);
    }

    private void setListeners() {
        binding.sendBtn.setOnClickListener(this::onSendClicked);
    }

    private void onSendClicked(View view) {
        readSentEmail();
    }

    private void readSentEmail() {
        String emailTo = binding.emailToEdt.getText().toString().trim();
        String subject = binding.subjectEdt.getText().toString().trim();
        String content = binding.contentEdt.getText().toString().trim();

        if (TextUtils.isEmpty(emailTo) || TextUtils.isEmpty(subject) || TextUtils.isEmpty(content)) {
            Toast.makeText(ComposeActivity.this, R.string.invalid_input_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (draftOpened) {
            updateInServer(emailTo, subject, content, Type.SENT);
        } else {
            insertToServer(emailTo, subject, content, Type.SENT);
        }
    }

    private void updateInServer(String emailTo, String subject, String content, String type) {
        final Email updateEmail = new Email(email.getId(), emailTo, subject, content, currentUser.getId(), type);
        networkHelper.updateEmail(updateEmail, currentUser, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Log.d(TAG, "Result of updating email in server: " + result);
                Error error = (result != null) ? result.getError() : null;
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.email_update_error);
                    Toast.makeText(ComposeActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                updateEmail.setSender(currentUser.getFirstName() + " " + currentUser.getLastName());
                updateEmail.setDate(new Date());
                updateEmail.setUserProfile(currentUser.getProfile());
                updateInDataBase(updateEmail);
            }
        });
    }

    private void updateInDataBase(Email updateEmail) {
        EmailAsyncTask updateTask = new EmailAsyncTask(getApplicationContext(), Action.UPDATE, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Error error = (result != null) ? result.getError() : null;
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.email_update_error);
                    Toast.makeText(ComposeActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(ComposeActivity.this, R.string.email_update_success, Toast.LENGTH_LONG).show();
                finish();
            }
        });
        updateTask.execute(updateEmail);
    }

    private void insertToServer(String emailTo, String subject, String content, String type) {
        final Email newEmail = new Email(emailTo, subject, content, currentUser.getId(), type);
        networkHelper.insertEmail(newEmail, currentUser, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Log.d(TAG, "Result of inserting email in server: " + result);
                Error error = (result != null) ? result.getError() : null;
                Email resultEmail = (result != null) ? result.getItem() : null;
                if ((result == null) || (resultEmail == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.email_insert_error);
                    Toast.makeText(ComposeActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }

                newEmail.setId(resultEmail.getId());
                newEmail.setSender(currentUser.getFirstName() + " " + currentUser.getLastName());
                newEmail.setDate(new Date());
                newEmail.setUserProfile(currentUser.getProfile());
                insertToDataBase(newEmail);
            }
        });
    }

    private void insertToDataBase(Email newEmail) {
        EmailAsyncTask emailInsertTask = new EmailAsyncTask(getApplicationContext(), Action.INSERT, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> response) {
                Error error = (response != null) ? response.getError() : null;
                if ((response == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.email_insert_error);
                    Toast.makeText(ComposeActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                if (newEmail.getType().equals(Type.DRAFT)) {
                    Toast.makeText(ComposeActivity.this, R.string.draft_insert_success, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ComposeActivity.this, R.string.email_insert_success, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
        emailInsertTask.execute(newEmail);
    }

    @Override
    public void onBackPressed() {
        if (!isEmpty()) {
            showDraftDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem delete = menu.findItem(R.id.action_delete);
        if (draftOpened) {
            delete.setVisible(true);
        } else {
            delete.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!isEmpty()) {
                    showDraftDialog();
                } else {
                    finish();
                }
                return true;
            case R.id.action_delete:
                deleteFromServer(email);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteFromServer(Email email) {
        networkHelper.deleteEmail(email, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                deleteDraft(email, position);
            }
        });
    }

    private void deleteDraft(Email email, int position) {
        EmailAsyncTask deleteTask = new EmailAsyncTask(getApplicationContext(), Action.DELETE, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Intent intent = new Intent();
                intent.putExtra(Constants.DRAFT_POSITION, position);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        deleteTask.execute(email);
    }

    private boolean isEmpty() {
        String emailTo = binding.emailToEdt.getText().toString().trim();
        String subject = binding.subjectEdt.getText().toString().trim();
        String content = binding.contentEdt.getText().toString().trim();

        if (TextUtils.isEmpty(emailTo) && TextUtils.isEmpty(subject) && TextUtils.isEmpty(content)) {
            return true;
        } else {
            return false;
        }
    }

    private void showDraftDialog() {
        new AlertDialog.Builder(ComposeActivity.this)
                .setTitle(R.string.unsaved_changes)
                .setMessage(R.string.discard_or_draft)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        readDraftEmail();
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                        dialog.dismiss();
                    }
                }).show();
    }

    private void readDraftEmail() {
        String emailTo = binding.emailToEdt.getText().toString().trim();
        String subject = binding.subjectEdt.getText().toString().trim();
        String content = binding.contentEdt.getText().toString().trim();
        insertToServer(emailTo, subject, content, Type.DRAFT);
    }
}
