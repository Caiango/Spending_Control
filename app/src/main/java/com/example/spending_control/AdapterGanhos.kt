package com.example.spending_control

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterGanhos(val mCtx: Context, val layoutResId: Int, val userList: List<User>) :
    ArrayAdapter<User>(mCtx, layoutResId, userList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(mCtx)
        val view: View = layoutInflater.inflate(layoutResId, null)

        val textViewNome = view.findViewById<TextView>(R.id.textViewElementoNome)
        val textViewFone = view.findViewById<TextView>(R.id.textViewElementoValor)

        val user = userList[position]

        textViewNome.text = user.elemento
        textViewFone.text = user.valor.toString()

        return view
    }


}