package com.example.monitoroptions

class NSEOptionData {

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
