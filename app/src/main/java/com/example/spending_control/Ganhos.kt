package com.example.spending_control

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_ganhos.*

class Ganhos : AppCompatActivity() {
    lateinit var UserList: MutableList<User>
    lateinit var refGanho: DatabaseReference
    lateinit var btnAddGanhos: Button
    lateinit var listaGanhos: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ganhos)
        UserList = mutableListOf()


        refGanho = FirebaseDatabase.getInstance().getReference("Usuário")

        btnAddGanhos = findViewById(R.id.btAddGanho)
        listaGanhos = findViewById(R.id.listaGanhos)
        btnAddGanhos.setOnClickListener { inserirGanho() }


        get()
        teste()

    }

    private fun inserirGanho() {
        var nome: String = editTextGanho.text.toString().trim()
        var valor: String = editValorGanho.text.toString().trim()
        val uid = "2"

        val UID = refGanho.push().key.toString()
        val USU = User(uid, nome, valor.toDouble())

        refGanho.child(UID).setValue(USU).addOnCanceledListener {
            Toast.makeText(this, "INSERT BEM SUCEDIDO", Toast.LENGTH_LONG).show()
        }
    }

    private fun get() {
        refGanho.orderByChild("userid").equalTo("2").addValueEventListener(object :
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
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    //FUNÇÃO PARA VERIFICAR NO BANCO E EXISTE CERTO TIPO DE INFORMAÇÃO
    private fun teste(){
        refGanho.orderByChild("userid").equalTo("2").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0!!.exists()) {
                    Toast.makeText(applicationContext, "DEU CERTO MAGO", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(applicationContext, "DEU RUIM MAGO", Toast.LENGTH_LONG).show()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

}



