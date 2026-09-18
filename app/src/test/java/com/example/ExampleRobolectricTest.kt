package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.LoanEntity
import com.example.data.model.LoanType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Tk Manager", appName)
    }

    @Test
    fun `loan entity calculations are accurate`() {
        val loan = LoanEntity(
            id = 1,
            name = "Test Borrower",
            type = LoanType.LENT.name,
            originalAmount = 1000.0,
            repaidAmount = 250.0,
            dateMillis = 1000L,
            dueDateMillis = 5000L
        )

        assertEquals(750.0, loan.remainingAmount, 0.001)
        assertEquals(0.25f, loan.progressFraction, 0.001f)
        assertEquals(25, loan.progressPercentage)
    }

    @Test
    fun `loan entity completion check`() {
        val loan = LoanEntity(
            id = 2,
            name = "Test Lender",
            type = LoanType.BORROWED.name,
            originalAmount = 500.0,
            repaidAmount = 500.0,
            dateMillis = 1000L,
            dueDateMillis = 5000L,
            isCompleted = true
        )

        assertEquals(0.0, loan.remainingAmount, 0.001)
        assertEquals(1.0f, loan.progressFraction, 0.001f)
        assertEquals(100, loan.progressPercentage)
        assertTrue(loan.isCompleted)
    }
}
