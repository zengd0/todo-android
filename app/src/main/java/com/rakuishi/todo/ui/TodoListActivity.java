package com.rakuishi.todo.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.rakuishi.todo.R;
import com.rakuishi.todo.bus.TodoEvent;
import com.rakuishi.todo.persistence.Todo;
import com.rakuishi.todo.persistence.TodoManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

public class TodoListActivity extends BaseActivity {

    public static final String TAG = TodoListActivity.class.getSimpleName();
    private TodoListAdapter adapter;
    private List<Todo> list = new ArrayList<>();
    @Inject TodoManager todoManager;
    @Inject Bus bus;

    @Bind(R.id.todo_list_listview) ListView listView;
    @Bind(R.id.todo_list_empty_view) TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);
        setContentView(R.layout.activity_todo_list);
        ButterKnife.bind(this);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        bus.register(this);

        list.addAll(todoManager.findAll());
        adapter = new TodoListAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyTextView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                todoManager.deleteCompleted();
                bus.post(new TodoEvent(TodoEvent.QUERY_DELETE));
                break;
//            case R.id.action_github:
//                IntentUtil.openUri(this, "https://github.com/rakuishi/Todo-Android/");
//                break;
//            case R.id.action_attributions:
//                IntentUtil.openUri(this, "https://github.com/rakuishi/Todo-Android/blob/master/ATTRIBUTIONS.md");
//                break;
//            case R.id.action_help:
//                IntentUtil.openUri(this, "https://github.com/rakuishi/Todo-Android/issues");
//                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.todo_list_add_imagebutton)
    void onClickInsertButton() {
        startActivity(TodoCreateActivity.createIntent(this));
    }

    @OnItemClick(R.id.todo_list_listview)
    void onItemClick(int position) {
        Todo todo = adapter.getItem(position);
        todoManager.update(todo, !todo.isCompleted());
        bus.post(new TodoEvent(TodoEvent.QUERY_UPDATE));
    }

    @OnItemLongClick(R.id.todo_list_listview)
    boolean onItemLongClick(int position, View view) {
        Todo todo = adapter.getItem(position);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Pair<View, String> pair =
                    new Pair<>(view.findViewById(R.id.item_todo_textview), getString(R.string.transition_name_todo_name));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pair);

            startActivity(TodoCreateActivity.createIntent(this, todo.getId()), options.toBundle());
        } else {
            startActivity(TodoCreateActivity.createIntent(this, todo.getId()));
        }

        return true;
    }

    @Subscribe
    public void onTodoEvent(TodoEvent event) {
        list.clear();
        list.addAll(todoManager.findAll());
        adapter.notifyDataSetChanged();
    }
}
