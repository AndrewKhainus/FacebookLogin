package com.radomar.facebooklogin.interfaces;

import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;

/**
 * Created by Radomar on 07.01.2016
 */
public interface PublisherInterface {

    void addListener(ActionListener listener);

    void removeListener(ActionListener listener);

    void notifySubscribers(GraphResponse response);

    void notifySubscribers(LoginResult loginResult);
}
