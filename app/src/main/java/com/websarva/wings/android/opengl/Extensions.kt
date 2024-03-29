package com.websarva.wings.android.mycreatures

import android.content.res.Resources
import androidx.annotation.RawRes

fun Resources.getRawTextFile(@RawRes resource: Int): String =
    openRawResource(resource).bufferedReader().use { it.readText() }