package ru.mpei.domain_tasks

import com.google.gson.Gson
import io.reactivex.Single
import kekmech.ru.common_annotations.EndpointUrl
import kekmech.ru.common_network.EmptyRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.mpei.domain_tasks.dto.TakeTaskItem
import ru.mpei.domain_tasks.dto.TasksItem

@EndpointUrl("http://oss-mpei.ru/")
interface TasksApi {

    @GET("Android/get_tasks.php")
    fun get(@Query("id") id: String, @Query("type") type: String): Single<List<TasksItem>>

    @POST("Android/take_task.php")
    fun take(@Body body: TakeTaskItem): Single<ResponseBody>
}