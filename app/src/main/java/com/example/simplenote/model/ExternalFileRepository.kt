package com.example.simplenote.model

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ExternalFileRepository(var context : Context) : NoteRepository{
    override fun addNote(note: Note) {
        if (isExternalStorageReadable()){
            FileOutputStream(noteFile(note.fileName)).use { output->
                output.write(note.noteText.toByteArray())
            }
        }
    }

    override fun getNote(fileName: String): Note {
        val note = Note(fileName,"")
        if (isExternalStorageReadable()){
            FileInputStream(noteFile(fileName)).use { stream->
                val text = stream.bufferedReader().use {
                    it.readText()
                }
                note.noteText
            }
        }
        return note
    }

    override fun deleteNote(fileName: String): Boolean {
        return isExternalStorageWritable() && noteFile(fileName).delete()
    }

    private fun noteDirectory():File? = context.getExternalFilesDir(null)

    private fun noteFile(fileName: String): File = File(noteDirectory(), fileName)

    private fun isExternalStorageWritable():Boolean =
        Environment.getExternalStorageState()  == Environment.MEDIA_MOUNTED

    private fun isExternalStorageReadable():Boolean =
        Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)


}