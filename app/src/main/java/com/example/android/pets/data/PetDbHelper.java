package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Created by Usuario on 13/05/2017.
 */

public class PetDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "PetDbHelper";

    //** Name of the database file */
    private static final String DATABASE_NAME = "shelter.db";

    /*
    Database version. If you change the database schema, you must
    increment the datebase version.
     */
    private static final int DATABASE_VERSION = 1;


    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is called for the first time
     * @param db is variable for database.
     */


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String thath contains the SQL statements to create
        // the pets table.
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + PetEntry.TABLE_NAME +
                " (" + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetEntry.COLUMN_PET_BREED + " TEXT, "
                + PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);

    }

    /**
     * This is called when the database needs to be upgraded.
     * @param db si variable for database
     * @param i will be a old version
     * @param i1 will be a new version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // The database is still at version 1, so there's
        // nothing to do be done here.

    }
}
