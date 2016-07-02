package com.sonelli.juicessh.pluginlibrary;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class PluginContract {

    public static final String AUTHORITY = "com.sonelli.juicessh.api.v1";

    public static final String PERMISSION_OPEN_SESSIONS = "com.sonelli.juicessh.api.v1.permission.OPEN_SESSIONS";

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

        public static final String TABLE_NAME = "connection";
        private static final String _ID = "_id";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_MODIFIED = "modified";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_PORT = "port";
        public static final String COLUMN_NICKNAME = "nickname";
        public static final String COLUMN_TYPE = "type";

        public static final String[] PROJECTION = {
                COLUMN_ID,
                String.format("rowid AS %s", _ID),
                COLUMN_MODIFIED,
                String.format("COALESCE(NULLIF(nickname,''), address) AS %s", COLUMN_NAME),
                COLUMN_ADDRESS,
                COLUMN_PORT,
                COLUMN_NICKNAME,
                COLUMN_TYPE
        };

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

        public static final String TABLE_NAME = "snippet";
        private static final String _ID = "_id";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_MODIFIED = "modified";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CONTENT = "content";

        public static final String[] PROJECTION = {
                COLUMN_ID,
                String.format("rowid AS %s", _ID),
                COLUMN_MODIFIED,
                COLUMN_NAME,
                COLUMN_CONTENT
        };

    }

    /**
     * Published API for Port Forwards
     */
    public static final class PortForwards implements BaseColumns {

        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/portforwards");
        public final static String PERMISSION_READ = "com.sonelli.juicessh.api.v1.permission.READ_PORT_FORWARDS";
        public final static String PERMISSION_WRITE = "com.sonelli.juicessh.api.v1.permission.WRITE_PORT_FORWARDS";

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.sonelli.juicessh.models.portforward";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.sonelli.juicessh.models.portforward";
        public static final String SORT_ORDER_DEFAULT = "name ASC";

        final public static int MODE_LOCAL = 0;
        final public static int MODE_REMOTE = 1;
        final public static int MODE_SOCKS = 2;

        public static final String TABLE_NAME = "portforward";
        private static final String _ID = "_id";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_MODIFIED = "modified";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_MODE = "mode";
        public static final String COLUMN_CONNECTION_ID = "connection_id";
        public static final String COLUMN_HOST = "host";
        public static final String COLUMN_LOCAL_PORT = "localPort";
        public static final String COLUMN_REMOTE_PORT = "remotePort";
        public static final String COLUMN_OPEN_IN_BROWSER = "openInBrowser";

        public static final String[] PROJECTION = {
                COLUMN_ID,
                String.format("rowid AS %s", _ID),
                COLUMN_MODIFIED,
                COLUMN_NAME,
                COLUMN_MODE,
                COLUMN_CONNECTION_ID,
                COLUMN_HOST,
                COLUMN_LOCAL_PORT,
                COLUMN_REMOTE_PORT,
                COLUMN_OPEN_IN_BROWSER
        };

    }

    /**
     * Published API for Connection Groups
     */
    public static final class ConnectionGroups implements BaseColumns {

        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/connectiongroups");
        public final static String PERMISSION_READ = "com.sonelli.juicessh.api.v1.permission.READ_CONNECTION_GROUPS";
        public final static String PERMISSION_WRITE = "com.sonelli.juicessh.api.v1.permission.WRITE_CONNECTION_GROUPS";

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.sonelli.juicessh.models.connectiongroup";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.sonelli.juicessh.models.connectiongroup";
        public static final String SORT_ORDER_DEFAULT = "name COLLATE NOCASE ASC";

        public static final String TABLE_NAME = "connectiongroup";
        private static final String _ID = "_id";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_MODIFIED = "modified";
        public static final String COLUMN_NAME = "name";

        public static final String[] PROJECTION = {
                COLUMN_ID,
                String.format("rowid AS %s", _ID),
                COLUMN_MODIFIED,
                COLUMN_NAME,
        };

    }

    /**
     * Published API for Connection Group Memberships
     */
    public static final class ConnectionGroupMemberships implements BaseColumns {

        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/connectiongroupmemberships");
        public final static String PERMISSION_READ = "com.sonelli.juicessh.api.v1.permission.READ_CONNECTION_GROUPS";
        public final static String PERMISSION_WRITE = "com.sonelli.juicessh.api.v1.permission.WRITE_CONNECTION_GROUPS";

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.sonelli.juicessh.models.connectiongroupmembership";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.sonelli.juicessh.models.connectiongroupmembership";
        public static final String SORT_ORDER_DEFAULT = "id ASC";

        public static final String TABLE_NAME = "connectiongroupmembership";
        private static final String _ID = "_id";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_MODIFIED = "modified";
        public static final String COLUMN_GROUP_ID = "group_id";
        public static final String COLUMN_CONNECTION_ID = "connection_id";

        public static final String[] PROJECTION = {
                COLUMN_ID,
                String.format("rowid AS %s", _ID),
                COLUMN_MODIFIED,
                COLUMN_GROUP_ID,
                COLUMN_CONNECTION_ID
        };

    }

    /**
     * Published API for Plugin Audit Log (read-only)
     */
    public static final class PluginLog implements BaseColumns {

        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/pluginlog");
        public final static String PERMISSION_READ = "com.sonelli.juicessh.api.v1.permission.READ_PLUGIN_LOG";

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.sonelli.juicessh.models.pluginlog";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.sonelli.juicessh.models.pluginlog";
        public static final String SORT_ORDER_DEFAULT = "modified DESC";

        public static final String TABLE_NAME = "plugin_log";
        private static final String _ID = "_id";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_MODIFIED = "modified";
        public static final String COLUMN_PACKAGE_NAME = "packageName";
        public static final String COLUMN_MESSAGE = "message";

        public static final String[] PROJECTION = {
                COLUMN_ID,
                String.format("rowid AS %s", _ID),
                COLUMN_MODIFIED,
                COLUMN_PACKAGE_NAME,
                COLUMN_MESSAGE
        };

    }


}
