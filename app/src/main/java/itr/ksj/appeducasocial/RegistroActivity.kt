package itr.ksj.appeducasocial

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import itr.ksj.appeducasocial.databinding.ActivityRegistroBinding


class RegistroActivity : AppCompatActivity() {
    private val autentificador = FirebaseAuth.getInstance()
    lateinit var englishSpanishTranslator: Translator
    var mensajeTraducido ="Error"
    private val db = FirebaseFirestore.getInstance()
    //private val UsersRef: DatabaseReference? = null
    lateinit var UsersRef: DatabaseReference
    lateinit var currentUserID: String


    private val enlace: ActivityRegistroBinding by lazy {//lazy para inicalizar una variable mas tarde
        ActivityRegistroBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(enlace.root)

        enlace.registrarBtn.setOnClickListener {
            val email = enlace.emailEditT.text.toString()
            val pass =  enlace.passEditT.text.toString()
            val nombre = enlace.nombreEditT.text.toString()
            if(TextUtils.isEmpty(email)){
                Toast.makeText(this,"El campo de texto Email no puede estar vacío",Toast.LENGTH_SHORT).show()
            }else if(TextUtils.isEmpty(pass)){
                Toast.makeText(this,"El campo de texto Contraseña no puede estar vacío",Toast.LENGTH_SHORT).show()
            }else if(TextUtils.isEmpty(nombre)){
                Toast.makeText(this,"El campo de texto Nombre no puede estar vacío",Toast.LENGTH_SHORT).show()
            }else{
                var barraCarga = ProgressDialog(this)
                barraCarga.setTitle("Creando una nueva cuenta")
                barraCarga.setMessage("Espere un momento mientras se valida todo.")
                barraCarga.show()
                barraCarga.setCanceledOnTouchOutside(true)
                db.collection("Users").whereEqualTo("nombre",nombre).addSnapshotListener { value, error ->
                    val users = value!!.toObjects(Users::class.java)
                    if(!users.isNullOrEmpty()){
                        Utils.alertaError(this,"Este nombre ya esta en uso...")
                        barraCarga.dismiss()
                    } else{
                        autentificador.createUserWithEmailAndPassword(email,pass)
                            .addOnSuccessListener {
                                val perfil = UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre)
                                    .build()

                                it.user!!.updateProfile(perfil)
                                    .addOnSuccessListener {
                                        val user = Users(FirebaseAuth.getInstance().uid,email,null,nombre,pass)

                                        db.collection("Users").add(user)
                                            .addOnSuccessListener {
                                                barraCarga.dismiss()
                                                AlertDialog.Builder(this).apply {
                                                    setTitle("Cuenta creada")
                                                    setMessage("Tu cuenta ha sido registrada correctamente")
                                                    setPositiveButton("Aceptar"){dialog:DialogInterface, _:Int ->
                                                        finish()
                                                    }
                                                    //guardadoBBDDUser()
                                                }.show()
                                            }
                                            .addOnFailureListener {
                                                Utils.alertaError(this,it.localizedMessage)
                                            }

                                    }
                                    .addOnFailureListener {
                                        barraCarga.dismiss()
                                        prepararTraductor(it.localizedMessage)
                                    }
                            }
                            .addOnFailureListener {
                                barraCarga.dismiss()
                                prepararTraductor(it.localizedMessage)
                            }
                    }
                }

            }

        }
    }
    private fun prepararTraductor(mensaje:String){
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
        englishSpanishTranslator = Translation.getClient(options)
        //Descargar modelo
        var conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        englishSpanishTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                //traducirMensaje()
                //englishSpanishTranslator.translate(mensaje)
                englishSpanishTranslator.translate(mensaje).addOnSuccessListener {
                    mensajeTraducido=it
                    Utils.alertaError(this,mensajeTraducido)
                    //ToDo Mirar task java para hacer esperar
                }

            }
            .addOnFailureListener { exception ->
                mensajeTraducido="Error en algun campo de texto"
            }
    }
    /*private fun guardadoBBDDUser(){
        currentUserID = autentificador.getCurrentUser()!!.getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID)
        val email = enlace.emailEditT.text.toString()
        val pass =  enlace.passEditT.text.toString()
        val nombre = enlace.nombreEditT.text.toString()
        val userMap = HashMap<String, String>()
        userMap["email"] = email
        userMap["pass"] = pass
        userMap["nombre"] = nombre
        UsersRef.updateChildren(userMap as Map<String, String>).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this,
                    "your Account is created Successfully.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val message: String = task.exception!!.message!!
                Toast.makeText(this, "Error Occured: $message", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }*/
}