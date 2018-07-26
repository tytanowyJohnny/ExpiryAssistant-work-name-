package kubacki.com.expiryassistant

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

object ProductsEntry : BaseColumns{

    const val TABLE_NAME = "products_entry"
    const val COLUMN_PRODUCT_NAME = "product_name"
    const val COLUMN_PRODUCT_EXPIRY = "product_expiry"
}

private const val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${ProductsEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${ProductsEntry.COLUMN_PRODUCT_NAME} TEXT," +
                "${ProductsEntry.COLUMN_PRODUCT_EXPIRY} LONG)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ProductsEntry.TABLE_NAME}"

class ProductsDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {

        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        // Do nothing for now
    }

    companion object {

        const val DATABASE_NAME = "ProductEntries.db"
        const val DATABASE_VERSION = 1

    }

}