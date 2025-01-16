package com.shop.bagrutproject.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shop.bagrutproject.R;
import com.shop.bagrutproject.models.Item;
import com.shop.bagrutproject.services.DatabaseService;
import com.shop.bagrutproject.utils.ImageUtil;

public class AddItem extends AppCompatActivity {

    private EditText etItemName, etItemInfo, etItemPrice;
    private Spinner spType, spColor,spCompany;
    private Button btnGallery, btnTakePic, btnAddItem;
    private ImageView imageView;
    private Uri imageUri;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private DatabaseService databaseService;

    /// Activity result launcher for selecting image from gallery
    private ActivityResultLauncher<Intent> selectImageLauncher;
    /// Activity result launcher for capturing image from camera
    private ActivityResultLauncher<Intent> captureImageLauncher;

    // One Preview Image
    ImageView IVPreviewImage;

    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        InitViews();

        /// request permission for the camera and storage
        ImageUtil.requestPermission(this);

        /// get the instance of the database service
        databaseService = DatabaseService.getInstance();



        /// register the activity result launcher for selecting image from gallery
        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        imageView.setImageURI(selectedImage);
                    }
                });

        /// register the activity result launcher for capturing image from camera
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        imageView.setImageBitmap(bitmap);
                    }
                });





        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();


            }
        });

        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImageFromCamera();

            }
        });

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = etItemName.getText().toString();
                String itemInfo = etItemInfo.getText().toString();
                String itemPrice = etItemPrice.getText().toString();
                String itemType = spType.getSelectedItem().toString();
                String itemColor = spColor.getSelectedItem().toString();
                String itemCompany = spCompany.getSelectedItem().toString();

                String imageBase64 = ImageUtil.convertTo64Base(imageView);
                double price = Double.parseDouble(itemPrice);

                if (itemName.isEmpty() || itemCompany.isEmpty() || itemInfo.isEmpty() ||
                        itemPrice.isEmpty() || itemType.isEmpty() || itemColor.isEmpty()) {
                    Toast.makeText(AddItem.this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddItem.this, "המוצר נוסף בהצלחה!", Toast.LENGTH_SHORT).show();
                }

                /// generate a new id for the item
                String id = databaseService.generateItemId();


                Item newItem = new Item(id, itemName, itemType, itemColor, itemCompany, itemInfo, price, imageBase64, 0, 0.0, 0.0);

                /// save the item to the database and get the result in the callback
                databaseService.createNewItem(newItem, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Log.d("TAG", "Item added successfully");
                        Toast.makeText(AddItem.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                        /// clear the input fields after adding the item for the next item
                        Log.d("TAG", "Clearing input fields");

                        Intent intent = new Intent(AddItem.this, AdminPage.class);
                        startActivity(intent);


                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e("TAG", "Failed to add item", e);
                        Toast.makeText(AddItem.this, "Failed to add food", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        });
    }

    private void InitViews() {
        etItemName = findViewById(R.id.etItemName);
        etItemInfo = findViewById(R.id.etItemInfo);
        etItemPrice = findViewById(R.id.etItemPrice);
        spType = findViewById(R.id.spType);
        spColor = findViewById(R.id.spColor);
        spCompany = findViewById(R.id.spCompany);
        btnGallery = findViewById(R.id.btnGallery);
        btnTakePic = findViewById(R.id.btnTakePic);
        btnAddItem = findViewById(R.id.btnAddItem);
        imageView = findViewById(R.id.imageView);
    }


    /// select image from gallery
            private void selectImageFromGallery() {
                //   Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //  selectImageLauncher.launch(intent);

                imageChooser();
            }

            /// capture image from camera
            private void captureImageFromCamera() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureImageLauncher.launch(takePictureIntent);
            }





            void imageChooser() {

                // create an instance of the
                // intent of the type image
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);

                // pass the constant to compare it
                // with the returned requestCode
                startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
            }

            // this function is triggered when user
            // selects the image from the imageChooser
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);

                if (resultCode == RESULT_OK) {

                    // compare the resultCode with the
                    // SELECT_PICTURE constant
                    if (requestCode == SELECT_PICTURE) {
                        // Get the url of the image from data
                        Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            // update the preview image in the layout
                            imageView.setImageURI(selectedImageUri);
                        }
                    }
                }
            }
        }




