package com.example.spending_control


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    lateinit var refMain: DatabaseReference
    lateinit var btnAddGanhos: ImageButton
    lateinit var listaGanhos: ListView
    lateinit var imgDelLast: ImageButton
    lateinit var imgDelAll: ImageButton
    lateinit var arrayListaID: ArrayList<String>
    lateinit var arrayListaValor: ArrayList<String>
    var valorGanho: Double = 0.0

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
        refMain = FirebaseDatabase.getInstance().getReference("Saldo")

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
    }

    private fun inserirGanho() {
        var nome: String = editTextGanho.text.toString().trim()
        var valor: String = editValorGanho.text.toString().trim()
        var idinsert: String = ""

        var valorGanho2 = editValorGanho.text.toString().trim().toInt()
        valorGanho += valorGanho2

        Toast.makeText(this, valorGanho.toString(), Toast.LENGTH_SHORT).show()

        val UID = refGanho.push().key.toString()
        idinsert = UID

        val USU = User(GOOGLEUSERID, nome, valor.toDouble(), idinsert)

        refGanho.child(UID).setValue(USU).addOnCompleteListener {
            Toast.makeText(this, "INSERT FEITO COM SUCESSO", Toast.LENGTH_SHORT).show()
        }
        getUnique()
        salvarSaldo()

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
                        salvarSaldo()
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
            removerUltimoValor()
            salvarSaldo()
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
                        Toast.makeText(applicationContext, "Nao achei", Toast.LENGTH_LONG).show()
                    }

                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
    }

    fun removerUltimoValor(){
        valorGanho -= arrayListaValor.last().toInt()
        Toast.makeText(this, valorGanho.toString(), Toast.LENGTH_SHORT).show()
    }

    fun salvarSaldo(){
        val VALOR = Valores("valorganho", valorGanho)
        refMain.child("valorGanho").setValue(VALOR)
    }

}





