package com.example.simplenote.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.simplenote.R
import com.example.simplenote.app.showToast
import com.example.simplenote.databinding.ActivityMainBinding
import com.example.simplenote.model.EncryptedFileRepository
import com.example.simplenote.model.InternalFileRepository
import com.example.simplenote.model.Note
import com.example.simplenote.model.NoteRepository

class MainActivity : AppCompatActivity() {

    //lazzy hanya bisa digunakan menggunakan val
    //Lazy adalah standar library yang telah disediakan
    // agar properties baru diinisialisasi ketika properties itu diakses
    private val repo: NoteRepository by lazy {
        EncryptedFileRepository(this)
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnWrite.setOnClickListener {
            //to ensure the user entered requirement
            if (binding.edtFileName.text.isNotEmpty()) {
                //writing a file will ensure the app crash may be a disk
                    // not enough space available or different permission
                try {
                    //memanggil addNote dengan mengirimkan note bahawa berisi namafile dan text
                    repo.addNote(
                        Note(
                            binding.edtFileName.text.toString(),
                            binding.edtNoteText.text.toString()
                        )
                    )
                } catch (e: Exception) {
                    showToast("File Write Failed")
                    e.printStackTrace()
                }

                //
                binding.edtFileName.text.clear()
                binding.edtNoteText.text.clear()
            } else {
                showToast("Please provide a Filename")
            }
        }

        binding.btnRead.setOnClickListener {
            if (binding.edtFileName.text.isNotEmpty()) {
                try {
                    val note = repo.getNote(binding.edtFileName.text.toString())
                    binding.edtNoteText.setText(note.noteText)
                } catch (e: Exception) {
                    showToast("File Read Failed")
                    e.printStackTrace()
                }
            } else {
                showToast("Please provide a Filename")
            }
        }

        binding.btnDelete.setOnClickListener {
            if (binding.edtFileName.text.isNotEmpty()) {
                try {
                    if (repo.deleteNote(binding.edtFileName.text.toString())) {
                        showToast("File Deleted")
                    } else {
                        showToast("File Could Not Be Deleted")
                    }
                } catch (e: Exception) {
                    showToast("File Delete Failed")
                    e.printStackTrace()
                }
                binding.edtFileName.text.clear()
                binding.edtNoteText.text.clear()
            } else {
                showToast("Please provide a Filename")
            }
        }
    }
}