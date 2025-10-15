package com.example.mailit.data.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.mailit.R;
import com.example.mailit.beans.Email;
import com.example.mailit.data.db.DbManager;
import com.example.mailit.utils.Action;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;

import java.util.List;

public class EmailAsyncTask extends AsyncTask<Email, Void, Result<Email>> {

    private final static String TAG = "EmailAsyncTask";
    private Context context;
    private Action action;
    private ResultListener<Email> resultListener;

    public EmailAsyncTask(Context context, Action action, ResultListener<Email> resultListener) {
        this.context = context;
        this.action = action;
        this.resultListener = resultListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Result<Email> doInBackground(Email... inputEmails) {
        DbManager db = DbManager.getInstance(context);
        Email inputEmail = (inputEmails.length > 0) ? inputEmails[0] : null;
        List<Email> emailsList = null;
        Error error = null;

        try {
            switch (action) {
                case GET_ALL_INBOX:
                    emailsList = db.emailDao().getAllInbox(inputEmail.getReceiver(), inputEmail.getType());
                    error = (emailsList == null) ? new Error(context.getString(R.string.email_get_all_inbox_error)) : null;
                    break;
                case GET_ALL_SENT:
                    emailsList = db.emailDao().getAllSent(inputEmail.getUserId(), inputEmail.getType());
                    error = (emailsList == null) ? new Error(context.getString(R.string.email_get_all_sent_error)) : null;
                    break;
                case GET_ALL_DRAFT:
                    emailsList = db.emailDao().getAllDraft(inputEmail.getUserId(), inputEmail.getType());
                    error = (emailsList == null) ? new Error(context.getString(R.string.email_get_all_draft_error)) : null;
                    break;
                case GET_ALL_INBOX_SEARCH:
                    emailsList = db.emailDao().getAllInboxSearch(inputEmail.getReceiver(), inputEmail.getType(), inputEmail.getKeyWord());
                    error = (emailsList == null) ? new Error(context.getString(R.string.email_get_all_search_error)) : null;
                    break;
                case GET_ALL_SENT_SEARCH:
                    emailsList = db.emailDao().getAllSentSearch(inputEmail.getUserId(), inputEmail.getType(), inputEmail.getKeyWord());
                    error = (emailsList == null) ? new Error(context.getString(R.string.email_get_all_search_error)) : null;
                    break;
                case GET_ALL_DRAFT_SEARCH:
                    emailsList = db.emailDao().getAllDraftSearch(inputEmail.getUserId(), inputEmail.getType(), inputEmail.getKeyWord());
                    error = (emailsList == null) ? new Error(context.getString(R.string.email_get_all_search_error)) : null;
                    break;
                case INSERT:
                    long iResult = db.emailDao().insert(inputEmail);
                    Log.d(TAG, "Insert result: " + iResult);
                    error = (iResult < 1) ? new Error(context.getString(R.string.email_insert_error)) : null;
                    break;
                case DELETE:
                    int dResult = db.emailDao().delete(inputEmail);
                    Log.d(TAG, "Delete result: " + dResult);
                    error = (dResult < 1) ? new Error(context.getString(R.string.email_delete_error)) : null;
                case UPDATE:
                    int uResult = db.emailDao().update(inputEmail);
                    Log.d(TAG, "Update result: " + uResult);
                    error = (uResult < 1) ? new Error(context.getString(R.string.email_update_error)) : null;
                    break;
                default:
                    Log.w(TAG, "Invalid database action: " + action);
                    error = new Error(context.getString(R.string.db_general_error));
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error = new Error(context.getString(R.string.db_general_error));
        }

        return new Result<Email>(inputEmail, emailsList, error);
    }

    @Override
    protected void onPostExecute(Result<Email> result) {
        super.onPostExecute(result);
        resultListener.onResult(result);
    }
}
