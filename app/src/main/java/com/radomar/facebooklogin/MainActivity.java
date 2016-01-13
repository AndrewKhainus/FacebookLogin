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
import com.radomar.facebooklogin.global.Constants;
import com.radomar.facebooklogin.interfaces.ActionListener;
import com.radomar.facebooklogin.interfaces.GetCallbackInterface;
import com.radomar.facebooklogin.interfaces.PublisherInterface;
import com.radomar.facebooklogin.interfaces.OnStartAddAndRemoveListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnStartAddAndRemoveListener,
                                                               PublisherInterface,
                                                               GetCallbackInterface {

    private FragmentManager mFragmentManager;
    private LoginFragment mLoginFragment;
    private RequestFragment mRequestFragment;

    private List<ActionListener> listeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        //TODO: here you can use fragmentId instead of tag
        FacebookFragment facebookFragment = (FacebookFragment) mFragmentManager.findFragmentByTag(Constants.FACEBOOK_FRAGMENT_TAG);

        if (facebookFragment == null) {
            facebookFragment = new FacebookFragment();
            mFragmentManager.beginTransaction().replace(R.id.flCont_AM, facebookFragment, Constants.FACEBOOK_FRAGMENT_TAG).commit();
        }
    }

    private void addTaskFragment() {
        mLoginFragment = (LoginFragment)mFragmentManager.findFragmentByTag(Constants.LOGIN_FRAGMENT_TAG);
        mRequestFragment = (RequestFragment)mFragmentManager.findFragmentByTag(Constants.REQUEST_FRAGMENT_TAG);

        if (mLoginFragment == null) {
            mLoginFragment = new LoginFragment();
            mFragmentManager.beginTransaction().add(R.id.flCont_AM, mLoginFragment, Constants.LOGIN_FRAGMENT_TAG).commit();
        }

        if (mRequestFragment == null) {
            mRequestFragment = new RequestFragment();
            mFragmentManager.beginTransaction().add(R.id.flCont_AM, mRequestFragment, Constants.REQUEST_FRAGMENT_TAG).commit();
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
    public void onStartAddListener(ActionListener listener) {
        addListener(listener);
    }

    @Override
    public void onStartRemoveListener(ActionListener listener) {
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

