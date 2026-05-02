package com.abryan.pointofsales.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abryan.pointofsales.model.ModelProduk

class DataProdukViewModel : ViewModel() {

    private val _listProduk = MutableLiveData<MutableList<ModelProduk>>()
    val listProduk: LiveData<MutableList<ModelProduk>> = _listProduk

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading


}