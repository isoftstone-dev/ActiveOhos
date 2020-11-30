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

import java.util.Collection;

import com.activeharmony.copy.android.LruCache;
import com.activeharmony.serializer.TypeSerializer;
import com.activeharmony.util.Log;
import ohos.app.Context;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.StoreConfig;

public final class Cache {
    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC CONSTANTS
    //////////////////////////////////////////////////////////////////////////////////////

    public static final int DEFAULT_CACHE_SIZE = 1024;

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    //////////////////////////////////////////////////////////////////////////////////////

    private static Context sContext;

    private static ModelInfo sModelInfo;
    private static DatabaseHelper sDatabaseHelper;

    private static LruCache<String, Model> sEntities;

    private static boolean sIsInitialized = false;

    //////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    //////////////////////////////////////////////////////////////////////////////////////

    private Cache() {
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    public static synchronized void initialize(Configuration configuration) {
        if (sIsInitialized) {
            Log.v("ActiveAndroid already initialized.");
            return;
        }

        sContext = configuration.getContext();
        sModelInfo = new ModelInfo(configuration);
        sDatabaseHelper = new DatabaseHelper(sContext, configuration);

        // TODO: It would be nice to override sizeOf here and calculate the memory
        // actually used, however at this point it seems like the reflection
        // required would be too costly to be of any benefit. We'll just set a max
        // object size instead.
        sEntities = new LruCache<>(configuration.getCacheSize());

        openDatabase();

        sIsInitialized = true;

        Log.v("ActiveAndroid initialized successfully.");
    }

    public static synchronized void clear() {
        sEntities.evictAll();
        Log.v("Cache cleared.");
    }

    public static synchronized void dispose() {
        closeDatabase();

        sEntities = null;
        sModelInfo = null;
        sDatabaseHelper = null;

        sIsInitialized = false;

        Log.v("ActiveAndroid disposed. Call initialize to use library.");
    }

    // Database access

    public static boolean isInitialized() {
        return sIsInitialized;
    }

    private static final StoreConfig config = StoreConfig.newDefaultConfig("RdbStore.db");
    private static final RdbOpenCallback callback = new RdbOpenCallback() {
        @Override
        public void onCreate(RdbStore db) {
//            sDatabaseHelper.executePragmas(db);
//            sDatabaseHelper.executeCreate(db);
//            sDatabaseHelper.executeMigrations(db, -1, db.getVersion());
//            sDatabaseHelper.executeCreateIndex(db);
            db.executeSql("CREATE TABLE IF NOT EXISTS user (Id INTEGER PRIMARY KEY AUTOINCREMENT, addr TEXT, age INTEGER, userId INTEGER, userName TEXT);");
        }

        @Override
        public void onOpen(RdbStore db) {
            super.onOpen(db);
//            sDatabaseHelper.executePragmas(db);
        }


        @Override
        public void onUpgrade(RdbStore db, int oldVersion, int newVersion) {
//            sDatabaseHelper.executePragmas(db);
//            sDatabaseHelper.executeCreate(db);
//            sDatabaseHelper.executeMigrations(db, oldVersion, newVersion);
        }

    };

    public static synchronized RdbStore openDatabase() {
        return sDatabaseHelper.getRdbStore(config, 1, callback, null);
    }

    public static synchronized void closeDatabase() {
        sDatabaseHelper.getRdbStore(config, 1, callback, null).close();
    }

    // Context access
    public static Context getContext() {
        return sContext;
    }

    // Entity cache
    public static String getIdentifier(Class<? extends Model> type, Long id) {
        return getTableName(type) + "@" + id;
    }

    public static String getIdentifier(Model entity) {
        return getIdentifier(entity.getClass(), entity.getId());
    }

    public static synchronized void addEntity(Model entity) {
        sEntities.put(getIdentifier(entity), entity);
    }

    public static synchronized Model getEntity(Class<? extends Model> type, long id) {
        return sEntities.get(getIdentifier(type, id));
    }

    public static synchronized void removeEntity(Model entity) {
        sEntities.remove(getIdentifier(entity));
    }

    // Model cache
    public static synchronized Collection<TableInfo> getTableInfos() {
        return sModelInfo.getTableInfos();
    }

    public static synchronized TableInfo getTableInfo(Class<? extends Model> type) {
        return sModelInfo.getTableInfo(type);
    }

    public static synchronized TypeSerializer getParserForType(Class<?> type) {
        return sModelInfo.getTypeSerializer(type);
    }

    public static synchronized String getTableName(Class<? extends Model> type) {
        return sModelInfo.getTableInfo(type).getTableName();
    }
}
