package com.example.appmenubutton

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.appmenubutton.database.Alumno
import com.example.appmenubutton.database.dbAlumnos

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DbFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DbFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

//    private lateinit var btnAgrega : Button
    private lateinit var db : dbAlumnos

    private lateinit var btnGuardar : Button
    private lateinit var btnBuscar : Button
    private lateinit var btnBorrar : Button
    private lateinit var inMatricula : EditText
    private lateinit var inNombre : EditText
    private lateinit var inDomicilio : EditText
    private lateinit var inEspecialidad : EditText
    private lateinit var inUrlImagen : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val View = inflater.inflate(R.layout.fragment_db, container, false)

        // inflate the layout for this fragment
//        btnAgrega = View.findViewById(R.id.btnTest)

        btnGuardar = View.findViewById(R.id.btnGuardar)
        btnBuscar = View.findViewById(R.id.btnBuscar)
        btnBorrar = View.findViewById(R.id.btnBorrar)
        inMatricula = View.findViewById(R.id.inMatrciula)
        inNombre = View.findViewById(R.id.inNombre)
        inDomicilio = View.findViewById(R.id.inDomicilio)
        inEspecialidad = View.findViewById(R.id.inEspecialidad)
        inUrlImagen = View.findViewById(R.id.inUrlImagen)

        btnGuardar.setOnClickListener {

            if (inNombre.text.toString().contentEquals("") ||
                inDomicilio.text.toString().contentEquals("") ||
                inMatricula.text.toString().contentEquals("") ||
                inEspecialidad.text.toString().contentEquals("")
            ) {
                Toast.makeText(
                    requireContext(),
                    "Faltó información por capturar",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                var alumno: Alumno
                alumno = Alumno()
                alumno.nombre = inNombre.text.toString()
                alumno.matricula = inMatricula.text.toString()
                alumno.domicilio = inDomicilio.text.toString()
                alumno.especialidad = inEspecialidad.text.toString()
                alumno.foto = inUrlImagen.text.toString()

                db = dbAlumnos(requireContext())
                db.openDatabase()
                var msg : String
                var id : Long
                var isRegistered: Boolean = db.matriculaExiste(alumno.matricula)
                if (isRegistered) {
                    msg = "Estudiante con matrícula ${alumno.matricula} se actualizó."
                    db.actualizarAlumno(alumno, alumno.matricula)
                    id = 1
                } else {
                    id = db.insertarAlumno(alumno)
                    msg = "Se agregó con éxito con ID " + id
                }
                Toast.makeText(
                    requireContext(),
                    msg,
                    Toast.LENGTH_SHORT
                ).show()
                db.close()
            }
        }

        btnBuscar.setOnClickListener {
            // validar
            if (inMatricula.text.toString().contentEquals("")) {
                Toast.makeText(
                    requireContext(),
                    "Faltó capturar matrícula",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                db = dbAlumnos(requireContext())
                db.openDatabase()
                var alumno : Alumno = Alumno()
                alumno = db.getAlumno(inMatricula.text.toString())
                if (alumno.id != 0) {
                    inNombre.setText(alumno.nombre)
                    inDomicilio.setText(alumno.domicilio)
                    inEspecialidad.setText(alumno.especialidad)
                    inUrlImagen.setText(alumno.foto)
                    btnBorrar.isEnabled = true
                }
                else
                    Toast.makeText(
                        requireContext(),
                        "No se encontró la matrícula",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

        btnBorrar.setOnClickListener {
            // validar
            if (inMatricula.text.toString().contentEquals("")) {
                Toast.makeText(
                    requireContext(),
                    "Faltó capturar matrícula",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                db = dbAlumnos(requireContext())
                db.openDatabase()
                var matricula : String = inMatricula.text.toString()
                var status : Int = db.borrarAlumno(matricula)
                if (status != 0) {
                    // clean
                    inMatricula.setText("")
                    inNombre.setText("")
                    inDomicilio.setText("")
                    inEspecialidad.setText("")
                    inUrlImagen.setText("")
                    // falta setear la imagen salvada por URL
                    btnBorrar.isEnabled = false
                    Toast.makeText(
                        requireContext(),
                        "Se borró el usuario con la matrícula $matricula",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else
                    Toast.makeText(
                        requireContext(),
                        "No se encontró la matrícula",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

//        btnAgrega.setOnClickListener({
//            var alumno: Alumno
//            alumno = Alumno()
//            alumno.nombre="Jose"
//            alumno.matricula="233333"
//            alumno.domicilio="Av. del sol 33"
//            alumno.foto="alumno.png"
//            alumno.especialidad = "pipipopo"
//
//            db = dbAlumnos(requireContext())
//            db.openDatabase()
//            db.insertarAlumno(alumno)
//        })
        return View
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DbFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DbFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}