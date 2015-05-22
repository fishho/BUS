package com.tool.bus;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;;

/**
 * Created by haoyu on 15-5-2.
 */
public class SwitchFragment extends Fragment {
    public static  final String CREATE_VIEW = "CREATE VIEW IF NOT EXISTS  ROUTE AS"
            +" SELECT ls1.stop as start,ls2.stop as end,ls1.line as line,"
            + "ls1.direction as direction,ls2.position-ls1.position as count"
            +" FROM line_stop ls1,line_stop ls2"
            +" WHERE ls1.line = ls2.line AND ls1.direction = ls2.direction AND ls1.position < ls2.position";
    public ListView listView = null;
    public List<HashMap<String,Object>> list = new ArrayList<>();
    public SimpleAdapter simpleAdapter = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.switcher, container, false);
        return rootView;
    }
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        listView = (ListView)getActivity().findViewById(R.id.switch_view);
        EditText endText = (EditText)getActivity().findViewById(R.id.end);
        endText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Button button1 = (Button)getActivity().findViewById(R.id.switch_query);
                button1.performClick();
                return  true;
            }
        });
        Button button = (Button)getActivity().findViewById(R.id.switch_query);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText startText = (EditText)getActivity().findViewById(R.id.start);
                String startString = startText.getText().toString();
                EditText endText = (EditText)getActivity().findViewById(R.id.end);
                String endString = endText.getText().toString();
                if (startString.equals("") ||endString.equals("")){
                    Toast.makeText(getActivity(),"请输入起止站点名",Toast.LENGTH_SHORT).show();
                }
                else {
                    list.clear();
                    simpleAdapter = new SimpleAdapter(getActivity().getApplicationContext(),
                            getdata(),
                            R.layout.switch_item,
                            new String[] {"description"},
                            new int[]{R.id.scheme});
                    listView.setAdapter(simpleAdapter);

                }

            }
        });
    }

    public List<HashMap<String, Object>> getdata() {
        String sqlStr1,sqlStr2,sqlStr3,description;
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory().getAbsolutePath()+
                "/BusDB/RS",null);
        db.execSQL(CREATE_VIEW);
        EditText startText = (EditText)getActivity().findViewById(R.id.start);
        EditText endText = (EditText)getActivity().findViewById(R.id.end);
        String startStr = startText.getText().toString();
        String endStr = endText.getText().toString();
        sqlStr1 = "SELECT ls1.stop as begin,ls2.stop as termination,ls1.line as routine," +
                "ls1.direction as direction,ls2.position-ls1.position as countall " +
                "FROM line_stop ls1,line_stop ls2 " +
                "WHERE ls1.line = ls2.line AND ls1.direction = ls2.direction AND " +
                "ls1.position<ls2.position AND ls1.stop LIKE '" + startStr+"%' AND " +
                "ls2.stop LIKE '"+ endStr+"%'";
        sqlStr2 = "SELECT rv1.start as begin,rv1.line as routine1,rv1.direction as direction1," +
                "rv1.end as switcher,rv2.line as routine2 ,rv2.direction as direction2,rv2.end as termination," +
                "rv1.count + rv2.count as countall FROM route rv1,route rv2 WHERE rv1.start LIKE '"+
                startStr+"%' AND rv1.end = rv2.start AND rv2.end LIKE '"+endStr+"%'";
        sqlStr3 = "SELECT rv1.start as begin,rv1.line as routine1,rv1.direction as direction1, " +
                "rv1.end as switcher1,rv2.line as routine2 ,rv2.direction as direction2,rv2.end as switcher2, " +
                "rv3.line as routine3, rv3.direction as direction3, rv3.end as termination, " +
                "rv1.count + rv2.count + rv3.count as countall FROM route rv1,route rv2,route rv3 " +
                "WHERE rv1.start LIKE '"+ startStr+"%' AND rv1.end = rv2.start AND rv2.end = rv3.start  " +
                "AND rv3.end LIKE '"+endStr+"%'";
        Cursor cursor = db.rawQuery(sqlStr1,null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String begin = cursor.getString(cursor.getColumnIndex("begin"));
                String routine = cursor.getString(cursor.getColumnIndex("routine"));
                Integer direction = cursor.getInt(cursor.getColumnIndex("direction"));
                Integer countall = cursor.getInt(cursor.getColumnIndex("countall"));
                String termination = cursor.getString(cursor.getColumnIndex("termination"));
                Cursor cursordir = db.rawQuery("SELECT stop" + direction + " FROM line WHERE line_name = '" + routine + "'", null);
                String dirString = null;
                while(cursordir.moveToNext()){
                       dirString = cursordir.getString(cursordir.getColumnIndex("stop"+direction));
                }
                cursordir.close();
                dirString = "往"+dirString+"方向";
                description = "在"+begin+" 乘坐 " + routine + dirString+"，直达" + termination + "，共经过" + countall+"站";
                HashMap<String, Object> map = new HashMap<>();
                map.put("description", description);
                list.add(map);
            }
        }
        else{
            cursor = db.rawQuery(sqlStr2,null);
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    String begin = cursor.getString(cursor.getColumnIndex("begin"));
                    String routine1 = cursor.getString(cursor.getColumnIndex("routine1"));
                    Integer direction1 = cursor.getInt(cursor.getColumnIndex("direction1"));
                    String switcher = cursor.getString(cursor.getColumnIndex("switcher"));
                    String routine2 = cursor.getString(cursor.getColumnIndex("routine2"));
                    Integer direction2 = cursor.getInt(cursor.getColumnIndex("direction2"));
                    Integer countall = cursor.getInt(cursor.getColumnIndex("countall"));
                    String termination = cursor.getString(cursor.getColumnIndex("termination"));
                    Cursor cursordir = db.rawQuery("SELECT stop" + direction1 + " FROM line WHERE line_name = '" + routine1 + "'", null);
                    String dirString1 = null;
                    while(cursordir.moveToNext()){
                        dirString1 = cursordir.getString(cursordir.getColumnIndex("stop"+direction1));
                    }
                    cursordir.close();
                    dirString1 = "往"+dirString1+"方向";
                    Cursor cursordir1 = db.rawQuery("SELECT stop" + direction2 + " FROM line WHERE line_name = '" + routine2 + "'", null);
                    String dirString2 = null;
                    while(cursordir1.moveToNext()){
                        dirString2 = cursordir1.getString(cursordir1.getColumnIndex("stop"+direction2));
                    }
                    cursordir1.close();
                    dirString2 = "往"+dirString2+"方向";

                    description = "在"+begin+" 乘坐 " + routine1 + dirString1 + "，到" + switcher + "，换乘 " + routine2 + dirString2 + "，到" +
                            termination + "，共经过" + countall + "站";
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("description", description);
                    list.add(map);
                }
            }
            else {
                cursor = db.rawQuery(sqlStr3,null);
            }
        }
        if (cursor.getCount() == 0){
            Toast.makeText(getActivity(),"无法到达",Toast.LENGTH_SHORT).show();
        }
        else {
            while (cursor.moveToNext()) {
                String begin = cursor.getString(cursor.getColumnIndex("begin"));
                String routine1 = cursor.getString(cursor.getColumnIndex("routine1"));
                Integer direction1 = cursor.getInt(cursor.getColumnIndex("direction1"));
                String switcher1 = cursor.getString(cursor.getColumnIndex("switcher1"));
                String routine2 = cursor.getString(cursor.getColumnIndex("routine2"));
                Integer direction2 = cursor.getInt(cursor.getColumnIndex("direction2"));
                String switcher2 = cursor.getString(cursor.getColumnIndex("switcher2"));
                String routine3 = cursor.getString(cursor.getColumnIndex("routine3"));
                Integer direction3 = cursor.getInt(cursor.getColumnIndex("direction3"));
                Integer countall = cursor.getInt(cursor.getColumnIndex("countall"));
                String termination = cursor.getString(cursor.getColumnIndex("termination"));
                Cursor cursordir = db.rawQuery("SELECT stop" + direction1 + " FROM line WHERE line_name = '" + routine1 + "'", null);
                String dirString1 = null;
                while(cursordir.moveToNext()){
                    dirString1 = cursordir.getString(cursordir.getColumnIndex("stop"+direction1));
                }
                cursordir.close();
                dirString1 = "往"+dirString1+"方向";
                Cursor cursordir1 = db.rawQuery("SELECT stop" + direction2 + " FROM line WHERE line_name = '" + routine2 + "'", null);
                String dirString2 = null;
                while(cursordir1.moveToNext()){
                    dirString2 = cursordir1.getString(cursordir1.getColumnIndex("stop"+direction2));
                }
                cursordir1.close();
                dirString2 = "往"+dirString2+"方向";
                Cursor cursordir2 = db.rawQuery("SELECT stop" + direction3 + " FROM line WHERE line_name = '" + routine3 + "'", null);
                String dirString3 = null;
                while(cursordir2.moveToNext()){
                    dirString3 = cursordir2.getString(cursordir2.getColumnIndex("stop"+direction3));
                }
                cursordir2.close();
                dirString3 = "往"+dirString3+"方向";

                description = "在"+begin+" 乘坐 " + routine1 + dirString1 + "，到" + switcher1 + "，换乘 " + routine2 +
                        dirString2 + "，到" +switcher2+"，换乘" +routine3+dirString3+"，到"+
                        termination + "，共经过" + countall + "站";
                HashMap<String, Object> map = new HashMap<>();
                map.put("description", description);
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
