package com.example.kotlinfirebasemessengertut

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerViewChatLog.adapter = adapter

       // val username = intent.getStringExtra(NewMessageActivity.USER_KEY) //iz new mesidza uzimamo vrednost usernamea
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY) //iz newMesidza uzimamo ceo objekat User
        supportActionBar?.title = user.username      //postavlja username kao title

        listenForMessages()

        btSendChatLog.setOnClickListener{
            Log.d(TAG, "Attemt to send message...")
            performSendMessage()
        }

    }
    private fun listenForMessages(){
        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) { //ovo kad u bazi primeti novu poruku
               val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if(chatMessage.fromId != FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(chatMessage.text))     //u recycle view dodaje tudju poruku
                    }
                    else{
                    adapter.add(ChatToItem(chatMessage.text))       //u recycle view dodaje tvoju poruku
                    }
                }
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

        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000 )
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our message: ${reference.key}")
            }

    }
    class ChatMessage(val id: String, val text: String, val fromId: String, val toId:String, val timestamp: Long){
        constructor() : this("", "", "", "",-1)
    }

}


class ChatFromItem(val text:String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tbFromMessage.text = text
    }

    override fun getLayout(): Int {//prikazuje dobijene poruke
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tbToMessage.text = text
    }

    override fun getLayout(): Int {//prikazuje poslate poruke
        return R.layout.chat_to_row
    }
}