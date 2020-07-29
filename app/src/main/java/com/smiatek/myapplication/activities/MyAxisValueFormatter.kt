package com.smiatek.myapplication.activities

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat

class MyAxisValueFormatter : IndexAxisValueFormatter() {
    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        //axis?.setLabelCount(5, true)
        val dateFormat = SimpleDateFormat("HH:mm:ss")
        return dateFormat.format(value)
    }

}