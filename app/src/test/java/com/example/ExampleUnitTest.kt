package com.example

import com.example.parser.CommandParser
import com.example.parser.ParsedAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testSinhalaCommands() {
        val batteryResult = CommandParser.parse("මගේ ෆෝන් එකේ බැටරි එක කීයද?")
        assertTrue(batteryResult.action is ParsedAction.GetBatteryStatus)
        assertEquals("Sinhala", batteryResult.detectedLanguage)

        val flashResult = CommandParser.parse("Flashlight on කරන්න")
        assertTrue(flashResult.action is ParsedAction.Flashlight)
        assertTrue((flashResult.action as ParsedAction.Flashlight).enable)
        assertEquals("Mixed", flashResult.detectedLanguage)

        val unlockResult = CommandParser.parse("මගේ phone එක unlock කරන්න")
        assertTrue(unlockResult.action is ParsedAction.RequestUnlock)
        assertEquals("Mixed", unlockResult.detectedLanguage)
        assertEquals("කරුණාකර ඉදිරියට යාමට ඔබගේ අනන්‍යතාවය තහවුරු කරන්න.", unlockResult.defaultLocalResponse)
    }

    @Test
    fun testEnglishCommands() {
        val flashOff = CommandParser.parse("Flashlight off")
        assertTrue(flashOff.action is ParsedAction.Flashlight)
        assertEquals(false, (flashOff.action as ParsedAction.Flashlight).enable)
        assertEquals("English", flashOff.detectedLanguage)

        val volumeUp = CommandParser.parse("Volume up")
        assertTrue(volumeUp.action is ParsedAction.AdjustVolume)
        assertTrue((volumeUp.action as ParsedAction.AdjustVolume).increase)

        val timeResult = CommandParser.parse("What time is it?")
        assertTrue(timeResult.action is ParsedAction.GetCurrentTime)

        val ytResult = CommandParser.parse("Open YouTube")
        assertTrue(ytResult.action is ParsedAction.OpenYouTube)

        val unlockResult = CommandParser.parse("Unlock my phone")
        assertTrue(unlockResult.action is ParsedAction.RequestUnlock)
        assertEquals("Please authenticate to continue.", unlockResult.defaultLocalResponse)
    }

    @Test
    fun testConversationalFallthrough() {
        val query = CommandParser.parse("What is the distance from Earth to Mars?")
        assertTrue(query.action is ParsedAction.ConversationalQuery)
    }
}
