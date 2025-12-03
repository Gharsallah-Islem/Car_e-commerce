package com.integmobile.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

/**
 * Extension functions for common operations
 */

// View Extensions
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

// Context Extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// Fragment Extensions
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

fun Fragment.showSnackbar(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(view, message, duration).show()
}

fun Fragment.showLongSnackbar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}

// String Extensions
fun String.isValidEmail(): Boolean = Validators.isValidEmail(this)

fun String.isValidPassword(): Boolean = Validators.isValidPassword(this)

fun String.isValidPhoneNumber(): Boolean = Validators.isValidPhoneNumber(this)

fun String.isValidOTP(): Boolean = Validators.isValidOTP(this)

// Double Extensions
fun Double.formatPrice(): String = String.format("%.2f DZD", this)

fun Double.formatDiscount(): String = String.format("%.0f%% OFF", this)

// Long Extensions (Timestamp)
fun Long.toFormattedDate(): String {
    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(this))
}

fun Long.toFormattedDateTime(): String {
    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(this))
}
