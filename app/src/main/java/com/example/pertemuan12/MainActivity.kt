package com.example.pertemuan12

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract.Helpers.insert
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.room.Update
import com.example.pertemuan12.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var mNoteDao : NoteDao
    private lateinit var executerService : ExecutorService
    private var updateId : Int = 0
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executerService = Executors.newSingleThreadExecutor()
        val db = NoteRoomDatabase.getDatabase(this)
        mNoteDao = db!!.noteDao()!!

        with(binding){
            btnAdd.setOnClickListener{
                insert(Note(title = edtTitle.text.toString(),
                    description = edtDesc.text.toString())
                )
                setEmptyField()
            }

            listView.setOnItemClickListener{
                adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Note
                updateId = item.id
                edtTitle.setText(item.title)
                edtDesc.setText(item.description)
            }
            btnUpdate.setOnClickListener {
                update(Note(
                    id = updateId,
                    title = edtTitle.text.toString(),
                    description = edtDesc.text.toString()
                ))
                updateId = 0
                setEmptyField()
            }
            listView.onItemLongClickListener=
                AdapterView.OnItemLongClickListener{
                    adapterView, view, i, l ->
                    val item = adapterView.adapter.getItem(i) as Note
                    delete(item)
                    true
                }
        }
    }

    private fun setEmptyField(){
        with (binding){
            edtTitle.setText("")
            edtDesc.setText("")
        }
    }

    private fun getAllNotes(){
        mNoteDao.allNotes.observe(this){
            notes ->
            val adapter : ArrayAdapter<Note> =
                ArrayAdapter<Note>(this,
                    android.R.layout.simple_list_item_1,
                    notes)
            binding.listView.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        getAllNotes()
    }

    private fun insert(note: Note) {
        executerService.execute() {
            mNoteDao.insert(note)
        }
    }
    private fun update(note: Note) {
        executerService.execute() {
            mNoteDao.update(note)
        }
    }
    private fun delete(note: Note) {
        executerService.execute() {
            mNoteDao.delete(note)
        }
    }
}