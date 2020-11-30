package com.activeharmony.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import android.content.ContentValues;
//import android.content.UriMatcher;
//import android.net.Uri;
//import android.util.SparseArray;

import com.activeharmony.ActiveAndroid;
import com.activeharmony.Cache;
import com.activeharmony.Configuration;
import com.activeharmony.Model;
import com.activeharmony.TableInfo;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.PathMatcher;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.RawRdbPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.utils.PlainArray;
import ohos.utils.net.Uri;


//public class ContentProvider /*extends android.content.ContentProvider*/ {
public class ContentProvider extends Ability {
    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE CONSTANTS
    //////////////////////////////////////////////////////////////////////////////////////

    private static final PathMatcher URI_MATCHER = new PathMatcher();
    private static final PlainArray<Class<? extends Model>> TYPE_CODES = new PlainArray<>();

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    //////////////////////////////////////////////////////////////////////////////////////

    private static String sAuthority;
    private static PlainArray<String> sMimeTypeCache = new PlainArray<>();

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    //    @Override
    public boolean onCreate() {
        ActiveAndroid.initialize(getConfiguration());
        sAuthority = getAuthority();

        final List<TableInfo> tableInfos = new ArrayList<TableInfo>(Cache.getTableInfos());
        final int size = tableInfos.size();
        for (int i = 0; i < size; i++) {
            final TableInfo tableInfo = tableInfos.get(i);
            final int tableKey = (i * 2) + 1;
            final int itemKey = (i * 2) + 2;

            // content://<authority>/<table>
            URI_MATCHER.addPath(sAuthority, tableKey);
            TYPE_CODES.put(tableKey, tableInfo.getType());

            // content://<authority>/<table>/<id>
            URI_MATCHER.addPath(sAuthority, itemKey);
            TYPE_CODES.put(itemKey, tableInfo.getType());
        }

        return true;
    }

    @Override
    public String getType(Uri uri) {
//        final int match = URI_MATCHER.match(uri);

//        String cachedMimeType = sMimeTypeCache.get(match);
//        if (cachedMimeType != null) {
//            return cachedMimeType;
//        }

        final Class<? extends Model> type = getModelType(uri);
//        final boolean single = ((match % 2) == 0);

        StringBuilder mimeType = new StringBuilder();
        mimeType.append("vnd");
        mimeType.append(".");
        mimeType.append(sAuthority);
        mimeType.append(".");
//        mimeType.append(single ? "item" : "dir");
        mimeType.append("/");
        mimeType.append("vnd");
        mimeType.append(".");
        mimeType.append(sAuthority);
        mimeType.append(".");
        mimeType.append(Cache.getTableName(type));

//        sMimeTypeCache.append(match, mimeType.toString());

        return mimeType.toString();
    }

    // SQLite methods

    @Override
    public int insert(Uri uri, ValuesBucket values) {
        final Class<? extends Model> type = getModelType(uri);
        final Long id = Cache.openDatabase().insert(Cache.getTableName(type), values);

        if (id != null && id > 0) {
            Uri retUri = createUri(type, id);
            notifyChange(retUri);

            return 0;
        }

        return -1;
    }

    @Override
    public int update(Uri uri, ValuesBucket values, DataAbilityPredicates predicates) {
        final Class<? extends Model> type = getModelType(uri);
        final int count = Cache.openDatabase().update(values, new RawRdbPredicates(
                Cache.getTableName(type),
                predicates.getWhereClause(),
                (String[]) predicates.getWhereArgs().toArray()));

        notifyChange(uri);

        return count;
    }

    @Override
    public int delete(Uri uri, DataAbilityPredicates predicates) {
        final Class<? extends Model> type = getModelType(uri);
        final int count = Cache.openDatabase().delete(new RawRdbPredicates(
                Cache.getTableName(type),
                predicates.getWhereClause(),
                (String[]) predicates.getWhereArgs().toArray()));

        notifyChange(uri);

        return count;
    }

    @Override
    public ResultSet query(Uri uri, String[] columns, DataAbilityPredicates predicates) {
        final Class<? extends Model> type = getModelType(uri);
//        final ResultSet cursor = Cache.openDatabase().query(
//                Cache.getTableName(type),
//                projection,
//                selection,
//                selectionArgs,
//                null,
//                null,
//                sortOrder);
        final ResultSet cursor = Cache.openDatabase().query(new RawRdbPredicates(
                Cache.getTableName(type),
                predicates.getWhereClause(),
                (String[]) predicates.getWhereArgs().toArray()), columns);

//        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    public static Uri createUri(Class<? extends Model> type, Long id) {
        final StringBuilder uri = new StringBuilder();
        uri.append("content://");
        uri.append(sAuthority);
        uri.append("/");
        uri.append(Cache.getTableName(type).toLowerCase());

        if (id != null) {
            uri.append("/");
            uri.append(id.toString());
        }

        return Uri.parse(uri.toString());
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PROTECTED METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    protected String getAuthority() {
        return "com.activeharmony.content.ContentProvider";
    }

    protected Configuration getConfiguration() {
        return new Configuration.Builder(getContext()).create();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    private Class<? extends Model> getModelType(Uri uri) {
//        final int code = URI_MATCHER.match(uri);
//        if (code != UriMatcher.NO_MATCH) {
//            return TYPE_CODES.get(code);
//        }

        return null;
    }


    private void notifyChange(Uri uri) {
        DataAbilityHelper.creator(Cache.getContext()).notifyChange(uri);
    }
}
