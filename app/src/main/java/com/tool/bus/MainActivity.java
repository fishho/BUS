package com.tool.bus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private UpdataInfo info;
    private String localVersion;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        if(!isExist()){
            write();
        }


        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/BusDB/RS", null);
        Log.d("version", Integer.toString(db.getVersion()));
        //db.setVersion(2);;
        localVersion = Integer.toString(db.getVersion());
        CheckVersionTask cv = new CheckVersionTask();
        new Thread(cv).start();
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    //page 1
                    return new StopFragment();


                case 1:
                    //page 2
                    return new RoutineFragment();

                default:
                    //this page does not exists
                    return new SwitchFragment();
            }
        }


            @Override
            public int getCount () {
                // Show 3 total pages.
                return 3;
            }

            @Override
            public CharSequence getPageTitle ( int position){
                Locale l = Locale.getDefault();
                switch (position) {
                    case 0:
                        return getString(R.string.title_section1);
                    case 1:
                        return getString(R.string.title_section2);
                    case 2:
                        return getString(R.string.title_section3);
                }
                return null;
            }

        }

    public class CheckVersionTask implements Runnable {
        InputStream is;
        String versionol;
        public void run() {
            try {
                String path = getResources().getString(R.string.url_server);
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    // 从服务器获得一个输入流
                    is = conn.getInputStream();
                }
                info = UpdataInfoParser.getUpdataInfo(is);
                versionol = info.getVersion();

                if (info.getVersion().equals(localVersion)) {
                    Log.i("信息", "版本号相同");
                    // LoginMain();
                } else {
                    Log.i("信息", "版本号不相同 ");
                    ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo.State gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
                    NetworkInfo.State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
                    if(gprs == NetworkInfo.State.CONNECTED || gprs == NetworkInfo.State.CONNECTING){
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "数据有更新,连接WIFI自动更新", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    //判断为wifi状态下才加载广告，如果是GPRS手机网络则不加载！
                    if(wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                HttpDownloader downloader = new HttpDownloader();
                                int result = downloader.downFile("https://php-csorder.rhcloud.com/RS", "BusDB/", "RS");
                                Log.d("结果", Integer.toString(result));
                                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory().getAbsolutePath() +
                                        "/BusDB/RS", null);
                                if (result == 0) {
                                    db.setVersion(Integer.parseInt(info.getVersion()));
                                    Log.d("version", Integer.toString(db.getVersion()));
                                    Looper.prepare();
                                    Toast.makeText(getApplicationContext(), " 更新成功", Toast.LENGTH_SHORT).show();
                                    Looper.loop();


                                }

                            }
                        }).start();
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

        /**
         * A placeholder fragment containing a simple view.
         */

            String filePath = android.os.Environment.getExternalStorageDirectory()+"/BusDB";



            private boolean isExist(){
                File file = new File(filePath + "/database.db");
                if(file.exists()){
                    return true;
                }else{
                    return false;
                }
            }

            private void write(){
                InputStream inputStream;
                try {
                    inputStream = getApplication().getResources().getAssets().open("RS");
                    File file = new File(filePath);
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(filePath + "/RS");
                    byte[] buffer = new byte[512];
                    int count = 0;
                    while((count = inputStream.read(buffer)) > 0){
                        fileOutputStream.write(buffer, 0 ,count);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    inputStream.close();
                    System.out.println("success");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }





}
