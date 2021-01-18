package com.logicielhouse.videoplayerclone

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), PermissionListener {

    private val videosList: ArrayList<VideoModel> = ArrayList()
    private lateinit var videosAdapter: VideosAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        videosAdapter = VideosAdapter(this,videosList)

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            setHasFixedSize(true)
            adapter = videosAdapter
        }

        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(this)
            .check()
    }

    private fun displayVideos() {
        var absolutePathThumbnail: String?

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
        val projection =
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.MediaColumns.DATA
            )

        val orderBy = MediaStore.Video.Media.DATE_ADDED

        val cursor: Cursor =
            this.contentResolver.query(collection, projection, null, null, orderBy)!!

        val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val durationColumn =
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                }
                else -> {0}
            }
        //val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

        videosList.clear()
        while (cursor.moveToNext()) {
            absolutePathThumbnail = cursor.getString(columnIndexData)

            val id = cursor.getLong(idColumn)
            val name = cursor.getString(nameColumn)
            val duration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getDuration2(cursor.getInt(durationColumn).toLong())
            } else {
                getDuration(absolutePathThumbnail!!)
            }


            val contentUri: Uri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                id
            )

            /*val thumbnail: Bitmap =
                applicationContext.contentResolver.loadThumbnail(
                    contentUri, Size(640, 480), null)*/

            val e1: String? = contentResolver.getType(contentUri)
            val ex = e1?.split("/")
            val extension = ex?.get(1) ?: "mp4"


            val videoModel =
                VideoModel(
                    contentUri.toString(),
                    contentUri.toString(),
                    absolutePathThumbnail,
                    name,
                    extension,
                    duration
                )

            videosList.add(videoModel)
        }
        cursor.close()
        videosAdapter.notifyDataSetChanged()
        Log.d("TAG", "displayVideos: $videosList")


    }

    private fun getDuration2(duration: Long): String {
        return (SimpleDateFormat("mm:ss")).format(Date(duration))
    }

    private fun getDuration(absolutePathThumbnail: String): String {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, Uri.fromFile(File(absolutePathThumbnail)))
            val time: String? =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeToMills = time?.toLong()
            retriever.release()
            getDuration2(timeToMills ?: 0L)
        } catch (e: Exception) {
            e.printStackTrace()
            "00:00"
        }
    }

    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
        displayVideos()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse) {
        if (response.isPermanentlyDenied) {
            showSettingsDialog()
        }
    }

    override fun onPermissionRationaleShouldBeShown(
        p0: PermissionRequest?,
        token: PermissionToken
    ) {
        token.continuePermissionRequest()
    }

    private fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Need Storage Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton(
            "GOTO SETTINGS"
        ) { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", "com.logicielhouse.videoplayerclone", null)
        )
        startActivity(intent)
    }
}