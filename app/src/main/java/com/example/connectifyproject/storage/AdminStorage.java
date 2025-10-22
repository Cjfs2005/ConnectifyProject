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
    private static final String DRAFTS_DIR = "tour_drafts";
    private final Gson gson = new GsonBuilder().create();

    public static class TourDraft {
        public String id; // UUID
        public String title;
        public String description;
        public String price;
        public String duration;
        public String date;
        public List<TourPlace> places = new ArrayList<>();
        public List<TourService> services = new ArrayList<>();
        public List<String> imageUris = new ArrayList<>();
        public long updatedAt; // epoch millis
    }

    private String getDraftFileName(String id) {
        return DRAFTS_DIR + "/" + id + ".json";
    }

    public String saveDraft(Context context, TourDraft draft) throws IOException {
        if (draft.id == null || draft.id.isEmpty()) {
            draft.id = java.util.UUID.randomUUID().toString();
        }
        draft.updatedAt = System.currentTimeMillis();

        String json = gson.toJson(draft);
        // Ensure directory exists
        java.io.File dir = new java.io.File(context.getFilesDir(), DRAFTS_DIR);
        if (!dir.exists()) dir.mkdirs();

        try (FileOutputStream fos = new FileOutputStream(new java.io.File(dir, draft.id + ".json"))) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
        }
        return draft.id;
    }

    public TourDraft loadDraft(Context context, String id) throws IOException {
        java.io.File dir = new java.io.File(context.getFilesDir(), DRAFTS_DIR);
        java.io.File file = new java.io.File(dir, id + ".json");
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return gson.fromJson(sb.toString(), TourDraft.class);
        }
    }

    public List<TourDraft> listDrafts(Context context) {
        List<TourDraft> list = new ArrayList<>();
        java.io.File dir = new java.io.File(context.getFilesDir(), DRAFTS_DIR);
        java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return list;
        for (java.io.File f : files) {
            try (FileInputStream fis = new FileInputStream(f);
                 InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                TourDraft draft = gson.fromJson(sb.toString(), TourDraft.class);
                if (draft != null) list.add(draft);
            } catch (IOException ignored) {}
        }
        // sort by updatedAt desc
        list.sort((a, b) -> Long.compare(b.updatedAt, a.updatedAt));
        return list;
    }

    public void deleteDraft(Context context, String id) {
        java.io.File dir = new java.io.File(context.getFilesDir(), DRAFTS_DIR);
        java.io.File file = new java.io.File(dir, id + ".json");
        if (file.exists()) file.delete();
    }

    public boolean hasAnyDraft(Context context) {
        java.io.File dir = new java.io.File(context.getFilesDir(), DRAFTS_DIR);
        java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        return files != null && files.length > 0;
    }
}
