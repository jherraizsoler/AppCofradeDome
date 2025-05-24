package com.example.CofradeDome.Adaptadores

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.CofradeDome.fragmentos.DAOCofradesEnsayosFragment
import com.example.CofradeDome.fragmentos.DAOCofradesFragment
import com.example.CofradeDome.fragmentos.DAOPermisosFragment
import com.example.CofradeDome.fragmentos.DAOListaFragment

class MyViewPagerAdapter(fragmentActivity: FragmentActivity, private val numeroPermiso: Int) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        if (numeroPermiso == 1) {
            return DAOListaFragment()
        } else if (numeroPermiso == 2) {
            return when (position) {
                0 -> DAOListaFragment()

                1 -> DAOCofradesFragment()

                else -> DAOListaFragment()
            }
        } else if (numeroPermiso == 3) {
            return when (position) {
                0 -> DAOListaFragment()
                1 -> DAOCofradesFragment()

                2 -> DAOPermisosFragment()
                3 -> DAOCofradesEnsayosFragment()
                else -> DAOListaFragment()

            }
        }
        return DAOListaFragment()
    }

    override fun getItemCount(): Int {
        if (numeroPermiso == 1) {
            return 1
        } else if (numeroPermiso == 2) {
            return 2
        } else if (numeroPermiso == 3) {
            return 4
        }
        return 1
    }
}
