package com.answersolutions.runandread

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val test = intArrayOf(1,2,3,4,5,6,7,8,9)

        print(test)
        print(test.indices)
        print(test.indices.reversed())
        assertEquals(4, 2 + 2)
    }
}