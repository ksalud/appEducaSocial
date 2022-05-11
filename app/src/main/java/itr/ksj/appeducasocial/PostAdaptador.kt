package itr.ksj.appeducasocial

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat


class PostAdaptador(private val activity:Activity,private val dataset: List<Post>) : RecyclerView.Adapter<PostAdaptador.ViewHolder>() {
    class ViewHolder(val layout: View) : RecyclerView.ViewHolder(layout)

    private val autentificador = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.card_post,parent,false)

        return ViewHolder(layout)
    }

    override fun getItemCount() = dataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        //dataset.sortedByDescending { it.date }
        val post = dataset[position]


        val likes = post.likes!!.toMutableList()
        var liked = likes.contains(autentificador.uid)

        holder.layout.findViewById<TextView>(R.id.contadorLike_textV).text="${likes.size} likes"
        holder.layout.findViewById<TextView>(R.id.nombreUsuario_txtV).text=post.userName
        holder.layout.findViewById<TextView>(R.id.textoPost_txtV).text=post.post
        holder.layout.findViewById<TextView>(R.id.textoPost_txtV).setMovementMethod(
            LinkMovementMethod.getInstance())

        val dateFormat = SimpleDateFormat("dd/M/yyyy hh:mm a")

        holder.layout.findViewById<TextView>(R.id.fecha_txtV).text=dateFormat.format(post.date)

        //Cambiar el color del boton si estaa pulsado o no
        setColor(liked,holder.layout.findViewById(R.id.btnLike))

        holder.layout.findViewById<TextView>(R.id.nombreUsuario_txtV).setOnClickListener {
            val intent = Intent(activity, PerfilActivity::class.java)
            intent.putExtra("usuario", holder.layout.findViewById<TextView>(R.id.nombreUsuario_txtV).text)
            activity.startActivity(intent)
        }
        holder.layout.findViewById<Button>(R.id.btnLike).setOnClickListener {
            liked = !liked
            setColor(liked,holder.layout.findViewById(R.id.btnLike))

            if(liked) likes.add(autentificador.uid!!)
            else likes.remove(autentificador.uid)


            val doc = db.collection("posts").document(post.uid!!)
            //Sicroniza los likes con una transación para que funcione bien
            db.runTransaction{
                it.update(doc,"likes",likes)
                null
            }
        }

        holder.layout.findViewById<Button>(R.id.btnCompartir).setOnClickListener {

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT,post.post)
                type ="text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent,null)
            activity.startActivity(shareIntent)
        }
        if(post.userName == autentificador.currentUser!!.displayName){
            holder.layout.findViewById<ImageButton>(R.id.btnBorrar).visibility=View.VISIBLE
            holder.layout.findViewById<ImageButton>(R.id.btnBorrar).setOnClickListener {
                AlertDialog.Builder(activity).apply {
                    setTitle("Borrar Post")
                    setMessage("Estas seguro que quieres borrar tu post")
                    setPositiveButton("Aceptar"){ dialog: DialogInterface, _:Int ->
                        db.collection("posts").document(post.uid!!).delete()
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener {
                                Utils.alertaError(activity,it.localizedMessage)
                            }
                    }
                    setNegativeButton("Cancelar",null)
                }.show()

                return@setOnClickListener
            }
        }

    }
    private fun setColor(liked:Boolean,likeButton:Button){
        if(liked) likeButton.setTextColor(ContextCompat.getColor(activity,R.color.teal_200))
        else likeButton.setTextColor(Color.BLACK)
    }



}