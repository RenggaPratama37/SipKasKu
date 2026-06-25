package com.renium.sipkasku.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.renium.sipkasku.viewmodel.ExportTransaction
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.File
import java.io.FileOutputStream

fun exportToExcel(
    context: Context,
    transactions: List<ExportTransaction>,
    monthLabel: String
): File {
    val fileName = "SipKasKu_$monthLabel.xlsx"
        .replace(" ", "_")
        .replace("/", "-")
    val file = File(context.cacheDir, fileName)

    FileOutputStream(file).use { fos ->
        Workbook(fos, "SipKasKu", "1.0").use { wb ->
            val ws: Worksheet = wb.newWorksheet("Report $monthLabel")

            // Header
            val headers = listOf("Date", "Title", "Category", "Pocket", "Type", "Amount")
            headers.forEachIndexed { col, header ->
                ws.value(0, col, header)
                ws.style(0, col)
                    .bold()
                    .fillColor("4472C4")
                    .fontColor("FFFFFF")
                    .set()
            }

            // Data rows
            var totalIncome = 0.0
            var totalExpense = 0.0

            transactions.forEachIndexed { rowIdx, tx ->
                val row = rowIdx + 1
                ws.value(row, 0, tx.date)
                ws.value(row, 1, tx.title)
                ws.value(row, 2, tx.categoryName)
                ws.value(row, 3, tx.pocketName)
                ws.value(row, 4, if (tx.isIncome) "Income" else "Expense")
                ws.value(row, 5, tx.amount)

                // Row Color
                val rowColor = if (tx.isIncome) "E8F5E9" else "FFEBEE"
                (0..5).forEach { col ->
                    ws.style(row, col).fillColor(rowColor).set()
                }

                if (tx.isIncome) totalIncome += tx.amount
                else totalExpense += tx.amount
            }

            // Summary rows
            val summaryRow = transactions.size + 2
            ws.value(summaryRow, 4, "Total Income")
            ws.value(summaryRow, 5, totalIncome)
            ws.style(summaryRow, 4).bold().set()
            ws.style(summaryRow, 5).bold().fontColor("2E7D32").set()

            ws.value(summaryRow + 1, 4, "Total Expense")
            ws.value(summaryRow + 1, 5, totalExpense)
            ws.style(summaryRow + 1, 4).bold().set()
            ws.style(summaryRow + 1, 5).bold().fontColor("D32F2F").set()

            ws.value(summaryRow + 2, 4, "Difference")
            ws.value(summaryRow + 2, 5, totalIncome - totalExpense)
            ws.style(summaryRow + 2, 4).bold().set()
            ws.style(summaryRow + 2, 5).bold().set()

            // Column width
            ws.width(0, 14.0)
            ws.width(1, 28.0)
            ws.width(2, 18.0)
            ws.width(3, 16.0)
            ws.width(4, 14.0)
            ws.width(5, 18.0)
        }
    }

    return file
}

fun shareExcelFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}
