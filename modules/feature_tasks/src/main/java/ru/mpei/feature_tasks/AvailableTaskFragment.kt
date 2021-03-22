package ru.mpei.feature_tasks

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Toast
import kekmech.ru.common_android.close
import kekmech.ru.common_android.closeWithSuccess
import kekmech.ru.common_android.viewbinding.viewBinding
import kekmech.ru.common_mvi.ui.BaseFragment
import kekmech.ru.common_navigation.BackButtonListener
import org.koin.android.ext.android.inject
import ru.mpei.domain_tasks.dto.TakeTaskItem
import ru.mpei.domain_tasks.dto.TasksItem
import ru.mpei.feature_tasks.TasksFragment.Companion.APP_PREFERENCES_FLAG
import ru.mpei.feature_tasks.TasksFragment.Companion.APP_PREFERENCES_ID
import ru.mpei.feature_tasks.databinding.FragmentTaskAvailableBinding
import ru.mpei.feature_tasks.mvi.*
import ru.mpei.feature_tasks.mvi.TasksEvent.Wish

class AvailableTaskFragment : BaseFragment<TasksEvent, TasksEffect, TasksState, TasksFeature>(), BackButtonListener {

    private lateinit var item: TasksItem
    private val mSettings: SharedPreferences by inject()
    private val tasksFeatureFactory: TasksFeatureFactory by inject()
    private val binding by viewBinding(FragmentTaskAvailableBinding::bind)

    override val initEvent: TasksEvent get() = Wish.System.InitTask

    override var layoutId: Int = R.layout.fragment_task_available

    override fun createFeature(): TasksFeature = tasksFeatureFactory.createTask()

    override fun onViewCreatedInternal(view: View, savedInstanceState: Bundle?) {
        super.onViewCreatedInternal(view, savedInstanceState)
        item = arguments?.get("availableTaskData") as TasksItem

        with(binding) {
            fragmentTaskToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            fragmentTaskToolbar.setNavigationOnClickListener { close() }

            fragmentTaskToolbarText.text = item.taskName

            taskNameAvail.text = item.taskName
            balanceAvail.text = item.price
            taskDescriptionAvail.text = if (item.taskDescription.isEmpty()) getString(R.string.no_description) else item.taskDescription
            placeAvail.text = Html.fromHtml(getString(R.string.place, item.location))
            beginDateAvail.text = Html.fromHtml(getString(R.string.begin_date, item.endDate.substring(0, item.endDate.length - 3)))
            endDateAvail.text = Html.fromHtml(getString(R.string.end_date, item.endDate.substring(0, item.endDate.length - 3)))
            refuseDateAvail.text = Html.fromHtml(getString(R.string.refuse_date, item.refuseInfo.substring(0, item.refuseInfo.length - 3)))

            btnTakeTaskAvail.setOnClickListener {
                if ( mSettings.getBoolean(APP_PREFERENCES_FLAG, false) ) {
                    feature.accept(Wish.TakeTask(TakeTaskItem(mSettings.getString(APP_PREFERENCES_ID, "").toString(), item.id.toString())))
                } else {
                    Toast.makeText(context, "Чтобы взять задание сначала необходимо авторизоваться!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun render(state: TasksState) {
        if (state.isLoading){
            binding.btnTakeTaskAvail.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.mpei_blue_dark))
        }
    }

    override fun handleEffect(effect: TasksEffect) {
        when (effect){
            is TasksEffect.TaskTaken -> {
                successfulTake()
            }
            is TasksEffect.ShowError -> {
                Toast.makeText(context, "Ошибка на сервере: " + effect.throwable, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun successfulTake(){
        binding.btnTakeTaskAvail.visibility = View.GONE
        binding.successfulTake.visibility = View.VISIBLE
    }

    override fun onBackPressed(): Boolean {
        closeWithSuccess()
        return true
    }

}