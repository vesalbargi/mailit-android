package com.example.mailit.data.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.mailit.R;
import com.example.mailit.beans.User;
import com.example.mailit.data.db.DbManager;
import com.example.mailit.utils.Action;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;

public class UserAsyncTask extends AsyncTask<User, Void, Result<User>> {

    private final static String TAG = "UserAsyncTask";
    private Context context;
    private Action action;
    private ResultListener<User> resultListener;

    public UserAsyncTask(Context context, Action action, ResultListener<User> resultListener) {
        this.context = context;
        this.action = action;
        this.resultListener = resultListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Result<User> doInBackground(User... inputUsers) {
        DbManager db = DbManager.getInstance(context);
        User inputUser = (inputUsers.length > 0) ? inputUsers[0] : null;
        Error error = null;

        try {
            switch (action) {
                case GET_ONE:
                    inputUser = db.userDao().getOne(inputUser.getId());
                    break;
                case INSERT:
                    long iResult = db.userDao().insert(inputUser);
                    Log.d(TAG, "Insert result: " + iResult);
                    error = (iResult < 1) ? new Error(context.getString(R.string.user_signup_error)) : null;
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

        return new Result<User>(inputUser, null, error);
    }

    @Override
    protected void onPostExecute(Result<User> result) {
        super.onPostExecute(result);
        resultListener.onResult(result);
    }
}
