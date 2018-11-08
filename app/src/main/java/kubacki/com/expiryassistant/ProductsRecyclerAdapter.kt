package kubacki.com.expiryassistant

import android.annotation.SuppressLint
import android.app.Activity
import android.app.FragmentManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.support.annotation.ColorRes
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.product_list_empty_row.view.*
import kotlinx.android.synthetic.main.product_list_row.view.*
import java.text.SimpleDateFormat

class ProductsRecyclerAdapter(private val dataSet: ArrayList<ProductEntry>, private val context: Context) : RecyclerView.Adapter<ProductsRecyclerAdapter.ViewHolder>() {

    // Some static values
    companion object {
        var productsToDeleteArray: HashMap<Int, ProductEntry> = HashMap()
        var checkMode = false

    }

    // Interface used to communicate with Main Activity
    interface ProductsRecyclerAdapterListener {

        fun onLongProductClick()
    }

    inner class ViewHolder(val linearLay: LinearLayout) : RecyclerView.ViewHolder(linearLay)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ProductsRecyclerAdapter.ViewHolder {


        val linearLay = LayoutInflater.from(parent.context).inflate(R.layout.product_list_row, parent, false) as LinearLayout

        // Handle long click which may end with item deletion
        if(!linearLay.hasOnClickListeners()) { // Check if there are listeners already

            linearLay.setOnLongClickListener {

                // Change checkMode state on long press
                if(!checkMode) {

                    checkMode = true

                    // Refresh adapter
                    notifyDataSetChanged()

                    // If state of rows is changed via long click, check if buttons on toolbar should be visible or hidden
                    val productsRecyclerAdapter = context as ProductsRecyclerAdapterListener

                    productsRecyclerAdapter.onLongProductClick()
                }

                true
            }

        }

        return ViewHolder(linearLay)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Set TextViews to correct values
        holder.linearLay.productNameRow.text = dataSet[holder.adapterPosition].pName

        // Format date to string
        val formatter = SimpleDateFormat("dd/MM/yyyy")

        val pExpiryString = formatter.format(dataSet[holder.adapterPosition].pExpiry)


        holder.linearLay.productExpiryRow.text = pExpiryString

        // Show/Hide checkboxes
        if (checkMode) {

            // Delete onClickListener setup earlier for showing edit dialog
            holder.linearLay.setOnClickListener(null)

            holder.linearLay.checkBox.visibility = View.VISIBLE

            holder.linearLay.checkBox.isChecked = false

            // Handle checking boxes by user
            if (!holder.linearLay.checkBox.hasOnClickListeners()) {

                holder.linearLay.checkBox.setOnCheckedChangeListener { _, isChecked ->

                    if (isChecked) {

                        // Change layout, red background and white letters
                        checkModeOn(holder)

                        // Add product to deletion list
                        productsToDeleteArray[holder.adapterPosition] = dataSet[holder.adapterPosition]

                    } else {

                        // Change layout to default
                        checkModeOff(holder)

                        // Delete product from deletion list
                        productsToDeleteArray.remove(holder.adapterPosition)
                    }
                }
            }
        } else {

            // Hide checkboxes
            holder.linearLay.checkBox.visibility = View.GONE

            // Bring back default colors
            checkModeOff(holder)

            // Clear array with items to delete
            productsToDeleteArray.clear()

            // Delete listener
            holder.linearLay.checkBox.setOnCheckedChangeListener(null)

            // Set onClickListener (for showing edit dialog)
            holder.linearLay.setOnClickListener {

                val editProductDialog = ProductDialogFragment().newInstance(dataSet[holder.adapterPosition].pID, dataSet[holder.adapterPosition].pName, dataSet[holder.adapterPosition].pExpiry)
                val fm = (context as AppCompatActivity).supportFragmentManager

                editProductDialog.show(fm, "edit_product_dialog")

            }
        }

    }

    private fun checkModeOn(holder: ViewHolder) {
        holder.linearLay.setBackgroundColor(Color.RED)
        holder.linearLay.productNameRow.setTextColor(Color.WHITE)
        holder.linearLay.productExpiryRow.setTextColor(Color.BLACK)
    }

    private fun checkModeOff(holder: ViewHolder) {
        holder.linearLay.setBackgroundColor(Color.WHITE)
        holder.linearLay.productNameRow.setTextColor(ResourcesCompat.getColor(context.resources, R.color.fontColorSingleRow, null))
        holder.linearLay.productExpiryRow.setTextColor(ResourcesCompat.getColor(context.resources, R.color.fontColorSingleRow, null))
    }

    override fun getItemCount(): Int {

        // Return size of dataSet
        return dataSet.size
    }

}