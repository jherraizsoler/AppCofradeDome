package com.example.CofradeDome.Activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.CofradeDome.Adaptadores.MyViewPagerAdapter
import com.example.CofradeDome.ConexionCliente.ClienteSSL
import com.example.CofradeDome.MiExecutorTask.MiExecutorTaskCallback
import com.example.CofradeDome.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity(), View.OnClickListener, MiExecutorTaskCallback {

    var myViewPagerAdapter: MyViewPagerAdapter? = null
    var idCofrade: Int = 0
    var permisoUsuario: Int = 0
    var correoElectronico: String = ""
    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    var lanzadorCambiarContraseña: ActivityResultLauncher<Intent>? = null
    var colorPrincipal: String? = null
    var fuenteEncabezados: Typeface? = null
    val fuenteDatos: Typeface? = null
    var fuenteFeedback: Typeface? = null
    var fuenteBotones: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        idCofrade = intent.getIntExtra("IDCOFRADEPASALISTA", 0)
        correoElectronico = intent.getStringExtra("EMAIL").toString()
        permisoUsuario = intent.getIntExtra("PERMISO", 0)

        // Recuperar los datos del Intent (si los hay)
        val intent = intent
        val ultimaPestañaRecibida =
            intent.getIntExtra("ultimaPestaña", 0) // 0 es el valor predeterminado
        val permisoUsuarioRecibido =
            intent.getIntExtra("permisos", -1) // -1 es el valor predeterminado

        Log.d(
            "MainActivity",
            "Última Pestaña Recibida (onCreate): $ultimaPestañaRecibida"
        )
        Log.d(
            "MainActivity",
            "Permisos Usuario Recibidos (onCreate): $permisoUsuarioRecibido"
        )

        // Decide qué valor usar para permisoUsuario
        permisoUsuario = if (permisoUsuarioRecibido != -1) {
            permisoUsuarioRecibido // Usar el valor que viene de Preferencias
        } else {
            permisoUsuario // Usar el valor inicial
        }
        // Restaurar la última pestaña solo al crear la actividad
        restaurarUltimaPestaña(ultimaPestañaRecibida)

        Log.i("MainActivityInfo", "Email usuario: $correoElectronico")
        Log.i("MainActivityInfo", "Permiso usuario: $permisoUsuario")

        configurarTabLayout(permisoUsuario)

        tabLayout = findViewById(R.id.tabLayout)
        val viewPager2 = findViewById<ViewPager2>(R.id.view_pager2)

        myViewPagerAdapter = MyViewPagerAdapter(this, permisoUsuario)
        viewPager2.adapter = myViewPagerAdapter

        tabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager2.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout?.getTabAt(position)!!.select()
            }
        })

        // Toolbar menu
        toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)

        lanzadorCambiarContraseña =
            registerForActivityResult<Intent, ActivityResult>(
                ActivityResultContracts.StartActivityForResult(),
                object : ActivityResultCallback<ActivityResult> {
                    override fun onActivityResult(resultado: ActivityResult) {
                        if (resultado.resultCode == RESULT_OK) {
                            Toast.makeText(
                                this@MainActivity,
                                "Contraseña cambiada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
    }

    // Menu Tollbar Item Salir
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val opcionSalir: MenuItem? = menu.findItem(R.id.Salir)
        val opcionCambiarContraseña: MenuItem? = menu.findItem(R.id.CambiarContraseña)
        val opcionPreferencias: MenuItem? = menu.findItem(R.id.Preferencias)
        val opcionMasInformacion: MenuItem? = menu.findItem(R.id.item_masInformacion)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intItemId = item.itemId


        if (intItemId == R.id.CambiarContraseña) {
            val mainContraseña = Intent(
                this@MainActivity,
                CambiarPasswordActivity::class.java
            )
            mainContraseña.putExtra("PERMISO", permisoUsuario)
            mainContraseña.putExtra("EMAIL", correoElectronico)

            lanzadorCambiarContraseña!!.launch(mainContraseña)
        } else if (intItemId == R.id.Salir) {
            finishAffinity()
            System.exit(0)
            return true
        } else if (intItemId == R.id.Preferencias) {
            val preferenciasIntent = Intent(
                this@MainActivity,
                PreferenciasActivity::class.java
            )
            preferenciasIntent.putExtra("ultima_pestaña", tabLayout!!.selectedTabPosition)
            preferenciasIntent.putExtra("permisos", permisoUsuario)
            startActivity(preferenciasIntent)
        }else if(intItemId == R.id.item_masInformacion){
            val masInformacionIntent = Intent(this@MainActivity,
                MasInformacionActivity::class.java
                )
            startActivity(masInformacionIntent)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun configurarTabLayout(numeroPermiso: Int) {
        val adapter = MyViewPagerAdapter(
            this,
            numeroPermiso
        ) // 'this' puede ser una Activity o FragmentActivity
        val viewPager = findViewById<ViewPager2>(R.id.view_pager2)
        viewPager.adapter = adapter

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        // Limpia las pestañas existentes
        tabLayout.removeAllTabs()

        // Configura las pestañas según el número de permiso
        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.setText("Lista")
                1 -> if (numeroPermiso >= 2) {
                    tab.setText("Gestion")
                }

                2 -> if (numeroPermiso >= 3) {
                    tab.setText("Permisos")
                }

                3 -> if (numeroPermiso >= 3) {
                    tab.setText("Ensayos")
                }
            }
        }.attach()
    }

    override fun onClick(v: View) {
    }

    override fun onRespuestaRecibida(respuesta: String) {
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Aplicar color principal
        val colorPrincipalPref = prefs.getString("colorPrincipal", "Morado")!!
        val rootView =
            findViewById<View>(android.R.id.content) // Obtiene la vista raíz de la Activity
        if (colorPrincipalPref.length > 0 && rootView != null) {
            colorPrincipal = colorPrincipalPref

            //Cambiar color backgroung toolbar
            val colorStateList =
                ColorStateList.valueOf(obtenerColorPrincipalDesdeNombre(colorPrincipalPref))
            toolbar!!.backgroundTintList = colorStateList

            // Cambiar logo del toolbar
            cambiarLogoToolbarDesdeNombre(colorPrincipalPref)
            // Fondo
            val colorFondo = obtenerColorFondoDesdeNombre(colorPrincipalPref)
            rootView.setBackgroundColor(colorFondo)

            //Cambiar el color al seleccionar un item en el tablayout
            tabLayout!!.setSelectedTabIndicatorColor(
                obtenerColorPrincipalDesdeNombre(
                    colorPrincipalPref
                )
            )


            // Establecer el color del texto seleccionado a rojo
            tabLayout!!.setTabTextColors(
                ContextCompat.getColor(this, R.color.gray),  // Color del texto no seleccionado
                obtenerColorPrincipalDesdeNombre(colorPrincipalPref) // Color del texto seleccionado
            )
        } else {
            Toast.makeText(
                this,
                "Fallo al cargar las preferencias en los colores.",
                Toast.LENGTH_SHORT
            ).show()
        }
        // Aplicar fuente de los Encabezados
        val fuenteEncabezadosPref = prefs.getString("fuenteEncabezados", "sans-serif")!!
        if (fuenteEncabezadosPref.length > 0) {
            fuenteEncabezados = obtenerFuenteDesdeNombre(fuenteEncabezadosPref)
        } else {
            Toast.makeText(
                this,
                "Error al cargar la fuente encabezados de las preferencias",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Aplicar fuente de los Feedback
        val fuenteFeedbackPref = prefs.getString("fuenteFeedback", "sans-serif")!!
        if (fuenteFeedbackPref.length > 0) {
            fuenteFeedback = obtenerFuenteDesdeNombre(fuenteFeedbackPref)
        } else {
            Toast.makeText(
                this,
                "Error al cargar la fuente del comentario de las preferencias",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Aplicar fuente de los Botonoes
        val fuenteBotonesPref = prefs.getString("fuenteBotones", "sans-serif")!!
        if (fuenteBotonesPref.length > 0) {
            fuenteBotones = obtenerFuenteDesdeNombre(fuenteBotonesPref)
        } else {
            Toast.makeText(
                this,
                "Error al cargar la fuente del boton de las preferencias",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun restaurarUltimaPestaña(indicePestaña: Int) {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager2 = findViewById<ViewPager2>(R.id.view_pager2)

        // Espera a que el TabLayout se configure completamente antes de seleccionar la pestaña
        tabLayout.post {
            if (indicePestaña < tabLayout.tabCount) {
                tabLayout.getTabAt(indicePestaña)!!.select()
                viewPager2.currentItem = indicePestaña
            }
        }
    }

    private fun obtenerColorPrincipalDesdeNombre(nombreColor: String): Int {
        return when (nombreColor) {
            "Azul" -> ContextCompat.getColor(this, R.color.Color_Principal_Azul)
            "Verde" -> ContextCompat.getColor(this, R.color.Color_Principal_Verde)
            "Rojo" -> ContextCompat.getColor(this, R.color.Color_Principal_Rojo)
            "Naranja" -> ContextCompat.getColor(this, R.color.Color_Principal_Naranja)
            "Morado" -> ContextCompat.getColor(this, R.color.Color_Principal_Morado)
            else -> ContextCompat.getColor(this, R.color.Color_Principal_Morado)
        }
    }

    private fun obtenerColorFondoDesdeNombre(nombreColor: String): Int {
        return when (nombreColor) {
            "Azul" -> ContextCompat.getColor(this, R.color.Color_fondo_Azul)
            "Verde" -> ContextCompat.getColor(this, R.color.Color_fondo_Verde)
            "Rojo" -> ContextCompat.getColor(this, R.color.Color_fondo_Rojo)
            "Naranja" -> ContextCompat.getColor(this, R.color.Color_fondo_Naranja)
            "Morado" -> ContextCompat.getColor(this, R.color.Color_fondo_Morado)
            else -> ContextCompat.getColor(this, R.color.Color_fondo_Morado)
        }
    }

    private fun cambiarLogoToolbarDesdeNombre(nombreColor: String) {
        when (nombreColor) {
            "Azul" -> toolbar!!.setLogo(R.mipmap.ic_logo_azul)
            "Verde" -> toolbar!!.setLogo(R.mipmap.ic_logo_verde)
            "Rojo" -> toolbar!!.setLogo(R.mipmap.ic_logo_rojo)
            "Naranja" -> toolbar!!.setLogo(R.mipmap.ic_logo_naranja)
            "Morado" -> toolbar!!.setLogo(R.mipmap.ic_logo_morado)
            else -> toolbar!!.setLogo(R.mipmap.ic_logo_morado)
        }
    }

    private fun obtenerFuenteDesdeNombre(nombreFuente: String?): Typeface {
        if (nombreFuente == null || nombreFuente.equals("sans-serif", ignoreCase = true)) {
            return Typeface.DEFAULT
        } else if (nombreFuente.equals("serif", ignoreCase = true)) {
            return Typeface.SERIF
        } else if (nombreFuente.equals("monospace", ignoreCase = true)) {
            return Typeface.MONOSPACE
        } else {
            try {
                val fontResourceId = resources.getIdentifier(
                    nombreFuente, "font",
                    packageName
                )
                if (fontResourceId != 0) {
                    val typeface = ResourcesCompat.getFont(this, fontResourceId)
                    if (typeface != null) {
                        return typeface
                    } else {
                        Log.e(
                            "LoginActivity",
                            "Error al cargar fuente (ResourcesCompat): $nombreFuente"
                        )
                        return Typeface.DEFAULT
                    }
                } else {
                    Log.e(
                        "LoginActivity",
                        "Recurso de fuente no encontrado: $nombreFuente"
                    )
                    return Typeface.DEFAULT
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error al cargar fuente: $nombreFuente", e)
                return Typeface.DEFAULT
            }
        }
    }
}
