package com.leonardo.drinkslab.ui.login.QRScanner

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.coroutinespermission.PermissionManager
import com.google.android.material.snackbar.Snackbar

import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.util.QRDecode
import com.leonardo.drinkslab.util.displayToast
import com.leonardo.drinkslab.util.showSnackbar
import kotlinx.android.synthetic.main.fragment_qr_scanner.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrScannerFragment : Fragment() {

    private val TAG: String = "AppDebug"
    private lateinit var codeScanner: CodeScanner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qr_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val activity = requireActivity()

        codeScanner = CodeScanner(activity, scannerView)

        scanQR(scannerView, activity)
    }

    private fun scanQR(scannerView: CodeScannerView?, activity: FragmentActivity) {
        scannerView?.let {
            codeScanner.decodeCallback = DecodeCallback {
                activity.runOnUiThread {
                    //val qrDecoded = QRDecode.decode(it.text)
                    val machineId: String? = it.text.toString()

                    Log.d(TAG, "QRScaanerFragment, MACHINE ID: $machineId")

                    machineId?.let {
                        val action = QrScannerFragmentDirections.actionQrScannerFragmentToPasswordInputFragment(it)
                        findNavController().navigate(action)
                    }
                }
            }
            scannerView.setOnClickListener {
                codeScanner.startPreview()
            }
        }?: run {
            activity!!.displayToast("Error", "Error")
        }
    }



    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}
