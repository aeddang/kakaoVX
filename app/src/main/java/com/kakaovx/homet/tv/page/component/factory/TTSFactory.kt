package com.kakaovx.homet.tv.page.component.factory


import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import com.kakaovx.homet.tv.BuildConfig
import com.kakaovx.homet.tv.R
import com.lib.util.Log
import okhttp3.ResponseBody
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

enum class BgmResource(val path:String){
    BREAK_BGM("${BuildConfig.APP_STORAGE_ADDRESS}contents/audio/break_bgm.mp3")
}
enum class StaticResource(val id:Int){

    END_BEEP(R.raw.end_long_beep),
    START_BEEP(R.raw.short_beep),
    VOICE_START(R.raw.voice_start),
    SEETING_START(R.raw.setting_start),
    SEETING_START_2(R.raw.setting_start_2),
    EXERCISE_START(R.raw.exercise_start),
    EXERCISE_TIME(R.raw.exercise_time),
    BREAK_TIME(R.raw.break_time),

    FIRST(R.raw.first),
    NEXT(R.raw.next),
    LAST(R.raw.last),
    EXERCISE_COUNT(R.raw.exercise_count)

}

enum class StaticSpeech(val id:Int){

}

class TTSFactory (val context: Context) {
    private val appTag = javaClass.simpleName
    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_GAME)
        .build()

    var bgmPlayer:MediaPlayer? = null
    var soundPool:SoundPool? = null
    private val staticSoundPool:SoundPool = SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(10).build()
    private val cachefiles:ArrayList<Pair<String, Pair<String, Int>>> = arrayListOf()
    private val staticfiles:ArrayList<Pair<String, Pair<String, Int>>> = arrayListOf()

    private val staticStreams:ArrayList<Int> = arrayListOf()
    private val instanceStreams:ArrayList<Int> = arrayListOf()


    private val cachePath = context.cacheDir.path + File.separator

    init {
        StaticResource.values().forEach { addStaticSound(it.id) }
    }

    fun destroy() {
        clear()
        staticSoundPool.release()
    }

    fun clear(){
        clearAllEffect()
        stopBgm()
    }

    fun clearAllEffect(){
        soundPool?.release()
        cachefiles.forEach {
            try {
                val file = File( it.second.first )
                file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        cachefiles.clear()
        soundPool = null
    }


    fun stopAllEffect(){
        staticStreams.forEach { staticSoundPool.stop(it) }
        instanceStreams.forEach { soundPool?.stop(it) }
        staticStreams.clear()
        instanceStreams.clear()
    }

    fun effect(speech:StaticSpeech){
        val find = staticfiles.find { context.getString(speech.id).hashCode().toString() == it.first }?.second?.second
        find?.let { staticSoundPool.play(it,1.0f,1.0f,1,0,1.0f).apply { staticStreams.add(this) } }
    }

    fun effect(resource:StaticResource){
        val find = staticfiles.find { resource.id.toString() == it.first }?.second?.second
        find?.let { sndid ->
            staticSoundPool.play(sndid,1.0f,1.0f,1,0,1.0f).apply {
                Log.d(appTag, "effect Sound: $this")
                staticStreams.add(this) }
        }
    }

    fun effect(sentence:String){
        val find = cachefiles.find { sentence == it.first }?.second?.second
        find?.let {
            soundPool?.let { pool->
                pool.play(it, 1.0f, 1.0f, 1, 0, 1.0f).apply { instanceStreams.add(this) }
            }
        }
    }

    fun stopBgm(){
        bgmPlayer?.stop()
        bgmPlayer?.release()
        bgmPlayer = null
    }

    fun playBgm(resource:BgmResource){
        stopBgm()
        bgmPlayer = MediaPlayer.create(context, Uri.parse(resource.path))
        bgmPlayer?.start()
    }

    fun onResume() {
        bgmPlayer?.start()
    }

    fun onPause() {
        bgmPlayer?.pause()
    }

    fun addStaticSound(id:Int){
        val sentence = id.toString()
        val find = staticfiles.find { sentence == it.first }
        if(find != null) return
        val soundId = staticSoundPool.load(context, id, 1)
        staticfiles.add(Pair( sentence, Pair("",soundId)))
    }



    private fun writeResponseBodyToDisk(body: ResponseBody, name:String = UUID.randomUUID().toString()): String? {
        try {
            val cacheFile = "$cachePath$name.mp3"
            var dataFile = File(cacheFile)
            if (dataFile.exists()) {
                //Log.d(appTag, "delete path = [${dataFile.path}]")
                dataFile.delete()
                dataFile = File(cacheFile)
            }

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(dataFile)
                outputStream.run {
                    while (true) {
                        val read = inputStream?.read(fileReader) ?: -1
                        if (read == -1) break
                        write(fileReader, 0, read)
                        fileSizeDownloaded += read.toLong()
                    }
                    flush()
                    Log.d(appTag, "file download complete")
                }
                return cacheFile
            } catch (e: IOException) {
                Log.d(appTag, "file download IOException")
                return cacheFile
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            Log.d(appTag, "file download IOException")
            return null
        }
    }



    private fun getPlayer():MediaPlayer{
        val player = MediaPlayer()
        player.setAudioAttributes(audioAttributes)
        return player
    }


}






