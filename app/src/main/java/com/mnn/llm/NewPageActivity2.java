package com.mnn.llm;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.widget.SearchView;
public class NewPageActivity2 extends AppCompatActivity {
    private static final String DATABASE_NAME = "MyDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Module1Items";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TEXT = "text";
    private SQLiteDatabase db;
    private List<String> module1Items;
    private ListView listViewModule1;
    private ArrayAdapter<String> adapterModule1;
    private SearchView searchView; // 假设你已经有了一个SearchView控件
    private int clickCount = 0;
    private String itemForDeletion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_page);

        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        listViewModule1 = findViewById(R.id.listview_module1);
        loadItemsFromDB();
        // 设置适配器
        adapterModule1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, module1Items);
        listViewModule1.setAdapter(adapterModule1);
        // 设置加号按钮的点击事件监听器
        Button addButton = findViewById(R.id.btn_add_todo);
        if (addButton != null) {
            addButton.setOnClickListener(v -> showInputDialog());
        }
        // 添加长按删除功能（只从列表中移除显示）
        Button viewAllDataButton = findViewById(R.id.btn_view_all_data);
        if (viewAllDataButton != null) {
            viewAllDataButton.setOnClickListener(v -> loadAllItemsFromDB());
        }

        listViewModule1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String itemToRemove = module1Items.get(position);
                module1Items.remove(itemToRemove);
                adapterModule1.notifyDataSetChanged();

                Toast.makeText(NewPageActivity2.this, "已取消显示: " + itemToRemove, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        listViewModule1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (clickCount == 0) {
                    itemForDeletion = module1Items.get(position);
                    Toast.makeText(NewPageActivity2.this, "连续点击两次以删除", Toast.LENGTH_SHORT).show();
                    clickCount++;
                } else if (clickCount == 1) {
                    clickCount++;
                    Toast.makeText(NewPageActivity2.this, "再次点击以确认删除", Toast.LENGTH_SHORT).show();
                } else if (clickCount == 2) {
                    deleteItemFromDB(itemForDeletion);
                    loadItemsFromDB(); // 更新列表显示
                    clickCount = 0; // 重置计数器
                }
            }
        });

        // 初始化并设置SearchView的查询监听器
        searchView = findViewById(R.id.search_view); // 替换成你的SearchView ID
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchInDB(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchInDB(newText);
                return true;
            }
        });
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入文字");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("确定", (dialog, which) -> {
            String text = input.getText().toString();
            addItemToDB(text);
            loadItemsFromDB(); // 更新列表显示
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addItemToDB(String text) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT, text);
        db.insert(TABLE_NAME, null, values);
    }

    private void loadItemsFromDB() {
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TEXT}, null, null, null, null, null);
        module1Items = new ArrayList<>();
        while (cursor.moveToNext()) {
            module1Items.add(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT)));
        }
        cursor.close();
    }

    private void searchInDB(String query) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_TEXT + " LIKE ?", new String[]{"%" + query + "%"});
        module1Items.clear();
        while (cursor.moveToNext()) {
            module1Items.add(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT)));
        }
        adapterModule1.notifyDataSetChanged();
        cursor.close();
    }
    private void deleteItemFromDB(String textToDelete) {
        db.delete(TABLE_NAME, COLUMN_TEXT + "=?", new String[]{textToDelete});
        Toast.makeText(this, "已从数据库中彻底删除: " + textToDelete, Toast.LENGTH_SHORT).show();
    }

// 加载数据库中所有数据并显示的方法
    private void loadAllItemsFromDB() {
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TEXT}, null, null, null, null, null);
        module1Items.clear();
        while (cursor.moveToNext()) {
            module1Items.add(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT)));
        }
        adapterModule1.notifyDataSetChanged();
        cursor.close();
        Toast.makeText(this, "加载了数据库中的所有数据", Toast.LENGTH_SHORT).show();
    }

    class MyDatabaseHelper extends SQLiteOpenHelper {
        public MyDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_TABLE_QUERY = "CREATE TABLE " + TABLE_NAME + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TEXT + " TEXT)";
            db.execSQL(CREATE_TABLE_QUERY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 在这里处理数据库升级逻辑
        }
    }
}