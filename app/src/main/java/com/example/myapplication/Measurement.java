package com.example.myapplication;

import androidx.annotation.NonNull;

public class Measurement {
    public long timestamp;
    public  String label;
    public  double value;
    public  String mote;

    public  Measurement() {

    }
    public Measurement(long timestamp, String label, double value, String mote) {
        this.timestamp = timestamp;
        this.label = label;
        this.value = value;
        this.mote = mote;
    }

    @NonNull
    @Override
    public String toString() {
        return "Measurement{" +
                "timestamp=" + timestamp +
                ", label='" + label + '\'' +
                ", value=" + value +
                ", mote='" + mote + '\'' +
                '}';
    }
}
