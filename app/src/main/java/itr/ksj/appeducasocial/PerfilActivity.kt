package itr.ksj.appeducasocial

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import itr.ksj.appeducasocial.databinding.ActivityPerfilBinding


class PerfilActivity : AppCompatActivity() {
    private val autentificador = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val enlace: ActivityPerfilBinding by lazy {
        ActivityPerfilBinding.inflate(layoutInflater)
    }

    lateinit var userProfileImageRef: StorageReference
    lateinit var imagenPerfil: CircleImageView
    var amigos: MutableList<String>? = null
    var sonAmigo=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(enlace.root)
        val intent = this.intent
        val extra = intent.extras



        val nombreUsuario = extra!!.getString("usuario")
        var uidPerfil=""

        if(autentificador.currentUser!!.displayName!!.equals(nombreUsuario)){
            enlace.followBtn.visibility=View.INVISIBLE
        }

        userProfileImageRef= FirebaseStorage.getInstance().reference.child("profile Images")
        imagenPerfil=enlace.imagenUsuarioPerfil

        db.collection("Users").addSnapshotListener { value, error ->
            val Users = value!!.toObjects(Users::class.java)

            Users.forEachIndexed { index, user ->
                if(user.userId==autentificador.uid){
                    amigos=user.amigos?.toMutableList()

                }
            }
        }

        db.collection("Users").addSnapshotListener { value, error ->
            val Users = value!!.toObjects(Users::class.java)

            Users.forEachIndexed { index, user ->
                if(user.nombre==nombreUsuario) {
                    sonAmigo=amigos!!.contains(user.userId)
                    if (sonAmigo){
                        setButtonUnfollow()
                    }else{
                        setButtonFollow()
                    }
                    uidPerfil=user.userId!!
                    if (user.imagenPerfil != null) {
                        Picasso.with(this).load(user.imagenPerfil).placeholder(R.drawable.profile)
                            .into(imagenPerfil)
                    }
                }
            }
        }
        enlace.followBtn.setOnClickListener {
            sonAmigo=!sonAmigo

            if(sonAmigo) amigos!!.add(uidPerfil)
            else amigos!!.remove(uidPerfil)

            db.collection("Users").addSnapshotListener { value, error ->
                val Users = value!!.toObjects(Users::class.java)
                val document=value!!.documents
                Users.forEachIndexed { index, user ->
                    if(user.userId==autentificador.uid){
                        val documentUsers=document[index].reference
                            .update("amigos",amigos)
                            .addOnSuccessListener { Toast.makeText(this,"Foto Editada",Toast.LENGTH_SHORT) }
                            .addOnFailureListener {  Toast.makeText(this,"Foto No Editada",Toast.LENGTH_LONG)  }

                    }
                }
            }
        }
        enlace.nombreUsuarioPerfil.setText(nombreUsuario)

        db.collection("posts").orderBy("date",
            Query.Direction.DESCENDING).whereEqualTo("userName",nombreUsuario).addSnapshotListener{ value, error ->
            val posts = value!!.toObjects(Post::class.java)
            //Problema al filtrar desordena los uid y fallan los likes
            //posts.sortByDescending { it.date }

            posts.forEachIndexed { index, post ->
                post.uid = value.documents[index].id
            }

            enlace.rv.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@PerfilActivity)
                adapter = PostAdaptador(this@PerfilActivity,posts)
            }
        }

    }

    fun setButtonUnfollow(){
        enlace.followBtn.setColorFilter(Color.RED)
        enlace.followBtn.setImageResource(R.drawable.ic_baseline_person_remove_24)
    }

    fun setButtonFollow(){
        enlace.followBtn.setColorFilter(Color.parseColor("#19a91f"))
        enlace.followBtn.setImageResource(R.drawable.ic_baseline_add_24)

    }
}