package com.example.spending_control

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var btDesp: Button
    lateinit var btGanho: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btDesp = findViewById(R.id.btGasto)
        btGanho = findViewById(R.id.btGanho)

        val saldo = 1
        val colorLucro = ContextCompat.getColor(this, R.color.lucro)
        val colorDespesa = ContextCompat.getColor(this, R.color.despesa)
        val colorNeutro = ContextCompat.getColor(this, R.color.neutro)

        btDesp.setOnClickListener {
            val intent = Intent(this, Despesas::class.java)
            startActivity(intent)
        }
        btGanho.setOnClickListener {
            val intent = Intent(this, Ganhos::class.java)
            startActivity(intent)
        }

        if (saldo > 0) {
            layoutID.setBackgroundColor(colorLucro)
            imageViewIni.setBackgroundColor(colorLucro)
        } else if (saldo < 0) {
            layoutID.setBackgroundColor(colorDespesa)
            imageViewIni.setBackgroundColor(colorDespesa)
        } else {
            layoutID.setBackgroundColor(colorNeutro)
            imageViewIni.setBackgroundColor(colorNeutro)
        }
    }
}
