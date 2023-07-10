package com.example.instagram.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.instagram.R
import com.example.instagram.database.model.ProfileSummary
import com.example.instagram.fragments.PhotoGridFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

private const val PROFILE_SUMMARY_ROW = 0
private const val BUTTON_ROW = 1
private const val VP = 3

class ProfileAdapter(
    val profileSummary: ProfileSummary,
    val OnFollowViewClicked: () -> Unit,
    val OnFollowingViewClicked: () -> Unit,
    val btnConfig: Pair<Pair<String, String>, Pair<() -> Unit, () -> Unit>>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var mContext: Context
    val startBtnListener: () -> Unit = btnConfig.second.first
    val endBtnListener: () -> Unit = btnConfig.second.second
    val btnStartText: String = btnConfig.first.first
    val btnEndText: String = btnConfig.first.second

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mContext = recyclerView.context
        super.onAttachedToRecyclerView(recyclerView)
    }

    inner class ProfileSummaryView(view: View) : RecyclerView.ViewHolder(view) {
        val profilePicture: ImageView = view.findViewById(R.id.profilePic)
        val fullName: TextView = view.findViewById(R.id.profileFullName)
        val bio: TextView = view.findViewById(R.id.profileBio)

        val postCount: TextView = view.findViewById(R.id.postCount)
        val followerCount: TextView = view.findViewById(R.id.followersCount)
        val followingCount: TextView = view.findViewById(R.id.followingCount)

        init {
            followerCount.setOnClickListener { OnFollowViewClicked.invoke() }
            followingCount.setOnClickListener { OnFollowingViewClicked.invoke() }
        }

    }

    inner class Buttons(view: View) : RecyclerView.ViewHolder(view) {
        val btnStart: MaterialButton = view.findViewById(R.id.btnStart)
        val btnEnd: MaterialButton = view.findViewById(R.id.btnEnd)

        init {
            btnStart.text = btnStartText
            btnEnd.text = btnEndText
            btnStart.setOnClickListener { startBtnListener.invoke() }
            btnEnd.setOnClickListener { endBtnListener.invoke() }
        }
    }

    inner class ViewPagerTabLayout(view: View) : RecyclerView.ViewHolder(view) {
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPagerPostAndTagPhoto)
        val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)

        init {
            /*tabLayout.setupWithViewPager(viewPager as ViewPager)*/
            viewPager.adapter = ScreenSlidePagerAdapter(mContext as FragmentActivity)


            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> {
                        tab.icon = mContext.getDrawable(R.drawable.grid)
                    }

                    1 -> {
                        tab.icon = mContext.getDrawable(R.drawable.tag)
                    }
                }
            }.attach()

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PROFILE_SUMMARY_ROW -> ProfileSummaryView(LayoutInflater.from(parent.context).inflate(R.layout.row_profile_overview, parent, false))
            BUTTON_ROW -> Buttons(LayoutInflater.from(parent.context).inflate(R.layout.row_follow_message_btns, parent, false))
            else -> ViewPagerTabLayout(LayoutInflater.from(parent.context).inflate(R.layout.row_profile_post_and_tag_photos, parent, false))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            PROFILE_SUMMARY_ROW -> {
                (holder as ProfileSummaryView).apply {
                    profilePicture.setBackgroundResource(R.drawable.ic_launcher_background)
                    fullName.text = "${profileSummary.f_name} ${profileSummary.l_name}"
                    bio.text = profileSummary.bio
                    postCount.text = profileSummary.postCount.toString()
                    followerCount.text = profileSummary.followerCount.toString()
                    followingCount.text = profileSummary.followingCount.toString()

                }
            }


            BUTTON_ROW -> {
                (holder as Buttons).apply {
                    // already assigned values in init{} inside inner class
                }
            }


            VP -> {
                (holder as ViewPagerTabLayout).apply {

                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> PROFILE_SUMMARY_ROW
            1 -> BUTTON_ROW
            else -> VP
        }
    }
}

private class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return PhotoGridFragment.newInstance(position)
    }
}
