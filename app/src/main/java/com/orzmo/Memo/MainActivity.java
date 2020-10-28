package com.orzmo.Memo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.orzmo.Memo.core.Memo;
import com.orzmo.Memo.core.MemoAdapter;
import com.orzmo.Memo.core.DatabaseHelper;
import com.yalantis.phoenix.PullToRefreshView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {

    private List<Memo> memoList = new ArrayList<>();
    private SQLiteDatabase db;
    private ListView listView;
    private DatabaseHelper databaseHelper;
    private MemoAdapter memoAdapter;
    private PullToRefreshView mPullToRefreshView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                    }
                }, 2000);
                Toast.makeText(MainActivity.this, "refresh succeeded!", Toast.LENGTH_SHORT).show();
            }
        });
        //创建数据库连接
        databaseHelper = new DatabaseHelper(this,"memo_db",null,1);
        this.db = databaseHelper.getWritableDatabase();
        //悬浮按钮
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });
        //连接数据库把数据加载到List里面
        initDailys();

        //适配器设置
        memoAdapter = new MemoAdapter(MainActivity.this, R.layout.item, memoList);
        listView = (ListView) this.findViewById(R.id.list_view);
        listView.setAdapter(memoAdapter);

        //列表点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Memo memo = memoList.get(i);
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("id", String.valueOf(memo.getId()));
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(this);
        initView();
    }


    private void showDialog(final Memo memo, final int position) {
        final AlertDialog.Builder dialog =
                new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("confirm delete?");
        dialog.setPositiveButton("delete anyway",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = databaseHelper.getWritableDatabase();
                        db.delete("memo","id=?",new String[]{String.valueOf(memo.getId())});
                        db.close();
                        memoAdapter.removeItem(position, memoList);
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                memoAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
        dialog.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialog.show();
    }


    //初始化sharedPreferences
    private void initView() {
        SharedPreferences sharedPreferences= getSharedPreferences("data", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username","");
        if (username.equals("")) {
            username = "unchained";
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.apply();

    }

    //初始化,读取数据库
    private void initDailys() {
        this.memoList.clear();
        Cursor cursor = db.query("memo", new String[]{"title","content","datetime","username","id"}, null, null, null, null, "datetime desc");
        //利用游标遍历所有数据对象
        //为了显示全部，把所有对象连接起来，放到TextView中
        while(cursor.moveToNext()){
            Memo memo = new Memo();
            memo.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            memo.setContent(cursor.getString(cursor.getColumnIndex("content")));
            memo.setUsername(cursor.getString(cursor.getColumnIndex("username")));
            memo.setDatetime(cursor.getString(cursor.getColumnIndex("datetime")));
            memo.setId(cursor.getInt(cursor.getColumnIndex("id")));
            memoList.add(memo);
        }
        cursor.close();
        db.close();
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Memo memo = (Memo) listView.getItemAtPosition(position);
        showDialog(memo, position);
        return true;
    }
}
