package com.example.kotlinfirebasemessengertut

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

       // val username = intent.getStringExtra(NewMessageActivity.USER_KEY) //iz new mesidza uzimamo vrednost usernamea
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY) //iz newMesidza uzimamo ceo objekat User
        supportActionBar?.title = user.username      //postavlja username kao title

        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        recyclerViewChatLog.adapter = adapter
    }
}

class ChatFromItem: Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout(): Int {//prikazuje dobijene poruke
        return R.layout.chat_from_row
    }
}

class ChatToItem: Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout(): Int {//prikazuje poslate poruke
        return R.layout.chat_to_row
    }
}