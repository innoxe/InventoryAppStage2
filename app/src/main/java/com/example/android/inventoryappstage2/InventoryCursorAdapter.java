package com.example.android.inventoryappstage2;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;

import java.text.NumberFormat;

public class InventoryCursorAdapter extends CursorAdapter {

    private Uri mCurrentUri;
    private Context mContext;
    private Intent mIntent;

    public InventoryCursorAdapter(Context context, Cursor c) {

        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Set the click item of 3dot button in the list
        mContext = context;

        // Find id of buttn for the menu in the single itemlist
        final ImageButton btn = view.findViewById(R.id.button_item_list);
        // Find id of sale button
        Button saleButton = view.findViewById(R.id.sale);

        final Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI,
                cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry._ID)));

        // Find individual views that we want to modify in the list item layout
        TextView priceTextView = view.findViewById(R.id.price);
        TextView nameTextView = view.findViewById(R.id.name);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        ImageView imageTextView = view.findViewById(R.id.imageList);

        //Instance currency format
        NumberFormat format = NumberFormat.getCurrencyInstance();

        // Find the columns of product attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);

        // Read the product attributes from the Cursor for the current product
        final int id = cursor.getInt(idColumnIndex);
        String image = cursor.getString(imageColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        double productPrice = Double.parseDouble(cursor.getString(priceColumnIndex)) / 100;

        // Update the Views with the attributes for the current product
        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(format.format(productPrice)));
        if (image != null && !TextUtils.isEmpty(image)) {
            Uri imageUri = Uri.parse(image);
            imageTextView.setImageURI(imageUri);
        }
        quantityTextView.setText(mContext.getResources().getString(R.string.list_label_quantity, quantity));

        // Set listener to the button sale in the list
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if quantity  is higher than zero
                if (quantity > 0){
                    int newQuantity = quantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_QUANTITY, newQuantity);
                    // Update the database
                    mContext.getContentResolver().update(uri,values,null,null);
                } else {
                    // Notify the user that quantity is less than zero and cannot be updated
                    Toast.makeText(mContext,mContext.getString(R.string.toast_msg_no_decrease),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Set the listener of 3dot button in the list
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                PopupMenu popupMenu = new PopupMenu(mContext, btn);
                popupMenu.getMenuInflater().inflate(R.menu.menu_item_list, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int i = item.getItemId();
                        if (i == R.id.action_list_details) {
                            mIntent = new Intent(mContext, DetailActivity.class);
                            mIntent.setData(mCurrentUri);
                            mContext.startActivity(mIntent);
                            return true;
                        } else if (i == R.id.action_list_edit) {
                            mIntent = new Intent(mContext, EditorActivity.class);
                            // Set the URI on the data field of the intent
                            mIntent.setData(mCurrentUri);
                            mContext.startActivity(mIntent);
                            return true;
                        } else if (i == R.id.action_list_delete) {
                            showDeleteConfirmationDialog();
                            return true;
                        } else {
                            return onMenuItemClick(item);
                        }
                    }
                });
                popupMenu.show();

            }
        });
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button.
                if (mCurrentUri != null) {
                    // Call the ContentResolver to delete the product at the given content URI.
                    int rowsDeleted = mContext.getContentResolver().delete(mCurrentUri, null, null);

                    // Show a toast message depending on whether or not the delete was successful.
                    if (rowsDeleted == 0) {
                        // If no rows were deleted, then there was an error with the delete.
                        Toast.makeText(mContext, mContext.getString(R.string.editor_delete_product_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the delete was successful and we can display a toast.
                        Toast.makeText(mContext, mContext.getString(R.string.editor_delete_product_successful),
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
