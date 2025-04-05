package com.example.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtil {
    
    /**
     * Convierte un Uri en un archivo multipart para subidas a la API
     */
    fun uriToMultipartImage(context: Context, uri: Uri, paramName: String): MultipartBody.Part? {
        try {
            // Obtener el nombre del archivo desde el Uri
            val fileName = getFileName(context, uri)
            
            // Crear un archivo temporal para guardar la imagen
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(uri)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            // Crear el cuerpo de la solicitud multipart
            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(paramName, fileName, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Convierte un String a un RequestBody
     */
    fun createPartFromString(data: String): RequestBody {
        return data.toRequestBody("text/plain".toMediaTypeOrNull())
    }
    
    /**
     * Obtiene el nombre del archivo desde un Uri
     */
    private fun getFileName(context: Context, uri: Uri): String {
        var fileName = "image_${System.currentTimeMillis()}.jpg"
        
        // Intenta obtener el nombre del archivo desde el Uri
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        
        return fileName
    }
} 