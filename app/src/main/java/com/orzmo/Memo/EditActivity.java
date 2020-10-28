package com.orzmo.Memo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.orzmo.Memo.core.Memo;
import com.orzmo.Memo.core.DatabaseHelper;
import com.wildma.pictureselector.PictureBean;
import com.wildma.pictureselector.PictureSelector;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EditActivity extends AppCompatActivity {
    private SQLiteDatabase db;
    private SharedPreferences sharedPreferences;
    private boolean isEdit = false;
    private String id;
    private ImageView imageView;
    private Bitmap bmp;
    //时间格式
    private Date date;
    private @SuppressLint("SimpleDateFormat")
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_page);

        //注册时间
        TextView time = findViewById(R.id.input_time);
        date = new Date();
        time.setText(date.toString());

        //连接数据库
        DatabaseHelper databaseHelper = new DatabaseHelper(this,"memo_db",null,1);
        this.db = databaseHelper.getWritableDatabase();

        //从前一个intent传回来的id
        this.id = String.valueOf(getIntent().getStringExtra("id"));

        //如果id是空就进入创建模式
        if (!this.id.equals("null")) {
            getData(this.id);
            this.isEdit = true;
        }

        //save按钮
        Button pushButton = (Button) findViewById(R.id.button_save);
        pushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMemo();
            }
        });
        //addImage按钮
        //imageView大小410 * 150dp
        imageView = findViewById(R.id.image_view);
        Button addImageButton = (Button) findViewById(R.id.button_img);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureSelector.create(EditActivity.this,PictureSelector.SELECT_REQUEST_CODE)
                        .selectPicture(true);
            }
        });

        EditText inputUsername = findViewById(R.id.input_username);

        //设置sharedPreferences
        this.sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username","");
        //设置username
        inputUsername.setText(username);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PictureSelector.SELECT_REQUEST_CODE) {
            if (data != null) {
                PictureBean pictureBean = data.getParcelableExtra(PictureSelector.PICTURE_RESULT);
                if (pictureBean.isCut()) {
                    bmp = BitmapFactory.decodeFile(pictureBean.getPath());
                    imageView.setImageBitmap(bmp);
                } else {
                    //没有裁剪的话bmp是空，注意
                    imageView.setImageURI(pictureBean.getUri());
                }

            }
        }
    }

    private void sendMemo() {
        EditText editTitle = findViewById(R.id.input_title);
        EditText editContent = findViewById(R.id.input_content);
        EditText editUsername = findViewById(R.id.input_username);

        //标题输入检查
        if (String.valueOf(editTitle.getText()).equals("") || String.valueOf(editContent.getText()).equals("")) {
            Toast toast = Toast.makeText(EditActivity.this, "标题或者内容为空", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        //用户名输入检查
        if (String.valueOf(editUsername.getText()).equals("")) {
            Toast toast = Toast.makeText(EditActivity.this, "作者为空", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        //sharedPreferences设置
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", String.valueOf(editUsername.getText()));
        editor.apply();

        //要插入的sql
        ContentValues values = new ContentValues();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        values.put("username", String.valueOf(editUsername.getText()));
        values.put("title", String.valueOf(editTitle.getText()));
        values.put("content", String.valueOf(editContent.getText()));
        values.put("datetime", formatter.format(date));

        //更新数据库
        if(this.isEdit){
            db.update("memo", values, "id = ?", new String[] { this.id });
        }else {
            this.db.insert("memo",null,values);
        }

        //提示
        Toast toast = Toast.makeText(EditActivity.this, "push succeeded!", Toast.LENGTH_LONG);
        toast.show();
        //跳转到主界面
        Intent intent = new Intent(EditActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void getData(String id) {
        @SuppressLint("Recycle") Cursor cursor = db.query("memo", new String[]{"title","content","datetime","username"}, "id=?", new String[] {id}, null, null, null);
        //利用游标遍历所有数据对象
        //为了显示全部，把所有对象连接起来，放到TextView中
        Memo memo = new Memo();
        while(cursor.moveToNext()){
            System.out.println(cursor.getString(cursor.getColumnIndex("title")));
            memo.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            memo.setContent(cursor.getString(cursor.getColumnIndex("content")));
            memo.setUsername(cursor.getString(cursor.getColumnIndex("username")));
            memo.setDatetime(cursor.getString(cursor.getColumnIndex("datetime")));
//            byte[] in = cursor.getBlob(cursor.getColumnIndex("img"));
//            memo.setImage(BitmapFactory.decodeByteArray(in,0,in.length));
        }
        EditText editTitle = findViewById(R.id.input_title);
        EditText editContent = findViewById(R.id.input_content);
        editTitle.setText(memo.getTitle());
        editContent.setText(memo.getContent());
//        imageView.setImageBitmap(memo.getImage());

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            new AlertDialog.Builder( this )
                    .setMessage( "confirm exit? all edition would be drop" )
                    .setNegativeButton( "cancel" ,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                }
                            })
                    .setPositiveButton( "confirm" ,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    Intent intent = new Intent(EditActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }).show();

            return true ;
        }
        return true;
    }
}
