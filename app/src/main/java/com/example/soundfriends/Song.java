package com.example.soundfriends;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.soundfriends.adapter.UploadedSongAdapter;
import com.example.soundfriends.fragments.CommentsFragment;
import com.example.soundfriends.utils.ImageProcessor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Song extends AppCompatActivity implements SensorEventListener {
    boolean isPlaying = false;
    boolean isDirty = false;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private final Handler handler = new Handler();
    private int currentPosition;
    private boolean loopEnabled = false;
    private String audioURL;

    private String songId = "";
    private Sensor accelerometer;
    private ImageView imageView;
    int songIndex;
    long songCount;
    ImageButton next, previous, play, pause;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean isShakeEnabled = false;
    ImageButton loopBtn, imgback, shuffle, imgDownload;
    private boolean isLoop, isShuffling = false;
    private boolean isSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        getData();
        //send song id to load comment of that song
        sendDataToFragment(songId);


        imageView = findViewById(R.id.phonering);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        seekBar = findViewById(R.id.seekbar);

        //update seekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // Update MediaPlayer position when the user slides the SeekBar
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // When the user starts sliding the SeekBar, set the flag
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // When the user stops sliding the SeekBar, unset the flag
                isSeeking = false;
            }
        });

        // Set up a timer to update the SeekBar while the music is playing
        final Handler handler = new Handler();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer != null && !isSeeking) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Update the SeekBar position
                            seekBar.setProgress(currentPosition);
                        }
                    });
                }
            }
        }, 0, 1000); // Update every 1 second (adjust as needed)


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShakeEnabled = !isShakeEnabled;
                if (isShakeEnabled) {
                    imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_pink));
                } else {
                    imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black));
                }
            }
        });

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Handle the completion of a song, e.g., play the next song in the playlist
                playNextSong();
            }
        });
        // Fetch the original playlist from Firebase
    }

    private void getData() {
        // Retrieve data from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            songId = intent.getStringExtra("songId");
        }

        // Create a reference to the "songs" node in your Firebase Realtime Database
        DatabaseReference songsRef = FirebaseDatabase.getInstance().getReference().child("songs");

        // Query the specific song by its ID
        Query songQuery = songsRef.orderByChild("id").equalTo(songId);


        // Truy vấn dữ liệu
        songQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                    // Lấy ra kết quả truy vấn được
                    String title = songSnapshot.child("title").getValue(String.class);
                    String artist = songSnapshot.child("artist").getValue(String.class);
                    String imgUrl = songSnapshot.child("urlImg").getValue(String.class);
                    String srl = songSnapshot.child("srl").getValue(String.class);
                    Integer nullIndex = songSnapshot.child("indexSong").getValue(Integer.class);
                    if (nullIndex != null) {
                        int currentIndex = nullIndex.intValue();
                        if (currentIndex > songIndex) {
                            songIndex = currentIndex;
                        }
                    }

                    audioURL = srl;

                    TextView textViewMusicTitle = findViewById(R.id.txtMusicTitle);
                    textViewMusicTitle.setText(title);

                    TextView txtArtist = findViewById(R.id.txtartist);
                    txtArtist.setText(artist);

                    ImageView imgSong = findViewById(R.id.imgsong);


                    // Lấy chuỗi bitmap từ Firebase (giả sử 'model.getUrlImg()' chứa chuỗi bitmap) và hiển thị ảnh
                    ImageProcessor.Base64ToImageView(imgSong, imgSong.getContext(), imgUrl);


                    try {
                        mediaPlayer.setDataSource(srl);
                        mediaPlayer.prepare();
                        seekBar.setMax(mediaPlayer.getDuration());

                        final Runnable updateSeekBar = new Runnable() {
                            @Override
                            public void run() {
                                TextView txtCurrentDuration = findViewById(R.id.txtCurrentDuration);
                                TextView txtDuration = findViewById(R.id.txtDuration);
                                currentPosition = mediaPlayer.getCurrentPosition();
                                txtCurrentDuration.setText(formatDuration(mediaPlayer.getCurrentPosition()));
                                txtDuration.setText(formatDuration(mediaPlayer.getDuration()));
                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                handler.postDelayed(this, 100); // update every 100ms
                            }
                        };
                        handler.postDelayed(updateSeekBar, 100);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    playMusic();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors
            }
        });


        // Khơi tạo MediaPlayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        play = findViewById(R.id.play);
        imgDownload = findViewById(R.id.btnDownload);
        loopBtn = findViewById(R.id.loopBtn);
        shuffle = findViewById(R.id.shuffle);

