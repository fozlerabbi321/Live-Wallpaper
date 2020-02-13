package com.itbd.livewallpaper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itbd.livewallpaper.Common.Common;
import com.itbd.livewallpaper.Model.CategoryItem;
import com.itbd.livewallpaper.Model.WallpaperItem;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    ImageView image_priview;
    Button btn_upload,btn_browser;
    MaterialSpinner spinner;
    private Uri filePath;
    String categoryIdSelected="";

    //FirStorage
    FirebaseStorage storage;
    StorageReference storageReference;

    Map<String,String> spinnerData = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        //Firebase Storage init
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // inti
        image_priview = findViewById(R.id.image_preview);
        btn_browser = findViewById(R.id.btn_browser);
        btn_upload = findViewById(R.id.btn_upload);
        spinner = findViewById(R.id.spinner);

        /// load spinner data
        loadCategoryToSpinner();

        btn_browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseImage();
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spinner.getSelectedIndex() == 0)// not selected item
                    Toast.makeText(UploadActivity.this, "Please choose category", Toast.LENGTH_SHORT).show();
                else
                uplaod();
            }
        });
    }

    private void uplaod()
    {
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading....");
            progressDialog.show();

            StorageReference ref = storageReference.child(new StringBuilder("images.")
                    .append(UUID.randomUUID().toString()).toString());

            ref.putFile(filePath)
                   .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                           progressDialog.dismiss();
                           saveToCategory(categoryIdSelected,taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());


                       }
                   }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(UploadActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded   "+(int)progress+"%");
                }
            });
        }
    }

    private void ChooseImage()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Common.PICK_IMAGE_REQUEST);
    }

    private void loadCategoryToSpinner()
    {
        FirebaseDatabase.getInstance()
                .getReference(Common.STR_CATEGORY_BACKGROUND)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            CategoryItem item = postSnapshot.getValue(CategoryItem.class);
                            String key = postSnapshot.getKey();

                            spinnerData.put(key,item.getName());

                        }
                        //
                        Object[] javaArray = spinnerData.values().toArray();
                        List<Object> valueList = new ArrayList<>();
                        valueList.add(0,"Category");
                        valueList.add(Arrays.asList(javaArray));
                        spinner.setItems(valueList);
                        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

                                //when user choose category,we will get categoryId key
                                Object[] javaArray = spinnerData.keySet().toArray();
                                List<Object> kewList = new ArrayList<>();
                                kewList.add(0,"Category_key");
                                kewList.add(Arrays.asList(javaArray));
                                spinner.setItems(kewList);
                                categoryIdSelected = kewList.get(position).toString();//assign kew when user choose category



                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null){
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                image_priview.setImageBitmap(bitmap);
                btn_upload.setEnabled(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveToCategory(String categoryIdSelected,String imageLink)
    {
        FirebaseDatabase.getInstance()
                .getReference(Common.STR_WALLPAPER).
                push()
                .setValue(new WallpaperItem(imageLink,categoryIdSelected))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UploadActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}
