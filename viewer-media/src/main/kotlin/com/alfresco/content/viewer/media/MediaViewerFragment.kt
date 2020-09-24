package com.alfresco.content.viewer.media

import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.Toast
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.ErrorMessageProvider
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import kotlin.math.max

class MediaViewerFragment : BaseMvRxFragment(R.layout.fragment_viewer_media) {

    private val viewModel: MediaViewerViewModel by fragmentViewModel()

    private lateinit var playerView: StyledPlayerView
    private lateinit var dataSourceFactory: DataSource.Factory
    private var player: SimpleExoPlayer? = null
    private var mediaItems: List<MediaItem>? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
    private var lastSeenTrackGroupArray: TrackGroupArray? = null
    private var startAutoPlay = false
    private var startWindow = 0
    private var startPosition: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataSourceFactory = DefaultDataSourceFactory(requireContext().applicationContext)
        playerView = view.findViewById(R.id.player_view)
        // Apply window insets to controls
        val controls = playerView.findViewById<View>(R.id.exo_controller)
        controls.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom
            )
            // Don't consume the insets as it may disrupt the hidden action bar
            insets
        }
        playerView.setErrorMessageProvider(PlayerErrorMessageProvider())
        playerView.requestFocus()

        if (savedInstanceState != null) {
            trackSelectorParameters =
                savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS)
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY)
            startWindow = savedInstanceState.getInt(KEY_WINDOW)
            startPosition = savedInstanceState.getLong(KEY_POSITION)
        } else {
            val builder = ParametersBuilder(requireContext())
            trackSelectorParameters = builder.build()
            clearStartPosition()
        }
    }

    override fun invalidate() {
        // no-op
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
            playerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
            playerView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateTrackSelectorParameters()
        updateStartPosition()
        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters)
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay)
        outState.putInt(KEY_WINDOW, startWindow)
        outState.putLong(KEY_POSITION, startPosition)
    }

    /**
     * @return Whether initialization was successful.
     */
    private fun initializePlayer(): Boolean {
        var player = this.player

        if (player == null) {
            val context = requireContext().applicationContext
            val renderersFactory: RenderersFactory = DefaultRenderersFactory(context)
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            val trackSelector = DefaultTrackSelector(context)
            trackSelector.parameters = trackSelectorParameters!!
            lastSeenTrackGroupArray = null
            player = SimpleExoPlayer.Builder(requireContext(), renderersFactory)
                .setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(trackSelector)
                .build()
            player.addListener(PlayerEventListener())
            player.addAnalyticsListener(EventLogger(trackSelector))
            player.setAudioAttributes(AudioAttributes.DEFAULT, true)
            player.playWhenReady = startAutoPlay
            playerView.player = player
            playerView.setPlaybackPreparer {
                player.prepare()
            }

            this.player = player
            this.trackSelector = trackSelector
        }

        val haveStartPosition = startWindow != C.INDEX_UNSET
        if (haveStartPosition) {
            player.seekTo(startWindow, startPosition)
        }
        player.setMediaItem(createMediaItem())
        player.prepare()

        return true
    }

    private fun createMediaItem(): MediaItem {
        return withState(viewModel) {
            MediaItem.Builder()
                .setUri(it.uri)
                .build()
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            updateTrackSelectorParameters()
            updateStartPosition()
            player?.release()
            player = null
            mediaItems = emptyList()
            trackSelector = null
        }
    }

    private fun updateTrackSelectorParameters() {
        if (trackSelector != null) {
            trackSelectorParameters = trackSelector?.parameters
        }
    }

    private fun updateStartPosition() {
        if (player != null) {
            startAutoPlay = player?.playWhenReady ?: false
            startWindow = player?.currentWindowIndex ?: 0
            startPosition = max(0, player?.contentPosition ?: 0)
        }
    }

    private fun clearStartPosition() {
        startAutoPlay = true
        startWindow = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
    }

    private fun showToast(messageId: Int) {
        showToast(getString(messageId))
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext().applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private inner class PlayerEventListener : Player.EventListener {

        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
            if (trackGroups !== lastSeenTrackGroupArray) {
                val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                        == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS
                    ) {
                        showToast(R.string.error_unsupported_video)
                    }
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                        == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS
                    ) {
                        showToast(R.string.error_unsupported_audio)
                    }
                }
                lastSeenTrackGroupArray = trackGroups
            }
        }
    }

    private inner class PlayerErrorMessageProvider : ErrorMessageProvider<ExoPlaybackException> {
        override fun getErrorMessage(e: ExoPlaybackException): Pair<Int, String> {
            var errorString = getString(R.string.error_generic)
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                val cause = e.rendererException
                if (cause is DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    errorString = if (cause.codecInfo == null) {
                        if (cause.cause is DecoderQueryException) {
                            getString(R.string.error_querying_decoders)
                        } else if (cause.secureDecoderRequired) {
                            getString(
                                R.string.error_no_secure_decoder,
                                cause.mimeType
                            )
                        } else {
                            getString(
                                R.string.error_no_decoder,
                                cause.mimeType
                            )
                        }
                    } else {
                        getString(
                            R.string.error_instantiating_decoder,
                            cause.codecInfo?.name
                        )
                    }
                }
            }
            return Pair.create(0, errorString)
        }
    }

    companion object {
        private const val KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters"
        private const val KEY_WINDOW = "window"
        private const val KEY_POSITION = "position"
        private const val KEY_AUTO_PLAY = "auto_play"
    }
}
