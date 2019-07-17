package com.domker.study.androidstudy.database;


import android.provider.BaseColumns;

public class SQLCommand {

    public static class SQLVideo implements BaseColumns{
        public static final String  TABLE_NAME="videos";
        public static final String  COLUMN_STUDENT_ID="student_id";
        public static final String  COLUMN_USER_NAME="user_name";
        public static final String  COLUMN_IMAGE_URL="image_url";
        public static final String  COLUMN_VIDEO_URL="video_url";
        public static final String  COLUMN_ID="server_id";
        public static final String  COLUMN_IMAGE_H="image_h";
        public static final String  COLUMN_IMAGE_W="image_w";
        public static final String  COLUMN_CREATE_AT="createAt";
        public static final String  COLUMN_UPDATE_AT="updateAt";
        public static final String  COLUMN_V="_v";


    }



    public static final String SQL_CREATE_VIDEO=
            "CREATE TABLE "+SQLVideo.TABLE_NAME
                    +"("+SQLVideo.COLUMN_ID+" TEXT PRIMARY KEY, "
                    +SQLVideo.COLUMN_STUDENT_ID+ " TEXT, "
                    +SQLVideo.COLUMN_USER_NAME+ " TEXT, "
                    +SQLVideo.COLUMN_VIDEO_URL+ " TEXT, "
                    +SQLVideo.COLUMN_IMAGE_URL+ " TEXT, "
                    +SQLVideo.COLUMN_CREATE_AT+ " TEXT, "
                    +SQLVideo.COLUMN_UPDATE_AT+ " TEXT, "
                    +SQLVideo.COLUMN_IMAGE_H+" INTEGER, "
                    +SQLVideo.COLUMN_IMAGE_W+" INTEGER, "
                    +SQLVideo.COLUMN_V+" INTEGER)";

}
