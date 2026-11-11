package com.example.parser

enum class SettingsSection {
    WIFI, BLUETOOTH, DISPLAY, SOUND, APPS, GENERAL
}

sealed class ParsedAction {
    object OpenYouTube : ParsedAction()
    object OpenWhatsApp : ParsedAction()
    object OpenCamera : ParsedAction()
    object OpenBrowser : ParsedAction()
    object OpenDialer : ParsedAction()
    object OpenSms : ParsedAction()
    data class Flashlight(val enable: Boolean) : ParsedAction()
    data class AdjustVolume(val increase: Boolean) : ParsedAction()
    object GetBatteryStatus : ParsedAction()
    object GetCurrentTime : ParsedAction()
    object RequestUnlock : ParsedAction()
    data class OpenSettings(val section: SettingsSection) : ParsedAction()
    object SetAlarm : ParsedAction()
    object CreateCalendarEvent : ParsedAction()
    data class LaunchApp(val appName: String) : ParsedAction()
    object Greeting : ParsedAction()
    data class ConversationalQuery(val query: String) : ParsedAction()
}

data class ParseResult(
    val action: ParsedAction,
    val detectedLanguage: String, // "Sinhala", "English", "Mixed"
    val defaultLocalResponse: String? = null
)

object CommandParser {

