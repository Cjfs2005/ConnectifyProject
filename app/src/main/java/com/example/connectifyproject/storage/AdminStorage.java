package com.example.connectifyproject.storage;

import android.content.Context;

import com.example.connectifyproject.models.TourPlace;
import com.example.connectifyproject.models.TourService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple storage helper for admin: stores a single tour draft as JSON in app-specific internal storage.
 */
public class AdminStorage {
    private static final String FILE_NAME = "tour_draft.json";
    private final Gson gson = new GsonBuilder().create();

    public static class TourDraft {
        public String title;
        public String description;
        public String price;
        public String duration;
        public String date;
        public List<TourPlace> places = new ArrayList<>();
        public List<TourService> services = new ArrayList<>();
    }

    public void saveDraft(Context context, TourDraft draft) throws IOException {
        String json = gson.toJson(draft);
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    public TourDraft loadDraft(Context context) throws IOException {
        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return gson.fromJson(sb.toString(), TourDraft.class);
        } catch (IOException e) {
            // No draft yet
            throw e;
        }
    }

    public boolean hasDraft(Context context) {
        try (FileInputStream ignored = context.openFileInput(FILE_NAME)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void clearDraft(Context context) {
        context.deleteFile(FILE_NAME);
    }
}
