package com.example.soundfriends.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.soundfriends.MainActivity;
import com.example.soundfriends.R;
import com.example.soundfriends.auth.Login;
import com.example.soundfriends.fragments.Model.Songs;
import com.example.soundfriends.fragments.Model.UploadSongs;
import com.example.soundfriends.utils.ToggleShowHideUI;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment  implements AdapterView.OnItemSelectedListener {

    TextView textViewImage;
    ProgressBar progressBar;
    RelativeLayout rlUploadingSong;
    Uri audioUri ;
    StorageReference mStorageref;
    StorageTask mUploadsTask ;
    DatabaseReference referenceSongs ;
    MediaMetadataRetriever metadataRetriever;
    byte [] art ;
    String title1, artist1, imageView1 = "", category1, userID;
    TextView title,artist,category;
    ImageView imageView ;
    String TAG = "huhu";
    UploadSongs uploadSongs;
    RecyclerView rcvlist_song_uploaded;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView textView;
    ImageView settingsAvatar;
    Button btnLogout, btnUpload, buttonUpload;
    Bitmap bitmap;
    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        textView = view.findViewById(R.id.textView);
        btnLogout = view.findViewById(R.id.logout);
        settingsAvatar = view.findViewById(R.id.settingsAvatar);

        textViewImage = view.findViewById(R.id.tvsrl);
        progressBar = view.findViewById(R.id.progressbar);
        rlUploadingSong = view.findViewById(R.id.rl_layout);
        title = view.findViewById(R.id.tvSong);
        artist = view.findViewById(R.id.tvArtist);
        category = view.findViewById(R.id.tvCategory);
        imageView = view.findViewById(R.id.img1);
        rcvlist_song_uploaded = view.findViewById(R.id.rcvlist_song_uploaded);


        rcvlist_song_uploaded.setLayoutManager(new LinearLayoutManager(requireContext()));

        //get Firebase user
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        if(user == null) {
            goAuthActivity();
        }else {
            userID = user.getUid();
            String info = user.getEmail() != null ? user.getEmail() : user.getDisplayName();
            textView.setText("Xin chào " + info);
            String url = user.getPhotoUrl().toString();
            Glide.with(this).load(Uri.parse(url)).into(settingsAvatar);

        }
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                goAuthActivity();
            }
        });

        //Nếu UserID lưu trong songs trùng với UserIDLogin thì hiện danh sách bài hát mà 2 user đấy trùng nhau
        FirebaseUser userIDLogin = FirebaseAuth.getInstance().getCurrentUser();
        String userIDLoginString = userIDLogin.getUid(); // Lấy UID của người dùng hiện tại

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference songsRef = database.getReference("songs");

        Query query = songsRef.orderByChild("userID").equalTo(userIDLoginString);

        FirebaseRecyclerOptions<Songs> options = new FirebaseRecyclerOptions.Builder<Songs>()
                .setQuery(query, Songs.class)
                .build();

        uploadSongs = new UploadSongs(options);
        rcvlist_song_uploaded.setAdapter(uploadSongs);



        metadataRetriever = new MediaMetadataRetriever();
        referenceSongs = FirebaseDatabase.getInstance().getReference().child("songs");
        mStorageref = FirebaseStorage.getInstance().getReference().child("songs");

        buttonUpload = view.findViewById(R.id.buttonUplaod);
        btnUpload = view.findViewById(R.id.bt_upload);


        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAudioFiles(view);
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFileTofirebase(view);
            }
        });


        return view;
    }

    private void goAuthActivity(){
        Intent unauthIntent = new Intent(getContext(), Login.class);
        startActivity(unauthIntent);
        getActivity().finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        uploadSongs.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        uploadSongs.stopListening();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void openAudioFiles(View v ){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(i,101);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode  == Activity.RESULT_OK && data.getData() != null){
            audioUri = data.getData();
            if(audioUri == null) {
                Log.d(TAG, "onActivityResult: OK");

            }

            String fileNames = getFileName(audioUri);
            textViewImage.setText(fileNames);

            Log.d("kk", "onActivityResult: OK" + fileNames);

            metadataRetriever.setDataSource(requireContext(), audioUri);
            art = metadataRetriever.getEmbeddedPicture();

            artist.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            title.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            category.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));

            artist1 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            title1 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            category1 = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);


            if(art!= null){
                bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
                imageView.setImageBitmap(bitmap);
                Log.d(TAG, "onActivityResult: " + bitmap);

            }else {
                Log.d(TAG, "onActivityResult: null");
            }
            ToggleShowHideUI.toggleShowUI(true, rlUploadingSong);
        }
        else {
            Log.d(TAG, "onActivityResult: NOt OK");
            ToggleShowHideUI.toggleShowUI(false, rlUploadingSong);
        }


    }

    @SuppressLint("Range")
    private  String getFileName(Uri uri){

        String result = null;
        Context context = requireContext();
        if(uri.getScheme().equals("content")){

            Cursor cursor = context.getContentResolver().query(uri, null,null,null,null);
            try {
                if (cursor != null && cursor.moveToFirst()) {

                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                }
            }
            finally {
                cursor.close();
            }
        }

        if(result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut != -1){
                result = result.substring(cut +1);

            }
        }
        return  result;
    }

    public  void  uploadFileTofirebase (View v ){
        if(textViewImage.equals("No file Selected")){
            Toast.makeText(getContext(), "Please select an image!", Toast.LENGTH_SHORT).show();
        }
        else{
            if(mUploadsTask != null && mUploadsTask.isInProgress()){
                Toast.makeText(getContext(), "songs uploads in already progress!", Toast.LENGTH_SHORT).show();
                ToggleShowHideUI.toggleShowUI(true, progressBar);
            }else {
                uploadFiles();
                ToggleShowHideUI.toggleShowUI(true, progressBar);
            }
        }

    }

    private void uploadFiles() {

        if(audioUri != null){
            Toast.makeText(getContext(), "uploads please wait!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            final  StorageReference storageReference = mStorageref.child(System.currentTimeMillis()+"."+getfileextension(audioUri));
            mUploadsTask = storageReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] imageBytes = byteArrayOutputStream.toByteArray();
                            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                            Log.d(TAG, "onSuccess: " + art);
                            Songs uploadSong = new Songs(title1, artist1, category1, base64Image,uri.toString(), userID);
                            String uploadId = referenceSongs.push().getKey();
                            referenceSongs.child(uploadId).setValue(uploadSong);

                            //change UI
                            ToggleShowHideUI.toggleShowUI(false, progressBar);
                            Toast.makeText(getContext(), "Tải lên thành công!", Toast.LENGTH_SHORT).show();
                            ToggleShowHideUI.toggleShowUI(false, rlUploadingSong);
                        }
                    });

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progess = (100.0* taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int) progess);

                }
            });

        }else {
            Toast.makeText(getContext(), "No file Selected to uploads", Toast.LENGTH_SHORT).show();
        }
    }

    private  String getfileextension(Uri audioUri){

        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }

    public class YourFragment extends Fragment {
        // ... Các phần khác của mã của Fragment ...
    }
}