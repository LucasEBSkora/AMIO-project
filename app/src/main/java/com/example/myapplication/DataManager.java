package com.example.myapplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import android.os.Handler;

public class DataManager {
    final private Map<Object, Consumer<SlidingWindow>> callbacks;
    SlidingWindow measurementsWindow;
    static private DataManager instance = null;


    private DataManager() {
        callbacks = new HashMap<>();
        measurementsWindow = new SlidingWindow(2);
    }

    static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void addListener(Object key, Consumer<SlidingWindow> callback) {
        callbacks.put(key, callback);
    }

    public void removeListener(Object key) {
        callbacks.remove(key);
    }

    public synchronized void updateData(List<Measurement> measurements) {
        measurementsWindow.addMeasurements(measurements);
        if (!measurementsWindow.windowFull()) return;
        for (Consumer<SlidingWindow> callback : callbacks.values()) {
            callback.accept(measurementsWindow);
        }
    }
}
