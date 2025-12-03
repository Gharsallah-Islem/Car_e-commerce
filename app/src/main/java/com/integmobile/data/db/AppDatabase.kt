package com.integmobile.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.integmobile.data.db.converters.OrderItemListConverter
import com.integmobile.data.db.converters.StringListConverter
import com.integmobile.data.db.converters.StringMapConverter
import com.integmobile.data.db.dao.CartItemDao
import com.integmobile.data.db.dao.OrderDao
import com.integmobile.data.db.dao.ProductDao
import com.integmobile.data.db.dao.UserDao
import com.integmobile.data.db.entity.CartItem
import com.integmobile.data.db.entity.Order
import com.integmobile.data.db.entity.Product
import com.integmobile.data.db.entity.User
import com.integmobile.utils.Constants

/**
 * Room Database configuration
 * Provides singleton instance and DAO access
 */
@Database(
    entities = [
        User::class,
        Product::class,
        CartItem::class,
        Order::class
    ],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    StringMapConverter::class,
    OrderItemListConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun orderDao(): OrderDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                Constants.DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
