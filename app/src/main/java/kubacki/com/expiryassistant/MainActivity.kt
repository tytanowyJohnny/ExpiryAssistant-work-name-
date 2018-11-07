package kubacki.com.expiryassistant

import android.content.ContentValues
import android.os.Bundle
import android.provider.BaseColumns
import android.support.design.widget.Snackbar
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity(), ProductDialogFragment.ProductDialogListener, ProductsRecyclerAdapter.ProductsRecyclerAdapterListener {

    /*
    Variables
     */
    // RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
    private lateinit var recyclerViewManager: RecyclerView.LayoutManager
    private lateinit var recyclerViewDecoration: DividerItemDecoration

    // Array to store product entries
    private lateinit var productsArray: ArrayList<ProductEntry>

    // Database vars
    private lateinit var dbHelper: ProductsDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        /*
        Schedule a job
         */
        

        // Add listener for Floating Action Button -> Show new product dialog
        addProductFab.setOnClickListener {

            val fm = supportFragmentManager
            val productDialogFragment = ProductDialogFragment().newInstance()

            productDialogFragment.show(fm, "new_product_dialog")
        }

        /*
        Lateinit variables initialization (except RecyclerView)
         */
        productsArray = ArrayList()

        // SQLite Database
        dbHelper = ProductsDbHelper(this)

        val dbReader = dbHelper.readableDatabase

        // Define which columns will be read
        val projection = arrayOf(BaseColumns._ID, ProductsEntry.COLUMN_PRODUCT_NAME, ProductsEntry.COLUMN_PRODUCT_EXPIRY)

        // Define sortOrder
        val sortOrder = "${ProductsEntry.COLUMN_PRODUCT_EXPIRY} DESC"

        // Setup cursor
        val cursor = dbReader.query(
                ProductsEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        )

        // Read information for DB using curson
        with(cursor) {

            while(moveToNext()) {

                val tempProduct = ProductEntry(getLong(getColumnIndex(BaseColumns._ID)), getString(getColumnIndex(ProductsEntry.COLUMN_PRODUCT_NAME)), Date(getLong(getColumnIndex(ProductsEntry.COLUMN_PRODUCT_EXPIRY))))

                productsArray.add(tempProduct)
            }
        }

        // Check if there are any entries from DB, if not show "empty_row" layout
        if(productsArray.size == 0)
            emptyRowInclude.visibility = View.VISIBLE

        // Assign LinearLayoutManager
        recyclerViewManager = LinearLayoutManager(this)

        // Create instance of custom adapter
        recyclerViewAdapter = ProductsRecyclerAdapter(productsArray, this)

        // Create instance of custom divider
        recyclerViewDecoration = DividerItemDecoration(ResourcesCompat.getDrawable(resources, R.drawable.divider_drawable, null))

        // Apply layout manager and custom adapter to RecyclerView
        recyclerView = findViewById<RecyclerView>(R.id.productsRecyclerView).apply {

            setHasFixedSize(true)
            layoutManager = recyclerViewManager
            adapter = recyclerViewAdapter
            addItemDecoration(recyclerViewDecoration, 0)
        }

    }

    /*
    Overriden function to handle communication from adapter that a long press has been detected
     */
    override fun onLongProductClick() {

        // Show toolbar buttons
        showToolbarButtons(true)

        // Disable floating action button
        addProductFab.isClickable = false

        /*
         Setup listeners for buttons
          */

        // Confirm deletion button
        toolbar.deleteRowButton.setOnClickListener {

            // Hide buttons
            showToolbarButtons(false)

            // Hide checkboxes
            ProductsRecyclerAdapter.checkMode = false

            // Delete items from array of products
            for(i in ProductsRecyclerAdapter.productsToDeleteArray) {

                if(productsArray.contains(i.value))
                    productsArray.remove(i.value)
            }

            /*
            Delete rows from DB
            */
            val dbDeletor = dbHelper.writableDatabase

            // Loop through array of items to be deleted
            val selection = "${BaseColumns._ID} = ?"

            // Count how many rows were deleted
            var rowCounter = 0

            for(i in ProductsRecyclerAdapter.productsToDeleteArray) {

                // Perform deletion for each row
                rowCounter += dbDeletor.delete(ProductsEntry.TABLE_NAME, selection, arrayOf(i.value.pID.toString()))

            }

            // Configure snackbar with count of deleted items
            val snackbar = Snackbar.make(mainCoordinatorLayout, "Products deleted:    $rowCounter", Snackbar.LENGTH_LONG)

            // Get snackbar view
            val snackbarView = snackbar.view

            // Get snackbar textView
            val snackTextView = snackbarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)

            // Change text color
            snackTextView.setTextColor(ResourcesCompat.getColor(resources, R.color.fontColorSnackbar, null))

            // Show snackbar
            snackbar.show()

            // Clear array with items to delete
            ProductsRecyclerAdapter.productsToDeleteArray.clear()

            // Refresh adapter
            recyclerViewAdapter.notifyDataSetChanged()

            // Delete listeners
            toolbar.deleteRowButton.setOnClickListener(null)
            toolbar.cancelDeleteRowButton.setOnClickListener(null)

            // Enable floating action button
            addProductFab.isClickable = true

        }

        // Dismiss button
        toolbar.cancelDeleteRowButton.setOnClickListener {

            // Hide buttons
            showToolbarButtons(false)

            // Hide checkboxes
            ProductsRecyclerAdapter.checkMode = false

            // Delete items from deletion array
            ProductsRecyclerAdapter.productsToDeleteArray.clear()

            // Refresh adapter
            recyclerViewAdapter.notifyDataSetChanged()

            // Delete listeners
            toolbar.deleteRowButton.setOnClickListener(null)
            toolbar.cancelDeleteRowButton.setOnClickListener(null)

            // Enable floating action button
            addProductFab.isClickable = true
        }


    }

    /*
        Overriden function from dialog interface to edit existing record
    */
    override fun onProductEdit(pName: String, pExpiry: Date, pID: Long?) {

        editLoop@ for((counter, i) in productsArray.withIndex()) {

            if(i.pID == pID) { // Change record in arrayList

                i.pName = pName
                i.pExpiry = pExpiry

                // Update adapter
                recyclerViewAdapter.notifyItemChanged(counter)

                /*
                 Update DB
                  */
                val dbUpdater = dbHelper.writableDatabase

                // Set values
                val values = ContentValues().apply {
                    put(ProductsEntry.COLUMN_PRODUCT_NAME, pName)
                    put(ProductsEntry.COLUMN_PRODUCT_EXPIRY, pExpiry.time)
                }

                // Update row with appropriate ID
                val selection = "${BaseColumns._ID} = ?"
                val selectionArgs = arrayOf(pID.toString())

                val success = dbUpdater.update(
                        ProductsEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                )

                // Show confirmation snackbar
                if(success == 1) {
                    // Create snackbar
                    val snackBar = customSnackbar(mainCoordinatorLayout, "Product $pName has been updated", ResourcesCompat.getColor(resources, R.color.fontColorSnackbar, null),Snackbar.LENGTH_SHORT)

                    // Show it
                    snackBar.show()

                } else {
                    // Create snackbar
                    val snackBar = customSnackbar(mainCoordinatorLayout, "Unable to update product in database, please try again", ResourcesCompat.getColor(resources, R.color.fontColorSnackbar, null), Snackbar.LENGTH_SHORT)

                    // Show it
                    snackBar.show()
                }

                // Exit loop, there will be no more matches
                break@editLoop

            }
        }

    }

    /*
    Overriden function from dialog interface to create a new record
     */
    override fun onProductSave(pName: String, pExpiry: Date) {

        /*
        Save to DB
         */
        val dbWriter = dbHelper.writableDatabase

        // Assign values to insert
        val values = ContentValues().apply {

            put(ProductsEntry.COLUMN_PRODUCT_NAME, pName)
            put(ProductsEntry.COLUMN_PRODUCT_EXPIRY, pExpiry.time)
        }

        // Insert new row
        val newRowId = dbWriter?.insert(ProductsEntry.TABLE_NAME, null, values)

        // Add new entry to productsArray
        if(newRowId != null)
            productsArray.add(ProductEntry(newRowId, pName, pExpiry))

        // Hide empty row notification if it is visible
        if(emptyRowInclude.visibility != View.GONE)
            emptyRowInclude.visibility = View.GONE

        // Refresh RecyclerView Adapter
        recyclerViewAdapter.notifyItemInserted(productsArray.size)


    }

    /*
    Helper funtions for showing / hiding toolbar buttons
     */
    private fun showToolbarButtons(show: Boolean) {

        if(show) {

            toolbar.deleteRowButton.visibility = View.VISIBLE
            toolbar.cancelDeleteRowButton.visibility = View.VISIBLE
        } else {

            toolbar.deleteRowButton.visibility = View.INVISIBLE
            toolbar.cancelDeleteRowButton.visibility = View.INVISIBLE
        }
    }
    /*
    Helper funtion for creating custom snakcbar
     */
    private fun customSnackbar(view: View, text: String, color: Int, length: Int) : Snackbar {

        // Make snackbar with view and text
        val snack = Snackbar.make(view, text, length)

        // Get snackbar view
        val snackView = snack.view

        // Get snackbar TextView
        val snackTextView = snackView.findViewById<TextView>(android.support.design.R.id.snackbar_text)

        // Set custom color
        snackTextView.setTextColor(color)

        return snack
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
