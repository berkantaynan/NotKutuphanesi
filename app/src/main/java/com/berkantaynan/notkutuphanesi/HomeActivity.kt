package com.berkantaynan.notkutuphanesi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        // FOTOĞRAFTA GÖRDÜĞÜM SENİN GERÇEK LİNKİNİ BURAYA YAZDIM:
        val databaseUrl = "https://notkutuphanesi-1ec2b-default-rtdb.firebaseio.com/"

        try {
            database = FirebaseDatabase.getInstance(databaseUrl).reference
        } catch (e: Exception) {
            Toast.makeText(this, "Bağlantı Hatası: ${e.message}", Toast.LENGTH_LONG).show()
        }

        val spinnerCourses = findViewById<Spinner>(R.id.spinnerCourses)
        val etNewNote = findViewById<EditText>(R.id.etNewNote)
        val etNoteLink = findViewById<EditText>(R.id.etNoteLink)
        val btnAddNote = findViewById<Button>(R.id.btnAddNote)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val listViewNotes = findViewById<ListView>(R.id.listViewNotes)

        val dersler = arrayOf(
            "Atatürk İlkeleri ve İnkılap Tarihi II",
            "Toplumsal Fayda için Programlama",
            "Girişimcilik Uygulamaları",
            "Temel İngilizce II",
            "Meslek Projesi",
            "Meslek Etiği",
            "Türk Dili II"
        )

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dersler)
        spinnerCourses.adapter = spinnerAdapter

        val notListesi = ArrayList<String>()
        val linkListesi = ArrayList<String>()
        val listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notListesi)
        listViewNotes.adapter = listAdapter

        spinnerCourses.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val secilenDers = dersler[position]

                database.child("DersNotlari").child(secilenDers).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        notListesi.clear()
                        linkListesi.clear()
                        for (notSnapshot in snapshot.children) {
                            val not = notSnapshot.child("not").value?.toString() ?: ""
                            val yazar = notSnapshot.child("email").value?.toString() ?: ""
                            val gelenLink = notSnapshot.child("link").value?.toString() ?: ""

                            notListesi.add("$yazar:\n$not")
                            linkListesi.add(gelenLink)
                        }
                        listAdapter.notifyDataSetChanged()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@HomeActivity, "Hata: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        listViewNotes.setOnItemClickListener { _, _, position, _ ->
            val tiklananLink = linkListesi[position]
            if (tiklananLink.isNotEmpty() && tiklananLink.startsWith("http")) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tiklananLink))
                startActivity(browserIntent)
            } else {
                Toast.makeText(this, "Bu notun geçerli bir linki yok.", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnAddNote.setOnClickListener {
            val secilenDers = spinnerCourses.selectedItem.toString()
            val notIcerigi = etNewNote.text.toString().trim()
            val linkIcerigi = etNoteLink.text.toString().trim()
            val kullaniciEmail = auth.currentUser?.email ?: "Anonim"

            if (notIcerigi.isEmpty()) {
                Toast.makeText(this, "Lütfen bir açıklama yazın!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val noteId = database.child("DersNotlari").child(secilenDers).push().key
            val notBilgisi = HashMap<String, Any>()
            notBilgisi["email"] = kullaniciEmail
            notBilgisi["not"] = notIcerigi
            notBilgisi["link"] = linkIcerigi

            if (noteId != null) {
                database.child("DersNotlari").child(secilenDers).child(noteId).setValue(notBilgisi)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Başarıyla Paylaşıldı!", Toast.LENGTH_SHORT).show()
                            etNewNote.text.clear()
                            etNoteLink.text.clear()
                        }
                    }
            }
        }
    }
}