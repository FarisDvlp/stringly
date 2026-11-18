package com.stringly.app.musicchord

import com.stringly.app.musicchord.model.*
import com.stringly.app.musicchord.transpose.ChordTransposer
import org.junit.Assert.*
import org.junit.Test

class ChordTransposerTest {

    @Test
    fun testTransposeSingleNoteUpAndDown() {
        assertEquals("C#", ChordTransposer.transposeSingleNote("C", 1))
        assertEquals("D", ChordTransposer.transposeSingleNote("C", 2))
        assertEquals("B", ChordTransposer.transposeSingleNote("C", -1))
        assertEquals("A", ChordTransposer.transposeSingleNote("C", -3))
        assertEquals("C", ChordTransposer.transposeSingleNote("C", 12))
    }

    @Test
    fun testTransposeComplexChords() {
        assertEquals("A#m7", ChordTransposer.transposeSingleNote("Am7", 1))
        assertEquals("Bbm7", ChordTransposer.transposeSingleNote("Am7", 1, preferFlats = true))
        assertEquals("C#m7", ChordTransposer.transposeSingleNote("Cm7", 1))
        assertEquals("D7", ChordTransposer.transposeSingleNote("C7", 2))
        assertEquals("Ebsus4", ChordTransposer.transposeSingleNote("Dsus4", 1, preferFlats = true))
        assertEquals("C#add9", ChordTransposer.transposeSingleNote("Cadd9", 1))
        assertEquals("F#m", ChordTransposer.transposeSingleNote("Em", 2))
    }

    @Test
    fun testTransposeSlashChords() {
        assertEquals("C#/F", ChordTransposer.transposeChord("C/E", 1))
        assertEquals("A/C#", ChordTransposer.transposeChord("G/B", 2))
        assertEquals("Bbm7/Ab", ChordTransposer.transposeChord("Am7/G", 1, preferFlats = true))
    }

    @Test
    fun testSongTransposePreservesMetadata() {
        val originalSong = Song(
            id = "test_1",
            title = "Test Song",
            artist = "Test Artist",
            originalKey = "C",
            tuning = "Standard (E A D G B E)",
            capoFret = 2,
            difficulty = "Easy",
            chordSheet = ChordSheet(
                listOf(
                    ChordSection(
                        "Verse",
                        listOf(
                            ChordLine(
                                listOf(ChordPlacement("C", 0), ChordPlacement("Am7", 10)),
                                "Hello world"
                            )
                        )
                    )
                )
            )
        )

        // Transpose up 2 semitones
        val transposed = ChordTransposer.transposeSong(originalSong, 2)

        assertEquals("D", transposed.transposedKey)
        assertEquals("C", transposed.originalKey)
        assertEquals("Capo 2", transposed.capoLabel)
        assertEquals("Standard (E A D G B E)", transposed.tuning)

        val firstLineChords = transposed.transposedChordSheet.sections[0].lines[0].chords
        assertEquals("D", firstLineChords[0].chord)
        assertEquals("Bm7", firstLineChords[1].chord)

        // Reset
        val resetSong = ChordTransposer.resetTranspose(originalSong)
        assertEquals("C", resetSong.transposedKey)
        assertEquals("C", resetSong.transposedChordSheet.sections[0].lines[0].chords[0].chord)
    }

    @Test
    fun testCapoLabelFormatting() {
        val songNoCapo = Song(
            id = "1", title = "A", artist = "B", originalKey = "C",
            tuning = "Standard", capoFret = null, difficulty = "Easy",
            chordSheet = ChordSheet(emptyList())
        )
        assertEquals("No Capo", songNoCapo.capoLabel)

        val songZeroCapo = Song(
            id = "2", title = "A", artist = "B", originalKey = "C",
            tuning = "Standard", capoFret = 0, difficulty = "Easy",
            chordSheet = ChordSheet(emptyList())
        )
        assertEquals("No Capo", songZeroCapo.capoLabel)

        val songWithCapo = Song(
            id = "3", title = "A", artist = "B", originalKey = "C",
            tuning = "Standard", capoFret = 3, difficulty = "Easy",
            chordSheet = ChordSheet(emptyList())
        )
        assertEquals("Capo 3", songWithCapo.capoLabel)
    }
}
