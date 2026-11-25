package com.stringly.app.chordfeedback

import com.stringly.app.chordfeedback.audio.ChordWavAssetHelper
import com.stringly.app.chordfeedback.model.ChordConstants
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ChordModelAndAudioTest {

    @Test
    fun testSevenMajorChordsMetadata() {
        val chords = ChordConstants.MAJOR_CHORDS
        assertEquals(7, chords.size)

        // A Major
        val chordA = ChordConstants.A_MAJOR
        assertEquals("A", chordA.name)
        assertEquals(9, chordA.rootPitchClass)   // A
        assertEquals(1, chordA.thirdPitchClass)  // C#
        assertEquals(4, chordA.fifthPitchClass)  // E
        assertEquals(listOf(9, 1, 4), chordA.pitchClasses)

        // B Major
        val chordB = ChordConstants.B_MAJOR
        assertEquals("B", chordB.name)
        assertEquals(11, chordB.rootPitchClass)  // B
        assertEquals(3, chordB.thirdPitchClass)   // D#
        assertEquals(6, chordB.fifthPitchClass)   // F#

        // C Major
        val chordC = ChordConstants.C_MAJOR
        assertEquals("C", chordC.name)
        assertEquals(0, chordC.rootPitchClass)   // C
        assertEquals(4, chordC.thirdPitchClass)  // E
        assertEquals(7, chordC.fifthPitchClass)  // G

        // D Major
        val chordD = ChordConstants.D_MAJOR
        assertEquals("D", chordD.name)
        assertEquals(2, chordD.rootPitchClass)   // D
        assertEquals(6, chordD.thirdPitchClass)  // F#
        assertEquals(9, chordD.fifthPitchClass)  // A

        // E Major
        val chordE = ChordConstants.E_MAJOR
        assertEquals("E", chordE.name)
        assertEquals(4, chordE.rootPitchClass)   // E
        assertEquals(8, chordE.thirdPitchClass)  // G#
        assertEquals(11, chordE.fifthPitchClass) // B

        // F Major
        val chordF = ChordConstants.F_MAJOR
        assertEquals("F", chordF.name)
        assertEquals(5, chordF.rootPitchClass)   // F
        assertEquals(9, chordF.thirdPitchClass)  // A
        assertEquals(0, chordF.fifthPitchClass)  // C

        // G Major
        val chordG = ChordConstants.G_MAJOR
        assertEquals("G", chordG.name)
        assertEquals(7, chordG.rootPitchClass)   // G
        assertEquals(11, chordG.thirdPitchClass) // B
        assertEquals(2, chordG.fifthPitchClass)  // D
    }

    @Test
    fun testGetChordByName() {
        assertNotNull(ChordConstants.getChordByName("A"))
        assertNotNull(ChordConstants.getChordByName("A Major"))
        assertNotNull(ChordConstants.getChordByName("c major"))
        assertNotNull(ChordConstants.getChordByName("G"))
        assertNull(ChordConstants.getChordByName("Am")) // Minor not in MVP
        assertNull(ChordConstants.getChordByName("C7"))
    }

    @Test
    fun testWavAudioDataGenerationAndFileCreation() {
        for (chord in ChordConstants.MAJOR_CHORDS) {
            val wavBytes = ChordWavAssetHelper.generateChordWavData(chord)
            assertTrue(wavBytes.size > 44) // Must have header (44 bytes) + audio data

            // Check RIFF header 'R' 'I' 'F' 'F'
            assertEquals('R'.code.toByte(), wavBytes[0])
            assertEquals('I'.code.toByte(), wavBytes[1])
            assertEquals('F'.code.toByte(), wavBytes[2])
            assertEquals('F'.code.toByte(), wavBytes[3])

            // Check 'W' 'A' 'V' 'E'
            assertEquals('W'.code.toByte(), wavBytes[8])
            assertEquals('A'.code.toByte(), wavBytes[9])
            assertEquals('V'.code.toByte(), wavBytes[10])
            assertEquals('E'.code.toByte(), wavBytes[11])
        }

        // Ensure WAV files exist in app/src/main/assets/chords/
        val assetsChordsDir = File("src/main/assets/chords")
        if (!assetsChordsDir.exists()) {
            assetsChordsDir.mkdirs()
        }
        ChordWavAssetHelper.ensureWavFilesExist(assetsChordsDir, ChordConstants.MAJOR_CHORDS)

        for (chord in ChordConstants.MAJOR_CHORDS) {
            val fileName = chord.audioAssetName.substringAfterLast("/")
            val file = File(assetsChordsDir, fileName)
            assertTrue(file.exists())
            assertTrue(file.length() > 44)
        }
    }
}
