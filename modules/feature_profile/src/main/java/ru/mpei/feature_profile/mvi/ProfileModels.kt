package ru.mpei.feature_profile.mvi

import kekmech.ru.common_mvi.Feature
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import ru.mpei.domain_profile.dto.*

typealias ProfileFeature = Feature<ProfileState, ProfileEvent, ProfileEffect>

enum class TasksType {
    PROCESS, CHECK, REFUSED, FINISHED
}

enum class ReportType {
    NEW, EDIT
}

data class ProfileState(
    val isLoading: Boolean = false,
    val profileData: ProfileItem = ProfileItem(),
    val isAuthorized: Boolean = false,
    val paramsItem: ParamsItem = ParamsItem(),
    val tasksList: List<TaskItem> = emptyList(),
    val takePhoto: Boolean = false
)

sealed class ProfileEvent{

    sealed class Wish: ProfileEvent(){
        object System{
            object InitLogin: Wish()
            object InitReport: Wish()
            object InitTask: Wish()
        }

        data class Authorization(val id: String, val pass: String): Wish()

        object Authentication: Wish()

        data class ValidateFields(val email:String, val pass: String): Wish()
        object ValidationFailed: Wish()

        object Exit: Wish()

        data class LoadTasks(val type: String): Wish()

        data class ConfirmTask(val body: ConfirmRefuseItem): Wish()
        data class SendReport(val body: ReportItem, val imageBody: MultipartBody.Part?): Wish()

        data class RefuseTask(val body: ConfirmRefuseItem): Wish()

        object AddPhoto: Wish()

        object OpenShop: Wish()
    }

    sealed class News : ProfileEvent() {

        data class Authorized(val profile: ProfileItem): News()
        data class AuthorizationFailed(val throwable: Throwable): News()

        data class Authenticated(val paramsItem: ParamsItem): News()
        data class AuthenticationFailed(val throwable: Throwable): News()

        data class TasksLoaded(val tasksList: List<TaskItem>): News()
        data class TasksLoadFailed( val throwable: Throwable): News()

        data class TaskConfirmed(val obj: ResponseBody): News()
        data class TaskConfirmError(val throwable: Throwable): News()

        data class ReportSent(val obj: ResponseBody): News()
        data class ReportSendError(val throwable: Throwable): News()

        data class TaskRefused(val obj: ResponseBody): News()
        data class TaskRefuseError(val throwable: Throwable): News()
    }

}

sealed class ProfileEffect{
    data class AuthorizationError(val throwable: Throwable): ProfileEffect()
    data class AuthenticationError(val throwable: Throwable): ProfileEffect()

    data class SaveParams(val paramsItem: ParamsItem): ProfileEffect()
    object ClearParams: ProfileEffect()

    data class Validate(val email: String, val pass: String): ProfileEffect()

    data class TasksLoadError(val throwable: Throwable): ProfileEffect()

    object ConfirmSuccess: ProfileEffect()
    data class ConfirmError(val throwable: Throwable): ProfileEffect()

    object ReportSendSuccess: ProfileEffect()
    data class ReportSendError(val throwable: Throwable): ProfileEffect()

    object RefuseSuccess: ProfileEffect()
    data class RefuseError(val throwable: Throwable): ProfileEffect()

    object AddPhoto: ProfileEffect()

    object OpenShop: ProfileEffect()
}

sealed class ProfileAction {
    data class Authorize(val id: String, val pass: String): ProfileAction()
    data class Authenticate(val email: String, val pass: String): ProfileAction()
    data class LoadTasks(val type: String,  val id: String): ProfileAction()
    data class ConfirmTask(val body: ConfirmRefuseItem): ProfileAction()
    data class SendReport(val body: ReportItem, val imageBody: MultipartBody.Part?): ProfileAction()
    data class RefuseTask(val body: ConfirmRefuseItem): ProfileAction()
}