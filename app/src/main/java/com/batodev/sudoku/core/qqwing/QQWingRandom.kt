package com.batodev.sudoku.core.qqwing

import java.util.Random

/**
 * Randomness helpers shared by puzzle generation: a single [Random] instance
 * (seedable via [random]), array shuffling/initialization, and picking a
 * random [Symmetry].
 */
internal object QQWingRandom {
    var random: Random = Random()

    fun fillIncrementing(arr: IntArray): IntArray {
        for (i in arr.indices) {
            arr[i] = i
        }
        return arr
    }

    /**
     * Shuffle the values in an array of integers.
     */
    fun shuffleArray(array: IntArray, size: Int) {
        for (i in 0 until size) {
            val tailSize = size - i
            val randTailPos = Math.abs(random.nextInt()) % tailSize + i
            val temp = array[i]
            array[i] = array[randTailPos]
            array[randTailPos] = temp
        }
    }

    // not the first and last value which are NONE and RANDOM
    val randomSymmetry: Symmetry
        get() {
            val values = Symmetry.values()
            return values[Math.abs(random.nextInt()) % (values.size - 1) + 1]
        }
}
