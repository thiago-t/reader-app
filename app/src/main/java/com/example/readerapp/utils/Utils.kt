package com.example.readerapp.utils

import com.google.firebase.Timestamp
import java.text.DateFormat

// Date format example: December 28
fun Timestamp.formatted(): String {
    return DateFormat.getDateInstance()
        .format(toDate())
        .toString().split(",").first()
}