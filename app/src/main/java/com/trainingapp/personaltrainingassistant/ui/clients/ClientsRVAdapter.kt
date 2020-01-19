package com.trainingapp.personaltrainingassistant.ui.clients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trainingapp.personaltrainingassistant.objects.Client
import com.trainingapp.personaltrainingassistant.R
import com.trainingapp.personaltrainingassistant.enumerators.ScheduleType

/**
 * Adapter to display all the clients found in the database. Displays all pertinent information about a client
 * @param clients List of all the clients as Client objects
 * @param clickListener Function used by ClientsFragment to handle item onClick event (ie edit client)
 * @param longClickListener Function used by ClientsFragment to handle item onLonClick event (ie delete client)
 */
class ClientsRVAdapter(private val clients: ArrayList<Client>, private val clickListener: (Int) -> Unit, private val longClickListener: (Client) -> Boolean): RecyclerView.Adapter<ClientsRVAdapter.ClientsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientsViewHolder {
        return ClientsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.client_row, parent, false))
    }

    override fun getItemCount(): Int = clients.size

    override fun onBindViewHolder(vh: ClientsViewHolder, position: Int) {
        vh.onBindItems(clients[position], clickListener, longClickListener)
    }

    inner class ClientsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun onBindItems(client: Client, clickListener: (Int) -> Unit, longClickListener: (Client) -> Boolean){
            itemView.findViewById<TextView>(R.id.txtClientsName).text = client.name
            itemView.findViewById<TextView>(R.id.txtClientsSessionType).text = client.getStrSessionType()
            //if client has constant/variable session show start and end date, if no schedule blank
            val strStartEndDate = if (client.startDate != "0") "${client.startDate}  -  ${client.endDate}" else ""
            itemView.findViewById<TextView>(R.id.txtClientsStartEndDate).text = strStartEndDate
            when (client.scheduleType) {//fill days and times depending upon ScheduleType
                ScheduleType.NO_SCHEDULE -> {
                    itemView.findViewById<TextView>(R.id.txtClientsDays).text = ""
                    itemView.findViewById<TextView>(R.id.txtClientsTimes).text = ""
                }
                ScheduleType.WEEKLY_CONSTANT -> {
                    itemView.findViewById<TextView>(R.id.txtClientsDays).text = client.getDaysString(false)
                    itemView.findViewById<TextView>(R.id.txtClientsTimes).text = client.getTimesString(false)
                }
                ScheduleType.WEEKLY_VARIABLE -> {
                    val strDays = "${client.days[0]}/week"
                    itemView.findViewById<TextView>(R.id.txtClientsDays).text = strDays
                    itemView.findViewById<TextView>(R.id.txtClientsTimes).text = ""
                }
                ScheduleType.MONTHLY_VARIABLE -> {
                    val strDays = "${client.days[0]}/month"
                    itemView.findViewById<TextView>(R.id.txtClientsDays).text = strDays
                    itemView.findViewById<TextView>(R.id.txtClientsTimes).text = ""
                }
                ScheduleType.BLANK -> {
                    itemView.findViewById<TextView>(R.id.txtClientsDays).text = ""
                    itemView.findViewById<TextView>(R.id.txtClientsTimes).text = ""
                }
            }
            itemView.setOnClickListener { clickListener(client.id) }
            itemView.setOnLongClickListener { longClickListener(client) }
        }
    }
}