package com.example.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {
    
    /**
     * Convierte una Uri en un archivo MultipartBody.Part para su subida
     * @param context El contexto de la aplicación
     * @param uri Uri del archivo
     * @param paramName Nombre del parámetro en el formulario
     * @return MultipartBody.Part del archivo o null si ocurre un error
     */
    fun uriToMultipartImage(context: Context, uri: Uri, paramName: String): MultipartBody.Part? {
        return try {
            // Intentar obtener el archivo físico de la Uri
            val tempFile = saveToCacheFile(context, uri)
            
            if (tempFile != null && tempFile.exists()) {
                Log.d("FileUtil", "Archivo creado correctamente: ${tempFile.absolutePath} (${tempFile.length()} bytes)")
                
                // Crear RequestBody a partir del archivo
                val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                
                // Crea parte multipart
                MultipartBody.Part.createFormData(
                    paramName,
                    tempFile.name,
                    requestFile
                )
            } else {
                Log.e("FileUtil", "No se pudo crear el archivo temporal para la imagen")
                null
            }
        } catch (e: Exception) {
            Log.e("FileUtil", "Error al convertir Uri a MultipartBody.Part", e)
            null
        }
    }
    
    /**
     * Guarda un archivo URI en el directorio de caché
     */
    private fun saveToCacheFile(context: Context, uri: Uri): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_${timeStamp}.jpg"
            val cacheFile = File(context.cacheDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(cacheFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            // Verificar que el archivo se creó correctamente
            if (cacheFile.exists() && cacheFile.length() > 0) {
                Log.d("FileUtil", "Imagen guardada en caché: ${cacheFile.absolutePath} (${cacheFile.length()} bytes)")
                cacheFile
            } else {
                Log.e("FileUtil", "Error: El archivo de caché está vacío o no existe")
                null
            }
        } catch (e: Exception) {
            Log.e("FileUtil", "Error al guardar imagen en caché", e)
            null
        }
    }
    
    /**
     * Convierte un String a un RequestBody
     */
    fun createPartFromString(data: String): RequestBody {
        return data.toRequestBody("text/plain".toMediaTypeOrNull())
    }
    
    /**
     * Convierte un Boolean a un RequestBody
     */
    fun createPartFromBoolean(data: Boolean): RequestBody {
        return data.toString().toRequestBody("text/plain".toMediaTypeOrNull())
    }
    
    /**
     * Convierte un JSON array a un RequestBody
     */
    fun createPartFromJsonArray(jsonString: String): RequestBody {
        return jsonString.toRequestBody("application/json".toMediaTypeOrNull())
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
    
    /**
     * Obtiene el tamaño del archivo en bytes
     */
    private fun getFileSize(context: Context, uri: Uri): Long {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    return it.getLong(sizeIndex)
                }
            }
        }
        
        // Si no se puede obtener del cursor, intentar abriendo el stream
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val size = inputStream?.available()?.toLong() ?: 0L
            inputStream?.close()
            return size
        } catch (e: Exception) {
            android.util.Log.e("FileUtil", "No se pudo determinar el tamaño del archivo", e)
            return 0L
        }
    }

    /**
     * Obtiene un archivo desde un Uri
     */
    fun getFileFromUri(context: Context, uri: Uri): File {
        val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
} 