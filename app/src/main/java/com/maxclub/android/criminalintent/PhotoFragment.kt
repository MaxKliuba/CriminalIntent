package com.maxclub.android.criminalintent

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.squareup.picasso.Picasso
import java.io.File

private const val ARG_PHOTO_FILE_PATH = "photoFilePath"

class PhotoFragment : DialogFragment() {
    private lateinit var photoImageView: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val photoFile = arguments?.getString(ARG_PHOTO_FILE_PATH)?.let {
            File(it)
        }

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_photo, null)

        photoImageView = view.findViewById(R.id.photo_image_view) as ImageView
        if (photoFile != null && photoFile.exists()) {
            Picasso.get()
                .load(photoFile)
                .fit()
                .centerCrop()
                .into(photoImageView)
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    companion object {
        fun newInstance(photoFilePath: String): PhotoFragment =
            PhotoFragment().apply {
                arguments = bundleOf(ARG_PHOTO_FILE_PATH to photoFilePath)
            }
    }
}