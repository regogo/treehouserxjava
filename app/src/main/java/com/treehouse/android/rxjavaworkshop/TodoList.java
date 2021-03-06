package com.treehouse.android.rxjavaworkshop;

import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;

public class TodoList implements Action1<Todo> {

    // replays all onNext() calls to any new subscriber
    ReplaySubject<TodoList> notifier = ReplaySubject.create();

    private List<Todo> todoList;

    public TodoList() {
        todoList = new ArrayList<>();
    }

    public TodoList(String json) {
        this();
        readJson(json);
    }

    public Observable<TodoList> asObservable() {
        return notifier;
    }

    public void add(Todo t) {
        todoList.add(t);
        notifier.onNext(this);
    }

    public void remove(Todo t) {
        todoList.remove(t);
        notifier.onNext(this);
    }

    @Override
    public void call(Todo todo) {
        toggle(todo);
    }

    private void toggle(Todo t) {
        Todo todo = todoList.get(todoList.indexOf(t));
        todo.isCompleted = !todo.isCompleted;
        notifier.onNext(this);
    }

    /**
     * Returns all items from this list in a List<>
     *
     * @return a List of all items
     */
    public List<Todo> getAll() {
        return todoList;
    }

    /**
     * Returns all items not marked as complete
     *
     * @return a List of items who's value for isComplete is false
     */
    public List<Todo> getIncomplete() {
        ArrayList<Todo> incomplete = new ArrayList<>();
        for (Todo t : todoList) {
            if (!t.isCompleted) {
                incomplete.add(t);
            }
        }

        return incomplete;
    }

    /**
     * Returns all completed items in the list
     *
     * @return a List of items who's value for isComplete is true
     */
    public List<Todo> getComplete() {
        ArrayList<Todo> complete = new ArrayList<>();
        for (Todo t : todoList) {
            if (t.isCompleted) {
                complete.add(t);
            }
        }

        return complete;
    }

    // read in the String as a json
    private void readJson(String json) {

        if (json == null || TextUtils.isEmpty(json.trim())) {
            return;
        }

        JsonReader reader = new JsonReader(new StringReader(json));

        try {
            reader.beginArray();

            while (reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
                reader.beginObject();

                String nameDesc = reader.nextName();
                if (!"description".equals(nameDesc)) {
                    Log.w(TodoList.class.getName(), "Expected 'description' but was " + nameDesc);
                }
                String description = reader.nextString();

                String nameComplete = reader.nextName();
                if (!"is_completed".equals(nameComplete)) {
                    Log.w(TodoList.class.getName(), "Expected 'is_completed' but was " + nameComplete);
                }
                boolean isComplete = reader.nextBoolean();

                add(new Todo(description, isComplete));

                reader.endObject();
            }

            reader.endArray();
        } catch (IOException e) {
            Log.i(TodoList.class.getName(), "Exception reading JSON " + e.getMessage());
        }

    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.beginArray();

            for (Todo t : todoList) {
                writer.beginObject();
                writer.name("description");
                writer.value(t.description);
                writer.name("is_completed");
                writer.value(t.isCompleted);
                writer.endObject();
            }

            writer.endArray();
            writer.close();
        } catch (IOException e) {
            Log.i(TodoList.class.getName(), "Exception writing JSON " + e.getMessage());
        }

        return new String(out.toByteArray());
    }

}
