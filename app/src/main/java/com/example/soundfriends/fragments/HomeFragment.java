package com.example.soundfriends.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.soundfriends.R;
import com.example.soundfriends.adapter.Main_BestCategoriesAdapter;
import com.example.soundfriends.adapter.Main_BestSingersAdapter;
import com.example.soundfriends.adapter.Main_BestSongsAdapter;
import com.example.soundfriends.fragments.Model.Song;
import com.example.soundfriends.utils.ToggleShowHideUI;
import com.example.soundfriends.utils.WrapContentLinearLayoutManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    RecyclerView rcvBestSongs, rcvBestSingers, rcvBestCategories;
    Main_BestSongsAdapter bestSongsAdapter;
    Main_BestSingersAdapter bestSingersAdapter;
    Main_BestCategoriesAdapter bestCategoriesAdapter;
    FirebaseDatabase database;
    DatabaseReference songsRef;
    ProgressBar pbLoadHome;
    List<Song> bestSongs = new ArrayList<>();
    List<Song> bestSingers = new ArrayList<>();
    List<Song> bestCategories = new ArrayList<>();
    LinearLayout layoutBestSong, layoutBestSinger, layoutBestCategory;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rcvBestSongs = view.findViewById(R.id.rcv_best_songs);
        rcvBestSingers = view.findViewById(R.id.rcv_best_singer);
        rcvBestCategories = view.findViewById(R.id.rcv_best_categories);
        pbLoadHome = view.findViewById(R.id.pbLoadHome);
        layoutBestSong = view.findViewById(R.id.layout_best_song);
        layoutBestSinger = view.findViewById(R.id.layout_best_singer);
        layoutBestCategory = view.findViewById(R.id.layout_best_category);

        WrapContentLinearLayoutManager wrapContentLinearLayoutManagerSongs = new WrapContentLinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rcvBestSongs.setLayoutManager(wrapContentLinearLayoutManagerSongs);
        WrapContentLinearLayoutManager wrapContentLinearLayoutManagerSingers = new WrapContentLinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rcvBestSingers.setLayoutManager(wrapContentLinearLayoutManagerSingers);
        WrapContentLinearLayoutManager wrapContentLinearLayoutManagerCategories = new WrapContentLinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rcvBestCategories.setLayoutManager(wrapContentLinearLayoutManagerCategories);

        //initial realtime DB
        database = FirebaseDatabase.getInstance();

        //set adapter
        bestSongsAdapter = new Main_BestSongsAdapter(getContext(), bestSongs);
        rcvBestSongs.setAdapter(bestSongsAdapter);
        bestSingersAdapter = new Main_BestSingersAdapter(getContext(), bestSingers);
        rcvBestSingers.setAdapter(bestSingersAdapter);
        bestCategoriesAdapter = new Main_BestCategoriesAdapter(getContext(), bestCategories);
        rcvBestCategories.setAdapter(bestCategoriesAdapter);

        //get data from Firebase
        getData();

        return view;
    }

    private void getData() {
        //get best songs
        songsRef = database.getReference("songs");
        songsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshotItem: snapshot.getChildren()){
                    //best song
                    Song song = dataSnapshotItem.getValue(Song.class);
                    bestSongs.add(song);

                    //best singer
                    String currentSinger = dataSnapshotItem.child("artist").getValue(String.class);
                    Boolean containsSinger = false;
                    for(Song songArtist : bestSingers){
                        if (songArtist.getArtist().equals(currentSinger)){
                            containsSinger = true;
                            break;
                        }
                    }
                    if (!containsSinger){
                        Song songArtist = dataSnapshotItem.getValue(Song.class);
                        bestSingers.add(songArtist);
                    }

                    //best category
                    String currentCategory = dataSnapshotItem.child("category").getValue(String.class);
                    Boolean containsCategory = false;
                    for(Song songCategory : bestCategories){
                        if (songCategory.getCategory().equals(currentCategory)){
                            containsCategory = true;
                            break;
                        }
                    }
                    if (!containsCategory){
                        Song songCategory = dataSnapshotItem.getValue(Song.class);
                        bestCategories.add(songCategory);
                    }
                }
                bestSongsAdapter.notifyDataSetChanged();
                bestSingersAdapter.notifyDataSetChanged();
                bestCategoriesAdapter.notifyDataSetChanged();

                ToggleShowHideUI.toggleShowUI(false, pbLoadHome);
                ToggleShowHideUI.toggleShowUI(true, layoutBestSong);
                ToggleShowHideUI.toggleShowUI(true, layoutBestSinger);
                ToggleShowHideUI.toggleShowUI(true, layoutBestCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    };
}