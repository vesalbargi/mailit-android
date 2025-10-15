package com.example.mailit.activities;

import static com.example.mailit.prefrences.PreferencesManager.PREF_KEY_IS_LOGIN;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mailit.R;
import com.example.mailit.adapters.EmailAdapter;
import com.example.mailit.beans.Email;
import com.example.mailit.beans.User;
import com.example.mailit.data.async.EmailAsyncTask;
import com.example.mailit.databinding.ActivityMainBinding;
import com.example.mailit.network.NetworkHelper;
import com.example.mailit.prefrences.PreferencesManager;
import com.example.mailit.utils.Action;
import com.example.mailit.utils.Constants;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;
import com.example.mailit.utils.Type;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener, EmailAdapter.EmailAdapterCallback {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NetworkHelper networkHelper;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private EmailAdapter adapter;
    private PreferencesManager preferences;
    private User currentUser;
    private List<Email> inboxEmails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initialize();
        setDrawerMenu();
        setDrawerHeader();
        setNavigationViewListener();
        setUpRecyclerView();
        setUpSwipeToDelete();
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

    private void setDrawerMenu() {
        drawerLayout = binding.myDrawerLayout;
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setDrawerHeader() {
        View headerView = binding.navView.getHeaderView(0);

        String firstName = currentUser.getFirstName();
        String lastName = currentUser.getLastName();
        TextView navName = (TextView) headerView.findViewById(R.id.name_tv);
        navName.setText(firstName + " " + lastName);

        String email = currentUser.getEmail();
        TextView navEmail = (TextView) headerView.findViewById(R.id.email_tv);
        navEmail.setText(email);

        String profile = currentUser.getProfile();
        ImageView navProfileIv = (ImageView) headerView.findViewById(R.id.profile_iv);
        TextView navProfileTv = (TextView) headerView.findViewById(R.id.profile_tv);
        checkAndSetProfile(profile, firstName, navProfileTv, navProfileIv);
    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
                Email email = inboxEmails.get(position);
                deleteFromServer(email);
                inboxEmails.remove(position);
                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, R.string.email_delete_success, Toast.LENGTH_SHORT).show();
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

    private void loadInboxEmails() {
        Email email = new Email(currentUser.getEmail());
        email.setType(Type.SENT);
        EmailAsyncTask getAllInboxTask = new EmailAsyncTask(getApplicationContext(), Action.GET_ALL_INBOX, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Log.d(TAG, "Result of loading inbox emails from database: " + result);
                Error error = (result != null) ? result.getError() : null;
                List<Email> usersList = (result != null) ? result.getItems() : new ArrayList<Email>();
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.email_get_all_inbox_error);
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                inboxEmails = usersList;
                adapter.emailsLoaded(inboxEmails);
            }
        });
        getAllInboxTask.execute(email);
    }

    private void setListeners() {
        binding.swipeRefresh.setOnRefreshListener(this);
        binding.composeFab.setOnClickListener(this);
        binding.searchBtn.setOnClickListener(this);
    }

    public boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
    }

    private void checkAndSetProfile(String profile, String name, TextView profileTv, ImageView profileIv) {
        if (!isValidURL(profile)) {
            int color = Integer.parseInt(profile);
            profileTv.setBackgroundTintList(ColorStateList.valueOf(color));
            String firstLetter = name.substring(0, 1);
            profileTv.setText(firstLetter.toUpperCase());
        } else {
            profileTv.setVisibility(View.INVISIBLE);
            profileIv.setVisibility(View.VISIBLE);
            Picasso.get().load(profile).into(profileIv);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_sent:
                goToSentActivity();
                break;
            case R.id.nav_draft:
                goToDraftActivity();
                break;
            case R.id.nav_logout:
                logoutUser();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void goToSentActivity() {
        Intent intent = new Intent(MainActivity.this, SentActivity.class);
        startActivity(intent);
    }

    private void goToDraftActivity() {
        Intent intent = new Intent(MainActivity.this, DraftActivity.class);
        startActivity(intent);
    }

    private void logoutUser() {
        preferences.put(PREF_KEY_IS_LOGIN, false);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public void onRefresh() {
        loadInboxEmails();
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.compose_fab:
                goToComposeActivity();
                break;
            case R.id.search_btn:
                onSearchClicked();
                break;
        }
    }

    private void onSearchClicked() {
        String keyWord = binding.searchEdt.getText().toString().trim();
        if (TextUtils.isEmpty(keyWord)) {
            binding.searchEdt.setError(getString(R.string.invalid_input_error));
        } else {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra(Constants.SEARCH_INBOX, keyWord);
            startActivity(intent);
            binding.searchEdt.setText("");
        }
    }

    private void goToComposeActivity() {
        Intent intent = new Intent(MainActivity.this, ComposeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.INBOX_DELETE) {
            if (resultCode == RESULT_OK) {
                int position = data.getIntExtra(Constants.INBOX_POSITION, -1);
                adapter.emailDeleted(position);
                Toast.makeText(MainActivity.this, R.string.email_delete_success, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClicked(Email email, int position) {
        updateReadStatus(email);
        adapter.itemRead(email, position);
        Intent intent = new Intent(MainActivity.this, EmailDetailActivity.class);
        intent.putExtra(Constants.EMAIL, email);
        intent.putExtra(Constants.POSITION_INBOX, position);
        startActivityForResult(intent, Constants.INBOX_DELETE);
    }

    private void updateReadStatus(Email email) {
        email.setRead(true);
        networkHelper.updateEmail(email, currentUser, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Error error = (result != null) ? result.getError() : null;
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.email_update_error);
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }
}