package com.example.courseschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import com.example.courseschedule.data.TimetableStorage

class ViewModelFactory(
    private val storage:TimetableStorage,
    private val context: Context
)
    :ViewModelProvider.Factory{
    override fun <T:ViewModel> create(modelClass:Class<T>):T{
        if(modelClass.isAssignableFrom(TimetableViewModel::class.java)
        ){
            return TimetableViewModel(storage, context) as T
        }
        throw IllegalArgumentException(
            "Unknown ViewModel"
        )
    }
}