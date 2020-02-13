package com.itbd.livewallpaper.Database.LocalDatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.itbd.livewallpaper.Database.Recents;
import static com.itbd.livewallpaper.Database.LocalDatabase.LocalDatabase.DATABASE_VERSION;

@Database(entities = Recents.class ,version = DATABASE_VERSION)
public abstract class LocalDatabase  extends RoomDatabase{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ITBDLiveWallpaper";

    public abstract RecentsDAO recentsDAO();
    private static LocalDatabase instance;
    public static LocalDatabase getInstance(Context context){

        if(instance == null){
            instance = Room.databaseBuilder(context,LocalDatabase.class,DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
