package com.example.haoji.dailyActivity;

import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.support.design.widget.NavigationView;
import com.example.haoji.Button.DragFloatActionButton;

import com.example.haoji.GlobalVariable;
import com.example.haoji.R;
import com.example.haoji.userActivity.RegisterActivity;
import com.example.haoji.userActivity.login1Activity;
import com.example.haoji.Button.SectorMenuButton;
import com.example.haoji.Button.ButtonData;
import com.example.haoji.Button.ButtonEventListener;
import com.example.haoji.userActivity.showinfoActivity;
import com.example.haoji.dailyActivity.read_userName;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class dailyActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String imgPath;
    private Handler handler;
    private static final int IMAGE = 1;
    private GlobalVariable app;
    private TextView textView;
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sidebar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initBottomSectorMenuButton();
        //Handler处理子进程获取的数据
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String val = data.getString("value");
                System.out.println(val);
                Intent intent = new Intent(dailyActivity.this ,newPlan.class);
                intent.putExtra("txt", val);
                startActivity(intent);
            }
        };
        //DragFloatActionButton addSchedule = (DragFloatActionButton) findViewById(R.id.addSchedule);
        //addSchedule.setOnClickListener(new View.OnClickListener() {
        //@Override
        //public void onClick(View view) {
        // Intent intent = new Intent(dailyActivity.this ,newPlan.class);
        //startActivity(intent);
        // }
        // });
        FragmentChat chat = new FragmentChat();
        getSupportFragmentManager().beginTransaction().replace(R.id.fg, chat).commit();
        OnedayFragmentRecyclerview onedaylist = new  OnedayFragmentRecyclerview();
        getSupportFragmentManager().beginTransaction().replace(R.id.fg2, onedaylist).commit();
        RadioGroup myTabRg = (RadioGroup) findViewById(R.id.tab_menu);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        app = (GlobalVariable) getApplication();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        textView = (TextView) headerView.findViewById(R.id.userNameSider);
        if(app.getState()==0)
            textView.setText("未登陆");
        else
        textView.setText(app.getUserName());

        myTabRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                switch (checkedId) {
                    case R.id.rbOneday:
                        FragmentChat chat = new FragmentChat();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fg, chat).commit();
                        OnedayFragmentRecyclerview onedaylist = new  OnedayFragmentRecyclerview();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fg2, onedaylist).commit();
                        break;
                    case R.id.rbThreeDay:
                        FragmentThreeDay threeday=new FragmentThreeDay();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fg,threeday).commit();
                        ThreedayFragmentRecyclerview threedaylist = new  ThreedayFragmentRecyclerview();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fg2, threedaylist).commit();
                        break;
                    case R.id.rbWeek:
                        FragmentWeek week = new FragmentWeek();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fg,week).commit();
                        WeekFragmentRecyclerview weeklist = new  WeekFragmentRecyclerview();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fg2, weeklist).commit();
                        break;

                    case R.id.rbMonth:
                        FragmentMonth month = new FragmentMonth();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fg, month)
                                .commit();
                        break;
                    default:
                        break;
                }

            }
        });
    }


    private void initBottomSectorMenuButton() {
        SectorMenuButton sectorMenuButton = (SectorMenuButton) findViewById(R.id.bottom_sector_menu);
        final List<ButtonData> buttonDatas = new ArrayList<>();
        int[] drawable = {R.mipmap.add, R.mipmap.speak,
                R.mipmap.dynamic, R.mipmap.text};
        for (int i = 0; i < 4; i++) {
            ButtonData buttonData = ButtonData.buildIconButton(this, drawable[i], 0);
            buttonData.setBackgroundColorId(this, R.color.colorAccent);
            buttonDatas.add(buttonData);
        }
        sectorMenuButton.setButtonDatas(buttonDatas);
        setListener(sectorMenuButton);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            imgPath = c.getString(columnIndex);
            c.close();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(imgPath);
                    RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"),file);
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("apikey","b5a900dce088957")
                            .addFormDataPart("file","image.jpg",fileBody)
                            .addFormDataPart("language","chs")
                            .build();
                    Request request = new Request.Builder()
                            .url("https://api.ocr.space/parse/image")
                            .post(requestBody)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        String jsonString = response.body().string();
                        try {
                            JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("ParsedResults");
                            String jsonString1 = jsonArray.getString(0);
                            String jsonString2 = new JSONObject(jsonString1).getString("ParsedText");
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            data.putString("value", jsonString2);
                            msg.setData(data);
                            handler.sendMessage(msg);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }catch(IOException ex){
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void setListener(final SectorMenuButton button) {
        button.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                int buttonid = index;
                if (buttonid == 3) {
                    Intent intent = new Intent(dailyActivity.this ,newPlan.class);
                    startActivity(intent);
                }
                if (buttonid == 2) {
                    //调用相册
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, IMAGE);
                }
            }

            @Override
            public void onExpand() {
            }

            @Override
            public void onCollapse() {
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(dailyActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        app = (GlobalVariable) getApplication();
        int id = item.getItemId();
        if (id == R.id.info) {
            if(app.getState()==0){//未登录状态
                Intent intent = new Intent(dailyActivity.this ,login1Activity.class);
                startActivity(intent);
            }
            else{//已登录状态
                Intent intent = new Intent(dailyActivity.this ,showinfoActivity.class);
                startActivity(intent);
            }

        } else if (id == R.id.analyse) {

        } else if (id == R.id.refresh) {

        } else if (id == R.id.setting) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}