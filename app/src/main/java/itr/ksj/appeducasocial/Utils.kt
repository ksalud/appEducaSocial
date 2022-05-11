package itr.ksj.appeducasocial

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.security.AccessControlContext

object Utils {
    fun alertaError(context: Context,mensaje:String){
        AlertDialog.Builder(context).apply {
            setTitle("Error")
            setMessage(mensaje)
            setPositiveButton("Aceptar",null)
        }.show()
    }
}