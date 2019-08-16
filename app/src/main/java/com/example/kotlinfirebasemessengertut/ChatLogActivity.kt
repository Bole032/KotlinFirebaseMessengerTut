package com.example.kotlinfirebasemessengertut

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.kotlinfirebasemessengertut.LatestMessagesActivity.Companion.currentUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var fromUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerViewChatLog.adapter = adapter

       // val username = intent.getStringExtra(NewMessageActivity.USER_KEY) //iz new mesidza uzimamo vrednost usernamea
        fromUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY) //iz newMesidza uzimamo ceo objekat User
        supportActionBar?.title = fromUser?.username      //postavlja username kao title

        listenForMessages()

        btSendChatLog.setOnClickListener{
            Log.d(TAG, "Attemt to send message...")
            performSendMessage()
        }

    }
    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = fromUser?.uid     //CELA LOGIKA SA TO I FROM JE POMESANA, PREDLAZEM DA SE PROMENI SA CURRENT I OTHER
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) { //ovo kad u bazi primeti novu poruku
               val chatMessage = p0.getValue(ChatMessage::class.java)
                val currentUser = LatestMessagesActivity.currentUser

                //ovaj if dole proverava da li je poruka koja je nadjena u databaseu pripada razgovoru ova dva korisnika, currentUser i fromUser
                if (chatMessage != null ) {
                    Log.d(TAG, chatMessage.text)


                    if(chatMessage.fromId == fromUser!!.uid) {

                        adapter.add(ChatFromItem(chatMessage.text, fromUser!!))     //u recycle view dodaje tudju poruku
                    }
                    else if(chatMessage.fromId == currentUser!!.uid){

                        adapter.add(ChatToItem(chatMessage.text, currentUser!!))       //u recycle view dodaje tvoju poruku
                    }
                }
                recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1) //da nam prikaze najnovije poruke
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage(){
        val text = tbChatLog.text.toString()
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        val fromId = FirebaseAuth.getInstance().uid //ko salje
        val toId = user.uid     //kome salje
        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000 )
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our message: ${reference.key}")
                tbChatLog.text.clear()
                recyclerViewChatLog.scrollToPosition(adapter.itemCount - 1)
            }
        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)
        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

    }
    class ChatMessage(val id: String, val text: String, val fromId: String, val toId:String, val timestamp: Long){
        constructor() : this("", "", "", "",-1)
    }

}


class ChatFromItem(val text:String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tbFromMessage.text = text

        // load the user image in to the imageView
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.iwFromMessage
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {//prikazuje dobijene poruke
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tbToMessage.text = text

        //loaduje image u imageView
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.iwToMessage
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {//prikazuje poslate poruke
        return R.layout.chat_to_row
    }
}