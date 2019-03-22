package tanvd.kosogor.web.utils

open class Console(private val prefix: String, private val mainColor: Color) {

    enum class Color(val ansi: String) {
        GREEN("\u001B[32m"),
        RED("\u001B[31m");

        companion object {
            const val RESET = "\u001B[0m"
        }
    }

    fun println(text: String, prefix: String = this.prefix, color: Console.Color = this.mainColor) {
        System.out.println("${color.ansi}$prefix$text${Color.RESET}")
    }
}
