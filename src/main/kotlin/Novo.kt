import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

class YTExtractorTest : YouTubeExtractor() {

  lateinit var files: HashMap<Int, YtFile>

  override fun onExtractionComplete(ytFiles: HashMap<Int, YtFile>?, videoMeta: VideoMeta?) {
    println("Completed!! ${ytFiles!!.size}")
    files = ytFiles
  }
}

fun main() {
  // first get video URL
  val extractor = YTExtractorTest()
  val thread = Thread {
    extractor.extract("https://www.youtube.com/watch?v=lpc1lEJ-SRc")
  }
  thread.start()

  thread.join()

  val myVideo = extractor.files.values.last {
    (it.format.height > 0) &&
            it.format.audioBitrate > 0
  }

  // then shows the video from some URL using Jetpack Compose
  singleWindowApplication(
    title = "Video Player",
    state = WindowState(width = 800.dp, height = 800.dp)
  ) {
    MaterialTheme {
      Box {
        val videoPlayerState = rememberVideoPlayerState(
          time = 0L,
          isPlaying = false,
        )
        val time by videoPlayerState.time.collectAsState()
        val isPlaying by videoPlayerState.isPlaying.collectAsState()
        val length by videoPlayerState.length.collectAsState()

        VideoPlayer(
          mrl = myVideo.url,
          state = videoPlayerState,
        )

        Column(
          modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
        ) {
          Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Button(
              onClick = {
                videoPlayerState.seekTo(videoPlayerState.time.value.time - 5000)
              }) {

              Text("Backward")
            }
            Button(
              onClick = {
                // show =  !show
                if (isPlaying) {
                  videoPlayerState.pause()
                } else {
                  videoPlayerState.play()
                }
              }) {

              Text(if (isPlaying) "Pause" else "Play")
            }
            Button(
              onClick = {
                videoPlayerState.seekTo(videoPlayerState.time.value.time + 5000)
              }) {

              Text("Forward")
            }
          }

          Slider(
            value = time.time / length.length.toFloat(),
            onValueChange = { videoPlayerState.seekTo((it * length.length).toLong()) },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}