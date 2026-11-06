package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Leichtgewichtiger, autarker QR-Code-Generator in reinem Kotlin ohne externe Abhängigkeiten.
 * Unterstützt ISO/IEC 18004 Standard (Byte-Modus, Versionen 1-10, Fehlerkorrektur Level L & M).
 */
object QrCodeGenerator {

    fun generateQrBitmap(content: String, size: Int = 512): ImageBitmap {
        val matrix = encode(content)
        val matrixSize = matrix.size
        val scale = (size / matrixSize).coerceAtLeast(1)
        val bitmapSize = matrixSize * scale

        val bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmapSize * bitmapSize)

        for (y in 0 until bitmapSize) {
            val my = (y / scale).coerceIn(0, matrixSize - 1)
            for (x in 0 until bitmapSize) {
                val mx = (x / scale).coerceIn(0, matrixSize - 1)
                pixels[y * bitmapSize + x] = if (matrix[my][mx]) Color.BLACK else Color.WHITE
            }
        }
        bitmap.setPixels(pixels, 0, bitmapSize, 0, 0, bitmapSize, bitmapSize)
        return bitmap.asImageBitmap()
    }

    private fun encode(text: String): Array<BooleanArray> {
        val dataBytes = text.toByteArray(Charsets.UTF_8)
        // Wählen der kleinsten Version (1..10), die die Daten fasst (Level M)
        val version = getMinVersion(dataBytes.size)
        val numModules = version * 4 + 17

        val matrix = Array(numModules) { BooleanArray(numModules) }
        val isFunction = Array(numModules) { BooleanArray(numModules) }

        // Finder Patterns (3 Ecken)
        drawFinderPattern(matrix, isFunction, 0, 0)
        drawFinderPattern(matrix, isFunction, numModules - 7, 0)
        drawFinderPattern(matrix, isFunction, 0, numModules - 7)

        // Alignment Patterns (ab Version 2)
        if (version >= 2) {
            val alignPos = getAlignmentPositions(version)
            for (r in alignPos) {
                for (c in alignPos) {
                    if (!isFunction[r][c]) {
                        drawAlignmentPattern(matrix, isFunction, r, c)
                    }
                }
            }
        }

        // Timing Patterns
        for (i in 8 until numModules - 8) {
            val bit = (i % 2 == 0)
            if (!isFunction[6][i]) {
                matrix[6][i] = bit
                isFunction[6][i] = true
            }
            if (!isFunction[i][6]) {
                matrix[i][6] = bit
                isFunction[i][6] = true
            }
        }

        // Dark Module
        matrix[numModules - 8][8] = true
        isFunction[numModules - 8][8] = true

        // Reservieren der Format-Informationen (rund um Finder)
        for (i in 0..8) {
            isFunction[8][i] = true
            isFunction[i][8] = true
            isFunction[8][numModules - 1 - i] = true
            isFunction[numModules - 1 - i][8] = true
        }

        // Daten & ECC generieren
        val codewords = makeCodewords(dataBytes, version)

        // Codewords in Matrix platzieren (Zig-Zag)
        var bitIndex = 0
        val totalBits = codewords.size * 8
        var right = numModules - 1
        var goingUp = true

        while (right > 0) {
            if (right == 6) right-- // Timing-Spalte überspringen
            val rowRange = if (goingUp) (numModules - 1 downTo 0) else (0 until numModules)
            for (row in rowRange) {
                for (colOffset in 0..1) {
                    val col = right - colOffset
                    if (!isFunction[row][col]) {
                        var bit = false
                        if (bitIndex < totalBits) {
                            val byteVal = codewords[bitIndex / 8].toInt() and 0xFF
                            val bitVal = (byteVal ushr (7 - (bitIndex % 8))) and 1
                            bit = (bitVal == 1)
                            bitIndex++
                        }
                        // Maske 0: (row + col) % 2 == 0
                        if ((row + col) % 2 == 0) bit = !bit
                        matrix[row][col] = bit
                    }
                }
            }
            right -= 2
            goingUp = !goingUp
        }

        // Format-Informationen für Maske 0, Level M (0b00) schreiben
        writeFormatBits(matrix, numModules, 0b101010000010010)

        // Ruhige Zone (Quiet Zone = 4 Module)
        val quiet = 4
        val fullSize = numModules + quiet * 2
        val fullMatrix = Array(fullSize) { BooleanArray(fullSize) }
        for (r in 0 until numModules) {
            for (c in 0 until numModules) {
                fullMatrix[r + quiet][c + quiet] = matrix[r][c]
            }
        }
        return fullMatrix
    }

    private fun drawFinderPattern(m: Array<BooleanArray>, f: Array<BooleanArray>, startR: Int, startC: Int) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isOuter = (r == 0 || r == 6 || c == 0 || c == 6)
                val isInner = (r in 2..4 && c in 2..4)
                m[startR + r][startC + c] = isOuter || isInner
                f[startR + r][startC + c] = true
            }
        }
        // Separator
        for (r in -1..7) {
            for (c in -1..7) {
                val pr = startR + r
                val pc = startC + c
                if (pr in m.indices && pc in m.indices && !f[pr][pc]) {
                    m[pr][pc] = false
                    f[pr][pc] = true
                }
            }
        }
    }

    private fun drawAlignmentPattern(m: Array<BooleanArray>, f: Array<BooleanArray>, centerR: Int, centerC: Int) {
        for (r in -2..2) {
            for (c in -2..2) {
                val pr = centerR + r
                val pc = centerC + c
                val isBlack = (Math.abs(r) == 2 || Math.abs(c) == 2 || (r == 0 && c == 0))
                m[pr][pc] = isBlack
                f[pr][pc] = true
            }
        }
    }

    private fun getMinVersion(dataLength: Int): Int {
        // Kapazitäten für Byte-Modus bei Fehlerkorrektur M
        val capacities = intArrayOf(0, 14, 26, 42, 62, 84, 106, 122, 152, 180, 213, 251, 287, 331, 362, 412)
        for (v in 1 until capacities.size) {
            if (dataLength <= capacities[v]) return v
        }
        return 10
    }

    private fun getAlignmentPositions(version: Int): IntArray {
        return when (version) {
            2 -> intArrayOf(6, 18)
            3 -> intArrayOf(6, 22)
            4 -> intArrayOf(6, 26)
            5 -> intArrayOf(6, 30)
            6 -> intArrayOf(6, 34)
            7 -> intArrayOf(6, 22, 38)
            8 -> intArrayOf(6, 24, 42)
            9 -> intArrayOf(6, 26, 46)
            10 -> intArrayOf(6, 28, 50)
            else -> intArrayOf(6, version * 4 + 10)
        }
    }

    private fun makeCodewords(data: ByteArray, version: Int): ByteArray {
        val totalCodewords = getTotalDataCodewords(version)
        val bitBuffer = mutableListOf<Boolean>()

        // Byte Mode indicator: 0100
        addBits(bitBuffer, 0b0100, 4)
        // Count indicator: 8 bits für Versionen 1-9, 16 bits für Version >= 10
        val countBits = if (version < 10) 8 else 16
        addBits(bitBuffer, data.size, countBits)

        // Datenbits
        for (b in data) {
            addBits(bitBuffer, b.toInt() and 0xFF, 8)
        }

        // Terminator: bis zu 4 Nullen
        val maxDataBits = totalCodewords * 8
        val terminatorBits = (maxDataBits - bitBuffer.size).coerceIn(0, 4)
        addBits(bitBuffer, 0, terminatorBits)

        // Auf volles Byte auffüllen
        while (bitBuffer.size % 8 != 0) {
            bitBuffer.add(false)
        }

        // Padding-Bytes 0xEC, 0x11
        val result = ByteArray(totalCodewords)
        var byteIdx = 0
        for (i in 0 until bitBuffer.size step 8) {
            var v = 0
            for (b in 0 until 8) {
                if (bitBuffer[i + b]) v = v or (1 shl (7 - b))
            }
            result[byteIdx++] = v.toByte()
        }

        val padBytes = byteArrayOf(0xEC.toByte(), 0x11.toByte())
        var padIdx = 0
        while (byteIdx < totalCodewords) {
            result[byteIdx++] = padBytes[padIdx % 2]
            padIdx++
        }

        // Reed-Solomon ECC berechnen
        val ecCodewordsCount = getEcCodewordsCount(version)
        val ecc = calcReedSolomon(result, ecCodewordsCount)
        return result + ecc
    }

    private fun addBits(buffer: MutableList<Boolean>, value: Int, count: Int) {
        for (i in count - 1 downTo 0) {
            buffer.add(((value ushr i) and 1) == 1)
        }
    }

    private fun getTotalDataCodewords(version: Int): Int {
        return when (version) {
            1 -> 16
            2 -> 28
            3 -> 44
            4 -> 64
            5 -> 86
            6 -> 108
            7 -> 124
            8 -> 154
            9 -> 182
            10 -> 216
            else -> 16
        }
    }

    private fun getEcCodewordsCount(version: Int): Int {
        return when (version) {
            1 -> 10
            2 -> 16
            3 -> 26
            4 -> 18 * 2
            5 -> 24 * 2
            6 -> 16 * 4
            7 -> 18 * 4
            8 -> 22 * 4
            9 -> 22 * 4
            10 -> 26 * 4
            else -> 10
        }
    }

    private fun calcReedSolomon(data: ByteArray, eccCount: Int): ByteArray {
        val exp = IntArray(512)
        val log = IntArray(256)
        var x = 1
        for (i in 0 until 255) {
            exp[i] = x
            exp[i + 255] = x
            log[x] = i
            x = (x shl 1) xor (if (x >= 128) 0x11D else 0)
        }

        // Generator-Polynom
        var gen = intArrayOf(1)
        for (i in 0 until eccCount) {
            val nextGen = IntArray(gen.size + 1)
            for (j in gen.indices) {
                nextGen[j] = nextGen[j] xor exp[(log[gen[j]] + exp[i]) % 255]
                nextGen[j + 1] = nextGen[j + 1] xor gen[j]
            }
            gen = nextGen
        }

        val result = IntArray(eccCount)
        for (b in data) {
            val factor = (b.toInt() and 0xFF) xor result[0]
            for (j in 0 until eccCount - 1) {
                result[j] = result[j + 1] xor (if (factor != 0) exp[(log[gen[j + 1]] + log[factor]) % 255] else 0)
            }
            result[eccCount - 1] = if (factor != 0) exp[(log[gen[eccCount]] + log[factor]) % 255] else 0
        }

        val resBytes = ByteArray(eccCount)
        for (i in 0 until eccCount) {
            resBytes[i] = result[i].toByte()
        }
        return resBytes
    }

    private fun writeFormatBits(matrix: Array<BooleanArray>, size: Int, format: Int) {
        val bits = BooleanArray(15)
        for (i in 0..14) {
            bits[i] = ((format ushr (14 - i)) and 1) == 1
        }

        // Oben links & Timing
        var idx = 0
        for (c in 0..8) {
            if (c != 6) matrix[8][c] = bits[idx++]
        }
        for (r in 7 downTo 0) {
            if (r != 6) matrix[r][8] = bits[idx++]
        }

        // Unten links & oben rechts
        for (i in 0..7) {
            matrix[size - 1 - i][8] = bits[i]
        }
        for (i in 0..6) {
            matrix[8][size - 7 + i] = bits[8 + i]
        }
    }
}
