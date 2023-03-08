package com.example.monitoroptions
import java.io.Serializable

class NSEOptionData : Serializable {
    var id : Int = 0
    var ce_price = ""
    var pe_price = ""
    var ce_strike = ""
    var pe_strike = ""
    var expiry = ""
    var alert = ""
    var previous_profit = ""

    fun NSEOptionData() {
        id = 0
        ce_price = ""
        pe_price = ""
        ce_strike = ""
        pe_strike = ""
        expiry = ""
        alert = ""
        previous_profit = ""
    }
}

class CEAndPE {
    var PorL : Double = 0.0
    var NotificationText = ""
    var identifier = ""
    var lastPriceCE : Double = 0.0
    var lastPricePE : Double = 0.0


    fun CEAndPE() {
        lastPriceCE = 0.0
        PorL = 0.0
        NotificationText = ""
        identifier = ""
        lastPricePE = 0.0
    }
}
