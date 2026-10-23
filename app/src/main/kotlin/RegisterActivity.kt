package com.example.wastrain

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    private var isPassVisible = false
    private var isConfirmPassVisible = false

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
            setPadding(dpToPx(24), dpToPx(28), dpToPx(24), dpToPx(32))
        }

        // Logo Image
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.wastrain_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        root.addView(logo, LinearLayout.LayoutParams(dpToPx(90), dpToPx(90)).apply { bottomMargin = dpToPx(12) })

        // Title
        val tvTitle = TextView(this).apply {
            text = "Buat Akun Baru"
            textSize = 22f
            setTextColor(Color.parseColor("#2F1B14"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        root.addView(tvTitle)

        val tvSub = TextView(this).apply {
            text = "Mulai jelajahi keindahan batik Nusantara"
            textSize = 14f
            setTextColor(Color.parseColor("#927D6E"))
            gravity = Gravity.CENTER
        }
        root.addView(tvSub, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dpToPx(4); bottomMargin = dpToPx(24) })

        // Full Name Field
        root.addView(createLabel("Nama Lengkap"), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(6) })
        etFullName = createEditText("Masukkan nama lengkap", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS)
        root.addView(etFullName, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(14) })

        // Email Field
        root.addView(createLabel("Email"), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(6) })
        etEmail = createEditText("nama@email.com", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        root.addView(etEmail, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(14) })

        // Password Field
        root.addView(createLabel("Password"), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(6) })
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
            hint = "Minimal 8 karakter"
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
                isPassVisible = !isPassVisible
                etPassword.inputType = if (isPassVisible) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                etPassword.setSelection(etPassword.text.length)
            }
        }
        passContainer.addView(btnTogglePass, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(passContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(14) })

        // Confirm Password Field
        root.addView(createLabel("Konfirmasi Password"), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(6) })
        val confirmContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#EFE3D8"))
            }
        }
        etConfirmPassword = EditText(this).apply {
            hint = "Ulangi password"
            textSize = 15f
            setTextColor(Color.parseColor("#35251E"))
            setHintTextColor(Color.parseColor("#A89589"))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(dpToPx(16), dpToPx(14), dpToPx(8), dpToPx(14))
            background = null
        }
        confirmContainer.addView(etConfirmPassword, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val btnToggleConfirm = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_view)
            setColorFilter(Color.parseColor("#70452F"))
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dpToPx(8), dpToPx(8), dpToPx(12), dpToPx(8))
            setOnClickListener {
                isConfirmPassVisible = !isConfirmPassVisible
                etConfirmPassword.inputType = if (isConfirmPassVisible) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                etConfirmPassword.setSelection(etConfirmPassword.text.length)
            }
        }
        confirmContainer.addView(btnToggleConfirm, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(confirmContainer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dpToPx(24) })

        // Register Button
        val btnRegister = Button(this).apply {
            text = "Daftar"
            textSize = 16f
            setTextColor(Color.WHITE)
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#70452F"))
                cornerRadius = dpToPx(12).toFloat()
            }
            setOnClickListener { handleRegister() }
        }
        root.addView(btnRegister, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(50)).apply { bottomMargin = dpToPx(20) })

        // Already Have Account Link
        val loginRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val tvAlready = TextView(this).apply {
            text = "Sudah punya akun? "
            textSize = 14f
            setTextColor(Color.parseColor("#927D6E"))
        }
        loginRow.addView(tvAlready)

        val tvLoginLink = TextView(this).apply {
            text = "Login"
            textSize = 14f
            setTextColor(Color.parseColor("#B76E4D"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener { finish() }
        }
        loginRow.addView(tvLoginLink)

        root.addView(loginRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        scrollView.addView(root)
        setContentView(scrollView)
    }

    private fun createLabel(labelText: String): TextView {
        return TextView(this).apply {
            text = labelText
            textSize = 14f
            setTextColor(Color.parseColor("#35251E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    private fun createEditText(hintText: String, inputTypeVal: Int): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 15f
            setTextColor(Color.parseColor("#35251E"))
            setHintTextColor(Color.parseColor("#A89589"))
            inputType = inputTypeVal
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#EFE3D8"))
            }
        }
    }

    private fun handleRegister() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty()) {
            etFullName.error = "Nama lengkap tidak boleh kosong"
            return
        }
        if (email.isEmpty()) {
            etEmail.error = "Email tidak boleh kosong"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Format email tidak valid"
            return
        }
        if (password.length < 8) {
            etPassword.error = "Password minimal 8 karakter"
            return
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Konfirmasi password tidak cocok"
            return
        }

        if (userDb.isEmailExists(email)) {
            etEmail.error = "Email sudah terdaftar. Silakan login."
            Toast.makeText(this, "Email ini sudah terdaftar", Toast.LENGTH_SHORT).show()
            return
        }

        val success = userDb.registerUser(fullName, email, password)
        if (success) {
            Toast.makeText(this, "Pendaftaran berhasil! Silakan masuk dengan akun baru Anda.", Toast.LENGTH_LONG).show()
            val intent = Intent(this, LoginActivity::class.java).apply {
                putExtra("REGISTERED_EMAIL", email)
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Gagal mendaftar akun. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
        }
    }
}
