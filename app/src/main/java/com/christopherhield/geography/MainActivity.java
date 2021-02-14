package com.christopherhield.geography;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final static ArrayList<SpannableString> subRegionDisplayed = new ArrayList<>();
    private final static ArrayList<Source> newsData = new ArrayList<>();
    private static Menu opt_menu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<Fragment> fragments;
    public static ArrayList<Article> articles;
    private MyPageAdapter pageAdapter;
    private ViewPager pager;
    public String currentCategory = "all";
    public String currentLanguage = "all";
    public String currentCountry = "all";
    private String itemTitle;
    public static HashMap<String, String> nameToId = new HashMap<>();
    public static int pageIndex = 0;
    private static HashMap<String, Integer> colorMap = new HashMap<>();

    public static int screenWidth, screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.drawer_list);

        // Set up the drawer item click callback method
        mDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectItem(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
        );

        // Create the drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        fragments = new ArrayList<>();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);

        // Load the data
        new Thread(new RegionLoader(this, 2, "all", "all", "all")).start();
    }


    public void setupCategories(HashSet<Source> set) {
        newsData.clear();
        newsData.addAll(set);
        Collections.sort(newsData);
        opt_menu.clear();
        Menu topicMenu = opt_menu.addSubMenu("Topics");
        topicMenu.add("all");
        Menu languageMenu = opt_menu.addSubMenu("Languages");
        languageMenu.add("all");
        Menu countryMenu = opt_menu.addSubMenu("Countries");
        countryMenu.add("all");

        HashSet<String> topicSet = new HashSet<>();
        HashSet<String> languageSet = new HashSet<>();
        HashSet<String> countrySet = new HashSet<>();

        for(Source s : set) {
            topicSet.add(s.getCategory());
            languageSet.add(s.getLanguage());
            countrySet.add(s.getCountry());
        }

        ArrayList<String> topicList = new ArrayList<>(topicSet);
        Collections.sort(topicList);

        ArrayList<String> languageList = new ArrayList<>(languageSet);
        for(int i=0; i<languageList.size(); i++)
            languageList.set(i, Utilities.codeToName(getResources(), languageList.get(i), R.raw.language_codes, "languages"));
        Collections.sort(languageList);

        ArrayList<String> countryList = new ArrayList<>(countrySet);
        for(int i=0; i<countryList.size(); i++)
            countryList.set(i, Utilities.codeToName(getResources(), countryList.get(i), R.raw.country_codes, "countries"));
        Collections.sort(countryList);

        for (String s : topicList) {
            SpannableString spannableString = new SpannableString(s);
            int color = Utilities.getRandomColor();
            colorMap.put(s, color);
            spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), 0);
            topicMenu.add(spannableString);
        }
        for (String s : languageList) languageMenu.add(s);
        for (String s : countryList) countryMenu.add(s);
    }
    public void setupCategoriesLeft(HashSet<Source> set) {
        if(set.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Sources Exist");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        ArrayList<Source> lst = new ArrayList<>(set);
        ArrayList<SpannableString> names = new ArrayList<>();

        for(Source s : lst) {
            if(colorMap.containsKey(s.getCategory())) {
                SpannableString spannableString = new SpannableString(s.getName());
                spannableString.setSpan(new ForegroundColorSpan(colorMap.get(s.getCategory())), 0, spannableString.length(), 0);
                names.add(spannableString);
            }
        }

        subRegionDisplayed.clear();
        subRegionDisplayed.addAll(names);Collections.sort(subRegionDisplayed, new Comparator<SpannableString>() {
            @Override
            public int compare(SpannableString spannableString, SpannableString t1) {
                return spannableString.toString().compareTo(t1.toString());
            }
        });

        setTitle("News Gateway (" + subRegionDisplayed.size() + ")");
        mDrawerList.setAdapter(new ArrayAdapter<SpannableString>(this, R.layout.drawer_item, subRegionDisplayed){});

        ((ArrayAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void selectItem(int position) {
        pager.setBackground(null);
        String currentSource = subRegionDisplayed.get(position).toString();
        setTitle(currentSource);
        currentSource = nameToId.get(currentSource);
        new Thread(new SubRegionLoader(this, currentSource)).start();
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void setCountries() {

        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);
        fragments.clear();

        if(articles!=null) for (int i = 0; i < articles.size(); i++) {
            fragments.add(
                    ArticleFragment.newInstance(this, articles.get(i), i+1, articles.size()));
        }

        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);

    }

    // You need the 2 below to make the drawer-toggle work properly:

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

//    @Override
//    public void on


    // You need the below to open the drawer when the toggle is clicked
    // Same method is called when an options menu item is selected.

    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }
        if(item.hasSubMenu())
            itemTitle = item.getTitle().toString();
        else {
            String it = item.getTitle().toString();
            switch (itemTitle) {
                case "Topics":
                    currentCategory = it.equals("all")?"":it;
                    break;
                case "Languages":
                    currentLanguage = it.equals("all")?"":Utilities.nameToCode(getResources(), it, R.raw.language_codes, "languages");
                    break;
                case "Countries":
                    currentCountry = it.equals("all")?"":Utilities.nameToCode(getResources(), it, R.raw.country_codes, "countries");
                    break;
            }
            new Thread(new RegionLoader(this, 1, currentCategory, currentLanguage, currentCountry)).start();
        }
        Log.d(TAG, "onOptionsItemSelected: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: " + item.getTitle().toString());


//        updateMenu();
        return super.onOptionsItemSelected(item);
    }

    // You need this to set up the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        opt_menu = menu;
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
//////////////////////////////////////

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;


        MyPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }



        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }
}
