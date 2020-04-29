package com.leonardo.drinkslab.ui.login.PermissionsRequest

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.coroutinespermission.PermissionManager
import com.google.android.material.snackbar.Snackbar

import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.util.displayToast
import com.leonardo.drinkslab.util.showSnackbar
import kotlinx.android.synthetic.main.fragment_permissions_request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PermissionsRequestFragment : Fragment() {

    private val TAG: String = "AppDebug"

    companion object {
        const val PERMISSION_CAMERA_CODE = 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_permissions_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionRequest()

    }

    private fun permissionRequest() {
        CoroutineScope(IO).launch {
            val permissionResult = PermissionManager.requestPermissions(
                this@PermissionsRequestFragment,
                PERMISSION_CAMERA_CODE,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            when(permissionResult) {
                is PermissionResult.PermissionGranted -> {
                    navigateToNextActivity()
                }
                is PermissionResult.PermissionDenied -> {
                    withContext(Main){
                        activity!!.displayToast("ES NECESARIO ACEPTAR LOS PERMISOS SOLICITADOS PREVIAMENTE ", "Info")
                    }
                }
                is PermissionResult.PermissionDeniedPermanently -> {
                    withContext(Main){
                        mostrarDialogoConfiguracionManualPermisos("PARA USO DE LA CÁMARA, DIRIJASE A AJUSTES PARA ACEPTAR LOS PERMISOS DE ALMACENAMIENTO Y CÁMARA DE FORMA MANUAL")
                    }
                }
                is PermissionResult.ShowRational -> {
                    container_permissions_request.showSnackbar(
                        "Acceso a la cámara y al almacenamiento requerido",
                        Snackbar.LENGTH_INDEFINITE,
                        "ACEPTAR"
                    ) {
                        permissionRequest()
                    }
                }
            }
        }
    }

    private fun navigateToNextActivity() {
        val action = PermissionsRequestFragmentDirections.actionPermissionsRequestFragmentToQrScannerFragment()
        findNavController().navigate(action)
    }

    private fun mostrarDialogoConfiguracionManualPermisos(mensaje: String) {
        val dialog = MaterialDialog(activity!!)
            .noAutoDismiss()
            .cancelOnTouchOutside(false)
            .title(text = "ATENCIÓN")
            .message(text = mensaje)
            .positiveButton(text = "ACEPTAR"){ dialog ->
                dialog.dismiss()
            }
        dialog.show()
    }

}
