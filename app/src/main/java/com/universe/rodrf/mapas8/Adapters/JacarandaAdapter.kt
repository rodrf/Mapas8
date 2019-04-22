package com.universe.rodrf.mapas8.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.universe.rodrf.mapas8.JacarandaItem
import com.universe.rodrf.mapas8.R
import kotlinx.android.synthetic.main.item_jacaranda.view.*
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.properties.Delegates

class JacarandaAdapter : RecyclerView.Adapter<JacarandaAdapter.JacarandaViewHolder>() {
    var dataSourceList: List<JacarandaItem> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }
    lateinit var onItemClickLner:(Int, JacarandaItem) ->Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JacarandaViewHolder {
        return JacarandaViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_jacaranda, parent, false)
        )
    }

    override fun getItemCount(): Int = dataSourceList.count()
    override fun onBindViewHolder(holder: JacarandaViewHolder, position: Int) {
        holder.bindView((dataSourceList[position]))
        holder.itemView.btnItemCard?.setOnClickListener{
            onItemClickLner(position, dataSourceList[position])
        }
    }
    fun setOnItemClickListener(block:(Int, JacarandaItem) -> Unit){
        onItemClickLner = block
    }

    override fun onViewRecycled(holder: JacarandaViewHolder) {
        super.onViewRecycled(holder)
        holder.job.cancel()
        //Cada que se recicle la vista
    }

    //First ViewGolder
    class JacarandaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var job = Job()
        val uiViewHolderScope = CoroutineScope(Dispatchers.Main+job)

        @SuppressLint("SetTextI18n")
        fun bindView(data: JacarandaItem) {
            itemView.ivItemCardHeader?.setImageResource(R.drawable.jacaranda_placeholder)
            itemView.tvItemCardTitle.text = "Jacaranda ${data.id}"

           job= uiViewHolderScope.launch(Dispatchers.IO) {
                val address = getGeocoderAddress(itemView.context, data.location)
                launch(Dispatchers.Main) {
                    itemView.tvItemCardAddress?.text ="$address"

                }
//                val address = async { getGeocoderAddress(itemView.context,data.location) }.await()
//                launch(Dispatchers.Main) {
//                    itemView.tvItemCardAddress?.text="${address.a}"              }
            }
        }
        suspend fun getGeocoderAddress(ctx: Context, location:LatLng):String{
            val geocoder = Geocoder(ctx, Locale.getDefault())
            return try {
                val listAddress = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                if (listAddress.isNotEmpty()){
                    val sinlgeAddress = listAddress[0]
                    val addressFragments = with(sinlgeAddress){
                        (0..maxAddressLineIndex).map { getAddressLine(it) }
                    }
                    addressFragments.joinToString(separator = "\n")
                }else{
                    "La dirección de esta ubicación no está disponible"
                }
            }catch (e: IOException){
                e.printStackTrace()
                "El servicio no está disponible"
            }catch (ilegal: IllegalArgumentException){
                ilegal.printStackTrace()
                "LatLong invalida, intenta nuevamente"
            }catch (error: Exception){
                error.printStackTrace()
                "General exception"
            }
        }

    }
}