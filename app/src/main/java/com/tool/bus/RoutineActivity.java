package com.tool.bus;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by haoyu on 15-4-20.
 */
public class RoutineActivity extends ActionBarActivity {
    public  List<HashMap<String,Object>> list = new ArrayList<>();
    public  SimpleAdapter simpleAdapter = null;
    public  int direction = 1 ;
    public  ListView listView;
    public  TextView way ,remark;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routine_result);
        listView = (ListView) findViewById(R.id.line_view);
        way = (TextView) findViewById(R.id.direction);
        simpleAdapter = new SimpleAdapter(this,
                getData(),
                R.layout.item,
                new String[] {"stop","position"},
                new int[]{R.id.stopName,R.id.postion});
        listView.setAdapter(simpleAdapter);

        Button switcher =(Button)findViewById(R.id.switcher);
        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (direction == 1) {
                    direction = 2;
                    way.setText("下行");
                } else {
                    direction = 1;
                    way.setText("上行");
                }
                list.clear();
                simpleAdapter = new SimpleAdapter(RoutineActivity.this,
                        getData(),
                        R.layout.item,
                        new String[] {"stop","position"},
                        new int[]{R.id.stopName,R.id.postion});
                listView.setAdapter(simpleAdapter);

            }
        });

    }


    public List<HashMap<String, Object>> getData(){

        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory().getAbsolutePath()+
                "/BusDB/RS",null);
        Intent intent = getIntent();
        String string = intent.getStringExtra("routineName");
        setTitle(string);
        Cursor cursordir = db.rawQuery("SELECT remark FROM line WHERE line_name = '" + string + "'", null);
        String remarkStr = null;
        while(cursordir.moveToNext()) {
            remarkStr = cursordir.getString(cursordir.getColumnIndex("remark"));
        }
        cursordir.close();
        remark = (TextView) findViewById(R.id.remark);
        remark.setText(remarkStr.replace("\\n","\n"));
        Cursor cursor = db.rawQuery("SELECT stop,position FROM line_stop WHERE line = '" + string + "' AND direction = " + direction + " ORDER BY position ASC", null);
        if (cursor.getCount() == 0){
            Toast.makeText(RoutineActivity.this,"线路不存在",Toast.LENGTH_SHORT).show();
        }
        else {
            while (cursor.moveToNext()) {
                String stop = cursor.getString(cursor.getColumnIndex("stop"));
                String position = cursor.getString(cursor.getColumnIndex("position"));
                HashMap<String, Object> map = new HashMap<>();
                map.put("stop", stop);
                map.put("position", position);
                list.add(map);
            }
        }
        cursor.close();
        if(db.isOpen()) {
            db.close();
        }
        return list;
    }
}
