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
import kotlinx.android.synthetic.main.activity_despesas.*

class Despesas : AppCompatActivity() {

    lateinit var UserList: ArrayList<User>
    lateinit var refGastos: DatabaseReference
    lateinit var refMain: DatabaseReference
    lateinit var btnAddGastos: ImageButton
    lateinit var listaGastos: ListView
    lateinit var imgDelLast: ImageButton
    lateinit var imgDelAll: ImageButton
    lateinit var arrayListaID: ArrayList<String>
    lateinit var arrayListaValor: ArrayList<String>
    var valorDesp: Int = 0

    //Variável que armazenará o idgoogle do usuário logado
    var GOOGLEUSERID: String = ""
    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_despesas)
        UserList = arrayListOf()
        arrayListaID = arrayListOf()
        arrayListaValor = arrayListOf()

        refGastos = FirebaseDatabase.getInstance().getReference("Gastos")
        refMain = FirebaseDatabase.getInstance().getReference("Saldo")

        btnAddGastos = findViewById(R.id.imageADDDesp)
        listaGastos = findViewById(R.id.listaDesp)
        imgDelLast = findViewById(R.id.imageExcluirDesp)
        imgDelAll = findViewById(R.id.imageDelAllDesp)
        btnAddGastos.setOnClickListener {
            if (editTextDesp.text.isEmpty() || editValorDesp.text.isEmpty()) {
                editTextDesp.setError("Insira o nome do produto")
                editValorDesp.setError("Insira um valor")
            } else {
                inserirDesp()
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

    private fun inserirDesp() {
        var nome: String = editTextDesp.text.toString().trim()
        var valor: String = editValorDesp.text.toString().trim()
        var idinsert: String = ""
        var valorDesp2 = editValorDesp.text.toString().trim().toInt()
        valorDesp += valorDesp2

        Toast.makeText(this, valorDesp.toString(), Toast.LENGTH_SHORT).show()

        val UID = refGastos.push().key.toString()
        idinsert = UID

        val USU = User(GOOGLEUSERID, nome, valor.toDouble(), idinsert)

        refGastos.child(UID).setValue(USU).addOnCompleteListener {
            Toast.makeText(this, "INSERT FEITO COM SUCESSO", Toast.LENGTH_SHORT).show()
        }
        getUnique()
        salvarSaldo()

    }

    private fun get() {
        //addValueEventListener chama o tempo todo o evento
        //essa função joga os dados da minha pesquisa numa lista, por consequente a lista mostra para o usuário os dados
        refGastos.orderByChild("userid").equalTo(GOOGLEUSERID).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0!!.exists()) {
                    UserList.clear()
                    for (h in p0.children) {
                        val user = h.getValue(User::class.java)
                        UserList.add(user!!)
                    }

                    val adapter = AdapterList(applicationContext, R.layout.lista_layout, UserList)
                    listaGastos.adapter = adapter
                } else {
                    val adapter = AdapterList(applicationContext, R.layout.lista_layout, UserList)
                    UserList.clear()
                    listaGastos.adapter = adapter
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
        refGastos.orderByChild("userid").equalTo(GOOGLEUSERID)
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
                        listaGastos.adapter = adapter
                        valorDesp = 0
                        salvarSaldo()
                    } else {
                        val adapter =
                            AdapterList(applicationContext, R.layout.lista_layout, UserList)
                        UserList.clear()
                        listaGastos.adapter = adapter
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
    }

    private fun removerLast() {
        //exclui o filho que tem o valor passado dentro do child
        refGastos.child(arrayListaID.last()).removeValue().addOnCompleteListener {
            Toast.makeText(this, "Ultima informação removida com sucesso", Toast.LENGTH_LONG)
                .show()
            getUnique()
            removerUltimoValor()
            salvarSaldo()

        }
    }

    private fun getUnique() {
        //essa função joga os idinsert dentro de uma lista (arrayListaID)
        //essa função tbm joga os valores dentro de uma lista (arrayListaValor)
        refGastos.orderByChild("userid").equalTo(GOOGLEUSERID)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    if (p0!!.exists()) {
                        for (datas in p0.getChildren()) {
                            //pega o valor idinsert
                            var unicoID = datas.child("idinsert").getValue().toString()
                            var unicoValor = datas.child("valor").getValue().toString()
                            //adiciona dentro da minha lista, essa lista será utilizada na exclusão do ultimo elemento
                            arrayListaID.add(unicoID)
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
        valorDesp -= arrayListaValor.last().toInt()
        Toast.makeText(this, valorDesp.toString(), Toast.LENGTH_SHORT).show()
    }

    fun salvarSaldo(){
        val VALOR = Valores("valordesp", valorDesp)
        refMain.child("valorDesp").setValue(VALOR)

    }
}
