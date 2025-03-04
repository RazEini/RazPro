package com.shop.bagrutproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shop.bagrutproject.R;
import com.shop.bagrutproject.adapters.CartAdapter;
import com.shop.bagrutproject.models.Cart;
import com.shop.bagrutproject.models.Item;
import com.shop.bagrutproject.models.Order;
import com.shop.bagrutproject.models.User;
import com.shop.bagrutproject.services.DatabaseService;
import com.shop.bagrutproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private static final String TAG = "CartActivity";

    private ListView cartListView;
    private TextView totalPriceText;
    private Button checkoutButton;
    private Cart cart;
    private CartAdapter cartAdapter;
    private DatabaseService databaseService;
    private User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartListView = findViewById(R.id.lvCart);
        totalPriceText = findViewById(R.id.cartItemsText);
        checkoutButton = findViewById(R.id.checkoutButton); // כפתור חדש

        databaseService = DatabaseService.getInstance();
        user = SharedPreferencesUtil.getUser(this);

        if (user == null) {
            return;
        }

        cartAdapter = new CartAdapter(this, new ArrayList<>(), new CartAdapter.OnCartClick() {
            @Override
            public void onItemLongClick(final int position, final Item cartItem) {
                cart.removeItem(position);
                databaseService.updateCart(cart, user.getUid(), new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        cartAdapter.removeItem(position);
                        updateTotalPrice();
                    }

                    @Override
                    public void onFailed(Exception e) {
                    }
                });
            }
        });

        cartListView.setAdapter(cartAdapter);

        databaseService.getCart(user.getUid(), new DatabaseService.DatabaseCallback<Cart>() {
            @Override
            public void onCompleted(Cart object) {
                cart = object;
                cartAdapter.setItems(cart.getItems());
                updateTotalPrice();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "onFailed: Failed to read cart", e);
            }
        });

        // כפתור "סיום הזמנה"
        checkoutButton.setOnClickListener(v -> processOrder());
    }

    private void updateTotalPrice() {
        double totalPrice = 0;
        for (Item item : cart.getItems()) {
            totalPrice += item.getPrice();
        }
        totalPriceText.setText("סך הכל: ₪" + totalPrice);
    }

    public void ReturnToShop(View view) {
        Intent intent = new Intent(CartActivity.this, ShopActivity.class);
        startActivity(intent);
    }

    private void processOrder() {
        if (cart == null || cart.getItems().isEmpty()) {
            Toast.makeText(this, "העגלה ריקה!", Toast.LENGTH_SHORT).show();
            return;
        }

        Order order = new Order(user.getUid(), cart.getItems());

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.child(order.getOrderId()).setValue(order)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CartActivity.this, "הזמנה נשמרה!", Toast.LENGTH_SHORT).show();

                    // מעבר לעמוד סיכום ההזמנה
                    Intent intent = new Intent(CartActivity.this, OrderSummaryActivity.class);
                    intent.putExtra("orderId", order.getOrderId());
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(CartActivity.this, "שגיאה בשמירת ההזמנה", Toast.LENGTH_SHORT).show()
                );
    }

}
