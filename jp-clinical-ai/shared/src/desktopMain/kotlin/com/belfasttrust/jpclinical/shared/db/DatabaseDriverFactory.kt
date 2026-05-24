package com.belfasttrust.jpclinical.shared.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databaseFile = File("clinical.db")
        val needsCreate = !databaseFile.exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:clinical.db")
        if (needsCreate) {
            ClinicalDatabase.Schema.create(driver)
        }
        return driver
    }
}
