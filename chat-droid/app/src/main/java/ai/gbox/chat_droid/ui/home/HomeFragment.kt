package ai.gbox.chat_droid.ui.home

import ai.gbox.chat_droid.R
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ai.gbox.chat_droid.databinding.FragmentHomeBinding

class HomeFragment: Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatListViewModel
    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        adapter = ChatListAdapter(emptyList()) { chat ->
            // TODO: navigate to chat details
        }
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.chatRecyclerView.adapter = adapter

        val application = requireActivity().application
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[ChatListViewModel::class.java]

        val prefs = context.getSharedPreferences("open_webui_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (!token.isNullOrEmpty()) {
            viewModel.loadChats(token)
        }

        viewModel.chatList.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}