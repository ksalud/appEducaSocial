package itr.ksj.appeducasocial

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import itr.ksj.appeducasocial.databinding.ActivityBuscarUserBinding


class BuscarUserActivity : AppCompatActivity() {

    private val enlace: ActivityBuscarUserBinding by lazy {//lazy para inicalizar una variable mas tarde
        ActivityBuscarUserBinding.inflate(layoutInflater)
    }
    private lateinit var lista:RecyclerView

    private lateinit var allUserDateBase:DatabaseReference
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(enlace.root)

        allUserDateBase = FirebaseDatabase.getInstance().reference.child("Users")
        lista=enlace.listaUser
        lista.setHasFixedSize(true)
        lista.layoutManager=LinearLayoutManager(this)

        enlace.buscarEt.addTextChangedListener{
            if(!it.isNullOrBlank()){
                enlace.btnPrueba.visibility=View.VISIBLE
            }else{
                enlace.btnPrueba.visibility=View.INVISIBLE
            }
        }
        enlace.btnPrueba.setOnClickListener {
            BuscarAmigos(enlace.buscarEt.text.toString())
        }

    }

    private fun BuscarAmigos( nombreAmigo:String) {

        var usuario= db.collection("Users").orderBy("nombre").startAt(nombreAmigo).endAt(nombreAmigo + "\uf8ff").addSnapshotListener{value,error ->
            val user = value!!.toObjects(Users::class.java)
            user.forEachIndexed { index, user ->
                user.uid = value.documents[index].id
            }
            enlace.listaUser.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@BuscarUserActivity)
                adapter =UserAdaptador(this@BuscarUserActivity,user)
            }
        }

        }



    inner class EncontrarAmigoHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        /*fun setProfileimage(contexto: Context, imagenPerfil:String){
            var imagen:CircleImageView=itemView.findViewById(R.id.all_user_profile_imagen)
            Picasso.with(contexto).load(imagenPerfil).placeholder(R.drawable.profile).into(imagen)
        }*/
        fun setNombre(nombre:String){
            var miNombre:TextView = itemView.findViewById(R.id.all_user_profile_nombre)
            miNombre.text=nombre
        }
    }
}