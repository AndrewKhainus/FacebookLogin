package com.radomar.facebooklogin.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.radomar.facebooklogin.interfaces.PublisherInterface;

/**
 * Created by Radomar on 06.01.2016
 */
public class RequestFragment extends Fragment implements GraphRequest.Callback {

    private GraphResponse mResponse;
    private PublisherInterface mPublisherInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        Log.d("sometag", "RequestFragment onAttach");
        mPublisherInterface = (PublisherInterface)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.d("sometag", "RequestFragment onCreate");
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPublisherInterface = null;
    }

    @Override
    public void onCompleted(GraphResponse response) {
//        Log.d("sometag", "RequestFragment onCompleted");
        mResponse = response;
        if (response != null) {
            mPublisherInterface.notifySubscribers(response);
        }
    }

    public GraphResponse getResponse() {
        return mResponse;
    }

}
