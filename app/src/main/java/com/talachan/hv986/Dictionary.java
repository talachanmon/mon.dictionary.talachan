package com.talachan.hv986;

import java.util.LinkedHashSet;
import java.util.Set;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Dictionary {
    public SQLiteDatabase database;

    public Dictionary(SQLiteDatabase database) {
        this.database = database;
    }

    // Generating Random query from Database
    public String getRandomWord() {
        Cursor records = database.rawQuery(
                "select * from eng2mon where rowid = (abs(random()) % (select max(rowid)+1 from eng2mon))", null);
        records.moveToFirst();
        int word_index = records.getColumnIndex("eng_word");
        String word = records.getString(word_index);
        return word;
    }

    // Getting Meaning for Input query
    public String getMeaning(String query) {
        try {
            Cursor meaningOfWordComponentQuery = database.rawQuery("Select * from eng2mon where eng_word='" + query + "'"
                    + "COLLATE NOCASE", null);
            boolean recordsExist = meaningOfWordComponentQuery.moveToFirst();
            if (recordsExist) {
                int index = meaningOfWordComponentQuery.getColumnIndex("meaning");
                String meaning = meaningOfWordComponentQuery.getString(index);
                return meaning;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void storeRecentWord(SharedPreferences recentQueries, String word) {
        String meaning = getMeaning(word);
        if (meaning != null) {
            Set recents = recentQueries.getStringSet("recentValues", new LinkedHashSet<String>());
            recents.add(word);
            SharedPreferences.Editor recentEditor = recentQueries.edit();
            recentEditor.putStringSet("recentValues", recents);
            recentEditor.commit();
        }
    }
}
