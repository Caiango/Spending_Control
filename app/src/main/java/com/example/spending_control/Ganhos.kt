package com.example.spending_control

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_despesas.view.*
import kotlinx.android.synthetic.main.activity_ganhos.*
import kotlinx.android.synthetic.main.lista_ganho.*
import java.lang.reflect.Type

class Ganhos : AppCompatActivity() {
    lateinit var UserList: ArrayList<User>
    lateinit var refGanho: DatabaseReference
    lateinit var btnAddGanhos: ImageButton
    lateinit var listaGanhos: ListView
    lateinit var imgDelLast: ImageButton
    lateinit var imgDelAll: ImageButton
    lateinit var arrayListaID: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ganhos)
        UserList = arrayListOf()
        arrayListaID = arrayListOf()

        refGanho = FirebaseDatabase.getInstance().getReference("Ganhos")

        btnAddGanhos = findViewById(R.id.imageADD)
        listaGanhos = findViewById(R.id.listaGanhos)
        imgDelLast = findViewById(R.id.imageExcluirGanho)
        imgDelAll = findViewById(R.id.imageDelAll)
        btnAddGanhos.setOnClickListener { inserirGanho() }
        imgDelLast.setOnClickListener { removerLast() }
        imgDelAll.setOnClickListener { removerAll() }

        get()
        //teste()
        getUnique()
    }

    private fun inserirGanho() {
        var nome: String = editTextGanho.text.toString().trim()
        var valor: String = editValorGanho.text.toString().trim()
        var idinsert: String = ""

        val username = "Caio"

        val UID = refGanho.push().key.toString()
        idinsert = UID

        val USU = User(username, nome, valor.toDouble(), idinsert)

        refGanho.child(UID).setValue(USU).addOnCompleteListener {
            Toast.makeText(this, "INSERT FEITO COM SUCESSO", Toast.LENGTH_SHORT).show()
        }
        getUnique()

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

    //FUNÇÃO PARA VERIFICAR NO BANCO SE EXISTE CERTO TIPO DE INFORMAÇÃO
    private fun teste() {
        refGanho.orderByChild("username").equalTo("Caio").addValueEventListener(object :
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
        refGanho.child(arrayListaID.last()).removeValue().addOnCompleteListener {
            Toast.makeText(this, "Ultima informação removida com sucesso", Toast.LENGTH_LONG)
                .show()
            getUnique()
        }
    }

    private fun getUnique() {
        refGanho.orderByChild("username").equalTo("Caio").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0!!.exists()) {
                    for (datas in p0.getChildren()) {
                        var unico = datas.child("idinsert").getValue().toString()
                        arrayListaID.add(unico)

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

}





