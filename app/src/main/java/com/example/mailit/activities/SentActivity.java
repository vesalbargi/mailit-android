package com.example.mailit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mailit.R;
import com.example.mailit.adapters.EmailAdapter;
import com.example.mailit.beans.Email;
import com.example.mailit.beans.User;
import com.example.mailit.data.async.EmailAsyncTask;
import com.example.mailit.databinding.ActivitySentBinding;
import com.example.mailit.network.NetworkHelper;
import com.example.mailit.prefrences.PreferencesManager;
import com.example.mailit.utils.Action;
import com.example.mailit.utils.Constants;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;
import com.example.mailit.utils.Type;

import java.util.ArrayList;
import java.util.List;

public class SentActivity extends AppCompatActivity implements EmailAdapter.EmailAdapterCallback {

    private static final String TAG = "SentActivity";
    private ActivitySentBinding binding;
    private NetworkHelper networkHelper;
    private EmailAdapter adapter;
    private PreferencesManager preferences;
    private User currentUser;
    private List<Email> sentEmails;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySentBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initialize();
        setUpRecyclerView();
        setUpSwipeToDelete();
        loadSentEmails();
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

    private void setUpRecyclerView() {
        adapter = new EmailAdapter(this, new ArrayList<>(), this);
        binding.emailRv.setLayoutManager(new LinearLayoutManager(this));
        binding.emailRv.setAdapter(adapter);
    }

    private void setUpSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Email email = sentEmails.get(position);
                deleteFromServer(email);
                sentEmails.remove(position);
                adapter.notifyDataSetChanged();
                Toast.makeText(SentActivity.this, R.string.email_delete_success, Toast.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.emailRv);
    }

    private void deleteFromServer(Email email) {
        networkHelper.deleteEmail(email, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                deleteFromDataBase(email);
            }
        });
    }

    private void deleteFromDataBase(Email email) {
        EmailAsyncTask deleteTask = new EmailAsyncTask(getApplicationContext(), Action.DELETE, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
            }
        });
        deleteTask.execute(email);
    }

    private void loadSentEmails() {
        Email email = new Email(currentUser.getId());
        email.setType(Type.SENT);
        EmailAsyncTask getAllSentTask = new EmailAsyncTask(getApplicationContext(), Action.GET_ALL_SENT, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Log.d(TAG, "Result of loading sent emails from database: " + result);
                Error error = (result != null) ? result.getError() : null;
                List<Email> usersList = (result != null) ? result.getItems() : new ArrayList<Email>();
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.email_get_all_sent_error);
                    Toast.makeText(SentActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                sentEmails = usersList;
                adapter.emailsLoaded(sentEmails);
            }
        });
        getAllSentTask.execute(email);
    }

    private void setListeners() {
        binding.searchBtn.setOnClickListener(this::onSearchClicked);
    }

    private void onSearchClicked(View view) {
        String keyWord = binding.searchEdt.getText().toString().trim();
        if (TextUtils.isEmpty(keyWord)) {
            binding.searchEdt.setError(getString(R.string.invalid_input_error));
        } else {
            Intent intent = new Intent(SentActivity.this, SearchActivity.class);
            intent.putExtra(Constants.SEARCH_SENT, keyWord);
            startActivity(intent);
            binding.searchEdt.setText("");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SENT_DELETE) {
            if (resultCode == RESULT_OK) {
                int position = data.getIntExtra(Constants.SENT_POSITION, -1);
                adapter.emailDeleted(position);
                Toast.makeText(SentActivity.this, R.string.email_delete_success, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClicked(Email email, int position) {
        Intent intent = new Intent(SentActivity.this, EmailDetailActivity.class);
        intent.putExtra(Constants.EMAIL, email);
        intent.putExtra(Constants.POSITION_SENT, position);
        startActivityForResult(intent, Constants.SENT_DELETE);
    }
}
