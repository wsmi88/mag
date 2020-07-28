package com.smiatek.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smiatek.myapplication.R
import com.smiatek.myapplication.db.Route
import com.smiatek.myapplication.db.RouteCoordinate
import kotlinx.android.synthetic.main.history_row.view.*
import java.text.SimpleDateFormat

class HistoryRecyclerAdapter(private val myDataset: ArrayList<Route>) :
    RecyclerView.Adapter<HistoryRecyclerAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderView = view.findViewById<TextView>(R.id.order_view)
        val dataView = view.findViewById<TextView>(R.id.data_view)

    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryRecyclerAdapter.MyViewHolder = MyViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.history_row, parent, false)
    )

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        val dateFormat = SimpleDateFormat("dd MMMM 'at 'HH:mm")

        holder.dataView.text = dateFormat.format(myDataset[position].timeStamp)
        holder.orderView.text = "Route ${position + 1}"
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}