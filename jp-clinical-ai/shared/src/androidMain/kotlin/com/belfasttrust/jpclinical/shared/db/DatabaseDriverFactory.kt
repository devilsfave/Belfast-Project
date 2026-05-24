package com.belfasttrust.jpclinical.shared.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = ClinicalDatabase.Schema,
            context = context,
            name = "clinical.db"
        )
    }
}
