package com.shop.bagrutproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shop.bagrutproject.R;
import com.shop.bagrutproject.models.Item;
import com.shop.bagrutproject.utils.ImageUtil;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView itemName, itemPrice, itemInfo, itemCompany, itemColor, itemType;
    private ImageView itemImage;
    private DatabaseReference databaseReference;
    private String itemId;
    private Button btnGoBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // קבלת ה-ID של המוצר שנבחר
        itemId = getIntent().getStringExtra("itemId");

        itemName = findViewById(R.id.itemName);
        itemPrice = findViewById(R.id.itemPrice);
        itemInfo = findViewById(R.id.itemInfo);
        itemCompany = findViewById(R.id.itemCompany);
        itemColor = findViewById(R.id.itemColor);
        itemType = findViewById(R.id.itemType);
        itemImage = findViewById(R.id.itemImage);

        btnGoBack = findViewById(R.id.btnGoToShop);

        btnGoBack.setOnClickListener(v -> {
            Intent intent = new Intent(ItemDetailActivity.this, ShopActivity.class);
            startActivity(intent);
            finish();
        });

        // משיכת המידע של המוצר מ-Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("items").child(itemId);

        fetchItemDetails();
    }

    private void fetchItemDetails() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Item item = dataSnapshot.getValue(Item.class);
                if (item != null) {
                    itemName.setText(item.getName());
                    itemPrice.setText("₪" + item.getPrice());
                    itemInfo.setText(item.getAboutItem());
                    itemCompany.setText(item.getCompany());
                    itemColor.setText(item.getColor());
                    itemType.setText(item.getType());

                    if (item.getPic() != null && !item.getPic().isEmpty()) {
                        itemImage.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
                    } else {
                        itemImage.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // טיפול בשגיאות
            }
        });
    }
}

