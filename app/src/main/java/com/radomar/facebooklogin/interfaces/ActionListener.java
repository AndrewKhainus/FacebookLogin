package com.radomar.facebooklogin.interfaces;

import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;

public interface ActionListener {
    void doAction(GraphResponse response);

    void doAction(LoginResult loginResult);
}