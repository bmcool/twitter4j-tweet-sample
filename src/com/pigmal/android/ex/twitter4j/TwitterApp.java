package com.pigmal.android.ex.twitter4j;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TwitterApp extends Activity {
	private static final String TAG = "T4JSample";

	private Button buttonLogin;
	private static Twitter twitter;
	private static RequestToken requestToken;
	private static SharedPreferences mSharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSharedPreferences = getSharedPreferences(Const.PREFERENCE_NAME, MODE_PRIVATE);
		buttonLogin = (Button) findViewById(R.id.twitterLogin);
		buttonLogin.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isConnected()) {
					ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
					configurationBuilder.setOAuthConsumerKey(Const.CONSUMER_KEY);
					configurationBuilder.setOAuthConsumerSecret(Const.CONSUMER_SECRET);
					Configuration configuration = configurationBuilder.build();
					twitter = new TwitterFactory(configuration).getInstance();
					try {
						requestToken = twitter.getOAuthRequestToken(Const.CALLBACK_URL);
						Toast.makeText(TwitterApp.this, "Please authorize this app!", Toast.LENGTH_LONG).show();
						TwitterApp.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
					} catch (TwitterException e) {
						e.printStackTrace();
					}
				} else {
					SharedPreferences.Editor editor = mSharedPreferences.edit();
					editor.remove(Const.PREF_KEY_TOKEN);
					editor.remove(Const.PREF_KEY_SECRET);
					editor.commit();
					buttonLogin.setText(R.string.label_connect);
				}
			}
		});
		
		Uri uri = getIntent().getData();
		if (uri != null && uri.toString().startsWith(Const.CALLBACK_URL)) {
			String verifier = uri.getQueryParameter(Const.IEXTRA_OAUTH_VERIFIER);
			try { 
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier); 
				Editor e = mSharedPreferences.edit();
				e.putString(Const.PREF_KEY_TOKEN, accessToken.getToken()); 
				e.putString(Const.PREF_KEY_SECRET, accessToken.getTokenSecret()); 
				e.commit();
			} catch (Exception e) { 
				Log.e(TAG, e.getMessage()); 
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show(); 
			}
		}		
	}

	protected void onResume() {
		super.onResume();
		
		if (isConnected()) {
			String oauthAccessToken = mSharedPreferences.getString(Const.PREF_KEY_TOKEN, "");
			String oAuthAccessTokenSecret = mSharedPreferences.getString(Const.PREF_KEY_SECRET, "");

			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
			Configuration conf = confbuilder
					.setOAuthConsumerKey(Const.CONSUMER_KEY)
					.setOAuthConsumerSecret(Const.CONSUMER_SECRET)
					.setOAuthAccessToken(oauthAccessToken)
					.setOAuthAccessTokenSecret(oAuthAccessTokenSecret)
					.build();
			twitter = new TwitterFactory(conf).getInstance();
			
			try {
				// BE CAREFUL! the repeat message will be ignore.
				twitter.updateStatus("This is new tweet with random number = " + Math.random());
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			buttonLogin.setText(R.string.label_disconnect);
		} else {
			Toast.makeText(TwitterApp.this, "You didn't login", Toast.LENGTH_LONG).show();
		}
	}

	private boolean isConnected() {
		return mSharedPreferences.getString(Const.PREF_KEY_TOKEN, null) != null;
	}
}
