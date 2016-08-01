package com.leadplatform.kfarmers.util.oauth;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import android.os.AsyncTask;
import android.util.Log;

public class OAuthRequestTokenTask extends AsyncTask<Void, Void, String>
{
    final String TAG = getClass().getName();

    private String callbackUrl;
    private OAuthConsumer consumer;
    private OAuthProvider provider;
    private OAuthRequestTokenListener listener;

    public OAuthRequestTokenTask(String callbackUrl, OAuthConsumer consumer, OAuthProvider provider, OAuthRequestTokenListener listener)
    {
        this.callbackUrl = callbackUrl;
        this.consumer = consumer;
        this.provider = provider;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... params)
    {
        String authUrl = null;
        try
        {
            if (callbackUrl == null)
                authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
            else
                authUrl = provider.retrieveRequestToken(consumer, callbackUrl);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error during OAUth retrieve request token", e);
        }

        return authUrl;
    }

    @Override
    protected void onPostExecute(String authUrl)
    {
        if (listener != null)
            listener.onSuccess(authUrl);
    }
}