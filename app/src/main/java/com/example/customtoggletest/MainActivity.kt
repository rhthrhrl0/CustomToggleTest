package com.example.customtoggletest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.customtoggletest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ToggleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(ToggleViewModel::class.java)

        binding.lifecycleOwner = this
        binding.vm = viewModel


        binding.customTg.setOnCheckedChangeListener { _,_->
            viewModel.changeMode()
        }

        //isDisabled가 바인딩으로 연결되어있으므로 뷰모델에서 관찰당하는 놈을 바꾸면 됨.
        binding.disabledBt.setOnClickListener {
            viewModel.changeActive()
        }
    }
}