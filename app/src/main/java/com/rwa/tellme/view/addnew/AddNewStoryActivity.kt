package com.rwa.tellme.view.addnew

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rwa.tellme.R
import com.rwa.tellme.data.Result
import com.rwa.tellme.databinding.ActivityAddNewStoryBinding
import com.rwa.tellme.utils.getFileFromUri
import com.rwa.tellme.utils.getImageUri
import com.rwa.tellme.utils.reduceFileImage
import com.rwa.tellme.utils.showAlertDialog
import com.rwa.tellme.utils.showToastMessage
import com.rwa.tellme.view.StoryViewModelFactory
import com.rwa.tellme.view.main.MainActivity
import com.yalantis.ucrop.UCrop
import java.io.File

class AddNewStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewStoryBinding
    private var currentImageUri: Uri? = null
    private lateinit var addNewStoryViewModel: AddNewStoryViewModel

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupGalleryAction()
        setupCameraAction()
        setupUploadAction()
        getMyLocation()
    }

    private fun setupViewModel() {
        val factory = StoryViewModelFactory.getInstance(this)
        addNewStoryViewModel = ViewModelProvider(this, factory)[AddNewStoryViewModel::class.java]

        addNewStoryViewModel.uploadResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }

                is Result.Success -> {
                    showLoading(false)
                    showToastMessage(baseContext, getString(R.string.upload_story_success))
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }

                is Result.Error -> {
                    showLoading(false)
                    showAlertDialog(this, getString(R.string.failed), result.error)
                }
            }
        }
    }

    private val uCropContract = object : ActivityResultContract<List<Uri>, Uri>() {
        override fun createIntent(context: Context, input: List<Uri>): Intent {
            val inputUri = input[0]
            val outputUri = input[1]

            val uCrop = UCrop.of(inputUri, outputUri).withAspectRatio(16f, 9f)

            return uCrop.getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri {
            return if (resultCode == RESULT_OK && intent != null) {
                UCrop.getOutput(intent)!!
            } else {
                Uri.EMPTY
            }
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                val outputUri = File(filesDir, "croppedImage.jpg").toUri()

                val listUri = listOf<Uri>(uri, outputUri)
                cropImage.launch(listUri)
            } ?: showToastMessage(this, getString(R.string.no_image_picked))
        }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            val outputUri = File(filesDir, "croppedImage.jpg").toUri()
            val listUri = listOf<Uri>(currentImageUri!!, outputUri)
            cropImage.launch(listUri)
        } else {
            showToastMessage(this, getString(R.string.no_image_captured))
        }
    }

    private val cropImage = registerForActivityResult(uCropContract) { uri ->
        if (uri != Uri.EMPTY) {
            currentImageUri = uri
            showImage()
        } else {
            showToastMessage(this, getString(R.string.process_canceled))
        }
    }

    private fun setupGalleryAction() {
        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun setupCameraAction() {
        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    REQUIRED_PERMISSION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                requestPermissionLauncher.launch(REQUIRED_PERMISSION)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                showAlertDialog(
                    this,
                    getString(R.string.alert),
                    getString(R.string.permission_denied)
                )
            }
        }

    private fun openCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private fun validateForm(): String {
        return if (currentImageUri == null) {
            getString(R.string.pick_image)
        } else if (binding.edAddDescription.text.isNullOrEmpty()) {
            getString(R.string.fill_description)
        } else {
            ""
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.ivImage.setImageURI(null)
            binding.ivImage.setImageDrawable(null)
            binding.ivImage.setImageURI(currentImageUri)
        } ?: showAlertDialog(this, getString(R.string.alert), getString(R.string.image_not_found))
    }

    private fun getMyLocation() {
        binding.checkboxAddLocation.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        LOCATION_PERMISSION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            LOCATION_COARSE_PERMISSION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        checkLocationStatus()
                    } else {
                        requestPermissionLauncherForLocation.launch(LOCATION_COARSE_PERMISSION)
                        buttonView.isChecked = false
                    }
                } else {
                    requestPermissionLauncherForLocation.launch(LOCATION_PERMISSION)
                    buttonView.isChecked = false
                }
            }
        }
    }

    private val requestPermissionLauncherForLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkLocationStatus()
            } else {
                showAlertDialog(
                    this,
                    getString(R.string.alert),
                    getString(R.string.permission_denied)
                )
            }
        }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            currentLocation = locationResult.lastLocation
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100L)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(100L)
            .setMaxUpdateDelayMillis(100L)
            .build()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    @SuppressLint("MissingPermission")
    private fun checkLocationStatus() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlertDialog(
                this,
                getString(R.string.turn_on_loc),
                getString(R.string.turn_on_loc_message)
            ) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                currentLocation = task.result
                if(currentLocation == null) {
                    requestNewLocationData()
                }
            }
            binding.checkboxAddLocation.isChecked = true
        }
    }

    private fun setupUploadAction() {
        binding.buttonAdd.setOnClickListener {
            if (validateForm().isNotEmpty()) {
                showAlertDialog(this, getString(R.string.alert), validateForm())
                return@setOnClickListener
            }

            val imageFile: File? = getFileFromUri(baseContext, currentImageUri!!)?.reduceFileImage()

            val lon = currentLocation?.longitude
            val lat = currentLocation?.latitude

            if (binding.checkboxAddLocation.isChecked && (lon == null || lat == null)) {
                showAlertDialog(
                    this,
                    getString(R.string.alert),
                    getString(R.string.location_not_found)
                )
                binding.checkboxAddLocation.isChecked = false
                return@setOnClickListener
            }

            imageFile?.let {
                addNewStoryViewModel.uploadStory(
                    binding.edAddDescription.text.toString(),
                    it,
                    lat,
                    lon
                )
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonAdd.isEnabled = !isLoading
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
        private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val LOCATION_COARSE_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    }
}