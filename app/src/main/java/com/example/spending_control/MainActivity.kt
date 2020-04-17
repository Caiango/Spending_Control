package com.example.spending_control

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var btDesp: Button
    lateinit var btGanho: Button
    lateinit var refMain: DatabaseReference

    //Variáveis de acesso as outras activities
    lateinit var mGastos: Despesas
    lateinit var mGanhos: Ganhos

    var saldo: Int = 0

    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Inicializados as variaeis de acesso as activities
        mGanhos = Ganhos()
        mGastos = Despesas()

        getsaldo()

        btDesp = findViewById(R.id.btGasto)
        btGanho = findViewById(R.id.btGanho)

        val colorLucro = ContextCompat.getColor(this, R.color.lucro)
        val colorDespesa = ContextCompat.getColor(this, R.color.despesa)
        val colorNeutro = ContextCompat.getColor(this, R.color.neutro)

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



        refMain = FirebaseDatabase.getInstance().getReference("Saldo")

        btDesp.setOnClickListener {
            val intent = Intent(this, Despesas::class.java)
            startActivity(intent)
        }
        btGanho.setOnClickListener {
            val intent = Intent(this, Ganhos::class.java)
            startActivity(intent)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val acct = GoogleSignIn.getLastSignedInAccount(this)

        if (acct != null) {

            ButSair.setOnClickListener {
                googleSignInClient.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

        }
    }

    fun getsaldo (){
        //Valor das despesas - valor dos ganhos para obter o saldo atual
        saldo = mGanhos.valorGanho - mGastos.valorDesp
        txSaldo.text = saldo.toString()
        Toast.makeText(this, mGanhos.valorGanho.toString(), Toast.LENGTH_SHORT).show()
    }

}
