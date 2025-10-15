package com.example.mailit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mailit.R;
import com.example.mailit.adapters.EmailAdapter;
import com.example.mailit.beans.Email;
import com.example.mailit.beans.User;
import com.example.mailit.data.async.EmailAsyncTask;
import com.example.mailit.databinding.ActivitySearchBinding;
import com.example.mailit.network.NetworkHelper;
import com.example.mailit.prefrences.PreferencesManager;
import com.example.mailit.utils.Action;
import com.example.mailit.utils.Constants;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;
import com.example.mailit.utils.Type;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements EmailAdapter.EmailAdapterCallback {

    private ActivitySearchBinding binding;
    private NetworkHelper networkHelper;
    private EmailAdapter adapter;
    private PreferencesManager preferences;
    private User currentUser;
    private List<Email> searchResult;
    private String inboxKeyWord;
    private String sentKeyWord;
    private String draftKeyWord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initialize();
        setUpActionBar();
        setUpRecyclerView();
        setUpSwipeToDelete();
        getData();
        pickType();
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
                Email email = searchResult.get(position);
                deleteFromServer(email);
                searchResult.remove(position);
                adapter.notifyDataSetChanged();
                Toast.makeText(SearchActivity.this, R.string.email_delete_success, Toast.LENGTH_SHORT).show();
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

    private void getData() {
        Intent intent = getIntent();
        inboxKeyWord = intent.getStringExtra(Constants.SEARCH_INBOX);
        sentKeyWord = intent.getStringExtra(Constants.SEARCH_SENT);
        draftKeyWord = intent.getStringExtra(Constants.SEARCH_DRAFT);
    }

    private void pickType() {
        if (inboxKeyWord != null) {
            inboxSearch();
        } else if (sentKeyWord != null) {
            sentSearch();
        } else if (draftKeyWord != null) {
            draftSearch();
        }
    }

    private void inboxSearch() {
        Email email = new Email(currentUser.getEmail());
        email.setType(Type.SENT);
        email.setKeyWord(inboxKeyWord);
        EmailAsyncTask inboxSearchTask = new EmailAsyncTask(getApplicationContext(), Action.GET_ALL_INBOX_SEARCH, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Error error = (result != null) ? result.getError() : null;
                List<Email> usersList = (result != null) ? result.getItems() : new ArrayList<Email>();
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.email_get_all_search_error);
                    Toast.makeText(SearchActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                searchResult = usersList;
                adapter.emailsLoaded(searchResult);
            }
        });
        inboxSearchTask.execute(email);
    }

    private void sentSearch() {
        Email email = new Email(currentUser.getId());
        email.setType(Type.SENT);
        email.setKeyWord(sentKeyWord);
        EmailAsyncTask sentSearchTask = new EmailAsyncTask(getApplicationContext(), Action.GET_ALL_SENT_SEARCH, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Error error = (result != null) ? result.getError() : null;
                List<Email> usersList = (result != null) ? result.getItems() : new ArrayList<Email>();
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.email_get_all_search_error);
                    Toast.makeText(SearchActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                searchResult = usersList;
                adapter.emailsLoaded(searchResult);
            }
        });
        sentSearchTask.execute(email);
    }

    private void draftSearch() {
        Email email = new Email(currentUser.getId());
        email.setType(Type.DRAFT);
        email.setKeyWord(draftKeyWord);
        EmailAsyncTask draftSearchTask = new EmailAsyncTask(getApplicationContext(), Action.GET_ALL_DRAFT_SEARCH, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Error error = (result != null) ? result.getError() : null;
                List<Email> usersList = (result != null) ? result.getItems() : new ArrayList<Email>();
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.email_get_all_search_error);
                    Toast.makeText(SearchActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                searchResult = usersList;
                adapter.emailsLoaded(searchResult);
            }
        });
        draftSearchTask.execute(email);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.DRAFT_DELETE) {
            if (resultCode == RESULT_OK) {
                int position = data.getIntExtra(Constants.DRAFT_POSITION, -1);
                adapter.emailDeleted(position);
                Toast.makeText(SearchActivity.this, R.string.draft_delete_success, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.INBOX_DELETE) {
            if (resultCode == RESULT_OK) {
                int position = data.getIntExtra(Constants.INBOX_POSITION, -1);
                adapter.emailDeleted(position);
                Toast.makeText(SearchActivity.this, R.string.email_delete_success, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClicked(Email email, int position) {
        if (email.getType().equals(Type.DRAFT)) {
            Intent draftIntent = new Intent(SearchActivity.this, ComposeActivity.class);
            draftIntent.putExtra(Constants.EMAIL, email);
            draftIntent.putExtra(Constants.POSITION, position);
            startActivityForResult(draftIntent, Constants.DRAFT_DELETE);
        } else {
            Intent sentIntent = new Intent(SearchActivity.this, EmailDetailActivity.class);
            sentIntent.putExtra(Constants.EMAIL, email);
            sentIntent.putExtra(Constants.POSITION_INBOX, position);
            startActivityForResult(sentIntent, Constants.INBOX_DELETE);
        }
    }
}
