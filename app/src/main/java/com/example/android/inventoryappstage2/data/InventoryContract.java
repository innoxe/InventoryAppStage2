package com.example.android.inventoryappstage2.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.android.inventoryappstage2.R;

public class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappstage2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    private InventoryContract() {
    }

    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        // Name of database table
        public final static String TABLE_NAME = "inventory";

        // ID column
        public final static String _ID = BaseColumns._ID;
        // Product name colomn
        public final static String COLUMN_PRODUCT_NAME = "ProductName";
        // Color column
        public final static String COLUMN_COLOR = "Color";
        // Price column
        public final static String COLUMN_PRICE = "Price";
        // Quantity column
        public final static String COLUMN_QUANTITY = "Quantity";
        // Product image
        public static final String COLUMN_IMAGE = "Image";
        // Supplier name column
        public final static String COLUMN_SUPPLIER_NAME = "SupplierName";
        // Supplier phone number column
        public final static String COLUMN_SUPPLIER_PHONE_NUMBER = "SupplierPhoneNumber";

        /**
         * Possible values for the color column
         */
        public static final int COLOR_UNKNOWN = 0;
        public static final int COLOR_BLACK = 1;
        public static final int COLOR_WHITE = 2;
        public static final int COLOR_RED = 3;
        public static final int COLOR_BLUE = 4;


        public static boolean isValidColor(int color) {
            if (color == COLOR_UNKNOWN || color == COLOR_BLACK || color == COLOR_WHITE || color == COLOR_RED || color == COLOR_BLUE) {
                return true;
            }
            return false;
        }

        /**
         * Method to get the name of the color from the array that already exists for spinner
         */
        public static String getColorText(Context context, int nColor) {
            String[] aColor;
            Resources res = context.getResources();
            aColor = res.getStringArray(R.array.array_color_options);
            return aColor[nColor];
        }
    }

}
