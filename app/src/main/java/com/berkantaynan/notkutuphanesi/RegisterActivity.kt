package com.berkantaynan.notkutuphanesi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    // Firebase Auth (Kimlik Doğrulama) nesnemizi tanımlıyoruz
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Firebase'i sayfa açıldığında başlatıyoruz
        auth = FirebaseAuth.getInstance()

        // Ekrandaki kutuları ve butonları kodumuza bağlıyoruz
        val etName = findViewById<EditText>(R.id.etRegisterName)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        // KAYIT OL butonuna tıklandığında çalışacak kodlar
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val name = etName.text.toString().trim()

            // 1. KONTROL: Kutular boş mu?
            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Boşsa işlemi burada kes
            }

            // 2. KONTROL: Şifre en az 6 karakter mi? (Firebase'in kuralı)
            if (password.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalıdır!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Her şey tamamsa Firebase'e Kayıt İsteği gönderiyoruz
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // İşlem Başarılıysa adama mesaj gösterip Giriş ekranına yolluyoruz
                        Toast.makeText(this, "Kayıt Başarılı! Giriş yapabilirsiniz.", Toast.LENGTH_LONG).show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Kayıt ekranını kapat
                    } else {
                        // Hata varsa (örn. e-posta zaten kayıtlıysa) hatayı göster
                        Toast.makeText(this, "Hata: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // "Zaten hesabın var mı? Giriş Yap" yazısına tıklanınca
        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}