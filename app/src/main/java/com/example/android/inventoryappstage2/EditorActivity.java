package com.example.android.inventoryappstage2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;

import java.text.NumberFormat;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int READ_REQUEST_CODE = 42;
    /*
     * Get Local symbol currency
     */
    NumberFormat mformatCurrency = NumberFormat.getCurrencyInstance(Locale.getDefault());
    String symbolCurrency = mformatCurrency.getCurrency().getSymbol();
    /**
     * Button for increase quantity
     */
    Button buttonIncreaseQuantity;
    /**
     * Button for decrease quantity
     */
    Button buttonDecreaseQuantity;
    /**
     * EditText field to enter the product name
     */
    private EditText mNameEditText;
    /**
     * Spinner field to enter the color product
     */
    private Spinner mColorSpinner;
    /**
     * EditText field to enter the price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the quantiy
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the supplier name
     */
    private EditText mSupplierEditText;
    /**
     * EditText field to enter the supplier telephone
     */
    private EditText mTelephoneEditText;
    /**
     * EditText field to enter the supplier telephone
     */
    private ImageView mProductImageView;
    /**
     * Content URI
     */
    private Uri mCurrentProductUri;
    private int mProductQuantity;
    private Uri imageUri = null;
    /**
     * Color of the product. The possible valid values are in the InventoryContract.java file:
     * {@link InventoryEntry#COLOR_UNKNOWN}, {@link InventoryEntry#COLOR_BLACK},
     * {@link InventoryEntry#COLOR_WHITE},{@link InventoryEntry#COLOR_RED} or {@link InventoryEntry#COLOR_BLUE}.
     */
    private int mColor = InventoryEntry.COLOR_UNKNOWN;

    /**
     * Boolean flag that keeps track of whether the product has been edited
     */
    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mProductHasChanged = true;
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }

    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_increase_n:
                    mProductQuantity = getInteger(mQuantityEditText.getText().toString().trim());
                    mProductQuantity++;
                    mQuantityEditText.setError(null);
                    mQuantityEditText.setText(String.valueOf(mProductQuantity));
                    break;
                case R.id.button_decrease_n:
                    mProductQuantity = getInteger(mQuantityEditText.getText().toString().trim());
                    //If the quantity is higher than 0 decrease and show quantity
                    if (mProductQuantity > 0) {
                        mProductQuantity--;
                        mQuantityEditText.setText(String.valueOf(mProductQuantity));
                    } else {
                        // Otherwise doesn't decrease and show message to user.
                        Toast.makeText(EditorActivity.this, getString(R.string.toast_msg_no_decrease), Toast.LENGTH_SHORT).show();
                        mQuantityEditText.setError(getText(R.string.less_than_zero_field));
                    }
                    break;
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a content URI, we know add new a product else edit.
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit));
            // Initialize a loader
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        }

        // Set the label TextView whit local symbol currency
        TextView currencyView = findViewById(R.id.label_price_currency);
        currencyView.setText(symbolCurrency);

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_product_name);
        mColorSpinner = findViewById(R.id.spinner_color);
        mPriceEditText = findViewById(R.id.edit_price);
        mQuantityEditText = findViewById(R.id.edit_quantity);
        mSupplierEditText = findViewById(R.id.edit_supplier);
        mTelephoneEditText = findViewById(R.id.edit_telephone_supplier);
        mProductImageView = findViewById(R.id.product_image);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mColorSpinner.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mTelephoneEditText.setOnTouchListener(mTouchListener);


        //Find the decrease and increase button view
        buttonDecreaseQuantity = findViewById(R.id.button_decrease_n);
        buttonIncreaseQuantity = findViewById(R.id.button_increase_n);
        //Set OnCliCkListener for the increase, decrease
        buttonDecreaseQuantity.setOnClickListener(mOnClickListener);
        buttonIncreaseQuantity.setOnClickListener(mOnClickListener);

        setupSpinner();

        Button selectImageButton = findViewById(R.id.button_choose_image);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        if (savedInstanceState != null) {
            mProductHasChanged = savedInstanceState.getBoolean("mProductHasChanged");
        }

    }

    /**
     * Intent for select an image.
     */
    public void chooseImage() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == READ_REQUEST_CODE) {
            imageUri = data.getData();
            mProductImageView.setImageURI(imageUri);
            Toast.makeText(getBaseContext(), R.string.editor_update_image, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Setup the dropdown spinner that allows the user to select the color of product.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter colorSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_color_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        colorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mColorSpinner.setAdapter(colorSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.color_black))) {
                        mColor = InventoryEntry.COLOR_BLACK;
                    } else if (selection.equals(getString(R.string.color_white))) {
                        mColor = InventoryEntry.COLOR_WHITE;
                    } else if (selection.equals(getString(R.string.color_red))) {
                        mColor = InventoryEntry.COLOR_RED;
                    } else if (selection.equals(getString(R.string.color_blue))) {
                        mColor = InventoryEntry.COLOR_BLUE;
                    } else {
                        mColor = InventoryEntry.COLOR_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mColor = InventoryEntry.COLOR_UNKNOWN;
            }
        });
    }

    /**
     * Check if the string is empty before to parseInt.
     */
    public int getInteger(String no) {
        if (!TextUtils.isEmpty(no)) {
            return Integer.parseInt(no); //convert string into integer
        } else {
            return 0; // if string is empty
        }
    }

    /**
     * Get user input from editor and save the product into database
     */
    private void saveProduct() {
        double price;

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        // Transform price in decimal
        if (priceString.contains(".")) {
            price = Double.parseDouble(priceString) * 100;
        } else {
            price = getInteger(priceString) * 100;
        }
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity = getInteger(quantityString);
        String supplierString = mSupplierEditText.getText().toString().trim();
        String telephoneString = mTelephoneEditText.getText().toString().trim();

        // Check if URI is null and all EditText empty. If true it doesn't save in the database
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(telephoneString) && mColor == InventoryEntry.COLOR_UNKNOWN) {
            showEmptyConfirmationDialog();
            return;
        }

        // Check if the required field are empyt
        // They are required
        if (nameString.isEmpty()) {
            mNameEditText.setError(getText(R.string.empty_field));
            Toast.makeText(this, getString(R.string.invalid_product_name_add_toast), Toast.LENGTH_SHORT).show();
            return;
        } else if (priceString.isEmpty()) {
            mPriceEditText.setError(getText(R.string.empty_field));
            Toast.makeText(this, getString(R.string.invalid_product_price_add_toast), Toast.LENGTH_SHORT).show();
            return;
        } else if (quantityString.isEmpty()) {
            mQuantityEditText.setError(getText(R.string.empty_field));
            Toast.makeText(this, getString(R.string.invalid_product_quantity_add_toast), Toast.LENGTH_SHORT).show();
            return;
        } else if (supplierString.isEmpty()) {
            mSupplierEditText.setError(getText(R.string.empty_field));
            Toast.makeText(this, getString(R.string.invalid_product_supplier_name_toast), Toast.LENGTH_SHORT).show();
            return;
        } else if (telephoneString.isEmpty()) {
            mTelephoneEditText.setError(getText(R.string.empty_field));
            Toast.makeText(this, getString(R.string.invalid_product_supplier_phone_toast), Toast.LENGTH_SHORT).show();
            return;
        } else if (imageUri == null) {
            Toast.makeText(this, getString(R.string.invalid_product_image_add_toast), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_COLOR, mColor);
        values.put(InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, telephoneString);

        // Check if the imageUri is present otherwise insert nothing in the field
        if (imageUri != null && !String.valueOf(imageUri).equals("")) {
            values.put(InventoryEntry.COLUMN_IMAGE, String.valueOf(imageUri));
        } else {
            values.put(InventoryEntry.COLUMN_IMAGE, "");
        }

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, show inserction failed.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, show that the insertion was successful .
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with
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
                finish();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to previous activity
                if (!mProductHasChanged) {
                    finish();
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to previous activity.
                                finish();
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // define a projection that contains all columns from the inventory table
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
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int colorColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_COLOR);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int telephoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int color = cursor.getInt(colorColumnIndex);
            double price = Double.parseDouble(cursor.getString(priceColumnIndex)) / 100;
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            // It's need a long type for number > 9 figure
            long telephone = cursor.getLong(telephoneColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            if (!TextUtils.isEmpty(image)) {
                imageUri = Uri.parse(image);
                mProductImageView.setImageURI(imageUri);
            }

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(String.valueOf(mformatCurrency.format(price).replace(mformatCurrency.getCurrency().getSymbol(), "")));
            String sQuantity = Integer.toString(quantity);
            mQuantityEditText.setText(sQuantity);
            mSupplierEditText.setText(supplier);
            String sTelephone = Long.toString(telephone);
            mTelephoneEditText.setText(sTelephone);

            // Set the selection of color spinne given the value of cursor
            switch (color) {
                case InventoryEntry.COLOR_BLACK:
                    mColorSpinner.setSelection(1);
                    break;
                case InventoryEntry.COLOR_WHITE:
                    mColorSpinner.setSelection(2);
                    break;
                case InventoryEntry.COLOR_RED:
                    mColorSpinner.setSelection(3);
                    break;
                case InventoryEntry.COLOR_BLUE:
                    mColorSpinner.setSelection(4);
                    break;
                default:
                    mColorSpinner.setSelection(0);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mColorSpinner.setSelection(0);
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mTelephoneEditText.setText("");

    }

    /**
     * Prevent lose edit information when the screen is rotated.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mProductHasChanged", mProductHasChanged);
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    public void showDeleteConfirmationDialog() {
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
     * Inform the user that no field has been edited
     */
    public void showEmptyConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.no_field_dialog_msg);
        builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Exit" button and finish the activity.
                finish();
            }
        });
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog.
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

}
