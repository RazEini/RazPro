package com.shop.bagrutproject.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shop.bagrutproject.R;
import com.shop.bagrutproject.models.Deal;
import com.shop.bagrutproject.models.Item;
import com.shop.bagrutproject.services.DatabaseService;
import com.shop.bagrutproject.utils.ImageUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends BaseAdapter {

    public interface OnCartClick {
        void onItemCheckedChanged(int position, boolean isChecked);
    }

    private final Context context;
    private final List<Item> cartItems;
    private final boolean showCheckbox;
    @Nullable
    private final OnCartClick onCartClick;
    private final DatabaseService databaseService;

    Map<String, Deal> deals = new HashMap<>(); // item type -> deal


    public CartAdapter(Context context, List<Item> cartItems, @Nullable OnCartClick onCartClick, boolean showCheckbox) {
        this.context = context;
        this.cartItems = cartItems;
        this.onCartClick = onCartClick;
        this.showCheckbox = showCheckbox;
        this.databaseService = DatabaseService.getInstance();
    }

    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public Object getItem(int position) {
        return cartItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cart_item_layout, parent, false);
        }

        final Item item = cartItems.get(position);

        TextView itemName = convertView.findViewById(R.id.itemName);
        TextView itemPrice = convertView.findViewById(R.id.itemPrice);
        ImageView itemImage = convertView.findViewById(R.id.itemImage);
        CheckBox deleteCheckBox = convertView.findViewById(R.id.deleteCheckBox);

        itemName.setText(item.getName());
        itemPrice.setText("₪" + item.getPrice());

        if (item.getPic() != null && !item.getPic().isEmpty()) {
            itemImage.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
        } else {
            itemImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // שליטה על הנראות של CheckBox
        if (showCheckbox) {
            deleteCheckBox.setVisibility(View.VISIBLE);
        } else {
            deleteCheckBox.setVisibility(View.GONE);
        }

        // ניתוק מאזין קודם
        deleteCheckBox.setOnCheckedChangeListener(null);

        // הגדרה מחדש
        deleteCheckBox.setChecked(false);  // או שתעדכן לפי המצב האמיתי אם צריך

        deleteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (onCartClick != null) {
                onCartClick.onItemCheckedChanged(position, isChecked);
            }
        });

        // עדכון המחיר עם הנחה אם יש
        updatePriceWithDeal(item, convertView);

        return convertView;
    }



    private void updatePriceWithDeal(Item item, View convertView) {
        TextView oldPriceTextView = convertView.findViewById(R.id.oldPriceTextView);
        TextView itemPrice = convertView.findViewById(R.id.itemPrice);

        Deal deal = this.deals.get(item.getType());

        if (deal == null || !deal.isValid()) {
            // אין מבצע או שהמבצע אינו בתוקף – מציגים מחיר רגיל בלבד
            itemPrice.setText("₪" + item.getPrice());
            oldPriceTextView.setVisibility(View.GONE);
            return;
        }

        // אם יש מבצע בתוקף
        double discount = deal.getDiscountPercentage();
        double originalPrice = item.getPrice();
        double finalPrice = originalPrice * (1 - discount / 100);

        // הצגת המחיר לאחר הנחה
        itemPrice.setText("₪" + finalPrice);

        // הצגת המחיר המקורי עם קו חוצה בצבע אדום
        oldPriceTextView.setVisibility(View.VISIBLE);
        SpannableString spannableString = new SpannableString("₪" + originalPrice);
        spannableString.setSpan(new StrikethroughSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        oldPriceTextView.setText(spannableString);
    }

    public void setItems(List<Item> items) {
        this.cartItems.clear();
        this.cartItems.addAll(items);
        this.notifyDataSetChanged();
    }

    public void removeItem(int position) {
        this.cartItems.remove(position);
        this.notifyDataSetChanged();
    }


    public void setDeals(Collection<Deal> deals) {
        this.deals.clear();
        for (Deal deal : deals) {
            this.deals.put(deal.getItemType(), deal);
        }
        notifyDataSetChanged();
    }
}
