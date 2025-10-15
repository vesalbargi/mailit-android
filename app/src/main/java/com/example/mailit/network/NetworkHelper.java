package com.example.mailit.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mailit.R;
import com.example.mailit.beans.Email;
import com.example.mailit.beans.User;
import com.example.mailit.utils.Result;
import com.example.mailit.utils.ResultListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkHelper {

    private static final String TAG = "NetworkHelper";
    private static NetworkHelper instance = null;
    private Context context;
    private Gson gson = new Gson();
    private RequestQueue requestQueue;
    private String appId;
    private String apiKey;
    private String hostUrl;

    private NetworkHelper(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.appId = context.getString(R.string.appId);
        this.apiKey = context.getString(R.string.apiKey);
        this.hostUrl = context.getString(R.string.hostUrl);
    }

    public static NetworkHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkHelper(context);
        }
        return instance;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return ((net != null) && net.isConnected());
    }

    private void printVolleyErrorDetails(VolleyError error) {
        NetworkResponse errResponse = (error != null) ? error.networkResponse : null;
        int statusCode = 0;
        String data = "";
        if (errResponse != null) {
            statusCode = errResponse.statusCode;
            byte[] bytes = errResponse.data;
            data = (bytes != null) ? new String(bytes, StandardCharsets.UTF_8) : "";
        }

        Log.e(TAG, "Volley error with status code " + statusCode + " received with this message: " + data);
    }

    public void signupUser(final User user, final ResultListener<User> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<User>(null, null, error));
            return;
        }

        String url = hostUrl + "/users";
        String userJson = null;
        try {
            userJson = gson.toJson(user);
        } catch (Exception ex) {
            ex.printStackTrace();
            Error error = new Error(context.getString(R.string.network_json_error));
            listener.onResult(new Result<User>(null, null, error));
            return;
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<User>(null, null, error));
                    return;
                }

                User resultUser = null;
                try {
                    resultUser = gson.fromJson(response, new TypeToken<User>() {
                    }.getType());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<User>(null, null, error));
                    return;
                }

                listener.onResult(new Result<User>(resultUser, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<User>(null, null, err));
            }
        };

        final String jsonStr = userJson;
        StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Revocable-Session", "1");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(request);
    }

    public void signinUser(final User user, final ResultListener<User> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<User>(null, null, error));
            return;
        }

        String url = hostUrl + "/login?username=" + user.getEmail() + "&password=" + user.getPassword();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "User signin response: " + response);
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<User>(null, null, error));
                    return;
                }

                User resultUser = null;
                try {
                    resultUser = gson.fromJson(response, new TypeToken<User>() {
                    }.getType());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<User>(null, null, error));
                    return;
                }

                listener.onResult(new Result<User>(resultUser, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<User>(null, null, err));
            }
        };

        StringRequest request = new StringRequest(Request.Method.GET, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Revocable-Session", "1");
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void insertEmail(final Email email, final User currentUser, final ResultListener<Email> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<Email>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/email";
        String emailJson = null;
        try {
            emailJson = gson.toJson(email);
        } catch (Exception ex) {
            ex.printStackTrace();
            Error error = new Error(context.getString(R.string.network_json_error));
            listener.onResult(new Result<Email>(null, null, error));
            return;
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Email insert response: " + response);
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<Email>(null, null, error));
                    return;
                }

                Email resultEmail = null;
                try {
                    resultEmail = gson.fromJson(response, new TypeToken<Email>() {
                    }.getType());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<Email>(null, null, error));
                    return;
                }

                listener.onResult(new Result<Email>(resultEmail, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<Email>(null, null, err));
            }
        };

        final String jsonStr = emailJson;
        StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", currentUser.getSessionToken());
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(request);
    }

    public void updateEmail(final Email email, final User currentUser, final ResultListener<Email> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<Email>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/email/" + email.getId();
        String emailJson = null;
        try {
            emailJson = gson.toJson(email);
        } catch (Exception ex) {
            ex.printStackTrace();
            Error error = new Error(context.getString(R.string.network_json_error));
            listener.onResult(new Result<Email>(null, null, error));
            return;
        }

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Email update response: " + response);
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<Email>(null, null, error));
                    return;
                }

                Email resultStd = null;
                try {
                    resultStd = gson.fromJson(response, new TypeToken<Email>() {
                    }.getType());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<Email>(null, null, error));
                    return;
                }

                listener.onResult(new Result<Email>(resultStd, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<Email>(null, null, err));
            }
        };

        final String jsonStr = emailJson;
        StringRequest request = new StringRequest(Request.Method.PUT, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Session-Token", currentUser.getSessionToken());
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonStr.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(request);
    }

    public void deleteEmail(final Email email, final ResultListener<Email> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<Email>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/email/" + email.getId();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Email delete response: " + response);
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<Email>(null, null, error));
                    return;
                }

                Email resultEmail = null;
                try {
                    resultEmail = gson.fromJson(response, new TypeToken<Email>() {
                    }.getType());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<Email>(null, null, error));
                    return;
                }

                listener.onResult(new Result<Email>(resultEmail, null, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<Email>(null, null, err));
            }
        };

        StringRequest request = new StringRequest(Request.Method.DELETE, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Revocable-Session", "1");
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public void getEmails(final ResultListener<Email> listener) {
        if (!isNetworkConnected()) {
            Error error = new Error(context.getString(R.string.network_connection_error));
            listener.onResult(new Result<Email>(null, null, error));
            return;
        }

        String url = hostUrl + "/classes/email/";

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Email get all response: " + response);
                if (TextUtils.isEmpty(response)) {
                    Error error = new Error(context.getString(R.string.network_general_error));
                    listener.onResult(new Result<Email>(null, null, error));
                    return;
                }

                List<String> resultString = null;
                List<Email> resultEmails = new ArrayList<>();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    resultString = (List) new ArrayList<Object>();
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            resultString.add(jsonArray.get(i).toString());
                        }
                    }
                    for (int i = 0; i < resultString.size(); i++) {
                        resultEmails.add(gson.fromJson(resultString.get(i), new TypeToken<Email>() {
                        }.getType()));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Error error = new Error(context.getString(R.string.network_json_error));
                    listener.onResult(new Result<Email>(null, null, error));
                    return;
                }

                listener.onResult(new Result<Email>(null, resultEmails, null));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                printVolleyErrorDetails(error);
                Error err = new Error(context.getString(R.string.network_general_error));
                listener.onResult(new Result<Email>(null, null, err));
            }
        };

        StringRequest request = new StringRequest(Request.Method.GET, url, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Parse-Application-Id", appId);
                headers.put("X-Parse-REST-API-Key", apiKey);
                headers.put("X-Parse-Revocable-Session", "1");
                return headers;
            }
        };
        requestQueue.add(request);
    }
}
