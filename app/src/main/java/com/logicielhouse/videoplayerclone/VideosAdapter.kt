package com.logicielhouse.videoplayerclone

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * Created by Abdullah on 1/18/2021.
 */
class VideosAdapter(private var context: Context, private val videosList: ArrayList<VideoModel>) :
    RecyclerView.Adapter<VideosAdapter.VideosViewHolder>() {
    class VideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vidImage: ImageView = itemView.findViewById(R.id.vid_img)
        val vidName: TextView = itemView.findViewById(R.id.vid_name)
        val vidDuration: TextView = itemView.findViewById(R.id.vid_duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_video_item, parent, false
        )
        return VideosViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {
        holder.apply {
            val item = videosList[position]
            Glide.with(vidImage.context).load(item.videoThumb)
                .error(R.drawable.ic_launcher_background).into(vidImage)

            vidName.text = item.videoName
            vidDuration.text = item.videoDuration

            itemView.setOnClickListener {
                val intent = Intent(context, VideoPlayActivity::class.java)
                intent.putExtra("video", item.videoUri)
                context.startActivity(intent)
            }

        }
    }

    override fun getItemCount(): Int = videosList.size
}