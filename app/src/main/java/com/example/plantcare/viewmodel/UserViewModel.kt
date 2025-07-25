package com.example.plantcare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.plantcare.model.UserModel
import com.example.plantcare.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

class UserViewModel(val repo : UserRepository) : ViewModel() {

    fun login(email : String, password : String, callback : (Boolean, String) -> Unit) {
        repo.login(email, password, callback)
    }
    fun register(email : String, password : String, callback : (Boolean, String, String) -> Unit) {
        repo.register(email, password, callback)
    }
    fun forgetPassword(email : String, callback : (Boolean, String) ->Unit) {
        repo.forgetPassword(email, callback)
    }
    fun getCurrentUser() : FirebaseUser? {
        return repo.getCurrentUser()
    }
    fun addUserToDatabase(userID : String, model: UserModel, callback: (Boolean, String) ->Unit) {
        repo.addUserToDatabase(userID, model, callback)
    }
    fun logout(callback : (Boolean, String) ->Unit) {
        repo.logout(callback)
    }

    private var _users = MutableLiveData<UserModel?>()
    val users : LiveData<UserModel?> get() = _users


    fun getUserByID(userID : String) {
        repo.getUserByID(userID) { users, success, message ->
            if(success && users != null) {
                _users.postValue(users)
            }
            else {
                _users.postValue(null)
            }
        }
    }


}

private fun UserRepository.getUserByID(userID: String, function: Any) {}
