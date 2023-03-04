package com.example.monitoroptions
import android.media.Ringtone
import android.os.Parcel
import android.os.Parcelable

class SoundManager(ringtone: Ringtone?)  {
     var _ringPlayer: Ringtone? = null;

    init {
        _ringPlayer = ringtone;
    }
    fun playRingtone() {
        if(_ringPlayer?.isPlaying() == true)
            _ringPlayer?.stop()

        _ringPlayer?.play()
    }

    fun stopRingtone() {
        if(_ringPlayer?.isPlaying() == true)
            _ringPlayer?.stop()
    }
}
