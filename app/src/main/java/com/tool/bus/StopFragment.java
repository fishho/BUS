package com.tool.bus;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
 * Created by haoyu on 15-5-2.
 */
public class StopFragment extends Fragment {
    public ListView listView = null;
    public List<HashMap<String,Object>> list = new ArrayList<>();
    public SimpleAdapter simpleAdapter = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.stop, container, false);
        return rootView;
    }
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        listView = (ListView)getActivity().findViewById(R.id.stop_view);
        listView.addFooterView(new View(getActivity()), null, true);
        final  EditText  stopText = (EditText)getActivity().findViewById(R.id.stop);
        final  Button button = (Button)getActivity().findViewById(R.id.stop_query);
        stopText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    button.performClick();
                    return true;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = stopText.getText().toString();
                if (string.equals("")){
                    Toast.makeText(getActivity(),"请输入站点名",Toast.LENGTH_SHORT).show();
                }
                else {
                    list.clear();
                    simpleAdapter = new SimpleAdapter(getActivity().getApplicationContext(),
                            getdata(),
                            R.layout.line_item,
                            new String[]{"line", "direction"},
                            new int[]{R.id.line, R.id.head});
                    listView.setAdapter(simpleAdapter);
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(position);
                String line = map.get("line");
                Intent intent = new Intent(getActivity(), RoutineActivity.class);
                intent.putExtra("routineName", line);
                startActivity(intent);
                //Toast.makeText(getActivity(),line_name,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public List<HashMap<String,Object>> getdata() {
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory().getAbsolutePath()+
                "/BusDB/RS",null);
        EditText stopText = (EditText)getActivity().findViewById(R.id.stop);
        String string = stopText.getText().toString();
        Cursor cursor = db.rawQuery("SELECT line.line_name as line,direction,stop1,stop2,stop FROM line_stop join line on line_stop.line = line.line_name WHERE stop LIKE'" +string+"%'",null);
        if (cursor.getCount() == 0)
        {
            Toast.makeText(getActivity(),"站点不存在",Toast.LENGTH_SHORT).show();
        }
        else{
            while (cursor.moveToNext()){
              //Integer id = cursor.getInt(cursor.getColumnIndex("id"));
                Integer direction = cursor.getInt(cursor.getColumnIndex("direction"));
                String line = cursor.getString(cursor.getColumnIndex("line"));
                String stopb = cursor.getString(cursor.getColumnIndex("stop"));
                String stop1 = cursor.getString(cursor.getColumnIndex("stop1"));
                String stop2 = cursor.getString(cursor.getColumnIndex("stop2"));
                String stop ;
                if(direction == 1){
                    stop= "往"+stop1+" 经过 "+stopb;
                }
                else{
                stop = "往"+stop2+" 经过 "+stopb;
                }
                HashMap<String,Object> map = new HashMap<>();
                //map.put("id", id);
                map.put("direction",stop);
                map.put("line", line);
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