package com.example.smj.blog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//import android.widget.ImageSwitcher;

public class PostActivity extends AppCompatActivity {



    private ImageView mSelectImage;
    private EditText mPosttitle;
    private EditText mPostDesc;

    private Button mSubmitbtn;
    private  Uri mImageUri=null;

    private static final int GALLERY_REQUEST=1;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;

    private FirebaseUser mCurrentUser;

    private DatabaseReference mDatabaseUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post2);

        mAuth=FirebaseAuth.getInstance();
        mCurrentUser=mAuth.getCurrentUser();

        mStorage=FirebaseStorage.getInstance().getReference();
        mDatabase=FirebaseDatabase.getInstance().getReference().child("Blog");

         mDatabaseUser=FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mSelectImage = (ImageView) findViewById(R.id.imageSelect);

        mPosttitle =(EditText) findViewById(R.id.titleField);
        mPostDesc=(EditText) findViewById(R.id.descField);
        mSubmitbtn=(Button)findViewById(R.id.submit);

        mProgress=new ProgressDialog(this);

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        mSubmitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startPosting();
            }
        });

    }

    private void startPosting() {


        mProgress.setMessage("Posting to  blog...");
        mProgress.show();

        final String title_val=mPosttitle.getText().toString().trim();
        final String desc_val=mPostDesc.getText().toString().trim();

        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri!=null){

            //mProgress.show();

            StorageReference filepath=mStorage.child("Blog_Images").child(mImageUri.getLastPathSegment());
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                           final DatabaseReference newPost=mDatabase.push();
                            //newPost.child("title").setValue(title_val);
                           // newPost.child("desc").setValue(desc_val);
                           // newPost.child("image").setValue(uri.toString());
                           // newPost.child("uid").setValue(mCurrentUser.getUid());

                            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    newPost.child("title").setValue(title_val);
                                    newPost.child("desc").setValue(desc_val);
                                    newPost.child("image").setValue(uri.toString());
                                    newPost.child("uid").setValue(mCurrentUser.getUid());
                                    newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                startActivity(new Intent(PostActivity.this,MainActivity.class));

                                            }
                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            Log.i("key",newPost.getKey());
                            mProgress.dismiss();
                           // startActivity(new Intent(PostActivity.this,MainActivity.class));
                        }
                    });



                    }



            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK){

            mImageUri=data.getData();
            mSelectImage.setImageURI(mImageUri);

        }
    }
}
