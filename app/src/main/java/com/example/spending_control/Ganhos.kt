package com.example.spending_control

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_despesas.view.*
import kotlinx.android.synthetic.main.activity_ganhos.*
import kotlinx.android.synthetic.main.lista_ganho.*

class Ganhos : AppCompatActivity() {
    lateinit var UserList: MutableList<User>
    lateinit var refGanho: DatabaseReference
    lateinit var btnAddGanhos: ImageButton
    lateinit var listaGanhos: ListView
    lateinit var imgDelLast: ImageButton
    lateinit var imgDelAll: ImageButton
    lateinit var idDel: String
    lateinit var listaID: MutableList<String>
    //variável que apontará para o index da listaID
    var contador: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ganhos)
        UserList = mutableListOf()
        listaID = mutableListOf()

        refGanho = FirebaseDatabase.getInstance().getReference("Usuário")


        btnAddGanhos = findViewById(R.id.imageADD)
        listaGanhos = findViewById(R.id.listaGanhos)
        imgDelLast = findViewById(R.id.imageExcluirGanho)
        imgDelAll = findViewById(R.id.imageDelAll)
        btnAddGanhos.setOnClickListener { inserirGanho() }
        imgDelLast.setOnClickListener { removerLast() }
        imgDelAll.setOnClickListener { removerAll() }




        get()
        //teste()

    }

    private fun inserirGanho() {
        var nome: String = editTextGanho.text.toString().trim()
        var valor: String = editValorGanho.text.toString().trim()

        val username = "Caio"

        val UID = refGanho.push().key.toString()
        // A listaID vai receber o valor criado pelo push.key
        listaID.add(UID)
        //o contador vai para o valor de 0 para apontar para o primeiro item da lista
        //contador = 0 index da lista = 0
        contador++

        val USU = User(username, nome, valor.toDouble())

        refGanho.child(UID).setValue(USU).addOnCompleteListener {
            Toast.makeText(this, "INSERT BEM SUCEDIDO", Toast.LENGTH_SHORT).show()
        }
    }

    private fun get() {
        refGanho.orderByChild("username").equalTo("Caio").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0!!.exists()) {
                    UserList.clear()
                    for (h in p0.children) {
                        val user = h.getValue(User::class.java)
                        UserList.add(user!!)
                    }

                    val adapter = AdapterGanhos(applicationContext, R.layout.lista_ganho, UserList)
                    listaGanhos.adapter = adapter
                } else {
                    val adapter = AdapterGanhos(applicationContext, R.layout.lista_ganho, UserList)
                    UserList.clear()
                    listaGanhos.adapter = adapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    //FUNÇÃO PARA VERIFICAR NO BANCO E EXISTE CERTO TIPO DE INFORMAÇÃO
    private fun teste() {
        refGanho.orderByChild("userid").equalTo("2").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0!!.exists()) {

                    Toast.makeText(applicationContext, "DEU BOM MAGO", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "DEU RUIM MAGO", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    private fun removerAll() {
        refGanho.removeValue().addOnCompleteListener {
            Toast.makeText(this, "Todas informações removidas com sucesso", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun removerLast() {
        refGanho.child(listaID.last()).removeValue().addOnCompleteListener {
            Toast.makeText(this, "Ultima informação removida com sucesso", Toast.LENGTH_LONG)
                .show()

                //vou remover o ultimo item da lista por meio do meu contador
                listaID.removeAt(contador)
                //contador apontará para o item anterior
                contador--
                Toast.makeText(this, contador.toString(), Toast.LENGTH_LONG).show()
        }
    }

}



