package com.example.android.inventoryappstage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    /*
     * Get Local symbol currency
     */
    NumberFormat mformatCurrency = NumberFormat.getCurrencyInstance(Locale.getDefault());
    /**
     * Button to call supplier
     */
    Button buttonCallSupplier;
    /**
     * Button for increase quantity
     */
    Button buttonIncreaseQuantity;
    /**
     * Button for decrease quantity
     */
    Button buttonDecreaseQuantity;
    /**
     * Button for save changes
     */
    Button buttonSaveChange;
    /**
     * Content URI
     */
    private Uri mCurrentProductUri;
    /**
     * TextView for the product name
     */
    private TextView mNameTextView;
    /**
     * TextView for the color
     */
    private TextView mColorTextView;
    /**
     * TextView field for the price
     */
    private TextView mPriceTextView;
    /**
     * TextView for the quantiy
     */
    private TextView mQuantityTextView;
    /**
     * TextView for the supplier name
     */
    private TextView mSupplierTextView;
    /**
     * TextView for the supplier telephone
     */
    private TextView mTelephoneTextView;
    /**
     * ImageView to show the product image
     */
    private ImageView mProductImageView;

    //Variable for increase and decrease button click
    private int mProductQuantity;

    //Variable for global quantity store from db
    private int mQuantityDefault;

    /**
     * Boolean flag that keeps track of whether the quantity has changed
     */
    private boolean mQuantityHasChanged = false;


    /*
     * Listener for call supplier button
     */
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.call_supplier:
                    callSupplier();
                    break;
                case R.id.button_increase_n:
                    mProductQuantity = Integer.parseInt(mQuantityTextView.getText().toString().trim());
                    mQuantityHasChanged = true;
                    mProductQuantity++;
                    mQuantityTextView.setText(String.valueOf(mProductQuantity));
                    buttonSaveChange.setVisibility(View.VISIBLE);
                    break;
                case R.id.button_decrease_n:
                    mProductQuantity = Integer.parseInt(mQuantityTextView.getText().toString().trim());
                    mQuantityHasChanged = true;
                    //If the quantity is higher than 0 decrease and show quantity
                    if (mProductQuantity > 0) {
                        mProductQuantity--;
                        mQuantityTextView.setText(String.valueOf(mProductQuantity));
                        buttonSaveChange.setVisibility(View.VISIBLE);
                    } else {
                        // Otherwise doesn't decrease and show message to user.
                        Toast.makeText(DetailActivity.this, getString(R.string.toast_msg_no_decrease), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.button_save_change:
                    updateProduct();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Initialize a loader
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        mNameTextView = findViewById(R.id.detail_product_name);
        mColorTextView = findViewById(R.id.detail_color);
        mPriceTextView = findViewById(R.id.detail_price);
        mQuantityTextView = findViewById(R.id.detail_quantity);
        mSupplierTextView = findViewById(R.id.detail_supplier);
        mTelephoneTextView = findViewById(R.id.detail_telephone_supplier);
        mProductImageView = findViewById(R.id.detail_image);


        // Find and Set listener for button call supllier
        buttonCallSupplier = findViewById(R.id.call_supplier);
        buttonCallSupplier.setOnClickListener(mOnClickListener);

        // Find id for button save change and set for click listner
        buttonSaveChange = findViewById(R.id.button_save_change);
        buttonSaveChange.setOnClickListener(mOnClickListener);

        //Find the decrease and increase button view
        buttonDecreaseQuantity = findViewById(R.id.button_decrease_n);
        buttonIncreaseQuantity = findViewById(R.id.button_increase_n);
        //Set OnCliCkListener for the increase, decrease
        buttonDecreaseQuantity.setOnClickListener(mOnClickListener);
        buttonIncreaseQuantity.setOnClickListener(mOnClickListener);

        //Se fab to call edit activity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // If the quantity hasn't changed, go to intent.
                if (!mQuantityHasChanged) {
                    Intent intent = new Intent(DetailActivity.this, EditorActivity.class);
                    intent.setData(mCurrentProductUri);
                    startActivity(intent);
                } else {
                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    showSaveConfirmationDialog(false);
                }

            }
        });

        if (savedInstanceState != null) {
            mQuantityHasChanged = savedInstanceState.getBoolean("mQuantityHasChanged");
            mQuantityTextView.setText(savedInstanceState.getString("textQuantity"));
            buttonSaveChange.setVisibility(savedInstanceState.getInt("visibility"));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_COLOR,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_IMAGE,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            // Find the columns of product attributes that we're interested in
            int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int colorColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_COLOR);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int telephoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            int id = cursor.getInt(idColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int color = cursor.getInt(colorColumnIndex);
            double price = Double.parseDouble(cursor.getString(priceColumnIndex)) / 100;
            mQuantityDefault = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            // It's need a long type for number > 9 figure
            long telephone = cursor.getLong(telephoneColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            setTitle(getString(R.string.detail_activity_title_edit) + id);

            if (image != null && !TextUtils.isEmpty(image)) {
                Uri imageUri = Uri.parse(image);
                mProductImageView.setImageURI(imageUri);
            }

            // Update the views on the screen with the values from the database
            mNameTextView.setText(name);
            mPriceTextView.setText(String.valueOf(mformatCurrency.format(price)));
            mQuantityTextView.setText(String.valueOf(mQuantityDefault));


            mSupplierTextView.setText(supplier);

            //Check if telephone has not zero and set the TextView
            if (telephone > 0) {
                String sTelephone = Long.toString(telephone);
                mTelephoneTextView.setText(sTelephone);

            } else {
                mTelephoneTextView.setText("");
            }

            mColorTextView.setText(InventoryEntry.getColorText(getApplicationContext(), color));
            colorDrawable(color);

        }

    }

    /**
     * Set the drawable resource with the current color product
     */
    public void colorDrawable(int nColor) {
        int[] aColor;
        ImageView myImg = findViewById(R.id.colorShape);
        Resources res = getResources();
        aColor = res.getIntArray(R.array.array_colors);
        //get drawable from image button
        GradientDrawable drawable = (GradientDrawable) myImg.getDrawable();
        drawable.setColor(aColor[nColor]);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameTextView.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_settings:
                Toast.makeText(this, getString(R.string.toast_action_settings),
                        Toast.LENGTH_SHORT).show();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the quantity hasn't changed, continue with navigating up to previous activity
                if (!mQuantityHasChanged) {
                    finish();
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                showSaveConfirmationDialog(true);

                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        // If the quantity hasn't changed, continue with handling back button press
        if (!mQuantityHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        showSaveConfirmationDialog(true);

    }

    // Check if finish or start activity
    public void ifFinish(boolean ifFinish) {
        if (!ifFinish) {
            mQuantityHasChanged = false;
            mQuantityTextView.setText(String.valueOf(mQuantityDefault));
            buttonSaveChange.setVisibility(View.GONE);
            Intent intent = new Intent(DetailActivity.this, EditorActivity.class);
            intent.setData(mCurrentProductUri);
            startActivity(intent);
            return;
        }
        finish();
    }


    /**
     * Prevent lose edit information and state of button save when the screen is rotated.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mQuantityHasChanged", mQuantityHasChanged);
        outState.putString("textQuantity", mQuantityTextView.getText().toString());
        outState.putInt("visibility", buttonSaveChange.getVisibility());

    }

    /**
     * It's important for save instance of TextView of quantity when rotate screen
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mQuantityTextView.setText(savedInstanceState.getString("textQuantity"));
    }

    /**
     * Set the dialog of save confirmation
     */
    public void showSaveConfirmationDialog(boolean ifFinish) {
        final boolean ifFinishBoolean = ifFinish;
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_quantity_changes_dialog_msg);
        builder.setPositiveButton(R.string.save_change, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Save" button.
                updateProduct();
                ifFinish(ifFinishBoolean);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
                ifFinish(ifFinishBoolean);
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    public void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button.
                deleteProduct();
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

    /**
     * Update the quantity of product
     */
    private void updateProduct() {
        String quantityString = mQuantityTextView.getText().toString().trim();

        int quantity = Integer.parseInt(quantityString);

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);

        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
                mQuantityHasChanged = false;
                buttonSaveChange.setVisibility(View.GONE);
            }
        }

    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void callSupplier() {

        //If in View mode and if the number is valid, call intent to phone app
        if (mCurrentProductUri != null) {
            String number = mTelephoneTextView.getText().toString();
            if (validCellPhone(number)) {
                startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + number)));
            } else {
                Toast.makeText(this, R.string.toast_invalid_telephone, Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean validCellPhone(String number) {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }
}
