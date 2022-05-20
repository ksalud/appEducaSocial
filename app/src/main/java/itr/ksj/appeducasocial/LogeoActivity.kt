package itr.ksj.appeducasocial

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import itr.ksj.appeducasocial.databinding.ActivityLogeoBinding


class LogeoActivity : AppCompatActivity() {

    private val autentificador = FirebaseAuth.getInstance()
    lateinit var englishSpanishTranslator: Translator
    var mensajeTraducido ="Error"


    private val enlace: ActivityLogeoBinding by lazy {//lazy para inicalizar una variable mas tarde
        ActivityLogeoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(enlace.root)
        //val englishSpanishTranslator = Translation.getClient(options)
        if(autentificador.currentUser!=null){
            irAlMain()
        }

        enlace.loginBtn.setOnClickListener {
            val email = enlace.emailEditT.text.toString()
            val pass = enlace.passEditT.text.toString()


            autentificador.signInWithEmailAndPassword(email,pass)
                .addOnSuccessListener {
                    irAlMain()
                }
                .addOnFailureListener {
                    // Create an English-Spanish translator:
                    prepararTraductor(it.localizedMessage)

                }

        }
        enlace.newCuentaBtn.setOnClickListener {
            val intento= Intent(this,RegistroActivity::class.java)
            startActivity(intento)
        }

    }

    /*private fun CheckExistenciaUsuario()
    {
        val current_user_id = autentificador.currentUser!!.uid

        userRef.addValueEventListener( object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (!dataSnapshot?.hasChild(current_user_id)!!) {
                    SendUserToSetupActivity()
                }

            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        })
    }*/


    private fun irAlMain(){
        val intento = Intent(this,MainActivity::class.java)
        startActivity(intento)
        finish()
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

    /*private fun traducirMensaje() {
        englishSpanishTranslator.translate()
    }*/

}