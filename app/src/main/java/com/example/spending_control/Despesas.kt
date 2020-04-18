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

    //lista responsavel por armazenar todos os dados trazidos do banco
    lateinit var UserList: ArrayList<User>
    lateinit var refGastos: DatabaseReference
    lateinit var refMain: DatabaseReference
    lateinit var btnAddGastos: ImageButton
    //lista responsavel por mostrar os dados ao usuario
    lateinit var listaGastos: ListView
    lateinit var imgDelLast: ImageButton
    lateinit var imgDelAll: ImageButton
    //lista para armazenar o idinsert trazidos do banco
    lateinit var arrayListaID: ArrayList<String>
    //lista para armazenar valores das despesas trazidos do banco
    lateinit var arrayListaValor: ArrayList<String>
    //variavel onde acumula o valor total de despesas, mando ela para o firebase
    var valorDesp: Double = 0.0

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

        //ver descrição das funções
        //pego e mostro os dados do firebase para o usuário
        get()
        //salvo nas listas o id e valor pegos do firebase
        getUnique()
        //pego o valorDesp dentro do firebase
        getFirebaseValor()

    }

    //PERGUNTA SER FEITA NOS "SELECTS": EX: DOS FILHOS DE GASTOS QUEM TEM O VALOR EQUALTO? SE TIVER FAÇA O SEGUINTE...

    private fun inserirDesp() {
        //passo a passo da função
        var nome: String = editTextDesp.text.toString().trim()
        var valor: String = editValorDesp.text.toString().trim()
        //variavel que vai receber o valor gerado pelo push.key e ser mandada para o banco com o mesmo valor do pai
        var idinsert: String = ""
        //Adicionar os valores digitados na edittext dentro de valorDesp
        var valorDesp2 = editValorDesp.text.toString().trim().toDouble()
        valorDesp += valorDesp2

        val UID = refGastos.push().key.toString()
        idinsert = UID

        //criando variavel onde armazenará todos os valores que serão enviados para o banco
        val USU = User(GOOGLEUSERID, nome, valor.toDouble(), idinsert)

        //mandando os dados para o banco
        refGastos.child(UID).setValue(USU).addOnCompleteListener {
            Toast.makeText(this, "Despesa adicionada com sucesso!", Toast.LENGTH_SHORT).show()
        }
        //ver descrição getUnique
        //pego id e valor que mandei pro banco e jogo nas listas
        getUnique()
        //ver descrição salvarSaldoFirebase
        //salvo a variável valorDesp no banco
        salvarSaldoFirebase()

    } //ok

    private fun get() {
        //addValueEventListener chama o tempo todo o evento
        //essa função joga os dados da minha pesquisa numa lista, por consequente a lista mostra para o usuário os dados
        refGastos.orderByChild("userid").equalTo(GOOGLEUSERID).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0!!.exists()) {
                    UserList.clear()
                    for (h in p0.children) {
                        //para todo valor dentro do filho jogo dentro da lista Userlist
                        val user = h.getValue(User::class.java)
                        UserList.add(user!!)
                    }
                    //seto o Adapter na minha listaganhos para mostrar ao usuário
                    val adapter = AdapterList(applicationContext, R.layout.lista_layout, UserList)
                    listaGastos.adapter = adapter
                } else {
                    //mesmo que não tenha dados atualizo a lista e mostro pro usuario
                    val adapter = AdapterList(applicationContext, R.layout.lista_layout, UserList)
                    UserList.clear()
                    listaGastos.adapter = adapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    } //ok

    private fun removerAll() {
        //addListenerForSingleValueEvent chama apenas uma vez o evento (é necessário chamar apenas uma vez aqui)
        //Essa função verifica se o googleuserid existe dentro do banco, se existir executa o evento dentro do if
        refGastos.orderByChild("userid").equalTo(GOOGLEUSERID)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    if (p0!!.exists()) {
                        UserList.clear()
                        for (h in p0.children) {
                            h.ref.removeValue()
                        }
                        //seta o adapter na minha listagastos
                        val adapter = AdapterList(applicationContext, R.layout.lista_layout, UserList)
                        listaGastos.adapter = adapter
                        //já que excluí tudo do firebase eu seto o valor de valorDesp como 0.0
                        valorDesp = 0.0
                        //e mando pro firebase
                        salvarSaldoFirebase()
                    } else {
                        val adapter =  AdapterList(applicationContext, R.layout.lista_layout, UserList)
                        UserList.clear()
                        listaGastos.adapter = adapter
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
    } //ok

    private fun removerLast() {
        //os filhos de Gastos que tiveram idinsert igual ao arraylistID.last serão excluidos, como o idsert é igual ao UID, ou seja úncio
        //so exclui um filho, no caso o ultimo
        refGastos.child(arrayListaID.last()).removeValue().addOnCompleteListener {
            Toast.makeText(this, "Ultima informação removida com sucesso", Toast.LENGTH_LONG)
                .show()
            //pego os valores atuais do firebase e salvo o id e valor nas listas
            getUnique()
            //removo o ultimo valor da lista local
            removerUltimoValorLista()
            //salva o ultimo valor acumulado das despesas no banco
            salvarSaldoFirebase()

        }
    } //ok

    private fun getUnique() {
        //essa função joga os idinsert dentro de uma lista (arrayListaID)
        //essa função tbm joga os valores dentro de uma lista (arrayListaValor)
        //se o valor encontrado dentro de userid(filho dentro de refgastos) = ao googleuserid
        refGastos.orderByChild("userid").equalTo(GOOGLEUSERID)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                //singleEventListener: só é necessário que chame o evento uma vez
                override fun onDataChange(p0: DataSnapshot) {

                    if (p0!!.exists()) {
                        //se existir meu select acima, para toda informação dentro do filho:
                        for (datas in p0.getChildren()) {
                            //pega o valor idinsert
                            var unicoID = datas.child("idinsert").getValue().toString()
                            //pega o valor "valor"
                            var unicoValor = datas.child("valor").getValue().toString()
                            //adiciona dentro das minhas listas, essas listas serão utilizadas na exclusão do ultimo elemento (ID ou Valor)
                            arrayListaID.add(unicoID)
                            arrayListaValor.add(unicoValor)

                        }
                    } else {
                        //ainda não há informações adicionadas
                    }

                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
    } //ok

    fun removerUltimoValorLista() {
        //anteriormente peguei todos os valores do banco e joguei na lista
        //agora eu excluo da minha lista local o ultimo valor
        valorDesp -= arrayListaValor.last().toDouble()
    } //ok

    fun salvarSaldoFirebase() {
        //todo insert será feito dentro do mesmo pathstring(valorDesp), ou seja um update...
        val VALOR = Valores("$GOOGLEUSERID GASTO", valorDesp)
        refMain.child("$GOOGLEUSERID GASTO").setValue(VALOR)

    }//ok

    fun getFirebaseValor() {
        //o refmain referecia Saldo
        //verifica dentro do banco os filhos que tiverem o idvalor = valordesp.
        refMain.orderByChild("idvalor").equalTo("$GOOGLEUSERID GASTO").addValueEventListener(object :
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
                        valorDesp = valor.toString().toDouble()
                    }


                } else {

                }

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    } //ok
}
