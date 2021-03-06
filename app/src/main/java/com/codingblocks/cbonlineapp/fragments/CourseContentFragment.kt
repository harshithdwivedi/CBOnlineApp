package com.codingblocks.cbonlineapp.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingblocks.cbonlineapp.DownloadStarter
import com.codingblocks.cbonlineapp.R
import com.codingblocks.cbonlineapp.adapters.SectionDetailsAdapter
import com.codingblocks.cbonlineapp.database.AppDatabase
import com.codingblocks.cbonlineapp.database.CourseRun
import com.codingblocks.cbonlineapp.database.CourseSection
import com.codingblocks.cbonlineapp.services.DownloadService
import com.codingblocks.cbonlineapp.utils.getPrefs
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_course_content.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.support.v4.startService


private const val ARG__ATTEMPT_ID = "attempt_id"

class CourseContentFragment : Fragment(), AnkoLogger, DownloadStarter {
    override fun startDownload(url: String, id: String, lectureContentId: String, title: String) {

        startService<DownloadService>("id" to id, "url" to url, "lectureContentId" to lectureContentId, "title" to title)
    }

    lateinit var attemptId: String
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(context!!)
    }

    private val courseDao by lazy {
        database.courseRunDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)
        arguments?.let {
            attemptId = it.getString(ARG__ATTEMPT_ID)!!
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_course_content, container, false)
        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)
        val sectionDao = database.sectionDao()
        val sectionsList = ArrayList<CourseSection>()
        val sectionAdapter = SectionDetailsAdapter(sectionsList, activity!!, this)
        view.rvExpendableView.layoutManager = LinearLayoutManager(context)
        view.rvExpendableView.adapter = sectionAdapter
        view.sectionProgressBar.show()
        sectionDao.getCourseSection(attemptId).observe(this, Observer<List<CourseSection>> {
            if (it.isNotEmpty()) {
                view.sectionProgressBar.hide()
            }
            courseDao.getRunByAtemptId(attemptId).observe(this, Observer<CourseRun>{courseRun ->
                sectionAdapter.setData(it as ArrayList<CourseSection>,courseRun.premium,courseRun.crStart)
            })
        })

        return view
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String) =
                CourseContentFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG__ATTEMPT_ID, param1)
                    }
                }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (view != null) {
                val params = Bundle()
                params.putString(FirebaseAnalytics.Param.ITEM_ID, getPrefs()?.SP_ONEAUTH_ID)
                params.putString(FirebaseAnalytics.Param.ITEM_NAME, "CourseContent")
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, params)
            }
        }
    }


}
