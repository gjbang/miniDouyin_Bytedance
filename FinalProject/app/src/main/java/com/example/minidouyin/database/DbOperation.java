package com.example.minidouyin.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.minidouyin.model.Video;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DbOperation {
    private SQLiteDatabase database;
    private SQLDbHelper dbHelper;

    public DbOperation(SQLiteDatabase database,SQLDbHelper dbHelper){
        this.database=database;
        this.dbHelper=dbHelper;
    }

    public void Initialize(){
        database=dbHelper.getWritableDatabase();
    }

    public void Destroy(){
        database.close();
        database=null;
        dbHelper.close();
        dbHelper=null;
    }

    public List<Video> loadVideoFromDatabase(){
        if(database==null){
            return Collections.emptyList();
        }

        List<Video> result=new LinkedList<>();
        Cursor cursor=null;

        try{
            cursor=database.query(SQLCommand.SQLVideo.TABLE_NAME,null,
                    null,null,null,null,
                    SQLCommand.SQLVideo.COLUMN_CREATE_AT+" DESC");

            while(cursor.moveToNext()){
                String student_id=cursor.getString(cursor.getColumnIndex(SQLCommand.SQLVideo.COLUMN_STUDENT_ID));
                String user_name=cursor.getString(cursor.getColumnIndex(SQLCommand.SQLVideo.COLUMN_USER_NAME));
                String image_url=cursor.getString(cursor.getColumnIndex(SQLCommand.SQLVideo.COLUMN_IMAGE_URL));
                String video_url=cursor.getString(cursor.getColumnIndex(SQLCommand.SQLVideo.COLUMN_VIDEO_URL));
                String _id=cursor.getString(cursor.getColumnIndex(SQLCommand.SQLVideo.COLUMN_ID));
                String createAT=cursor.getString(cursor.getColumnIndex(SQLCommand.SQLVideo.COLUMN_CREATE_AT));
                String updateAt=cursor.getString(cursor.getColumnIndex(SQLCommand.SQLVideo.COLUMN_UPDATE_AT));


                int image_w=cursor.getInt(cursor.getColumnIndex(SQLCommand.SQLVideo.COLUMN_IMAGE_W));
                int image_h=cursor.getInt((cursor.getColumnIndex(SQLCommand.SQLVideo.COLUMN_IMAGE_H)));
                int _v=cursor.getInt(cursor.getColumnIndex(SQLCommand.SQLVideo.COLUMN_V));

                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );

                Video video=new Video();
                video.set_id(_id);
                video.setStudentId(student_id);
                video.setUserName(user_name);
                video.setImageUrl(image_url);
                video.setVideoUrl(video_url);
                video.setCreatedAt(sdf.parse(createAT));
                video.setUpdatedAt(sdf.parse(updateAt));
                video.setImage_w(image_w);
                video.setImage_h(image_h);

                result.add(video);

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }

        return result;
    }

    public boolean saveVideo2Database(List<Video> videos){
        if(database==null|| videos.isEmpty()){
            return false;
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );

        for(int i=0;i<videos.size();++i){
            ContentValues values=new ContentValues();
            values.put(SQLCommand.SQLVideo.COLUMN_ID,videos.get(i).get_id());
            values.put(SQLCommand.SQLVideo.COLUMN_STUDENT_ID,videos.get(i).getStudentId());
            values.put(SQLCommand.SQLVideo.COLUMN_USER_NAME,videos.get(i).getUserName());

            values.put(SQLCommand.SQLVideo.COLUMN_VIDEO_URL,videos.get(i).getVideoUrl());
            values.put(SQLCommand.SQLVideo.COLUMN_IMAGE_URL,videos.get(i).getImageUrl());

            values.put(SQLCommand.SQLVideo.COLUMN_CREATE_AT,sdf.format(videos.get(i).getCreatedAt()));
            values.put(SQLCommand.SQLVideo.COLUMN_UPDATE_AT,sdf.format(videos.get(i).getUpdatedAt()));

            values.put(SQLCommand.SQLVideo.COLUMN_IMAGE_H,videos.get(i).getImage_h());
            values.put(SQLCommand.SQLVideo.COLUMN_IMAGE_W,videos.get(i).getImage_w());

            values.put(SQLCommand.SQLVideo.COLUMN_V,0);
            Long rowId=database.insert(SQLCommand.SQLVideo.TABLE_NAME,null,values);

            if(rowId==-1){
                return false;
            }
        }

        return true;

    }
}
