package dev.vstd.shoppingcart.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.keego.shoppingcart.R
import dev.keego.shoppingcart.databinding.ActivityMainBinding
import dev.vstd.shoppingcart.common.utils.beGone
import dev.vstd.shoppingcart.common.utils.beVisible
import dev.vstd.shoppingcart.pricecompare.retrofit.model.Filter
import dev.vstd.shoppingcart.pricecompare.retrofit.model.SerpResult
import dev.vstd.shoppingcart.pricecompare.ui.adapter.ItemFilterDrawerAdapter


@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.container_main) as NavHostFragment).navController
        binding.botNav.setupWithNavController(navController)
        closeDrawer()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in listOf(
                    R.id.groupsFragment,
                    R.id.comparingFragment,
                    R.id.shoppingFragment,
                    R.id.settingFragment
                )
            ) {
                binding.botNav.beVisible()
            } else {
                binding.botNav.beGone()
            }
        }

    }

    fun closeDrawer() {
        binding.filterDrawer.close()
    }

    fun openDrawer() {
        binding.filterDrawer.open()

    }

    fun initAdapterDrawer() {
//        binding.itemFilterRecyclerView.adapter = ItemFilterDrawerAdapter {
//
//        }
    }

    fun setFilterAdapter(adapter: ItemFilterDrawerAdapter) {
        binding.itemFilterRecyclerView.adapter = adapter

    }

    fun setFilterDrawerData(filters: List<Filter>) {
        (binding.itemFilterRecyclerView.adapter as ItemFilterDrawerAdapter).setData(filters)
    }


}