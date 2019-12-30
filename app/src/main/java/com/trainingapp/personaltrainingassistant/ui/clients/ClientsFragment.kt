package com.trainingapp.personaltrainingassistant.ui.clients

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.personaltrainingassistant.objects.Client
import com.trainingapp.personaltrainingassistant.database.DatabaseOperations
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.activities.AddEditClientActivity
import kotlinx.android.synthetic.main.fragment_clients.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ClientsFragment : Fragment(), CoroutineScope {

    private lateinit var databaseOperations: DatabaseOperations
    private lateinit var rvClients: RecyclerView
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onAttach(context: Context) {
        super.onAttach(context)
        databaseOperations =
            DatabaseOperations(
                context
            )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_clients, container, false)
        rvClients = root.findViewById(R.id.rvClients)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    private fun setAdapter() = launch{
        val result = getAdapter()
        rvClients.adapter = result
        prgClientsData.visibility = View.GONE
    }

    private suspend fun getAdapter(): ClientsRVAdapter = withContext(Dispatchers.IO){
        ClientsRVAdapter(databaseOperations.getAllClients(), {clientID -> onItemClick(clientID)}, {client -> onItemLongClick(client) })
    }

    private fun onItemClick(clientID: Int){
        val intent = Intent(context, AddEditClientActivity::class.java)
        intent.putExtra("id", clientID)
        startActivity(intent)
    }

    private fun onItemLongClick(client: Client): Boolean {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(getString(R.string.confirm_delete_client, client.name))
        alertDialog.setPositiveButton(R.string.confirm) { _, _ -> databaseOperations.deleteClient(client); setAdapter()}
        alertDialog.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
        alertDialog.show()
        return true
    }
}