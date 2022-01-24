package com.example.chatapp

import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder

object NavOptions {
    fun getNavOptions(): NavOptions.Builder {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.enter)
            .setPopEnterAnim(R.anim.enter)
        return navOptions
    }
}