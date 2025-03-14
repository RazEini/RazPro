package com.shop.bagrutproject.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shop.bagrutproject.models.Cart;
import com.shop.bagrutproject.models.Comment;
import com.shop.bagrutproject.models.Item;
import com.shop.bagrutproject.models.Order;
import com.shop.bagrutproject.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/// a service to interact with the Firebase Realtime Database.
/// this class is a singleton, use getInstance() to get an instance of this class
/// @see #getInstance()
/// @see FirebaseDatabase

public class DatabaseService {

    /// tag for logging
    /// @see Log
    private static final String TAG = "DatabaseService";

    /// callback interface for database operations
    /// @param <T> the type of the object to return
    /// @see DatabaseCallback#onCompleted(Object)
    /// @see DatabaseCallback#onFailed(Exception)
    public interface DatabaseCallback<T> {
        /// called when the operation is completed successfully
        void onCompleted(T object);

        /// called when the operation fails with an exception
        void onFailed(Exception e);
    }

    /// the instance of this class
    /// @see #getInstance()
    private static DatabaseService instance;

    /// the reference to the database
    /// @see DatabaseReference
    /// @see FirebaseDatabase#getReference()
    private final DatabaseReference databaseReference;

    /// use getInstance() to get an instance of this class
    /// @see DatabaseService#getInstance()
    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    /// get an instance of this class
    /// @return an instance of this class
    /// @see DatabaseService
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }


    // private generic methods to write and read data from the database

    /// write data to the database at a specific path
    /// @param path the path to write the data to
    /// @param data the data to write (can be any object, but must be serializable, i.e. must have a default constructor and all fields must have getters and setters)
    /// @param callback the callback to call when the operation is completed
    /// @return void
    /// @see DatabaseCallback
    private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        databaseReference.child(path).setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback == null) return;
                callback.onCompleted(task.getResult());
            } else {
                if (callback == null) return;
                callback.onFailed(task.getException());
            }
        });
    }

    /// read data from the database at a specific path
    /// @param path the path to read the data from
    /// @return a DatabaseReference object to read the data from
    /// @see DatabaseReference

    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }


    /// get data from the database at a specific path
    /// @param path the path to get the data from
    /// @param clazz the class of the object to return
    /// @param callback the callback to call when the operation is completed
    /// @return void
    /// @see DatabaseCallback
    /// @see Class
    private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    /// get a list of data from the database at a specific path
    /// @param path the path to get the data from
    /// @param clazz the class of the objects to return
    /// @param callback the callback to call when the operation is completed
    private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull Map<String, String> filter, @NotNull final DatabaseCallback<List<T>> callback) {
        Query dbRef = readData(path);

        for (Map.Entry<String, String> entry : filter.entrySet()) {
            dbRef = dbRef.orderByChild(entry.getKey()).equalTo(entry.getValue());
        }

        dbRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<T> tList = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                T t = dataSnapshot.getValue(clazz);
                tList.add(t);
            });

            callback.onCompleted(tList);
        });
    }


    /// generate a new id for a new object in the database
    /// @param path the path to generate the id for
    /// @return a new id for the object
    /// @see String
    /// @see DatabaseReference#push()

    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    // end of private methods for reading and writing data

    // public methods to interact with the database

    /// create a new user in the database
    /// @param user the user object to create
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive void
    ///            if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see User
    public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData("Users/" + user.getUid(), user, callback);
    }

  

    /// get a user from the database
    /// @param uid the id of the user to get
    /// @param callback the callback to call when the operation is completed
    ///               the callback will receive the user object
    ///             if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see User
    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData("Users/" + uid, User.class, callback);
    }

    /// get a cart from the database
    /// @param uid the id of the cart to get
    /// @param callback the callback to call when the operation is completed
    ///                the callback will receive the cart object
    ///               if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see Cart
    public void getCart(@NotNull final  String uid,@NotNull final DatabaseCallback<Cart> callback) {
        getData("Users/" + uid+"/cart" , Cart.class, callback);
    }

    /// generate a new id for a new item in the database
    /// @return a new id for the item
    /// @see #generateNewId(String)
    /// @see Item
    public String generateItemId() {
        return generateNewId("items");
    }

    /// generate a new id for a new cart in the database
    /// @return a new id for the cart
    /// @see #generateNewId(String)
    /// @see Cart
    public String generateCartId() {
        return generateNewId("carts");
    }

    /// create a new item in the database
    /// @param item the item object to create
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive void
    ///             if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see Item
    public void createNewItem(@NotNull final Item item, @Nullable final DatabaseCallback<Void> callback) {
        writeData("items/" + item.getId(), item, callback);
    }

    /// create a new cart in the database
    /// @param cart the cart object to create
    /// @param callback the callback to call when the operation is completed
    ///               the callback will receive void
    ///              if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see Cart
    public void updateCart(@NotNull final Cart cart,String uid ,@Nullable final DatabaseCallback<Void> callback) {
        writeData("Users/" + uid+"/cart", cart, callback);
    }


    public void updateUser(@NotNull final User user ,@Nullable final DatabaseCallback<Void> callback) {
        writeData("Users/" + user.getUid(), user, callback);
    }





    /// get a item from the database
    /// @param itemId the id of the item to get
    /// @param callback the callback to call when the operation is completed
    ///               the callback will receive the item object
    ///              if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see Item
    public void getItem(@NotNull final String itemId, @NotNull final DatabaseCallback<Item> callback) {
        getData("items/" + itemId, Item.class, callback);
    }




    /// get all the items from the database
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive a list of item objects
    ///            if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see List
    /// @see Item
    /// @see #getData(String, Class, DatabaseCallback)
    public void getItems(@NotNull final DatabaseCallback<List<Item>> callback) {
        getDataList("items", Item.class, new HashMap<>(), callback);
    }

    /// get all the users from the database
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive a list of item objects
    ///            if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see List
    /// @see Item
    /// @see #getData(String, Class, DatabaseCallback)
    public void getUsers(@NotNull final DatabaseCallback<List<User>> callback) {
        readData("Users").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<User> users = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                User user = dataSnapshot.getValue(User.class);
                Log.d(TAG, "Got user: " + user);
                users.add(user);
            });

            callback.onCompleted(users);
        });
    }

    public void deleteUser(String uid, DatabaseCallback<Void> callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onCompleted(null);
            } else {
                callback.onFailed(task.getException());
            }
        });
    }

    public void getOrder(String orderId, DatabaseCallback<Order> callback) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Order order = snapshot.getValue(Order.class);
                if (order != null) {
                    callback.onCompleted(order);
                } else {
                    callback.onFailed(new Exception("Order not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailed(error.toException());
            }
        });
    }

    public void clearCart(String userId, DatabaseCallback<Void> callback) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId);
        cartRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onCompleted(null);
                    } else {
                        callback.onFailed(task.getException());
                    }
                });
    }

    public void createCartForUser(String uid, Cart cart, DatabaseCallback<Void> callback) {
        // Reference to the Firebase database under the user's cart node
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(uid);

        // Save the cart to the Firebase database
        cartRef.setValue(cart)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Cart created and saved successfully.");
                        if (callback != null) {
                            callback.onCompleted(null); // Indicate successful creation
                        }
                    } else {
                        Log.e(TAG, "Failed to create cart", task.getException());
                        if (callback != null) {
                            callback.onFailed(task.getException()); // Pass the error
                        }
                    }
                });
    }

    public void getOrders(String userId, final DatabaseCallback<List<Order>> callback) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Order> orders = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Order order = snapshot.getValue(Order.class);
                            orders.add(order);
                        }
                        callback.onCompleted(orders);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onFailed(databaseError.toException());
                    }
                }
        );
    }

    public void getComments(String itemId, DatabaseCallback<List<Comment>> callback) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(itemId);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Comment> commentList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                callback.onCompleted(commentList);
                // עדכון הדירוג הממוצע לאחר הוספת תגובה חדשה
                updateAverageRating(itemId, new DatabaseCallback<Double>() {
                    @Override
                    public void onCompleted(Double result) {
                        // אפשר כאן לעדכן את הדירוג הממוצע ב-UI אם צריך
                    }

                    @Override
                    public void onFailed(Exception exception) {
                        // טיפול בשגיאה אם יש
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailed(databaseError.toException());
            }
        });
    }

    // פונקציה שתחשב את הממוצע של הדירוגים
    public void updateAverageRating(String itemId, DatabaseCallback<Double> callback) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(itemId);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double sum = 0;
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    if (comment != null) {
                        sum += comment.getRating();
                        count++;
                    }
                }
                double averageRating = count > 0 ? sum / count : 0;
                // עדכון הדירוג הממוצע של המוצר
                DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("items").child(itemId);
                itemRef.child("averageRating").setValue(averageRating)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                callback.onCompleted(averageRating);
                            } else {
                                callback.onFailed(task.getException());
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailed(databaseError.toException());
            }
        });
    }
}


