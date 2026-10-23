package com.example.wastrain

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRemember: CheckBox
    private var isPasswordVisible = false

    private lateinit var userDb: UserDatabaseHelper

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userDb = UserDatabaseHelper(this)

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.parseColor("#FAF3E8"))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(24), dpToPx(32), dpToPx(24), dpToPx(32))
        }

        // Logo Image
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.wastrain_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        root.addView(logo, LinearLayout.LayoutParams(dpToPx(100), dpToPx(100)).apply { bottomMargin = dpToPx(12) })


        // Title
        val tvWelcome = TextView(this).apply {
            text = "Selamat Datang Kembali"
            textSize = 20f
            setTextColor(Color.parseColor("#70452F"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        root.addView(tvWelcome, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(6) })

        val tvSub = TextView(this).apply {
            text = "Lanjutkan perjalanan mengenal wastra Nusantara."
            textSize = 14f
            setTextColor(Color.parseColor("#927D6E"))
            gravity = Gravity.CENTER
        }
        root.addView(tvSub, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(4); bottomMargin = dpToPx(24) })

        // Email Input Box
        val tvEmailLabel = TextView(this).apply {
            text = "Email"
            textSize = 14f
            setTextColor(Color.parseColor("#35251E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        root.addView(tvEmailLabel, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(6) })

        etEmail = EditText(this).apply {
            hint = "nama@email.com"
            textSize = 15f
            setTextColor(Color.parseColor("#35251E"))
            setHintTextColor(Color.parseColor("#A89589"))
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#EFE3D8"))
            }
        }
        root.addView(etEmail, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(16) })

        // Check if prefilled from Registration
        val regEmail = intent.getStringExtra("REGISTERED_EMAIL")
        if (!regEmail.isNullOrEmpty()) {
            etEmail.setText(regEmail)
        }

        // Password Input Box
        val tvPasswordLabel = TextView(this).apply {
            text = "Password"
            textSize = 14f
            setTextColor(Color.parseColor("#35251E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        root.addView(tvPasswordLabel, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(6) })

        val passContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#EFE3D8"))
            }
        }

        etPassword = EditText(this).apply {
            hint = "Masukkan password"
            textSize = 15f
            setTextColor(Color.parseColor("#35251E"))
            setHintTextColor(Color.parseColor("#A89589"))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(dpToPx(16), dpToPx(14), dpToPx(8), dpToPx(14))
            background = null
        }
        passContainer.addView(etPassword, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val btnTogglePass = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_view)
            setColorFilter(Color.parseColor("#70452F"))
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dpToPx(8), dpToPx(8), dpToPx(12), dpToPx(8))
            setOnClickListener {
                isPasswordVisible = !isPasswordVisible
                if (isPasswordVisible) {
                    etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                etPassword.setSelection(etPassword.text.length)
            }
        }
        passContainer.addView(btnTogglePass, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(passContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(12) })

        // Remember & Forgot Password Row
        val optionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        cbRemember = CheckBox(this).apply {
            text = "Ingat Saya"
            textSize = 13f
            setTextColor(Color.parseColor("#70452F"))
            isChecked = true
        }
        optionsRow.addView(cbRemember, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val tvForgot = TextView(this).apply {
            text = "Lupa Password?"
            textSize = 13f
            setTextColor(Color.parseColor("#B76E4D"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                Toast.makeText(this@LoginActivity, "Link pemulihan dikirimkan jika email terdaftar.", Toast.LENGTH_SHORT).show()
            }
        }
        optionsRow.addView(tvForgot)

        root.addView(optionsRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(20) })

        // Login Button
        val btnLogin = Button(this).apply {
            text = "Masuk"
            textSize = 16f
            setTextColor(Color.WHITE)
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#70452F"))
                cornerRadius = dpToPx(12).toFloat()
            }
            setOnClickListener { handleLogin() }
        }
        root.addView(btnLogin, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(50)).apply { bottomMargin = dpToPx(10) })

        // Guest / Direct Access Button
        val btnGuest = Button(this).apply {
            text = "🚀  Masuk Beranda (Mode Tamu)"
            textSize = 14f
            setTextColor(Color.parseColor("#70452F"))
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1E1D2"))
                cornerRadius = dpToPx(12).toFloat()
            }
            setOnClickListener {
                val prefs = getSharedPreferences("WastraIN_Prefs", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putBoolean("IS_LOGGED_IN", true)
                    putBoolean("ONBOARDING_COMPLETED", true)
                    putString("USER_EMAIL", "tamu@wastrain.id")
                    putString("USER_NAME", "Tamu")
                    apply()
                }
                Toast.makeText(this@LoginActivity, "Selamat datang di Beranda WastraIN!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
        root.addView(btnGuest, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(48)).apply { bottomMargin = dpToPx(10) })

        // Google Login Button
        val btnGoogle = Button(this).apply {
            text = "🌐  Masuk dengan Google"
            textSize = 14f
            setTextColor(Color.parseColor("#2F1D18"))
            isAllCaps = false
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#EFE3D8"))
            }
            setOnClickListener {
                Toast.makeText(this@LoginActivity, "Login Google memerlukan Play Services. Gunakan Email atau Mode Tamu.", Toast.LENGTH_LONG).show()
            }
        }
        root.addView(btnGoogle, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(48)).apply { bottomMargin = dpToPx(20) })

        // Register Footer
        val registerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val tvNoAccount = TextView(this).apply {
            text = "Belum punya akun? "
            textSize = 14f
            setTextColor(Color.parseColor("#927D6E"))
        }
        registerRow.addView(tvNoAccount)

        val tvRegister = TextView(this).apply {
            text = "Daftar"
            textSize = 14f
            setTextColor(Color.parseColor("#B76E4D"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }
        registerRow.addView(tvRegister)

        root.addView(registerRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        scrollView.addView(root)
        setContentView(scrollView)
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email tidak boleh kosong"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Format email tidak valid"
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Password tidak boleh kosong"
            return
        }

        val account = userDb.validateLogin(email, password)
        val userName = account?.fullName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

        val prefs = getSharedPreferences("WastraIN_Prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("IS_LOGGED_IN", true)
            putBoolean("ONBOARDING_COMPLETED", true)
            putString("USER_EMAIL", email)
            putString("USER_NAME", userName)
            apply()
        }

        Toast.makeText(this, "Selamat datang kembali, $userName!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
