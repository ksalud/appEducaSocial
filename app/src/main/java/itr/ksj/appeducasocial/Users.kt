package itr.ksj.appeducasocial

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Exclude

class Users(val userId:String?=null,val email:String?=null,var imagenPerfil:String?=null,val nombre:String?=null,val pass:String?=null) {
    @Exclude
    @set : Exclude
    @get : Exclude
    var uid:String?=FirebaseAuth.getInstance().uid


    constructor() : this(null,null,null,null,null)
}