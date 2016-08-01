package com.leadplatform.kfarmers.util.oauth;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;


public class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, AccessToken>
{
    final String TAG = getClass().getName();

    private OAuthProvider provider;
    private OAuthConsumer consumer;
    private OAuthRetrieveAccessTokenListener listener;

    public RetrieveAccessTokenTask(OAuthConsumer consumer, OAuthProvider provider, OAuthRetrieveAccessTokenListener listener)
    {
        this.consumer = consumer;
        this.provider = provider;
        this.listener = listener;
    }

    @Override
    protected AccessToken doInBackground(Uri... params)
    {
        AccessToken accessToken = new AccessToken();
        final Uri uri = params[0];
        final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

        try
        {
            provider.retrieveAccessToken(consumer, oauth_verifier);
            accessToken.oAuthToken = consumer.getToken();
            accessToken.oAuthSecret = consumer.getTokenSecret();
            consumer.setTokenWithSecret(accessToken.oAuthToken, accessToken.oAuthSecret);
        }
        catch (Exception e)
        {
            Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
        }

        return accessToken;
    }

    @Override
    protected void onPostExecute(AccessToken accessToken)
    {
        if (listener != null)
            listener.onSuccess(accessToken);
    }
}