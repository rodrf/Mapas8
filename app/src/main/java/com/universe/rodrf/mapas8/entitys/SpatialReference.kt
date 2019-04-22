package com.universe.rodrf.mapas8.entitys

import com.google.gson.annotations.SerializedName


data class SpatialReference(

	@field:SerializedName("latestWkid")
	val latestWkid: Int? = null,

	@field:SerializedName("wkid")
	val wkid: Int? = null
)