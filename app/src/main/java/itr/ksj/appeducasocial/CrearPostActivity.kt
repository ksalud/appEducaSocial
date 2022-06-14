package itr.ksj.appeducasocial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import itr.ksj.appeducasocial.databinding.ActivityCrearPostBinding
import itr.ksj.appeducasocial.databinding.ActivityLogeoBinding
import java.util.*
import kotlin.collections.ArrayList

class CrearPostActivity : AppCompatActivity() {
    private val autentificador = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    private val enlace: ActivityCrearPostBinding by lazy {//lazy para inicalizar una variable mas tarde
        ActivityCrearPostBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(enlace.root)
        var categoria= mutableListOf<String>()
        enlace.btnPublicar.setOnClickListener {
            val postText = enlace.escribirPostText.text.toString()
            if(postText!="") {
                val date = Date()
                val userName = autentificador.currentUser!!.displayName


                val post = Post(postText, date, userName,categoria as ArrayList<String>)

                db.collection("posts").add(post)
                    .addOnSuccessListener {
                        finish()
                    }
                    .addOnFailureListener {
                        Utils.alertaError(this, it.localizedMessage)
                    }
            }
        }
        enlace.addCategoria.setOnClickListener {
            if(categoria.size<3) {
                if (!enlace.categoriaTxt.text.toString().equals("")) {
                    if (!categoria.contains(enlace.categoriaTxt.text.toString())) {
                        categoria.add(enlace.categoriaTxt.text.toString())
                        enlace.categoriaTxt.setText("")
                    }
                    enlace.categoriasAdd.setText("Catergorias: " + categoria.toString())
                }
            }else{
                Utils.alertaError(this,"No se permiten mas de 3 categorias",)
            }
        }
    }
}