package com.example.firstapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity2 extends AppCompatActivity implements Runnable{

    private static final String TAG = "MainActivity2";
    EditText txt_dollar,txt_euro,txt_won;
    Handler handler;
    String today;  //新日期
    String old_date;  //旧日期
    ListAdapter adapter;
    RateManager rateManager = new RateManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent intent = getIntent();

        final ListView listView = (ListView)findViewById(R.id.mylist);

        //显示列表："one", "two", "three", "four"
        /*String data[] = {"one", "two", "three", "four"};
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);*/

        //使用extra获得数据
        /*double dollar2 = intent.getDoubleExtra("dollar_rate_key",0.0f);
        double euro2 = intent.getDoubleExtra("euro_rate_key",0.0f);
        double won2 = intent.getDoubleExtra("won_rate_key",0.0f);*/

        //使用bundle获得数据
        Bundle bundle = intent.getExtras();
        float dollar2 = bundle.getFloat("dollar_rate_key",0.1f);
        float euro2 = bundle.getFloat("euro_rate_key",0.1f);
        float won2 = bundle.getFloat("won_rate_key",0.1f);

        Log.i(TAG,"onCreate:dollar2=" + dollar2);
        Log.i(TAG,"onCreate:euro2=" + euro2);
        Log.i(TAG,"onCreate:won2=" + won2);

        txt_dollar = (EditText)findViewById(R.id.txt_dollar);
        txt_euro = (EditText)findViewById(R.id.txt_euro);
        txt_won = (EditText)findViewById(R.id.txt_won);

        txt_dollar.setText(String.valueOf(dollar2));
        txt_euro.setText(String.valueOf(euro2));
        txt_won.setText(String.valueOf(won2));

        // 每天更新一次汇率
        // 判断Myrate文件里面所存的日期与现在的日期对比，判断是否更新
        SharedPreferences sp = getSharedPreferences("Myrate", Activity.MODE_PRIVATE);
        old_date = sp.getString("date", "");  //旧日期
        Log.i(TAG, "the old date=" + old_date);
        Thread t = new Thread(this);
        t.start();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 5) {
                    List<String> list = (List<String>) msg.obj;
                    adapter = new ArrayAdapter<String>(MainActivity2.this, android.R.layout.simple_list_item_1, list);
                    listView.setAdapter(adapter);

                    /*if(list.contains("no data")){
                        listView.setAdapter(adapter);
                        listView.setEmptyView(findViewById(R.id.nodata));
                    }else{
                        adapter = new ArrayAdapter<String>(MainActivity2.this, android.R.layout.simple_list_item_1, list);
                        listView.setAdapter(adapter);
                    }*/

                    //String str = (String) msg.obj;
                    //Log.i(TAG, "handleMessage:getMessage meg = " + str);
                }
                super.handleMessage(msg);

            }
        };
    }
        /*//如果日期不匹配，则更新
        if(!ud.equals(dt)){
            Log.i(TAG,"onCreate:the new_date=" + dt);
            //开启子线程
            Thread t = new Thread(this);
            t.start();
            //线程间消息同步
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 5) {
                        List<String> list = (List<String>) msg.obj;
                        adapter = new ArrayAdapter<String>(MainActivity2.this, android.R.layout.simple_list_item_1, list);
                        listView.setAdapter(adapter);

                        //String str = (String) msg.obj;
                        //Log.i(TAG, "handleMessage:getMessage meg = " + str);
                    }
                    super.handleMessage(msg);

                }
            };
        }else {
            Log.i(TAG, "onCreate:the old_date=" + ud);
            Thread t = new Thread(this);
            t.start();
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 5) {
                        listView.setAdapter(adapter);
                        listView.setEmptyView(findViewById(R.id.nodata));
                    }
                    super.handleMessage(msg);

                }
            };
        }*/

    public void btn_save(View btn){
        if(btn.getId()==R.id.btn_save){
            Intent intent_save = getIntent();
            Bundle bdl = new Bundle();
            float newDollar = Float.parseFloat(txt_dollar.getText().toString());
            float newEuro = Float.parseFloat(txt_euro.getText().toString());
            float newWon = Float.parseFloat(txt_won.getText().toString());

            //将汇率存入Myrate文件里面
            SharedPreferences sp = getSharedPreferences("Myrate", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat("dollar_rate",newDollar);
            editor.putFloat("euro_rate",newEuro);
            editor.putFloat("won_rate",newWon);
            editor.apply();

            bdl.putFloat("dollar_rate_key",newDollar);
            bdl.putFloat("euro_rate_key",newEuro);
            bdl.putFloat("won_rate_key",newWon);
            intent_save.putExtras(bdl);

            //设置resultCode及带回的数据
            setResult(1,intent_save);
            //返回到调用页面
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.first_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu1){

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void run() {
        /*//获取msg对象，用于返回主线程
        Message msg = handler.obtainMessage(5);
        //msg.what = 5;
        msg.obj = "Hello from run()";
        handler.sendMessage(msg);*/
        Log.i(TAG,"run:run()......");
        List<String> list2 = new ArrayList<String>();
        Message msg = handler.obtainMessage(5);
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        today = format.format(date); //新日期
        if(today.equals(old_date)){
            Log.i(TAG,"run:日期相等,从数据库中获取数据");
            RateManager rateManager = new RateManager(MainActivity2.this);
            //从数据库中获取数据
            list2.add("从数据库中获取的数据：");
            for(RateItem rateItem :rateManager.listAll()){
                list2.add(rateItem.getCurname() + " ==> " + rateItem.getCurrate());
            }
            //list2.add("no data");
        }else{
            Log.i(TAG,"run:日期不等,从网络中获取数据");
            list2.add("从网络中获取的数据：");
            try{
                List<RateItem> ratelist = new ArrayList<RateItem>();
            /*url = new URL("https://www.usd-cny.com/bankofchina.htm");
            HttpsURLConnection http = (HttpsURLConnection)url.openConnection();
            InputStream in = http.getInputStream();
            String html = inputStream2String(in);
            Log.i(TAG, "run:html=" + html);
            Message msg = handler.obtainMessage(5);
            msg.obj = html;
            handler.sendMessage(msg);*/
                float dollar = 0, euro = 0, won = 0;
                //方法一：
                String url = "http://www.usd-cny.com/bankofchina.htm";
                Document doc = Jsoup.connect(url).get();
                Log.i(TAG, "run: "+ doc.title());
                Elements tables = doc.getElementsByTag("table");
                Element table0 = tables.get(0);
                // 获取 TD 中的数据
                Elements tds = table0.getElementsByTag("td");

                for(int i=0; i<tds.size(); i+=6){
                    Element td1 = tds.get(i);
                    Element td2 = tds.get(i + 5);
                    String str1 = td1.text();
                    String val = td2.text();
                    //Log.i(TAG, "run: " + str1 + "==>" + val);
                    String s = (String)(str1 + "==>" + val);
                    list2.add(s);
                    float v = 100f / Float.parseFloat(val);
                    float rate =(float)(Math.round(v*100))/100;

                    RateItem rateItem = new RateItem(str1, v);
                    //rateItem.setCurname(str1);
                    //rateItem.setCurrate(v);
                    ratelist.add(rateItem);
                    Log.i(TAG, "run: 成功添加数据 " + str1 + "==>" + v);

                    // 获取需要的数据并返回
                    if(str1.equals("美元")){
                        dollar = rate;
                        Log.i(TAG, "run: dollar_rate==>" + dollar);
                        txt_dollar.setText(String.valueOf(dollar));
                    }else if(str1.equals("欧元")){
                        euro = rate;
                        Log.i(TAG, "run: euro_rate==>" + euro);
                        txt_euro.setText(String.valueOf(euro));
                    }else if(str1.equals("韩元")){
                        won = rate;
                        Log.i(TAG, "run: won_rate==>" + won);
                        txt_won.setText(String.valueOf(won));
                    }
                }

                RateManager rm = new RateManager(MainActivity2.this);
                rm.deleteAll();
                Log.i("db","已删除所有记录");
                rm.addAll(ratelist);
                Log.i("db","已添加新记录集");
                //方法二：
            /*Element table = doc.getElementsByTag("table").first();
            Elements trs = table.getElementsByTag("tr");
            for(Element tr:trs){
                Elements tds = tr.getElementsByTag("td");
                if(tds.size>0){
                    //获取数据
                    String td1 = tds.get(0).text();
                    String td2 = tds.get(5).text();
                }
            }*/
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //更新记录日期
            SharedPreferences sp = getSharedPreferences("Myrate", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("date", today);
            edit.commit();
            Log.i("run","the new date=" + today);
        }
        msg.obj = list2;
        handler.sendMessage(msg);
    }
}