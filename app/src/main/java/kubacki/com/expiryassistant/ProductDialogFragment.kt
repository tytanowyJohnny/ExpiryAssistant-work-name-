package kubacki.com.expiryassistant


import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import kotlinx.android.synthetic.main.fragment_new_product_dialog.*
import java.util.Date
import java.util.Calendar

private val NO_ID: Long = -1

class ProductDialogFragment : DialogFragment() {

    private val selectedDate = Calendar.getInstance()
    private var productID: Long? = NO_ID
    private var productName: String? = ""
    private var productExpiry: Date? = null

    interface ProductDialogListener {
        fun onProductSave(pName: String, pExpiry: Date)
        fun onProductEdit(pName: String, pExpiry: Date, pID: Long?)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get arguments
        if(arguments != null) {

            productID = arguments?.getLong("productID")
            productName = arguments?.getString("productName")
            productExpiry = Date(arguments?.getLong("productExpiry")!!)
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_product_dialog, container, false)
    }

    fun newInstance() : ProductDialogFragment {

        productID = NO_ID

        return ProductDialogFragment()
    }

    fun newInstance(pID: Long, pName: String, pExpiry: Date) : ProductDialogFragment {

        val frag = ProductDialogFragment()
        val args = Bundle()

        args.putLong("productID", pID)
        args.putString("productName", pName)
        args.putLong("productExpiry", pExpiry.time)

        frag.arguments = args

        return frag
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If we are editing existing record, use appropriate values to be visible in inputs (date picker is below)
        if(productID != NO_ID)
            productNameInput.editText?.setText(productName)


        // Create listener to see if user wants to hide keyboard
        view.setOnTouchListener { v, _ ->

            // Perform click to avoid warning in AS
            v?.performClick()

            if (v != null) {
                hideKeyboard(v)
            }

            true
        }

        // Create listener for Date dialog picker
        val datePickerListener = DatePickerDialog.OnDateSetListener { _: DatePicker, i: Int, i1: Int, i2: Int ->

            // Format output date
            val tempString = "$i2/$i1/$i"

            // Store date in Date format (as Long)
            selectedDate.set(i, i1, i2)

            // Set field content
            productExpiryInput.editText?.setText(tempString)

        }

        // Create listener for Edit Text (attribute focusable set to "false" in XML)
        productExpiryInput.editText?.setOnClickListener {

            if (productID == NO_ID) { // New product

                // Get current date
                val currentDate = Calendar.getInstance()


                // Create new DatePickerDialog
                val datePickerDialog = DatePickerDialog(activity, datePickerListener, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH))

                // Show dialog
                datePickerDialog.show()

            } else { // Existing product

                // Create instance of calendar
                val tempCalendar = Calendar.getInstance()

                // Convert date to calendar
                tempCalendar.time = productExpiry

                // Create new DatePickerDialog
                val datePickerDialog = DatePickerDialog(activity, datePickerListener, tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH))

                // Show dialog
                datePickerDialog.show()
            }

        }

        // Create listeners for positive and negative buttons
        dialogButtonPositive.setOnClickListener {

            val newProductDialogListener = activity as ProductDialogListener

            if(productID == NO_ID) // If new product, save it
                newProductDialogListener.onProductSave(productNameInput.editText?.text.toString(), selectedDate.time)
            else // If existing product, update it
                newProductDialogListener.onProductEdit(productNameInput.editText?.text.toString(), selectedDate.time, productID)

            dialog.dismiss()

        }

        dialogButtonNegative.setOnClickListener{

            // dismiss dialog
            dialog.dismiss()

        }

    }

    // Helper function to hide keyboard
    private fun hideKeyboard(view: View) {

        val imm = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}
