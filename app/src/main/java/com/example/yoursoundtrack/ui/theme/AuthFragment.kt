package com.example.yoursoundtrack.ui.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.managers.FirebaseAuthManager
import com.google.android.material.card.MaterialCardView

class AuthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        val layoutWelcome = view.findViewById<LinearLayout>(R.id.layout_welcome)
        val cardLoginPopup = view.findViewById<MaterialCardView>(R.id.card_login_popup)
        val layoutRegister = view.findViewById<LinearLayout>(R.id.layout_register)

        val btnWelcomeLogin = view.findViewById<Button>(R.id.btn_welcome_login)
        val btnWelcomeNewUser = view.findViewById<Button>(R.id.btn_welcome_new_user)
        val btnSubmitLogin = view.findViewById<Button>(R.id.btn_submit_login)
        val btnSubmitRegister = view.findViewById<Button>(R.id.btn_submit_register)

        val loginEmailInput = view.findViewById<EditText>(R.id.et_login_email)
        val loginPasswordInput = view.findViewById<EditText>(R.id.et_login_password)

        val registerNameInput = view.findViewById<EditText>(R.id.et_register_name)
        val registerEmailInput = view.findViewById<EditText>(R.id.et_register_email)
        val registerPasswordInput = view.findViewById<EditText>(R.id.et_register_password)

        btnWelcomeLogin.setOnClickListener {
            layoutWelcome.visibility = View.GONE
            cardLoginPopup.visibility = View.VISIBLE
        }

        btnWelcomeNewUser.setOnClickListener {
            layoutWelcome.visibility = View.GONE
            layoutRegister.visibility = View.VISIBLE
        }

        // --- LOGIN LOGIC ---
        btnSubmitLogin.setOnClickListener {
            val email = loginEmailInput.text.toString().trim()
            val password = loginPasswordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuthManager.loginUser(email, password) { user, error ->
                if (user != null) {
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                    if (isAdded && findNavController().currentDestination?.id == R.id.navigation_auth) {
                        findNavController().navigate(R.id.navigation_home)
                    }
                } else {
                    Toast.makeText(context, "Login Error: $error", Toast.LENGTH_LONG).show()
                }
            }
        }

        // --- REGISTRATION & FIRESTORE PROFILE LOGIC ---
        btnSubmitRegister.setOnClickListener {
            val name = registerNameInput.text.toString().trim()
            val email = registerEmailInput.text.toString().trim()
            val password = registerPasswordInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Calls createAccount passing username, email, and password
            FirebaseAuthManager.createAccount(name, email, password) { user, error ->
                if (user != null) {
                    Toast.makeText(context, "Welcome, $name!", Toast.LENGTH_SHORT).show()
                    if (isAdded && findNavController().currentDestination?.id == R.id.navigation_auth) {
                        findNavController().navigate(R.id.navigation_home)
                    }
                } else {
                    Toast.makeText(context, "Registration Error: $error", Toast.LENGTH_LONG).show()
                }
            }
        }

        return view
    }
}