package com.sonelli.juicessh.pluginexample.contracts;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.UUID;

public class JuiceSSHContract {

    public static final String AUTHORITY = "com.sonelli.juicessh.api.v1";

    /**
     * Published API for Connections
     */
    public static final class Connections implements BaseColumns {

        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/connections");
        public final static String PERMISSION_READ = "com.sonelli.juicessh.api.v1.permission.READ_CONNECTIONS";
        public final static String PERMISSION_WRITE = "com.sonelli.juicessh.api.v1.permission.WRITE_CONNECTIONS";

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.sonelli.juicessh.models.connection";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.sonelli.juicessh.models.connection";
        public static final String SORT_ORDER_DEFAULT = "name COLLATE NOCASE ASC";

        public final static int TYPE_SSH = 0;
        public final static int TYPE_MOSH = 1;
        public final static int TYPE_LOCAL = 2;
        public final static int TYPE_TELNET = 3;

        public static final String ID = "id";
        public static final String _ID = "_id";
        public static final String MODIFIED = "modified";
        public static final String NAME = "name";
        public static final String ADDRESS = "address";
        public static final String NICKNAME = "nickname";
        public static final String TYPE = "type";

        public static final String[] PROJECTION = {
                ID,
                "rowid AS _id",
                MODIFIED,
                "COALESCE(NULLIF(nickname,''), address) AS name",
                ADDRESS,
                NICKNAME,
                TYPE
        };

        public static Intent generateConnectIntent(UUID connectionId, String command, boolean runInBackground){

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("ssh://" + connectionId));

            /**
             * You will either be responsible for closing the terminal via an exit command
             * or the user will need to close it via the notification.
             */
            if(runInBackground){
                intent.putExtra("RUN_IN_BACKGROUND", true);
            }

            /**
             * Command will be run once connected
             * The output and return code will be accessible via the activity if you used
             * startActivityForResult() via your onActivityResult() method.
             * The return code will be available as Intent.getIntExtra("COMMAND_RETURN_CODE", -1);
             * The output will be available as Intent.getStringExtra("COMMAND_OUTPUT");
             */
            if(command != null){
                intent.putExtra("RUN_ON_CONNECT", command);
            }

            return intent;

        }

    }

    /**
     * Published API for Snippets
     */
    public static final class Snippets implements BaseColumns {

        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/snippets");
        public final static String PERMISSION_READ = "com.sonelli.juicessh.api.v1.permission.READ_SNIPPETS";
        public final static String PERMISSION_WRITE = "com.sonelli.juicessh.api.v1.permission.WRITE_SNIPPETS";

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.sonelli.juicessh.models.snippet";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.sonelli.juicessh.models.snippet";
        public static final String SORT_ORDER_DEFAULT = "name COLLATE NOCASE ASC";

        public static final String ID = "id";
        public static final String _ID = "_id";
        public static final String MODIFIED = "modified";
        public static final String NAME = "name";
        public static final String CONTENT = "content";

        public static final String[] PROJECTION = {
                ID,
                "rowid AS _id",
                MODIFIED,
                NAME,
                CONTENT
        };

    }

}
