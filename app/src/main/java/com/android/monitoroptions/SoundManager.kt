package com.android.monitoroptions
import android.media.Ringtone

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
