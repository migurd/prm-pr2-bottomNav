package com.example.appmenubutton

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.appmenubutton.database.Alumno
import com.example.appmenubutton.database.dbAlumnos

private const val ARG_ALUMNO = "alumno"
private const val PICK_IMAGE_REQUEST = 1
private const val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 2

class DbFragment : Fragment() {

    private var alumno: Alumno? = null
    private lateinit var db: dbAlumnos
    private lateinit var btnGuardar: Button
    private lateinit var btnBuscar: Button
    private lateinit var btnBorrar: Button
    private lateinit var btnSubirFoto: Button
    private lateinit var inMatricula: EditText
    private lateinit var inNombre: EditText
    private lateinit var inDomicilio: EditText
    private lateinit var inEspecialidad: EditText
    private lateinit var imgAlumno: ImageView
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            alumno = it.getParcelable(ARG_ALUMNO)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_db, container, false)

        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnBuscar = view.findViewById(R.id.btnBuscar)
        btnBorrar = view.findViewById(R.id.btnBorrar)
        btnSubirFoto = view.findViewById(R.id.btnSubirFoto)
        inMatricula = view.findViewById(R.id.inMatricula)
        inNombre = view.findViewById(R.id.inNombre)
        inDomicilio = view.findViewById(R.id.inDomicilio)
        inEspecialidad = view.findViewById(R.id.inEspecialidad)
        imgAlumno = view.findViewById(R.id.imgAlumno)

        alumno?.let {
            populateFields(it)
            inMatricula.isEnabled = false
            btnBorrar.isEnabled = true
        } ?: run {
            btnBorrar.isEnabled = false
        }

        btnSubirFoto.setOnClickListener {
            handlePhotoUpload()
        }

        btnGuardar.setOnClickListener {
            saveOrUpdateAlumno()
        }

        btnBuscar.setOnClickListener {
            searchAlumno()
        }

        btnBorrar.setOnClickListener {
            deleteAlumno()
        }

        return view
    }

    private fun populateFields(alumno: Alumno) {
        inMatricula.setText(alumno.matricula)
        inNombre.setText(alumno.nombre)
        inDomicilio.setText(alumno.domicilio)
        inEspecialidad.setText(alumno.especialidad)
        if (!alumno.foto.isNullOrEmpty()) {
            Glide.with(this)
                .load(alumno.foto)
                .placeholder(R.mipmap.foto)
                .error(R.mipmap.foto)
                .into(imgAlumno)
        } else {
            imgAlumno.setImageResource(R.mipmap.foto)
        }
    }

    private fun handlePhotoUpload() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_READ_EXTERNAL_STORAGE
            )
        } else {
            openImageChooser()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.mipmap.foto)
                .error(R.mipmap.foto)
                .into(imgAlumno)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openImageChooser()
            } else {
                Toast.makeText(requireContext(), "Permiso denegado para acceder a archivos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveOrUpdateAlumno() {
        if (inNombre.text.toString().isEmpty() ||
            inDomicilio.text.toString().isEmpty() ||
            inMatricula.text.toString().isEmpty() ||
            inEspecialidad.text.toString().isEmpty()
        ) {
            Toast.makeText(requireContext(), "Faltó información por capturar", Toast.LENGTH_SHORT).show()
        } else {
            db = dbAlumnos(requireContext())
            db.openDatabase()

            val isRegistered = db.matriculaExiste(inMatricula.text.toString())
            val currentAlumno = if (isRegistered) db.getAlumno(inMatricula.text.toString()) else null

            val newImageUrl = imageUri?.toString()
            val updatedImageUrl = newImageUrl ?: currentAlumno?.foto

            val alumno = Alumno(
                nombre = inNombre.text.toString(),
                matricula = inMatricula.text.toString(),
                domicilio = inDomicilio.text.toString(),
                especialidad = inEspecialidad.text.toString(),
                foto = updatedImageUrl
            )

            val msg: String
            if (isRegistered) {
                db.actualizarAlumno(alumno, alumno.matricula)
                msg = "Estudiante con matrícula ${alumno.matricula} se actualizó."
            } else {
                db.insertarAlumno(alumno)
                msg = "Se agregó con éxito."
                clearFields()
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            db.close()

            // Close the fragment if an Alumno object was passed
            if (arguments?.getParcelable<Alumno>(ARG_ALUMNO) != null) {
                parentFragmentManager.popBackStack()
            }
        }
    }



    private fun searchAlumno() {
        if (inMatricula.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "Faltó capturar matrícula", Toast.LENGTH_SHORT).show()
        } else {
            db = dbAlumnos(requireContext())
            db.openDatabase()
            val alumno = db.getAlumno(inMatricula.text.toString())
            if (alumno.id != 0) {
                populateFields(alumno)
                btnBorrar.isEnabled = true
            } else {
                Toast.makeText(requireContext(), "No se encontró la matrícula", Toast.LENGTH_SHORT).show()
            }
            db.close()
        }
    }

    private fun deleteAlumno() {
        if (inMatricula.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "Faltó capturar matrícula", Toast.LENGTH_SHORT).show()
        } else {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmación")
            builder.setMessage("¿Está seguro de que desea borrar al alumno con matrícula ${inMatricula.text}?")
            builder.setPositiveButton("Sí") { dialog, _ ->
                db = dbAlumnos(requireContext())
                db.openDatabase()
                val matricula = inMatricula.text.toString()
                val status = db.borrarAlumno(matricula)
                if (status != 0) {
                    clearFields()
                    btnBorrar.isEnabled = false
                    Toast.makeText(requireContext(), "Se borró el usuario con la matrícula $matricula", Toast.LENGTH_SHORT).show()

                    // Close the fragment if an Alumno object was passed
                    if (arguments?.getParcelable<Alumno>(ARG_ALUMNO) != null) {
                        parentFragmentManager.popBackStack()
                    }
                } else {
                    Toast.makeText(requireContext(), "No se encontró la matrícula", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
                db.close()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            val alert = builder.create()
            alert.show()
        }
    }


    private fun clearFields() {
        inMatricula.setText("")
        inNombre.setText("")
        inDomicilio.setText("")
        inEspecialidad.setText("")
        imgAlumno.setImageResource(R.mipmap.foto)
        imageUri = null
    }

    companion object {
        @JvmStatic
        fun newInstance(alumno: Alumno) =
            DbFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ALUMNO, alumno)
                }
            }
    }
}
