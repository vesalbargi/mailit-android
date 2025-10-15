package com.example.mailit.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mailit.R;
import com.example.mailit.beans.Email;
import com.example.mailit.data.async.EmailAsyncTask;
import com.example.mailit.databinding.ItemEmailBinding;
import com.example.mailit.utils.Action;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.ViewHolder> {

    private Context context;
    private List<Email> emails;
    private EmailAdapterCallback callback;
    private LayoutInflater layoutInflater;

    public EmailAdapter(Context context, List<Email> emails, EmailAdapterCallback callback) {
        this.context = context;
        this.emails = emails;
        this.callback = callback;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEmailBinding binding = ItemEmailBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }

    public void emailDeleted(int position) {
        emails.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public void emailAdded(Email email) {
        emails.add(email);
        notifyDataSetChanged();
    }

    public void emailsLoaded(List<Email> loadedEmails) {
        emails = loadedEmails;
        notifyDataSetChanged();
    }

    public void itemRead(Email email, int position) {
        email.setRead(true);
        EmailAsyncTask updateTask = new EmailAsyncTask(context.getApplicationContext(), Action.UPDATE, new ResultListener<Email>() {
            @Override
            public void onResult(Result<Email> result) {
                Error error = (result != null) ? result.getError() : null;
                if ((result == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : context.getString(R.string.email_update_error);
                    Toast.makeText(context.getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                notifyItemChanged(position);
            }
        });
        updateTask.execute(email);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ItemEmailBinding binding;
        private Email email;
        private int position;


        public ViewHolder(@NonNull ItemEmailBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
            binding.mainLay.setOnClickListener(this::onItemClicked);
        }

        public void setData(int position) {
            this.position = position;
            email = emails.get(position);

            binding.fullNameTv.setText(email.getSender());
            binding.subjectTv.setText(email.getSubject());
            binding.contentTv.setText(email.getContent());
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String date = dateFormat.format(email.getDate());
            binding.dateTv.setText(date);
            checkAndSetProfile(email.getUserProfile(), email.getSender(), binding.profileTv, binding.profileIv);
            checkReadStatus(email);
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

        private void checkReadStatus(Email email) {
            if (!email.isRead()) {
                binding.fullNameTv.setTypeface(null, Typeface.BOLD);
                binding.subjectTv.setTypeface(null, Typeface.BOLD);
                binding.contentTv.setTypeface(null, Typeface.BOLD);
                binding.dateTv.setTypeface(null, Typeface.BOLD);
            } else {
                binding.fullNameTv.setTypeface(null, Typeface.NORMAL);
                binding.subjectTv.setTypeface(null, Typeface.NORMAL);
                binding.contentTv.setTypeface(null, Typeface.NORMAL);
                binding.dateTv.setTypeface(null, Typeface.NORMAL);
            }
        }

        private void onItemClicked(View view) {
            callback.onItemClicked(email, position);
        }
    }

    public interface EmailAdapterCallback {

        void onItemClicked(Email email, int position);
    }
}
