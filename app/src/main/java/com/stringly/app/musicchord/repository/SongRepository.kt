package com.stringly.app.musicchord.repository

import android.content.Context
import com.stringly.app.musicchord.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

interface SongRepository {
    suspend fun getAllSongs(): List<Song>
    suspend fun searchSongs(query: String): List<Song>
    suspend fun getSongById(id: String): Song?
}

class SongRepositoryImpl(
    private val context: Context,
    private val jsonFileName: String = "songs.json"
) : SongRepository {

    private var cachedSongs: List<Song>? = null

    override suspend fun getAllSongs(): List<Song> = withContext(Dispatchers.IO) {
        cachedSongs ?: loadSongsFromAssets().also { cachedSongs = it }
    }

    override suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        val allSongs = getAllSongs()
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isEmpty()) {
            return@withContext allSongs
        }
        allSongs.filter { song ->
            song.title.lowercase().contains(cleanQuery) ||
                    song.artist.lowercase().contains(cleanQuery)
        }
    }

    override suspend fun getSongById(id: String): Song? = withContext(Dispatchers.IO) {
        getAllSongs().firstOrNull { it.id == id }
    }

    private fun loadSongsFromAssets(): List<Song> {
        val jsonString = try {
            context.assets.open(jsonFileName).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            return emptyList()
        }

        val songsList = mutableListOf<Song>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val songObj = jsonArray.getJSONObject(i)
                val song = parseSong(songObj)
                songsList.add(song)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songsList
    }

    private fun parseSong(json: JSONObject): Song {
        val id = json.getString("id")
        val title = json.getString("title")
        val artist = json.getString("artist")
        val originalKey = json.getString("originalKey")
        val tuning = json.getString("tuning")
        val capoFret = if (json.isNull("capoFret")) null else json.getInt("capoFret")
        val difficulty = json.getString("difficulty")
        val bpm = if (json.isNull("bpm")) null else json.getInt("bpm")
        val guitarTab = if (json.isNull("guitarTab")) null else json.getString("guitarTab")

        val chordSheetObj = json.getJSONObject("chordSheet")
        val sectionsArray = chordSheetObj.getJSONArray("sections")
        val sections = mutableListOf<ChordSection>()

        for (i in 0 until sectionsArray.length()) {
            val secObj = sectionsArray.getJSONObject(i)
            val secName = secObj.getString("name")
            val linesArray = secObj.getJSONArray("lines")
            val lines = mutableListOf<ChordLine>()

            for (j in 0 until linesArray.length()) {
                val lineObj = linesArray.getJSONObject(j)
                val lyric = lineObj.optString("lyric", "")
                val chordsArray = lineObj.getJSONArray("chords")
                val placements = mutableListOf<ChordPlacement>()

                for (k in 0 until chordsArray.length()) {
                    val chordObj = chordsArray.getJSONObject(k)
                    val chord = chordObj.getString("chord")
                    val pos = chordObj.getInt("position")
                    placements.add(ChordPlacement(chord, pos))
                }
                lines.add(ChordLine(placements, lyric))
            }
            sections.add(ChordSection(secName, lines))
        }

        return Song(
            id = id,
            title = title,
            artist = artist,
            originalKey = originalKey,
            tuning = tuning,
            capoFret = capoFret,
            difficulty = difficulty,
            bpm = bpm,
            chordSheet = ChordSheet(sections),
            guitarTab = guitarTab
        )
    }
}
