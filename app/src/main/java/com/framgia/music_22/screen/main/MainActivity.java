package com.framgia.music_22.screen.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.framgia.music_22.data.model.User;
import com.framgia.music_22.data.source.local.sqlite.DatabaseSQLite;
import com.framgia.music_22.screen.base.Navigator;
import com.framgia.music_22.screen.login.LoginActivity;
import com.framgia.music_22.screen.music_player.PlayMusicFragment;
import com.framgia.music_22.utils.ConnectionChecking;
import com.framgia.music_22.utils.TypeTab;
import com.framgia.vnnht.music_22.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String SONG_ID = "SongId";
    private static final String TITLE = "Title";
    private static final String GENRE = "Genre";
    private static final String SONG_PATH = "SongPath";
    private static final String ARTIST_NAME = "ArtistName";
    private static final String DURATION = "Duration";
    private static final String TIME_FORMAT = "mm:ss";

    private ReloadFragmentCallBack mReloadFragmentCallBack;
    private ViewPager mPagerMain;
    private BottomNavigationView mBottomNavigationViewavigation;
    private MenuItem mPrevMenuItem;
    private View mInclude, mViewClick;
    private Navigator mNavigator;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private TextView txtHeader_username, txtHeader_email;
    private FirebaseAuth mAuth;
    private SharedPreferences share;

    private User curUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        curUser = (User) getIntent().getSerializableExtra("UserInfo");
        initView();
        initDatabase();
    }

    private void initDatabase() {
        DatabaseSQLite databaseSQLite = new DatabaseSQLite(this);
        databaseSQLite.queryData("CREATE TABLE IF NOT EXISTS "
                + DatabaseSQLite.TABLE_NAME
                + " ("
                + SONG_ID
                + " TEXT PRIMARY KEY,"
                + TITLE
                + " TEXT,"
                + GENRE
                + " TEXT,"
                + SONG_PATH
                + " TEXT,"
                + ARTIST_NAME
                + " TEXT,"
                + DURATION
                + " INTEGER)");
    }

    public void setReloadFragmentCallBack(ReloadFragmentCallBack reloadFragmentCallBack) {
        mReloadFragmentCallBack = reloadFragmentCallBack;
    }

    private void initView() {
        mBottomNavigationViewavigation = findViewById(R.id.navigation);
        mInclude = findViewById(R.id.include_mini_player);
        mViewClick = findViewById(R.id.view_mini_player);
        mAuth = FirebaseAuth.getInstance();

        mBottomNavigationViewavigation.setOnNavigationItemSelectedListener(this);
        mPagerMain = findViewById(R.id.pager_main);
        mPagerMain.addOnPageChangeListener(this);
        setUpViewPager(mPagerMain);
        mInclude.setVisibility(View.GONE);

        mNavigator = new Navigator();

        mViewClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment =
                        mNavigator.findFragment(MainActivity.this, PlayMusicFragment.TAG);
                mNavigator.showFragment(getSupportFragmentManager(), fragment, false, true);
            }
        });

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mDrawerLayout = findViewById(R.id.container);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.bringToFront();
        mNavigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        txtHeader_email = mNavigationView.getHeaderView(0).findViewById(R.id.txtHeader_Email);
        txtHeader_username = mNavigationView.getHeaderView(0).findViewById(R.id.txtHeader_Username);

        checkLogin();

    }

    public void checkLogin(){

        share = getSharedPreferences("MyShare", MODE_PRIVATE);

        String name = share.getString("URName", "");
        String email = share.getString("UREmail", "");

        if (name.length() == 0 || email.length() ==0) {
            hideItem();
        } else {
            txtHeader_email.setText(email);
            txtHeader_username.setText(name);
            hideLogin();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_homepage:
                mPagerMain.setCurrentItem(TypeTab.TAB_HOME);
                break;
            case R.id.item_offline:
                mReloadFragmentCallBack.reloadFragment();
                mPagerMain.setCurrentItem(TypeTab.TAB_OFFLINE);
                break;
            case R.id.item_singer:
                mPagerMain.setCurrentItem(TypeTab.TAB_ARTIST);
                break;
            case R.id.item_gernes:
                mPagerMain.setCurrentItem(TypeTab.TAB_GENRES);
                break;
            case R.id.nav_login:
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_logout:
                SharedPreferences.Editor editor = share.edit();
                editor.clear();
                editor.commit();
                curUser = null;
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(getIntent());
                break;
        }
        return true;

    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (mPrevMenuItem != null) {
            mPrevMenuItem.setChecked(false);
        } else {
            mBottomNavigationViewavigation.getMenu().getItem(0).setChecked(true);
        }
        mBottomNavigationViewavigation.getMenu().getItem(position).setChecked(true);
        mPrevMenuItem = mBottomNavigationViewavigation.getMenu().getItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void addFragment(Fragment fragment, Boolean haveAnimation, String tag,
                            Boolean addToBackStack) {
        Navigator navigator = new Navigator();
        navigator.addFragment(getSupportFragmentManager(), fragment, R.id.fl_container_full,
                addToBackStack, tag, null, haveAnimation);
    }

    private void setUpViewPager(ViewPager viewPager) {
        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mainPagerAdapter);
        ConnectionChecking connectionChecking = new ConnectionChecking(getApplicationContext());
        if (!connectionChecking.isNetworkConnection()) {
            viewPager.setCurrentItem(TypeTab.TAB_OFFLINE);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment playMusicFragment = mNavigator.findFragment(this, PlayMusicFragment.TAG);
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        if(curUser != null){
            super.onBackPressed();
        }
        if (playMusicFragment != null && playMusicFragment.isVisible()) {
            mNavigator.hideFragment(getSupportFragmentManager(), playMusicFragment, false, true);
            mInclude.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
            this.overridePendingTransition(R.anim.slide_bottom_up, R.anim.slide_top_down);
        }
    }

    public void hideBottomButton() {
        mBottomNavigationViewavigation.setVisibility(View.GONE);
    }

    public void showBottomButton() {
        mBottomNavigationViewavigation.setVisibility(View.VISIBLE);
    }

    public void hideItem() {
        Menu nav_Menu = mNavigationView.getMenu();
        nav_Menu.findItem(R.id.nav_profile).setVisible(false);
        nav_Menu.findItem(R.id.nav_chat).setVisible(false);
        nav_Menu.findItem(R.id.nav_message).setVisible(false);
        nav_Menu.findItem(R.id.nav_logout).setVisible(false);
        nav_Menu.findItem(R.id.nav_login).setVisible(true);
    }

    public void hideLogin() {
        Menu nav_Menu = mNavigationView.getMenu();
        nav_Menu.findItem(R.id.nav_profile).setVisible(true);
        nav_Menu.findItem(R.id.nav_chat).setVisible(true);
        nav_Menu.findItem(R.id.nav_message).setVisible(true);
        nav_Menu.findItem(R.id.nav_logout).setVisible(true);
        nav_Menu.findItem(R.id.nav_login).setVisible(false);
    }
}
