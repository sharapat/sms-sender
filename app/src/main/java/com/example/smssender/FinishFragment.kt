package com.example.smssender

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_finish.*

class FinishFragment : Fragment(R.layout.fragment_finish) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnBack.setOnClickListener {
            (requireActivity() as MainActivity).btnSend.visibility = View.VISIBLE
            (requireActivity() as MainActivity).btnImport.visibility = View.VISIBLE
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
    }
}