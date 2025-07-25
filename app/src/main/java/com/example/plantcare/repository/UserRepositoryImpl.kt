package com.example.plantcare.repository


import com.example.plantcare.model.UserModel


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepositoryImpl : UserRepository {



    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref : DatabaseReference = database.reference.child("users")


    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful) {
                callback(true, "Login Successfully")
            }
            else {
                callback(false, "${it.exception?.message}")
            }
        }

    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful) {
                callback(true, "Registered Successfully", "${auth.currentUser?.uid}")
            }
            else {
                callback(false, "${it.exception?.message}", "")
            }
        }

    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {



        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if(it.isSuccessful) {
                callback(true, "Reset email sent to $email")
            }
            else {
                callback(false, "${it.exception?.message}")
            }
        }


    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }


    //Create
    override fun addUserToDatabase(
        userID: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userID).setValue(model).addOnCompleteListener {
            if(it.isSuccessful) {
                callback(true, "Registration Succesfull")
            }
            else {
                callback(false, "${it.exception?.message}")
            }
        }
    }


    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "Logout successful")
        } catch ( e : Exception  ) {
            callback(false, "${e.message}")
        }
    }

    override fun getUserByID(
        userID: String,
        callback: (UserModel?, Boolean, String) -> Unit
    ) {
        ref.child(userID).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val users = snapshot.getValue(UserModel::class.java)
                    if (users != null ) {
                        callback(users,true,"data fetched")
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                callback(null,false,error.message)
            }
        })
    }




}