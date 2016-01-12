package com.radomar.facebooklogin.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.radomar.facebooklogin.interfaces.PublisherInterface;

/**
 * Created by Radomar on 05.01.2016
 */
public class LoginFragment extends Fragment implements FacebookCallback<LoginResult> {

    private LoginResult mLoginResult;
    private PublisherInterface mPublisherInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        Log.d("sometag", "LoginFragment onAttach");
        mPublisherInterface = (PublisherInterface)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.d("sometag", "LoginFragment onCreate");
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPublisherInterface = null;
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
//        Log.d("sometag", "LoginFragment onSuccess");
        mLoginResult = loginResult;
        //TODO: may cause NPE
        mPublisherInterface.notifySubscribers(loginResult);
    }

    @Override
    public void onCancel() {
//        Log.d("sometag", "LoginFragment onCancel");
    }

    @Override
    public void onError(FacebookException error) {
//        Log.d("sometag", "LoginFragment onError");
    }

    public LoginResult getLoginResult() {
        return mLoginResult;
    }
}
