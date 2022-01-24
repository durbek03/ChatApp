package com.example.chatapp.viewpagerFragments

import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.chatapp.NavOptions
import com.example.chatapp.R
import com.example.chatapp.adapters.GroupRvAdapter
import com.example.chatapp.databinding.FragmentGroupsBinding
import com.example.chatapp.models.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentGroupsBinding
    lateinit var database: FirebaseDatabase
    lateinit var auth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var groupList: ArrayList<Group>
    lateinit var rvAdapter: GroupRvAdapter
    lateinit var groupsRef: DatabaseReference
    lateinit var valueEventListener: ValueEventListener
    lateinit var checkRef: DatabaseReference
    lateinit var checkEventListener: ChildEventListener

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
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_groups, container, false)
        binding = FragmentGroupsBinding.bind(root)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        binding.addGroup.setOnClickListener {
            findNavController().navigate(R.id.addGroupFragment, null, NavOptions.getNavOptions().build())
        }

        rvAdapter = GroupRvAdapter(database, object : GroupRvAdapter.GroupClick {
            override fun onClick(group: Group) {
                findNavController().navigate(R.id.groupChatFragment, Bundle().apply { putSerializable("group", group) }, NavOptions.getNavOptions().build())
            }
        })
        binding.rv.adapter = rvAdapter
        groupList = arrayListOf()
        groupsRef = database.getReference("groups")
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                groupList.clear()
                val groups = snapshot.children
                for (i in groups) {
                    val group = i.getValue(Group::class.java)

                    checkEventListener = object : ChildEventListener {
                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {

                            val id = snapshot.value as String
                            if (group?.uid == id) {
                                var hasGroup = false
                                for (a in groupList) {
                                    if (a.uid == id) {
                                        hasGroup = true
                                        break
                                    }
                                }
                                if (!hasGroup) {
                                    groupList.add(group)
                                    binding.rv.adapter = rvAdapter
                                    rvAdapter.submitList(groupList)
                                    hasGroup = false
                                }
                            }
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                            val id = snapshot.value as String
                            val removeList = arrayListOf<Group>()
                            for (a in groupList) {
                                if (a.uid == id) {
                                    removeList.add(a)
                                    break
                                }
                            }
                            groupList.removeAll(removeList.toSet())
                            binding.rv.adapter = rvAdapter
                            rvAdapter.submitList(groupList)
                        }

                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    }
                    checkRef = database.getReference("users/${currentUser.uid}/groups")
                    checkRef.addChildEventListener(checkEventListener)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        groupsRef.addValueEventListener(valueEventListener)
        return root
    }

    override fun onDestroyView() {
        groupsRef.removeEventListener(valueEventListener)
        checkRef.removeEventListener(checkEventListener)
        super.onDestroyView()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}