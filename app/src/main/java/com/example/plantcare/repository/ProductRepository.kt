package com.example.plantcare.repository


import android.content.Context
import android.net.Uri
import com.example.plantcare.model.ProductModel

interface ProductRepository {
    fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit)
    fun deleteProduct(productID : String, callback: (Boolean, String) -> Unit)
    fun updateProduct(productID : String, productData : MutableMap<String, Any?>, callback: (Boolean, String) -> Unit)
    fun getProductByID(productID : String, callback : (ProductModel?, Boolean, String) ->Unit)
    fun getAllProduct(callback: (List<ProductModel?>, Boolean, String) -> Unit)


    fun uploadImage(context: Context,imageUri: Uri, callback: (String?) -> Unit)

    fun getFileNameFromUri(context: Context,uri: Uri): String?

}

