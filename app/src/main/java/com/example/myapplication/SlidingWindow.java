package com.example.myapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlidingWindow {
    final int windowSize;
    List<Map<String, Measurement>> lastMeasurements;

    public SlidingWindow(int windowSize) {
        this.windowSize = windowSize;
        lastMeasurements = new ArrayList<>(windowSize);
    }

    public boolean windowFull() {
        return this.windowSize == lastMeasurements.size();
    }

    public void addMeasurements(List<Measurement> measurements) {
        if (lastMeasurements.size() == windowSize) {
            lastMeasurements.remove(0);
        }
        Map<String, Measurement> measurementMap = new HashMap<>();
        for (Measurement measurement : measurements) {
            measurementMap.put(measurement.mote, measurement);
        }

        lastMeasurements.add(measurementMap);
    }

    public Map<String, Measurement> get(int index) {
        return lastMeasurements.get(index);
    }

    public Map<String, Measurement> getMostRecent() {
        return lastMeasurements.get(lastMeasurements.size() - 1);
    }
}
