package com.smiatek.myapplication.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smiatek.myapplication.R
import kotlinx.android.synthetic.main.history_row.view.*

class HistoryRecyclerAdapter(private val myDataset: Array<String>) :
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
    ): HistoryRecyclerAdapter.MyViewHolder {
        // create a new view
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}