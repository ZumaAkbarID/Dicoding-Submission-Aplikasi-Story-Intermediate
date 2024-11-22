package com.rwa.tellme.view.addnew

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.rwa.tellme.R
import com.rwa.tellme.data.Result
import com.rwa.tellme.databinding.ActivityAddNewStoryBinding
import com.rwa.tellme.di.AuthInjection
import com.rwa.tellme.di.StoryInjection
import com.rwa.tellme.utils.getFileFromUri
import com.rwa.tellme.utils.getImageUri
import com.rwa.tellme.utils.reduceFileImage
import com.rwa.tellme.utils.showAlertDialog
import com.rwa.tellme.utils.showToastMessage
import com.rwa.tellme.view.StoryViewModelFactory
import com.rwa.tellme.view.ViewModelFactory
import com.rwa.tellme.view.main.MainActivity
import com.yalantis.ucrop.UCrop
import java.io.File

class AddNewStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewStoryBinding
    private var currentImageUri: Uri? = null
    private lateinit var addNewStoryViewModel: AddNewStoryViewModel

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                showAlertDialog(
                    this,
                    getString(R.string.alert),
                    getString(R.string.permission_denied)
                )
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        setupViewModel()
        setupGalleryAction()
        setupCameraAction()
        setupUploadAction()
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
            currentImageUri = getImageUri(this)
            launcherIntentCamera.launch(currentImageUri!!)
        }
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

    private fun setupUploadAction() {
        binding.buttonAdd.setOnClickListener {
            if (validateForm().isNotEmpty()) {
                showAlertDialog(this, getString(R.string.alert), validateForm())
                return@setOnClickListener
            }

            val imageFile: File? = getFileFromUri(baseContext, currentImageUri!!)?.reduceFileImage()

            imageFile?.let {
                addNewStoryViewModel.uploadStory(binding.edAddDescription.text.toString(), it)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonAdd.isEnabled = !isLoading
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}