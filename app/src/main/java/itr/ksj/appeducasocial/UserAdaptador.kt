package itr.ksj.appeducasocial

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdaptador(private val activity: Activity, private val dataset: List<Users>) : RecyclerView.Adapter<UserAdaptador.ViewHolder>() {
    class ViewHolder(val layout: View) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.usuarios_lista_layout,parent,false)

        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = dataset[position]
        Log.i("GAG",user.nombre!!)
        holder.layout.findViewById<TextView>(R.id.all_user_profile_nombre).text = user.nombre

        Picasso.with(activity).load(user.imagenPerfil).placeholder(R.drawable.profile).into(holder.layout.findViewById<CircleImageView>(R.id.all_user_profile_imagen))

    }
    override fun getItemCount()=dataset.size
}