package com.example.smj.blog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class BlogSingleActivity extends AppCompatActivity {

    private String mPost_key=null;

    private DatabaseReference mDatabase;

    private ImageView mBlogSingleImage;
    private TextView mBlogSingleTitle;
    private TextView mBlogSingleDesc;

    private Button mSingleRemoveBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        mDatabase=FirebaseDatabase.getInstance().getReference().child("Blog");

        mAuth=FirebaseAuth.getInstance();

        mPost_key=getIntent().getExtras().getString("blog_id");


        mBlogSingleDesc=(TextView)findViewById(R.id.singleBlogDesc);
        mBlogSingleImage=(ImageView)findViewById(R.id.singleBlogImage);
        mBlogSingleTitle=(TextView)findViewById(R.id.singleBlogTitle);
        mSingleRemoveBtn=(Button)findViewById(R.id.singleRemoveBtn);

        //Toast.makeText(BlogSingleActivity.this, post_key, Toast.LENGTH_SHORT).show();

        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String post_title=(String) dataSnapshot.child("title").getValue();
                final String post_desc=(String) dataSnapshot.child("desc").getValue();
                final String post_Image=(String) dataSnapshot.child("image").getValue();
                String post_uid =(String) dataSnapshot.child("uid").getValue();



                mBlogSingleTitle.setText(post_title);
                mBlogSingleDesc.setText(post_desc);


                Picasso.with(BlogSingleActivity.this).load(post_Image).into(mBlogSingleImage);

                if (mAuth.getCurrentUser().getUid().equals(post_uid)){
                    mSingleRemoveBtn.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mSingleRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child(mPost_key).removeValue();

                Intent mainIntent=new Intent(BlogSingleActivity.this,MainActivity.class);
                startActivity(mainIntent);
            }
        });
    }
}
