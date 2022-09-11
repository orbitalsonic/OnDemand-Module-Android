package com.orbitalsonic.ondemandmodule

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide


class ImageAdapter(context: Context,pagesList:Array<String>) : PagerAdapter() {
    private val mContext: Context = context
    private val mWallpapersImagesList = pagesList

    override fun getCount(): Int {
        return mWallpapersImagesList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val imageView = ImageView(mContext)
        imageView.scaleType = ImageView.ScaleType.FIT_XY
//        imageView.setImageResource(mImageIds[position])
        Glide.with(mContext)
            .load(Uri.parse("file:///android_asset/wallpapers/${mWallpapersImagesList[position]}.webp"))
            .placeholder(R.drawable.bg_glide)
            .into(imageView)
        container.addView(imageView, 0)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ImageView)
    }
}