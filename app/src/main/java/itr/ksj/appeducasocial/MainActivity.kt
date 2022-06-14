package itr.ksj.appeducasocial

//import kotlinx.android.synthetic.main.activity_main.*
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import itr.ksj.appeducasocial.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val autentificador = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    lateinit var userRef:DatabaseReference
    lateinit var userProfileImageRef: StorageReference
    lateinit var imagenPerfil:CircleImageView
    //lateinit var storageTask:StorageTask
    lateinit var resultUri:Uri
    var myUri=""

    lateinit var navigationView: NavigationView
    val enlace:ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(enlace.root)

        //userRef=FirebaseDatabase.getInstance().getReference().child("Users").child(autentificador.currentUser.toString())
        userProfileImageRef= FirebaseStorage.getInstance().reference.child("profile Images")

        navigationView = enlace.panellateral

        val navView =  navigationView.inflateHeaderView(R.layout.navigation_header)
        navView.findViewById<TextView>(R.id.usuarioTxt).text=autentificador.currentUser!!.displayName
        imagenPerfil=navView.findViewById<CircleImageView>(R.id.imagenPerfil)
        db.collection("Users").addSnapshotListener { value, error ->
            val Users = value!!.toObjects(Users::class.java)
            Users.forEachIndexed { index, user ->
                if(user.userId==autentificador.uid) {
                    if (user.imagenPerfil != null) {
                        Picasso.with(this).load(user.imagenPerfil).placeholder(R.drawable.profile)
                            .into(imagenPerfil)
                    }
                }
            }
        }

        //Evento click a la imagen de perfil para poder abrir la galeria del telefono
        navView.findViewById<CircleImageView>(R.id.imagenPerfil).setOnClickListener {
            val galeriaIntent:Intent=Intent()
            galeriaIntent.action = Intent.ACTION_GET_CONTENT
            galeriaIntent.type = "image/*"
            startActivityForResult(galeriaIntent, Gallery_Pick)
        }

        //ordenar por fecha
        desFiltrar()


    enlace.btnBuscar.setOnClickListener {
        filtrarPorCategoria(enlace.buscarCategoriaTxt.text.toString())

    }
    enlace.buscarCategoriaTxt.addTextChangedListener {
        if(it.isNullOrEmpty()){
            desFiltrar()
        }
    }


    enlace.btnAddPost.setOnClickListener {
        val intent = Intent(this,CrearPostActivity::class.java)
        startActivity(intent)
    }

    navigationView.setNavigationItemSelectedListener { item ->
        UserMenuSelector(item)
        false
    }


    }

    private fun UserMenuSelector(item: MenuItem) {
        when(item.itemId){
            R.id.logout_item -> {
                autentificador.signOut()
                finish()
            }
            R.id.amigos_item ->{
                irAVerAmigos()
            }
            R.id.buscar_item ->{
                irABuscarAmigos()
            }
        }
    }

    private fun irAVerAmigos() {
        val intent = Intent(this, AmigosActivity::class.java)
        startActivity(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== Gallery_Pick && resultCode==RESULT_OK && data!=null){
            val ImagenUri: Uri? = data.data
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this)
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            var result = CropImage.getActivityResult(data)

            if(resultCode == RESULT_OK){
                resultUri = result.uri
                imagenPerfil.setImageURI(resultUri)
                subirArchivo()

            }
        }
    }
    private fun irABuscarAmigos(){
        val intento= Intent(this,BuscarUserActivity::class.java)
        startActivity(intento)
    }

    private fun subirArchivo() {
        val dialogo=ProgressDialog(this)
        dialogo.setTitle("Cambiando foto...")
        dialogo.setMessage("Espere mientras la foto se cambia")
        dialogo.show()
        if(resultUri != null){
            var fileRef = userProfileImageRef
                .child(autentificador.currentUser!!.uid+".jpg")
            val uploadTask = fileRef.putFile(resultUri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT)
                    throw UnsupportedOperationException()
                }
                fileRef.downloadUrl
            }.addOnCompleteListener {
                if (it.isSuccessful){
                    val descargaUri = it.result
                    myUri=descargaUri.toString()


                    db.collection("Users").addSnapshotListener { value, error ->
                        val Users = value!!.toObjects(Users::class.java)
                        val document=value!!.documents
                        Users.forEachIndexed { index, user ->
                            if(user.userId==autentificador.uid){
                                //Actualizar la foto de  perfil en la base de datos
                                val documentUsers=document[index].reference
                                    .update("imagenPerfil",myUri)
                                    .addOnSuccessListener { Toast.makeText(this,"Foto Editada",Toast.LENGTH_SHORT) }
                                    .addOnFailureListener {  Toast.makeText(this,"Foto No Editada",Toast.LENGTH_LONG)  }

                            }
                            dialogo.dismiss()
                        }
                    }

                }
            }


        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu,menu)

        return true
    }

    fun filtrarPorCategoria(categoria:String){
        db.collection("posts").whereArrayContains("categorias",categoria).orderBy("date",
            Query.Direction.DESCENDING).addSnapshotListener{ value, error ->
            val posts = value?.toObjects(Post::class.java)
            //Problema al filtrar desordena los uid y fallan los likes
            //posts.sortByDescending { it.date }
            if(!posts.isNullOrEmpty()){
                posts.forEachIndexed { index, post ->
                    post.uid = value.documents[index].id
                }

                enlace.rv.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    adapter = PostAdaptador(this@MainActivity,posts)
                }
            }

        }
    }
    fun desFiltrar(){
        db.collection("posts").orderBy("date",
            Query.Direction.DESCENDING).addSnapshotListener{ value, error ->
            val posts = value!!.toObjects(Post::class.java)
            //Problema al filtrar desordena los uid y fallan los likes
            //posts.sortByDescending { it.date }

            posts.forEachIndexed { index, post ->
                post.uid = value.documents[index].id
            }

            enlace.rv.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = PostAdaptador(this@MainActivity,posts)
            }
        }
    }


    companion object {
        //Variable Intent galleria
        const val Gallery_Pick = 1
    }
}