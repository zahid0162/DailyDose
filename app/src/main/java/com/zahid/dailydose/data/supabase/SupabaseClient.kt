package com.zahid.dailydose.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object SupabaseClient {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                host = "com.zahid.dailydose" // this can be anything, eg. your package name or app/company url (not your Supabase url)
                scheme = "reset_password"
                // On Android only, you can set OAuth and SSO logins to open in a custom tab, rather than an external browser:
                defaultExternalAuthAction = ExternalAuthAction.DEFAULT//defaults to ExternalAuthAction.ExternalBrowser
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)

        }
    }
    
    val auth = client.auth
    val postgrest = client.postgrest
    val realtime = client.realtime
    val storage = client.storage
}