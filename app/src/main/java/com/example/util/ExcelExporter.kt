package com.example.util

import com.example.data.model.LoanEntity
import com.example.data.model.RepaymentEntity
import com.example.data.model.TransactionEntity
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Lightweight, zero-dependency OpenXML (.xlsx) Excel generator.
 * Generates standards-compliant Excel workbooks with multiple sheets.
 */
object ExcelExporter {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun exportToStream(
        outputStream: OutputStream,
        transactions: List<TransactionEntity>,
        loans: List<LoanEntity>,
        repayments: List<RepaymentEntity>
    ) {
        ZipOutputStream(outputStream).use { zip ->
            // 1. [Content_Types].xml
            addZipEntry(zip, "[Content_Types].xml", buildContentTypesXml())

            // 2. _rels/.rels
            addZipEntry(zip, "_rels/.rels", buildPackageRelsXml())

            // 3. xl/workbook.xml
            addZipEntry(zip, "xl/workbook.xml", buildWorkbookXml())

            // 4. xl/_rels/workbook.xml.rels
            addZipEntry(zip, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml())

            // 5. xl/styles.xml
            addZipEntry(zip, "xl/styles.xml", buildStylesXml())

            // 6. xl/worksheets/sheet1.xml (Transactions)
            addZipEntry(zip, "xl/worksheets/sheet1.xml", buildTransactionsSheet(transactions))

            // 7. xl/worksheets/sheet2.xml (Loans)
            addZipEntry(zip, "xl/worksheets/sheet2.xml", buildLoansSheet(loans))

            // 8. xl/worksheets/sheet3.xml (Repayment History)
            addZipEntry(zip, "xl/worksheets/sheet3.xml", buildRepaymentsSheet(repayments))
        }
    }

