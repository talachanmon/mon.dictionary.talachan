package com.talachan.hv986;

import java.io.*;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static String databaseName;
    private static String databasePath;
    private static final int DATABASE_VERSION = 3;
    private SQLiteDatabase dataBase;
    private final Context dbContext;

    public DataBaseHelper(Context context, String DB_NAME, String PATH) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        databaseName = DB_NAME;
        databasePath = "/data/data/" + PATH + "/";
        dbContext = context;
        if (checkDataBase()) {
            openDataBase();
        } else {
            try {
                copyDataBase();
                this.close();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = dbContext.getAssets().open(databaseName);
        String outFileName = databasePath + databaseName;
        File file = new File(outFileName);
        OutputStream myOutput = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public SQLiteDatabase openDataBase() throws SQLException {
        String dbPath = databasePath + databaseName;
        dataBase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        return dataBase;
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        boolean exist = false;
        try {
            String dbPath = databasePath + databaseName;
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.v("santosh", "database error");
        }
        if (checkDB != null) {
            exist = true;
            checkDB.close();
        }
        return exist;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}