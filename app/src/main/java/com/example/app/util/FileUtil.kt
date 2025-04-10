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
     * Enfoque simplificado con mejor manejo de errores
     */
    fun uriToMultipartImage(context: Context, uri: Uri, paramName: String): MultipartBody.Part? {
        try {
            // Añadir cerca de la línea 201, justo después de detectar que hay imagen
            // (imagen != null)
            android.util.Log.d("FileUtil", "Comprobando si hay imagen")
            if (uri == null) {
                android.util.Log.e("FileUtil", "La imagen es null")
                return null
            }
            
            // Obtener toda la información posible sobre la imagen
            val mimeType = context.contentResolver.getType(uri) ?: "unknown/mime"
            android.util.Log.d("FileUtil", "MIME type desde contentResolver: $mimeType")
            
            try {
                android.util.Log.d("FileUtil", "Intentando crear archivo temporal desde URI")
                val imageFile = getFileFromUri(context, uri)
                android.util.Log.d("FileUtil", "URI: $uri")
                android.util.Log.d("FileUtil", "Ruta archivo: ${imageFile.absolutePath}")
                android.util.Log.d("FileUtil", "Existe: ${imageFile.exists()}")
                android.util.Log.d("FileUtil", "Tamaño: ${imageFile.length()} bytes")
                android.util.Log.d("FileUtil", "Es archivo: ${imageFile.isFile}")
                android.util.Log.d("FileUtil", "Permisos: R=${imageFile.canRead()}, W=${imageFile.canWrite()}")
            } catch (e: Exception) {
                android.util.Log.e("FileUtil", "Error al analizar la imagen", e)
            }
            
            // Comprobar si la imagen es demasiado grande
            val fileSize = getFileSize(context, uri)
            android.util.Log.d("FileUtil", "Tamaño de archivo calculado: $fileSize bytes (${fileSize/1024} KB)")
            
            if (fileSize > 5 * 1024 * 1024) { // 5MB límite
                android.util.Log.e("FileUtil", "La imagen es demasiado grande: ${fileSize/1024/1024}MB")
                return null
            }
            
            try {
                android.util.Log.d("FileUtil", "Abriendo InputStream para leer bytes")
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    android.util.Log.e("FileUtil", "No se pudo obtener InputStream de la URI")
                    return null
                }
                
                android.util.Log.d("FileUtil", "Leyendo bytes desde InputStream")
                val byteArray = inputStream.readBytes()
                inputStream.close()
                
                android.util.Log.d("FileUtil", "Bytes leídos: ${byteArray.size}")
                
                if (byteArray.isEmpty()) {
                    android.util.Log.e("FileUtil", "Array de bytes está vacío")
                    return null
                }
                
                // Nombre de archivo simple sin caracteres especiales
                val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                val fileName = "image_$timeStamp.jpg"
                
                android.util.Log.d("FileUtil", "Procesando imagen: $fileName, tamaño: ${byteArray.size} bytes")
                
                // Crear RequestBody directamente desde los bytes
                android.util.Log.d("FileUtil", "Creando RequestBody con MIME type: $mimeType")
                val requestBody = byteArray.toRequestBody(mimeType.toMediaTypeOrNull())
                
                // Crear la parte multipart con un nombre de archivo sencillo
                android.util.Log.d("FileUtil", "Creando MultipartBody.Part")
                val part = MultipartBody.Part.createFormData(paramName, fileName, requestBody)
                android.util.Log.d("FileUtil", "MultipartBody.Part creado exitosamente")
                
                return part
            } catch (e: Exception) {
                android.util.Log.e("FileUtil", "Error procesando bytes de la imagen", e)
                return null
            }
        } catch (e: Exception) {
            android.util.Log.e("FileUtil", "Error general en uriToMultipartImage", e)
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