    private fun addZipEntry(zip: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zip.putNextEntry(entry)
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        zip.write(bytes)
        zip.closeEntry()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun buildContentTypesXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
            <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
            <Default Extension="xml" ContentType="application/xml"/>
            <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
            <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
            <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
            <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
            <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
        </Types>
    """.trimIndent()

    private fun buildPackageRelsXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
        </Relationships>
    """.trimIndent()

    private fun buildWorkbookXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
            <sheets>
                <sheet name="Transactions" sheetId="1" r:id="rId1"/>
                <sheet name="Loans" sheetId="2" r:id="rId2"/>
                <sheet name="Repayment History" sheetId="3" r:id="rId3"/>
            </sheets>
        </workbook>
    """.trimIndent()

    private fun buildWorkbookRelsXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
            <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
            <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
            <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
        </Relationships>
    """.trimIndent()

    private fun buildStylesXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
            <fonts count="2">
                <font><sz val="11"/><name val="Calibri"/></font>
                <font><b/><sz val="11"/><name val="Calibri"/></font>
            </fonts>
            <fills count="2">
                <fill><patternFill patternType="none"/></fill>
                <fill><patternFill patternType="gray125"/></fill>
            </fills>
            <borders count="1">
                <border><left/><right/><top/><bottom/><diagonal/></border>
            </borders>
            <cellStyleXfs count="1">
                <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
            </cellStyleXfs>
            <cellXfs count="2">
                <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
                <xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
            </cellXfs>
        </styleSheet>
    """.trimIndent()

    private fun getColumnName(colIndex: Int): String {
        var num = colIndex
        val sb = StringBuilder()
        while (num >= 0) {
            sb.append(('A'.code + (num % 26)).toChar())
            num = (num / 26) - 1
        }
        return sb.reverse().toString()
    }

    private fun buildTransactionsSheet(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n")
        sb.append("<sheetData>\n")

        // Headers
        val headers = listOf("Type", "Title", "Category", "Amount", "Date", "Note")
        sb.append("<row r=\"1\">\n")
        headers.forEachIndexed { i, header ->
            val cellRef = "${getColumnName(i)}1"
            sb.append("<c r=\"$cellRef\" s=\"1\" t=\"inlineStr\"><is><t>${escapeXml(header)}</t></is></c>\n")
        }
        sb.append("</row>\n")

        // Rows
        transactions.forEachIndexed { rowIndex, tx ->
            val rowNum = rowIndex + 2
            val formattedDate = dateFormatter.format(Date(tx.dateMillis))
            sb.append("<row r=\"$rowNum\">\n")
            sb.append("<c r=\"A$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(tx.type)}</t></is></c>\n")
            sb.append("<c r=\"B$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(tx.title)}</t></is></c>\n")
            sb.append("<c r=\"C$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(tx.category)}</t></is></c>\n")
            sb.append("<c r=\"D$rowNum\"><v>${String.format(Locale.US, "%.2f", tx.amount)}</v></c>\n")
            sb.append("<c r=\"E$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(formattedDate)}</t></is></c>\n")
            sb.append("<c r=\"F$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(tx.note ?: "")}</t></is></c>\n")
            sb.append("</row>\n")
        }

        sb.append("</sheetData>\n</worksheet>")
        return sb.toString()
    }

    private fun buildLoansSheet(loans: List<LoanEntity>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n")
        sb.append("<sheetData>\n")

        // Headers: Name, Loan Type, Original Amount, Repaid Amount, Remaining Amount, Date, Due Date, Status, Note
        val headers = listOf(
            "Name", "Loan Type", "Original Amount", "Repaid Amount",
            "Remaining Amount", "Date", "Due Date", "Status", "Note"
        )
        sb.append("<row r=\"1\">\n")
        headers.forEachIndexed { i, header ->
            val cellRef = "${getColumnName(i)}1"
            sb.append("<c r=\"$cellRef\" s=\"1\" t=\"inlineStr\"><is><t>${escapeXml(header)}</t></is></c>\n")
        }
        sb.append("</row>\n")

        // Rows
        loans.forEachIndexed { rowIndex, loan ->
            val rowNum = rowIndex + 2
            val formattedDate = dateFormatter.format(Date(loan.dateMillis))
            val formattedDueDate = dateFormatter.format(Date(loan.dueDateMillis))
            val status = if (loan.isCompleted || loan.remainingAmount <= 0.0) "Completed" else "Active"

            sb.append("<row r=\"$rowNum\">\n")
            sb.append("<c r=\"A$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(loan.name)}</t></is></c>\n")
            sb.append("<c r=\"B$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(loan.type)}</t></is></c>\n")
            sb.append("<c r=\"C$rowNum\"><v>${String.format(Locale.US, "%.2f", loan.originalAmount)}</v></c>\n")
            sb.append("<c r=\"D$rowNum\"><v>${String.format(Locale.US, "%.2f", loan.repaidAmount)}</v></c>\n")
            sb.append("<c r=\"E$rowNum\"><v>${String.format(Locale.US, "%.2f", loan.remainingAmount)}</v></c>\n")
            sb.append("<c r=\"F$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(formattedDate)}</t></is></c>\n")
            sb.append("<c r=\"G$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(formattedDueDate)}</t></is></c>\n")
            sb.append("<c r=\"H$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(status)}</t></is></c>\n")
            sb.append("<c r=\"I$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(loan.note ?: "")}</t></is></c>\n")
            sb.append("</row>\n")
        }

        sb.append("</sheetData>\n</worksheet>")
        return sb.toString()
    }

    private fun buildRepaymentsSheet(repayments: List<RepaymentEntity>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n")
        sb.append("<sheetData>\n")

        // Headers: Loan Name, Repayment Amount, Repayment Date
        val headers = listOf("Loan Name", "Repayment Amount", "Repayment Date", "Note")
        sb.append("<row r=\"1\">\n")
        headers.forEachIndexed { i, header ->
            val cellRef = "${getColumnName(i)}1"
            sb.append("<c r=\"$cellRef\" s=\"1\" t=\"inlineStr\"><is><t>${escapeXml(header)}</t></is></c>\n")
        }
        sb.append("</row>\n")

        // Rows
        repayments.forEachIndexed { rowIndex, rep ->
            val rowNum = rowIndex + 2
            val formattedDate = dateFormatter.format(Date(rep.dateMillis))
            sb.append("<row r=\"$rowNum\">\n")
            sb.append("<c r=\"A$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(rep.loanName)}</t></is></c>\n")
            sb.append("<c r=\"B$rowNum\"><v>${String.format(Locale.US, "%.2f", rep.amount)}</v></c>\n")
            sb.append("<c r=\"C$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(formattedDate)}</t></is></c>\n")
            sb.append("<c r=\"D$rowNum\" t=\"inlineStr\"><is><t>${escapeXml(rep.note ?: "")}</t></is></c>\n")
            sb.append("</row>\n")
        }

        sb.append("</sheetData>\n</worksheet>")
        return sb.toString()
    }
}
