package com.maxclub.android.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class TimePickerFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance().apply {
            time = date
        }
        val initialYear = calendar[Calendar.YEAR]
        val initialMonth = calendar[Calendar.MONTH]
        val initialDay = calendar[Calendar.DAY_OF_MONTH]
        val initialHour = calendar[Calendar.HOUR_OF_DAY]
        val initialMinute = calendar[Calendar.MINUTE]
        val is24HourView = DateFormat.is24HourFormat(requireContext())

        val timeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val resultDate: Date =
                GregorianCalendar(initialYear, initialMonth, initialDay, hour, minute).time
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(KEY_RESULT_DATE to resultDate)
            )
        }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHour,
            initialMinute,
            is24HourView
        )
    }

    companion object {
        const val REQUEST_KEY = "com.maxclub.android.criminalintent.TimePickerFragment"
        const val KEY_RESULT_DATE = "date"

        fun newInstance(date: Date): TimePickerFragment =
            TimePickerFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATE, date)
                }
            }
    }
}