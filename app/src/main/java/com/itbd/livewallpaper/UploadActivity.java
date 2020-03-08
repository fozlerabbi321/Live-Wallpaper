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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    private String productRandomKey, downloadImageUrl;

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

    private void uplaod() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading....");
            progressDialog.show();

            final StorageReference ref = storageReference.child(new StringBuilder("images/")
                    .append(UUID.randomUUID().toString()).toString());

            final UploadTask uploadTask = ref.putFile(filePath);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(UploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // upload image  task successfully message listener
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // download image url path in task
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            downloadImageUrl = ref.getDownloadUrl().toString();
                            return ref.getDownloadUrl();
                        }
                        // download image url complete listener
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                downloadImageUrl = task.getResult().toString();
                             //   Toast.makeText(AdminAddNewProductActivity.this, "got the Product image Url Successfully...", Toast.LENGTH_SHORT).show();
                                // save to product database method
                                saveToCategory(categoryIdSelected,downloadImageUrl);
                            }
                        }
                    });
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


                    ///
           /* ref.putFile(filePath)
                   .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                           progressDialog.dismiss();
                           saveToCategory(categoryIdSelected,taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());



                       }
                   }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
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
        }*/



    private void ChooseImage()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    private void loadCategoryToSpinner()
    {
        FirebaseDatabase.getInstance().getReference(Common.STR_CATEGORY_BACKGROUND)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            CategoryItem item = postSnapshot.getValue(CategoryItem.class);
                            String key = postSnapshot.getKey();
                            spinnerData.put(key,item.getName());
                        }
                        //Because Material Spinner will not receive hint so we need custom hint
                        //This is my tip....
                        Object[] javaArray = spinnerData.values().toArray();
                        List<Object> valueList = new ArrayList<>();
                        valueList.add(0,"Category");//we will add first item is hint :D
                        valueList.addAll(Arrays.asList(javaArray));//And add all remain category name
                        spinner.setItems(valueList);//set source data for spinner

                        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

                                //when user choose category,we will get categoryId (key)
                                Object[] keyArray = spinnerData.keySet().toArray();
                                List<Object> kewList = new ArrayList<>();
                                kewList.add(0,"Category_Key");
                                kewList.addAll(Arrays.asList(keyArray));
                                spinner.setItems(kewList);
                                categoryIdSelected = kewList.get(position).toString();//Assign kew when user choose category



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
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
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
