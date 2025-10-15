package com.example.mailit.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mailit.R;
import com.example.mailit.beans.Email;
import com.example.mailit.data.async.EmailAsyncTask;
import com.example.mailit.databinding.ActivityEmailDetailBinding;
import com.example.mailit.network.NetworkHelper;
import com.example.mailit.utils.Action;
import com.example.mailit.utils.Constants;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class EmailDetailActivity extends AppCompatActivity {

    private ActivityEmailDetailBinding binding;
    private NetworkHelper networkHelper;
    private Email email;
    private int inboxPosition;
    private int sentPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initialize();
        setUpActionBar();
        getDataOrFinish();
        setData();
    }

    private void initialize() {
        networkHelper = NetworkHelper.getInstance(getApplicationContext());
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
    }

    private void getDataOrFinish() {
        Intent intent = getIntent();
        if (!intent.hasExtra(Constants.EMAIL)) {
            finish();
        }
        email = (Email) intent.getSerializableExtra(Constants.EMAIL);
        inboxPosition = intent.getIntExtra(Constants.POSITION_INBOX, -1);
        sentPosition = intent.getIntExtra(Constants.POSITION_SENT, -1);
    }

    private void setData() {
        String fullName = email.getSender();
        String receiver = email.getReceiver();
        String subject = email.getSubject();
        String content = email.getContent();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String date = dateFormat.format(email.getDate());
        String profile = email.getUserProfile();
        binding.fullNameTv.setText(fullName);
        binding.receiverTv.setText(receiver);
        binding.subjectTv.setText(subject);
        binding.contentTv.setText(content);
        binding.dateTv.setText(date);
        checkAndSetProfile(profile, fullName, binding.profileTv, binding.profileIv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
                deleteFromDataBase(email, inboxPosition, sentPosition);
            }
        });
    }

    private void deleteFromDataBase(Email email, int inboxPosition, int sentPosition) {
        EmailAsyncTask deleteTask = new EmailAsyncTask(getApplicationContext(), Action.DELETE, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Intent intent = new Intent();
                intent.putExtra(Constants.INBOX_POSITION, inboxPosition);
                intent.putExtra(Constants.SENT_POSITION, sentPosition);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        deleteTask.execute(email);
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
}
