package com.tmdstudios.mocktrader.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import com.tmdstudios.mocktrader.R

class FragmentHome : Fragment() {
    private lateinit var cvGit: CardView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        sharedPreferences = this.requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        view.findViewById<Button>(R.id.btStart).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_fragmentHome_to_fragmentTrader)
        }

        view.findViewById<Button>(R.id.btReset).setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Reset the game?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                        with(sharedPreferences.edit()) {
                            putBoolean("reset", true)
                            apply()
                        }
                        Navigation.findNavController(view).navigate(R.id.action_fragmentHome_to_fragmentTrader)
                    })
                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                        Navigation.findNavController(view).navigate(R.id.action_fragmentHome_to_fragmentTrader)
                    })
            builder.create().show()
        }

        cvGit = view.findViewById(R.id.cvGit)
        cvGit.setOnClickListener {
            val uri = Uri.parse("https://github.com/TMDStudios/MockTrader")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        view.findViewById<TextView>(R.id.tvMoreFromTMD).setOnClickListener {
            val uri = Uri.parse("https://tmdstudios.wordpress.com")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        return view
    }
}