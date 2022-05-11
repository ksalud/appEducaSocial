package itr.ksj.appeducasocial

import android.net.Uri
import android.os.Bundle
import android.view.View
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(enlace.root)
        val intent = this.intent
        val extra = intent.extras

        val nombreUsuario = extra!!.getString("usuario")
        if(autentificador.currentUser!!.displayName!!.equals(nombreUsuario)){
            enlace.followBtn.visibility=View.INVISIBLE
        }
        userProfileImageRef= FirebaseStorage.getInstance().reference.child("profile Images")
        imagenPerfil=enlace.imagenUsuarioPerfil
        db.collection("Users").addSnapshotListener { value, error ->
            val Users = value!!.toObjects(Users::class.java)
            Users.forEachIndexed { index, user ->
                if(user.nombre==nombreUsuario) {
                    if (user.imagenPerfil != null) {
                        Picasso.with(this).load(user.imagenPerfil).placeholder(R.drawable.profile)
                            .into(imagenPerfil)
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
}