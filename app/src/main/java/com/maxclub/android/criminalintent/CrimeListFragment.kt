package com.maxclub.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var emptyListStubView: View
    private lateinit var newCrimeButton: Button
    private var adapter: CrimeAdapter? = CrimeAdapter()
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        emptyListStubView = view.findViewById(R.id.empty_list_stub) as View
        newCrimeButton = view.findViewById(R.id.new_crime_button) as Button

        val layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.layoutManager = layoutManager
        crimeRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                layoutManager.orientation
            )
        )
        crimeRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner) { crimes ->
            crimes?.let {
                Log.i(TAG, "Got crimes ${crimes.size}")
                updateUI(crimes)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        newCrimeButton.setOnClickListener {
            createNewCrime()
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.new_crime_button -> {
                createNewCrime()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun updateUI(crimes: List<Crime>) {
        (crimeRecyclerView.adapter as CrimeAdapter).setCrimes(crimes)
        emptyListStubView.visibility = if (crimes.isEmpty()) View.VISIBLE else View.INVISIBLE
    }

    private fun createNewCrime() {
        val crime = Crime()
        crimeListViewModel.addCrime(crime)
        callbacks?.onCrimeSelected(crime.id)
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = crime.title
            dateTextView.text = crime.getFormattedDateTime(context)
            solvedImageView.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
        }

        override fun onClick(view: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeAdapter :
        RecyclerView.Adapter<CrimeHolder>() {
        private var crimes: SortedList<Crime> =
            SortedList(Crime::class.java, object : SortedListAdapterCallback<Crime>(this) {
                override fun compare(item1: Crime, item2: Crime): Int =
                    item2.date.compareTo(item1.date)

                override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean =
                    oldItem.id == newItem.id

                override fun areItemsTheSame(item1: Crime?, item2: Crime?): Boolean = item1 == item2

            })

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

        override fun getItemCount() = crimes.size()

        fun setCrimes(crimes: List<Crime>) {
            this.crimes.replaceAll(crimes)
        }
    }

    companion object {
        fun newInstance() = CrimeListFragment()
    }
}