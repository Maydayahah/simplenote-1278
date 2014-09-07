package com.automattic.simplenote.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.automattic.simplenote.R;
import com.automattic.simplenote.Simplenote;
import com.automattic.simplenote.models.Note;
import com.automattic.simplenote.models.Tag;
import com.automattic.simplenote.utils.TagsAdapter;
import com.simperium.client.Bucket;

import java.util.List;

/**
 * Created by richard on 8/30/14.
 */
public class SimpleNoteWidgetProvider extends AppWidgetProvider{

    private static final String TAG = "WidgetProvider";

    /**
     * Intent with this action is broadcast whenever the foward button is tapped.
     */
    public static final String ACTION_FORWARD = "com.automattic.simplenote.action.ACTION_WIDGET_FORWARD";
    public static final String ACTION_BACKWARD = "com.automattic.simplenote.action.ACTION_WIDGET_BACKWARD";
    public static final String ACTION_DELETE_NOTE = "com.automattic.simplenote.action.ACTION_WIDGET_DELETE";
    public static final String ACTION_NEW_NOTE = "com.automattic.simplenote.action.ACTION_WIDGET_NEW_NOTE";
    public static final String ACTION_SEARCH_NOTE = "com.automattic.simplenote.action.ACTION_WIDGET_SEARCH";
    public static final String ACTION_SHARE_NOTE = "com.automattic.simplenote.action.ACTION_WIDGET_SHARE";
    public static final String ACTION_SHOW_ALL_NOTES = "com.automattic.simplenote.action.ACTION_WIDGET_SHOW_ALL";
    public static final String ACTION_LAUNCH_APP = "com.automattic.simplenote.action.ACTION_WIDGET_LAUNCH_APP";
    public static final String ACTION_NOTIFY_DATA_SET_CHANGED = "com.automattic.simplenote.action.ACTION_NOTIFY_DATA_SET_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i(TAG, "onReceive: intent " + intent.getAction().toString());

