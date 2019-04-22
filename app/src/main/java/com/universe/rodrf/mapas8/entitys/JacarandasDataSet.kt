package com.universe.rodrf.mapas8.entitys


import com.google.gson.annotations.SerializedName


data class JacarandasDataSet(

	@field:SerializedName("features")
	val jacaradas: List<FeaturesItem?>? = null,

	@field:SerializedName("displayFieldName")
	val displayFieldName: String? = null,

	@field:SerializedName("spatialReference")
	val spatialReference: SpatialReference? = null,

	@field:SerializedName("fields")
	val fields: List<FieldsItem?>? = null,

	@field:SerializedName("fieldAliases")
	val fieldAliases: FieldAliases? = null,

	@field:SerializedName("geometryType")
	val geometryType: String? = null
)