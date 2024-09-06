package com.zime.garage.common

import java.util.prefs.Preferences

/**
 * 자체 설정값 저장시 사용함.
 * Preferences.userRoot().node("com/zime/garage") : Preferences
 */

object PreferencesManager {

    fun load() : String{
        println("PreferencesManager load")
        try {
            val prefs = Preferences.userRoot().node("com/zime/garage")
            val username = prefs.get("username", "default")
            val age = prefs.getInt("age", 0)

            println("Username: $username")
            println("Age: $age")


            // 저장 위치 출력 (운영체제에 따라 다름, 명시적 출력 X)
            println("Preferences stored at: ${System.getProperty("user.home")}/.java/.userPrefs/com/zime/garage")

            return username

        } catch (e: Exception) {
            println("PreferencesManager load error: $e")
        }

        return ""
    }

    fun TestWrite() {
        val prefs = Preferences.userRoot().node("com/zime/garage")
        // 데이터 저장
        prefs.put("username", "JohnDoe")
        prefs.putInt("age", 30)
    }
}