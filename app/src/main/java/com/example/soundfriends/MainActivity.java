package com.example.soundfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soundfriends.adapter.ViewPagerAdapter;
import com.example.soundfriends.auth.Login;
import com.example.soundfriends.fragments.SearchFragment;
import com.example.soundfriends.utils.ToggleInputFocus;
import com.example.soundfriends.utils.ZoomOutPageTransformer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    EditText inputSearch;
    ImageButton btnSearch;
    ViewPager2 viewPager;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager2) findViewById(R.id.view_pager);
        //disable swipe to change view pager
        viewPager.setUserInputEnabled(false);
//        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);
        inputSearch = findViewById(R.id.search_bar);
        btnSearch = findViewById(R.id.btnSearch);

        //set view pager adapter at bottom navigation
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        int pagePosition = 0;
        Intent intent = getIntent();
        if (intent != null) {
            System.out.println("WTF");
            String pagePositionString = intent.getStringExtra("pagePosition");
            System.out.println("pagePositionString" + pagePositionString);

            if(pagePositionString != null) {
                pagePosition = Integer.parseInt(pagePositionString);
                viewPager.setCurrentItem(pagePosition);
                bottomNavigationView.getMenu().findItem(R.id.menu_settings).setChecked(true);
            }

        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position){
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.menu_home).setChecked(true);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.menu_search).setChecked(true);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.menu_settings).setChecked(true);
                        break;
                }
            }
        });
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.menu_home){
                    viewPager.setCurrentItem(0);
                } else if (item.getItemId() == R.id.menu_search) {
                    viewPager.setCurrentItem(1);
                } else viewPager.setCurrentItem(2);

                ToggleInputFocus.unfocusAndHideKeyboard(MainActivity.this, inputSearch);
                return true;
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSearch();
            }
        });
    }

    private void handleSearch() {
        if (!inputSearch.getText().toString().trim().isEmpty()){
            String searchValue = inputSearch.getText().toString().trim();
            Bundle bundle = new Bundle();
            bundle.putString("search_keyword", searchValue);

            SearchFragment searchFragment = new SearchFragment();
            searchFragment.setArguments(bundle);

            viewPager.setCurrentItem(1, true);
        }
    }
}