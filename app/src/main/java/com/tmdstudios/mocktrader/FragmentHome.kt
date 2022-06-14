package com.tmdstudios.mocktrader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FragmentHome : Fragment() {
    private lateinit var cvGit: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<Button>(R.id.btStart).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_fragmentHome_to_fragmentTrader)
        }

        cvGit = view.findViewById(R.id.cvGit)
        cvGit.setOnClickListener {
            val uri = Uri.parse("https://github.com/TMDStudios/MockTrader")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        return view
    }
}