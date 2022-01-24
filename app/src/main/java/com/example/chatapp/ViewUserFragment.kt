package com.example.chatapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.FragmentViewUserBinding
import com.example.chatapp.models.User
import com.squareup.picasso.Picasso

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewUserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewUserFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentViewUserBinding
    lateinit var args: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        args = arguments?.getSerializable("user") as User
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_view_user, container, false)
        binding = FragmentViewUserBinding.bind(root)

        binding.avatar.clipToOutline = true
        if (args.profileImg != null) {
            Picasso.get().load(args.profileImg).into(binding.avatar)
        }

        binding.nickname.text = args.nickName
        binding.bio.text = args.bio

        binding.btnText.setOnClickListener {
            findNavController().popBackStack()
            findNavController().popBackStack()
            findNavController().navigate(R.id.userChatFragment, Bundle().apply { putSerializable("user", args) }, NavOptions.getNavOptions().build())
        }

        binding.icReturn.setOnClickListener {
            findNavController().popBackStack()
        }

        return root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewUserFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewUserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}