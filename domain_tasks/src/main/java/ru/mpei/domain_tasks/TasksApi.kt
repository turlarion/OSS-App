package ru.mpei.domain_tasks

import io.reactivex.Single
import kekmech.ru.common_network.EmptyRequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface TasksApi {
    @POST("")
    fun get(
            @Body body: EmptyRequestBody = EmptyRequestBody
    ): Single<List<Any>>
}