package com.itbd.livewallpaper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itbd.livewallpaper.Common.Common;
import com.itbd.livewallpaper.Database.DataSource.RecentsRepository;
import com.itbd.livewallpaper.Database.LocalDatabase.LocalDatabase;
import com.itbd.livewallpaper.Database.LocalDatabase.RecentsDataSource;
import com.itbd.livewallpaper.Database.Recents;
import com.itbd.livewallpaper.Helper.SaveImageHelper;
import com.itbd.livewallpaper.Model.WallpaperItem;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.LogRecord;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ViewWallpaper extends AppCompatActivity {

    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton floatingActionButton,fabDownload;
    ImageView imageView;
    CoordinatorLayout rootLayout;

    //Room Database
    CompositeDisposable compositeDisposable;
    RecentsRepository recentsRepository;

    // Floating button github
    FloatingActionMenu mainFloationg;
    com.github.clans.fab.FloatingActionButton fbShare;

    //Facebook
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    AlertDialog dialog;

    // set wallpaper picasso
    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            try {
                wallpaperManager.setBitmap(bitmap);
                Snackbar.make(rootLayout,"Wallpaper was set",Snackbar.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

//fb share
    private Target facebookConvertBitmap = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            SharePhoto sharePhoto = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();

            if(ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(sharePhoto)
                        .build();
                shareDialog.show(content);
            }

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case Common.PERMISSION_REQUEST_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    AlertDialog dialog = new SpotsDialog.Builder().setContext(ViewWallpaper.this).build();
                    dialog.show();
                    dialog.setMessage("Please Waiting....");

                    String fileName = UUID.randomUUID().toString()+"png";
                    Picasso.with(getBaseContext())
                            .load(Common.select_background.getImageLink())
                            .into(new SaveImageHelper(getBaseContext(),dialog,
                                    getApplicationContext().getContentResolver(),
                                    fileName,"ITBD Live Wallpaper Image"));
                }
                else
                    Toast.makeText(this, "You need accept this permisfsion to download image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wallpaper);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        String  url = (Common.select_background.getImageLink());


        // Init RoomDatabase
        compositeDisposable = new CompositeDisposable();
        LocalDatabase database = LocalDatabase.getInstance(this);
        recentsRepository = RecentsRepository.getInstance(RecentsDataSource.getInstance(database.recentsDAO()));


        //Init
        rootLayout = findViewById(R.id.rootLayout);
        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollaspaseAppBar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setTitle(Common.CATEGORY_SELECTED);
        imageView = findViewById(R.id.imageThumb);

        //img
        /*Picasso.with(this)
                .load(Common.select_background.getImageLink())
                .into(imageView);
*/
        // gif img
        Glide.with(ViewWallpaper.this)
                .load(Common.select_background.getImageLink())
                .override(350, 350)
                .centerCrop()
                .placeholder(R.drawable.ic_load)
                .into(imageView);

        mainFloationg = findViewById(R.id.menu);
        fbShare = findViewById(R.id.menu_share);
        fbShare.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                // create call back
                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(ViewWallpaper.this, "Share Successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(ViewWallpaper.this, "Share cancelled !", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(ViewWallpaper.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                //we will fetch photo from link and to bitmap

                //picasso
               /* Picasso.with(getBaseContext())
                        .load(Common.select_background.getImageLink())
                        .into(facebookConvertBitmap);*/
                //glide
                Glide.with(ViewWallpaper.this)
                        .asBitmap()
                        .load(Common.select_background.getImageLink())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {


                                SharePhoto sharePhoto = new SharePhoto.Builder()
                                        .setBitmap(bitmap)
                                        .build();

                                if(ShareDialog.canShow(SharePhotoContent.class))
                                {
                                    SharePhotoContent content = new SharePhotoContent.Builder()
                                            .addPhoto(sharePhoto)
                                            .build();
                                    shareDialog.show(content);
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

            }
        });


        //add to recents
        addToRecents();

        // save wallpaper btn
        floatingActionButton = findViewById(R.id.fabWallpaper);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
              /*  //picasso
                Picasso.with(ViewWallpaper.this)
                        .load(Common.select_background.getImageLink())
                        .into(target);
*/
              //gif
                Glide.with(ViewWallpaper.this)
                        .asBitmap()
                        .load(Common.select_background.getImageLink())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                WallpaperManager wallpaperManager = WallpaperManager.getInstance(ViewWallpaper.this);
                                try {
                                    wallpaperManager.setBitmap(resource);
                                    Snackbar.make(rootLayout,"Wallpaper was set",Snackbar.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

            }
        });

        // save device btn
        fabDownload = findViewById(R.id.fabDownload);
        fabDownload.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //check permission
                if(ActivityCompat.checkSelfPermission(ViewWallpaper.this, Manifest.permission.WRITE_EXTERNAL_STORAGE )
                        != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, Common.PERMISSION_REQUEST_CODE);
                }
                else{
                    dialog = new SpotsDialog.Builder().setContext(ViewWallpaper.this).build();
                    dialog.show();
                    dialog.setMessage("Please Waiting....");

                    //picasso
                   /* String fileName = UUID.randomUUID().toString()+"png";
                    Picasso.with(getBaseContext())
                            .load(Common.select_background.getImageLink())
                            .into(new SaveImageHelper(getBaseContext(),dialog,
                                    getApplicationContext().getContentResolver(),
                                    fileName,"ITBD Live Wallpaper Image"));*/

                   //glide
                    Glide.with(ViewWallpaper.this)
                            .asBitmap()
                            .load(Common.select_background.getImageLink())
                            .override(300,600)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    saveBitmap(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });

                }

            }
        });

        //View Count
        increaseViewCount();

    }

    private void saveBitmap(Bitmap bitmap)
    {
        //String fileName = UUID.randomUUID()+".png";
        String  url = (Common.select_background.getImageLink());
        String fileName= url.substring(url.lastIndexOf('/'), url.length());

        String path = Environment.getExternalStorageDirectory().toString();
        File folder = new File(path + "/" + getString(R.string.app_name));
        folder.mkdirs();
        File file = new File(folder,fileName+".png");

        if(file.exists())
        {
            Toast.makeText(this, "Already Downloaded", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }else {
            if(file.exists())
                file.delete();
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                outputStream.flush();
                outputStream.close();

                //this line send picture to gallery
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                Snackbar.make(rootLayout,"Download successfully",Snackbar.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void increaseViewCount()
    {
        FirebaseDatabase.getInstance().
                getReference(Common.STR_WALLPAPER)
                .child(Common.select_background_key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild("viewCount")){

                            WallpaperItem wallpaperItem = dataSnapshot.getValue(WallpaperItem.class);
                            long count = wallpaperItem.getViewCount() +1;
                            //update
                            Map<String,Object> update_view = new HashMap<>();
                            update_view.put("viewCount",count);

                            FirebaseDatabase.getInstance().
                                    getReference(Common.STR_WALLPAPER)
                                    .child(Common.select_background_key)
                                    .updateChildren(update_view)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ViewWallpaper.this, "Cannot Update view count", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else //if view count is not set (default 1)
                        {
                            Map<String,Object> update_view = new HashMap<>();
                            update_view.put("viewCount",Long.valueOf(1));

                            FirebaseDatabase.getInstance().
                                    getReference(Common.STR_WALLPAPER)
                                    .child(Common.select_background_key)
                                    .updateChildren(update_view)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ViewWallpaper.this, "Cannot set default view count", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void addToRecents()
    {
        // add recents
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                Recents  recents = new Recents(Common.select_background.getImageLink(),
                        Common.select_background.getCategoryId(),
                        String.valueOf(System.currentTimeMillis())
                ,Common.select_background_key);
                recentsRepository.insertRecents(recents);
                e.onComplete();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d("ERROR RABBI", throwable.getMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                });

        compositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        //Picasso.with(this).cancelRequest(target);
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

}
