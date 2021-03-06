package itr.ksj.appeducasocial

import com.google.firebase.firestore.Exclude
import java.util.*
import kotlin.collections.ArrayList


class Post(val post:String?=null, val date: Date?=null, val userName:String?=null,val categorias:ArrayList<String>? = arrayListOf(),val likes:ArrayList<String>? = arrayListOf()) {
    @Exclude
    @set : Exclude
    @get : Exclude
    var uid:String?=null

    constructor() : this(null,null,null,null,null)


}