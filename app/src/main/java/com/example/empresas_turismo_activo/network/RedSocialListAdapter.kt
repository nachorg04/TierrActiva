package com.example.empresas_turismo_activo.network

import com.example.empresas_turismo_activo.data.model.RedSocial
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.Locale

class RedSocialListAdapter {

    @FromJson
    fun desdeJson(map: Map<String, String>?): List<RedSocial> =
        map.orEmpty()
            .entries
            .sortedBy { it.key }
            .map { (plataforma, url) ->
                RedSocial(
                    plataforma = plataforma.replaceFirstChar { ch ->
                        if (ch.isLowerCase()) ch.titlecase(Locale.ROOT) else ch.toString()
                    },
                    url = url.trim(),
                )
            }

    @ToJson
    fun aJson(list: List<RedSocial>): Map<String, String> =
        list.associate { it.plataforma to it.url }
}
