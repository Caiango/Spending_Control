package com.example.spending_control

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    lateinit var btDesp: Button
    lateinit var btGanho: Button
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var btUpdate: Button
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

        btDesp = findViewById(R.id.btGasto)
        btGanho = findViewById(R.id.btGanho)
        btUpdate = findViewById(R.id.buttonCalcular)

        colorLucro = ContextCompat.getColor(this, R.color.lucro)
        colorDespesa = ContextCompat.getColor(this, R.color.despesa)
        colorNeutro = ContextCompat.getColor(this, R.color.neutro)

        //EVENTOS DE CLICK BOTOES
        btDesp.setOnClickListener {
            val intent = Intent(this, Despesas::class.java)
            startActivity(intent)
        }
        btGanho.setOnClickListener {
            val intent = Intent(this, Ganhos::class.java)
            startActivity(intent)
        }
        btUpdate.setOnClickListener {

            getFirebaseValorGanho()

            getFirebaseValorGasto()

            saldo = saldoGanho - saldoGasto

            //Limitar numero para até 2 casas decimais
            var df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            txSaldo.text = "R$ ${df.format(saldo)}"

            screenColor()
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

            screenColor()
            getFirebaseValorGanho()
            getFirebaseValorGasto()

            txSaldo.text = "Saldo"

        }


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
        //Progress Bar para carregar informações
        val progress: ProgressDialog = ProgressDialog(this)
        progress.setTitle("Obtendo Dados")
        progress.setMessage("Carregando Dados")
        progress.show()
        //o refmain referecia Saldo
        //verifica dentro do banco os filhos que tiverem o idvalor = valorganho.
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
                            //pego o filho espcifico, transformo em double e jogo dentro de valorDesp
                            saldoGanho = valor.toString().toDouble()
                            txMostrarGanho.text = "GANHOS: R$ $saldoGanho"
                            progress.hide()
                            progress.dismiss()
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

    fun getFirebaseValorGasto() {
        //Progress Bar para carregar informações
        val progress: ProgressDialog = ProgressDialog(this)
        progress.setTitle("Obtendo Dados")
        progress.setMessage("Carregando Dados")
        progress.show()
        //o refmain referecia Saldo
        //verifica dentro do banco os filhos que tiverem o idvalor = valorganho.
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


}
