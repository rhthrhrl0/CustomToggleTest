package com.example.customtoggletest

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class ToggleViewModel: ViewModel() {
    companion object{
        private val ON:String="켜짐"
        private val OFF:String="꺼짐"
        private val NO_ACTIVE:String="비활성화"
    }

    //초깃값은 활성화에 꺼짐으로
    private val _toggleText:MutableLiveData<String> by lazy{
        MutableLiveData<String>().also{
            it.value= OFF
        }
    }
    val toggleText:LiveData<String>
        get() = _toggleText

    //초깃값은 활성화
    private val _disabled:MutableLiveData<Boolean> by lazy{
        MutableLiveData<Boolean>().also{
            it.value= false
        }
    }
    val disabled:LiveData<Boolean>
        get() = _disabled

    //초깃값은 선택되지 않음
    private val _selected:MutableLiveData<Boolean> by lazy{
        MutableLiveData<Boolean>().also{
            it.value= false
        }
    }
    val selected:LiveData<Boolean>
        get() = _selected


    //활성화되어있는 경우에만 동작해야함.
    fun changeMode(){
        Log.d("kmj","셀렉티드 변경 뷰모델")
        if(disabled.value==false){
            if(selected.value==true){
                //켜진상태였다면 반대로 끔
                _selected.value= false
                _toggleText.value= OFF
            }
            else{
                //꺼진상태였다면 킴
                _selected.value= true
                _toggleText.value= ON
            }
        }
    }

    fun changeActive(){
        //비활성화 모드였다면 다시 활성화 시키기
        if(disabled.value== true){
            _disabled.value= false
            if(selected.value==true)
                _toggleText.value= ON
            else
                _toggleText.value= OFF
        }
        else{
            //활성화모드였다면 비활성화로 바꾸기
            _disabled.value= true
            _toggleText.value= NO_ACTIVE
        }
    }
}