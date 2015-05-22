package com.tool.bus;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by haoyu on 15-4-19.
 */
public class RoutineFragment extends Fragment {
    public ListView listView = null;
    public List<HashMap<String,Object>> list = new ArrayList<>();
    public SimpleAdapter simpleAdapter = null;
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.routine, container, false);
        return  rootView;
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView =(ListView)getActivity().findViewById(R.id.line1_view);
        listView.addFooterView(new View(getActivity()), null, true);
        final Button button = (Button) getActivity().findViewById(R.id.routine_query);
        final EditText routineText = (EditText)getActivity().findViewById(R.id.routine);
        routineText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    button.performClick();
                return  true;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = routineText.getText().toString();
                if (string.equals("")) {
                    Toast.makeText(getActivity(), "请输入线路名", Toast.LENGTH_SHORT).show();
                } else {
                    list.clear();
                    simpleAdapter = new SimpleAdapter(getActivity().getApplicationContext(),
                            getdata(),
                            R.layout.routine_item,
                            new String[]{"line_name"},
                            new int[]{R.id.lineforline});
                    listView.setAdapter(simpleAdapter);
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(position);
                String line_name = map.get("line_name");
                Intent intent = new Intent(getActivity(), RoutineActivity.class);
                intent.putExtra("routineName", line_name);
                startActivity(intent);
            }
        });
    }

    public List<HashMap<String,Object>> getdata() {
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory().getAbsolutePath()+
                "/BusDB/RS",null);
        EditText routineText = (EditText) getActivity().findViewById(R.id.routine);
        String string = routineText.getText().toString();
        Cursor cursor = db.rawQuery("SELECT line_name FROM line WHERE line_name LIKE '%" + string + "%'", null);
        if (cursor.getCount() == 0)
        {
            Toast.makeText(getActivity(),"查找不到",Toast.LENGTH_SHORT).show();
        }
        else {
            while (cursor.moveToNext()){
                String line_name = cursor.getString(cursor.getColumnIndex("line_name"));
                HashMap<String,Object> map = new HashMap<>();
                map.put("line_name",line_name);
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
