package com.csahmad.moodcloud;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/** The activity for viewing the {@link Profile} of a user and the mood history of that user.
 * @author Taylor
 */
public class ViewProfileActivity extends AppCompatActivity {

    private static final int GET_POST_REQUEST = 0;

    private int position;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutMananger;
    private static Profile profile;
    private ProfileController profileController = new ProfileController();
    private PostController postController = new PostController();
    private int loadCount = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    private int firstVisibleItems, visibleItemCount, totalItemCount;
    ArrayList<Post> mDataset;
    Post deleted;

    @Override
    protected void onStart() {

        super.onStart();
        setContentView(R.layout.activity_view_profile);
        mRecyclerView = (RecyclerView) findViewById(R.id.profilePostList);
        Intent intent = getIntent();
        String id = intent.getStringExtra("ID");

        if (id == null) id = LocalData.getSignedInProfile(this).getId();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            if (!id.equals(LocalData.getSignedInProfile(this).getId())) {

                FloatingActionButton pinButton =
                        (FloatingActionButton) findViewById(R.id.pinProfileButton);

                pinButton.setVisibility(View.VISIBLE);
            }
        }

        try {
            if(id.equals(LocalData.getSignedInProfile(getApplicationContext()).getId())){
                profile = LocalData.getSignedInProfile(getApplicationContext());
            } else {
                profile = profileController.getProfileFromID(id);
            }
            if (profile.getId() == null)
                throw new RuntimeException("Oh noes ID is null");

        } catch (TimeoutException e){}
        mLayoutMananger = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutMananger);
        TextView nameText = (TextView) findViewById(R.id.profileName);
        nameText.setText("Name: " + profile.getName());
        try {
            if (ConnectionManager.haveConnection(getApplicationContext())) {
                mDataset = postController.getPosts(profile, null, 0);
            } else {
                if (LocalData.getSignedInProfile(getApplicationContext()).equals(profile)) {
                    mDataset = LocalData.getUserPosts(getApplicationContext());
                } else {
                    finish();
                }
            }
            mAdapter = new ViewProfileActivity.MyAdapter(mDataset);
            mRecyclerView.setAdapter(mAdapter);
        } catch (TimeoutException e) {
            e.printStackTrace();
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
                        (firstVisibleItems + visibleThreshold) &&
                        ConnectionManager.haveConnection(getApplicationContext())) {
                    try {
                        loadCount = loadCount + ElasticSearchController.getResultSize();
                        ArrayList<Post> newDS = postController.getPosts(profile, null, loadCount);
                        mDataset.addAll(newDS);
                    } catch (TimeoutException e) {}
                    mAdapter.notifyDataSetChanged();
                    loading = true;
                }
            }
        });

        final Button editButton = (Button) findViewById(R.id.button4);
        if (LocalData.getSignedInProfile(getApplicationContext()).equals(profile)) {
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    Context context = view.getContext();
                    Intent intent = new Intent(context, EditProfileActivity.class);
                    startActivity(intent);
                }}
            );
        } else {
            editButton.setVisibility(View.INVISIBLE);
        }

        final Button followeditbutton = (Button) findViewById(R.id.followeditbutton);
        if (LocalData.getSignedInProfile(getApplicationContext()).equals(profile)) {
            if (ConnectionManager.haveConnection(getApplicationContext())){
                FollowRequestController followRequestController = new FollowRequestController();
                try {
                    Double count = followRequestController.getFollowRequestCount(profile);

                    followeditbutton.setText("Follow Requests");

                    if (count != null){

                        long longCount = count.longValue();

                        if (longCount == 0l) {
                            followeditbutton.setText("No Requests");
                            followeditbutton.setEnabled(false);
                        }

                        else
                            followeditbutton.setText("Follow Requests (" +
                                    Long.toString(longCount) + ")");
                    }
                } catch (TimeoutException e) {}
                followeditbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        Context context = view.getContext();
                        Intent intent = new Intent(context, FollowRequestActivity.class);
                        startActivity(intent);
                    }}
                );} else {
                followeditbutton.setVisibility(View.GONE);
            }
        }else {
            FollowController followController = new FollowController();
            final FollowRequestController followRequestController = new FollowRequestController();
            if (followController.followExists(LocalData.getSignedInProfile(getApplicationContext()),profile)){
                followeditbutton.setText("Followed");
                followeditbutton.setClickable(FALSE);
            } else {
                if (followRequestController.requestExists(LocalData.getSignedInProfile(getApplicationContext()),profile)){
                    followeditbutton.setText("Request Sent");
                    followeditbutton.setClickable(FALSE);
                }else {
                    followeditbutton.setText("Follow");
                    followeditbutton.setClickable(TRUE);
                    followeditbutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view){
                            FollowRequest followRequest = new FollowRequest(
                                    LocalData.getSignedInProfile(getApplicationContext()), profile);
                            followRequestController.addOrUpdateFollows(followRequest);
                            try {
                                followRequestController.waitForTask();
                                if (followRequestController.getFollowRequestFromID(followRequest.getId()).equals(followRequest)) {
                                    followeditbutton.setText("Request Sent");
                                } else {
                                    followeditbutton.setText("Request Not Sent");
                                }
                                followeditbutton.setClickable(FALSE);
                            } catch (InterruptedException e) {}
                            catch (TimeoutException e) {}
                            catch (ExecutionException e) {}
                        }}
                    );}
            }}

        ImageButton imageButton = (ImageButton) findViewById(R.id.backButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                finish();
            }}
        );

        ImageButton addPost = (ImageButton) findViewById(R.id.addPost);
        addPost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, AddOrEditPostActivity.class);

                startActivity(intent);
            }
        });
        ImageButton searchButton = (ImageButton) findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Context context = view.getContext();
                Intent intent = new Intent(context, SearchMoods.class);
                startActivity(intent);
            }}
        );

        Button followingButton = (Button) findViewById(R.id.followingButton);
        followingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, FollowingActivity.class);
                startActivity(intent);
            }
        });

        Button newsfeedButton = (Button) findViewById(R.id.newsfeedButton);
        newsfeedButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, NewsFeedActivity.class);
                startActivity(intent);
            }
        });

        Button profileButton = (Button) findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, ViewProfileActivity.class);
                intent.putExtra("ID",LocalData.getSignedInProfile(getApplicationContext()).getId());
                startActivity(intent);
            }
        });

        this.setImage(profile.getImageBitmap());
    }

    public void onUndoDeletePost(View v) {

        PostController controller = new PostController();
        // Setting ID to null because we're just adding a new post with same properties
        deleted.setId(null);
        controller.addOrUpdatePosts(deleted);

        try {
            controller.waitForTask();
        }

        catch (Exception e) {
            ConnectionManager.showConnectionError(this);
        }

        Intent intent = new Intent(this, ViewPostActivity.class);
        intent.putExtra("POST", deleted);
        startActivityForResult(intent, GET_POST_REQUEST);
    }

    // Modified from:
    // https://developer.android.com/guide/topics/ui/shortcuts.html
    // Accessed April 6, 2017
    public void onPinProfileClicked(View v) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            List<ShortcutInfo> existing = shortcutManager.getDynamicShortcuts();

            for (ShortcutInfo existingShortcut: existing) {

                // If shortcut already exists, remove it
                if (existingShortcut.getId().equals(profile.getId())) {
                    shortcutManager.removeDynamicShortcuts(Arrays.asList(profile.getId()));
                    Toast.makeText(getApplicationContext(), "Removed " + profile.getName(),
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            Intent intent = new Intent(getApplicationContext(), ViewProfileActivity.class);
            intent.putExtra("ID", profile.getId());
            intent.setAction(Intent.ACTION_VIEW);

            ShortcutInfo.Builder shortcut = new ShortcutInfo.Builder(this, profile.getId())
                    .setShortLabel(profile.getName())
                    .setLongLabel(profile.getName())
                    .setIntent(intent)
                    .setDisabledMessage(profile.getName() + " shortcut disabled");

            Bitmap profileImage = profile.getImageBitmap();

            if (profileImage != null)
                shortcut.setIcon(Icon.createWithBitmap(profileImage));

            shortcutManager.addDynamicShortcuts(Arrays.asList(shortcut.build()));

            Toast.makeText(getApplicationContext(), "Hold app icon for " + profile.getName(),
                    Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Set the image for the ImageView (to display the profile picture) to the given image. If the
     * given image is null, do nothing.
     *
     * @param image the image to display in the ImageView
     */
    public void setImage(Bitmap image) {

        if (image != null) {
            ImageView imageView = (ImageView) this.findViewById(R.id.profilePictureView);
            imageView.setImageBitmap(image);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case GET_POST_REQUEST:

                if (resultCode == RESULT_OK) {

                    Post post = data.getParcelableExtra("POST");

                    if (post == null) {
                        mDataset.remove(position);
                        //LocalData.deletePostAt(position, post,this);
                    }

                    else {
                        mDataset.set(position, post);
                        //LocalData.updatePostAt(position, post, this);
                    }

                    mAdapter.notifyDataSetChanged();

                    deleted = data.getParcelableExtra("DELETED_POST");

                    if (deleted != null) {

                        final ViewProfileActivity activity = this;

                        View.OnClickListener onClickListener = new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                activity.onUndoDeletePost(v);
                            }
                        };

                        Snackbar
                                .make(findViewById(android.R.id.content), "Post deleted", Snackbar.LENGTH_LONG)
                                .setAction("Undo", onClickListener)
                                .setActionTextColor(Color.RED)
                                .show();
                    }
                }

                break;
        }
    }

    /**
     * MyAdapter controls the list of the profile's posts by extending RecyclerView <br>
     *     http://www.androidhive.info/2016/01/android-working-with-recycler-view/ <br>
     *         2017-03-7
     * @author Taylor
     */
    public class MyAdapter extends RecyclerView.Adapter<ViewProfileActivity.MyAdapter.ViewHolder> {
        private ArrayList<Post> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public TextView mNameView;
            public ImageView mMoodView;
            public TextView mTextView;

            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.postText);
                mNameView = (TextView) v.findViewById(R.id.postName);
                mMoodView = (ImageView) v.findViewById(R.id.postMood);
                v.setOnClickListener(this);
            }
            @Override
            public void onClick(View view){
                position = mRecyclerView.getChildLayoutPosition(view);
                Post post = mDataset.get(position);
                Context context = view.getContext();
                Intent intent = new Intent(context, ViewPostActivity.class);
                intent.putExtra("POST",post);
                startActivityForResult(intent, GET_POST_REQUEST);
            }
        }

        public MyAdapter(ArrayList<Post> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public ViewProfileActivity.MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.profile_post_item, parent, false);

//            ViewProfileActivity.MyAdapter.ViewHolder vh = new ViewProfileActivity.MyAdapter.ViewHolder(v);
//            return vh;
//            mwschafe fixing redudant variable from code above to code below
            return new ViewProfileActivity.MyAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewProfileActivity.MyAdapter.ViewHolder holder, int position) {
            Post post = mDataset.get(position);
            holder.mNameView.setText(profile.getName());
            holder.mTextView.setText(post.getText());
            int[] draws = new int[]{R.drawable.angry,R.drawable.confused,R.drawable.disgusted,
                    R.drawable.fear,R.drawable.happy,R.drawable.sad,R.drawable.shame,R.drawable.suprised};
            holder.mMoodView.setImageResource(draws[post.getMood()]);
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
    /*public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private ViewProfileActivity.ClickListener clickListener;
        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ViewProfileActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }
        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }
        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }
*/
}