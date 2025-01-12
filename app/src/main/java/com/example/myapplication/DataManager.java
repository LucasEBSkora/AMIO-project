package com.example.myapplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import android.os.Handler;

public class DataManager {
    private Map<Object, Consumer<List<Measurement>>> callbacks;
    Handler handler;
    List<Measurement> measurements;
    static private DataManager instance = null;


    private DataManager() {
        callbacks = new HashMap<>();
        handler = new Handler();
    }

    static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void addListener(Object key, Consumer<List<Measurement>> callback) {
        callbacks.put(key, callback);
    }

    public void removeListener(Object key) {
        callbacks.remove(key);
    }

    public synchronized void updateData(List<Measurement> measurements) {
        this.measurements = measurements;
        for (Consumer<List<Measurement>> callback : callbacks.values()) {
            handler.post(()->{callback.accept(measurements);});
        }
    }

    public List<Measurement> getMeasurements() {
        return  measurements;
    }

}
