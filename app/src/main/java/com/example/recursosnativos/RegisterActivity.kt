package com.example.recursosnativos

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.recursosnativos.ui.theme.RecursosnativosTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import java.io.File

class RegisterActivity : ComponentActivity() {

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoFile: File
    private var savedImagePath: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            requestLocation({ location ->
                Log.d("RegisterActivity", "Deu certo: $location")
            }, {
                Log.d("RegisterActivity", "N deu certo")
            })
        } else {
            Log.d("RegisterActivity", "N tem permissao")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    savedImagePath = photoFile.absolutePath
                    Log.d("RegisterActivity", "Deu certo: $savedImagePath")
                } else {
                    Log.d("RegisterActivity", "N deu certo")
                }
            }

        setContent {
            RecursosnativosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegisterApp(
                        activity = this@RegisterActivity,
                        cameraLauncher = cameraLauncher,
                        savedImagePath = savedImagePath,
                        fusedLocationClient = fusedLocationClient
                    )
                }
            }
        }
    }

    fun createImageFile(activity: RegisterActivity): File {
        val storageDir: File? = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        ).also {
            photoFile = it
        }
    }

    fun getFileUri(activity: RegisterActivity, file: File): Uri {
        return FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.provider",
            file
        )
    }

    fun requestLocation(onSuccess: (Location?) -> Unit, onFailure: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                onSuccess(location)
            }.addOnFailureListener {
                onFailure()
            }
        }
    }

    @Composable
    fun RegisterApp(
        activity: RegisterActivity,
        cameraLauncher: ActivityResultLauncher<Intent>,
        savedImagePath: String?,
        fusedLocationClient: FusedLocationProviderClient
    ) {
        var emailField by remember { mutableStateOf("") }
        var nameField by remember { mutableStateOf("") }
        var comentarioField by remember { mutableStateOf("") }
        var imagePath by remember { mutableStateOf(savedImagePath) }
        var latitude by remember { mutableStateOf<String?>(null) }
        var longitude by remember { mutableStateOf<String?>(null) }
        val dbHelper = DatabaseHelper(activity)
        dbHelper.resetDatabase()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = emailField,
                onValueChange = { emailField = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nameField,
                onValueChange = { nameField = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = comentarioField,
                onValueChange = { comentarioField = it },
                label = { Text("Comentario") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            latitude?.let { Text(text = "Latitude: $it") }
            longitude?.let { Text(text = "Longitude: $it") }

            Button(
                onClick = {
                    activity.requestLocation(
                        onSuccess = { location ->
                            location?.let {
                                latitude = it.latitude.toString()
                                longitude = it.longitude.toString()
                            }
                        },
                        onFailure = {
                            Log.d("RegisterActivity", "N deu certo")
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Obter Localização")
            }

            imagePath?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        val user = User(
                            email = emailField,
                            name = nameField,
                            comment = comentarioField,
                            imagePath = imagePath
                        )
                        dbHelper.insertUser(user)

                        val allUsers = dbHelper.getAllUsers()
                        for (u in allUsers) {
                            Log.d(
                                "teste caralho",
                                "User ID: ${u.id}, Email: ${u.email}, Name: ${u.name} Comment: ${u.comment}, Image Path: ${u.imagePath}"
                            )
                        }

                        val intent = Intent(activity, MainActivity::class.java)
                        activity.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cadastrar")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        val imageFile = activity.createImageFile(activity)
                        val imageUri = activity.getFileUri(activity, imageFile)

                        imagePath = imageUri.toString()

                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        }
                        cameraLauncher.launch(cameraIntent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abrir Câmera")
                }
            }
        }
    }
}