        AppWidgetManager awManager = AppWidgetManager.getInstance(context);
        String action = intent.getAction();
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);


        if (action.equals(ACTION_FORWARD)){

            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID){
                throw new IllegalArgumentException("intent has no widget id.");
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int currentNote = prefs.getInt(WidgetService.PREF_WIDGET_NOTE, WidgetService.NO_NOTE);

            if (currentNote == WidgetService.NO_NOTE || currentNote < 0){
                currentNote = 0;
            } else {
                currentNote++;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(WidgetService.PREF_WIDGET_NOTE, currentNote);
            editor.commit();

            awManager.notifyAppWidgetViewDataChanged(widgetId, R.id.avf_widget_populated);
            Log.i(TAG, "note set to " + currentNote + ". Updating widget id " + widgetId);
        } else if (action.equals(ACTION_NOTIFY_DATA_SET_CHANGED)){

            // update all widgets
            int ids[] = awManager.getAppWidgetIds(new ComponentName(context, SimpleNoteWidgetProvider.class));
            if (ids != null){
                for (int i : ids) {
                    Log.i(TAG, "notify data set changed. widget id: " + Integer.toString(i));
                    awManager.notifyAppWidgetViewDataChanged(i, R.id.avf_widget_populated);
                }
            }


        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i(TAG, "onUpdate. Processing " + appWidgetIds.length + " widgets.");


        // create remote views for each app widget.
        for (int i = 0; i < appWidgetIds.length; i++) {

            // create intent that starts widget service
            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);

            // add the intent URI as an extra so the OS an match it with the service.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // create a remote view, specifying the widget layout that should be used.
            RemoteViews rViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            rViews.setRemoteAdapter(appWidgetIds[i], R.id.avf_widget_populated, intent);

            // specify the sibling to the collection view that is shown when no data is available.
            rViews.setEmptyView(appWidgetIds[i], R.id.tv_widget_empty);

            setupPendingIntents(context, appWidgetManager, appWidgetIds[i]);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rViews);

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Register pending intents to widget UI buttons.
     * @param ctx context needed to access remote view.
     * @param appWidgetManager widget manager that will be updated
     * @param widgetId widget id managed by widget manager.
     */
    private void setupPendingIntents(Context ctx, AppWidgetManager appWidgetManager, int widgetId){

        PendingIntentBuilder piBuilder = new PendingIntentBuilder(ctx, appWidgetManager);
        piBuilder.setLayout(R.layout.widget_layout);
        piBuilder.setWidgetId(widgetId);

        piBuilder.setAction(SimpleNoteWidgetProvider.ACTION_BACKWARD);
        piBuilder.setChildView(R.id.ib_widget_backward);
        piBuilder.setOnClickPendingIntent();

        piBuilder.setAction(SimpleNoteWidgetProvider.ACTION_FORWARD);
        piBuilder.setChildView(R.id.ib_widget_forward);
        piBuilder.setOnClickPendingIntent();

        piBuilder.setAction(SimpleNoteWidgetProvider.ACTION_DELETE_NOTE);
        piBuilder.setChildView(R.id.ib_widget_delete);
        piBuilder.setOnClickPendingIntent();

        piBuilder.setAction(SimpleNoteWidgetProvider.ACTION_NEW_NOTE);
        piBuilder.setChildView(R.id.ib_widget_new);
        piBuilder.setOnClickPendingIntent();

        piBuilder.setAction(SimpleNoteWidgetProvider.ACTION_SEARCH_NOTE);
        piBuilder.setChildView(R.id.ib_widget_search);
        piBuilder.setOnClickPendingIntent();

        piBuilder.setAction(SimpleNoteWidgetProvider.ACTION_SHARE_NOTE);
        piBuilder.setChildView(R.id.ib_widget_share);
        piBuilder.setOnClickPendingIntent();

        piBuilder.setAction(SimpleNoteWidgetProvider.ACTION_SHOW_ALL_NOTES);
        piBuilder.setChildView(R.id.ib_widget_showallnotes);
        piBuilder.setOnClickPendingIntent();

        piBuilder.setAction(SimpleNoteWidgetProvider.ACTION_LAUNCH_APP);
        piBuilder.setChildView(R.id.ib_widget_app_icon);
        piBuilder.setOnClickPendingIntent();



        // setup the pending intent template for data set updates
        piBuilder.clear();
        piBuilder.setAction(SimpleNoteWidgetProvider.ACTION_NOTIFY_DATA_SET_CHANGED);
        piBuilder.setWidgetId(widgetId);
        piBuilder.setLayout(R.layout.widget_layout);
        piBuilder.setChildView(R.id.avf_widget_populated);
        piBuilder.setPendingIntentTemplate();

    }



    private PendingIntent setupForwardPendingIntent(Context ctx,
                                                    int appWidgetId ){
        Intent i = new Intent(ctx, SimpleNoteWidgetProvider.class);
        i.setAction(SimpleNoteWidgetProvider.ACTION_FORWARD);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);


        PendingIntent result = PendingIntent.getBroadcast(ctx, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return result;

    }
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.i(TAG, "onDeleted");

    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.i(TAG, "onEnabled");

    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.i(TAG, "onDisabled");

    }


    private static class PendingIntentBuilder{

        private final Context mContext;
        private final AppWidgetManager mManager;
        private Integer mLayoutResId;
        private Integer mChildViewResId;
        private Integer mWidgetId;
        private String mAction;

        public PendingIntentBuilder(Context ctx, AppWidgetManager manager){
            mContext = ctx;
            mManager = manager;

        }

        public PendingIntent build(){

            validate(mLayoutResId, "layout resource id", "setLayout(int)");
            validate(mChildViewResId, "child resource id", "setChildView(int)");
            validate(mAction, "action", "setAction(String)");
            validate(mWidgetId, "widget id", "setWidgetId(int)");

            Intent i = new Intent(mContext, SimpleNoteWidgetProvider.class);
            i.setAction(mAction);
            i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);


            return PendingIntent.getBroadcast(mContext, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);

        }

        private void validate(Object underTest, String name, String setterName){
            if (underTest == null){
                throw new IllegalStateException(new StringBuilder().append(name)
                        .append(" cannot be null. Call ")
                        .append(setterName)
                        .toString());
            }
        }

        public PendingIntentBuilder setLayout(int resId){
            mLayoutResId = resId;
            return this;
        }

        public PendingIntentBuilder setChildView(int resId){
            mChildViewResId = resId;
            return this;
        }

        public PendingIntentBuilder setAction(String action){
            mAction = action;
            return this;
        }

        public PendingIntentBuilder setWidgetId(int widgetId){
            mWidgetId = widgetId;
            return this;
        }

        public void setPendingIntentTemplate(){
            // setup pending intents for buttons
            // Create a view that will show data for this item.
            RemoteViews rViews = new RemoteViews(mContext.getPackageName(),
                    mLayoutResId);
            rViews.setPendingIntentTemplate(mChildViewResId, build());
            Log.i(TAG, "setPendingIntentTemplate set for remote view with action " + mAction);
        }

        public void setOnClickPendingIntent(){


            // setup pending intents for buttons
            // Create a view that will show data for this item.
            RemoteViews rViews = new RemoteViews(mContext.getPackageName(),
                    mLayoutResId);
            rViews.setOnClickPendingIntent(mChildViewResId, build());
            mManager.updateAppWidget(mWidgetId, rViews);

            Log.i(TAG, "onClickPendingIntent set for remote view with action " + mAction);

        }

        /**
         * Clears out everything set through builder functions.  The values passed to the
         * constructor are untouched.
         */
        public void clear(){
            mLayoutResId = null;
            mChildViewResId = null;
            mAction = null;

        }


    }
}