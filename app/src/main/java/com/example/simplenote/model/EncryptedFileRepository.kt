package com.example.simplenote.model

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.*
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptedFileRepository(var context: Context) :
    NoteRepository {

    private val passwordString = "Swordfish"

    override fun addNote(note: Note) {
        if (isExternalStorageWritable()) {
            ObjectOutputStream(noteFileOutputStream(note.fileName)).use { output ->
                output.writeObject(encrypt(note.noteText.toByteArray()))
            }
        }
    }

    override fun getNote(fileName: String): Note {
        val note = Note(fileName, "")
        if (isExternalStorageReadable()) {
            ObjectInputStream(noteFileInputStream(note.fileName)).use { stream ->
                val mapFromFile = stream.readObject() as HashMap<String, ByteArray>
                val decrypted = decrypt(mapFromFile)
                if (decrypted != null) {
                    note.noteText = String(decrypted)
                }
            }
        }
        return note
    }

    override fun deleteNote(fileName: String): Boolean =
        isExternalStorageWritable() && noteFile(fileName).delete()

    private fun decrypt(map: HashMap<String, ByteArray>): ByteArray? {
        var decrypted: ByteArray? = null
        try {
            val salt = map["salt"]
            val iv = map["iv"]
            val encrypted = map["encrypted"]

            // 1 regenerate key from password
            val passwordChar = passwordString.toCharArray()
            val pbKeySpec = PBEKeySpec(passwordChar, salt, 1324, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, "AES")

            // 2 Decrypt
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            decrypted = cipher.doFinal(encrypted)
        } catch (e: Exception) {
            Log.e("SIMPLENOTE", "decryption exception", e)
        }
        // 3
        return decrypted
    }

    private fun encrypt(plainTextBytes: ByteArray): HashMap<String, ByteArray> {
        val map = HashMap<String, ByteArray>()

        try {
            //Random salt for next step
            // 1
            val random = SecureRandom()
            // 2
            val salt = ByteArray(256)
            // 3
            random.nextBytes(salt)

            //PBKDF2 - derive the key from the password, don't use passwords directly
            // 4
            val passwordChar = passwordString.toCharArray() //Turn password into char[] array
            // 5
            val pbKeySpec = PBEKeySpec(passwordChar, salt, 1324, 256) //1324 iterations
            // 6
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            // 7
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            // 8
            val keySpec = SecretKeySpec(keyBytes, "AES")

            //Create initialization vector for AES
            // 9
            val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
            // 10
            val iv = ByteArray(16)
            // 11
            ivRandom.nextBytes(iv)
            // 12
            val ivSpec = IvParameterSpec(iv)

            //Encrypt
            // 13
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            // 14
            val encrypted = cipher.doFinal(plainTextBytes)
            // 15
            map["salt"] = salt
            map["iv"] = iv
            map["encrypted"] = encrypted
        } catch (e: Exception) {
            Log.e("MYAPP", "encryption exception", e)
        }
        return map
    }

    private fun noteDirectory(): File? = context.getExternalFilesDir(null)

    private fun noteFile(fileName: String): File = File(noteDirectory(), fileName)

    private fun noteFileOutputStream(fileName: String): FileOutputStream =
        FileOutputStream(noteFile(fileName))

    private fun noteFileInputStream(fileName: String): FileInputStream =
        FileInputStream(noteFile(fileName))

    private fun isExternalStorageWritable(): Boolean =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    private fun isExternalStorageReadable(): Boolean =
        Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
}