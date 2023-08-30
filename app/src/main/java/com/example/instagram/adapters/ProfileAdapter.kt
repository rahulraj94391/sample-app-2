package com.example.instagram.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.instagram.ImageUtil
import com.example.instagram.R
import com.example.instagram.database.model.ProfileSummary
import com.example.instagram.fragments.PhotoGridFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PROFILE_SUMMARY_ROW = 0
private const val BUTTON_ROW = 1
private const val VP = 3

const val EDIT_PROFILE = "Edit Profile"
const val SHARE_PROFILE = "Share Profile"
const val FOLLOW = "Follow"
const val UNFOLLOW = "Unfollow"
const val MESSAGE = "Message"

class ProfileAdapter(
    private var profileSummary: ProfileSummary,
    private val onFollowViewClicked: () -> Unit,
    private val onFollowingViewClicked: () -> Unit,
    private val onEditProfileClicked: () -> Unit,
    private val onShareProfileClicked: () -> Unit,
    private val onUnfollowClicked: () -> Unit,
    private val onFollowClicked: () -> Unit,
    private val onMessageClicked: () -> Unit,
    private var isFollowing: Boolean,
    private val ownId: Long,
    val userProfileId: Long,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var mContext: Context
    private lateinit var imageUtil: ImageUtil

    private fun btn(btnStart: MaterialButton, btnEnd: MaterialButton) {
        if (ownId == userProfileId) {
            btnStart.text = EDIT_PROFILE
            btnEnd.text = SHARE_PROFILE
            btnStart.setOnClickListener { onEditProfileClicked.invoke() }
            btnEnd.setOnClickListener { onShareProfileClicked.invoke() }
        } else if (isFollowing) {
            btnStart.text = UNFOLLOW
            btnEnd.text = MESSAGE
            btnStart.setOnClickListener {
                onUnfollowClicked.invoke()
                isFollowing = !isFollowing
            }
            btnEnd.setOnClickListener { onMessageClicked.invoke() }
        } else {
            btnStart.text = FOLLOW
            btnEnd.text = MESSAGE
            btnStart.setOnClickListener {
                onFollowClicked.invoke()
                isFollowing = !isFollowing
            }
            btnEnd.setOnClickListener { onMessageClicked.invoke() }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mContext = recyclerView.context
        imageUtil = ImageUtil(mContext)
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
            followerCount.setOnClickListener { onFollowViewClicked.invoke() }
            followingCount.setOnClickListener { onFollowingViewClicked.invoke() }
        }

    }

    inner class Buttons(view: View) : RecyclerView.ViewHolder(view) {
        val btnStart: MaterialButton = view.findViewById(R.id.btnStart)
        val btnEnd: MaterialButton = view.findViewById(R.id.btnEnd)
    }

    inner class ViewPagerTabLayout(view: View) : RecyclerView.ViewHolder(view) {
        private val viewPager: ViewPager2 = view.findViewById(R.id.viewPagerPostAndTagPhoto)
        private val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)

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

                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap: Bitmap = if (profileSummary.profilePicUrl != null)
                            imageUtil.getBitmap(profileSummary.profilePicUrl!!)
                        else
                            ContextCompat.getDrawable(mContext, R.drawable.person_outlined)!!.toBitmap()

                        withContext(Dispatchers.Main) {
                            profilePicture.setImageBitmap(bitmap)
                        }
                    }

                    fullName.text = "${profileSummary.first_name} ${profileSummary.last_name}"
                    bio.text = profileSummary.bio
                    postCount.text = profileSummary.postCount.toString()
                    followerCount.text = profileSummary.followerCount.toString()
                    followingCount.text = profileSummary.followingCount.toString()
                }
            }


            BUTTON_ROW -> {
                (holder as Buttons).apply {
                    // already assigned values in init{} inside inner class
                    btn(btnStart, btnEnd)
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

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return PhotoGridFragment.newInstance(position, userProfileId)
        }
    }
}
