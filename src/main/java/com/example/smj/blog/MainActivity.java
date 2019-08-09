package com.example.smj.blog;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mBlogList;

    private DatabaseReference mDatabase;

    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private boolean mProcessLike=false;

    private DatabaseReference mDatabaseLike;
    private DatabaseReference mDatabaseCurrentUser;

    //private Query mQueryCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();

        mAuthListener =new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser()==null){

                    Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike=FirebaseDatabase.getInstance().getReference().child("Likes");

       // String currentUserId=mAuth.getCurrentUser().getUid();

        mDatabaseCurrentUser=FirebaseDatabase.getInstance().getReference().child("Blog");

        //mQueryCurrentUser=mDatabaseCurrentUser.orderByChild("uid").equalTo(currentUserId);

        mDatabaseUsers.keepSynced(true);
        mDatabase.keepSynced(true);
        mDatabaseLike.keepSynced(true);

        mBlogList = (RecyclerView) findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        checkUserExist();
    }

    protected void onStart() {
        super.onStart();
//        checkUserExist();

        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(


                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                mDatabase

                //mQueryCurrentUser
        )


        {
            @Override
            protected void populateViewHolder(@NonNull BlogViewHolder viewHolder, @NonNull final Blog model, int position) {

                final String post_key=getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(),model.getImage());
                viewHolder.setUsername(model.getUsername());

                viewHolder.setLikeBtn(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       // Toast.makeText(MainActivity.this, post_key, Toast.LENGTH_SHORT).show();

                        Intent singleBlogIntent=new Intent(MainActivity.this,BlogSingleActivity.class);
                        singleBlogIntent.putExtra("blog_id",post_key);
                        startActivity(singleBlogIntent);
                    }
                });
                viewHolder.mLikebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcessLike=true;



                            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(mProcessLike) {

                                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                            mProcessLike = false;

                                        } else {
                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Randomvalue");
                                            mProcessLike = false;
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                    }
                });

            }


        };
        mBlogList.setAdapter(firebaseRecyclerAdapter);
    }
    private void checkUserExist() {

       // FirebaseUser currentUser = mAuth.getCurrentUser();

        if(mAuth.getCurrentUser()!=null) {
            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(user_id))
                    {
                        Intent setupIntent=new Intent(MainActivity.this,SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }



    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageView mLikebtn;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;



        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mLikebtn=(ImageView) mView.findViewById(R.id.like_btn);

            mDatabaseLike=FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth=FirebaseAuth.getInstance();

            mDatabaseLike.keepSynced(true);

        }

        public void setLikeBtn(final String post_key){
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    if (mAuth.getCurrentUser()!=null && dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){

                        mLikebtn.setImageResource(R.drawable.like);

                    }
                    else {
                        mLikebtn.setImageResource(R.drawable.heart_outlined_shape);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        public void setTitle(String title) {
           TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);

        }

        public void setDesc(String desc) {
            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }
        public void setUsername(String username){
            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText("Posted by "+username);
        }

        public void setImage(final Context ctx, final String image){
            final ImageView post_image =(ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).into(post_image);

                }
            });







        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }
        if(item.getItemId()==R.id.action_logout){
            logout();

        }

        return true;
    }

    private void logout() {
        mAuth.signOut();
    }


}
