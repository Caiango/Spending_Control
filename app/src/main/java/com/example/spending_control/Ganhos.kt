package com.example.spending_control


import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_ganhos.*

class Ganhos : AppCompatActivity() {
    lateinit var UserList: ArrayList<User>
    lateinit var refGanho: DatabaseReference
    lateinit var btnAddGanhos: ImageButton
    lateinit var listaGanhos: ListView
    lateinit var imgDelLast: ImageButton
    lateinit var imgDelAll: ImageButton
    lateinit var arrayListaID: ArrayList<String>
    lateinit var arrayListaValor: ArrayList<String>

    companion object {
    var valorGanho: Double = 0.0
    var refMain: DatabaseReference = FirebaseDatabase.getInstance().getReference("Saldo")
    }


    //Variável que armazenará o idgoogle do usuário logado
    var GOOGLEUSERID: String = ""
    lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ganhos)
        UserList = arrayListOf()
        arrayListaID = arrayListOf()
        arrayListaValor = arrayListOf()

        refGanho = FirebaseDatabase.getInstance().getReference("Ganhos")
        //refMain = FirebaseDatabase.getInstance().getReference("Saldo")

        btnAddGanhos = findViewById(R.id.imageADD)
        listaGanhos = findViewById(R.id.listaGanhos)
        imgDelLast = findViewById(R.id.imageExcluirGanho)
        imgDelAll = findViewById(R.id.imageDelAll)
        btnAddGanhos.setOnClickListener {
            if (editTextGanho.text.isEmpty() || editValorGanho.text.isEmpty()) {
                editTextGanho.setError("Insira o nome do produto")
                editValorGanho.setError("Insira um valor")
            } else {
                inserirGanho()
            }
        }
        imgDelLast.setOnClickListener { removerLast() }
        imgDelAll.setOnClickListener { removerAll() }

        //Login Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val acct = GoogleSignIn.getLastSignedInAccount(this)

        if (acct != null) {

            var userid: String? = acct.id
            //GOOGLEUSERID não pode receber valor null, pois ele será utilizado no orderbychild que tbm não aceita null
            //Por isso o if
            if (userid!!.isNotEmpty()) {
                GOOGLEUSERID = userid.toString()
            }

        }

        get()
        getUnique()
        getFirebaseValor()
    }

    private fun inserirGanho() {
        var nome: String = editTextGanho.text.toString().trim()
        var valor: String = editValorGanho.text.toString().trim()
        var idinsert: String = ""

        var valorGanho2 = editValorGanho.text.toString().trim().toDouble()
        valorGanho += valorGanho2

        val UID = refGanho.push().key.toString()
        idinsert = UID

        val USU = User(GOOGLEUSERID, nome, valor.toDouble(), idinsert)

        refGanho.child(UID).setValue(USU).addOnCompleteListener {
            Toast.makeText(this, "Ganho inserido com sucesso!", Toast.LENGTH_SHORT).show()
        }
        getUnique()
        salvarSaldoFirebase()

    }

    private fun get() {
        //addValueEventListener chama o tempo todo o evento
        //essa função joga os dados da minha pesquisa numa lista, por consequente a lista mostra para o usuário os dados
        refGanho.orderByChild("userid").equalTo(GOOGLEUSERID).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0!!.exists()) {
                    UserList.clear()
                    for (h in p0.children) {
                        val user = h.getValue(User::class.java)
                        UserList.add(user!!)
                    }

                    val adapter = AdapterList(applicationContext, R.layout.lista_layout, UserList)
                    listaGanhos.adapter = adapter
                } else {
                    val adapter = AdapterList(applicationContext, R.layout.lista_layout, UserList)
                    UserList.clear()
                    listaGanhos.adapter = adapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    private fun removerAll() {
        //addListenerForSingleValueEvent chama apenas uma vez o evento
        //Essa função verifica se o userid existe dentro do banco, se existir executa o evento dentro do if
        refGanho.orderByChild("userid").equalTo(GOOGLEUSERID)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    if (p0!!.exists()) {
                        UserList.clear()
                        for (h in p0.children) {
                            h.ref.removeValue()
                        }

                        val adapter =
                            AdapterList(applicationContext, R.layout.lista_layout, UserList)
                        listaGanhos.adapter = adapter
                        valorGanho = 0.0
                        salvarSaldoFirebase()
                    } else {
                        val adapter =
                            AdapterList(applicationContext, R.layout.lista_layout, UserList)
                        UserList.clear()
                        listaGanhos.adapter = adapter
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
    }

    private fun removerLast() {
        //exclui o filho que tem o valor passado dentro do child
        refGanho.child(arrayListaID.last()).removeValue().addOnCompleteListener {
            Toast.makeText(this, "Ultima informação removida com sucesso", Toast.LENGTH_LONG)
                .show()
            getUnique()
            removerUltimoValorLista()
            salvarSaldoFirebase()
        }
    }

    private fun getUnique() {
        //essa função joga os idinsert dentro de uma lista
        refGanho.orderByChild("userid").equalTo(GOOGLEUSERID)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    if (p0!!.exists()) {
                        for (datas in p0.getChildren()) {
                            //pega o valor idinsert
                            var unico = datas.child("idinsert").getValue().toString()
                            var unicoValor = datas.child("valor").getValue().toString()
                            //adiciona dentro da minha lista, essa lista será utilizada na exclusão do ultimo elemento
                            arrayListaID.add(unico)
                            arrayListaValor.add(unicoValor)

                        }
                    } else {
                        Toast.makeText(applicationContext, "Nao há dados salvos", Toast.LENGTH_LONG)
                            .show()
                    }

                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
    }

    fun removerUltimoValorLista() {
        valorGanho -= arrayListaValor.last().toDouble()
    }

    fun salvarSaldoFirebase() {
        val VALOR = Valores("$GOOGLEUSERID GANHO", valorGanho)
        refMain.child("$GOOGLEUSERID GANHO").setValue(VALOR)
    }


    fun getFirebaseValor() {
        //Progress Bar para carregar informações
        val progress: ProgressDialog = ProgressDialog(this)
        progress.setTitle("ProgressDialog")
        progress.setMessage("Carregando Dados")
        progress.show()
        //o refmain referecia Saldo
        //verifica dentro do banco os filhos que tiverem o idvalor = valorganho.
        refMain.orderByChild("idvalor").equalTo("$GOOGLEUSERID GANHO")
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
                            valorGanho = valor.toString().toDouble()
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





