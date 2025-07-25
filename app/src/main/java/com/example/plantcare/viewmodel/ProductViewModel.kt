package com.example.plantcare.viewmodel

import com.example.plantcare.model.ProductModel


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.plantcare.repository.ProductRepository

class ProductViewModel(val repo : ProductRepository) : ViewModel() {

    fun uploadImage(context: Context,imageUri: Uri, callback: (String?) -> Unit){
        repo.uploadImage(context,imageUri,callback)
    }

    fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
        repo.addProduct(model, callback)
    }
    fun deleteProduct(productID : String, callback: (Boolean, String) -> Unit) {
        repo.deleteProduct(productID, callback)
    }
    fun updateProduct(productID : String, productData : MutableMap<String, Any?>, callback: (Boolean, String) -> Unit) {
        repo.updateProduct(productID, productData, callback)
    }

    private val _products = MutableLiveData<ProductModel?>()
    val products : LiveData<ProductModel?> get() = _products
    fun getProductByID(productID : String) {
        repo.getProductByID(productID) {
                data, success, message ->
            if(success) {
                _products.postValue(data)
            }
            else {
                _products.postValue(null)
            }
        }
    }



    private val _allProducts = MutableLiveData<List<ProductModel?>>()
    val allProducts: LiveData<List<ProductModel?>> get() = _allProducts

    private val _loading = MutableLiveData<Boolean>()
    var loading = MutableLiveData<Boolean>()
        get() = _loading

    fun getAllProduct() {
        _loading.postValue(true)
        repo.getAllProduct  { data, success, message ->
            if (success) {
                _loading.postValue(false)

                Log.d("check",message)
                _allProducts.postValue(data)
            } else {
                _loading.postValue(false)

                Log.d("check",message)
                _allProducts.postValue(emptyList())

            }}
    }
}