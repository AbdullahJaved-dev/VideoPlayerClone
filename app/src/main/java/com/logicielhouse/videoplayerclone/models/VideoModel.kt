package com.logicielhouse.videoplayerclone.models

import android.graphics.Bitmap

/**
 * Created by Abdullah on 1/18/2021.
 */
data class VideoModel(
    val videoThumb: String,
    val videoUri: String,
    val videoPath: String,
    val videoName: String,
    val videoExtension: String,
    val videoDuration: String
)
