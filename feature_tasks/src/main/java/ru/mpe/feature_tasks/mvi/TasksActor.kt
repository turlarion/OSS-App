package ru.mpe.feature_tasks.mvi

import io.reactivex.Observable
import kekmech.ru.common_mvi.Actor
import ru.mpei.domain_tasks.TasksRepository

class TasksActor(
        private val tasksRepository: TasksRepository
) : Actor<TasksAction, TasksEvent> {
    override fun execute(action: TasksAction): Observable<TasksEvent> = when(action){
        is TasksAction.LoadTasksList -> tasksRepository.observeTasks()
                .mapEvents(TasksEvent.News::TasksLoaded, TasksEvent.News::TasksLoadError)
    }
}