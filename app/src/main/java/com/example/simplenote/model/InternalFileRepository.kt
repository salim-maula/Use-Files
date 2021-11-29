package com.example.simplenote.model

import android.content.Context
import java.io.File

class InternalFileRepository(var context : Context) : NoteRepository {
    override fun addNote(note: Note) {

        //this code open the file in fileOutputStream using the Context.MODE_PRIVATE flag
        //this flage make file private in app
        context.openFileOutput(note.fileName, Context.MODE_PRIVATE).use {output->
            output.write(note.noteText.toByteArray())
        }
    }

    override fun getNote(fileName: String): Note {
        //empty string so that a valid object gets returned from this function even if the read
        //operation fails.
        val note = Note(fileName, "")
        //Open and consume the FileInputStream with use()
        context.openFileInput(fileName).use { stream->
            // Open a BufferedReader with use() so that you can efficiently read the file
            val text = stream.bufferedReader().use {
                it.readText()
            }
            //4
            note.noteText = text
        }
        //5
        return note
    }

    override fun deleteNote(fileName: String): Boolean {
        return noteFile(fileName).delete()
    }

    private fun noteFile(fileName: String): File = File(noteDirectory())

    private fun noteDirectory(): String = context.filesDir.absolutePath
}