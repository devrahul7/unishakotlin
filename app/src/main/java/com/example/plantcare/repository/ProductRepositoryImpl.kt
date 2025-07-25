package com.example.plantcare.repository


import com.example.plantcare.model.ProductModel


import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateOf
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

class ProductRepositoryImpl : ProductRepository {


    val database = FirebaseDatabase.getInstance()
    val ref = database.reference.child("products")

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dl0ttohkp",
            "api_key" to "496435522733669",
            "api_secret" to "Wyr3snCCbDOETmjhMvYH_AILUj0"
        )
    )

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                // ✅ Fix: Remove extensions from file name before upload
                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?

                imageUrl = imageUrl?.replace("http://", "https://")

                // ✅ Run UI updates on the Main Thread
                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    override fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    override fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        val id = ref.push().key.toString()     // passing auto generated id so that there is no null value in product model
        model.productID = id
        ref.child(model.productID).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product added Successfully!")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteProduct(
        productID: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productID).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product deleted Successfully!")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateProduct(
        productID: String,
        productData: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(productID).updateChildren(productData).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product updated Successfully!")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getProductByID(
        productID: String,
        callback: (ProductModel?, Boolean, String) -> Unit
    ) {
        ref.child(productID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val product = snapshot.getValue(ProductModel::class.java)
                    if(product != null) {
                        callback(product, true, "product fetched")
                    }
                    else {
                        callback(null, false, "product not found")
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }

        })
    }

    override fun getAllProduct(callback: (List<ProductModel?>, Boolean, String) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    var allProducts = mutableListOf<ProductModel>()
                    for(eachProduct in snapshot.children) {
                        var products = eachProduct.getValue(ProductModel::class.java)
                        if(products != null) {
                            allProducts.add(products)
                        }

                    }
                    callback(allProducts, true, "product fetched")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList(), false, error.message)
            }

        })
    }
}