    fun parse(input: String): ParseResult {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()
        val hasSinhalaChars = trimmed.any { it in '\u0D80'..'\u0DFF' }
        val hasEnglishChars = trimmed.any { it in 'a'..'z' || it in 'A'..'Z' }

        val detectedLang = when {
            hasSinhalaChars && hasEnglishChars -> "Mixed"
            hasSinhalaChars -> "Sinhala"
            else -> "English"
        }

        // 1. Wake word / Greetings
        if (lower.contains("hey madu") || lower.contains("hey jarvis") || lower.contains("hello jarvis") ||
            trimmed.contains("හලෝ ජාර්විස්") || trimmed.contains("හායි මාදු")
        ) {
            val response = if (hasSinhalaChars) {
                "ඔව් මම සූදානම්! ඔබට මගෙන් කුමක්ද අවශ්‍ය?"
            } else {
                "Yes, I am online and at your service. How may I assist you today?"
            }
            return ParseResult(ParsedAction.Greeting, detectedLang, response)
        }

        // 2. Unlock phone
        if (lower.contains("unlock my phone") || lower.contains("unlock phone") ||
            trimmed.contains("unlock කරන්න") || trimmed.contains("අන්ලොක් කරන්න") ||
            trimmed.contains("ෆෝන් එක unlock")
        ) {
            val response = if (hasSinhalaChars) {
                "කරුණාකර ඉදිරියට යාමට ඔබගේ අනන්‍යතාවය තහවුරු කරන්න."
            } else {
                "Please authenticate to continue."
            }
            return ParseResult(ParsedAction.RequestUnlock, detectedLang, response)
        }

        // 3. Flashlight ON
        if (lower.contains("flashlight on") || lower.contains("torch on") || lower.contains("turn on flashlight") ||
            trimmed.contains("flashlight on කරන්න") || trimmed.contains("ටෝච් එක දාන්න") || trimmed.contains("ලයිට් එක දාන්න")
        ) {
            val response = if (hasSinhalaChars) "ෆ්ලෑෂ් ලයිට් ක්‍රියාත්මක කළා." else "Flashlight turned on."
            return ParseResult(ParsedAction.Flashlight(true), detectedLang, response)
        }

        // 4. Flashlight OFF
        if (lower.contains("flashlight off") || lower.contains("torch off") || lower.contains("turn off flashlight") ||
            trimmed.contains("flashlight off කරන්න") || trimmed.contains("ටෝච් එක නිවන්න") || trimmed.contains("ලයිට් එක නිවන්න")
        ) {
            val response = if (hasSinhalaChars) "ෆ්ලෑෂ් ලයිට් ක්‍රියා විරහිත කළා." else "Flashlight turned off."
            return ParseResult(ParsedAction.Flashlight(false), detectedLang, response)
        }

        // 5. Volume UP
        if (lower.contains("volume up") || lower.contains("increase volume") || lower.contains("sound up") ||
            trimmed.contains("volume වැඩි කරන්න") || trimmed.contains("සද්දෙ වැඩි කරන්න") || trimmed.contains("වොලියුම් වැඩි කරන්න")
        ) {
            val response = if (hasSinhalaChars) "ශබ්දය වැඩි කළා." else "Volume increased."
            return ParseResult(ParsedAction.AdjustVolume(true), detectedLang, response)
        }

        // 6. Volume DOWN
        if (lower.contains("volume down") || lower.contains("decrease volume") || lower.contains("sound down") ||
            trimmed.contains("volume අඩු කරන්න") || trimmed.contains("සද්දෙ අඩු කරන්න") || trimmed.contains("වොලියුම් අඩු කරන්න")
        ) {
            val response = if (hasSinhalaChars) "ශබ්දය අඩු කළා." else "Volume decreased."
            return ParseResult(ParsedAction.AdjustVolume(false), detectedLang, response)
        }

        // 7. Battery Status
        if (lower.contains("battery") || lower.contains("charge") ||
            trimmed.contains("බැටරි") || trimmed.contains("චාජ්")
        ) {
            return ParseResult(ParsedAction.GetBatteryStatus, detectedLang, null)
        }

        // 8. Time / Date
        if (lower.contains("what time") || lower.contains("current time") || lower.contains("time is it") ||
            trimmed.contains("වෙලාව") || trimmed.contains("වේලාව")
        ) {
            return ParseResult(ParsedAction.GetCurrentTime, detectedLang, null)
        }

        // 9. Open YouTube
        if (lower.contains("youtube") && (lower.contains("open") || trimmed.contains("open කරන්න") || trimmed.contains("දාන්න") || trimmed.contains("අරින්න"))) {
            val response = if (hasSinhalaChars) "YouTube විවෘත කරමින් පවතී." else "Opening YouTube."
            return ParseResult(ParsedAction.OpenYouTube, detectedLang, response)
        }

        // 10. Open WhatsApp
        if (lower.contains("whatsapp") && (lower.contains("open") || trimmed.contains("open කරන්න") || trimmed.contains("දාන්න") || trimmed.contains("අරින්න"))) {
            val response = if (hasSinhalaChars) "WhatsApp විවෘත කරමින් පවතී." else "Opening WhatsApp."
            return ParseResult(ParsedAction.OpenWhatsApp, detectedLang, response)
        }

        // 11. Open Camera
        if (lower.contains("camera") || trimmed.contains("කැමරා") || trimmed.contains("කැමරාව")) {
            val response = if (hasSinhalaChars) "කැමරාව විවෘත කරමින් පවතී." else "Opening Camera."
            return ParseResult(ParsedAction.OpenCamera, detectedLang, response)
        }

        // 12. Open Browser
        if (lower.contains("browser") || lower.contains("chrome") || lower.contains("internet") || trimmed.contains("බ්‍රවුසරය")) {
            val response = if (hasSinhalaChars) "අන්තර්ජාල බ්‍රවුසරය විවෘත කරමින් පවතී." else "Opening Browser."
            return ParseResult(ParsedAction.OpenBrowser, detectedLang, response)
        }

        // 13. Open Dialer
        if (lower.contains("dialer") || lower.contains("call") || lower.contains("phone app") || trimmed.contains("කෝල්") || trimmed.contains("ඇමතුම්")) {
            val response = if (hasSinhalaChars) "දුරකථන ඇමතුම් පුවරුව විවෘත කරමින් පවතී." else "Opening Phone dialer."
            return ParseResult(ParsedAction.OpenDialer, detectedLang, response)
        }

        // 14. Open SMS
        if (lower.contains("sms") || lower.contains("message") || trimmed.contains("මැසේජ්") || trimmed.contains("පණිවිඩ")) {
            val response = if (hasSinhalaChars) "පණිවිඩ යෙදුම විවෘත කරමින් පවතී." else "Opening Messaging app."
            return ParseResult(ParsedAction.OpenSms, detectedLang, response)
        }

        // 15. Settings
        if (lower.contains("wifi") || lower.contains("wi-fi") || trimmed.contains("වයිෆයි")) {
            val response = if (hasSinhalaChars) "Wi-Fi සැකසුම් විවෘත කරමින් පවතී." else "Opening Wi-Fi settings."
            return ParseResult(ParsedAction.OpenSettings(SettingsSection.WIFI), detectedLang, response)
        }
        if (lower.contains("bluetooth") || trimmed.contains("බ්ලූටූත්")) {
            val response = if (hasSinhalaChars) "Bluetooth සැකසුම් විවෘත කරමින් පවතී." else "Opening Bluetooth settings."
            return ParseResult(ParsedAction.OpenSettings(SettingsSection.BLUETOOTH), detectedLang, response)
        }
        if (lower.contains("display setting") || lower.contains("screen setting") || trimmed.contains("තිර සැකසුම්")) {
            val response = if (hasSinhalaChars) "තිර සැකසුම් විවෘත කරමින් පවතී." else "Opening Display settings."
            return ParseResult(ParsedAction.OpenSettings(SettingsSection.DISPLAY), detectedLang, response)
        }
        if (lower.contains("sound setting") || lower.contains("audio setting") || trimmed.contains("ශබ්ද සැකසුම්")) {
            val response = if (hasSinhalaChars) "ශබ්ද සැකසුම් විවෘත කරමින් පවතී." else "Opening Sound settings."
            return ParseResult(ParsedAction.OpenSettings(SettingsSection.SOUND), detectedLang, response)
        }
        if (lower.contains("open setting") || trimmed.contains("settings open") || trimmed.contains("සැකසුම් විවෘත")) {
            val response = if (hasSinhalaChars) "පද්ධති සැකසුම් විවෘත කරමින් පවතී." else "Opening System Settings."
            return ParseResult(ParsedAction.OpenSettings(SettingsSection.GENERAL), detectedLang, response)
        }

        // 16. Alarm
        if (lower.contains("alarm") || trimmed.contains("ඇලާމ්") || trimmed.contains("එලාම්")) {
            val response = if (hasSinhalaChars) "ඇලාරම් සැකසුම් විවෘත කරමින් පවතී." else "Opening Alarm clock."
            return ParseResult(ParsedAction.SetAlarm, detectedLang, response)
        }

        // 17. Calendar
        if (lower.contains("calendar") || trimmed.contains("දින දර්ශනය") || trimmed.contains("කැලැන්ඩරය")) {
            val response = if (hasSinhalaChars) "දින දර්ශනය විවෘත කරමින් පවතී." else "Opening Calendar."
            return ParseResult(ParsedAction.CreateCalendarEvent, detectedLang, response)
        }

        // 18. Generic "open [appName]"
        val openMatch = Regex("open\\s+([a-zA-Z0-9 ]+)", RegexOption.IGNORE_CASE).find(lower)
        if (openMatch != null) {
            val targetApp = openMatch.groupValues[1].trim()
            if (targetApp.isNotEmpty() && targetApp !in listOf("the", "my", "this")) {
                val response = "Opening $targetApp."
                return ParseResult(ParsedAction.LaunchApp(targetApp), detectedLang, response)
            }
        }
        val sinhalaOpenMatch = Regex("([a-zA-Z0-9]+)\\s*(?:open|අරින්න|දාන්න)\\s*කරන්න", RegexOption.IGNORE_CASE).find(trimmed)
        if (sinhalaOpenMatch != null) {
            val targetApp = sinhalaOpenMatch.groupValues[1].trim()
            if (targetApp.isNotEmpty()) {
                val response = "$targetApp විවෘත කරමින් පවතී."
                return ParseResult(ParsedAction.LaunchApp(targetApp), detectedLang, response)
            }
        }

        // 19. All other conversational and knowledge queries -> Gemini AI
        return ParseResult(ParsedAction.ConversationalQuery(trimmed), detectedLang, null)
    }
}
