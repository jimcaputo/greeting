package com.example.greetingcard

import android.R
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Audio
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.greetingcard.ui.theme.GreetingCardTheme
import androidx.lifecycle.viewmodel.compose.viewModel

const val APP_NAME = "Marriage Saver"

class SpeechRecognitionManager(
    val context: Context,
    val transcriptionViewModel: TranscriptionViewModel): RecognitionListener {

    lateinit private var speechRecognizer: SpeechRecognizer
    lateinit private var speechIntent: Intent
    lateinit private var audioManager: AudioManager

    public fun create() {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0)
        //audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        //speechIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        speechRecognizer.setRecognitionListener(this)
        speechRecognizer.startListening(speechIntent)
    }

    override fun onError(errorCode: Int) {
        // http://developer.android.com/reference/android/speech/SpeechRecognizer.html#ERROR_INSUFFICIENT_PERMISSIONS
        when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> Log.v(APP_NAME,"Audio recording error")
            SpeechRecognizer.ERROR_CLIENT -> Log.v(APP_NAME,"Client side error")
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> Log.v(APP_NAME,"Insufficient permissions")
            SpeechRecognizer.ERROR_NETWORK -> Log.v(APP_NAME,"Network error")
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> Log.v(APP_NAME,"Network timeout")
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> Log.v(APP_NAME,"RecognitionService busy")
            SpeechRecognizer.ERROR_SERVER -> Log.v(APP_NAME,"Error from server")
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> Log.v(APP_NAME,"No speech input")
            SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> Log.v(APP_NAME, "Language not available (ie downloaded yet)")
            SpeechRecognizer.ERROR_NO_MATCH -> {
                speechRecognizer.startListening(speechIntent)
            }
            else -> Log.v(APP_NAME, "Error Code: $errorCode")
        }
    }

    override fun onResults(bundle: Bundle) {
        val words = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        //val scores = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
        Log.v(APP_NAME, words!![0])
        transcriptionViewModel.updateTranscription(words!![0])
        if (words[0].startsWith("Okay Donald", ignoreCase = true) == true) {
            Log.v(APP_NAME, "Keyword detected!")
            //val notificationUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            //val mediaPlayer = MediaPlayer.create(context, notificationUri)
            //mediaPlayer.start()
        }
        speechRecognizer.startListening(speechIntent)
    }

    override fun onReadyForSpeech(bundle: Bundle) {
    }

    override fun onBeginningOfSpeech() {
    }

    override fun onRmsChanged(v: Float) {
    }

    override fun onBufferReceived(bytes: ByteArray) {
    }

    override fun onEndOfSpeech() {
    }

    override fun onPartialResults(bundle: Bundle) {
    }

    override fun onEvent(i: Int, bundle: Bundle) {
    }
}

// need to get permissions
// need to download language model in settings -> languages -> speech
// for virtual device, need to open settings (little 3 dots above device) and enable microphone pass through

class MainActivity : ComponentActivity() {

    //private val transcriptionViewModel = TranscriptionViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GreetingCardTheme {
                Greeting()
            }
        }
    }
}


class TranscriptionViewModel : ViewModel() {
    var transcription by mutableStateOf("fuck off")
        private set

    public fun updateTranscription(text: String) {
        transcription = text
    }


}

@Composable
fun Greeting(transcriptionViewModel: TranscriptionViewModel = viewModel()) {
    val context = LocalContext.current
    var buttonText by rememberSaveable { mutableStateOf("Start Capture") }

    Column(
        //verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,

        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Welcome to " + APP_NAME,
            fontSize = 20.sp,
            lineHeight = 80.sp,
            color = Color.Blue
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                modifier = Modifier.padding(10.dp),
                onClick = {
                    if (buttonText == "Start Capture") {
                        val speechRecognitionManager = SpeechRecognitionManager(
                            context = context, transcriptionViewModel = transcriptionViewModel)
                        speechRecognitionManager.create()
                        buttonText = "Stop Capture"
                    }
                    else {
                        buttonText = "Start Capture"
                    }
                }
            ) {
                Text(
                    text = buttonText
                )
            }
            Button(
                modifier = Modifier.padding(10.dp),
                onClick = {
                    transcriptionViewModel.updateTranscription("foobar")
                }
            ) {
                Text(
                    text = "Update Transcription"
                )
            }
        }
        Row() {
            Text(text = transcriptionViewModel.transcription)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GreetingCardTheme {
        Greeting()
    }
}