package com.example.imageeditor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.imageeditor.databinding.ActivityMainBinding
import com.theartofdev.edmodo.cropper.CropImage
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private var readPermissionsGranted = false

    private var writePermissionsGranted = false

    private val cropActivityResultContract = object: ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity().getIntent(this@MainActivity)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }

    }

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionsGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionsGranted
            writePermissionsGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionsGranted
        }

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContract) { uri ->
             uri?.let {
                 Log.e(TAG, "onCreate: croppedImageUri = $uri", )
                 binding.editedImageView.setImageURI(uri)
             }
        }

        requestOrUpdatePermissions()



        binding.addImageView.setOnClickListener {
            cropActivityResultLauncher.launch(null)
        }

    }

    private fun requestOrUpdatePermissions() {
        val hasReadPermissions = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermissions = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionsGranted = hasReadPermissions
        writePermissionsGranted = hasWritePermissions || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!readPermissionsGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!writePermissionsGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}