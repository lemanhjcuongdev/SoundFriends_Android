package com.example.soundfriends.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.soundfriends.MainActivity;
import com.example.soundfriends.R;
import com.example.soundfriends.auth.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

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
    Button btnLogout;
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

        //get Firebase user
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if(user == null) {
            goAuthActivity();
        }else {
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
        return view;
    }

    private void goAuthActivity(){
        Intent unauthIntent = new Intent(getContext(), Login.class);
        startActivity(unauthIntent);
        getActivity().finish();
    }
}