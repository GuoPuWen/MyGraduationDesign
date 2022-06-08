package com.example.opencvdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.databaseHelper.Database;
import com.example.databaseHelper.History;
import com.example.databaseHelper.MyHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {



    private ListView lv1;
    private final static String TAG = "HistoryActivity";     //全局日志TAG

//    private int[] imagesId={R.drawable.cat,R.drawable.monkey,R.drawable.rabbit,R.drawable.rat};
    private	String[] names={"短毛猫","猴子","兔子","老鼠"};
    private  String[] contents={"可爱","顽皮","温顺","伶俐"};
    private SQLiteDatabase database = null;     //数据库对象
    private List<History> historys = new ArrayList<>();



    public void init() {
        MyHelper myHelper = new MyHelper(this);
        database = myHelper.getWritableDatabase();
        Cursor rawQuery = database.rawQuery("select * from " + Database.TABLE_NAME, null);
        if(rawQuery != null) {
            while(rawQuery.moveToNext()) {
                String pUri = rawQuery.getString(rawQuery.getColumnIndex(Database.PURI));
                String sUri = rawQuery.getString(rawQuery.getColumnIndex(Database.SURI));
                String text = rawQuery.getString(rawQuery.getColumnIndex(Database.TEXT));
                Integer id = rawQuery.getInt(rawQuery.getColumnIndex(Database.ID));
                Long time = rawQuery.getLong(rawQuery.getColumnIndex(Database.TIME));
                History history = new History(sUri, pUri, text, time);
                history.setId(id);
                Log.i(TAG, history.toString());
                historys.add(history);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        init();
        getSupportActionBar().setTitle("文字识别系统-测试版本");
        lv1 = (ListView) findViewById(R.id.listView);

        this.registerForContextMenu(lv1);

        String TargetPath = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + "images";
        BaseAdapter adapter = new BaseAdapter() {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.list_item, parent, false);
                }

                ImageView face = (ImageView)convertView.findViewById(R.id.face);
                TextView name =(TextView)convertView.findViewById(R.id.name);
                TextView mark = (TextView)convertView.findViewById(R.id.mark);


                Log.i(TAG, historys.get(position).getsUri());
                Bitmap test = BitmapUtils.getBitmapFromPath(historys.get(position).getsUri());
                face.setImageBitmap(test);

//                face.setImageResource(imagesId[position]);
                name.setText(historys.get(position).getText());
                Date date = new Date(historys.get(position).getTime());
                String pattern = "MM月dd日HH时mm分";
                SimpleDateFormat df = new SimpleDateFormat(pattern);
                mark.setText(df.format(date));
                return convertView;
            }

            @Override
            public long getItemId(int position) {
                // TODO 自动生成的方法存根
                return position;
            }

            @Override
            public Object getItem(int position) {
                // TODO 自动生成的方法存根
                return historys.get(position);
            }

            @Override
            public int getCount() {
                // TODO 自动生成的方法存根
                return historys.size();
            }

        };///new BaseAdapter()

        lv1.setAdapter(adapter);
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                   @Override
                   public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                       // 输出参数view可知，其就是TextView，因此将起强转为TextView型，就可被打印
//                       TextView textView = (TextView) view.findViewById(R.id.name);
                       Toast.makeText(HistoryActivity.this, "点击第" + i + "个", Toast.LENGTH_SHORT).show();
                       Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
                       intent.putExtra("data", historys.get(i));
                       startActivity(intent);

                   }
        });
        String[] mItems = {"删除", "取消"};
        lv1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                final int position = historys.size() - pos - 1;
                Log.i(TAG, "pos为" + pos);
                Log.i(TAG, "position" + position);

                AlertDialog.Builder builder=new AlertDialog.Builder(HistoryActivity.this);
                builder.setMessage("确定删除?");
                builder.setTitle("提示");


                //添加AlertDialog.Builder对象的setPositiveButton()方法
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyHelper myHelper = new MyHelper(HistoryActivity.this);
                        database = myHelper.getWritableDatabase();
                           database.execSQL("delete from " + Database.TABLE_NAME+ " where   " + Database.ID + " = " + historys.get(position).getId());
                        historys.remove(position);
                        adapter.notifyDataSetChanged();
                    }

                });

                //添加AlertDialog.Builder对象的setNegativeButton()方法
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();



                return false;
            }
        });
    }
}