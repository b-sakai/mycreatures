package com.websarva.wings.android.mycreatures.database

import java.io.Serializable

data class Speicies (val name: String) : Serializable {
    var key: Long = 0 // primary key in database
    public var scientificName: String = ""
    public var explanation: String = ""
}