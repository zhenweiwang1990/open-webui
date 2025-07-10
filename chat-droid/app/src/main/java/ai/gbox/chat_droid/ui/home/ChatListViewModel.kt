package ai.gbox.chat_droid.ui.home

import ai.gbox.chat_droid.network.ChatTitle
import ai.gbox.chat_droid.repository.ChatRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    private val chatRepository = ChatRepository()

    private val _chatList = MutableLiveData<List<ChatTitle>>()
    val chatList: LiveData<List<ChatTitle>> get() = _chatList

    fun loadChats(token: String) {
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    chatRepository.getChatList(token)
                }
                _chatList.value = list
            } catch (e: Exception) {
                e.printStackTrace()
                _chatList.value = emptyList()
            }
        }
    }
} 