package com.radomar.facebooklogin;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.FacebookCallback;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.radomar.facebooklogin.fragments.FacebookFragment;
import com.radomar.facebooklogin.fragments.LoginFragment;
import com.radomar.facebooklogin.fragments.RequestFragment;
import com.radomar.facebooklogin.interfaces.ActionListener;
import com.radomar.facebooklogin.interfaces.GetCallbackInterface;
import com.radomar.facebooklogin.interfaces.StartAddAndRemoveListener;
import com.radomar.facebooklogin.interfaces.PublisherInterface;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements StartAddAndRemoveListener,
                                                               PublisherInterface,
        GetCallbackInterface {

    public static final String FACEBOOK_FRAGMENT_TAG = "facebook_fragment";
    public static final String LOGIN_FRAGMENT_TAG = "login_fragment_tag";
    public static final String REQUEST_FRAGMENT_TAG = "request_fragment_tag";

    private FragmentManager mFragmentManager;

    private FacebookFragment mFacebookFragment;
    private LoginFragment mLoginFragment;
    private RequestFragment mRequestFragment;

    private List<ActionListener> listeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("sometag", "MainActivity onCreate");

        mFragmentManager = getSupportFragmentManager();

        startFacebookFragment();
        addTaskFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("sometag", "MainActivity onResume");
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("sometag", "MainActivity onPause");
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    private void startFacebookFragment() {
        mFacebookFragment = (FacebookFragment)mFragmentManager.findFragmentByTag(FACEBOOK_FRAGMENT_TAG);

        if (mFacebookFragment == null) {
            mFacebookFragment = new FacebookFragment();
            mFragmentManager.beginTransaction().replace(R.id.flCont_AM, mFacebookFragment, FACEBOOK_FRAGMENT_TAG).commit();
        }
    }

    private void addTaskFragment() {
        mLoginFragment = (LoginFragment)mFragmentManager.findFragmentByTag(LOGIN_FRAGMENT_TAG);
        mRequestFragment = (RequestFragment)mFragmentManager.findFragmentByTag(REQUEST_FRAGMENT_TAG);

        if (mLoginFragment == null) {
            mLoginFragment = new LoginFragment();
            mFragmentManager.beginTransaction().add(R.id.flCont_AM, mLoginFragment, LOGIN_FRAGMENT_TAG).commit();
        }

        if (mRequestFragment == null) {
            mRequestFragment = new RequestFragment();
            mFragmentManager.beginTransaction().add(R.id.flCont_AM, mRequestFragment, REQUEST_FRAGMENT_TAG).commit();
        }


    }


    @Override
    public void addListener(ActionListener listener) {
        listeners.add(listener);
        if (mLoginFragment.getLoginResult() != null) {
            notifySubscribers(mLoginFragment.getLoginResult());
        }

        if (mRequestFragment.getResponse() != null) {
            notifySubscribers(mRequestFragment.getResponse());
        }
    }

    @Override
    public void removeListener(ActionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void notifySubscribers(GraphResponse response) {
        for (ActionListener actionListener: listeners) {
            actionListener.doAction(response);
        }
    }

    @Override
    public void notifySubscribers(LoginResult loginResult) {
        for (ActionListener actionListener: listeners) {
            actionListener.doAction(loginResult);
        }
    }

    @Override
    public void StartAddListener(ActionListener listener) {
        addListener(listener);
    }

    @Override
    public void StartRemoveListener(ActionListener listener) {
        removeListener(listener);
    }


    @Override
    public FacebookCallback<LoginResult> getFacebookCallback() {
        return mLoginFragment;
    }

    @Override
    public GraphRequest.Callback getGraphRequestCallback() {
        return mRequestFragment;
    }
}

