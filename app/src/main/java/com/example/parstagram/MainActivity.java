package com.example.parstagram;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private EditText etDescription;
    private Button btnCaptureImage;
    private ImageView ivPostImage;
    private Button btnSubmit;
    private File photoFile;
    public String photoFileName = "photo.jpg";
    private int SOME_WIDTH = 400;
    private Button btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etDescription = findViewById(R.id.etDescription);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        ivPostImage = findViewById(R.id.ivPostImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);

            }
        });
        
        
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
        
        //queryPosts();


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                if (description.isEmpty()){
                    Toast.makeText(MainActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoFile == null || ivPostImage.getDrawable() == null){
                    Toast.makeText(MainActivity.this, "There is no image!", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, currentUser, photoFile);
            }
        });
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(MainActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // to get the image we override this method
    // will be invoked when child app returns to parent app, instagram home
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below

                // See code above
                Uri takenPhotoUri = Uri.fromFile(getPhotoFileUri(photoFileName));
// by this point we have the camera photo on disk
                Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
// See BitmapScaler.java: https://gist.github.com/nesquena/3885707fd3773c09f1bb
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, SOME_WIDTH);


                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
// Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
// Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                try {
                    FileOutputStream fos = new FileOutputStream(photoFile);
// Write the bytes of the bitmap to file
                    fos.write(bytes.toByteArray());
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Load the taken image into a preview
                ImageView ivPostImage = (ImageView) findViewById(R.id.ivPostImage);
                ivPostImage.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);

    }

    private void savePost(String description, ParseUser currentUser, File photoFile) {
        Post post = new Post();
        post.setDescription(description);
        post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null){
                    Log.e(TAG, "Error while saving");
                    Toast.makeText(MainActivity.this, "Error while saving", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Post was successful");
                etDescription.setText("");
                // empty resource id
                ivPostImage.setImageResource(0);
            }
        });
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Post post: posts){
                    Log.i(TAG, "Post:"+ post.getDescription() + "username:" + post.getUser().getUsername());
                }

            }
        });
    }
}
