package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;


/**
 * Created by Usuario on 21/05/2017.
 */

public class PetProvider extends ContentProvider {


    //** Tag for the log messages */
    private static final String TAG = "PetProvider";

    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It´s common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUrimatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUrimatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUrimatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /* Database helper object */
    private PetDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object
     * @return
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given Uri. Use the given projection, selection,
     * selection arguments, and sort order.
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUrimatcher.match(uri);
        switch (match){
            case PETS:
                cursor = db.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                selection = PetContract.PetEntry._ID + "+?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     * @param uri
     * @return
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * Insert new data into the provider with the given ContentValues
     * @param uri
     * @param contentValues
     * @return
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUrimatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null){
            throw new IllegalArgumentException("Pet requires a name");
        }
        // Check that the gender is not null or not is valid
        Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }
        // Check that the weight is gretter or equal to 0.
        Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0){
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        // No need check the breed. Null is valid too.

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(PetContract.PetEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1){
            Log.e("LogTag", "Failed to insert row for " + uri);
            return null;
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)  {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUrimatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that the selection and selection args
                return database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not suported for " + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection argumens, with the new ContentValues.
     * @param uri
     * @param contentValues
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUrimatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know wich row to update. Selection will be "_id=?" and
                // selection arguments will be a String array containing the actual ID.
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * especified in the selection and selection arguments (wich could be 0 or 1 or more pets).
     * Return the number of rows that were sucessfully updated.
     */
    private int updatePet (Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the COLUM_PET_NAME key is present, check that the name value is not null.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if (name == null) {throw new IllegalArgumentException("Pet requires name");}
        }

        // If the COLUM_PET_GENDER key is present, check that the gender value is invalid.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the COLUMN_PET_WEIGHT key is present, check that the weight value is valid.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) { throw new IllegalArgumentException("Pet requires valid weight"); }
        }

        // No need to check the breed, any value is valid (including null)

        //If there are no values to update, then don't try to update the database
        if (values.size() == 0) { return 0; }



        // Get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        return database.update(PetContract.PetEntry.TABLE_NAME, values, selection, selectionArgs);

    }

}
