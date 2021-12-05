package com.moon.loadinganimationtextview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.moon.loadinganimationtextview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.setText(
            LoadingTextView.Companion.TextAttribute(
                "HelloWorld!",
                28f,
                "#000000"
            )
        )

        binding.btnStart.setOnClickListener{
            binding.textView.startLoading()
        }

        binding.btnNewValue.setOnClickListener {
            binding.textView.stopFadeOutLoading {
                binding.textView.startFirst(
                    LoadingTextView.Companion.TextAttribute(
                        "NewText",
                        30f,
                        "#123456"
                    )
                )
            }
        }

        binding.btnStop.setOnClickListener {
            binding.textView.stopFadeInLoading()
        }
    }
}