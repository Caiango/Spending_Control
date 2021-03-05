package com.example.spending_control

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    lateinit var googleSignInClient: GoogleSignInClient
    var saldoGanho: Double = 0.0
    var saldoGasto: Double = 0.0
    var colorLucro: Int = 0
    var colorDespesa: Int = 0
    var colorNeutro: Int = 0
    var saldo: Double = 0.0

    //Variável que armazenará o idgoogle do usuário logado
    var GOOGLEUSERID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Pegando cores do meu Resources
        colorLucro = ContextCompat.getColor(this, R.color.lucro)
        colorDespesa = ContextCompat.getColor(this, R.color.despesa)
        colorNeutro = ContextCompat.getColor(this, R.color.neutro)

        //EVENTOS DE CLICK BOTOES
        btGasto.setOnClickListener {
            val intent = Intent(this, Despesas::class.java)
            startActivity(intent)
        }
        btGanho.setOnClickListener {
            val intent = Intent(this, Ganhos::class.java)
            startActivity(intent)
        }

        //LOGIN GOOGLE
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

            var userid: String? = acct.id
            //GOOGLEUSERID não pode receber valor null, pois ele será utilizado no orderbychild que tbm não aceita null
            //Por isso o if
            if (userid!!.isNotEmpty()) {
                GOOGLEUSERID = userid.toString()
            }

            getFirebaseValorGanho()
            getFirebaseValorGasto()

            txSaldo.text = "Saldo"

        }


    }

    override fun onResume() {
        super.onResume()
        chamarResume()
    }

    fun screenColor() {
        // LOGICA COR DA TELA
        if (saldo > 0.0) {
            layoutID.setBackgroundColor(colorLucro)
            imageViewIni.setBackgroundColor(colorLucro)
        } else if (saldo < 0.0) {
            layoutID.setBackgroundColor(colorDespesa)
            imageViewIni.setBackgroundColor(colorDespesa)
        } else {
            layoutID.setBackgroundColor(colorNeutro)
            imageViewIni.setBackgroundColor(colorNeutro)
        }

    }

    fun getFirebaseValorGanho() {
        val progress: ProgressDialog = ProgressDialog(this)
        progress.setTitle("Obtendo Dados")
        progress.setMessage("Carregando Dados")
        progress.show()

        Ganhos.refMain.orderByChild("idvalor").equalTo("$GOOGLEUSERID GANHO")
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    if (p0!!.exists()) {
                        //se o select acima existir eu pego os filhos dentro de Saldo
                        //children = os filhos dentro de Saldo (apenas um nesse caso)
                        for (h in p0.children) {
                            //para todos elementos(h) dentro dos filhos de Saldo que atenderam ao meu select,
                            //child = pego o filho específico(h.child) dentro dos filhos de Saldo (o filho especifico é valor nesse caso),
                            //e jogo dentro de uma variavel
                            var valor = h.child("valor").getValue()
                            //pego o filho espcifico, transformo em double e jogo dentro de saldoGanho
                            saldoGanho = valor.toString().toDouble()
                            txMostrarGanho.text = "GANHOS: R$ $saldoGanho"
                            progress.hide()
                            progress.dismiss()
                            updateSaldo()
                            screenColor()
                        }

                    } else {
                        progress.hide()
                        progress.dismiss()

                    }

                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented")
                }
            })

    }

    fun getFirebaseValorGasto() {
        val progress: ProgressDialog = ProgressDialog(this)
        progress.setTitle("Obtendo Dados")
        progress.setMessage("Carregando Dados")
        progress.show()

        Ganhos.refMain.orderByChild("idvalor").equalTo("$GOOGLEUSERID GASTO")
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    if (p0!!.exists()) {
                        //se o select acima existir eu pego os filhos dentro de Saldo
                        //children = os filhos dentro de Saldo (apenas um nesse caso)
                        for (h in p0.children) {
                            //para todos elementos(h) dentro dos filhos de Saldo que atenderam ao meu select,
                            //child = pego o filho específico(h.child) dentro dos filhos de Saldo (o filho especifico é valor nesse caso),
                            //e jogo dentro de uma variavel
                            var valor = h.child("valor").getValue()
                            //pego o filho espcifico, transformo em double e jogo dentro de valorDesp
                            saldoGasto = valor.toString().toDouble()
                            txMostrarDesp.text = "DESPESAS: R$ $saldoGasto"
                            progress.hide()
                            progress.dismiss()
                            updateSaldo()
                            screenColor()
                        }

                    } else {
                        progress.hide()
                        progress.dismiss()

                    }

                }


                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })


    }

    fun chamarResume() {
        getFirebaseValorGanho()
        getFirebaseValorGasto()
        updateSaldo()
    }

    fun updateSaldo() {
        saldo = saldoGanho - saldoGasto

        var df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        txSaldo.text = "R$ ${df.format(saldo)}"

    }


}
