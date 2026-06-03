package com.renium.sipkasku.utils

import android.content.Context
import com.renium.sipkasku.data.local.TransactionEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

suspend fun exportTransactionsToCsv(
    context: Context,
    transactions: List<TransactionEntity>,
    categoriesMap: Map<Int, String> = emptyMap()
): File {
    val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(context.cacheDir, "transactions_$time.csv")

    FileWriter(file).use { writer ->
        writer.append("id,title,amount,category,isIncome,date\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        transactions.forEach { t ->
            val safeTitle = t.title.replace("\"", "\"\"")
            val categoryName = t.categoryId?.let { categoriesMap[it] } ?: ""
            val safeCategory = categoryName.replace("\"", "\"\"")
            val line = buildString {
                append(t.id)
                append(',')
                append('"')
                append(safeTitle)
                append('"')
                append(',')
                append(t.amount)
                append(',')
                append('"')
                append(safeCategory)
                append('"')
                append(',')
                append(t.isIncome)
                append(',')
                append(sdf.format(Date(t.date)))
            }
            writer.append(line).append('\n')
        }
        writer.flush()
    }

    return file
}
