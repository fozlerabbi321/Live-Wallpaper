package com.itbd.livewallpaper.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;


@Entity(tableName = "recents",primaryKeys = {"imageLink","categoryId"})
public class Recents {

    @ColumnInfo(name="imageLink")
    @NonNull
    private String imageLink;

    @ColumnInfo(name="categoryId")
    @NonNull
    private String categoryId;

    @ColumnInfo(name="saveTime")
    private String saveTime;

    @ColumnInfo(name="key")
    private String key;

    public Recents(@NonNull String imageLink, @NonNull String categoryId, String saveTime, String key) {
        this.imageLink = imageLink;
        this.categoryId = categoryId;
        this.saveTime = saveTime;
        this.key = key;
    }

    @NonNull
    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(@NonNull String imageLink) {
        this.imageLink = imageLink;
    }

    @NonNull
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(String saveTime) {
        this.saveTime = saveTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
