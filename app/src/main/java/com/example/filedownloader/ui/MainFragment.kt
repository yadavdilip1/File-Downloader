package com.example.filedownloader.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.filedownloader.data.Constants
import com.example.filedownloader.data.Constants.storagePermission
import com.example.filedownloader.data.MainViewModel
import com.example.filedownloader.data.downloadFile
import com.example.filedownloader.databinding.FragmentMainBinding
import com.example.filedownloader.utils.DownloadResult
import com.example.filedownloader.utils.showPermissionsAlert
import com.example.filedownloader.utils.toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.ktor.client.*
import io.ktor.client.engine.android.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.downloadButton.setOnClickListener {
            checkPermissions()
        }
    }

    private fun checkPermissions() {

        Dexter.withContext(requireContext())
            .withPermissions(*storagePermission)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        when {
                            report.areAllPermissionsGranted() -> {
                                downloadFile()
                            }
                            report.isAnyPermissionPermanentlyDenied -> {
                                requireContext().showPermissionsAlert("")
                            }
                            else -> {
                                requireContext().toast("Required Permissions not granted")
                            }
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener {
                requireContext().toast(it.name)
            }
            .check()
    }

    private fun downloadFile() {

        val ktor = HttpClient(Android)
        viewModel.setDownloading(true)
        CoroutineScope(Dispatchers.IO).launch {

            ktor.downloadFile(Constants.fileURL, requireContext()).collect {

                withContext(Dispatchers.Main) {
                    when (it) {
                        is DownloadResult.Success -> {
                            viewModel.setDownloading(false)
                            binding.progressBar.progress = 0
                            requireContext().toast("File Downloaded to path : ${it.path}")
                        }

                        is DownloadResult.Error -> {
                            viewModel.setDownloading(false)
                            requireContext().toast("Error while downloading file")
                        }

                        is DownloadResult.Progress -> {
                            binding.progressBar.progress = it.progress
                        }
                    }

                }

            }

        }

    }


}
