package com.csahmad.moodcloud;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class FollowRequestActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutMananger;
    private FollowRequestController followRequestController = new FollowRequestController();
    private int loadCount = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    private int firstVisibleItems, visibleItemCount, totalItemCount;
    ArrayList<FollowRequest> mDataset;
    //mwschafe made ProfileController private

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_request);
        mRecyclerView = (RecyclerView) findViewById(R.id.followerList);

        mLayoutMananger = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutMananger);

        try{
            mDataset = followRequestController.getFollowRequests(
                    LocalData.getSignedInProfile(getApplicationContext()),0
            );
            mAdapter = new FollowRequestActivity.MyAdapter(mDataset);
            mRecyclerView.setAdapter(mAdapter);
        } catch (TimeoutException e){
            System.err.println("TimeoutException: " + e.getMessage());
        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = mLayoutMananger.getChildCount();
                totalItemCount = mLayoutMananger.getItemCount();
                firstVisibleItems = mLayoutMananger.findFirstVisibleItemPosition();
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <=
                        (firstVisibleItems + visibleThreshold)) {
                    loadCount = loadCount + ElasticSearchController.getResultSize();
                    try {
                        ArrayList<FollowRequest> newDS = followRequestController.getFollowRequests(
                                LocalData.getSignedInProfile(getApplicationContext()),loadCount);
                        mDataset.addAll(newDS);
                    } catch (TimeoutException e) {}
                    mAdapter.notifyDataSetChanged();
                    loading = true;
                }
            }
        });

        ImageButton imageButton = (ImageButton) findViewById(R.id.backButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                finish();
            }}
        );


    }

    /**
     * MyAdapter controls the list of profiles the signed in user is following by extending RecyclerView <br>
     *     http://www.androidhive.info/2016/01/android-working-with-recycler-view/ <br>
     *         2017-03-7
     * @author Taylor
     */
    public class MyAdapter extends RecyclerView.Adapter<FollowRequestActivity.MyAdapter.ViewHolder> {
        private ArrayList<FollowRequest> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mNameView;
            public Button accept;
            public Button decline;
            public TextView result;
            public ViewHolder(View v) {
                super(v);
                accept = (Button) v.findViewById(R.id.accept);
                decline = (Button) v.findViewById(R.id.decline);
                mNameView = (TextView) v.findViewById(R.id.followerName);
                result = (TextView) v.findViewById(R.id.result);
            }
        }

        public MyAdapter(ArrayList<FollowRequest> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public FollowRequestActivity.MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.follow_request_item, parent, false);

//            FollowingActivity.MyAdapter.ViewHolder vh = new FollowingActivity.MyAdapter.ViewHolder(v);
//            return vh;
//            mwschafe fixing redudant variable from code above to code below
            return new FollowRequestActivity.MyAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final FollowRequestActivity.MyAdapter.ViewHolder holder, int position) {
            final FollowRequest followRequest= mDataset.get(position);
            final Profile profile = followRequest.getFollower();
            holder.mNameView.setText(profile.getName());
            holder.accept.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Follow follow = new Follow(profile, LocalData.getSignedInProfile(getApplicationContext()));
                    FollowController followController = new FollowController();
                    followController.addOrUpdateFollows(follow);
                    followRequestController.deleteFollowRequests(followRequest);
                    //holder.accept.setVisibility(View.GONE);
                    //holder.decline.setVisibility(View.GONE);
                    //holder.result.setText("Request Accepted");
                    mDataset.clear();
                    try{
                        mDataset = followRequestController.getFollowRequests(
                                LocalData.getSignedInProfile(getApplicationContext()),0
                        );}catch (TimeoutException e){
                        System.err.println("TimeoutException: " + e.getMessage());
                    }
                    notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Request Accepted",
                            Toast.LENGTH_LONG).show();
                }
            });
            holder.decline.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    followRequestController.deleteFollowRequests(followRequest);
                    //holder.accept.setVisibility(View.GONE);
                    //holder.decline.setVisibility(View.GONE);
                    //holder.result.setText("Request Declined");
                    mDataset.clear();
                    try{
                    mDataset = followRequestController.getFollowRequests(
                            LocalData.getSignedInProfile(getApplicationContext()),0
                    );}catch (TimeoutException e){
                        System.err.println("TimeoutException: " + e.getMessage());
                    }
                    notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Request Declined",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }


}

