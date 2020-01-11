package com.trainingapp.personaltrainingassistant.ui.clients

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

/**
 * Fragment called upon from 'Clients' action within the NavigationDrawer. Used to display, edit and delete clients
 */
class ClientsFragment : Fragment(), CoroutineScope {

    private lateinit var databaseOperations: DatabaseOperations
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onAttach(context: Context) {
        super.onAttach(context)
        databaseOperations = DatabaseOperations(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_clients, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()//calls UI coroutine to get ClientsRVAdapter
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    /**
     * UI coroutine to get and set rvClients adapter
     */
    private fun setAdapter() = launch{
        val result = getAdapter()//awaits IO coroutine to get adapter
        rvClients.adapter = result//displays adapter once coroutine has finished
        prgClientsData.visibility = View.GONE//makes progress bar disappear once data received
    }

    /**
     * Suspendable IO coroutine to get a ClientsRVAdapter for rvClients
     */
    private suspend fun getAdapter(): ClientsRVAdapter = withContext(Dispatchers.IO){
        ClientsRVAdapter(databaseOperations.getAllClients(), {clientID -> onItemClick(clientID)}, {client -> onItemLongClick(client) })
    }

    /**
     * Method sent to ClientsRVAdapter to handle item onClick event. Opens a AddEditClientActivity with the id of the client to be edited
     * @param clientID id of the client clicked, from adapter
     */
    private fun onItemClick(clientID: Int){
        val intent = Intent(context, AddEditClientActivity::class.java)
        intent.putExtra("id", clientID)
        startActivity(intent)
    }

    /**
     * Method sent to ClientsRVAdapter to handle item onLongClick event. Opens an AlertDialog to make the user confirm client deletion
     * @param client Client object to be removed from rhe database, from adapter
     * @return always true since the callback consumed the long click (See Android View.onLongClickListener for more info)
     */
    private fun onItemLongClick(client: Client): Boolean {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(getString(R.string.confirm_delete_client, client.name))
        alertDialog.setPositiveButton(R.string.confirm) { _, _ -> databaseOperations.deleteClient(client); setAdapter()}
        alertDialog.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
        alertDialog.show()
        return true
    }
}