//        play.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!isDirty) {
//                    playAudio();
//                    play.setImageResource(R.drawable.pause);
//                    isPlaying =true;
//                    isDirty = true;
//                } else {
//                    if (!isPlaying) {
//                        resumeAudio();
//                        play.setImageResource(R.drawable.pause);
//                    } else {
//                        mediaPlayer.seekTo(currentPosition);
//                        mediaPlayer.pause();
//                        play.setImageResource(R.drawable.play);
//                    }
//                }
//                isPlaying = !isPlaying;
//            }
//        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    pauseMusic();
                } else {
                    playMusic();
                }
            }
        });


        imgDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadAudio(audioURL);
            }
        });

        //click next
        next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNextSong();
            }
        });

        //click previous
        previous = findViewById(R.id.previous);

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPreviousSong();
            }
        });
        imgback = findViewById(R.id.imgback);

        imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển đến Activity mới
                Intent intent = new Intent(Song.this, UploadedSongAdapter.class);

                // Khởi động Activity mới
                startActivity(intent);
            }
        });

        loopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI_Loop();
                if (mediaPlayer != null) {
                    if (isLoop) {
                        mediaPlayer.setLooping(false); // Disable loop
                        isLoop = false;
                    } else {
                        mediaPlayer.setLooping(true); // Enable loop
                        isLoop = true;
                    }
                }
            }
        });

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI_Shuffle();
                //toggleShuffle();
            }
        });
    }

    public void setShuffle(){
        if (isShuffling) isShuffling=false;
        else isShuffling = true;
    }
    private void updateUI_Loop() {
        // Update UI elements based on the shuffling state
        if (isLoop) {
            loopBtn.setImageResource(R.drawable.loop);
        } else {
            loopBtn.setImageResource(R.drawable.loop_color);
        }
    }

    private void updateUI_Shuffle() {
        // Update UI elements based on the shuffling state
        if (isShuffling) {
            shuffle.setImageResource(R.drawable.shuffle);
        } else {
            shuffle.setImageResource(R.drawable.shuffle_color);
        }
    }

    private void playMusic() {
        mediaPlayer.start();
        play.setImageResource(R.drawable.pause); // Set the pause icon
        Toast.makeText(this, "Song is playing", Toast.LENGTH_SHORT).show();
    }

    private void pauseMusic() {
        mediaPlayer.pause();
        play.setImageResource(R.drawable.play); // Set the play icon
        Toast.makeText(this, "Song is paused", Toast.LENGTH_SHORT).show();
    }


    private void playNextSong() {

        DatabaseReference songsRef = FirebaseDatabase.getInstance().getReference().child("songs");
        songsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Đếm số lượng bài hát từ DataSnapshot
                songCount = (int) dataSnapshot.getChildrenCount();
                // Bây giờ bạn đã cập nhật giá trị của songCount dựa trên số lượng bài hát trên Firebase
                // Bạn có thể sử dụng giá trị này trong code của bạn.
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu cần
            }
        });

        if (songIndex < songCount - 1) {
            int nextSongIndex = songIndex + 1; // Tăng chỉ số bài hát để lấy bài hát tiếp theo
            songIndex = nextSongIndex;
//            Toast.makeText(getApplicationContext(), "oke " + songIndex, Toast.LENGTH_SHORT).show();
            changeSong(nextSongIndex);
        } else {
            // Đã đến cuối danh sách bài hát
            // ...
        }

    }

    private void playPreviousSong() {
        DatabaseReference songsRef = FirebaseDatabase.getInstance().getReference().child("songs");
        songsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                songCount = (int) dataSnapshot.getChildrenCount();
                if (songIndex > 0) {
                    int previousSongIndex = songIndex - 1;
                    songIndex = previousSongIndex;
                    // Giảm chỉ số bài hát để lấy bài hát trước đó
                    changeSong(previousSongIndex);
                } else {
                    // Đã ở đầu danh sách bài hát
                    // Xử lý theo ý muốn của bạn, ví dụ: quay lại cuối danh sách hoặc hiển thị thông báo
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu cần
            }
        });

    }

        private void changeSong(int next){
            // Dừng bài hát hiện tại nếu đang chạy
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }

            // Khởi tạo MediaPlayer
            mediaPlayer = new MediaPlayer();

        DatabaseReference songsRef = FirebaseDatabase.getInstance().getReference().child("songs");
        Query songQuery = songsRef.orderByChild("indexSong").equalTo(next);
        // Truy vấn dữ liệu

        songQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                    // Lấy ra kết quả truy vấn được
                    String title = songSnapshot.child("title").getValue(String.class);
                    String artist = songSnapshot.child("artist").getValue(String.class);
                    String imgUrl = songSnapshot.child("urlImg").getValue(String.class);
                    String srl = songSnapshot.child("srl").getValue(String.class);

                    audioURL = srl;

                    TextView textViewMusicTitle = findViewById(R.id.txtMusicTitle);
                    textViewMusicTitle.setText(title);

                    TextView txtArtist = findViewById(R.id.txtartist);
                    txtArtist.setText(artist);

                    ImageView imgSong = findViewById(R.id.imgsong);

                    //update comment per song
                    sendDataToFragment(songSnapshot.child("id").getValue(String.class));

                    // Lấy chuỗi bitmap từ Firebase (giả sử 'model.getUrlImg()' chứa chuỗi bitmap) và hiển thị ảnh
                    ImageProcessor.Base64ToImageView(imgSong, imgSong.getContext(), imgUrl);

                    try {
                        // ... (Các bước khác để chuẩn bị bài hát mới)
                        mediaPlayer.setDataSource(srl);
                        mediaPlayer.prepare();

                        seekBar = findViewById(R.id.seekbar);
                        seekBar.setMax(mediaPlayer.getDuration());

                        final Runnable updateSeekBar = new Runnable() {
                            @Override
                            public void run() {
                                TextView txtCurrentDuration = findViewById(R.id.txtCurrentDuration);
                                TextView txtDuration = findViewById(R.id.txtDuration);
                                currentPosition = mediaPlayer.getCurrentPosition();
                                txtCurrentDuration.setText(formatDuration(mediaPlayer.getCurrentPosition()));
                                txtDuration.setText(formatDuration(mediaPlayer.getDuration()));
                                seekBar.setProgress(currentPosition);
                                handler.postDelayed(this, 100); // update every 100ms
                            }
                        };
                        handler.postDelayed(updateSeekBar, 100);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors
            }
        });

            resumeAudio();
            play.setImageResource(R.drawable.play);
            isDirty=false;
    }

    private void resumeAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(currentPosition);
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    private void playAudio() {
        // Chạy các hàm cần thực thi sau khi lấy được nhạc
        mediaPlayer.start();
        // below line is use to display a toast message.
        Toast.makeText(this, "Audio started playing..", Toast.LENGTH_SHORT).show();
    }

    private String formatDuration(long durationMs) {
        long hours = TimeUnit.MILLISECONDS.toHours(durationMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void downloadAudio(String AUDIO_URL) {
        Uri uri = Uri.parse(AUDIO_URL);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("My Audio");
        request.setDescription("Đang tải nhạc xuống...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "my_audio.mp3");
        Toast.makeText(this, "Đang tải nhạc xuống...", Toast.LENGTH_SHORT).show();

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        // You can track the download progress using the downloadId if needed
        // For example, you can query the DownloadManager for download status
        // by calling downloadManager.query(new DownloadManager.Query().setFilterById(downloadId))
        // and updating your UI accordingly.
    }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && isShakeEnabled) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Tính gia tốc tổng cộng
            double acceleration = Math.sqrt(x * x + y * y + z * z);

            // Kiểm tra nếu gia tốc vượt qua một ngưỡng (đây là ví dụ)
            if (acceleration > 12) {
                // Xử lý sự kiện lắc, ví dụ: chuyển bài hát
//                Toast.makeText(getApplicationContext(), "xảy ra sự kiện lắc", Toast.LENGTH_SHORT).show();
                playNextSong();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Ngừng lắng nghe sensor khi ứng dụng tạm dừng
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tiếp tục lắng nghe sensor khi ứng dụng tiếp tục
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendDataToFragment(String songId){
        //create comment fragment
        CommentsFragment commentsFragment = new CommentsFragment();

        Bundle bundle = new Bundle();
        bundle.putString("key_song_id", songId);
        commentsFragment.setArguments(bundle);

        //use FragmentManager
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.comment_fragment_container, commentsFragment)
                .commit();
    }
}