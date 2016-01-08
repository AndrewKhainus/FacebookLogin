package com.radomar.facebooklogin.interfaces;

import com.facebook.FacebookCallback;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;

/**
 * Created by Radomar on 06.01.2016
 */
public interface GetCallbackInterface {

    FacebookCallback<LoginResult> getFacebookCallback();

    GraphRequest.Callback getGraphRequestCallback();
}
