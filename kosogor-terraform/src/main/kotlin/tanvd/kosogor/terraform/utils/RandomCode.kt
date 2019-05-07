package tanvd.kosogor.terraform.utils

import java.math.BigInteger
import java.security.SecureRandom

internal object RandomCode {
    private val rnd = SecureRandom()
    private const val defaultLength: Int = 25

    /** It is highly recommended to use default radix equal to 36 **/
    @Suppress("MagicNumber")
    fun next(len: Int = defaultLength, radix: Int = 36): String {
        return BigInteger(128, rnd).toString(radix).takeLast(len)
    }
}
