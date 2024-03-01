package com.mnn.llm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NewPageActivity2 extends AppCompatActivity {
    private List<String> module1Items = new ArrayList<>();
    private ListView listViewModule1;
    private ArrayAdapter<String> adapterModule1;

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_MODULE1_ITEMS = "module1Items";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_page);

        listViewModule1 = findViewById(R.id.listview_module1);

        // 加载数据
        module1Items = loadItemsFromPrefs();

        // 设置适配器
        adapterModule1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, module1Items);
        listViewModule1.setAdapter(adapterModule1);

        // 设置加号按钮的点击事件监听器
        Button addButton = findViewById(R.id.btn_add_todo);
        if (addButton != null) {
            addButton.setOnClickListener(v -> showInputDialog());
        }

        // 添加长按删除功能
        listViewModule1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // 删除被长按的模块项
                String itemToRemove = module1Items.get(position);
                module1Items.remove(itemToRemove);

                // 通知适配器数据已更新
                adapterModule1.notifyDataSetChanged();

                // 保存新的数据到SharedPreferences
                saveItemsToPrefs(module1Items);

                Toast.makeText(NewPageActivity2.this, "已删除: " + itemToRemove, Toast.LENGTH_SHORT).show();
                return true; // 表示已处理长按事件
            }
        });
    }

    // 显示输入对话框的方法
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入文字");

        // 设置输入框
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // 设置对话框的按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            String text = input.getText().toString();
            module1Items.add(text);
            adapterModule1.notifyDataSetChanged();
            saveItemsToPrefs(module1Items);
        });
        builder.setNegativeButton("取消", null);

        // 显示对话框
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 保存数据到SharedPreferences的方法
    private void saveItemsToPrefs(List<String> items) {
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        String jsonString = new Gson().toJson(items);
        editor.putString(PREF_MODULE1_ITEMS, jsonString);
        editor.apply();
    }

    // 从SharedPreferences读取数据的方法
    private List<String> loadItemsFromPrefs() {
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String jsonString = sharedPrefs.getString(PREF_MODULE1_ITEMS, null);

        if (jsonString != null) {

            Type type = new TypeToken<List<String>>(){}.getType(); // 这里需要TypeToken类

            return new Gson().fromJson(jsonString, type);

        }

        return new ArrayList<>();
    }
}