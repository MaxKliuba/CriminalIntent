package com.maxclub.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val TAG_DIALOG_DATE = "DialogDate"
private const val TAG_DIALOG_TIME = "DialogTime"

class CrimeFragment : Fragment() {
    private lateinit var crime: Crime
    private lateinit var titleField: TextInputEditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var suspectButton: Button
    private lateinit var reportButton: Button
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }

    private val pickContactActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                val contactUri: Uri? = it.data?.data
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = contactUri?.let { uri ->
                    requireActivity().contentResolver
                        .query(uri, queryFields, null, null, null)
                }
                cursor?.use { _cursor ->
                    if (_cursor.count != 0) {
                        _cursor.moveToFirst()
                        val suspect = _cursor.getString(0)
                        crime.suspect = suspect
                        crimeDetailViewModel.saveCrime(crime)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as TextInputEditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        suspectButton = view.findViewById(R.id.crime_suspect_button) as Button
        reportButton = view.findViewById(R.id.crime_report_button) as Button

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner) { crime ->
            crime?.let {
                this.crime = it
                updateUI()
            }
        }

        childFragmentManager.setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            crime.date = result.getSerializable(DatePickerFragment.KEY_RESULT_DATE) as Date
            updateUI()
        }

        childFragmentManager.setFragmentResultListener(
            TimePickerFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            crime.date = result.getSerializable(TimePickerFragment.KEY_RESULT_DATE) as Date
            updateUI()
        }
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                //
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                //
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.childFragmentManager, TAG_DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.childFragmentManager, TAG_DIALOG_TIME)
            }
        }

        solvedCheckBox.apply {
            isChecked = crime.isSolved
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            val packageManager: PackageManager = requireActivity().packageManager
            isEnabled = pickContactIntent.resolveActivity(packageManager) != null

            setOnClickListener {
                pickContactActivityResultLauncher.launch(pickContactIntent)
            }

            setOnLongClickListener {
                if (crime.suspect.isNotBlank()) {
                    crime.suspect = ""
                    crimeDetailViewModel.saveCrime(crime)
                    true
                } else
                    false
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also {
                val chooserIntent = Intent.createChooser(it, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            R.id.delete_crime -> {
                crimeDetailViewModel.deleteCrime(crime)
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.getFormattedDate()
        timeButton.text = crime.getFormattedTime(context)
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        suspectButton.text = if (crime.suspect.isNotBlank()) {
            crime.suspect
        } else {
            getString(R.string.crime_suspect_text)
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(
            R.string.crime_report,
            crime.title,
            crime.getFormattedDateTime(context),
            solvedString,
            suspect
        )

    }

    companion object {
        fun newInstance(crimeId: UUID) =
            CrimeFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CRIME_ID, crimeId)
                }
            }
    }
}