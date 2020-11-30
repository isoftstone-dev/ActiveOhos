package com.activeharmony;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.activeharmony.copy.android.TextUtils;
import com.activeharmony.util.IOUtils;
import com.activeharmony.util.Log;
import com.activeharmony.util.NaturalOrderComparator;
import com.activeharmony.util.SQLiteUtils;
import com.activeharmony.util.SqlParser;
import ohos.app.Context;
import ohos.data.rdb.RdbStore;

public final class DatabaseHelper extends ohos.data.DatabaseHelper {
    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC CONSTANTS
    //////////////////////////////////////////////////////////////////////////////////////

    public final static String MIGRATION_PATH = "migrations";

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE FIELDS
    //////////////////////////////////////////////////////////////////////////////////////

    private final String mSqlParser;

    //////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    //////////////////////////////////////////////////////////////////////////////////////

    public DatabaseHelper(Context context, Configuration configuration) {
        super(context);

        // TODO 有错，后续改进
//        copyAttachedDatabase(configuration.getContext(), configuration.getDatabaseName());
        mSqlParser = configuration.getSqlParser();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // OVERRIDEN METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    //    @Override
    public void onOpen(RdbStore db) {
        executePragmas(db);
    }

    //    @Override
    public void onCreate(RdbStore db) {
        executePragmas(db);
        executeCreate(db);
        executeMigrations(db, -1, db.getVersion());
        executeCreateIndex(db);
    }

    //    @Override
    public void onUpgrade(RdbStore db, int oldVersion, int newVersion) {
        executePragmas(db);
        executeCreate(db);
        executeMigrations(db, oldVersion, newVersion);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    public void copyAttachedDatabase(Context context, String databaseName) {
//        final File dbPath = context.getDatabasePath(databaseName);
        final File dbPath = context.getDatabaseDir();

        // If the database already exists, return
        if (dbPath.exists()) {
            return;
        }

        // Make sure we have a path to the file
        dbPath.getParentFile().mkdirs();

        // Try to copy database file
        try {
//            final InputStream inputStream = context.getAssets().open(databaseName);
            final InputStream inputStream = new FileInputStream(databaseName);
            final OutputStream output = new FileOutputStream(dbPath);

            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer, 0, 8192)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("Failed to open file", e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    public void executePragmas(RdbStore db) {
//        if (SQLiteUtils.FOREIGN_KEYS_SUPPORTED) {
//            db.execSQL("PRAGMA foreign_keys=ON;");
//            Log.i("Foreign Keys supported. Enabling foreign key features.");
//        }
//		db.execSQL("PRAGMA foreign_keys=ON;");
        db.executeSql("PRAGMA foreign_keys=ON;");
        Log.i("Foreign Keys supported. Enabling foreign key features.");
    }

    public void executeCreateIndex(RdbStore db) {
        db.beginTransaction();
        try {
            for (TableInfo tableInfo : Cache.getTableInfos()) {
                String[] definitions = SQLiteUtils.createIndexDefinition(tableInfo);

                for (String definition : definitions) {
                    db.executeSql(definition);
                }
            }
            // TODO 后续改进
//            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void executeCreate(RdbStore db) {
        db.beginTransaction();
        try {
            for (TableInfo tableInfo : Cache.getTableInfos()) {
                db.executeSql(SQLiteUtils.createTableDefinition(tableInfo));
            }
            // TODO 后续改进
//            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public boolean executeMigrations(RdbStore db, int oldVersion, int newVersion) {
        boolean migrationExecuted = false;
        // TODO 后续改进
        /*try {
            final List<String> files = Arrays.asList(Cache.getContext().getAssets().list(MIGRATION_PATH));
            Collections.sort(files, new NaturalOrderComparator());

            db.beginTransaction();
            try {
                for (String file : files) {
                    try {
                        final int version = Integer.valueOf(file.replace(".sql", ""));

                        if (version > oldVersion && version <= newVersion) {
                            executeSqlScript(db, file);
                            migrationExecuted = true;

                            Log.i(file + " executed succesfully.");
                        }
                    } catch (NumberFormatException e) {
                        Log.w("Skipping invalidly named file: " + file, e);
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (IOException e) {
            Log.e("Failed to execute migrations.", e);
        }*/

        return migrationExecuted;
    }

    private void executeSqlScript(RdbStore db, String file) {

        InputStream stream = null;

        try {
            // TODO 后续改进
//            stream = Cache.getContext().getAssets().open(MIGRATION_PATH + "/" + file);

            if (Configuration.SQL_PARSER_DELIMITED.equalsIgnoreCase(mSqlParser)) {
                executeDelimitedSqlScript(db, stream);

            } else {
                executeLegacySqlScript(db, stream);

            }

        } catch (IOException e) {
            Log.e("Failed to execute " + file, e);

        } finally {
            IOUtils.closeQuietly(stream);

        }
    }

    private void executeDelimitedSqlScript(RdbStore db, InputStream stream) throws IOException {

        List<String> commands = SqlParser.parse(stream);

        for (String command : commands) {
            db.executeSql(command);
        }
    }

    private void executeLegacySqlScript(RdbStore db, InputStream stream) throws IOException {

        InputStreamReader reader = null;
        BufferedReader buffer = null;

        try {
            reader = new InputStreamReader(stream);
            buffer = new BufferedReader(reader);
            String line = null;

            while ((line = buffer.readLine()) != null) {
                line = line.replace(";", "").trim();
                if (!TextUtils.isEmpty(line)) {
                    db.executeSql(line);
                }
            }

        } finally {
            IOUtils.closeQuietly(buffer);
            IOUtils.closeQuietly(reader);

        }
    }
}
