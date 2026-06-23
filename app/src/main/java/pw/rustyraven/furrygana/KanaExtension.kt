package pw.rustyraven.furrygana

fun Char.isKanji(): Boolean {
    return this in '\u4E00'..'\u9FFF' ||    // CJK Unified Ideographs
            this in '\u3400'..'\u4DBF' ||    // CJK Extension A
            this in '\uF900'..'\uFAFF'       // CJK Compatibility Ideographs
}

fun Char.isHiragana() = this in '\u3040'..'\u309F'
fun Char.isKatakana() = this in '\u30A0'..'\u30FF'
fun Char.isKana() = isHiragana() || isKatakana()

fun String.katakanaToHiragana(): String {
    return this.map { char ->
        when (char) {
            in '\u30A1'..'\u30F6' -> (char.code - 0x60).toChar() // ァ..ヶ → ぁ..げ
            else -> char                                         // остальное как есть
        }
    }.joinToString("")
}