package itr.ksj.appeducasocial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import itr.ksj.appeducasocial.databinding.ActivityAmigosBinding
import itr.ksj.appeducasocial.databinding.ActivityBuscarUserBinding

class AmigosActivity : AppCompatActivity() {

    private val enlace: ActivityAmigosBinding by lazy {//lazy para inicalizar una variable mas tarde
        ActivityAmigosBinding.inflate(layoutInflater)
    }
    private lateinit var lista: RecyclerView

    private lateinit var allUserDateBase: DatabaseReference

    private val db = FirebaseFirestore.getInstance()
    private val autentificador = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(enlace.root)



        //allUserDateBase = FirebaseDatabase.getInstance().reference.child("Users")
        lista=enlace.listaUser
        lista.setHasFixedSize(true)
        lista.layoutManager= LinearLayoutManager(this)




        db.collection("Users").orderBy("nombre").addSnapshotListener{value,error ->
            val users = value!!.toObjects(Users::class.java)
            val userAux = mutableListOf<Users>()
            var usuarioLogeado= Users()
            users.forEachIndexed { index, user ->
                user.uid = value.documents[index].id
                Log.d("KSJ",user.uid!!)
                if(user.userId==autentificador.uid){
                   usuarioLogeado=user
                }
            }
            users.forEachIndexed { index, user ->
                user.uid = value.documents[index].id

                if(usuarioLogeado.amigos!!.contains(user.userId)){
                    userAux.add(user)
                }
            }
            enlace.listaUser.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@AmigosActivity)
                adapter =UserAdaptador(this@AmigosActivity,userAux)
            }
        }
    }
}