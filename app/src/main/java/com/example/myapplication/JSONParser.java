package com.example.myapplication;

import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JSONParser {
    private final JsonReader reader;

    public JSONParser(InputStream stream) {
        reader = new JsonReader(new InputStreamReader(stream));
    }

    // https://developer.android.com/reference/android/util/JsonReader
    public List<Measurement> getMeasurements() {
        List<Measurement> measurements = new ArrayList<>();
        try {
            reader.beginObject();
            String field = reader.nextName();
            if (!Objects.equals(field, "data")) {
                Log.d("TP1", "expected array named data, got field " + field + " instead");
            }
            reader.beginArray();
            while (reader.hasNext()) {
                measurements.add(readMeasurement());
            }
            reader.endArray();
            reader.endObject();
        } catch (IOException ignored) {
        }

        return measurements;
    }

    private Measurement readMeasurement() {
        Measurement measurement = new Measurement();
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String field = reader.nextName();
                if (Objects.equals(field, "timestamp")) {
                    measurement.timestamp = reader.nextLong();
                } else if (Objects.equals(field, "label")) {
                    measurement.label = reader.nextString();
                } else if (Objects.equals(field, "value")) {
                    measurement.value = reader.nextDouble();
                } else if (Objects.equals(field, "mote")) {
                    measurement.mote = reader.nextString();
                } else {
                    Log.d("TP1", "unexpected field " + field + " in measurement");
                }
            }
            reader.endObject();
        } catch (IOException e) {
            Log.d("TP1", "exception when parsing measurement JSON object: " + e.getMessage());
            return null;
        }
        return measurement;
    }
}
